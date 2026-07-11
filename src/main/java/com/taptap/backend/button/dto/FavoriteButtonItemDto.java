package com.taptap.backend.button.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FavoriteButtonItemDto(
        Long buttonId,
        String buttonName,
        String iconName,
        String iconColor,
        Long categoryId,
        String categoryName,
        Boolean isFavorite,
        Integer favoriteOrder,
        Boolean goalEnabled,
        String goalName,
        String goalPeriodUnit,
        Integer goalCount,
        String goalComparisonType,
        Boolean expiryEnabled,
        LocalDate expiredAt,
        Boolean isActive,
        LocalDateTime lastRecordedAt,
        LocalDateTime createdAt
) {
}