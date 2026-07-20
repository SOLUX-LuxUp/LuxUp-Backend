package com.taptap.backend.team.dto;

import java.util.List;

public record UpdateButtonSharingResponseDto(
        Long teamId,
        Long userId,
        List<TeamProfileButtonItemDto> buttons
) {
}
