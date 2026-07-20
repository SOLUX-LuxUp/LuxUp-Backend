package com.taptap.backend.team.dto;

import java.time.LocalDate;
import java.util.List;

public record WeeklyInsightResponseDto(
        Long teamId,
        LocalDate weekStart,
        LocalDate weekEnd,
        Long totalTapCount,
        InsightTopButtonDto topButton,
        List<WeeklyDailyTapCountDto> dailyTapCounts,
        List<InsightButtonTapCountDto> buttonTapCounts,
        List<InsightMemberActivityDto> memberActivity
) {
}
