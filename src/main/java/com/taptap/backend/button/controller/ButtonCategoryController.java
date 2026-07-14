package com.taptap.backend.button.controller;

import com.taptap.backend.button.dto.*;
import com.taptap.backend.button.service.ButtonCategoryService;
import com.taptap.backend.config.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/buttons/categories")
public class ButtonCategoryController {

    private final ButtonCategoryService buttonCategoryService;

    public ButtonCategoryController(ButtonCategoryService buttonCategoryService) {
        this.buttonCategoryService = buttonCategoryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponseDto>> createCategory(
            Authentication authentication,
            @RequestBody CreateCategoryRequestDto request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        CategoryResponseDto response = buttonCategoryService.createCategory(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("카테고리가 생성되었습니다.", response));
    }

    @PatchMapping("/{category_id}")
    public ApiResponse<CategoryUpdateResponseDto> updateCategoryName(
            Authentication authentication,
            @PathVariable("category_id") Long categoryId,
            @RequestBody UpdateCategoryNameRequestDto request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("카테고리 이름이 수정되었습니다.", buttonCategoryService.updateCategoryName(userId, categoryId, request));
    }

    @DeleteMapping("/{category_id}")
    public ApiResponse<Object> deleteCategory(
            Authentication authentication,
            @PathVariable("category_id") Long categoryId,
            @RequestParam("delete_buttons") Boolean deleteButtons
    ) {
        Long userId = (Long) authentication.getPrincipal();
        buttonCategoryService.deleteCategory(userId, categoryId, deleteButtons);
        return ApiResponse.success("카테고리가 삭제되었습니다.", null);
    }
}