package com.taptap.backend.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "login_type", nullable = false, length = 20)
    private String loginType; // EMAIL, GOOGLE

    @Column(name = "google_sub", unique = true)
    private String googleSub;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // ACTIVE, DELETED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 비즈니스 로직: 프로필 수정용 편의 메서드
    public void updateProfile(String username, String profileImageUrl) {
        if (username != null) this.username = username;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
        this.updatedAt = LocalDateTime.now();
    }
}