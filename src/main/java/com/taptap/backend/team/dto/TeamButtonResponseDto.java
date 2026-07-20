package com.taptap.backend.team.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TeamButtonResponseDto(
        Long teamButtonId,
        Long teamId,
        String buttonName,
        String iconName,
        String iconColor,
        String description,
        String tapPermission,
        Long categoryId,
        List<Long> allowedUserIds,
        Long createdBy,
        LocalDateTime createdAt
) {
}
