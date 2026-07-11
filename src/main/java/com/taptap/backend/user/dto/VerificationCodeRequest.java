package com.taptap.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerificationCodeRequest(
        @NotBlank @Email String email
) {
}