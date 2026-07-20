package com.taptap.backend.team.dto;

public record CreateTeamRequestDto(
        String teamName,
        String teamImageUrl,
        String iconName,
        String iconColor,
        Integer maxMember
) {
}
