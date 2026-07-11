package com.taptap.backend.button.dto;

public record FavoriteResponseDto(Long buttonId, Boolean isFavorite, Integer favoriteOrder) {
}