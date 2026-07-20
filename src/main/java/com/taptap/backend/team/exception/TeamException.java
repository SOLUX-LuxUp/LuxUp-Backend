package com.taptap.backend.team.exception;

import org.springframework.http.HttpStatus;

public class TeamException extends RuntimeException {
    private final HttpStatus status;

    public TeamException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
