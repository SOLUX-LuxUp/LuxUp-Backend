package com.taptap.backend.team.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_button_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamButtonRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "team_button_id", nullable = false)
    private Long teamButtonId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "memo", length = 500)
    private String memo;

    @Column(name = "emoji", length = 10)
    private String emoji;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    void onCreate() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }
}
