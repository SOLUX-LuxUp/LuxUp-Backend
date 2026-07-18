package com.taptap.backend.insight.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "이 달의 라이프스타일 + 라이프스타일 추천 통합 응답")
public class LifestyleRecommendationsResponseDto {
    private String lifestyleLabel;
    private String lifestyleCaption;
    private List<AnalysisButtonDto> analysisButtons;
    private List<LifestyleRecommendationDto> recommendations;
}