package com.taptap.backend.setting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "9.2 알림 마스터 설정 수정 요청")
public class NotificationSettingUpdateRequest {
    private Boolean masterEnabled;
    private Boolean soundEnabled;
    private Boolean vibrationEnabled;
    private Boolean popupOverlay;
}