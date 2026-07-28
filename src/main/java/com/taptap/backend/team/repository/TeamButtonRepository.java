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

    // 팀 버튼 카테고리 삭제(delete_buttons=true) - 해당 카테고리 버튼들도 함께 soft delete
    @Modifying
    @Query("UPDATE TeamButton b SET b.isActive = false, b.deletedAt = CURRENT_TIMESTAMP WHERE b.teamId = :teamId AND b.categoryId = :categoryId AND b.deletedAt IS NULL")
    void deactivateByTeamIdAndCategoryId(@Param("teamId") Long teamId, @Param("categoryId") Long categoryId);

    // 팀 버튼 카테고리 삭제(delete_buttons=false) - 버튼은 유지하고 categoryId만 초기화(No Category로 이동)
    @Modifying
    @Query("UPDATE TeamButton b SET b.categoryId = null WHERE b.teamId = :teamId AND b.categoryId = :categoryId")
    void clearCategoryIdByTeamIdAndCategoryId(@Param("teamId") Long teamId, @Param("categoryId") Long categoryId);
}
