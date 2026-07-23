package com.taptap.backend.team.service;

import com.taptap.backend.team.dto.*;
import com.taptap.backend.team.entity.*;
import com.taptap.backend.team.exception.TeamException;
import com.taptap.backend.team.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeamButtonService {

    private static final int DEFAULT_TIMELINE_LIMIT = 30;
    private static final int MAX_BUTTON_NAME_LENGTH = 100;

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamButtonRepository teamButtonRepository;
    private final TeamButtonCategoryRepository teamButtonCategoryRepository;
    private final TeamButtonRecordRepository teamButtonRecordRepository;
    private final TeamButtonUserSettingRepository teamButtonUserSettingRepository;

    public TeamButtonService(TeamRepository teamRepository, TeamMemberRepository teamMemberRepository,
                              TeamButtonRepository teamButtonRepository, TeamButtonCategoryRepository teamButtonCategoryRepository,
                              TeamButtonRecordRepository teamButtonRecordRepository, TeamButtonUserSettingRepository teamButtonUserSettingRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.teamButtonRepository = teamButtonRepository;
        this.teamButtonCategoryRepository = teamButtonCategoryRepository;
        this.teamButtonRecordRepository = teamButtonRecordRepository;
        this.teamButtonUserSettingRepository = teamButtonUserSettingRepository;
    }

    @Transactional
    public TeamButtonResponseDto createButton(Long userId, Long teamId, CreateTeamButtonRequestDto request) {
        Team team = requireTeam(teamId);
        TeamMember requester = requireMembership(teamId, userId);
        requirePermission(team.getButtonCreatePermission(), requester);

        if (request.categoryId() != null) {
            requireCategoryInTeam(teamId, request.categoryId());
        }

        String buttonName = isBlank(request.buttonName()) ? "새로운 버튼" : request.buttonName();
        requireValidButtonNameLength(buttonName);

        TeamButton button = TeamButton.builder()
                .teamId(teamId)
                .categoryId(request.categoryId())
                .buttonName(buttonName)
                .iconName(request.iconName())
                .iconColor(request.iconColor())
                .description(request.description())
                .tapPermission(isBlank(request.tapPermission()) ? "all" : request.tapPermission())
                .createdBy(userId)
                .build();
        TeamButton saved = teamButtonRepository.save(button);

        List<Long> allowedUserIds = syncAllowedUsers(saved, request.tapPermission(), request.allowedUserIds());

        return new TeamButtonResponseDto(
                saved.getTeamButtonId(), saved.getTeamId(), saved.getButtonName(), saved.getIconName(),
                saved.getIconColor(), saved.getDescription(), saved.getTapPermission(), saved.getCategoryId(),
                allowedUserIds, saved.getCreatedBy(), saved.getCreatedAt()
        );
    }

    public List<TeamButtonCategoryResponseDto> getCategories(Long userId, Long teamId) {
        requireTeam(teamId);
        requireMembership(teamId, userId);
        return teamButtonCategoryRepository.findAllByTeamIdAndDeletedAtIsNullOrderByDisplayOrderAsc(teamId).stream()
                .map(c -> new TeamButtonCategoryResponseDto(c.getCategoryId(), c.getCategoryName(), c.getCategoryColor(), c.getDisplayOrder()))
                .collect(Collectors.toList());
    }

    public List<TeamButtonListItemDto> listButtons(Long userId, Long teamId) {
        requireTeam(teamId);
        requireMembership(teamId, userId);
        List<TeamButton> buttons = teamButtonRepository.findAllByTeamIdAndIsActiveTrueAndDeletedAtIsNull(teamId);
        Map<Long, String> categoryNames = categoryNameMap(teamId);

        List<TeamButtonListItemDto> items = buttons.stream()
                .map(b -> new AbstractMap.SimpleEntry<>(b, buildLatestSummary(b.getTeamButtonId())))
                .map(entry -> {
                    TeamButton b = entry.getKey();
                    LatestRecordSummaryDto latest = entry.getValue();
                    boolean hasTapPermission = hasTapPermission(b, userId);
                    return new TeamButtonListItemDto(
                            b.getTeamButtonId(), b.getButtonName(), b.getIconName(), b.getIconColor(),
                            b.getCategoryId(), b.getCategoryId() == null ? null : categoryNames.get(b.getCategoryId()),
                            b.getTapPermission(), hasTapPermission, latest
                    );
                })
                .collect(Collectors.toList());

        items.sort((a, c) -> {
            LocalDateTime ta = a.latestRecord() == null ? null : a.latestRecord().recordedAt();
            LocalDateTime tc = c.latestRecord() == null ? null : c.latestRecord().recordedAt();
            if (ta == null && tc == null) return 0;
            if (ta == null) return 1;
            if (tc == null) return -1;
            return tc.compareTo(ta);
        });
        return items;
    }

    public TeamButtonDetailResponseDto getButtonDetail(Long userId, Long teamId, Long teamButtonId) {
        Team team = requireTeam(teamId);
        TeamMember requester = requireMembership(teamId, userId);
        TeamButton button = requireButton(teamId, teamButtonId);
        TeamMember creator = teamMemberRepository.findByTeamIdAndUserIdAndDeletedAtIsNull(teamId, button.getCreatedBy()).orElse(null);
        String categoryName = button.getCategoryId() == null ? null : categoryNameMap(teamId).get(button.getCategoryId());

        boolean canEdit = hasEditPermission(team.getButtonEditPermission(), requester, button.getCreatedBy());
        boolean canDelete = hasEditPermission(team.getButtonDeletePermission(), requester, button.getCreatedBy());
        boolean isTeamOwner = requester.isOwner();

        TeamButtonUserSetting mySetting = teamButtonUserSettingRepository
                .findByTeamButtonIdAndUserId(teamButtonId, userId).orElse(null);
        MyPermissionDto myPermission = new MyPermissionDto(
                hasTapPermission(button, userId),
                mySetting == null ? "granted" : mySetting.getPermissionStatus(),
                mySetting == null || mySetting.getIsEnabled()
        );

        List<Long> allowedUserIds = "custom".equals(button.getTapPermission())
                ? teamButtonUserSettingRepository.findAllByTeamButtonId(teamButtonId).stream()
                    .filter(TeamButtonUserSetting::getHasTapPermission)
                    .map(TeamButtonUserSetting::getUserId)
                    .collect(Collectors.toList())
                : null;

        return new TeamButtonDetailResponseDto(
                button.getTeamButtonId(), button.getTeamId(), button.getButtonName(), button.getIconName(),
                button.getIconColor(), button.getDescription(), button.getTapPermission(), button.getIsActive(),
                creator == null ? null : new MemberProfileDto(creator.getUserId(), creator.getDisplayName(), creator.getProfileImageUrl()),
                myPermission, canEdit, canDelete, isTeamOwner, button.getCategoryId(), categoryName, allowedUserIds,
                buildLatestSummary(teamButtonId), button.getCreatedAt(), button.getUpdatedAt()
        );
    }

    @Transactional
    public UpdateTeamButtonResponseDto updateButton(Long userId, Long teamId, Long teamButtonId, UpdateTeamButtonRequestDto request) {
        Team team = requireTeam(teamId);
        TeamMember requester = requireMembership(teamId, userId);
        TeamButton button = requireButton(teamId, teamButtonId);
        requireEditPermission(team.getButtonEditPermission(), requester, button.getCreatedBy());

        if (request.buttonName() != null && !request.buttonName().isBlank()) {
            requireValidButtonNameLength(request.buttonName());
            button.setButtonName(request.buttonName());
        }
        if (request.iconName() != null) button.setIconName(request.iconName());
        if (request.iconColor() != null) button.setIconColor(request.iconColor());
        if (request.description() != null) button.setDescription(request.description());
        if (request.categoryId() != null) {
            requireCategoryInTeam(teamId, request.categoryId());
            button.setCategoryId(request.categoryId());
        }
        if (request.tapPermission() != null) {
            button.setTapPermission(request.tapPermission());
        }

        TeamButton saved = teamButtonRepository.save(button);
        List<Long> allowedUserIds = null;
        if (request.tapPermission() != null || request.allowedUserIds() != null) {
            allowedUserIds = syncAllowedUsers(saved, saved.getTapPermission(), request.allowedUserIds());
        }

        return new UpdateTeamButtonResponseDto(
                saved.getTeamButtonId(), saved.getButtonName(), saved.getCategoryId(), saved.getIconName(),
                saved.getIconColor(), saved.getDescription(), saved.getTapPermission(), allowedUserIds, saved.getUpdatedAt()
        );
    }

    @Transactional
    public DeleteTeamButtonResponseDto deleteButton(Long userId, Long teamId, Long teamButtonId) {
        Team team = requireTeam(teamId);
        TeamMember requester = requireMembership(teamId, userId);
        TeamButton button = requireButton(teamId, teamButtonId);
        requireEditPermission(team.getButtonDeletePermission(), requester, button.getCreatedBy());

        LocalDateTime now = LocalDateTime.now();
        button.setDeletedAt(now);
        button.setIsActive(false);
        teamButtonRepository.save(button);
        return new DeleteTeamButtonResponseDto(teamButtonId, now);
    }

    @Transactional
    public CreateTeamButtonRecordResponseDto createRecord(Long userId, Long teamId, Long teamButtonId, CreateTeamButtonRecordRequestDto request) {
        requireMembership(teamId, userId);
        TeamButton button = requireButton(teamId, teamButtonId);

        if (!hasTapPermission(button, userId)) {
            throw new TeamException(HttpStatus.FORBIDDEN, "탭 권한이 없습니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        TeamButtonRecord record = TeamButtonRecord.builder()
                .teamId(teamId)
                .teamButtonId(teamButtonId)
                .userId(userId)
                .recordedAt(now)
                .memo(request == null ? null : request.memo())
                .emoji(request == null ? null : request.emoji())
                .build();
        TeamButtonRecord saved = teamButtonRecordRepository.save(record);

        return new CreateTeamButtonRecordResponseDto(
                saved.getRecordId(), saved.getTeamButtonId(), saved.getUserId(), saved.getRecordedAt(), saved.getMemo(), saved.getEmoji()
        );
    }

    public LatestRecordResponseDto getLatestRecord(Long userId, Long teamId, Long teamButtonId) {
        requireMembership(teamId, userId);
        TeamButton button = requireButton(teamId, teamButtonId);

        TeamButtonRecord record = teamButtonRecordRepository
                .findFirstByTeamButtonIdAndDeletedAtIsNullOrderByRecordedAtDesc(teamButtonId).orElse(null);

        TeamButtonTimelineItemDto latest = record == null ? null : new TeamButtonTimelineItemDto(
                record.getRecordId(), record.getRecordedAt(), record.getMemo(), record.getEmoji(), memberProfile(teamId, record.getUserId())
        );
        return new LatestRecordResponseDto(teamButtonId, button.getButtonName(), button.getIconName(), button.getIconColor(), latest);
    }

    public TeamButtonTimelineResponseDto getTimeline(Long userId, Long teamId, Long teamButtonId, Long cursor, Integer limit) {
        requireMembership(teamId, userId);
        requireButton(teamId, teamButtonId);

        int pageSize = (limit == null || limit <= 0) ? DEFAULT_TIMELINE_LIMIT : limit;
        Pageable pageable = PageRequest.of(0, pageSize + 1, Sort.unsorted());
        List<TeamButtonRecord> records = teamButtonRecordRepository.findTimeline(teamButtonId, cursor, pageable);

        boolean hasMore = records.size() > pageSize;
        List<TeamButtonRecord> page = hasMore ? records.subList(0, pageSize) : records;

        List<TeamButtonTimelineItemDto> items = page.stream()
                .map(r -> new TeamButtonTimelineItemDto(r.getRecordId(), r.getRecordedAt(), r.getMemo(), r.getEmoji(), memberProfile(teamId, r.getUserId())))
                .collect(Collectors.toList());

        Long nextCursor = hasMore && !page.isEmpty() ? page.get(page.size() - 1).getRecordId() : null;
        return new TeamButtonTimelineResponseDto(items, hasMore, nextCursor);
    }

    @Transactional
    public UpdateTeamButtonRecordDetailResponseDto updateRecordDetail(
            Long userId, Long teamId, Long teamButtonId, Long recordId, UpdateTeamButtonRecordDetailRequestDto request
    ) {
        requireTeam(teamId);
        requireMembership(teamId, userId);
        requireButton(teamId, teamButtonId);

        if (request.getMemo() == null && request.getEmoji() == null) {
            throw new TeamException(HttpStatus.BAD_REQUEST, "memo, emoji 중 하나는 포함되어야 합니다.");
        }

        TeamButtonRecord record = requireOwnRecord(teamButtonId, recordId, userId);

        if (request.getMemo() != null) {
            record.setMemo(request.getMemo().orElse(null));
        }
        if (request.getEmoji() != null) {
            record.setEmoji(request.getEmoji().orElse(null));
        }
        TeamButtonRecord saved = teamButtonRecordRepository.save(record);

        return new UpdateTeamButtonRecordDetailResponseDto(
                saved.getRecordId(), saved.getTeamButtonId(), saved.getMemo(), saved.getEmoji(), saved.getRecordedAt()
        );
    }

    @Transactional
    public void deleteRecord(Long userId, Long teamId, Long teamButtonId, Long recordId) {
        requireTeam(teamId);
        requireMembership(teamId, userId);
        requireButton(teamId, teamButtonId);

        TeamButtonRecord record = requireOwnRecord(teamButtonId, recordId, userId);
        record.setDeletedAt(LocalDateTime.now());
        teamButtonRecordRepository.save(record);
    }

    @Transactional
    public NotificationToggleResponseDto toggleNotification(Long userId, Long teamId, Long teamButtonId) {
        requireMembership(teamId, userId);
        requireButton(teamId, teamButtonId);

        TeamButtonUserSetting setting = teamButtonUserSettingRepository.findByTeamButtonIdAndUserId(teamButtonId, userId)
                .orElseGet(() -> TeamButtonUserSetting.builder().teamButtonId(teamButtonId).userId(userId).build());
        setting.setIsEnabled(!setting.getIsEnabled());
        TeamButtonUserSetting saved = teamButtonUserSettingRepository.save(setting);
        return new NotificationToggleResponseDto(teamButtonId, userId, saved.getIsEnabled());
    }

    // ---- helpers ----

    private LatestRecordSummaryDto buildLatestSummary(Long teamButtonId) {
        TeamButtonRecord latest = teamButtonRecordRepository
                .findFirstByTeamButtonIdAndDeletedAtIsNullOrderByRecordedAtDesc(teamButtonId).orElse(null);
        if (latest == null) {
            return null;
        }
        // 최근 24시간 내 기록한 사람 중 최신순 상위 3명 (recordedByCount로 +N 표시)
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<TeamButtonRecord> recent = teamButtonRecordRepository
                .findTimeline(teamButtonId, null, PageRequest.of(0, 100))
                .stream()
                .filter(r -> r.getRecordedAt().isAfter(since))
                .collect(Collectors.toList());

        LinkedHashSet<Long> orderedUserIds = new LinkedHashSet<>();
        for (TeamButtonRecord r : recent) {
            orderedUserIds.add(r.getUserId());
        }
        Long teamId = latest.getTeamId();
        List<MemberProfileDto> recordedBy = orderedUserIds.stream()
                .limit(3)
                .map(uid -> memberProfile(teamId, uid))
                .collect(Collectors.toList());

        return new LatestRecordSummaryDto(latest.getRecordedAt(), recordedBy, orderedUserIds.size());
    }

    private List<Long> syncAllowedUsers(TeamButton button, String tapPermission, List<Long> allowedUserIds) {
        if (!"custom".equals(tapPermission)) {
            return null;
        }
        teamButtonUserSettingRepository.deleteAllByTeamButtonId(button.getTeamButtonId());
        if (allowedUserIds == null || allowedUserIds.isEmpty()) {
            return List.of();
        }
        List<TeamButtonUserSetting> settings = allowedUserIds.stream()
                .map(uid -> TeamButtonUserSetting.builder()
                        .teamButtonId(button.getTeamButtonId())
                        .userId(uid)
                        .hasTapPermission(true)
                        .permissionStatus("granted")
                        .build())
                .collect(Collectors.toList());
        teamButtonUserSettingRepository.saveAll(settings);
        return allowedUserIds;
    }

    private boolean hasTapPermission(TeamButton button, Long userId) {
        return switch (button.getTapPermission()) {
            case "custom" -> teamButtonUserSettingRepository.findByTeamButtonIdAndUserId(button.getTeamButtonId(), userId)
                    .map(s -> s.getHasTapPermission() && "granted".equals(s.getPermissionStatus())).orElse(false);
            default -> true; // all
        };
    }

    private void requirePermission(String permission, TeamMember requester) {
        if ("leader_only".equals(permission) && !requester.isOwner()) {
            throw new TeamException(HttpStatus.FORBIDDEN, "버튼 생성 권한이 없습니다.");
        }
    }

    private void requireEditPermission(String permission, TeamMember requester, Long createdBy) {
        if (!hasEditPermission(permission, requester, createdBy)) {
            throw new TeamException(HttpStatus.FORBIDDEN, "해당 작업에 대한 권한이 없습니다.");
        }
    }

    private boolean hasEditPermission(String permission, TeamMember requester, Long createdBy) {
        if (requester.isOwner()) {
            return true;
        }
        return "creator_or_leader".equals(permission) && requester.getUserId().equals(createdBy);
    }

    private void requireValidButtonNameLength(String buttonName) {
        if (buttonName.length() > MAX_BUTTON_NAME_LENGTH) {
            throw new TeamException(HttpStatus.BAD_REQUEST, "버튼 이름은 최대 " + MAX_BUTTON_NAME_LENGTH + "자까지 입력 가능합니다.");
        }
    }

    private void requireCategoryInTeam(Long teamId, Long categoryId) {
        boolean exists = teamButtonCategoryRepository.findAllByTeamIdAndDeletedAtIsNullOrderByDisplayOrderAsc(teamId)
                .stream().anyMatch(c -> c.getCategoryId().equals(categoryId));
        if (!exists) {
            throw new TeamException(HttpStatus.BAD_REQUEST, "존재하지 않는 카테고리입니다.");
        }
    }

    private Map<Long, String> categoryNameMap(Long teamId) {
        return teamButtonCategoryRepository.findAllByTeamIdAndDeletedAtIsNullOrderByDisplayOrderAsc(teamId).stream()
                .collect(Collectors.toMap(TeamButtonCategory::getCategoryId, TeamButtonCategory::getCategoryName));
    }

    private MemberProfileDto memberProfile(Long teamId, Long userId) {
        return teamMemberRepository.findByTeamIdAndUserIdAndDeletedAtIsNull(teamId, userId)
                .map(m -> new MemberProfileDto(m.getUserId(), m.getDisplayName(), m.getProfileImageUrl()))
                .orElse(new MemberProfileDto(userId, null, null));
    }

    private TeamMember requireMembership(Long teamId, Long userId) {
        return teamMemberRepository.findByTeamIdAndUserIdAndDeletedAtIsNull(teamId, userId)
                .orElseThrow(() -> new TeamException(HttpStatus.FORBIDDEN, "팀 미가입 유저입니다."));
    }

    private Team requireTeam(Long teamId) {
        return teamRepository.findByTeamIdAndDeletedAtIsNull(teamId)
                .orElseThrow(() -> new TeamException(HttpStatus.NOT_FOUND, "존재하지 않는 팀입니다."));
    }

    private TeamButton requireButton(Long teamId, Long teamButtonId) {
        return teamButtonRepository.findByTeamButtonIdAndTeamIdAndDeletedAtIsNull(teamButtonId, teamId)
                .orElseThrow(() -> new TeamException(HttpStatus.NOT_FOUND, "존재하지 않는 팀 또는 버튼입니다."));
    }

    // 기록 수정/삭제 - 본인 기록만 가능 (개인 버튼의 findOwnedRecord와 동일한 원칙)
    private TeamButtonRecord requireOwnRecord(Long teamButtonId, Long recordId, Long userId) {
        TeamButtonRecord record = teamButtonRecordRepository.findByRecordIdAndTeamButtonIdAndDeletedAtIsNull(recordId, teamButtonId)
                .orElseThrow(() -> new TeamException(HttpStatus.NOT_FOUND, "존재하지 않는 기록입니다."));
        if (!record.getUserId().equals(userId)) {
            throw new TeamException(HttpStatus.FORBIDDEN, "본인 기록이 아닙니다.");
        }
        return record;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
