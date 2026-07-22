package com.taptap.backend.team.dto;

public record TeamButtonCategoryResponseDto(
        Long categoryId,
        String categoryName,
        String categoryColor,
        Integer displayOrder
) {
}
