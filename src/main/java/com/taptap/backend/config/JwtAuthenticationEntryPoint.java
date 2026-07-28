package com.taptap.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 토큰이 없거나/무효하거나/만료돼서 SecurityFilterChain 단계에서 인증에 실패했을 때 호출됨.
// 이게 없으면 Spring Security 기본 동작으로 본문 없는 403이 내려가서, API 명세서에 적힌
// "401 Unauthorized" + ApiResponse 포맷과 어긋나는 문제가 있었음 (하연님 관련 X, 프론트 전체 공통 이슈)
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Object> body = new ApiResponse<>(false, "유효하지 않은 토큰입니다.", null);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
