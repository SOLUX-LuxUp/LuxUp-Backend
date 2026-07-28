package com.taptap.backend.team.dto;

import java.time.LocalDateTime;

public record UpdateTeamButtonCategoryResponseDto(
        Long categoryId,
        String categoryName,
        String categoryColor,
        Integer displayOrder,
        LocalDateTime updatedAt
) {
}
