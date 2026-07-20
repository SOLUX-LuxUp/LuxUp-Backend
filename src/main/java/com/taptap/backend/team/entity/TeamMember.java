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
