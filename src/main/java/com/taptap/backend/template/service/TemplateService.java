package com.taptap.backend.template.service;

import com.taptap.backend.button.entity.Button;
import com.taptap.backend.button.entity.ButtonCategory;
import com.taptap.backend.button.repository.ButtonCategoryRepository;
import com.taptap.backend.button.repository.ButtonRepository;
import com.taptap.backend.template.dto.*;
import com.taptap.backend.template.entity.Template;
import com.taptap.backend.template.entity.TemplateButton;
import com.taptap.backend.template.entity.UserTemplateSelection;
import com.taptap.backend.template.exception.TemplateException;
import com.taptap.backend.template.repository.TemplateButtonRepository;
import com.taptap.backend.template.repository.TemplateRepository;
import com.taptap.backend.template.repository.UserTemplateSelectionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TemplateService {

    private static final int MAX_SELECTABLE_PRESETS = 10;

    private final TemplateRepository templateRepository;
    private final TemplateButtonRepository templateButtonRepository;
    private final ButtonCategoryRepository buttonCategoryRepository;
    private final ButtonRepository buttonRepository;
    private final UserTemplateSelectionRepository userTemplateSelectionRepository;

    public TemplateService(TemplateRepository templateRepository,
                           TemplateButtonRepository templateButtonRepository,
                           ButtonCategoryRepository buttonCategoryRepository,
                           ButtonRepository buttonRepository,
                           UserTemplateSelectionRepository userTemplateSelectionRepository) {
        this.templateRepository = templateRepository;
        this.templateButtonRepository = templateButtonRepository;
        this.buttonCategoryRepository = buttonCategoryRepository;
        this.buttonRepository = buttonRepository;
        this.userTemplateSelectionRepository = userTemplateSelectionRepository;
    }

    public List<TemplateResponseDto> getActiveTemplates() {
        return templateRepository.findByIsActiveTrueAndDeletedAtIsNullOrderByTemplateIdAsc()
                .stream()
                .map(TemplateResponseDto::from)
                .toList();
    }

    public TemplatePreviewResponseDto getTemplatePreview(Long templateId) {
        Template template = templateRepository.findById(templateId)
                .filter(t -> t.getIsActive() && t.getDeletedAt() == null)
                .orElseThrow(() -> new TemplateException(HttpStatus.NOT_FOUND, "존재하지 않는 템플릿입니다."));

        List<TemplateButton> buttons =
                templateButtonRepository.findByTemplateIdAndDeletedAtIsNullOrderByDisplayOrderAsc(templateId);

        Map<String, List<TemplateButton>> grouped = buttons.stream()
                .collect(Collectors.groupingBy(
                        TemplateButton::getCategoryName,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<TemplateCategoryGroupDto> categories = grouped.entrySet().stream()
                .map(entry -> new TemplateCategoryGroupDto(
                        entry.getKey(),
                        entry.getValue().stream().map(TemplatePreviewButtonDto::from).toList()
                ))
                .toList();

        return TemplatePreviewResponseDto.of(template, categories);
    }

    @Transactional
    public TemplateApplyResponseDto applyTemplate(Long userId, Long templateId, TemplateApplyRequestDto request) {
        List<Long> selectedPresetIds = request.selectedPresetIds();

        // 1. selectedPresetIds 개수 검증
        if (selectedPresetIds == null || selectedPresetIds.isEmpty() || selectedPresetIds.size() > MAX_SELECTABLE_PRESETS) {
            throw new TemplateException(HttpStatus.BAD_REQUEST, "선택 가능한 버튼은 1개 이상 10개 이하입니다.");
        }

        // 2. 이미 온보딩 완료한 유저인지 확인
        userTemplateSelectionRepository.findByUserId(userId)
                .filter(UserTemplateSelection::getIsCompleted)
                .ifPresent(s -> {
                    throw new TemplateException(HttpStatus.BAD_REQUEST, "이미 온보딩을 완료한 유저입니다.");
                });

        // 3. 템플릿 존재 확인
        Template template = templateRepository.findById(templateId)
                .filter(t -> t.getIsActive() && t.getDeletedAt() == null)
                .orElseThrow(() -> new TemplateException(HttpStatus.NOT_FOUND, "존재하지 않는 템플릿입니다."));

        // 4. 선택한 preset들이 해당 템플릿 소속인지 + 실제 존재하는지 검증
        List<TemplateButton> selectedPresets = templateButtonRepository.findAllById(selectedPresetIds).stream()
                .filter(tb -> tb.getDeletedAt() == null && tb.getTemplateId().equals(templateId))
                .toList();

        if (selectedPresets.size() != selectedPresetIds.size()) {
            throw new TemplateException(HttpStatus.BAD_REQUEST, "선택한 버튼 중 유효하지 않은 항목이 있습니다.");
        }

        // 5. category_name 기준 그룹핑(등장 순서 유지) 후 button_category 생성
        Map<String, List<TemplateButton>> groupedByCategory = selectedPresets.stream()
                .collect(Collectors.groupingBy(
                        TemplateButton::getCategoryName,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        Map<String, ButtonCategory> categoryMap = new LinkedHashMap<>();
        int order = 0;
        for (String categoryName : groupedByCategory.keySet()) {
            ButtonCategory category = new ButtonCategory();
            category.setUserId(userId);
            category.setCategoryName(categoryName);
            category.setDisplayOrder(order++);
            categoryMap.put(categoryName, buttonCategoryRepository.save(category));
        }

        // 6. button 생성
        List<Button> buttonsToCreate = new ArrayList<>();
        for (TemplateButton preset : selectedPresets) {
            ButtonCategory category = categoryMap.get(preset.getCategoryName());

            Button button = new Button();
            button.setUserId(userId);
            button.setCategoryId(category.getCategoryId());
            button.setSourcePresetId(preset.getPresetId());
            button.setButtonName(preset.getButtonName());
            button.setIconName(preset.getIconName());
            button.setIconColor(preset.getIconColor());
            buttonsToCreate.add(button);
        }
        List<Button> savedButtons = buttonRepository.saveAll(buttonsToCreate);

        // 7. user_template_selection 저장 (온보딩 완료 처리)
        UserTemplateSelection selection = new UserTemplateSelection();
        selection.setUserId(userId);
        selection.setTemplateId(templateId);
        selection.setIsCompleted(true);
        userTemplateSelectionRepository.save(selection);

        List<CreatedCategoryDto> createdCategories = categoryMap.values().stream()
                .map(CreatedCategoryDto::from)
                .toList();
        List<CreatedButtonDto> createdButtons = savedButtons.stream()
                .map(CreatedButtonDto::from)
                .toList();

        return new TemplateApplyResponseDto(
                template.getTemplateId(),
                template.getTemplateName(),
                createdCategories,
                createdButtons
        );
    }

    @Transactional
    public void skipTemplate(Long userId) {
        // 이미 온보딩 완료한 유저인지 확인
        userTemplateSelectionRepository.findByUserId(userId)
                .filter(UserTemplateSelection::getIsCompleted)
                .ifPresent(s -> {
                    throw new TemplateException(HttpStatus.BAD_REQUEST, "이미 온보딩을 완료한 유저입니다.");
                });

        UserTemplateSelection selection = new UserTemplateSelection();
        selection.setUserId(userId);
        selection.setTemplateId(null);
        selection.setIsCompleted(true);
        userTemplateSelectionRepository.save(selection);
    }
}