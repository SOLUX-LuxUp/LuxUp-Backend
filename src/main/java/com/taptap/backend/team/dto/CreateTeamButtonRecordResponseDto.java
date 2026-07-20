package com.taptap.backend.team.dto;

import java.time.LocalDateTime;

public record CreateTeamButtonRecordResponseDto(
        Long recordId,
        Long teamButtonId,
        Long userId,
        LocalDateTime recordedAt,
        String memo,
        String emoji
) {
}
