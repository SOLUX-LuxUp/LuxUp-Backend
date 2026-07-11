package com.taptap.backend.button.repository;

import com.taptap.backend.button.entity.Button;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ButtonRepository extends JpaRepository<Button, Long> {

    @Query("SELECT b.buttonId FROM Button b WHERE b.userId = :userId AND b.isActive = true")
    List<Long> findActiveButtonIdsByUserId(@Param("userId") Long userId);
    long countByUserId(Long userId);

    @Modifying
    @Query("UPDATE Button b SET b.isActive = false WHERE b.userId = :userId")
    void deactivateAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Button b SET b.isActive = false WHERE b.categoryId = :categoryId")
    void deactivateByCategoryId(@Param("categoryId") Long categoryId);

    @Modifying
    @Query("UPDATE Button b SET b.categoryId = null WHERE b.categoryId = :categoryId")
    void clearCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT MAX(b.favoriteOrder) FROM Button b WHERE b.userId = :userId AND b.isFavorite = true")
    Integer findMaxFavoriteOrderByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Button b SET b.favoriteOrder = b.favoriteOrder - 1 WHERE b.userId = :userId AND b.favoriteOrder > :order")
    void decrementFavoriteOrderAfter(@Param("userId") Long userId, @Param("order") Integer order);

    List<Button> findByUserIdAndIsActiveTrue(Long userId);
}