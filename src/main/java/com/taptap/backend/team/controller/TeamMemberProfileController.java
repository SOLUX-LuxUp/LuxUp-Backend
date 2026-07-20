package com.taptap.backend.team.controller;

import com.taptap.backend.config.ApiResponse;
import com.taptap.backend.team.dto.*;
import com.taptap.backend.team.service.TeamMemberProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/teams/{team_id}/members")
public class TeamMemberProfileController {

    private final TeamMemberProfileService teamMemberProfileService;

    public TeamMemberProfileController(TeamMemberProfileService teamMemberProfileService) {
        this.teamMemberProfileService = teamMemberProfileService;
    }

    @GetMapping("/profile")
    public ApiResponse<TeamProfileResponseDto> getProfile(
            Authentication authentication,
            @PathVariable("team_id") Long teamId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("팀 내 프로필 조회에 성공했습니다.", teamMemberProfileService.getProfile(userId, teamId));
    }

    @PatchMapping("/profile")
    public ApiResponse<UpdateTeamProfileResponseDto> updateProfile(
            Authentication authentication,
            @PathVariable("team_id") Long teamId,
            @RequestBody UpdateTeamProfileRequestDto request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("팀 내 프로필이 수정되었습니다.", teamMemberProfileService.updateProfile(userId, teamId, request));
    }

    @GetMapping("/{user_id}/records")
    public ApiResponse<MemberRecordsResponseDto> getMemberRecords(
            Authentication authentication,
            @PathVariable("team_id") Long teamId,
            @PathVariable("user_id") Long targetUserId,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("팀원 버튼 기록 조회에 성공했습니다.",
                teamMemberProfileService.getMemberRecords(userId, teamId, targetUserId, cursor, limit));
    }

    @PatchMapping("/me/sharing")
    public ApiResponse<UpdateButtonSharingResponseDto> updateSharing(
            Authentication authentication,
            @PathVariable("team_id") Long teamId,
            @RequestBody UpdateButtonSharingRequestDto request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("버튼 공유 설정이 수정되었습니다.", teamMemberProfileService.updateSharing(userId, teamId, request));
    }
}
