package com.taptap.backend.team.controller;

import com.taptap.backend.config.ApiResponse;
import com.taptap.backend.team.dto.*;
import com.taptap.backend.team.service.TeamButtonPermissionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/teams/{team_id}/buttons/{team_button_id}/permission")
public class TeamButtonPermissionController {

    private final TeamButtonPermissionService teamButtonPermissionService;

    public TeamButtonPermissionController(TeamButtonPermissionService teamButtonPermissionService) {
        this.teamButtonPermissionService = teamButtonPermissionService;
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<TapPermissionRequestResponseDto>> requestPermission(
            Authentication authentication,
            @PathVariable("team_id") Long teamId,
            @PathVariable("team_button_id") Long teamButtonId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        TapPermissionRequestResponseDto response = teamButtonPermissionService.requestPermission(userId, teamId, teamButtonId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("탭 권한을 요청했습니다.", response));
    }

    @PatchMapping("/{user_id}")
    public ApiResponse<TapPermissionDecisionResponseDto> decidePermission(
            Authentication authentication,
            @PathVariable("team_id") Long teamId,
            @PathVariable("team_button_id") Long teamButtonId,
            @PathVariable("user_id") Long targetUserId,
            @RequestBody TapPermissionDecisionRequestDto request
    ) {
        Long approverId = (Long) authentication.getPrincipal();
        return ApiResponse.success("탭 권한 요청이 처리되었습니다.",
                teamButtonPermissionService.decidePermission(approverId, teamId, teamButtonId, targetUserId, request));
    }

    @GetMapping("/requests")
    public ApiResponse<List<TapPermissionRequestListItemDto>> listPendingRequests(
            Authentication authentication,
            @PathVariable("team_id") Long teamId,
            @PathVariable("team_button_id") Long teamButtonId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("탭 권한 요청 목록 조회에 성공했습니다.",
                teamButtonPermissionService.listPendingRequests(userId, teamId, teamButtonId));
    }
}
