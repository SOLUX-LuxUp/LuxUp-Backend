package com.taptap.backend.insight.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

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
    private LocalDateTime lastRecordedAt; // 기록이 아예 없던 버튼이면 null (그 경우 화면에서 "기록 없음" 등으로 처리)
}