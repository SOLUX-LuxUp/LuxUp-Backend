package com.taptap.backend.team.dto;

import java.time.LocalDate;

public record MonthlyDailyTapCountDto(
        LocalDate date,
        Long tapCount
) {
}
