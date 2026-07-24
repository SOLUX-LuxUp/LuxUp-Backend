package com.taptap.backend.team.service;

import com.taptap.backend.team.dto.*;
import com.taptap.backend.team.entity.Team;
import com.taptap.backend.team.entity.TeamMember;
import com.taptap.backend.team.entity.TeamButton;
import com.taptap.backend.team.entity.TeamButtonRecord;
import com.taptap.backend.team.exception.TeamException;
import com.taptap.backend.team.repository.TeamButtonRecordRepository;
import com.taptap.backend.team.repository.TeamButtonRepository;
import com.taptap.backend.team.repository.TeamMemberRepository;
import com.taptap.backend.team.repository.TeamRepository;
import com.taptap.backend.user.entity.User;
import com.taptap.backend.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TeamService {

    private static final String INVITE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final TeamButtonRecordRepository teamButtonRecordRepository;
    private final TeamButtonRepository teamButtonRepository;

    public TeamService(TeamRepository teamRepository, TeamMemberRepository teamMemberRepository, UserRepository userRepository,
                        TeamButtonRecordRepository teamButtonRecordRepository, TeamButtonRepository teamButtonRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
        this.teamButtonRecordRepository = teamButtonRecordRepository;
        this.teamButtonRepository = teamButtonRepository;
    }

    @Transactional
    public TeamResponseDto createTeam(Long userId, CreateTeamRequestDto request) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new TeamException(HttpStatus.UNAUTHORIZED, "유효하지 않은 사용자입니다."));

        String teamName = (request.teamName() == null || request.teamName().isBlank())
                ? defaultTeamName(userId)
                : request.teamName();

        Integer maxMember = request.maxMember() == null ? 10 : request.maxMember();
        requireValidMaxMember(maxMember);

        Team team = Team.builder()
                .teamName(teamName)
                .teamImageUrl(request.teamImageUrl())
                .iconName(request.iconName())
                .iconColor(request.iconColor())
                .inviteCode(generateUniqueInviteCode())
                .maxMember(maxMember)
                .createdBy(userId)
                .build();
        Team saved = teamRepository.save(team);

        TeamMember owner = TeamMember.builder()
                .teamId(saved.getTeamId())
                .userId(userId)
                .displayName(creator.getUsername())
                .profileImageUrl(creator.getProfileImageUrl())
                .role("owner")
                .build();
        teamMemberRepository.save(owner);

        return new TeamResponseDto(
                saved.getTeamId(),
                saved.getTeamName(),
                saved.getTeamImageUrl(),
                saved.getIconName(),
                saved.getIconColor(),
                saved.getInviteCode(),
                saved.getMaxMember(),
                userId,
                saved.getCreatedAt()
        );
    }

    public List<TeamListItemDto> listTeams(Long userId) {
        List<Long> teamIds = teamMemberRepository.findTeamIdsByUserId(userId);
        if (teamIds.isEmpty()) {
            return List.of();
        }

        List<Team> teams = teamRepository.findAllById(teamIds);

        return teams.stream()
                .map(team -> toListItem(team, userId))
                .sorted(
                        Comparator.comparing(TeamListItemDto::isFavorite, Comparator.reverseOrder())
                                .thenComparing(TeamListItemDto::updatedAt, Comparator.reverseOrder())
                )
                .collect(Collectors.toList());
    }

    @Transactional
    public JoinTeamResponseDto joinTeam(Long userId, JoinTeamRequestDto request) {
        if (request.inviteCode() == null || request.inviteCode().isBlank()) {
            throw new TeamException(HttpStatus.BAD_REQUEST, "초대코드는 필수입니다.");
        }

        Team team = teamRepository.findByInviteCodeAndDeletedAtIsNull(request.inviteCode())
                .orElseThrow(() -> new TeamException(HttpStatus.NOT_FOUND, "존재하지 않는 초대코드입니다."));

        if (teamMemberRepository.existsByTeamIdAndUserIdAndDeletedAtIsNull(team.getTeamId(), userId)) {
            throw new TeamException(HttpStatus.CONFLICT, "이미 가입된 팀입니다.");
        }

        long currentMemberCount = teamMemberRepository.countByTeamIdAndDeletedAtIsNull(team.getTeamId());
        if (currentMemberCount >= team.getMaxMember()) {
            throw new TeamException(HttpStatus.FORBIDDEN, "팀 최대 인원을 초과했습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TeamException(HttpStatus.UNAUTHORIZED, "유효하지 않은 사용자입니다."));

        TeamMember member = TeamMember.builder()
                .teamId(team.getTeamId())
                .userId(userId)
                .displayName(user.getUsername())
                .profileImageUrl(user.getProfileImageUrl())
                .role("member")
                .build();
        TeamMember saved = teamMemberRepository.save(member);

        return new JoinTeamResponseDto(team.getTeamId(), team.getTeamName(), team.getTeamImageUrl(), saved.getJoinedAt());
    }

    public TeamSettingsResponseDto getSettings(Long userId, Long teamId) {
        Team team = findActiveTeam(teamId);
        TeamMember requester = requireMembership(teamId, userId);
        long memberCount = teamMemberRepository.countByTeamIdAndDeletedAtIsNull(teamId);
        Long ownerUserId = teamMemberRepository.findOwnerByTeamId(teamId)
                .map(TeamMember::getUserId)
                .orElse(team.getCreatedBy());

        return new TeamSettingsResponseDto(
                team.getTeamId(),
                team.getTeamName(),
                team.getTeamImageUrl(),
                team.getIconName(),
                team.getIconColor(),
                team.getInviteCode(),
                team.getMaxMember(),
                memberCount,
                team.getButtonCreatePermission(),
                team.getButtonEditPermission(),
                team.getButtonDeletePermission(),
                ownerUserId,
                requester.getIsNotification()
        );
    }

    // 팀 알림 설정 — 요청자(본인) 기준 팀 전체 알림 on/off 토글 (team_member.is_notification 사용)
    @Transactional
    public TeamNotificationToggleResponseDto toggleNotification(Long userId, Long teamId) {
        findActiveTeam(teamId);
        TeamMember member = requireMembership(teamId, userId);
        member.setIsNotification(!Boolean.TRUE.equals(member.getIsNotification()));
        teamMemberRepository.save(member);
        return new TeamNotificationToggleResponseDto(teamId, userId, member.getIsNotification());
    }

    @Transactional
    public UpdateTeamSettingsResponseDto updateSettings(Long userId, Long teamId, UpdateTeamSettingsRequestDto request) {
        requireOwner(teamId, userId);
        Team team = findActiveTeam(teamId);

        if (request.teamName() != null && !request.teamName().isBlank()) {
            team.setTeamName(request.teamName());
        }
        if (request.teamImageUrl() != null) {
            team.setTeamImageUrl(request.teamImageUrl());
        }
        if (request.iconName() != null) {
            team.setIconName(request.iconName());
        }
        if (request.iconColor() != null) {
            team.setIconColor(request.iconColor());
        }
        if (request.maxMember() != null) {
            requireValidMaxMember(request.maxMember());
            long currentMemberCount = teamMemberRepository.countByTeamIdAndDeletedAtIsNull(teamId);
            if (request.maxMember() < currentMemberCount) {
                throw new TeamException(HttpStatus.BAD_REQUEST, "최대 인원은 현재 팀원 수보다 작을 수 없습니다.");
            }
            team.setMaxMember(request.maxMember());
        }
        if (request.buttonCreatePermission() != null) {
            team.setButtonCreatePermission(request.buttonCreatePermission());
        }
        if (request.buttonEditPermission() != null) {
            team.setButtonEditPermission(request.buttonEditPermission());
        }
        if (request.buttonDeletePermission() != null) {
            team.setButtonDeletePermission(request.buttonDeletePermission());
        }

        if (request.newOwnerUserId() != null) {
            transferOwnership(teamId, userId, request.newOwnerUserId());
        }

        Team saved = teamRepository.save(team);
        return new UpdateTeamSettingsResponseDto(saved.getTeamId(), saved.getTeamName(), saved.getUpdatedAt());
    }

    @Transactional
    public FavoriteResponseDto toggleFavorite(Long userId, Long teamId) {
        TeamMember member = teamMemberRepository.findByTeamIdAndUserIdAndDeletedAtIsNull(teamId, userId)
                .orElseThrow(() -> new TeamException(HttpStatus.NOT_FOUND, "존재하지 않는 팀 또는 미가입 팀입니다."));
        member.setIsFavorite(!member.getIsFavorite());
        teamMemberRepository.save(member);
        return new FavoriteResponseDto(teamId, member.getIsFavorite());
    }

    @Transactional
    public LeaveTeamResponseDto leaveTeam(Long userId, Long teamId) {
        TeamMember member = teamMemberRepository.findByTeamIdAndUserIdAndDeletedAtIsNull(teamId, userId)
                .orElseThrow(() -> new TeamException(HttpStatus.NOT_FOUND, "존재하지 않는 팀 또는 미가입 팀입니다."));

        if (member.isOwner()) {
            throw new TeamException(HttpStatus.FORBIDDEN, "팀장은 팀원에게 팀장 위임 후 탈퇴 가능합니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        member.setDeletedAt(now);
        teamMemberRepository.save(member);
        return new LeaveTeamResponseDto(teamId, userId, now);
    }

    @Transactional
    public KickMemberResponseDto kickMember(Long requesterId, Long teamId, Long targetUserId) {
        requireOwner(teamId, requesterId);

        if (requesterId.equals(targetUserId)) {
            throw new TeamException(HttpStatus.BAD_REQUEST, "본인을 강제 추방할 수 없습니다. 팀 탈퇴를 이용해주세요.");
        }

        TeamMember target = teamMemberRepository.findByTeamIdAndUserIdAndDeletedAtIsNull(teamId, targetUserId)
                .orElseThrow(() -> new TeamException(HttpStatus.NOT_FOUND, "존재하지 않는 팀 또는 미가입 팀원입니다."));

        LocalDateTime now = LocalDateTime.now();
        target.setDeletedAt(now);
        teamMemberRepository.save(target);
        return new KickMemberResponseDto(teamId, targetUserId, now);
    }

    @Transactional
    public DeleteTeamResponseDto deleteTeam(Long userId, Long teamId) {
        requireOwner(teamId, userId);
        Team team = findActiveTeam(teamId);

        LocalDateTime now = LocalDateTime.now();
        team.setDeletedAt(now);
        teamRepository.save(team);
        // 실제 하드 삭제(및 관련 팀 버튼/카테고리/기록/공유설정 정리)는 TeamHardDeletionScheduler가
        // deletedAt + 3일 시점에 매 정시 배치로 수행함
        return new DeleteTeamResponseDto(teamId, now, now.plusDays(3));
    }

    public List<TeamMemberListItemDto> listMembers(Long userId, Long teamId) {
        findActiveTeam(teamId);
        if (!teamMemberRepository.existsByTeamIdAndUserIdAndDeletedAtIsNull(teamId, userId)) {
            throw new TeamException(HttpStatus.FORBIDDEN, "팀 미가입 유저입니다.");
        }
        List<TeamMember> members = teamMemberRepository.findAllByTeamIdAndDeletedAtIsNullOrderByJoinedAtAsc(teamId);
        return members.stream()
                .map(m -> new TeamMemberListItemDto(
                        m.getUserId(), m.getDisplayName(), m.getProfileImageUrl(), m.getRole(), m.getJoinedAt(),
                        latestRecordOf(teamId, m.getUserId())
                ))
                .collect(Collectors.toList());
    }

    private MemberLatestRecordDto latestRecordOf(Long teamId, Long userId) {
        return teamButtonRecordRepository.findFirstByTeamIdAndUserIdAndDeletedAtIsNullOrderByRecordedAtDesc(teamId, userId)
                .map(r -> new MemberLatestRecordDto(buttonNameOf(r), r.getRecordedAt()))
                .orElse(null);
    }

    private String buttonNameOf(TeamButtonRecord record) {
        return teamButtonRepository.findById(record.getTeamButtonId())
                .map(TeamButton::getButtonName)
                .orElse(null);
    }

    private void transferOwnership(Long teamId, Long currentOwnerId, Long newOwnerUserId) {
        TeamMember currentOwner = teamMemberRepository.findByTeamIdAndUserIdAndDeletedAtIsNull(teamId, currentOwnerId)
                .orElseThrow(() -> new TeamException(HttpStatus.NOT_FOUND, "존재하지 않는 팀원입니다."));
        TeamMember newOwner = teamMemberRepository.findByTeamIdAndUserIdAndDeletedAtIsNull(teamId, newOwnerUserId)
                .orElseThrow(() -> new TeamException(HttpStatus.BAD_REQUEST, "newOwnerUserId가 팀 소속 멤버가 아닙니다."));

        currentOwner.setRole("member");
        newOwner.setRole("owner");
        teamMemberRepository.save(currentOwner);
        teamMemberRepository.save(newOwner);
    }

    private TeamMember requireOwner(Long teamId, Long userId) {
        TeamMember member = teamMemberRepository.findByTeamIdAndUserIdAndDeletedAtIsNull(teamId, userId)
                .orElseThrow(() -> new TeamException(HttpStatus.NOT_FOUND, "존재하지 않는 팀 또는 미가입 팀입니다."));
        if (!member.isOwner()) {
            throw new TeamException(HttpStatus.FORBIDDEN, "팀장 권한이 없습니다.");
        }
        return member;
    }

    // GET /settings, 팀 알림 설정 등 팀장 여부와 무관하게 "가입한 멤버"이기만 하면 되는 액션에 사용
    private TeamMember requireMembership(Long teamId, Long userId) {
        return teamMemberRepository.findByTeamIdAndUserIdAndDeletedAtIsNull(teamId, userId)
                .orElseThrow(() -> new TeamException(HttpStatus.FORBIDDEN, "팀 미가입 유저입니다."));
    }

    private Team findActiveTeam(Long teamId) {
        return teamRepository.findByTeamIdAndDeletedAtIsNull(teamId)
                .orElseThrow(() -> new TeamException(HttpStatus.NOT_FOUND, "존재하지 않는 팀입니다."));
    }

    private TeamListItemDto toListItem(Team team, Long requestingUserId) {
        List<TeamMember> members = teamMemberRepository.findAllByTeamIdAndDeletedAtIsNullOrderByJoinedAtAsc(team.getTeamId());

        TeamMember requestingMember = members.stream()
                .filter(m -> m.getUserId().equals(requestingUserId))
                .findFirst()
                .orElse(null);

        boolean isFavorite = requestingMember != null && Boolean.TRUE.equals(requestingMember.getIsFavorite());
        boolean isOwner = requestingMember != null && requestingMember.isOwner();

        List<MemberProfileDto> profiles = members.stream()
                .map(m -> new MemberProfileDto(m.getUserId(), m.getDisplayName(), m.getProfileImageUrl()))
                .collect(Collectors.toList());

        Map<Long, TeamMember> memberByUserId = members.stream()
                .collect(Collectors.toMap(TeamMember::getUserId, m -> m, (a, b) -> a));
        List<TeamButtonRecord> recentRecords = teamButtonRecordRepository.findTop20ByTeamIdAndDeletedAtIsNullOrderByRecordedAtDesc(team.getTeamId());

        TeamLatestRecordDto latestRecord = recentRecords.stream()
                .findFirst()
                .map(r -> {
                    TeamButton b = teamButtonRepository.findById(r.getTeamButtonId()).orElse(null);
                    return new TeamLatestRecordDto(
                            r.getTeamButtonId(), b == null ? null : b.getButtonName(),
                            b == null ? null : b.getIconName(), b == null ? null : b.getIconColor(),
                            r.getRecordedAt()
                    );
                })
                .orElse(null);

        List<MemberProfileDto> recentUpdatedMembers = recentRecords.stream()
                .map(TeamButtonRecord::getUserId)
                .distinct()
                .limit(3)
                .map(uid -> {
                    TeamMember m = memberByUserId.get(uid);
                    return m == null ? new MemberProfileDto(uid, null, null)
                            : new MemberProfileDto(m.getUserId(), m.getDisplayName(), m.getProfileImageUrl());
                })
                .collect(Collectors.toList());

        return new TeamListItemDto(
                team.getTeamId(),
                team.getTeamName(),
                team.getTeamImageUrl(),
                team.getIconName(),
                team.getIconColor(),
                isFavorite,
                isOwner,
                team.getMaxMember(),
                (long) members.size(),
                profiles,
                latestRecord,
                recentUpdatedMembers,
                team.getDeletedAt() != null,
                team.getScheduledDeletionAt(),
                team.getUpdatedAt()
        );
    }

    // 팀 최대 인원: 5단위 스텝, 상한 30 (기획 확정: 하연/누리 논의 결과)
    private static final java.util.Set<Integer> VALID_MAX_MEMBER_VALUES = java.util.Set.of(5, 10, 15, 20, 25, 30);

    private void requireValidMaxMember(Integer maxMember) {
        if (!VALID_MAX_MEMBER_VALUES.contains(maxMember)) {
            throw new TeamException(HttpStatus.BAD_REQUEST, "팀 최대 인원은 5명 단위로 5~30명 사이여야 합니다.");
        }
    }

    private String defaultTeamName(Long userId) {
        long existingCount = teamRepository.count(); // TODO: createdBy 기준으로 좁히는 카운트 쿼리로 교체 권장
        return "새로운 팀 " + (existingCount + 1);
    }

    private String generateUniqueInviteCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(INVITE_CODE_LENGTH);
            for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
                sb.append(INVITE_CODE_CHARS.charAt(random.nextInt(INVITE_CODE_CHARS.length())));
            }
            code = sb.toString();
        } while (teamRepository.existsByInviteCode(code));
        return code;
    }
}
