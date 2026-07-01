package com.taptap.backend.setting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

public class NotificationSettingDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "알림 마스터 설정 수정 요청 스펙")
    public static class UpdateRequest {
        @Schema(description = "전체 알림 ON/OFF 토글", example = "true")
        private Boolean masterEnabled;

        @Schema(description = "소리 알림 ON/OFF", example = "false")
        private Boolean soundEnabled;

        @Schema(description = "진동 알림 ON/OFF", example = "true")
        private Boolean vibrationEnabled;

        @Schema(description = "다른 화면 위에 표시 (0=팝업만, 1=다른 화면 위에)", example = "true")
        private Boolean popupOverlay;
    }

    @Getter
    @Builder
    @Schema(description = "알림 마스터 설정 응답 스펙")
    public static class Response {
        @Schema(description = "전체 알림 ON/OFF 상태", example = "true")
        private Boolean masterEnabled;

        @Schema(description = "소리 알림 ON/OFF 상태", example = "true")
        private Boolean soundEnabled;

        @Schema(description = "진동 알림 ON/OFF 상태", example = "true")
        private Boolean vibrationEnabled;

        @Schema(description = "팝업 전역 표출 방식 상태", example = "false")
        private Boolean popupOverlay;
    }
}