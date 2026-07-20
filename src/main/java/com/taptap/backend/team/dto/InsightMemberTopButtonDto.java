package com.taptap.backend.team.dto;

public record InsightMemberTopButtonDto(
        Long teamButtonId,
        String buttonName,
        String iconName,
        String iconColor,
        Long tapCount
) {
}
