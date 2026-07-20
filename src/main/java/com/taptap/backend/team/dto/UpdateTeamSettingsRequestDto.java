package com.taptap.backend.team.dto;

public record UpdateTeamSettingsRequestDto(
        String teamName,
        String teamImageUrl,
        String iconName,
        String iconColor,
        Integer maxMember,
        String buttonCreatePermission,
        String buttonEditPermission,
        String buttonDeletePermission,
        Long newOwnerUserId
) {
}
