package com.taptap.backend.user.controller;

import com.taptap.backend.config.dto.ApiResponse;
import com.taptap.backend.setting.service.SettingService;
import com.taptap.backend.user.dto.UserProfileDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Profile", description = "유저 프로필 관련 API (담당: 심세희)")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final SettingService settingService;

    @Operation(summary = "9.1 유저 프로필 조회")
    @GetMapping("/profile")
    public ApiResponse<UserProfileDto.Response> getUserProfile(
            @RequestHeader(value = "X-Test-User-Id", defaultValue = "1") Long userId) {

        UserProfileDto.Response response = settingService.getUserProfile(userId);
        return ApiResponse.success("프로필 조회가 완료되었습니다.", response);
    }

    @Operation(summary = "9.1 유저 프로필 수정")
    @PatchMapping("/profile")
    public ApiResponse<UserProfileDto.Response> updateUserProfile(
            @RequestHeader(value = "X-Test-User-Id", defaultValue = "1") Long userId,
            @RequestBody UserProfileDto.UpdateRequest request) {

        UserProfileDto.Response response = settingService.updateUserProfile(userId, request);
        return ApiResponse.success("프로필이 수정되었습니다.", response);
    }
}