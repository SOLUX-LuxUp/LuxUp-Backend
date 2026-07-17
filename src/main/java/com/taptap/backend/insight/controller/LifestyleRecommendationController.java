package com.taptap.backend.insight.controller;

import com.taptap.backend.config.ApiResponse;
import com.taptap.backend.insight.dto.LifestyleRecommendationActionRequestDto;
import com.taptap.backend.insight.dto.LifestyleRecommendationActionResponseDto;
import com.taptap.backend.insight.dto.LifestyleRecommendationDto;
import com.taptap.backend.insight.service.LifestyleRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "라이프스타일 추천", description = "AI 기반 버튼 추천 / 미사용 버튼 삭제 제안 API")
@RestController
@RequestMapping("/api/lifestyle-recommendations")
@RequiredArgsConstructor
public class LifestyleRecommendationController {

    private final LifestyleRecommendationService lifestyleRecommendationService;

    @Operation(summary = "7.3.5 라이프스타일 추천 목록 조회 (없으면 새로 생성)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ApiResponse<List<LifestyleRecommendationDto>> getRecommendations(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<LifestyleRecommendationDto> response = lifestyleRecommendationService.getRecommendations(userId);
        return ApiResponse.success("라이프스타일 추천 조회가 완료되었습니다.", response);
    }

    @Operation(summary = "7.3.5 라이프스타일 추천 수락/거절")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{rec_id}")
    public ApiResponse<LifestyleRecommendationActionResponseDto> processAction(
            Authentication authentication,
            @PathVariable("rec_id") Long recId,
            @RequestBody LifestyleRecommendationActionRequestDto request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        LifestyleRecommendationActionResponseDto response =
                lifestyleRecommendationService.processAction(userId, recId, request.getAction());
        return ApiResponse.success("추천이 처리되었습니다.", response);
    }
}