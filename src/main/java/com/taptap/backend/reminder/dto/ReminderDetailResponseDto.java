package com.taptap.backend.reminder.dto;

import com.taptap.backend.reminder.entity.FrequencyType;
import com.taptap.backend.reminder.entity.Reminder;
import com.taptap.backend.reminder.entity.ReminderMode;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record ReminderDetailResponseDto(
        Long reminderId,
        Long buttonId,
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
    public static ReminderDetailResponseDto from(Reminder reminder) {
        return new ReminderDetailResponseDto(
                reminder.getReminderId(),
                reminder.getButtonId(),
                reminder.getFrequencyType(),
                reminder.getDaysOfWeek(),
                reminder.getIntervalWeeks(),
                reminder.getDayOfMonth(),
                reminder.getOnceActivatedAt(),
                reminder.getReminderMode(),
                reminder.getRemindTimes(),
                reminder.getIntervalHours(),
                reminder.getActiveStartTime(),
                reminder.getActiveEndTime(),
                LocalDateTime.now()
        );
    }
}