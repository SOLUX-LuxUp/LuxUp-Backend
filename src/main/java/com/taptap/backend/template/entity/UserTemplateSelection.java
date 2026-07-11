package com.taptap.backend.template.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_template_selection")
public class UserTemplateSelection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "selection_id")
    private Long selectionId;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "selected_at", updatable = false)
    private LocalDateTime selectedAt;

    @PrePersist
    void onCreate() {
        selectedAt = LocalDateTime.now();
    }

    public Long getSelectionId() {
        return selectionId;
    }

    public void setSelectionId(Long selectionId) {
        this.selectionId = selectionId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public LocalDateTime getSelectedAt() {
        return selectedAt;
    }
}