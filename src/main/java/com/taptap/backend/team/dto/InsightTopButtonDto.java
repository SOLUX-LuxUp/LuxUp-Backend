package com.taptap.backend.team.dto;

import java.util.List;

public record InsightTopButtonDto(
        Long teamButtonId,
        String buttonName,
        String iconName,
        String iconColor,
        Long tapCount,
        List<MemberProfileDto> tappedMembers
) {
}
