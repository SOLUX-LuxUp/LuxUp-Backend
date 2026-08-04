package com.taptap.backend.team.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_member_id")
    private Long teamMemberId;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    // "팀 내 프로필 수정"으로 프로필 사진을 직접 커스터마이징했는지 여부.
    // false면 개인 파트 프로필 사진을 실시간으로 반영하고, true면 이 값을 그대로 유지한다. (기획 확정 A안, 김누리님)
    @Builder.Default
    @Column(name = "profile_image_customized", nullable = false)
    private Boolean profileImageCustomized = false;

    @Builder.Default
    @Column(name = "role", nullable = false, length = 20)
    private String role = "member"; // owner / member

    @Builder.Default
    @Column(name = "is_button_public", nullable = false)
    private Boolean isButtonPublic = true;

    @Builder.Default
    @Column(name = "is_favorite", nullable = false)
    private Boolean isFavorite = false;

    @Builder.Default
    @Column(name = "is_notification", nullable = false)
    private Boolean isNotification = true;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    // 팀 탈퇴/강제추방 시각 (소프트 딜리트)
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    void onCreate() {
        joinedAt = LocalDateTime.now();
    }

    public boolean isOwner() {
        return "owner".equals(role);
    }
}
