package com.taptap.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "9.1 프로필 수정 요청")
public class UserProfileUpdateRequest {
    @Schema(description = "유저 이름 수정", example = "새이름")
    private String username;

    @Schema(description = "프로필 사진 수정 URL", example = "https://cdn.example.com/profile/new.jpg")
    private String profileImageUrl;
}