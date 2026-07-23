package com.taptap.backend.team.dto;

import java.time.LocalDateTime;

public record UpdateTeamButtonRecordDetailResponseDto(
        Long recordId,
        Long teamButtonId,
        String memo,
        String emoji,
        LocalDateTime recordedAt
) {
}
