package com.taptap.backend.reminder.repository;

import java.time.LocalDateTime;
import java.time.LocalTime;

public interface ReminderListProjection {

    Long getReminderId();
    Long getButtonId();
    String getButtonName();
    String getIconName();
    String getIconColor();
    Long getCategoryId();
    String getCategoryName();
    Boolean getIsEnabled();
    String getFrequencyType();
    String getDaysOfWeek();
    Integer getIntervalWeeks();
    String getDayOfMonth();
    LocalDateTime getOnceActivatedAt();
    String getReminderMode();
    String getRemindTimes();
    Integer getIntervalHours();
    LocalTime getActiveStartTime();
    LocalTime getActiveEndTime();
    LocalDateTime getUpdatedAt();
}