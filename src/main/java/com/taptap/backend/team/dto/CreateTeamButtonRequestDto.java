package com.taptap.backend.team.dto;

import java.util.List;

public record CreateTeamButtonRequestDto(
        String buttonName,
        String iconName,
        String iconColor,
        String description,
        String tapPermission,
        Long categoryId,
        List<Long> allowedUserIds
) {
}
