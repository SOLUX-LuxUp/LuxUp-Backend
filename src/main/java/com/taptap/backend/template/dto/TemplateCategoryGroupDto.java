package com.taptap.backend.template.dto;

import java.util.List;

public record TemplateCategoryGroupDto(
        String categoryName,
        List<TemplatePreviewButtonDto> buttons
) {
}