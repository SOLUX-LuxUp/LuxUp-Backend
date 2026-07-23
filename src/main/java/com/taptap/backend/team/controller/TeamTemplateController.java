package com.taptap.backend.team.controller;

import com.taptap.backend.config.ApiResponse;
import com.taptap.backend.team.dto.*;
import com.taptap.backend.team.service.TeamTemplateService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TeamTemplateController {

    private final TeamTemplateService teamTemplateService;

    public TeamTemplateController(TeamTemplateService teamTemplateService) {
        this.teamTemplateService = teamTemplateService;
    }

    @GetMapping("/api/team-templates")
    public ApiResponse<List<TeamTemplateDto>> listTemplates() {
        return ApiResponse.success("팀 템플릿 목록 조회에 성공했습니다.", teamTemplateService.listTemplates());
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/api/teams/{team_id}/template")
    public ApiResponse<TeamTemplateStatusResponseDto> getTemplateStatus(
            Authentication authentication,
            @PathVariable("team_id") Long teamId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("팀 템플릿 선택 여부 조회에 성공했습니다.", teamTemplateService.getTemplateStatus(userId, teamId));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/api/teams/{team_id}/template")
    public ResponseEntity<ApiResponse<ApplyTeamTemplateResponseDto>> selectTemplate(
            Authentication authentication,
            @PathVariable("team_id") Long teamId,
            @RequestBody ApplyTeamTemplateRequestDto request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        ApplyTeamTemplateResponseDto response = teamTemplateService.selectTemplate(userId, teamId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("팀 템플릿이 선택되었습니다.", response));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/api/teams/{team_id}/template/skip")
    public ApiResponse<SkipTeamTemplateResponseDto> skipTemplate(
            Authentication authentication,
            @PathVariable("team_id") Long teamId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("템플릿 선택을 건너뛰었습니다.", teamTemplateService.skipTemplate(userId, teamId));
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/api/teams/{team_id}/template/suggestions")
    public ApiResponse<List<TemplateSuggestionDto>> getSuggestions(
            Authentication authentication,
            @PathVariable("team_id") Long teamId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("템플릿 추천 버튼 목록 조회에 성공했습니다.", teamTemplateService.getSuggestions(userId, teamId));
    }
}
