package com.taptap.backend.setting.controller;

import com.taptap.backend.config.ApiResponse;
import com.taptap.backend.setting.dto.NotificationSettingResponse;
import com.taptap.backend.setting.dto.NotificationSettingUpdateRequest;
import com.taptap.backend.setting.service.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "9.2 Notification Setting", description = "알림 전역 설정 관련 API")
@RestController
@RequestMapping("/api/users/notification-settings")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;

    @Operation(summary = "9.2 알림 마스터 설정 조회")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ApiResponse<NotificationSettingResponse> getNotificationSettings(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        NotificationSettingResponse response = settingService.getNotificationSettings(userId);
        return ApiResponse.success("알림 설정 조회가 완료되었습니다.", response);
    }

    @Operation(summary = "9.2 알림 마스터 설정 수정")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping
    public ApiResponse<NotificationSettingResponse> updateNotificationSettings(
            Authentication authentication,
            @RequestBody NotificationSettingUpdateRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        NotificationSettingResponse response = settingService.updateNotificationSettings(userId, request);
        return ApiResponse.success("알림 설정이 저장되었습니다.", response);
    }
}