package com.taptap.backend.button.dto;

import java.util.List;

public record FavoriteOrderRequestDto(List<Long> buttonIds) {
}