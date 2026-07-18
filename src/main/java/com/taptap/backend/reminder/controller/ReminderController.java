package com.taptap.backend.reminder.controller;

import com.taptap.backend.config.ApiResponse;
import com.taptap.backend.reminder.dto.ReminderDetailRequestDto;
import com.taptap.backend.reminder.dto.ReminderDetailResponseDto;
import com.taptap.backend.reminder.dto.ReminderListResponseDto;
import com.taptap.backend.reminder.dto.ReminderToggleRequestDto;
import com.taptap.backend.reminder.dto.ReminderToggleResponseDto;
import com.taptap.backend.reminder.service.ReminderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping
    public ApiResponse<List<ReminderListResponseDto>> getReminders(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search
    ) {
        List<ReminderListResponseDto> data = reminderService.getReminderList(userId, categoryId, search);
        return ApiResponse.success("알림 목록 조회가 완료되었습니다.", data);
    }

    @PutMapping("/{button_id}/detail")
    public ApiResponse<ReminderDetailResponseDto> updateDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable("button_id") Long buttonId,
            @Valid @RequestBody ReminderDetailRequestDto request
    ) {
        ReminderDetailResponseDto data = reminderService.updateDetail(userId, buttonId, request);
        return ApiResponse.success("알림 상세 설정이 저장되었습니다.", data);
    }

    @PatchMapping("/{button_id}")
    public ApiResponse<ReminderToggleResponseDto> toggle(
            @AuthenticationPrincipal Long userId,
            @PathVariable("button_id") Long buttonId,
            @Valid @RequestBody ReminderToggleRequestDto request
    ) {
        ReminderToggleResponseDto data = reminderService.toggle(userId, buttonId, request.isEnabled());
        String message = Boolean.TRUE.equals(request.isEnabled())
                ? "알림이 활성화되었습니다."
                : "알림이 비활성화되었습니다.";
        return ApiResponse.success(message, data);
    }
}