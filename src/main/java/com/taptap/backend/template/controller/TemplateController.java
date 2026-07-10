package com.taptap.backend.template.controller;

import com.taptap.backend.config.ApiResponse;
import com.taptap.backend.template.dto.TemplateApplyRequestDto;
import com.taptap.backend.template.dto.TemplateApplyResponseDto;
import com.taptap.backend.template.dto.TemplatePreviewResponseDto;
import com.taptap.backend.template.dto.TemplateResponseDto;
import com.taptap.backend.template.service.TemplateService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public ApiResponse<List<TemplateResponseDto>> getTemplates() {
        List<TemplateResponseDto> templates = templateService.getActiveTemplates();
        return ApiResponse.success("템플릿 목록 조회가 완료되었습니다.", templates);
    }

    @GetMapping("/{template_id}/recommendations")
    public ApiResponse<TemplatePreviewResponseDto> getTemplatePreview(@PathVariable Long template_id) {
        TemplatePreviewResponseDto preview = templateService.getTemplatePreview(template_id);
        return ApiResponse.success("템플릿 미리보기 조회가 완료되었습니다.", preview);
    }

    @PostMapping("/{template_id}/apply")
    public ResponseEntity<ApiResponse<TemplateApplyResponseDto>> applyTemplate(
            Authentication authentication,
            @PathVariable Long template_id,
            @RequestBody TemplateApplyRequestDto request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        TemplateApplyResponseDto response = templateService.applyTemplate(userId, template_id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("템플릿이 적용되었습니다.", response));
    }

    @PostMapping("/skip")
    public ApiResponse<Object> skipTemplate(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        templateService.skipTemplate(userId);
        return ApiResponse.success("빈 화면으로 시작합니다.", null);
    }
}