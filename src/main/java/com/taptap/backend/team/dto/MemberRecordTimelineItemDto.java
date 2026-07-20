package com.taptap.backend.team.dto;

import java.time.LocalDateTime;

public record MemberRecordTimelineItemDto(
        Long recordId,
        String buttonName,
        LocalDateTime recordedAt,
        String memo,
        String emoji
) {
}
