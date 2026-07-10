package com.taptap.backend.user.controller;

import com.taptap.backend.config.ApiResponse;
import com.taptap.backend.user.dto.WithdrawRequest;
import com.taptap.backend.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/me")
    public ApiResponse<Void> withdraw(
            Authentication authentication,
            @Valid @RequestBody WithdrawRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        authService.withdraw(userId, request);
        return ApiResponse.<Void>success("회원탈퇴가 완료되었습니다.", null);
    }
}