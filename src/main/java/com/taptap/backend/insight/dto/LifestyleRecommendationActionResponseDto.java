package com.taptap.backend.insight.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LifestyleRecommendationActionResponseDto {
    private Long recId;
    private String recType;
    private String action;
    private Long createdButtonId; // recType=ADD, action=accept일 때만 값 있음
}