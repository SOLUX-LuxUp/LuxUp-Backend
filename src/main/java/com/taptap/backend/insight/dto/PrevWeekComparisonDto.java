package com.taptap.backend.insight.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PrevWeekComparisonDto {
    private int prevTotalTapCount;
    private double changeRate;
    private WeeklyTopButtonDto prevTopButton;
}