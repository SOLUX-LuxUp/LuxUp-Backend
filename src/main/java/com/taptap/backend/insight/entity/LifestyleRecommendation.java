package com.taptap.backend.insight.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ⚠️ ERD 원안의 source_button_id(JSON)는 "추천의 근거가 된 버튼들" 용도라 지금은 안 쓰고,
 *    DELETE 추천일 때 "지울 대상 버튼"을 가리킬 target_button_id 컬럼을 새로 추가했다.
 *    ddl-auto=update라 자동으로 테이블에 반영된다.
 */
@Entity
@Table(name = "lifestyle_recommendation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LifestyleRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rec_id")
    private Long recId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "rec_type", nullable = false, length = 20)
    private String recType; // ADD / DELETE

    @Column(name = "suggested_button_name", length = 50)
    private String suggestedButtonName;

    @Column(name = "suggested_icon_name", length = 100)
    private String suggestedIconName;

    @Column(name = "suggested_icon_color", length = 20) // HEX 아니라 색상 이름(darkgrey 등)이 들어가서 넉넉하게 확장
    private String suggestedIconColor;

    @Column(name = "target_button_id")
    private Long targetButtonId; // DELETE 추천일 때만 사용

    @Builder.Default
    @Column(name = "is_accepted", nullable = false)
    private Boolean isAccepted = false;

    @Builder.Default
    @Column(name = "is_dismissed", nullable = false)
    private Boolean isDismissed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void accept() {
        this.isAccepted = true;
    }

    public void dismiss() {
        this.isDismissed = true;
    }
}