package com.taptap.backend.team.dto;

public record InsightMemberActivityDto(
        Long userId,
        String displayName,
        String profileImageUrl,
        Long tapCount,
        InsightMemberTopButtonDto topButton
) {
}
