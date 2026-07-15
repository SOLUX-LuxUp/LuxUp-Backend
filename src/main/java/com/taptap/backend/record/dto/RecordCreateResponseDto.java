package com.taptap.backend.record.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "4.2 행동 기록(탭) 응답")
public class RecordCreateResponseDto {

    private Long recordId;
    private Long buttonId;
    private LocalDateTime recordedAt;
}