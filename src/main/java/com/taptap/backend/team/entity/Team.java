package com.taptap.backend.team.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "team")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long teamId;

    @Column(name = "team_name", nullable = false, length = 255)
    private String teamName;

    @Column(name = "team_image_url")
    private String teamImageUrl;

    @Column(name = "icon_name", length = 100)
    private String iconName;

    @Column(name = "icon_color", length = 20)
    private String iconColor;

    @Column(name = "invite_code", nullable = false, unique = true, length = 20)
    private String inviteCode;

    @Builder.Default
    @Column(name = "max_member", nullable = false)
    private Integer maxMember = 10;

    // 팀 권한 관리 화면 기준: 버튼 생성/수정/삭제 권한을 분리 (기존 tap/delete permission 대체)
    @Builder.Default
    @Column(name = "button_create_permission", nullable = false, length = 20)
    private String buttonCreatePermission = "anyone"; // anyone / leader_only

    @Builder.Default
    @Column(name = "button_edit_permission", nullable = false, length = 20)
    private String buttonEditPermission = "creator_or_leader"; // creator_or_leader / leader_only

    @Builder.Default
    @Column(name = "button_delete_permission", nullable = false, length = 20)
    private String buttonDeletePermission = "leader_only"; // creator_or_leader / leader_only

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    // 팀 삭제 요청 시각. 실제 하드 삭제는 배치 잡이 deletedAt + 3일 시점에 처리 (스케줄러는 별도 작업 필요)
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getScheduledDeletionAt() {
        return deletedAt == null ? null : deletedAt.plusDays(3);
    }
}
