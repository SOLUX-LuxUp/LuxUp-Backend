package com.taptap.backend.user.dto;

public record GoogleLoginResponse(
        String accessToken,
        String refreshToken,
        Long userId,
        boolean isNewUser,
        boolean isOnboardingRequired
) {
}