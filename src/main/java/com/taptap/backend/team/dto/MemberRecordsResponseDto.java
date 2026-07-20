package com.taptap.backend.team.dto;

import java.util.List;

public record MemberRecordsResponseDto(
        Long userId,
        String displayName,
        String profileImageUrl,
        Boolean hasMore,
        Long nextCursor,
        List<MemberRecordButtonItemDto> buttons,
        List<MemberRecordTimelineItemDto> recentTimeline
) {
}
