package com.taptap.backend.team.dto;

import java.util.List;

public record MonthlyInsightResponseDto(
        Long teamId,
        Integer year,
        Integer month,
        Long totalTapCount,
        InsightTopButtonDto topButton,
        List<MonthlyDailyTapCountDto> dailyTapCounts,
        List<MonthlyCategoryTapCountDto> categoryTapCounts,
        List<MonthlyButtonTapCountDto> buttonTapCounts,
        List<InsightMemberActivityDto> memberActivity
) {
}
