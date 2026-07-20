package com.taptap.backend.team.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TeamButtonDetailResponseDto(
        Long teamButtonId,
        Long teamId,
        String buttonName,
        String iconName,
        String iconColor,
        String description,
        String tapPermission,
        Boolean isActive,
        MemberProfileDto createdBy,
        MyPermissionDto myPermission,
        Long categoryId,
        String categoryName,
        List<Long> allowedUserIds,
        LatestRecordSummaryDto latestRecord,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
