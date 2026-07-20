package com.taptap.backend.team.dto;

public record TeamButtonListItemDto(
        Long teamButtonId,
        String buttonName,
        String iconName,
        String iconColor,
        Long categoryId,
        String categoryName,
        String tapPermission,
        Boolean hasTapPermission,
        LatestRecordSummaryDto latestRecord
) {
}
