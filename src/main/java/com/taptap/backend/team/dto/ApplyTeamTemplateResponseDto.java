package com.taptap.backend.team.dto;

import java.util.List;

public record ApplyTeamTemplateResponseDto(
        Long teamId,
        Long templateId,
        String templateType,
        String templateName,
        List<TeamButtonCategoryResponseDto> categories
) {
}
