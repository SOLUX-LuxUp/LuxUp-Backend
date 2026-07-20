package com.taptap.backend.team.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// ERD 갭 보완: 팀 버튼용 카테고리 테이블 신규 (개인 button_category와 별개)
@Entity
@Table(name = "team_button_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamButtonCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;

    @Column(name = "category_color", length = 20)
    private String categoryColor;

    @Builder.Default
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
