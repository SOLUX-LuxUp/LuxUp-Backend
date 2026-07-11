package com.taptap.backend.user.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank String refreshToken
) {}