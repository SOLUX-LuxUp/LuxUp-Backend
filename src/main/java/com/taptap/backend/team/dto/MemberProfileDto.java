package com.taptap.backend.team.dto;

public record MemberProfileDto(
        Long userId,
        String displayName,
        String profileImageUrl
) {
}
