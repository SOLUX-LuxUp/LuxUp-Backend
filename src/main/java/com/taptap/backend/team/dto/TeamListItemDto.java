package com.taptap.backend.team.dto;

import java.time.LocalDateTime;
import java.util.List;

// 참고: latestRecord / recentUpdatedMembers는 team_button 브랜치 머지 후 추가 예정
public record TeamListItemDto(
        Long teamId,
        String teamName,
        String teamImageUrl,
        String iconName,
        String iconColor,
        Boolean isFavorite,
        Integer maxMember,
        Long memberCount,
        List<MemberProfileDto> memberProfiles,
        LocalDateTime updatedAt
) {
}
