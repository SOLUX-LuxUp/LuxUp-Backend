package com.taptap.backend.team.controller;

import com.taptap.backend.config.ApiResponse;
import com.taptap.backend.team.dto.DailyInsightResponseDto;
import com.taptap.backend.team.dto.MonthlyInsightResponseDto;
import com.taptap.backend.team.dto.WeeklyInsightResponseDto;
import com.taptap.backend.team.service.TeamInsightService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/teams/{team_id}/insights")
public class TeamInsightController {

    private final TeamInsightService teamInsightService;

    public TeamInsightController(TeamInsightService teamInsightService) {
        this.teamInsightService = teamInsightService;
    }

    @GetMapping("/daily")
    public ApiResponse<DailyInsightResponseDto> getDailyInsight(
            Authentication authentication,
            @PathVariable("team_id") Long teamId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("탭 데일리 통계 조회에 성공했습니다.", teamInsightService.getDailyInsight(userId, teamId, date));
    }

    @GetMapping("/weekly")
    public ApiResponse<WeeklyInsightResponseDto> getWeeklyInsight(
            Authentication authentication,
            @PathVariable("team_id") Long teamId,
            @RequestParam(value = "week_start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("탭 위클리 통계 조회에 성공했습니다.", teamInsightService.getWeeklyInsight(userId, teamId, weekStart));
    }

    @GetMapping("/monthly")
    public ApiResponse<MonthlyInsightResponseDto> getMonthlyInsight(
            Authentication authentication,
            @PathVariable("team_id") Long teamId,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("탭 먼슬리 통계 조회에 성공했습니다.", teamInsightService.getMonthlyInsight(userId, teamId, year, month));
    }
}
