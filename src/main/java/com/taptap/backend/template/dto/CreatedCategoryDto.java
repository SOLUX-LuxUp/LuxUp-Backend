package com.taptap.backend.template.dto;

import com.taptap.backend.button.entity.ButtonCategory;

public record CreatedCategoryDto(
        Long categoryId,
        String categoryName
) {
    public static CreatedCategoryDto from(ButtonCategory category) {
        return new CreatedCategoryDto(category.getCategoryId(), category.getCategoryName());
    }
}