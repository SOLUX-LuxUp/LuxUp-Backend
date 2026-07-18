package com.taptap.backend.record.repository;

import com.taptap.backend.record.entity.ButtonRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ButtonRecordRepository extends JpaRepository<ButtonRecord, Long> {

    @Modifying
    @Query("UPDATE ButtonRecord br SET br.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE br.buttonId IN :buttonIds AND br.deletedAt IS NULL")
    void softDeleteByButtonIds(@Param("buttonIds") List<Long> buttonIds);

    // 4.7 마지막 기록 시간 조회 - 소프트 삭제 안 된 것 중 가장 최근 기록 1건
    Optional<ButtonRecord> findTopByButtonIdAndDeletedAtIsNullOrderByRecordedAtDesc(Long buttonId);

    // 5.1 최근 기록 조회(홈) - 여러 버튼 중 소프트 삭제 안 된 것 중 가장 최근 기록 1건
    Optional<ButtonRecord> findTopByButtonIdInAndDeletedAtIsNullOrderByRecordedAtDesc(List<Long> buttonIds);

    // 5.2 최근 기록 조회(버튼 상세) - 총 기록 횟수
    long countByButtonIdAndDeletedAtIsNull(Long buttonId);

    // 5.2 최근 기록 조회(버튼 상세) - 오늘 기록 횟수
    @Query("SELECT COUNT(br) FROM ButtonRecord br WHERE br.buttonId = :buttonId AND br.deletedAt IS NULL " +
            "AND br.recordedAt >= :startOfDay AND br.recordedAt < :endOfDay")
    long countTodayByButtonId(
            @Param("buttonId") Long buttonId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    // 5.3 타임라인 조회 - 커서(recordId) 기반 페이지네이션, 최신순
    @Query("SELECT br FROM ButtonRecord br WHERE br.buttonId = :buttonId AND br.deletedAt IS NULL " +
            "AND (:cursor IS NULL OR br.recordId < :cursor) ORDER BY br.recordId DESC")
    List<ButtonRecord> findTimeline(
            @Param("buttonId") Long buttonId,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    // 9~11. 인사이트 - 여러 버튼의 특정 기간(start 이상 ~ end 미만) 기록, 최신순
    @Query("SELECT br FROM ButtonRecord br WHERE br.buttonId IN :buttonIds AND br.deletedAt IS NULL " +
            "AND br.recordedAt >= :start AND br.recordedAt < :end ORDER BY br.recordedAt DESC")
    List<ButtonRecord> findRecordsInRange(
            @Param("buttonIds") List<Long> buttonIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}