package com.taptap.backend.team.controller;

import com.taptap.backend.config.ApiResponse;
import com.taptap.backend.team.dto.*;
import com.taptap.backend.team.service.TeamButtonService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/teams/{team_id}/buttons")
public class TeamButtonController {

    private final TeamButtonService teamButtonService;

    public TeamButtonController(TeamButtonService teamButtonService) {
        this.teamButtonService = teamButtonService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TeamButtonResponseDto>> createButton(
            Authentication authentication,
            @PathVariable("team_id") Long teamId,
            @RequestBody CreateTeamButtonRequestDto request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        TeamButtonResponseDto response = teamButtonService.createButton(userId, teamId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("팀 공유 버튼이 생성되었습니다.", response));
    }

    @GetMapping("/categories")
    public ApiResponse<List<TeamButtonCategoryResponseDto>> getCategories(
            Authentication authentication,
            @PathVariable("team_id") Long teamId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("팀 버튼 카테고리 목록 조회에 성공했습니다.", teamButtonService.getCategories(userId, teamId));
    }

    @GetMapping
    public ApiResponse<List<TeamButtonListItemDto>> listButtons(
            Authentication authentication,
            @PathVariable("team_id") Long teamId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("팀 공유 버튼 목록 조회에 성공했습니다.", teamButtonService.listButtons(userId, teamId));
    }

    @GetMapping("/{team_button_id}")
    public ApiResponse<TeamButtonDetailResponseDto> getButtonDetail(
            Authentication authentication,
            @PathVariable("team_id") Long teamId,
            @PathVariable("team_button_id") Long teamButtonId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("팀 공유 버튼 상세 조회에 성공했습니다.", teamButtonService.getButtonDetail(userId, teamId, teamButtonId));
    }

    @PatchMapping("/{team_button_id}")
    public ApiResponse<UpdateTeamButtonResponseDto> updateButton(
            Authentication authentication,
            @PathVariable("team_id") Long teamId,
            @PathVariable("team_button_id") Long teamButtonId,
            @RequestBody UpdateTeamButtonRequestDto request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("팀 공유 버튼이 수정되었습니다.", teamButtonService.updateButton(userId, teamId, teamButtonId, request));
    }

    @DeleteMapping("/{team_button_id}")
    public ApiResponse<DeleteTeamButtonResponseDto> deleteButton(
            Authentication authentication,
            @PathVariable("team_id") Long teamId,
            @PathVariable("team_button_id") Long teamButtonId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("팀 공유 버튼이 삭제되었습니다.", teamButtonService.deleteButton(userId, teamId, teamButtonId));
    }

    @PostMapping("/{team_button_id}/records")
    public ResponseEntity<ApiResponse<CreateTeamButtonRecordResponseDto>> createRecord(
            Authentication authentication,
            @PathVariable("team_id") Long teamId,
            @PathVariable("team_button_id") Long teamButtonId,
            @RequestBody(required = false) CreateTeamButtonRecordRequestDto request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        CreateTeamButtonRecordResponseDto response = teamButtonService.createRecord(userId, teamId, teamButtonId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("버튼이 기록되었습니다.", response));
    }

    @GetMapping("/{team_button_id}/records/latest")
    public ApiResponse<LatestRecordResponseDto> getLatestRecord(
            Authentication authentication,
            @PathVariable("team_id") Long teamId,
            @PathVariable("team_button_id") Long teamButtonId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("최근 기록 조회에 성공했습니다.", teamButtonService.getLatestRecord(userId, teamId, teamButtonId));
    }

    @GetMapping("/{team_button_id}/records")
    public ApiResponse<TeamButtonTimelineResponseDto> getTimeline(
            Authentication authentication,
            @PathVariable("team_id") Long teamId,
            @PathVariable("team_button_id") Long teamButtonId,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("기록 타임라인 조회에 성공했습니다.", teamButtonService.getTimeline(userId, teamId, teamButtonId, cursor, limit));
    }

    @PatchMapping("/{team_button_id}/notification")
    public ApiResponse<NotificationToggleResponseDto> toggleNotification(
            Authentication authentication,
            @PathVariable("team_id") Long teamId,
            @PathVariable("team_button_id") Long teamButtonId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("알림 설정이 변경되었습니다.", teamButtonService.toggleNotification(userId, teamId, teamButtonId));
    }
}
