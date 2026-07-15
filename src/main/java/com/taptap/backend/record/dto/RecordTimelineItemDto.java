package com.taptap.backend.record.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "타임라인 개별 기록 항목")
public class RecordTimelineItemDto {

    private Long recordId;
    private LocalDateTime recordedAt;
    private String memo;
    private String emoji;
}