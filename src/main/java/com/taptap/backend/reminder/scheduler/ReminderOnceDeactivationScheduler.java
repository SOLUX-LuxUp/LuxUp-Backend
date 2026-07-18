package com.taptap.backend.reminder.scheduler;

import com.taptap.backend.reminder.service.ReminderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReminderOnceDeactivationScheduler {

    private final ReminderService reminderService;

    public ReminderOnceDeactivationScheduler(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Scheduled(cron = "0 */10 * * * *")
    public void deactivateExpiredOnceReminders() {
        reminderService.deactivateExpiredOnceReminders();
    }
}