package com.taptap.backend.team.dto;

public record TeamProfileButtonItemDto(
        Long buttonId,
        String buttonName,
        String iconName,
        String iconColor,
        Long categoryId,
        String categoryName,
        Boolean isShared
) {
}
