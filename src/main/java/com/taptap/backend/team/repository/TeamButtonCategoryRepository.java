package com.taptap.backend.team.repository;

import com.taptap.backend.team.entity.TeamButtonCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamButtonCategoryRepository extends JpaRepository<TeamButtonCategory, Long> {
    List<TeamButtonCategory> findAllByTeamIdAndDeletedAtIsNullOrderByDisplayOrderAsc(Long teamId);
    List<TeamButtonCategory> findAllByTeamId(Long teamId);
    boolean existsByTeamIdAndCategoryNameAndDeletedAtIsNull(Long teamId, String categoryName);

    @Query("SELECT MAX(c.displayOrder) FROM TeamButtonCategory c WHERE c.teamId = :teamId AND c.deletedAt IS NULL")
    Integer findMaxDisplayOrderByTeamId(@Param("teamId") Long teamId);

    // 팀 하드 삭제 배치 - 팀에 속한 모든 카테고리 완전 삭제
    @Modifying
    @Query("DELETE FROM TeamButtonCategory c WHERE c.teamId = :teamId")
    void deleteAllByTeamId(@Param("teamId") Long teamId);
}
