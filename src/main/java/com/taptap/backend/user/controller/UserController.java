package com.taptap.backend.user.controller;

import com.taptap.backend.config.ApiResponse;
import com.taptap.backend.user.dto.UserProfileResponse;
import com.taptap.backend.user.dto.UserProfileUpdateRequest;
import com.taptap.backend.user.dto.WithdrawRequest;
import com.taptap.backend.user.service.AuthService;
import com.taptap.backend.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.taptap.backend.user.dto.PasswordChangeRequestDto;
import com.taptap.backend.user.service.UserPasswordService;

@Tag(name = "User", description = "유저 프로필 · 계정 관리 API")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;
    private final UserProfileService userProfileService;
    private final UserPasswordService userPasswordService;

    public UserController(AuthService authService, UserProfileService userProfileService, UserPasswordService userPasswordService) {
        this.authService = authService;
        this.userProfileService = userProfileService;
        this.userPasswordService = userPasswordService;
    }

    @Operation(summary = "9.1 유저 프로필 조회")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse.Get> getUserProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserProfileResponse.Get response = userProfileService.getUserProfile(userId);
        return ApiResponse.success("프로필 조회가 완료되었습니다.", response);
    }

    @Operation(summary = "9.1 유저 프로필 수정")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/profile")
    public ApiResponse<UserProfileResponse.Update> updateUserProfile(
            Authentication authentication,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        UserProfileResponse.Update response = userProfileService.updateUserProfile(userId, request);
        return ApiResponse.success("프로필이 수정되었습니다.", response);
    }

    @Operation(summary = "9.4 회원 탈퇴")
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

    @Operation(summary = "비밀번호 변경")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/password")
    public ApiResponse<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody PasswordChangeRequestDto request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        userPasswordService.changePassword(userId, request);
        return ApiResponse.<Void>success("비밀번호가 변경되었습니다.", null);
    }
}