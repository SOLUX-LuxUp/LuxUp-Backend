package com.taptap.backend.button.dto;

import java.time.LocalDate;

public record UpdateButtonRequestDto(
        String buttonName,
        String iconName,
        String iconColor,
        Long categoryId,
        Boolean clearCategory,
        Boolean expiryEnabled,
        LocalDate expiredAt
) {
}