package com.taptap.backend.reminder.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "reminder")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reminder_id")
    private Long reminderId;

    @Column(name = "button_id", nullable = false)
    private Long buttonId;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_type", nullable = false, length = 10)
    private FrequencyType frequencyType = FrequencyType.DAILY;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "days_of_week")
    private List<Integer> daysOfWeek;

    @Column(name = "interval_weeks")
    private Integer intervalWeeks;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "day_of_month")
    private List<Integer> dayOfMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_mode", nullable = false, length = 10)
    private ReminderMode reminderMode = ReminderMode.INTERVAL;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "remind_times")
    private List<LocalTime> remindTimes;

    @Column(name = "interval_hours")
    private Integer intervalHours = 24;

    @Column(name = "active_start_time")
    private LocalTime activeStartTime;

    @Column(name = "active_end_time")
    private LocalTime activeEndTime;

    @Column(name = "once_activated_at")
    private LocalDateTime onceActivatedAt;

    @Column(name = "last_reminded_at")
    private LocalDateTime lastRemindedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateDetail(FrequencyType frequencyType,
                             List<Integer> daysOfWeek,
                             Integer intervalWeeks,
                             List<Integer> dayOfMonth,
                             ReminderMode reminderMode,
                             List<LocalTime> remindTimes,
                             Integer intervalHours,
                             LocalTime activeStartTime,
                             LocalTime activeEndTime) {
        this.frequencyType = frequencyType;
        this.daysOfWeek = daysOfWeek;
        this.intervalWeeks = intervalWeeks;
        this.dayOfMonth = dayOfMonth;
        this.reminderMode = reminderMode;
        this.remindTimes = remindTimes;
        this.intervalHours = intervalHours;
        this.activeStartTime = activeStartTime;
        this.activeEndTime = activeEndTime;
        this.onceActivatedAt = (frequencyType == FrequencyType.ONCE) ? LocalDateTime.now() : null;
    }

    public void enable() {
        this.isEnabled = true;
        if (this.frequencyType == FrequencyType.ONCE) {
            this.onceActivatedAt = LocalDateTime.now();
        }
    }

    public void disable() {
        this.isEnabled = false;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.isEnabled = false;
    }

    public Long getReminderId() {
        return reminderId;
    }

    public Long getButtonId() {
        return buttonId;
    }

    public void setButtonId(Long buttonId) {
        this.buttonId = buttonId;
    }

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public FrequencyType getFrequencyType() {
        return frequencyType;
    }

    public void setFrequencyType(FrequencyType frequencyType) {
        this.frequencyType = frequencyType;
    }

    public List<Integer> getDaysOfWeek() {
        return daysOfWeek;
    }

    public Integer getIntervalWeeks() {
        return intervalWeeks;
    }

    public List<Integer> getDayOfMonth() {
        return dayOfMonth;
    }

    public ReminderMode getReminderMode() {
        return reminderMode;
    }

    public void setReminderMode(ReminderMode reminderMode) {
        this.reminderMode = reminderMode;
    }

    public List<LocalTime> getRemindTimes() {
        return remindTimes;
    }

    public Integer getIntervalHours() {
        return intervalHours;
    }

    public void setIntervalHours(Integer intervalHours) {
        this.intervalHours = intervalHours;
    }

    public LocalTime getActiveStartTime() {
        return activeStartTime;
    }

    public LocalTime getActiveEndTime() {
        return activeEndTime;
    }

    public LocalDateTime getOnceActivatedAt() {
        return onceActivatedAt;
    }

    public LocalDateTime getLastRemindedAt() {
        return lastRemindedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}