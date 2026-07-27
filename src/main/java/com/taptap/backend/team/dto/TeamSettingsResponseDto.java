package com.taptap.backend.team.dto;

import java.time.LocalDateTime;

public record TeamSettingsResponseDto(
        Long teamId,
        String teamName,
        String teamImageUrl,
        String iconName,
        String iconColor,
        String inviteCode,
        Integer maxMember,
        Long memberCount,
        String buttonCreatePermission,
        String buttonEditPermission,
        String buttonDeletePermission,
        Long ownerUserId,
        Boolean notificationEnabled,
        Boolean isDeleting,
        LocalDateTime scheduledDeletionAt
) {
}
