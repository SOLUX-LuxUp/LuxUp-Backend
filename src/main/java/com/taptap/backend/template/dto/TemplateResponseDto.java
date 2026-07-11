package com.taptap.backend.template.dto;

import com.taptap.backend.template.entity.Template;

public record TemplateResponseDto(
        Long templateId,
        String templateType,
        String templateName,
        String description
) {
    public static TemplateResponseDto from(Template template) {
        return new TemplateResponseDto(
                template.getTemplateId(),
                template.getTemplateType(),
                template.getTemplateName(),
                template.getDescription()
        );
    }
}