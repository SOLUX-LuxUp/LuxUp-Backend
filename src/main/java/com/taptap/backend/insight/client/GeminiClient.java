package com.taptap.backend.insight.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Gemini API 호출만 담당하는 클라이언트.
 * ⚠️ 나중에 다른 AI(Claude 등)로 바꾸거나, 라이프스타일 추천 로직을 조정할 때
 *    이 파일 하나만 건드리면 되도록 격리시켰다.
 */
@Component
public class GeminiClient {

    private static final String GENERATE_ENDPOINT_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";
    private static final String LIST_MODELS_ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models";
    private static final String MODEL = "gemini-3.5-flash";

    @Value("${gemini.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 프롬프트를 보내고, 모델이 생성한 텍스트만 뽑아서 반환한다.
     * 실패 시 null.
     */
    public String generateText(String prompt) {
        try {
            String url = String.format(GENERATE_ENDPOINT_TEMPLATE, MODEL);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode textNode = root.path("candidates").path(0)
                    .path("content").path("parts").path(0).path("text");

            return textNode.isMissingNode() ? null : textNode.asText();
        } catch (Exception e) {
            // 지금은 콘솔에 원인만 남기고 null 반환. 나중에 로깅 방식 팀 컨벤션 생기면 교체.
            System.err.println("[GeminiClient] AI 호출 실패: " + e.getMessage());
            return null;
        }
    }
}