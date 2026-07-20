package com.taptap.backend.button.repository;

import com.taptap.backend.button.entity.ButtonCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ButtonCategoryRepository extends JpaRepository<ButtonCategory, Long> {
    List<ButtonCategory> findAllByUserId(Long userId);
    boolean existsByCategoryIdAndUserId(Long categoryId, Long userId);
    boolean existsByUserIdAndCategoryNameAndDeletedAtIsNull(Long userId, String categoryName);
    boolean existsByUserIdAndCategoryNameAndDeletedAtIsNullAndCategoryIdNot(Long userId, String categoryName, Long categoryId);

    @Query("SELECT MAX(c.displayOrder) FROM ButtonCategory c WHERE c.userId = :userId AND c.deletedAt IS NULL")
    Integer findMaxDisplayOrderByUserId(@Param("userId") Long userId);
}