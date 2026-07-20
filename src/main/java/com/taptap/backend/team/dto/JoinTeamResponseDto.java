package com.taptap.backend.team.dto;

import java.time.LocalDateTime;

public record JoinTeamResponseDto(
        Long teamId,
        String teamName,
        String teamImageUrl,
        LocalDateTime joinedAt
) {
}
