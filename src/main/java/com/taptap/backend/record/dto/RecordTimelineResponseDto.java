package com.taptap.backend.record.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "5.3 타임라인 조회 응답")
public class RecordTimelineResponseDto {

    private List<RecordTimelineItemDto> records;
    private Long nextCursor;
    private boolean hasMore;
}