package com.taptap.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

public class UserProfileDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "프로필 수정 요청 스펙")
    public static class UpdateRequest {
        @Schema(description = "변경할 유저 이름", example = "새이름")
        private String username;

        @Schema(description = "변경할 프로필 이미지 URL", example = "https://cdn.example.com/profile/new.jpg")
        private String profileImageUrl;
    }

    @Getter
    @Builder
    @Schema(description = "프로필 조회 응답 스펙")
    public static class Response {
        @Schema(description = "유저 고유 고유 ID", example = "1")
        private Long userId;

        @Schema(description = "유저 이름", example = "심세희")
        private String username;

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profile/1.jpg")
        private String profileImageUrl;

        @Schema(description = "가입 방식 구분", example = "EMAIL")
        private String loginType;

        @Schema(description = "연동된 이메일 계정", example = "user@example.com")
        private String email;
    }
}