package com.taptap.backend.insight.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LifestyleRecommendationDto {
    private Long recId;
    private String recType; // ADD / DELETE

    // recType == ADD 일 때만 값 있음
    private String suggestedButtonName;
    private String suggestedIconName;
    private String suggestedIconColor;

    // recType == DELETE 일 때만 값 있음
    private Long buttonId;
    private String buttonName;
}