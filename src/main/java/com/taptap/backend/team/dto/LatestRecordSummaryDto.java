package com.taptap.backend.team.dto;

import java.time.LocalDateTime;
import java.util.List;

public record LatestRecordSummaryDto(
        LocalDateTime recordedAt,
        List<MemberProfileDto> recordedBy,
        Integer recordedByCount
) {
}
