package com.taptap.backend.insight.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "12. 라이프스타일 추천 수락/거절 요청")
public class LifestyleRecommendationActionRequestDto {

    @Schema(description = "accept(수락) 또는 dismiss(거절)", example = "accept")
    private String action;
}