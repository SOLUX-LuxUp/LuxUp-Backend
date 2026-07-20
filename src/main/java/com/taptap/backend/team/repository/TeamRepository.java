package com.taptap.backend.team.repository;

import com.taptap.backend.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByInviteCode(String inviteCode);
    Optional<Team> findByInviteCodeAndDeletedAtIsNull(String inviteCode);
    Optional<Team> findByTeamIdAndDeletedAtIsNull(Long teamId);
}
