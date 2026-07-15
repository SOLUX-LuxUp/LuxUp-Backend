package com.taptap.backend.setting.entity;

import com.taptap.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_notification_setting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserNotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    private Long settingId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @Column(name = "master_enabled", nullable = false)
    private Boolean masterEnabled = true;

    @Builder.Default
    @Column(name = "sound_enabled", nullable = false)
    private Boolean soundEnabled = true;

    @Builder.Default
    @Column(name = "vibration_enabled", nullable = false)
    private Boolean vibrationEnabled = true;

    @Builder.Default
    @Column(name = "popup_overlay", nullable = false)
    private Boolean popupOverlay = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 비즈니스 로직: 알림 마스터 설정 일괄/선택 수정용 편의 메서드
    public void updateSettings(Boolean masterEnabled, Boolean soundEnabled, Boolean vibrationEnabled, Boolean popupOverlay) {
        if (masterEnabled != null) this.masterEnabled = masterEnabled;
        if (soundEnabled != null) this.soundEnabled = soundEnabled;
        if (vibrationEnabled != null) this.vibrationEnabled = vibrationEnabled;
        if (popupOverlay != null) this.popupOverlay = popupOverlay;
        this.updatedAt = LocalDateTime.now();
    }
}