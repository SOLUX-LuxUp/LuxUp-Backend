package com.taptap.backend.team.dto;

public record MyPermissionDto(
        Boolean hasTapPermission,
        String permissionStatus,
        Boolean isNotificationEnabled
) {
}
