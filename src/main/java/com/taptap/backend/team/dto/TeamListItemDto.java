package com.taptap.backend.team.dto;

import java.time.LocalDateTime;
import java.util.List;

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
        TeamLatestRecordDto latestRecord,
        List<MemberProfileDto> recentUpdatedMembers,
        LocalDateTime updatedAt
) {
}
