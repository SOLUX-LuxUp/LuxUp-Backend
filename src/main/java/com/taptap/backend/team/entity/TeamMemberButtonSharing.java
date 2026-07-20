package com.taptap.backend.team.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 개인 버튼(button)을 특정 팀에 공유할지 여부. TeamMember.isButtonPublic(전체 공개 토글)과 별개로
// 버튼 단위 세부 공유 설정을 담당 (팀원 기록 공유 API, 8.2.1/8.2.3/8.2.4)
@Entity
@Table(name = "team_member_button_sharing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMemberButtonSharing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sharing_id")
    private Long sharingId;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 개인 button 테이블의 button_id (팀 소유 버튼 아님)
    @Column(name = "button_id", nullable = false)
    private Long buttonId;

    @Builder.Default
    @Column(name = "is_shared", nullable = false)
    private Boolean isShared = false;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void onSave() {
        updatedAt = LocalDateTime.now();
    }
}
