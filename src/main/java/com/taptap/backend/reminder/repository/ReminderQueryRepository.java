package com.taptap.backend.reminder.repository;

import com.taptap.backend.reminder.dto.ReminderListResponseDto;
import com.taptap.backend.reminder.entity.Reminder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReminderQueryRepository extends Repository<Reminder, Long> {

    @Query("""
            SELECT new com.taptap.backend.reminder.dto.ReminderListResponseDto(
                r.reminderId, r.buttonId, b.buttonName, b.iconName, b.iconColor,
                bc.categoryId, bc.categoryName, r.isEnabled,
                r.frequencyType, r.daysOfWeek, r.intervalWeeks, r.dayOfMonth, r.onceActivatedAt,
                r.reminderMode, r.remindTimes, r.intervalHours, r.activeStartTime, r.activeEndTime,
                r.updatedAt)
            FROM Reminder r
            JOIN Button b ON b.buttonId = r.buttonId
            LEFT JOIN ButtonCategory bc ON bc.categoryId = b.categoryId
            WHERE b.userId = :userId
              AND b.isActive = true
              AND r.deletedAt IS NULL
              AND (:categoryId IS NULL OR bc.categoryId = :categoryId)
              AND (:search IS NULL OR b.buttonName LIKE CONCAT('%', :search, '%'))
            ORDER BY r.isEnabled DESC, r.updatedAt DESC, r.createdAt DESC
            """)
    List<ReminderListResponseDto> findReminderList(@Param("userId") Long userId,
                                                   @Param("categoryId") Long categoryId,
                                                   @Param("search") String search);
}