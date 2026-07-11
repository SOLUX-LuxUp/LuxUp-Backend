package com.taptap.backend.button.dto;

import java.time.LocalDateTime;

public record CategoryResponseDto(
        Long categoryId,
        String categoryName,
        Integer displayOrder,
        LocalDateTime createdAt
) {
}