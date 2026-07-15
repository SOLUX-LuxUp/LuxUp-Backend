package com.taptap.backend.setting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "9.2 알림 마스터 설정 조회 및 수정 응답")
public class NotificationSettingResponse {
    @Schema(description = "전체 알림 ON/OFF 토글", example = "true")
    private Boolean masterEnabled;

    @Schema(description = "소리 알림 ON/OFF", example = "true")
    private Boolean soundEnabled;

    @Schema(description = "진동 알림 ON/OFF", example = "true")
    private Boolean vibrationEnabled;

    @Schema(description = "팝업 전역 표출 방식 (다른 화면 위에 표시)", example = "false")
    private Boolean popupOverlay;
}