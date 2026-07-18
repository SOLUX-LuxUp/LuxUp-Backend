package com.taptap.backend.insight.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryTapCountDto {
    private Long categoryId;
    private String categoryName;
    private int count;
    private double ratio;
}