package com.taptap.backend.config;

import com.taptap.backend.template.exception.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.taptap.backend.button.exception.ButtonException;
import com.taptap.backend.reminder.exception.ReminderException;
import com.taptap.backend.team.exception.TeamException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    // 요청 body가 JSON 파싱이 안 되는 경우 (형식 자체가 깨진 JSON, 타입 불일치 등)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleMessageNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, "요청 본문 형식이 올바르지 않습니다.", null));
    }

    // path/query 파라미터 타입이 안 맞는 경우 (예: /api/teams/{team_id}에 숫자가 아닌 값)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, "요청 파라미터 형식이 올바르지 않습니다.", null));
    }

    // 위에서 처리되지 않은 예상치 못한 예외에 대한 최종 안전망.
    // 이게 없으면 Spring Boot 기본 에러 응답({timestamp, status, error, path})이 그대로 나가서
    // 다른 모든 응답이 쓰는 ApiResponse({success, message, data}) 포맷과 어긋남
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpectedException(Exception e) {
        log.error("예상치 못한 서버 오류", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "서버 내부 오류가 발생했습니다.", null));
    }
}