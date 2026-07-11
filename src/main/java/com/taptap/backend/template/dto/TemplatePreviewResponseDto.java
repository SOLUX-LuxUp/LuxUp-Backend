package com.taptap.backend.template.dto;

import com.taptap.backend.template.entity.Template;

import java.util.List;

public record TemplatePreviewResponseDto(
        Long templateId,
        String templateName,
        List<TemplateCategoryGroupDto> categories
) {
    public static TemplatePreviewResponseDto of(Template template, List<TemplateCategoryGroupDto> categories) {
        return new TemplatePreviewResponseDto(
                template.getTemplateId(),
                template.getTemplateName(),
                categories
        );
    }
}