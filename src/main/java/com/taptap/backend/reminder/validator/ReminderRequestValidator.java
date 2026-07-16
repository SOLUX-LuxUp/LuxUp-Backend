package com.taptap.backend.reminder.validator;

import com.taptap.backend.reminder.dto.ReminderDetailRequestDto;
import com.taptap.backend.reminder.entity.FrequencyType;
import com.taptap.backend.reminder.entity.ReminderMode;
import com.taptap.backend.reminder.exception.ReminderException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ReminderRequestValidator {

    public void validate(ReminderDetailRequestDto dto) {
        validateReminderMode(dto);
        validateFrequencyType(dto);
    }

    private void validateReminderMode(ReminderDetailRequestDto dto) {
        boolean hasRemindTimes = dto.remindTimes() != null && !dto.remindTimes().isEmpty();
        boolean hasIntervalHours = dto.intervalHours() != null;
        boolean hasActiveWindow = dto.activeStartTime() != null || dto.activeEndTime() != null;

        if (dto.reminderMode() == ReminderMode.TIME) {
            if (!hasRemindTimes || hasIntervalHours || hasActiveWindow) {
                throw new ReminderException(HttpStatus.BAD_REQUEST,
                        "remindTimes와 intervalHours는 reminderMode에 맞게 하나만 입력해야 합니다.");
            }
        } else if (dto.reminderMode() == ReminderMode.INTERVAL) {
            if (!hasIntervalHours || hasRemindTimes) {
                throw new ReminderException(HttpStatus.BAD_REQUEST,
                        "remindTimes와 intervalHours는 reminderMode에 맞게 하나만 입력해야 합니다.");
            }
        }
    }

    private void validateFrequencyType(ReminderDetailRequestDto dto) {
        FrequencyType type = dto.frequencyType();

        boolean hasDaysOfWeek = dto.daysOfWeek() != null && !dto.daysOfWeek().isEmpty();
        boolean hasIntervalWeeks = dto.intervalWeeks() != null;
        boolean hasDayOfMonth = dto.dayOfMonth() != null && !dto.dayOfMonth().isEmpty();
        boolean hasActiveWindow = dto.activeStartTime() != null || dto.activeEndTime() != null;

        String message = "frequencyType과 daysOfWeek/intervalWeeks/dayOfMonth 조합이 올바르지 않습니다.";

        switch (type) {
            case DAILY -> {
                if (hasDaysOfWeek || hasIntervalWeeks || hasDayOfMonth) {
                    throw new ReminderException(HttpStatus.BAD_REQUEST, message);
                }
            }
            case WEEKLY -> {
                if (!hasDaysOfWeek || hasIntervalWeeks || hasDayOfMonth) {
                    throw new ReminderException(HttpStatus.BAD_REQUEST, message);
                }
            }
            case MONTHLY -> {
                if (!hasDayOfMonth || hasDaysOfWeek || hasIntervalWeeks) {
                    throw new ReminderException(HttpStatus.BAD_REQUEST, message);
                }
            }
            case CUSTOM -> {
                if (!hasDaysOfWeek || !hasIntervalWeeks || hasDayOfMonth) {
                    throw new ReminderException(HttpStatus.BAD_REQUEST, message);
                }
            }
            case ONCE -> {
                if (hasDaysOfWeek || hasIntervalWeeks || hasDayOfMonth || hasActiveWindow) {
                    throw new ReminderException(HttpStatus.BAD_REQUEST, message);
                }
            }
        }
    }
}