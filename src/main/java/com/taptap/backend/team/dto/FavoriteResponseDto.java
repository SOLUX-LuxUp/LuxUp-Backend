package com.taptap.backend.team.dto;

public record FavoriteResponseDto(
        Long teamId,
        Boolean isFavorite
) {
}
