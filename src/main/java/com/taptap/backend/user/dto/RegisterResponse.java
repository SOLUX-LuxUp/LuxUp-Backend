package com.taptap.backend.user.dto;

import java.time.LocalDateTime;

public record RegisterResponse(
        Long userId,
        String email,
        String accessToken,
        String refreshToken,
        boolean isOnboardingRequired,
        LocalDateTime createdAt
) {
}