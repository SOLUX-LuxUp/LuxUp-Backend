package com.taptap.backend.team.dto;

public record SkipTeamTemplateResponseDto(
        Long teamId,
        Boolean isSkipped
) {
}
