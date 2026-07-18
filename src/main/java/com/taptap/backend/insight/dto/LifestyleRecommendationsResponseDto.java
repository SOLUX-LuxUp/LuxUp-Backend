package com.taptap.backend.insight.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "이 달의 라이프스타일 + 라이프스타일 추천 통합 응답")
public class LifestyleRecommendationsResponseDto {
    // 이번 달 기록이 7일 이상 & 20회 이상일 때만 true. false면 lifestyleLabel은 null.
    private boolean analysisAvailable;
    private String lifestyleLabel;
    private String lifestyleCaption;
    private List<AnalysisButtonDto> analysisButtons;
    private List<LifestyleRecommendationDto> recommendations;
}