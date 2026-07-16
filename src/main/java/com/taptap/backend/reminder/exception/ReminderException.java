package com.taptap.backend.reminder.exception;

import org.springframework.http.HttpStatus;

public class ReminderException extends RuntimeException {
    private final HttpStatus status;

    public ReminderException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}