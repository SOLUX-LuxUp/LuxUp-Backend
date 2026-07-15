package com.taptap.backend.record.controller;

import com.taptap.backend.config.ApiResponse;
import com.taptap.backend.record.dto.RecordCreateResponseDto;
import com.taptap.backend.record.dto.RecordDetailResponseDto;
import com.taptap.backend.record.dto.RecordDetailUpdateRequestDto;
import com.taptap.backend.record.dto.RecordLatestResponseDto;
import com.taptap.backend.record.dto.RecordSummaryResponseDto;
import com.taptap.backend.record.dto.RecordTimelineResponseDto;
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

    @Operation(summary = "5.3 타임라인 조회")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/timeline")
    public ApiResponse<RecordTimelineResponseDto> getTimeline(
            Authentication authentication,
            @PathVariable("button_id") Long buttonId,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "limit", required = false, defaultValue = "30") Integer limit
    ) {
        Long userId = (Long) authentication.getPrincipal();
        RecordTimelineResponseDto response = recordService.getTimeline(userId, buttonId, cursor, limit);
        return ApiResponse.success("타임라인 조회가 완료되었습니다.", response);
    }

    @Operation(summary = "5.4 타임라인 상세 기록 추가 (메모·이모지)")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{record_id}/detail")
    public ApiResponse<RecordDetailResponseDto> updateDetail(
            Authentication authentication,
            @PathVariable("button_id") Long buttonId,
            @PathVariable("record_id") Long recordId,
            @RequestBody RecordDetailUpdateRequestDto request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        RecordDetailResponseDto response = recordService.updateDetail(userId, buttonId, recordId, request);
        return ApiResponse.success("기록이 수정되었습니다.", response);
    }

    @Operation(summary = "5.5 타임라인 기록 삭제")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{record_id}")
    public ApiResponse<Void> deleteRecord(
            Authentication authentication,
            @PathVariable("button_id") Long buttonId,
            @PathVariable("record_id") Long recordId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        recordService.deleteRecord(userId, buttonId, recordId);
        return ApiResponse.<Void>success("기록이 삭제되었습니다.", null);
    }
}