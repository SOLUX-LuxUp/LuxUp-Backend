package com.taptap.backend.record.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "5.4 타임라인 상세 기록 추가 응답")
public class RecordDetailResponseDto {

    private Long recordId;
    private String memo;
    private String emoji;
    private LocalDateTime recordedAt;
}