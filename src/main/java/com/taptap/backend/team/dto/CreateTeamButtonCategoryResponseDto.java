package com.taptap.backend.team.dto;

import java.time.LocalDateTime;

public record CreateTeamButtonCategoryResponseDto(
        Long categoryId,
        String categoryName,
        String categoryColor,
        Integer displayOrder,
        LocalDateTime createdAt
) {
}
