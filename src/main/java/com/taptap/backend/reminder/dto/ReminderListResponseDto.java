package com.taptap.backend.reminder.dto;

import com.taptap.backend.reminder.entity.FrequencyType;
import com.taptap.backend.reminder.entity.ReminderMode;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record ReminderListResponseDto(
        Long reminderId,
        Long buttonId,
        String buttonName,
        String iconName,
        String iconColor,
        Long categoryId,
        String categoryName,
        Boolean isEnabled,
        FrequencyType frequencyType,
        List<Integer> daysOfWeek,
        Integer intervalWeeks,
        List<Integer> dayOfMonth,
        LocalDateTime onceActivatedAt,
        ReminderMode reminderMode,
        List<LocalTime> remindTimes,
        Integer intervalHours,
        LocalTime activeStartTime,
        LocalTime activeEndTime,
        LocalDateTime updatedAt
) {
}