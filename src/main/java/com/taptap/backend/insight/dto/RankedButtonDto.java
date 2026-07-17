package com.taptap.backend.insight.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RankedButtonDto {
    private int rank;
    private Long buttonId;
    private String buttonName;
    private int count;
}