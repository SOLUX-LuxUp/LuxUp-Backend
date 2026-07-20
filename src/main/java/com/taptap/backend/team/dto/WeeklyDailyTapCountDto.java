package com.taptap.backend.team.dto;

import java.time.LocalDate;
import java.util.List;

public record WeeklyDailyTapCountDto(
        LocalDate date,
        Long tapCount,
        List<InsightCategoryTapCountDto> categories
) {
}
