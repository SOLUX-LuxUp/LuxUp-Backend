package com.taptap.backend.team.dto;

import java.time.LocalDateTime;

// 참고: latestRecord(최근 기록 버튼)는 team_button 브랜치 머지 후 추가 예정
public record TeamMemberListItemDto(
        Long userId,
        String displayName,
        String profileImageUrl,
        String role,
        LocalDateTime joinedAt
) {
}
