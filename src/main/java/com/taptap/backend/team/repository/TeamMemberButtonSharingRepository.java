package com.taptap.backend.team.repository;

import com.taptap.backend.team.entity.TeamMemberButtonSharing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberButtonSharingRepository extends JpaRepository<TeamMemberButtonSharing, Long> {
    List<TeamMemberButtonSharing> findAllByTeamIdAndUserId(Long teamId, Long userId);
    Optional<TeamMemberButtonSharing> findByTeamIdAndUserIdAndButtonId(Long teamId, Long userId, Long buttonId);
    List<TeamMemberButtonSharing> findAllByTeamIdAndUserIdAndIsSharedTrue(Long teamId, Long userId);
}
