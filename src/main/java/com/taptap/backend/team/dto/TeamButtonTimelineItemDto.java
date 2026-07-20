package com.taptap.backend.team.dto;

import java.time.LocalDateTime;

public record TeamButtonTimelineItemDto(
        Long recordId,
        LocalDateTime recordedAt,
        String memo,
        String emoji,
        MemberProfileDto recordedBy
) {
}
