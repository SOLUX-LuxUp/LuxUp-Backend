package com.taptap.backend.team.service;

import com.taptap.backend.team.dto.*;
import com.taptap.backend.team.entity.TeamButton;
import com.taptap.backend.team.entity.TeamButtonUserSetting;
import com.taptap.backend.team.entity.TeamMember;
import com.taptap.backend.team.exception.TeamException;
import com.taptap.backend.team.repository.TeamButtonRepository;
import com.taptap.backend.team.repository.TeamButtonUserSettingRepository;
import com.taptap.backend.team.repository.TeamMemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamButtonPermissionService {

    private final TeamButtonRepository teamButtonRepository;
    private final TeamButtonUserSettingRepository teamButtonUserSettingRepository;
    private final TeamMemberRepository teamMemberRepository;

    public TeamButtonPermissionService(TeamButtonRepository teamButtonRepository,
                                        TeamButtonUserSettingRepository teamButtonUserSettingRepository,
                                        TeamMemberRepository teamMemberRepository) {
        this.teamButtonRepository = teamButtonRepository;
        this.teamButtonUserSettingRepository = teamButtonUserSettingRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @Transactional
    public TapPermissionRequestResponseDto requestPermission(Long userId, Long teamId, Long teamButtonId) {
        requireMembership(teamId, userId);
        TeamButton button = requireButton(teamId, teamButtonId);

        if (!"custom".equals(button.getTapPermission())) {
            throw new TeamException(HttpStatus.BAD_REQUEST, "커스텀 탭 권한 버튼이 아닙니다.");
        }

        TeamButtonUserSetting setting = teamButtonUserSettingRepository
                .findByTeamButtonIdAndUserId(teamButtonId, userId).orElse(null);

        if (setting != null && ("granted".equals(setting.getPermissionStatus()) || "pending".equals(setting.getPermissionStatus()))) {
            throw new TeamException(HttpStatus.CONFLICT, "이미 권한을 보유했거나 요청 중입니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (setting == null) {
            setting = TeamButtonUserSetting.builder()
                    .teamButtonId(teamButtonId)
                    .userId(userId)
                    .hasTapPermission(false)
                    .permissionStatus("pending")
                    .requestedAt(now)
                    .build();
        } else {
            setting.setHasTapPermission(false);
            setting.setPermissionStatus("pending");
            setting.setRequestedAt(now);
            setting.setDecidedBy(null);
        }
        TeamButtonUserSetting saved = teamButtonUserSettingRepository.save(setting);

        return new TapPermissionRequestResponseDto(teamButtonId, userId, saved.getPermissionStatus(), saved.getRequestedAt());
    }

    @Transactional
    public TapPermissionDecisionResponseDto decidePermission(Long approverId, Long teamId, Long teamButtonId, Long targetUserId, TapPermissionDecisionRequestDto request) {
        TeamMember approver = requireMembership(teamId, approverId);
        TeamButton button = requireButton(teamId, teamButtonId);

        boolean isCreator = approver.getUserId().equals(button.getCreatedBy());
        if (!approver.isOwner() && !isCreator) {
            throw new TeamException(HttpStatus.FORBIDDEN, "팀장 또는 버튼 생성자만 처리할 수 있습니다.");
        }

        String action = request == null ? null : request.action();
        if (!"granted".equals(action) && !"denied".equals(action)) {
            throw new TeamException(HttpStatus.BAD_REQUEST, "action 값은 granted 또는 denied여야 합니다.");
        }

        TeamButtonUserSetting setting = teamButtonUserSettingRepository
                .findByTeamButtonIdAndUserId(teamButtonId, targetUserId)
                .orElseThrow(() -> new TeamException(HttpStatus.NOT_FOUND, "존재하지 않는 권한 요청 내역입니다."));

        setting.setPermissionStatus(action);
        setting.setHasTapPermission("granted".equals(action));
        setting.setDecidedBy(approverId);
        TeamButtonUserSetting saved = teamButtonUserSettingRepository.save(setting);

        return new TapPermissionDecisionResponseDto(
                teamButtonId, targetUserId, saved.getPermissionStatus(), saved.getDecidedBy(), saved.getUpdatedAt()
        );
    }

    public List<TapPermissionRequestListItemDto> listPendingRequests(Long userId, Long teamId, Long teamButtonId) {
        requireMembership(teamId, userId);
        requireButton(teamId, teamButtonId);

        return teamButtonUserSettingRepository.findAllByTeamButtonIdAndPermissionStatus(teamButtonId, "pending").stream()
                .map(setting -> {
                    TeamMember member = teamMemberRepository
                            .findByTeamIdAndUserIdAndDeletedAtIsNull(teamId, setting.getUserId()).orElse(null);
                    return new TapPermissionRequestListItemDto(
                            setting.getUserId(),
                            member == null ? null : member.getDisplayName(),
                            member == null ? null : member.getProfileImageUrl(),
                            setting.getRequestedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    private TeamMember requireMembership(Long teamId, Long userId) {
        return teamMemberRepository.findByTeamIdAndUserIdAndDeletedAtIsNull(teamId, userId)
                .orElseThrow(() -> new TeamException(HttpStatus.FORBIDDEN, "팀 미가입 유저입니다."));
    }

    private TeamButton requireButton(Long teamId, Long teamButtonId) {
        return teamButtonRepository.findByTeamButtonIdAndTeamIdAndDeletedAtIsNull(teamButtonId, teamId)
                .orElseThrow(() -> new TeamException(HttpStatus.NOT_FOUND, "존재하지 않는 팀 또는 버튼입니다."));
    }
}
