package com.taptap.backend.insight.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ButtonTapCountDto {
    private Long buttonId;
    private String buttonName;
    private int count;
    private double ratio;
}