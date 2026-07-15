package com.taptap.backend.record.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "4.7 마지막 기록 시간 조회 응답")
public class RecordLatestResponseDto {

    private Long buttonId;
    private LocalDateTime lastRecordedAt;
    private long elapsedSeconds;
}