package com.taptap.backend.button.dto;

import java.util.List;

public record ButtonListResponseDto(
        List<FavoriteButtonItemDto> favorites,
        List<CategoryGroupDto> categories
) {
}