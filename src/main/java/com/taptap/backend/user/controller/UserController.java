package com.taptap.backend.user.controller;

import com.taptap.backend.config.dto.ApiResponse;
import com.taptap.backend.user.dto.UserProfileResponse;
import com.taptap.backend.user.dto.UserProfileUpdateRequest;
import com.taptap.backend.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "9.1 User Profile", description = "유저 프로필 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    // ⚠️ 기존에는 setting.service.SettingService를 의존하고 있었는데,
    //    9.1(프로필)은 setting 도메인이 아니라 user 도메인 책임이라 UserProfileService로 교체했습니다.
    private final UserProfileService userProfileService;

    @Operation(summary = "9.1 유저 프로필 조회")
    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse.Get> getUserProfile(
            @RequestHeader(value = "X-Test-User-Id", defaultValue = "1") Long userId) {
        UserProfileResponse.Get response = userProfileService.getUserProfile(userId);
        return ApiResponse.success("프로필 조회가 완료되었습니다.", response);
    }

    @Operation(summary = "9.1 유저 프로필 수정")
    @PatchMapping("/profile")
    public ApiResponse<UserProfileResponse.Update> updateUserProfile(
            @RequestHeader(value = "X-Test-User-Id", defaultValue = "1") Long userId,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        UserProfileResponse.Update response = userProfileService.updateUserProfile(userId, request);
        return ApiResponse.success("프로필이 수정되었습니다.", response);
    }
}