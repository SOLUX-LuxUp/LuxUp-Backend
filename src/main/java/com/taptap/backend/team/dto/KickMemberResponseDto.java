package com.taptap.backend.team.dto;

import java.time.LocalDateTime;

public record KickMemberResponseDto(
        Long teamId,
        Long userId,
        LocalDateTime removedAt
) {
}
