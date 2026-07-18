package com.taptap.backend.reminder.dto;

import jakarta.validation.constraints.NotNull;

public record ReminderToggleRequestDto(@NotNull Boolean isEnabled) {
}