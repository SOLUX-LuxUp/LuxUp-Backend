package com.taptap.backend.team.dto;

import java.util.List;

public record TeamButtonTimelineResponseDto(
        List<TeamButtonTimelineItemDto> records,
        Boolean hasMore,
        Long nextCursor
) {
}
