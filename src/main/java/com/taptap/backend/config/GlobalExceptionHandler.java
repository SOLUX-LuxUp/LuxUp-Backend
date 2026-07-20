package com.taptap.backend.config;

import com.taptap.backend.template.exception.TemplateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.taptap.backend.button.exception.ButtonException;
import com.taptap.backend.reminder.exception.ReminderException;
import com.taptap.backend.team.exception.TeamException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthException(AuthException e) {
        return ResponseEntity.status(e.getStatus())
                .body(new ApiResponse<>(false, e.getMessage(), null));
    }

    @ExceptionHandler(TemplateException.class)
    public ResponseEntity<ApiResponse<Object>> handleTemplateException(TemplateException e) {
        return ResponseEntity.status(e.getStatus())
                .body(new ApiResponse<>(false, e.getMessage(), null));
    }

    @ExceptionHandler(ButtonException.class)
    public ResponseEntity<ApiResponse<Object>> handleButtonException(ButtonException e) {
        return ResponseEntity.status(e.getStatus())
                .body(new ApiResponse<>(false, e.getMessage(), null));
    }

    @ExceptionHandler(ReminderException.class)
    public ResponseEntity<ApiResponse<Object>> handleReminderException(ReminderException e) {
        return ResponseEntity.status(e.getStatus())
                .body(new ApiResponse<>(false, e.getMessage(), null));
    }

    @ExceptionHandler(TeamException.class)
    public ResponseEntity<ApiResponse<Object>> handleTeamException(TeamException e) {
        return ResponseEntity.status(e.getStatus())
                .body(new ApiResponse<>(false, e.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("입력값이 올바르지 않습니다.");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiResponse<>(false, message, null));
    }
}