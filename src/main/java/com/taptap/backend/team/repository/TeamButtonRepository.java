package com.taptap.backend.team.repository;

import com.taptap.backend.team.entity.TeamButton;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamButtonRepository extends JpaRepository<TeamButton, Long> {
    Optional<TeamButton> findByTeamButtonIdAndTeamIdAndDeletedAtIsNull(Long teamButtonId, Long teamId);
    List<TeamButton> findAllByTeamIdAndIsActiveTrueAndDeletedAtIsNull(Long teamId);

    // 팀 인사이트 - 삭제된 버튼도 과거 기록의 이름 표시를 위해 포함
    List<TeamButton> findAllByTeamId(Long teamId);

    // 팀 하드 삭제 배치 - 팀에 속한 모든 팀 버튼 완전 삭제
    @Modifying
    @Query("DELETE FROM TeamButton b WHERE b.teamId = :teamId")
    void deleteAllByTeamId(@Param("teamId") Long teamId);
}
