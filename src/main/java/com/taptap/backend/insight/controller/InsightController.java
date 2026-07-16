package com.taptap.backend.insight.controller;

import com.taptap.backend.config.ApiResponse;
import com.taptap.backend.insight.dto.InsightDailyResponseDto;
import com.taptap.backend.insight.dto.InsightWeeklyResponseDto;
import com.taptap.backend.insight.service.InsightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "인사이트", description = "개인 인사이트 관련 API")
@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightController {

    private final InsightService insightService;

    @Operation(summary = "7.1 데일리 인사이트 조회")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/daily")
    public ApiResponse<InsightDailyResponseDto> getDailyInsight(
            Authentication authentication,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Long userId = (Long) authentication.getPrincipal();
        InsightDailyResponseDto response = insightService.getDailyInsight(userId, date);
        return ApiResponse.success("데일리 인사이트 조회가 완료되었습니다.", response);
    }

    @Operation(summary = "7.2 위클리 인사이트 조회")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/weekly")
    public ApiResponse<InsightWeeklyResponseDto> getWeeklyInsight(
            Authentication authentication,
            @RequestParam(value = "weekStart", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart
    ) {
        Long userId = (Long) authentication.getPrincipal();
        InsightWeeklyResponseDto response = insightService.getWeeklyInsight(userId, weekStart);
        return ApiResponse.success("위클리 인사이트 조회가 완료되었습니다.", response);
    }
}