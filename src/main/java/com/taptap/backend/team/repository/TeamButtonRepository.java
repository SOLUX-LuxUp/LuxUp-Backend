package com.taptap.backend.team.repository;

import com.taptap.backend.team.entity.TeamButton;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamButtonRepository extends JpaRepository<TeamButton, Long> {
    Optional<TeamButton> findByTeamButtonIdAndTeamIdAndDeletedAtIsNull(Long teamButtonId, Long teamId);
    List<TeamButton> findAllByTeamIdAndIsActiveTrueAndDeletedAtIsNull(Long teamId);
}
