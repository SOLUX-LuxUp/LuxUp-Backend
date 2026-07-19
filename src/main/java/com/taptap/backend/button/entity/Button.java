package com.taptap.backend.button.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "button")
public class Button {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "button_id")
    private Long buttonId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "source_preset_id")
    private Long sourcePresetId;

    @Column(name = "button_name", nullable = false, length = 50)
    private String buttonName = "새로운 버튼 1";

    @Column(name = "icon_name", nullable = false, length = 100)
    private String iconName;

    @Column(name = "icon_color", nullable = false, length = 20)
    private String iconColor;

    @Column(name = "goal_enabled", nullable = false)
    private Boolean goalEnabled = false;

    @Column(name = "goal_name", length = 50)
    private String goalName;

    @Column(name = "goal_period_unit", length = 10)
    private String goalPeriodUnit;

    @Column(name = "goal_count")
    private Integer goalCount;

    @Column(name = "goal_comparison_type", length = 10)
    private String goalComparisonType;

    @Column(name = "expired_at")
    private LocalDate expiredAt;

    @Column(name = "expiry_enabled", nullable = false)
    private Boolean expiryEnabled = false;

    @Column(name = "is_favorite", nullable = false)
    private Boolean isFavorite = false;

    @Column(name = "favorite_order")
    private Integer favoriteOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "expiry_d3_notified", nullable = false)
    private Boolean expiryD3Notified = false;

    @Column(name = "expiry_d0_notified", nullable = false)
    private Boolean expiryD0Notified = false;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getButtonId() {
        return buttonId;
    }

    public void setButtonId(Long buttonId) {
        this.buttonId = buttonId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getSourcePresetId() {
        return sourcePresetId;
    }

    public void setSourcePresetId(Long sourcePresetId) {
        this.sourcePresetId = sourcePresetId;
    }

    public String getButtonName() {
        return buttonName;
    }

    public void setButtonName(String buttonName) {
        this.buttonName = buttonName;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getIconColor() {
        return iconColor;
    }

    public void setIconColor(String iconColor) {
        this.iconColor = iconColor;
    }

    public Boolean getGoalEnabled() {
        return goalEnabled;
    }

    public void setGoalEnabled(Boolean goalEnabled) {
        this.goalEnabled = goalEnabled;
    }

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public String getGoalPeriodUnit() {
        return goalPeriodUnit;
    }

    public void setGoalPeriodUnit(String goalPeriodUnit) {
        this.goalPeriodUnit = goalPeriodUnit;
    }

    public Integer getGoalCount() {
        return goalCount;
    }

    public void setGoalCount(Integer goalCount) {
        this.goalCount = goalCount;
    }

    public String getGoalComparisonType() {
        return goalComparisonType;
    }

    public void setGoalComparisonType(String goalComparisonType) {
        this.goalComparisonType = goalComparisonType;
    }

    public LocalDate getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(LocalDate expiredAt) {
        this.expiredAt = expiredAt;
    }

    public Boolean getExpiryEnabled() {
        return expiryEnabled;
    }

    public void setExpiryEnabled(Boolean expiryEnabled) {
        this.expiryEnabled = expiryEnabled;
    }

    public Boolean getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public Integer getFavoriteOrder() {
        return favoriteOrder;
    }

    public void setFavoriteOrder(Integer favoriteOrder) {
        this.favoriteOrder = favoriteOrder;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Boolean getExpiryD3Notified() {
        return expiryD3Notified;
    }

    public void setExpiryD3Notified(Boolean expiryD3Notified) {
        this.expiryD3Notified = expiryD3Notified;
    }

    public Boolean getExpiryD0Notified() {
        return expiryD0Notified;
    }

    public void setExpiryD0Notified(Boolean expiryD0Notified) {
        this.expiryD0Notified = expiryD0Notified;
    }
}