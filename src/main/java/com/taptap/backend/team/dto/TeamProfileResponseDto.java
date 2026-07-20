package com.taptap.backend.team.dto;

import java.util.List;

public record TeamProfileResponseDto(
        Long teamId,
        Long userId,
        String displayName,
        String profileImageUrl,
        List<TeamProfileButtonItemDto> buttons
) {
}
