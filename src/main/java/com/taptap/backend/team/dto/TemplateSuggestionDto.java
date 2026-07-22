package com.taptap.backend.team.dto;

public record TemplateSuggestionDto(
        String buttonName,
        String iconName,
        String iconColor,
        Long categoryId,
        String categoryName
) {
}
