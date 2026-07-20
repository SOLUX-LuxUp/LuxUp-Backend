package com.taptap.backend.team.dto;

public record InsightButtonTapCountDto(
        Long teamButtonId,
        String buttonName,
        String iconName,
        String iconColor,
        Long categoryId,
        String categoryName,
        Long tapCount
) {
}
