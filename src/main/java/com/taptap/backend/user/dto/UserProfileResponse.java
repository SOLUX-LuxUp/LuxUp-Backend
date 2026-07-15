package com.taptap.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

public class UserProfileResponse {

    @Getter
    @Builder
    @Schema(description = "9.1 프로필 조회 응답 (명세서 27p 기준)")
    public static class Get {
        private Long userId;
        private String username;
        private String profileImageUrl;
        private String loginType;
        private String email;
    }

    @Getter
    @Builder
    @Schema(description = "9.1 프로필 수정 응답 (명세서 28p 기준 - 필드 3개만)")
    public static class Update {
        private Long userId;
        private String username;
        private String profileImageUrl;
    }
}