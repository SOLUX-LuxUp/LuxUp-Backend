package com.taptap.backend.team.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 버튼별 x 유저별 알림/탭권한 상태. tapPermission="custom"일 때 allowedUserIds가 여기 저장됨.
// 탭 권한 요청/승인 워크플로우(팀 공유 버튼 탭 권한 API, 다음 브랜치)도 이 테이블을 같이 사용.
@Entity
@Table(name = "team_button_user_setting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamButtonUserSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    private Long settingId;

    @Column(name = "team_button_id", nullable = false)
    private Long teamButtonId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder.Default
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    @Builder.Default
    @Column(name = "has_tap_permission", nullable = false)
    private Boolean hasTapPermission = true;

    @Builder.Default
    @Column(name = "permission_status", nullable = false, length = 20)
    private String permissionStatus = "granted"; // granted / pending / denied

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void onSave() {
        updatedAt = LocalDateTime.now();
    }
}
