package com.taptap.backend.team.dto;

import java.time.LocalDateTime;

public record InsightTimelineItemDto(
        Long teamButtonId,
        String buttonName,
        String iconName,
        String iconColor,
        LocalDateTime tappedAt,
        MemberProfileDto member
) {
}
