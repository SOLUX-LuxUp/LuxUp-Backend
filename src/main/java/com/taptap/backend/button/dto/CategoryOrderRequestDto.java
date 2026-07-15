package com.taptap.backend.button.dto;

import java.util.List;

public record CategoryOrderRequestDto(List<Long> categoryIds) {
}