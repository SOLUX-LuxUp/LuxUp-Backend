package com.taptap.backend.reminder.repository;

import com.taptap.backend.reminder.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    @Modifying
    @Query("UPDATE Reminder r SET r.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE r.buttonId IN :buttonIds AND r.deletedAt IS NULL")
    void softDeleteByButtonIds(@Param("buttonIds") List<Long> buttonIds);
}