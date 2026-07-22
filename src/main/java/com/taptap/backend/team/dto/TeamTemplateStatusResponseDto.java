package com.taptap.backend.team.dto;

public record TeamTemplateStatusResponseDto(
        Boolean hasSelectedTemplate,
        Long templateId,
        String templateType,
        String templateName
) {
}
