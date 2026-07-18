package com.taptap.backend.reminder.repository;

import com.taptap.backend.reminder.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    Optional<Reminder> findByButtonIdAndDeletedAtIsNull(Long buttonId);

    @Query("SELECT r FROM Reminder r WHERE r.frequencyType = com.taptap.backend.reminder.entity.FrequencyType.ONCE " +
            "AND r.isEnabled = true AND r.onceActivatedAt IS NOT NULL " +
            "AND r.onceActivatedAt <= :threshold AND r.deletedAt IS NULL")
    List<Reminder> findExpiredOnceReminders(@Param("threshold") LocalDateTime threshold);

    @Modifying
    @Query("UPDATE Reminder r SET r.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE r.buttonId IN :buttonIds AND r.deletedAt IS NULL")
    void softDeleteByButtonIds(@Param("buttonIds") List<Long> buttonIds);
}