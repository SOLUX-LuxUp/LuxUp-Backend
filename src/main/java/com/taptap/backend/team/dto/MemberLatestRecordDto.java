package com.taptap.backend.team.dto;

import java.time.LocalDateTime;

public record MemberLatestRecordDto(
        String buttonName,
        LocalDateTime recordedAt
) {
}
