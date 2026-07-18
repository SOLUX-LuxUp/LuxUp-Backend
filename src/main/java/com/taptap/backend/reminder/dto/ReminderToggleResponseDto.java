package com.taptap.backend.reminder.dto;

import com.taptap.backend.reminder.entity.Reminder;

import java.time.LocalDateTime;

public record ReminderToggleResponseDto(
        Long reminderId,
        Long buttonId,
        Boolean isEnabled,
        LocalDateTime updatedAt
) {
    public static ReminderToggleResponseDto from(Reminder reminder) {
        return new ReminderToggleResponseDto(
                reminder.getReminderId(),
                reminder.getButtonId(),
                reminder.getIsEnabled(),
                LocalDateTime.now()
        );
    }
}