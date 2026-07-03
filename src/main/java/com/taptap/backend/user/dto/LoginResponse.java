package com.taptap.backend.user.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        Long userId,
        boolean isOnboardingRequired
) {
}