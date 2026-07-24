package com.taptap.backend.team.dto;

public record TeamNotificationToggleResponseDto(
        Long teamId,
        Long userId,
        Boolean notificationEnabled
) {
}
