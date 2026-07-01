package com.taptap.backend.setting.controller;

import com.taptap.backend.config.dto.ApiResponse;
import com.taptap.backend.setting.dto.NotificationSettingDto;
import com.taptap.backend.setting.service.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification Setting", description = "알림 전역 설정 관련 API (담당: 심세희)")
@RestController
@RequestMapping("/api/users/notification-settings")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;

    @Operation(summary = "9.2 알림 마스터 설정 조회")
    @GetMapping
    public ApiResponse<NotificationSettingDto.Response> getNotificationSettings(
            @RequestHeader(value = "X-Test-User-Id", defaultValue = "1") Long userId) {

        NotificationSettingDto.Response response = settingService.getNotificationSettings(userId);
        return ApiResponse.success("알림 설정 조회가 완료되었습니다.", response);
    }

    @Operation(summary = "9.2 알림 마스터 설정 수정")
    @PatchMapping
    public ApiResponse<NotificationSettingDto.Response> updateNotificationSettings(
            @RequestHeader(value = "X-Test-User-Id", defaultValue = "1") Long userId,
            @RequestBody NotificationSettingDto.UpdateRequest request) {

        NotificationSettingDto.Response response = settingService.updateNotificationSettings(userId, request);
        return ApiResponse.success("알림 설정이 수정되었습니다.", response);
    }
}