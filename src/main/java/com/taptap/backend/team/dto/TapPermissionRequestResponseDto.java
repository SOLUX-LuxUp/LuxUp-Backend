package com.taptap.backend.team.dto;

import java.time.LocalDateTime;

public record TapPermissionRequestResponseDto(
        Long teamButtonId,
        Long userId,
        String permissionStatus,
        LocalDateTime requestedAt
) {
}
