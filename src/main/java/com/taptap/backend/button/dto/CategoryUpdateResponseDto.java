package com.taptap.backend.button.dto;

import java.time.LocalDateTime;

public record CategoryUpdateResponseDto(
        Long categoryId,
        String categoryName,
        Integer displayOrder,
        LocalDateTime updatedAt
) {
}