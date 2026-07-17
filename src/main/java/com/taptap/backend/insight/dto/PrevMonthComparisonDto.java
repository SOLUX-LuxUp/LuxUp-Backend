package com.taptap.backend.insight.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PrevMonthComparisonDto {
    private int prevTotalTapCount;
    private double changeRate;
    private List<RankedButtonDto> prevTop5Buttons;
}