package com.taptap.backend.team.repository;

import com.taptap.backend.team.entity.TeamMemberButtonSharing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMemberButtonSharingRepository extends JpaRepository<TeamMemberButtonSharing, Long> {
    List<TeamMemberButtonSharing> findAllByTeamIdAndUserId(Long teamId, Long userId);
    Optional<TeamMemberButtonSharing> findByTeamIdAndUserIdAndButtonId(Long teamId, Long userId, Long buttonId);
    List<TeamMemberButtonSharing> findAllByTeamIdAndUserIdAndIsSharedTrue(Long teamId, Long userId);

    // 팀 하드 삭제 배치 - 팀에 속한 모든 개인 버튼 공유 설정 완전 삭제
    @Modifying
    @Query("DELETE FROM TeamMemberButtonSharing s WHERE s.teamId = :teamId")
    void deleteAllByTeamId(@Param("teamId") Long teamId);
}
