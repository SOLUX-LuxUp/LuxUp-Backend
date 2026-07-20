package com.taptap.backend.team.dto;

public record InsightCategoryTapCountDto(
        Long categoryId,
        String categoryName,
        String categoryColor,
        Long tapCount
) {
}
