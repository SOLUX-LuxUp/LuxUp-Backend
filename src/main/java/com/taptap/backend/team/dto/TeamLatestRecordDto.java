package com.taptap.backend.team.dto;

import java.time.LocalDateTime;

public record TeamLatestRecordDto(
        Long teamButtonId,
        String buttonName,
        String iconName,
        String iconColor,
        LocalDateTime recordedAt
) {
}
