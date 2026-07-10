package com.taptap.backend.template.exception;

import org.springframework.http.HttpStatus;

public class TemplateException extends RuntimeException {
    private final HttpStatus status;

    public TemplateException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}