package com.taptap.backend.user.dto;

import jakarta.validation.constraints.NotBlank;

public record WithdrawRequest(
        @NotBlank String refreshToken
) {}