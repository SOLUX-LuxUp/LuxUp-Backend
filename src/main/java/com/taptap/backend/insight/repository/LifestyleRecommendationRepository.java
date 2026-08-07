package com.taptap.backend.insight.repository;

import com.taptap.backend.insight.entity.LifestyleRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LifestyleRecommendationRepository extends JpaRepository<LifestyleRecommendation, Long> {

    // 수락도 거절도 안 했고, 만료(expires_at)도 안 지난 "아직 유효한" 추천들
    @Query("SELECT lr FROM LifestyleRecommendation lr WHERE lr.userId = :userId " +
            "AND lr.isAccepted = false AND lr.isDismissed = false " +
            "AND (lr.expiresAt IS NULL OR lr.expiresAt > CURRENT_TIMESTAMP)")
    List<LifestyleRecommendation> findActiveByUserId(@Param("userId") Long userId);

    // 이번 달에 이미 ADD 추천을 생성한 적이 있는지 (수락/거절 여부와 무관하게, "이번 달에 1번 생성했는가"만 확인)
    boolean existsByUserIdAndRecTypeAndCreatedAtBetween(
            Long userId, String recType, LocalDateTime start, LocalDateTime end
    );

    // 특정 달(createdAt 기준)에 생성됐고, 아직 수락/거절 안 된 추천 목록.
    // 먼슬리 인사이트에서 과거 달을 조회할 때도 "그 달의 ADD 추천"을 그대로 보여줘야 하므로,
    // findActiveByUserId와 달리 expiresAt(만료) 조건은 걸지 않는다.
    @Query("SELECT lr FROM LifestyleRecommendation lr WHERE lr.userId = :userId " +
            "AND lr.recType = :recType " +
            "AND lr.isAccepted = false AND lr.isDismissed = false " +
            "AND lr.createdAt >= :start AND lr.createdAt < :end")
    List<LifestyleRecommendation> findByUserIdAndRecTypeAndCreatedAtBetweenAndNotActioned(
            @Param("userId") Long userId, @Param("recType") String recType,
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end
    );
}