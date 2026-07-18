package com.taptap.backend.insight.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class DailyTapCountDto {
    private String date;
    private int total;
    private Map<Long, Integer> categories;
}