package com.taptap.backend.team.dto;

import java.time.LocalDateTime;

public record DeleteTeamResponseDto(
        Long teamId,
        LocalDateTime requestedAt,
        LocalDateTime scheduledDeletionAt
) {
}
