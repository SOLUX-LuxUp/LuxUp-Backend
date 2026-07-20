package com.taptap.backend.team.dto;

public record UpdateTeamProfileRequestDto(
        String displayName,
        String profileImageUrl
) {
}
