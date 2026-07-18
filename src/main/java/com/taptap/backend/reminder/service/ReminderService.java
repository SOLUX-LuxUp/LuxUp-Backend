package com.taptap.backend.reminder.service;

import com.taptap.backend.button.entity.Button;
import com.taptap.backend.button.repository.ButtonRepository;
import com.taptap.backend.reminder.dto.ReminderDetailRequestDto;
import com.taptap.backend.reminder.dto.ReminderDetailResponseDto;
import com.taptap.backend.reminder.dto.ReminderListResponseDto;
import com.taptap.backend.reminder.dto.ReminderToggleResponseDto;
import com.taptap.backend.reminder.entity.FrequencyType;
import com.taptap.backend.reminder.entity.Reminder;
import com.taptap.backend.reminder.entity.ReminderMode;
import com.taptap.backend.reminder.exception.ReminderException;
import com.taptap.backend.reminder.repository.ReminderQueryRepository;
import com.taptap.backend.reminder.repository.ReminderRepository;
import com.taptap.backend.reminder.validator.ReminderRequestValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final ReminderQueryRepository reminderQueryRepository;
    private final ButtonRepository buttonRepository;
    private final ReminderRequestValidator validator;

    public ReminderService(ReminderRepository reminderRepository,
                           ReminderQueryRepository reminderQueryRepository,
                           ButtonRepository buttonRepository,
                           ReminderRequestValidator validator) {
        this.reminderRepository = reminderRepository;
        this.reminderQueryRepository = reminderQueryRepository;
        this.buttonRepository = buttonRepository;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public List<ReminderListResponseDto> getReminderList(Long userId, Long categoryId, String search) {
        return reminderQueryRepository.findReminderList(userId, categoryId, search);
    }

    @Transactional
    public ReminderDetailResponseDto updateDetail(Long userId, Long buttonId, ReminderDetailRequestDto request) {
        validator.validate(request);
        validateOwnedActiveButton(userId, buttonId);

        Reminder reminder = reminderRepository.findByButtonIdAndDeletedAtIsNull(buttonId)
                .orElseGet(() -> {
                    Reminder r = new Reminder();
                    r.setButtonId(buttonId);
                    return r;
                });

        reminder.updateDetail(
                request.frequencyType(),
                request.daysOfWeek(),
                request.intervalWeeks(),
                request.dayOfMonth(),
                request.reminderMode(),
                request.remindTimes(),
                request.intervalHours(),
                request.activeStartTime(),
                request.activeEndTime()
        );

        Reminder saved = reminderRepository.save(reminder);
        return ReminderDetailResponseDto.from(saved);
    }

    @Transactional
    public ReminderToggleResponseDto toggle(Long userId, Long buttonId, Boolean isEnabled) {
        validateOwnedActiveButton(userId, buttonId);

        Reminder reminder = reminderRepository.findByButtonIdAndDeletedAtIsNull(buttonId)
                .orElseGet(() -> {
                    Reminder r = new Reminder();
                    r.setButtonId(buttonId);
                    r.setFrequencyType(FrequencyType.DAILY);
                    r.setReminderMode(ReminderMode.INTERVAL);
                    r.setIntervalHours(24);
                    return r;
                });

        if (Boolean.TRUE.equals(isEnabled)) {
            reminder.enable();
        } else {
            reminder.disable();
        }

        Reminder saved = reminderRepository.save(reminder);
        return ReminderToggleResponseDto.from(saved);
    }

    @Transactional
    public void deactivateExpiredOnceReminders() {
        List<Reminder> expired = reminderRepository.findExpiredOnceReminders(LocalDateTime.now().minusHours(24));
        expired.forEach(Reminder::disable);
    }

    private void validateOwnedActiveButton(Long userId, Long buttonId) {
        Button button = buttonRepository.findById(buttonId)
                .orElseThrow(() -> new ReminderException(HttpStatus.NOT_FOUND, "존재하지 않는 버튼입니다."));

        if (!button.getUserId().equals(userId) || !Boolean.TRUE.equals(button.getIsActive())) {
            throw new ReminderException(HttpStatus.NOT_FOUND, "존재하지 않는 버튼입니다.");
        }
    }
}