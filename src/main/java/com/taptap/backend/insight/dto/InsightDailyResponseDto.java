package com.taptap.backend.insight.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "9. 데일리 인사이트 조회 응답")
public class InsightDailyResponseDto {
    private String targetDate;
    private int totalTapCount;
    private TopButtonDto topButton;
    private String peakTimeSlot;
    private List<CategoryTapCountDto> categoryTapCounts;
    private List<ButtonTapCountDto> buttonTapCounts;
    private List<InsightTimelineItemDto> timeline;
}