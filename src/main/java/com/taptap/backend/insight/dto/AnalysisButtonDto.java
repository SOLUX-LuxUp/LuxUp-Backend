package com.taptap.backend.insight.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnalysisButtonDto {
    private Long buttonId;
    private String buttonName;
    private String iconName;
    private String iconColor;
}