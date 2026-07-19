package com.taptap.backend.button.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ButtonResponseDto(
        Long buttonId,
        String buttonName,
        String iconName,
        String iconColor,
        Long categoryId,
        Boolean expiryEnabled,
        LocalDate expiredAt,
        Boolean isFavorite,
        Boolean isActive,
        LocalDateTime lastRecordedAt,
        LocalDateTime createdAt
) {
}