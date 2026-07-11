package com.taptap.backend.button.exception;

import org.springframework.http.HttpStatus;

public class ButtonException extends RuntimeException {
    private final HttpStatus status;

    public ButtonException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}