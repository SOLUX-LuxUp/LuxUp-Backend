package com.taptap.backend.button.service;

import com.taptap.backend.button.dto.CategoryOrderItemDto;
import com.taptap.backend.button.dto.CategoryResponseDto;
import com.taptap.backend.button.dto.CategoryUpdateResponseDto;
import com.taptap.backend.button.dto.CreateCategoryRequestDto;
import com.taptap.backend.button.dto.UpdateCategoryNameRequestDto;
import com.taptap.backend.button.entity.ButtonCategory;
import com.taptap.backend.button.exception.ButtonException;
import com.taptap.backend.button.repository.ButtonCategoryRepository;
import com.taptap.backend.button.repository.ButtonRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ButtonCategoryService {

    private final ButtonCategoryRepository buttonCategoryRepository;
    private final ButtonRepository buttonRepository;

    public ButtonCategoryService(ButtonCategoryRepository buttonCategoryRepository, ButtonRepository buttonRepository) {
        this.buttonCategoryRepository = buttonCategoryRepository;
        this.buttonRepository = buttonRepository;
    }

    @Transactional
    public CategoryResponseDto createCategory(Long userId, CreateCategoryRequestDto request) {
        if (request.categoryName() == null || request.categoryName().isBlank()) {
            throw new ButtonException(HttpStatus.BAD_REQUEST, "카테고리 이름은 필수입니다.");
        }
        boolean duplicate = buttonCategoryRepository
                .existsByUserIdAndCategoryNameAndDeletedAtIsNull(userId, request.categoryName());
        if (duplicate) {
            throw new ButtonException(HttpStatus.CONFLICT, "동일 이름의 카테고리가 이미 존재합니다.");
        }

        Integer maxOrder = buttonCategoryRepository.findMaxDisplayOrderByUserId(userId);
        int nextOrder = (maxOrder == null ? 0 : maxOrder) + 1;

        ButtonCategory category = new ButtonCategory();
        category.setUserId(userId);
        category.setCategoryName(request.categoryName());
        category.setDisplayOrder(nextOrder);

        ButtonCategory saved = buttonCategoryRepository.save(category);
        return new CategoryResponseDto(saved.getCategoryId(), saved.getCategoryName(), saved.getDisplayOrder(), saved.getCreatedAt());
    }

    @Transactional
    public void deleteCategory(Long userId, Long categoryId, boolean deleteButtons) {
        ButtonCategory category = buttonCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ButtonException(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."));
        if (!category.getUserId().equals(userId)) {
            throw new ButtonException(HttpStatus.FORBIDDEN, "본인 카테고리가 아닙니다.");
        }

        category.setDeletedAt(LocalDateTime.now());
        buttonCategoryRepository.save(category);

        if (deleteButtons) {
            buttonRepository.deactivateByCategoryId(categoryId);
        } else {
            buttonRepository.clearCategoryId(categoryId);
        }
    }

    @Transactional
    public CategoryUpdateResponseDto updateCategoryName(Long userId, Long categoryId, UpdateCategoryNameRequestDto request) {
        if (request.categoryName() == null || request.categoryName().isBlank()) {
            throw new ButtonException(HttpStatus.BAD_REQUEST, "카테고리 이름은 필수입니다.");
        }

        ButtonCategory category = buttonCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ButtonException(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."));
        if (!category.getUserId().equals(userId)) {
            throw new ButtonException(HttpStatus.FORBIDDEN, "본인 카테고리가 아닙니다.");
        }

        boolean duplicate = buttonCategoryRepository
                .existsByUserIdAndCategoryNameAndDeletedAtIsNullAndCategoryIdNot(userId, request.categoryName(), categoryId);
        if (duplicate) {
            throw new ButtonException(HttpStatus.CONFLICT, "동일 이름의 카테고리가 이미 존재합니다.");
        }

        category.setCategoryName(request.categoryName());
        ButtonCategory saved = buttonCategoryRepository.save(category);
        return new CategoryUpdateResponseDto(saved.getCategoryId(), saved.getCategoryName(), saved.getDisplayOrder(), saved.getUpdatedAt());
    }

    @Transactional
    public List<CategoryOrderItemDto> updateCategoryOrder(Long userId, List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new ButtonException(HttpStatus.BAD_REQUEST, "카테고리 ID 목록은 비어있을 수 없습니다.");
        }

        List<ButtonCategory> categories = buttonCategoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new ButtonException(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리 ID가 포함되어 있습니다.");
        }

        Map<Long, ButtonCategory> categoryMap = categories.stream()
                .collect(Collectors.toMap(ButtonCategory::getCategoryId, c -> c));

        for (ButtonCategory category : categories) {
            if (!category.getUserId().equals(userId)) {
                throw new ButtonException(HttpStatus.NOT_FOUND, "존재하지 않거나 본인 소유가 아닌 카테고리 ID가 포함되어 있습니다.");
            }
        }

        List<CategoryOrderItemDto> result = new ArrayList<>();
        int order = 1;
        for (Long id : categoryIds) {
            ButtonCategory category = categoryMap.get(id);
            category.setDisplayOrder(order);
            result.add(new CategoryOrderItemDto(id, order));
            order++;
        }
        buttonCategoryRepository.saveAll(categories);
        return result;
    }
}