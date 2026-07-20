package com.taptap.backend.team.dto;

import java.time.LocalDateTime;

public record DeleteTeamButtonResponseDto(
        Long teamButtonId,
        LocalDateTime deletedAt
) {
}
