package com.taptap.backend.team.dto;

public record LatestRecordResponseDto(
        Long teamButtonId,
        String buttonName,
        TeamButtonTimelineItemDto latestRecord
) {
}
