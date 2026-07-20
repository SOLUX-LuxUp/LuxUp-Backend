package com.taptap.backend.team.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyInsightResponseDto(
        Long teamId,
        LocalDate targetDate,
        Long totalTapCount,
        InsightTopButtonDto topButton,
        List<InsightTimelineItemDto> timeline,
        List<InsightCategoryTapCountDto> categories,
        List<InsightButtonTapCountDto> buttonTapCounts,
        List<InsightMemberActivityDto> memberActivity
) {
}
