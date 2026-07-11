package com.taptap.backend.button.service;

import com.taptap.backend.button.dto.CategoryResponseDto;
import com.taptap.backend.button.dto.CreateCategoryRequestDto;
import com.taptap.backend.button.entity.ButtonCategory;
import com.taptap.backend.button.exception.ButtonException;
import com.taptap.backend.button.repository.ButtonCategoryRepository;
import com.taptap.backend.button.repository.ButtonRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
}