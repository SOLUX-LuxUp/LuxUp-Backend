package com.taptap.backend.team.service;

import com.taptap.backend.team.dto.*;
import com.taptap.backend.team.entity.Team;
import com.taptap.backend.team.entity.TeamMember;
import com.taptap.backend.team.exception.TeamException;
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
import java.util.stream.Collectors;

@Service
public class TeamService {

    private static final String INVITE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    public TeamService(TeamRepository teamRepository, TeamMemberRepository teamMemberRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TeamResponseDto createTeam(Long userId, CreateTeamRequestDto request) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new TeamException(HttpStatus.UNAUTHORIZED, "유효하지 않은 사용자입니다."));

        String teamName = (request.teamName() == null || request.teamName().isBlank())
                ? defaultTeamName(userId)
                : request.teamName();

        Integer maxMember = request.maxMember() == null ? 10 : request.maxMember();
        // ⚠️ 디자인 상 선택 가능 값이 5/10/15로 보임. 확정되면 아래 유효성 검사를 enum 체크로 교체할 것.
        if (maxMember < 2 || maxMember > 100) {
            throw new TeamException(HttpStatus.BAD_REQUEST, "팀 최대 인원 값이 올바르지 않습니다.");
        }

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
        requireOwner(teamId, userId);
        Team team = findActiveTeam(teamId);
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
                ownerUserId
        );
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
        // TODO: deletedAt + 3일 시점에 하드 삭제(및 관련 팀 버튼/기록 정리)하는 배치/스케줄러 별도 구현 필요
        return new DeleteTeamResponseDto(teamId, now, now.plusDays(3));
    }

    public List<TeamMemberListItemDto> listMembers(Long userId, Long teamId) {
        if (!teamMemberRepository.existsByTeamIdAndUserIdAndDeletedAtIsNull(teamId, userId)) {
            throw new TeamException(HttpStatus.FORBIDDEN, "팀 미가입 유저입니다.");
        }
        List<TeamMember> members = teamMemberRepository.findAllByTeamIdAndDeletedAtIsNullOrderByJoinedAtAsc(teamId);
        return members.stream()
                .map(m -> new TeamMemberListItemDto(m.getUserId(), m.getDisplayName(), m.getProfileImageUrl(), m.getRole(), m.getJoinedAt()))
                .collect(Collectors.toList());
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

    private Team findActiveTeam(Long teamId) {
        return teamRepository.findByTeamIdAndDeletedAtIsNull(teamId)
                .orElseThrow(() -> new TeamException(HttpStatus.NOT_FOUND, "존재하지 않는 팀입니다."));
    }

    private TeamListItemDto toListItem(Team team, Long requestingUserId) {
        List<TeamMember> members = teamMemberRepository.findAllByTeamIdAndDeletedAtIsNullOrderByJoinedAtAsc(team.getTeamId());

        boolean isFavorite = members.stream()
                .filter(m -> m.getUserId().equals(requestingUserId))
                .findFirst()
                .map(TeamMember::getIsFavorite)
                .orElse(false);

        List<MemberProfileDto> profiles = members.stream()
                .map(m -> new MemberProfileDto(m.getUserId(), m.getDisplayName(), m.getProfileImageUrl()))
                .collect(Collectors.toList());

        return new TeamListItemDto(
                team.getTeamId(),
                team.getTeamName(),
                team.getTeamImageUrl(),
                team.getIconName(),
                team.getIconColor(),
                isFavorite,
                team.getMaxMember(),
                (long) members.size(),
                profiles,
                team.getUpdatedAt()
        );
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
