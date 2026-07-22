package com.taptap.backend.team.dto;

public record LatestRecordResponseDto(
        Long teamButtonId,
        String buttonName,
        String iconName,
        String iconColor,
        TeamButtonTimelineItemDto latestRecord
) {
}
