package com.taptap.backend.team.repository;

import com.taptap.backend.team.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    Optional<TeamMember> findByTeamIdAndUserIdAndDeletedAtIsNull(Long teamId, Long userId);
    List<TeamMember> findAllByTeamIdAndDeletedAtIsNullOrderByJoinedAtAsc(Long teamId);
    long countByTeamIdAndDeletedAtIsNull(Long teamId);
    boolean existsByTeamIdAndUserIdAndDeletedAtIsNull(Long teamId, Long userId);

    @Query("SELECT tm FROM TeamMember tm WHERE tm.teamId = :teamId AND tm.role = 'owner' AND tm.deletedAt IS NULL")
    Optional<TeamMember> findOwnerByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT tm.teamId FROM TeamMember tm WHERE tm.userId = :userId AND tm.deletedAt IS NULL")
    List<Long> findTeamIdsByUserId(@Param("userId") Long userId);
}
