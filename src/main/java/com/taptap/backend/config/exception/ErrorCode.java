package com.taptap.backend.config.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 400 Bad Request
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    INVALID_USERNAME_FORMAT(HttpStatus.BAD_REQUEST, "유저 이름 형식이 올바르지 않습니다."),
    INVALID_NOTIFICATION_FIELD(HttpStatus.BAD_REQUEST, "잘못된 필드값입니다."),

    // 401 Unauthorized (대부분 Security 필터에서 처리되지만, 서비스 레벨 방어용으로도 사용)
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    // 404 Not Found
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않거나 탈퇴한 유저입니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}