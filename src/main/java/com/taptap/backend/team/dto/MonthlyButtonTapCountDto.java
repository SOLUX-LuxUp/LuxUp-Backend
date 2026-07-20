package com.taptap.backend.team.dto;

public record MonthlyButtonTapCountDto(
        Long teamButtonId,
        String buttonName,
        Long tapCount
) {
}
