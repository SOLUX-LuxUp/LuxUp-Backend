package com.taptap.backend.insight.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "10. 위클리 인사이트 조회 응답")
public class InsightWeeklyResponseDto {
    private String weekStart;
    private String weekEnd;
    private int totalTapCount;
    private List<DailyTapCountDto> dailyTapCounts;
    private List<CategoryTapCountDto> categoryTapCounts;
    private List<ButtonTapCountDto> buttonTapCounts;
    private String peakDay;
    private String peakTimeSlot;
    private WeeklyTopButtonDto topButton;
    private PrevWeekComparisonDto prevWeekComparison;
}