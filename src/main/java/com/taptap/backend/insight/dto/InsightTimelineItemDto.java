package com.taptap.backend.insight.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InsightTimelineItemDto {
    private Long recordId;
    private Long buttonId;
    private String buttonName;
    private LocalDateTime recordedAt;
    private String memo;
    private String emoji;
}