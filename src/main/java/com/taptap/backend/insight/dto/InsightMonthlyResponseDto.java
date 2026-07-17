package com.taptap.backend.insight.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@Schema(description = "11. 먼슬리 인사이트 조회 응답 (라이프스타일 추천은 /api/lifestyle-recommendations로 별도 분리)")
public class InsightMonthlyResponseDto {
    private int year;
    private int month;
    private int totalTapCount;
    private Map<String, Integer> dailyTapCounts;
    private List<CategoryTapCountDto> categoryTapCounts;
    private List<ButtonTapCountDto> buttonTapCounts;
    private List<RankedButtonDto> top3Buttons;
    private TopCategoryDto topCategory;
    private String busiestDay;
    private double weekdayRatio;
    private double weekendRatio;
    private Map<String, TimeSlotCategoryEntryDto> timeSlotCategory;
    private PrevMonthComparisonDto prevMonthComparison;
}