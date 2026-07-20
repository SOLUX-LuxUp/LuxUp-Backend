package com.taptap.backend.team.dto;

import java.time.LocalDateTime;

public record UpdateTeamProfileResponseDto(
        Long teamId,
        Long userId,
        String displayName,
        String profileImageUrl,
        LocalDateTime updatedAt
) {
}
