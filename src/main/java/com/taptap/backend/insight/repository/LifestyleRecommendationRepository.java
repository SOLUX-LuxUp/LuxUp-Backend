package com.taptap.backend.insight.repository;

import com.taptap.backend.insight.entity.LifestyleRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LifestyleRecommendationRepository extends JpaRepository<LifestyleRecommendation, Long> {

    // 수락도 거절도 안 했고, 만료(expires_at)도 안 지난 "아직 유효한" 추천들
    @Query("SELECT lr FROM LifestyleRecommendation lr WHERE lr.userId = :userId " +
            "AND lr.isAccepted = false AND lr.isDismissed = false " +
            "AND (lr.expiresAt IS NULL OR lr.expiresAt > CURRENT_TIMESTAMP)")
    List<LifestyleRecommendation> findActiveByUserId(@Param("userId") Long userId);
}