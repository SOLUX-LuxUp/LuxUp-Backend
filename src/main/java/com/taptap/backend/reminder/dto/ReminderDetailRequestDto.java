package com.taptap.backend.reminder.dto;

import com.taptap.backend.reminder.entity.FrequencyType;
import com.taptap.backend.reminder.entity.ReminderMode;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.List;

public record ReminderDetailRequestDto(
        @NotNull FrequencyType frequencyType,
        List<Integer> daysOfWeek,
        Integer intervalWeeks,
        List<Integer> dayOfMonth,
        @NotNull ReminderMode reminderMode,
        List<LocalTime> remindTimes,
        Integer intervalHours,
        LocalTime activeStartTime,
        LocalTime activeEndTime
) {
}