package com.taptap.backend.user.controller;

import com.taptap.backend.config.ApiResponse;
import com.taptap.backend.user.dto.*;
import com.taptap.backend.user.service.AuthService;
import com.taptap.backend.user.service.EmailVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final EmailVerificationService emailVerificationService;
    private final AuthService authService;

    public AuthController(EmailVerificationService emailVerificationService, AuthService authService) {
        this.emailVerificationService = emailVerificationService;
        this.authService = authService;
    }

    @PostMapping("/email/verification-code")
    public ApiResponse<VerificationCodeResponse> sendVerificationCode(
            @Valid @RequestBody VerificationCodeRequest request
    ) {
        VerificationCodeResponse response = emailVerificationService.sendCode(request.email());
        return ApiResponse.success("인증코드가 발송되었습니다.", response);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다.", response));
    }

    @PostMapping("/sessions")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success("로그인이 완료되었습니다.", response);
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/sessions")
    public ApiResponse<Void> logout(
            Authentication authentication,
            @Valid @RequestBody LogoutRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        authService.logout(userId, request);
        return ApiResponse.<Void>success("로그아웃이 완료되었습니다.", null);
    }

    @PostMapping("/tokens/refresh")
    public ApiResponse<TokenRefreshResponse> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        TokenRefreshResponse response = authService.refreshAccessToken(request);
        return ApiResponse.success("토큰이 재발급되었습니다.", response);
    }

    @PostMapping("/google/sessions")
    public ResponseEntity<ApiResponse<GoogleLoginResponse>> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request
    ) {
        AuthService.GoogleLoginResult result = authService.googleLogin(request);
        HttpStatus status = result.isNewUser() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(ApiResponse.success("로그인이 완료되었습니다.", result.response()));
    }

}