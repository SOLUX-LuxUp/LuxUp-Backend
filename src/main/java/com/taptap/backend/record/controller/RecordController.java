package com.taptap.backend.record.controller;

import com.taptap.backend.config.ApiResponse;
import com.taptap.backend.record.dto.RecordCreateResponseDto;
import com.taptap.backend.record.dto.RecordLatestResponseDto;
import com.taptap.backend.record.dto.RecordSummaryResponseDto;
import com.taptap.backend.record.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "기록", description = "행동 기록(탭) 관련 API")
@RestController
@RequestMapping("/api/buttons/{button_id}/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @Operation(summary = "4.2 행동 기록 (탭)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ApiResponse<RecordCreateResponseDto> createRecord(
            Authentication authentication,
            @PathVariable("button_id") Long buttonId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        RecordCreateResponseDto response = recordService.createRecord(userId, buttonId);
        return ApiResponse.success("기록이 완료되었습니다.", response);
    }

    @Operation(summary = "기록 취소 (팝업 3초 이내)")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{record_id}/cancel")
    public ApiResponse<Void> cancelRecord(
            Authentication authentication,
            @PathVariable("button_id") Long buttonId,
            @PathVariable("record_id") Long recordId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        recordService.cancelRecord(userId, buttonId, recordId);
        return ApiResponse.<Void>success("기록이 취소되었습니다.", null);
    }

    @Operation(summary = "4.7 마지막 기록 시간 조회")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/latest")
    public ApiResponse<RecordLatestResponseDto> getLatestRecord(
            Authentication authentication,
            @PathVariable("button_id") Long buttonId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        RecordLatestResponseDto response = recordService.getLatestRecord(userId, buttonId);
        return ApiResponse.success("마지막 기록 시간 조회가 완료되었습니다.", response);
    }

    @Operation(summary = "5.2 최근 기록 조회 (버튼 상세)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/summary")
    public ApiResponse<RecordSummaryResponseDto> getButtonSummary(
            Authentication authentication,
            @PathVariable("button_id") Long buttonId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        RecordSummaryResponseDto response = recordService.getButtonSummary(userId, buttonId);
        return ApiResponse.success("버튼 요약 조회가 완료되었습니다.", response);
    }
}