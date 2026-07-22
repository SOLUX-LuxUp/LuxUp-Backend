package com.taptap.backend.team.repository;

import com.taptap.backend.team.entity.TeamButtonRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TeamButtonRecordRepository extends JpaRepository<TeamButtonRecord, Long> {
    Optional<TeamButtonRecord> findFirstByTeamButtonIdAndDeletedAtIsNullOrderByRecordedAtDesc(Long teamButtonId);

    @Query("SELECT r FROM TeamButtonRecord r WHERE r.teamButtonId = :teamButtonId AND r.deletedAt IS NULL " +
            "AND (:cursor IS NULL OR r.recordId < :cursor) ORDER BY r.recordId DESC")
    List<TeamButtonRecord> findTimeline(@Param("teamButtonId") Long teamButtonId, @Param("cursor") Long cursor,
                                         org.springframework.data.domain.Pageable pageable);

    long countByTeamButtonIdAndUserIdAndDeletedAtIsNull(Long teamButtonId, Long userId);

    // 팀원 목록 조회 - 팀원별 최근 팀 버튼 기록 1건
    Optional<TeamButtonRecord> findFirstByTeamIdAndUserIdAndDeletedAtIsNullOrderByRecordedAtDesc(Long teamId, Long userId);

    // 팀 인사이트 - 특정 기간(start 이상 ~ end 미만) 팀 전체 기록
    List<TeamButtonRecord> findAllByTeamIdAndDeletedAtIsNullAndRecordedAtGreaterThanEqualAndRecordedAtLessThan(
            Long teamId, LocalDateTime start, LocalDateTime end);

    // 팀 목록 조회 - 팀 최근 기록(latestRecord) + 최근 활동 멤버(recentUpdatedMembers) 계산용
    List<TeamButtonRecord> findTop20ByTeamIdAndDeletedAtIsNullOrderByRecordedAtDesc(Long teamId);

    // 팀 하드 삭제 배치 - 팀에 속한 모든 팀 버튼 기록 완전 삭제
    @Modifying
    @Query("DELETE FROM TeamButtonRecord r WHERE r.teamId = :teamId")
    void deleteAllByTeamId(@Param("teamId") Long teamId);
}
