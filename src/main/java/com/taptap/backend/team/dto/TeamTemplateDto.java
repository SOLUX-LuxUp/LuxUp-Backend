package com.taptap.backend.team.dto;

public record TeamTemplateDto(
        Long templateId,
        String templateType,
        String templateName,
        String description,
        String subDescription
) {
}
