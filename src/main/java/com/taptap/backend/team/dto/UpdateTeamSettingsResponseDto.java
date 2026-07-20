package com.taptap.backend.team.dto;

import java.time.LocalDateTime;

public record UpdateTeamSettingsResponseDto(
        Long teamId,
        String teamName,
        LocalDateTime updatedAt
) {
}
