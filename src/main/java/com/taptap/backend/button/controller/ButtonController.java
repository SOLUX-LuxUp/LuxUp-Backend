package com.taptap.backend.button.controller;

import com.taptap.backend.button.dto.*;
import com.taptap.backend.button.service.ButtonCategoryService;
import com.taptap.backend.button.service.ButtonService;
import com.taptap.backend.config.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/buttons")
public class ButtonController {

    private final ButtonService buttonService;
    private final ButtonCategoryService buttonCategoryService;

    public ButtonController(ButtonService buttonService, ButtonCategoryService buttonCategoryService) {
        this.buttonService = buttonService;
        this.buttonCategoryService = buttonCategoryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ButtonResponseDto>> createButton(
            Authentication authentication,
            @RequestBody CreateButtonRequestDto request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        ButtonResponseDto response = buttonService.createButton(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("버튼이 생성되었습니다.", response));
    }

    @PatchMapping("/{button_id}")
    public ApiResponse<UpdateButtonResponseDto> updateButton(
            Authentication authentication,
            @PathVariable("button_id") Long buttonId,
            @RequestBody UpdateButtonRequestDto request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("버튼 정보가 수정되었습니다.", buttonService.updateButton(userId, buttonId, request));
    }

    @DeleteMapping("/{button_id}")
    public ApiResponse<Object> deleteButton(Authentication authentication, @PathVariable("button_id") Long buttonId) {
        Long userId = (Long) authentication.getPrincipal();
        buttonService.deleteButton(userId, buttonId);
        return ApiResponse.success("버튼과 모든 기록이 삭제되었습니다.", null);
    }

    @PatchMapping("/{button_id}/favorite")
    public ApiResponse<FavoriteResponseDto> setFavorite(
            Authentication authentication,
            @PathVariable("button_id") Long buttonId,
            @RequestBody FavoriteRequestDto request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("즐겨찾기가 설정되었습니다.", buttonService.setFavorite(userId, buttonId, request.isFavorite()));
    }

    @PatchMapping("/favorite-order")
    public ApiResponse<List<FavoriteOrderItemDto>> updateFavoriteOrder(
            Authentication authentication,
            @RequestBody FavoriteOrderRequestDto request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("즐겨찾기 순서가 변경되었습니다.", buttonService.updateFavoriteOrder(userId, request.buttonIds()));
    }

    @PatchMapping("/category-order")
    public ApiResponse<List<CategoryOrderItemDto>> updateCategoryOrder(
            Authentication authentication,
            @RequestBody CategoryOrderRequestDto request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("카테고리 순서가 변경되었습니다.", buttonCategoryService.updateCategoryOrder(userId, request.categoryIds()));
    }

    @GetMapping("/search")
    public ApiResponse<List<FavoriteButtonItemDto>> searchButtons(
            Authentication authentication,
            @RequestParam("keyword") String keyword
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("버튼 검색이 완료되었습니다.", buttonService.searchButtons(userId, keyword));
    }
    @GetMapping("/favorites")
    public ApiResponse<List<FavoriteButtonItemDto>> getFavoriteButtons(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("즐겨찾기 버튼 목록 조회가 완료되었습니다.", buttonService.getFavoriteButtons(userId));
    }

    @GetMapping
    public ApiResponse<ButtonListResponseDto> getButtons(
            Authentication authentication,
            @RequestParam(value = "category_id", required = false) Long categoryId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("버튼 목록 조회가 완료되었습니다.", buttonService.getButtons(userId, categoryId));
    }
}