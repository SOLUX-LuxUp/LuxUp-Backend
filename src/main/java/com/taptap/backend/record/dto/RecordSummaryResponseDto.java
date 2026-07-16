package com.taptap.backend.record.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "5.2 최근 기록 조회(버튼 상세) 응답")
public class RecordSummaryResponseDto {

    private Long buttonId;
    private LocalDateTime lastRecordedAt;
    private long todayCount;
    private long totalCount;
}