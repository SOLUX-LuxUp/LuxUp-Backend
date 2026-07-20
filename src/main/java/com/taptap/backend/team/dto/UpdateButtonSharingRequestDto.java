package com.taptap.backend.team.dto;

import java.util.List;

public record UpdateButtonSharingRequestDto(
        List<ButtonSharingUpdateItemDto> buttons
) {
}
