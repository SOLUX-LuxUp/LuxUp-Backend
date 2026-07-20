package com.taptap.backend.team.service;

import com.taptap.backend.button.entity.Button;
import com.taptap.backend.button.entity.ButtonCategory;
import com.taptap.backend.button.repository.ButtonCategoryRepository;
import com.taptap.backend.button.repository.ButtonRepository;
import com.taptap.backend.record.entity.ButtonRecord;
import com.taptap.backend.record.repository.ButtonRecordRepository;
import com.taptap.backend.team.dto.*;
import com.taptap.backend.team.entity.TeamMember;
import com.taptap.backend.team.entity.TeamMemberButtonSharing;
import com.taptap.backend.team.exception.TeamException;
import com.taptap.backend.team.repository.TeamMemberButtonSharingRepository;
import com.taptap.backend.team.repository.TeamMemberRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TeamMemberProfileService {

    private static final int DEFAULT_RECORD_LIMIT = 30;

    private final TeamMemberRepository teamMemberRepository;
    private final ButtonRepository buttonRepository;
    private final ButtonCategoryRepository buttonCategoryRepository;
    private final ButtonRecordRepository buttonRecordRepository;
    private final TeamMemberButtonSharingRepository teamMemberButtonSharingRepository;

    public TeamMemberProfileService(TeamMemberRepository teamMemberRepository, ButtonRepository buttonRepository,
                                     ButtonCategoryRepository buttonCategoryRepository, ButtonRecordRepository buttonRecordRepository,
                                     TeamMemberButtonSharingRepository teamMemberButtonSharingRepository) {
        this.teamMemberRepository = teamMemberRepository;
        this.buttonRepository = buttonRepository;
        this.buttonCategoryRepository = buttonCategoryRepository;
        this.buttonRecordRepository = buttonRecordRepository;
        this.teamMemberButtonSharingRepository = teamMemberButtonSharingRepository;
    }

    public TeamProfileResponseDto getProfile(Long userId, Long teamId) {
        TeamMember member = requireMembership(teamId, userId);
        List<Button> buttons = buttonRepository.findByUserIdAndIsActiveTrue(userId);
        Map<Long, Boolean> sharedMap = sharedMap(teamId, userId);
        Map<Long, String> categoryNames = categoryNameMap(userId);

        List<TeamProfileButtonItemDto> buttonItems = buttons.stream()
                .map(b -> toProfileButtonItem(b, categoryNames, sharedMap))
                .collect(Collectors.toList());

        return new TeamProfileResponseDto(teamId, userId, member.getDisplayName(), member.getProfileImageUrl(), buttonItems);
    }

    @Transactional
    public UpdateTeamProfileResponseDto updateProfile(Long userId, Long teamId, UpdateTeamProfileRequestDto request) {
        TeamMember member = requireMembership(teamId, userId);

        if (request.displayName() != null && !request.displayName().isBlank()) {
            member.setDisplayName(request.displayName());
        }
        if (request.profileImageUrl() != null) {
            member.setProfileImageUrl(request.profileImageUrl());
        }
        teamMemberRepository.save(member);

        return new UpdateTeamProfileResponseDto(teamId, userId, member.getDisplayName(), member.getProfileImageUrl(), LocalDateTime.now());
    }

    public MemberRecordsResponseDto getMemberRecords(Long requesterId, Long teamId, Long targetUserId, Long cursor, Integer limit) {
        requireMembership(teamId, requesterId);
        TeamMember target = teamMemberRepository.findByTeamIdAndUserIdAndDeletedAtIsNull(teamId, targetUserId)
                .orElseThrow(() -> new TeamException(HttpStatus.NOT_FOUND, "존재하지 않는 팀원입니다."));

        if (!Boolean.TRUE.equals(target.getIsButtonPublic())) {
            throw new TeamException(HttpStatus.FORBIDDEN, "해당 팀원은 버튼 기록을 공개하지 않았습니다.");
        }

        List<Long> sharedButtonIds = teamMemberButtonSharingRepository.findAllByTeamIdAndUserIdAndIsSharedTrue(teamId, targetUserId).stream()
                .map(TeamMemberButtonSharing::getButtonId)
                .collect(Collectors.toList());

        if (sharedButtonIds.isEmpty()) {
            return new MemberRecordsResponseDto(targetUserId, target.getDisplayName(), target.getProfileImageUrl(), false, null, List.of(), List.of());
        }

        Map<Long, Button> buttonMap = buttonRepository.findAllById(sharedButtonIds).stream()
                .collect(Collectors.toMap(Button::getButtonId, b -> b));

        List<MemberRecordButtonItemDto> buttonItems = sharedButtonIds.stream()
                .map(buttonMap::get)
                .filter(b -> b != null)
                .map(b -> new MemberRecordButtonItemDto(b.getButtonId(), b.getButtonName(), b.getIconName(), b.getIconColor()))
                .collect(Collectors.toList());

        int pageSize = (limit == null || limit <= 0) ? DEFAULT_RECORD_LIMIT : limit;
        List<ButtonRecord> records = buttonRecordRepository.findTimelineByButtonIds(sharedButtonIds, cursor, PageRequest.of(0, pageSize + 1));
        boolean hasMore = records.size() > pageSize;
        List<ButtonRecord> page = hasMore ? records.subList(0, pageSize) : records;

        List<MemberRecordTimelineItemDto> timeline = page.stream()
                .map(r -> {
                    Button b = buttonMap.get(r.getButtonId());
                    return new MemberRecordTimelineItemDto(
                            r.getRecordId(), b == null ? null : b.getButtonName(), r.getRecordedAt(), r.getMemo(), r.getEmoji()
                    );
                })
                .collect(Collectors.toList());

        Long nextCursor = hasMore && !page.isEmpty() ? page.get(page.size() - 1).getRecordId() : null;

        return new MemberRecordsResponseDto(targetUserId, target.getDisplayName(), target.getProfileImageUrl(), hasMore, nextCursor, buttonItems, timeline);
    }

    @Transactional
    public UpdateButtonSharingResponseDto updateSharing(Long userId, Long teamId, UpdateButtonSharingRequestDto request) {
        requireMembership(teamId, userId);

        if (request == null || request.buttons() == null || request.buttons().isEmpty()) {
            throw new TeamException(HttpStatus.BAD_REQUEST, "수정할 버튼 목록이 필요합니다.");
        }

        List<Long> buttonIds = request.buttons().stream().map(ButtonSharingUpdateItemDto::buttonId).collect(Collectors.toList());
        Map<Long, Button> buttonMap = buttonRepository.findAllById(buttonIds).stream()
                .collect(Collectors.toMap(Button::getButtonId, b -> b));
        Map<Long, String> categoryNames = categoryNameMap(userId);

        List<TeamProfileButtonItemDto> results = request.buttons().stream()
                .map(item -> {
                    Button button = buttonMap.get(item.buttonId());
                    if (button == null || !button.getUserId().equals(userId)) {
                        throw new TeamException(HttpStatus.FORBIDDEN, "본인 소유의 버튼만 공유 설정을 변경할 수 있습니다.");
                    }
                    TeamMemberButtonSharing sharing = teamMemberButtonSharingRepository
                            .findByTeamIdAndUserIdAndButtonId(teamId, userId, item.buttonId())
                            .orElseGet(() -> TeamMemberButtonSharing.builder()
                                    .teamId(teamId).userId(userId).buttonId(item.buttonId()).build());
                    sharing.setIsShared(Boolean.TRUE.equals(item.isShared()));
                    teamMemberButtonSharingRepository.save(sharing);

                    return toProfileButtonItem(button, categoryNames, Map.of(item.buttonId(), sharing.getIsShared()));
                })
                .collect(Collectors.toList());

        return new UpdateButtonSharingResponseDto(teamId, userId, results);
    }

    // ---- helpers ----

    private TeamProfileButtonItemDto toProfileButtonItem(Button b, Map<Long, String> categoryNames, Map<Long, Boolean> sharedMap) {
        return new TeamProfileButtonItemDto(
                b.getButtonId(), b.getButtonName(), b.getIconName(), b.getIconColor(),
                b.getCategoryId(), b.getCategoryId() == null ? null : categoryNames.get(b.getCategoryId()),
                sharedMap.getOrDefault(b.getButtonId(), false)
        );
    }

    private Map<Long, Boolean> sharedMap(Long teamId, Long userId) {
        return teamMemberButtonSharingRepository.findAllByTeamIdAndUserId(teamId, userId).stream()
                .collect(Collectors.toMap(TeamMemberButtonSharing::getButtonId, TeamMemberButtonSharing::getIsShared));
    }

    private Map<Long, String> categoryNameMap(Long userId) {
        return buttonCategoryRepository.findAllByUserId(userId).stream()
                .collect(Collectors.toMap(ButtonCategory::getCategoryId, ButtonCategory::getCategoryName));
    }

    private TeamMember requireMembership(Long teamId, Long userId) {
        return teamMemberRepository.findByTeamIdAndUserIdAndDeletedAtIsNull(teamId, userId)
                .orElseThrow(() -> new TeamException(HttpStatus.FORBIDDEN, "팀 미가입 유저입니다."));
    }
}
