package com.taptap.backend.button.dto;

import java.util.List;

public record CategoryGroupDto(
        Long categoryId,
        String categoryName,
        Integer displayOrder,
        List<CategoryButtonItemDto> buttons
) {
}