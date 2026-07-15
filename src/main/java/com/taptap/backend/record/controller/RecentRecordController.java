package com.taptap.backend.record.controller;

import com.taptap.backend.config.ApiResponse;
import com.taptap.backend.record.dto.RecordRecentResponseDto;
import com.taptap.backend.record.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * /api/buttons/{button_id}/records/... 하위가 아니라
 * 최상위 /api/records 경로를 쓰는 API라 별도 컨트롤러로 분리했다.
 */
@Tag(name = "기록", description = "최근 기록(홈) 관련 API")
@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecentRecordController {

    private final RecordService recordService;

    @Operation(summary = "5.1 최근 기록 조회 (홈)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/recent")
    public ApiResponse<RecordRecentResponseDto> getRecentRecord(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        RecordRecentResponseDto response = recordService.getRecentRecord(userId);
        return ApiResponse.success("최근 기록 조회가 완료되었습니다.", response);
    }
}