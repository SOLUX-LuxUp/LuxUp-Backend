package com.taptap.backend.team.dto;

import java.time.LocalDateTime;

public record TapPermissionDecisionResponseDto(
        Long teamButtonId,
        Long userId,
        String permissionStatus,
        Long grantedBy,
        LocalDateTime updatedAt
) {
}
