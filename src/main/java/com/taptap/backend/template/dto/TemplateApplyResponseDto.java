package com.taptap.backend.template.dto;

import java.util.List;

public record TemplateApplyResponseDto(
        Long templateId,
        String templateName,
        List<CreatedCategoryDto> createdCategories,
        List<CreatedButtonDto> createdButtons
) {
}