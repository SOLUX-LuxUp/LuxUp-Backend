package com.taptap.backend.team.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_button")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamButton {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_button_id")
    private Long teamButtonId;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    // ERD 갭 보완: 팀 버튼 카테고리 FK 신규 (기존 ERD엔 없었음)
    @Column(name = "category_id")
    private Long categoryId;

    @Builder.Default
    @Column(name = "button_name", nullable = false, length = 100)
    private String buttonName = "새로운 버튼";

    @Column(name = "icon_name", length = 100)
    private String iconName;

    // 색상 토큰 문자열 (개인 button과 동일 컨벤션). HEX 아님.
    @Column(name = "icon_color", length = 20)
    private String iconColor;

    @Column(name = "description", length = 50)
    private String description;

    // 팀 단위 생성/수정/삭제 권한은 Team.buttonCreate/Edit/DeletePermission이 담당.
    // tapPermission(실행 권한)만 버튼별 개별 설정 유지 (디자인상 버튼 생성 화면에 있음)
    @Builder.Default
    @Column(name = "tap_permission", nullable = false, length = 20)
    private String tapPermission = "all"; // all / custom (팀장도 일반 멤버와 동일하게 custom의 allowedUserIds에 포함되어야 선택 가능 — 별도 owner_only 옵션 없음, 디자인 확인 완료)

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

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
}
