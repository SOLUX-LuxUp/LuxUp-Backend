package com.taptap.backend.team.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UpdateTeamButtonResponseDto(
        Long teamButtonId,
        String buttonName,
        Long categoryId,
        String iconName,
        String iconColor,
        String description,
        String tapPermission,
        List<Long> allowedUserIds,
        LocalDateTime updatedAt
) {
}
