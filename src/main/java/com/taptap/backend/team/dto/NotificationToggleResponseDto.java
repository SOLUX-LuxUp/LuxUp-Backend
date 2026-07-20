package com.taptap.backend.team.dto;

public record NotificationToggleResponseDto(
        Long teamButtonId,
        Long userId,
        Boolean isEnabled
) {
}
