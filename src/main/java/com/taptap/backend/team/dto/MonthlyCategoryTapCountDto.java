package com.taptap.backend.team.dto;

public record MonthlyCategoryTapCountDto(
        Long categoryId,
        String categoryName,
        String categoryColor,
        Long tapCount,
        Double ratio
) {
}
