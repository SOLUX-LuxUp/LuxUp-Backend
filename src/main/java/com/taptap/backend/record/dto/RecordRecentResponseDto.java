package com.taptap.backend.record.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "5.1 최근 기록 조회(홈) 응답")
public class RecordRecentResponseDto {

    private Long buttonId;
    private String buttonName;
    private String iconName;
    private String iconColor;
    private LocalDateTime lastRecordedAt;
}