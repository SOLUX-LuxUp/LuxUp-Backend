package com.taptap.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank String verificationCode,
        @NotBlank @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.") String password
) {
}