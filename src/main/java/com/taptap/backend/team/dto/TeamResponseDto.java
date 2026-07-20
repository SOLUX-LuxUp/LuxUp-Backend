package com.taptap.backend.team.dto;

import java.time.LocalDateTime;

public record TeamResponseDto(
        Long teamId,
        String teamName,
        String teamImageUrl,
        String iconName,
        String iconColor,
        String inviteCode,
        Integer maxMember,
        Long ownerUserId,
        LocalDateTime createdAt
) {
}
