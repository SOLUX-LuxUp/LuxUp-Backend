package com.taptap.backend.config.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 모든 API 응답의 공통 포맷.
 * @JsonInclude(NON_NULL) 덕분에 message가 null이면 응답 JSON에서 아예 빠집니다.
 * (API 명세서의 GET 응답 예시가 message 없이 success/data만 있는 것과 맞추기 위함.
 *  기존 컨트롤러들처럼 GET에서도 message를 계속 채워 보내는 건 문제 없고, 그냥 선택 사항입니다.)
 */
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, null, data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    // 🆕 예외 처리용. GlobalExceptionHandler에서 사용합니다.
    // ⚠️ ApiResponse<Void>로 선언했더니 springdoc이 Void 타입 스키마를 생성하다가
    //    /v3/api-docs에서 500을 내는 경우가 있어 Object로 완화했습니다.
    public static ApiResponse<Object> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }
}