package com.taptap.backend.team.controller;

import com.taptap.backend.config.ApiResponse;
import com.taptap.backend.team.dto.*;
import com.taptap.backend.team.service.TeamService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TeamResponseDto>> createTeam(
            Authentication authentication,
            @RequestBody CreateTeamRequestDto request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        TeamResponseDto response = teamService.createTeam(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("팀이 생성되었습니다.", response));
    }

    @GetMapping
    public ApiResponse<List<TeamListItemDto>> listTeams(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("팀 목록 조회에 성공했습니다.", teamService.listTeams(userId));
    }

    @PostMapping("/join")
    public ApiResponse<JoinTeamResponseDto> joinTeam(
            Authentication authentication,
            @RequestBody JoinTeamRequestDto request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("팀에 가입했습니다.", teamService.joinTeam(userId, request));
    }

    @GetMapping("/{team_id}/settings")
    public ApiResponse<TeamSettingsResponseDto> getSettings(
            Authentication authentication,
            @PathVariable("team_id") Long teamId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("팀 설정 조회에 성공했습니다.", teamService.getSettings(userId, teamId));
    }

    @PatchMapping("/{team_id}/settings")
    public ApiResponse<UpdateTeamSettingsResponseDto> updateSettings(
            Authentication authentication,
            @PathVariable("team_id") Long teamId,
            @RequestBody UpdateTeamSettingsRequestDto request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("팀 설정이 수정되었습니다.", teamService.updateSettings(userId, teamId, request));
    }

    @PatchMapping("/{team_id}/notification")
    public ApiResponse<TeamNotificationToggleResponseDto> toggleNotification(
            Authentication authentication,
            @PathVariable("team_id") Long teamId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("팀 알림 설정이 변경되었습니다.", teamService.toggleNotification(userId, teamId));
    }

    @PatchMapping("/{team_id}/favorite")
    public ApiResponse<FavoriteResponseDto> toggleFavorite(
            Authentication authentication,
            @PathVariable("team_id") Long teamId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("팀 즐겨찾기 상태가 변경되었습니다.", teamService.toggleFavorite(userId, teamId));
    }

    @DeleteMapping("/{team_id}/leave")
    public ApiResponse<LeaveTeamResponseDto> leaveTeam(
            Authentication authentication,
            @PathVariable("team_id") Long teamId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("팀에서 탈퇴했습니다.", teamService.leaveTeam(userId, teamId));
    }

    @DeleteMapping("/{team_id}/members/{user_id}")
    public ApiResponse<KickMemberResponseDto> kickMember(
            Authentication authentication,
            @PathVariable("team_id") Long teamId,
            @PathVariable("user_id") Long targetUserId
    ) {
        Long requesterId = (Long) authentication.getPrincipal();
        return ApiResponse.success("팀원이 추방되었습니다.", teamService.kickMember(requesterId, teamId, targetUserId));
    }

    @GetMapping("/{team_id}/members")
    public ApiResponse<List<TeamMemberListItemDto>> listMembers(
            Authentication authentication,
            @PathVariable("team_id") Long teamId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("팀원 목록 조회에 성공했습니다.", teamService.listMembers(userId, teamId));
    }

    @DeleteMapping("/{team_id}")
    public ApiResponse<DeleteTeamResponseDto> deleteTeam(
            Authentication authentication,
            @PathVariable("team_id") Long teamId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("팀 삭제가 요청되었습니다. 3일 후 완전히 삭제됩니다.", teamService.deleteTeam(userId, teamId));
    }
}
