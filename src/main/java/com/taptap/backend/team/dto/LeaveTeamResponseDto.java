package com.taptap.backend.team.dto;

import java.time.LocalDateTime;

public record LeaveTeamResponseDto(
        Long teamId,
        Long userId,
        LocalDateTime leftAt
) {
}
