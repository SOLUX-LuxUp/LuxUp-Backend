package com.taptap.backend.button.dto;

import java.time.LocalDate;

public record UpdateButtonRequestDto(
        String buttonName,
        String iconName,
        String iconColor,
        Long categoryId,
        Boolean clearCategory,
        Boolean goalEnabled,
        String goalName,
        String goalPeriodUnit,
        Integer goalCount,
        String goalComparisonType,
        Boolean expiryEnabled,
        LocalDate expiredAt
) {
}