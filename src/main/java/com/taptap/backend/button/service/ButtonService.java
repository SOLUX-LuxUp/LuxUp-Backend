package com.taptap.backend.button.service;

import com.taptap.backend.button.dto.*;
import com.taptap.backend.button.entity.Button;
import com.taptap.backend.button.entity.ButtonCategory;
import com.taptap.backend.button.exception.ButtonException;
import com.taptap.backend.button.repository.ButtonCategoryRepository;
import com.taptap.backend.button.repository.ButtonRecordQueryRepository;
import com.taptap.backend.button.repository.ButtonRepository;
import com.taptap.backend.record.repository.ButtonRecordRepository;
import com.taptap.backend.reminder.repository.ReminderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ButtonService {

    // TODO: 누리님께 받은 실제 아이콘/색상 풀로 교체 필요 (현재는 임시 placeholder)
    private static final List<String> DEFAULT_ICON_NAMES = List.of(
            "icon-water", "icon-stretch", "icon-book", "icon-meditation"
    );
    private static final List<String> DEFAULT_ICON_COLORS = List.of(
            "#FF5733", "#00BFFF", "#A8D8A8", "#FFC107"
    );

    private static final List<String> ALLOWED_PERIOD_UNITS = List.of("DAY", "WEEK", "MONTH", "YEAR");
    private static final List<String> ALLOWED_COMPARISON_TYPES = List.of("GTE", "LTE");

    private final ButtonRepository buttonRepository;
    private final ButtonCategoryRepository buttonCategoryRepository;
    private final ButtonRecordRepository buttonRecordRepository;
    private final ReminderRepository reminderRepository;
    private final ButtonRecordQueryRepository buttonRecordQueryRepository;
    private final Random random = new Random();

    public ButtonService(ButtonRepository buttonRepository,
                         ButtonCategoryRepository buttonCategoryRepository,
                         ButtonRecordRepository buttonRecordRepository,
                         ReminderRepository reminderRepository,
                         ButtonRecordQueryRepository buttonRecordQueryRepository) {
        this.buttonRepository = buttonRepository;
        this.buttonCategoryRepository = buttonCategoryRepository;
        this.buttonRecordRepository = buttonRecordRepository;
        this.reminderRepository = reminderRepository;
        this.buttonRecordQueryRepository = buttonRecordQueryRepository;
    }

    @Transactional
    public ButtonResponseDto createButton(Long userId, CreateButtonRequestDto request) {
        validateExpiry(request.expiryEnabled(), request.expiredAt());
        validateGoal(request.goalEnabled(), request.goalPeriodUnit(), request.goalCount(), request.goalComparisonType());
        validateCategoryOwnership(userId, request.categoryId());

        Button button = new Button();
        button.setUserId(userId);
        button.setCategoryId(request.categoryId());

        button.setButtonName(resolveButtonName(userId, request.buttonName()));
        button.setIconName(resolveIconName(request.iconName()));
        button.setIconColor(resolveIconColor(request.iconColor()));

        boolean goalEnabled = Boolean.TRUE.equals(request.goalEnabled());
        button.setGoalEnabled(goalEnabled);
        button.setGoalName(goalEnabled ? request.goalName() : null);
        button.setGoalPeriodUnit(goalEnabled ? request.goalPeriodUnit() : null);
        button.setGoalCount(goalEnabled ? request.goalCount() : null);
        button.setGoalComparisonType(goalEnabled ? request.goalComparisonType() : null);

        boolean expiryEnabled = Boolean.TRUE.equals(request.expiryEnabled());
        button.setExpiryEnabled(expiryEnabled);
        button.setExpiredAt(expiryEnabled ? request.expiredAt() : null);

        Button saved = buttonRepository.save(button);
        return toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public ButtonListResponseDto getButtons(Long userId, Long categoryIdFilter) {
        List<Button> buttons = buttonRepository.findByUserIdAndIsActiveTrue(userId);

        if (categoryIdFilter != null) {
            buttons = buttons.stream()
                    .filter(b -> categoryIdFilter.equals(b.getCategoryId()))
                    .toList();
        }

        Map<Long, LocalDateTime> lastRecordedMap = fetchLastRecordedAtMap(buttons);

        List<FavoriteButtonItemDto> favorites = buildFavorites(buttons, lastRecordedMap);
        List<CategoryGroupDto> categories = buildCategoryGroups(userId, buttons, lastRecordedMap);

        return new ButtonListResponseDto(favorites, categories);
    }

    @Transactional(readOnly = true)
    public List<FavoriteButtonItemDto> getFavoriteButtons(Long userId) {
        List<Button> buttons = buttonRepository.findByUserIdAndIsActiveTrue(userId);
        Map<Long, LocalDateTime> lastRecordedMap = fetchLastRecordedAtMap(buttons);
        return buildFavorites(buttons, lastRecordedMap);
    }

    private Map<Long, LocalDateTime> fetchLastRecordedAtMap(List<Button> buttons) {
        if (buttons.isEmpty()) {
            return Map.of();
        }
        List<Long> buttonIds = buttons.stream().map(Button::getButtonId).toList();
        List<Object[]> rows = buttonRecordQueryRepository.findLatestRecordedAtByButtonIds(buttonIds);
        Map<Long, LocalDateTime> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put((Long) row[0], (LocalDateTime) row[1]);
        }
        return map;
    }

    private List<FavoriteButtonItemDto> buildFavorites(List<Button> buttons, Map<Long, LocalDateTime> lastRecordedMap) {
        Map<Long, ButtonCategory> categoryMap = loadCategoriesForButtons(buttons);

        return buttons.stream()
                .filter(b -> Boolean.TRUE.equals(b.getIsFavorite()))
                .sorted(Comparator.comparing(Button::getFavoriteOrder, Comparator.nullsLast(Integer::compareTo)))
                .map(b -> {
                    ButtonCategory category = b.getCategoryId() != null ? categoryMap.get(b.getCategoryId()) : null;
                    return new FavoriteButtonItemDto(
                            b.getButtonId(), b.getButtonName(), b.getIconName(), b.getIconColor(),
                            b.getCategoryId(), category != null ? category.getCategoryName() : null,
                            b.getIsFavorite(), b.getFavoriteOrder(),
                            b.getGoalEnabled(), b.getGoalName(), b.getGoalPeriodUnit(), b.getGoalCount(), b.getGoalComparisonType(),
                            b.getExpiryEnabled(), b.getExpiredAt(), b.getIsActive(),
                            lastRecordedMap.get(b.getButtonId()), b.getCreatedAt()
                    );
                })
                .toList();
    }

    private List<CategoryGroupDto> buildCategoryGroups(Long userId, List<Button> buttons, Map<Long, LocalDateTime> lastRecordedMap) {
        Map<Long, ButtonCategory> categoryMap = loadCategoriesForButtons(buttons);

        Map<Long, List<Button>> grouped = buttons.stream()
                .collect(Collectors.groupingBy(b -> b.getCategoryId() == null ? -1L : b.getCategoryId()));

        List<CategoryGroupDto> result = new ArrayList<>();

        grouped.entrySet().stream()
                .filter(e -> e.getKey() != -1L)
                .sorted(Comparator.comparing(e -> categoryMap.get(e.getKey()).getDisplayOrder()))
                .forEach(e -> {
                    ButtonCategory category = categoryMap.get(e.getKey());
                    result.add(new CategoryGroupDto(
                            category.getCategoryId(), category.getCategoryName(), category.getDisplayOrder(),
                            toCategoryButtonItems(e.getValue(), lastRecordedMap)
                    ));
                });

        List<Button> uncategorized = grouped.get(-1L);
        if (uncategorized != null && !uncategorized.isEmpty()) {
            result.add(new CategoryGroupDto(null, null, null, toCategoryButtonItems(uncategorized, lastRecordedMap)));
        }

        return result;
    }

    private List<CategoryButtonItemDto> toCategoryButtonItems(List<Button> buttons, Map<Long, LocalDateTime> lastRecordedMap) {
        return buttons.stream()
                .map(b -> new CategoryButtonItemDto(
                        b.getButtonId(), b.getButtonName(), b.getIconName(), b.getIconColor(),
                        b.getIsFavorite(), b.getFavoriteOrder(),
                        b.getGoalEnabled(), b.getGoalName(), b.getGoalPeriodUnit(), b.getGoalCount(), b.getGoalComparisonType(),
                        b.getExpiryEnabled(), b.getExpiredAt(), b.getIsActive(),
                        lastRecordedMap.get(b.getButtonId()), b.getCreatedAt()
                ))
                .toList();
    }

    private Map<Long, ButtonCategory> loadCategoriesForButtons(List<Button> buttons) {
        List<Long> categoryIds = buttons.stream()
                .map(Button::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return buttonCategoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(ButtonCategory::getCategoryId, c -> c));
    }

    @Transactional
    public UpdateButtonResponseDto updateButton(Long userId, Long buttonId, UpdateButtonRequestDto request) {
        Button button = getOwnedActiveButton(userId, buttonId);

        if (Boolean.TRUE.equals(request.clearCategory())) {
            button.setCategoryId(null);
        } else if (request.categoryId() != null) {
            validateCategoryOwnership(userId, request.categoryId());
            button.setCategoryId(request.categoryId());
        }

        if (request.buttonName() != null) {
            button.setButtonName(request.buttonName());
        }
        if (request.iconName() != null) {
            button.setIconName(request.iconName());
        }
        if (request.iconColor() != null) {
            button.setIconColor(request.iconColor());
        }

        applyGoalUpdate(button, request);
        applyExpiryUpdate(button, request);

        Button saved = buttonRepository.save(button);
        return new UpdateButtonResponseDto(
                saved.getButtonId(), saved.getButtonName(), saved.getIconName(), saved.getIconColor(),
                saved.getCategoryId(), saved.getGoalEnabled(), saved.getGoalName(), saved.getGoalPeriodUnit(),
                saved.getGoalCount(), saved.getGoalComparisonType(), saved.getExpiryEnabled(),
                saved.getExpiredAt(), saved.getUpdatedAt()
        );
    }

    @Transactional
    public void deleteButton(Long userId, Long buttonId) {
        Button button = getOwnedActiveButton(userId, buttonId);
        button.setIsActive(false);
        buttonRepository.save(button);

        List<Long> buttonIds = List.of(buttonId);
        buttonRecordRepository.softDeleteByButtonIds(buttonIds);
        reminderRepository.softDeleteByButtonIds(buttonIds);
    }

    @Transactional
    public FavoriteResponseDto setFavorite(Long userId, Long buttonId, Boolean isFavorite) {
        Button button = getOwnedActiveButton(userId, buttonId);

        if (Boolean.TRUE.equals(isFavorite)) {
            if (!Boolean.TRUE.equals(button.getIsFavorite())) {
                Integer maxOrder = buttonRepository.findMaxFavoriteOrderByUserId(userId);
                button.setIsFavorite(true);
                button.setFavoriteOrder((maxOrder == null ? 0 : maxOrder) + 1);
                buttonRepository.save(button);
            }
        } else {
            if (Boolean.TRUE.equals(button.getIsFavorite())) {
                Integer removedOrder = button.getFavoriteOrder();
                button.setIsFavorite(false);
                button.setFavoriteOrder(null);
                buttonRepository.save(button);
                buttonRepository.decrementFavoriteOrderAfter(userId, removedOrder);
            }
        }

        return new FavoriteResponseDto(button.getButtonId(), button.getIsFavorite(), button.getFavoriteOrder());
    }

    @Transactional
    public List<FavoriteOrderItemDto> updateFavoriteOrder(Long userId, List<Long> buttonIds) {
        List<Button> buttons = buttonRepository.findAllById(buttonIds);

        if (buttons.size() != buttonIds.size()) {
            throw new ButtonException(HttpStatus.NOT_FOUND, "존재하지 않는 버튼 ID가 포함되어 있습니다.");
        }

        Map<Long, Button> buttonMap = buttons.stream()
                .collect(Collectors.toMap(Button::getButtonId, b -> b));

        for (Button button : buttons) {
            if (!button.getUserId().equals(userId)) {
                throw new ButtonException(HttpStatus.NOT_FOUND, "존재하지 않는 버튼 ID가 포함되어 있습니다.");
            }
            if (!Boolean.TRUE.equals(button.getIsFavorite())) {
                throw new ButtonException(HttpStatus.BAD_REQUEST, "즐겨찾기 설정되지 않은 버튼 ID가 포함되어 있습니다.");
            }
        }

        List<FavoriteOrderItemDto> result = new ArrayList<>();
        int order = 1;
        for (Long id : buttonIds) {
            Button button = buttonMap.get(id);
            button.setFavoriteOrder(order);
            result.add(new FavoriteOrderItemDto(id, order));
            order++;
        }
        buttonRepository.saveAll(buttons);
        return result;
    }

    private void applyGoalUpdate(Button button, UpdateButtonRequestDto request) {
        Boolean goalEnabled = request.goalEnabled() != null ? request.goalEnabled() : button.getGoalEnabled();
        String goalName = request.goalName() != null ? request.goalName() : button.getGoalName();
        String goalPeriodUnit = request.goalPeriodUnit() != null ? request.goalPeriodUnit() : button.getGoalPeriodUnit();
        Integer goalCount = request.goalCount() != null ? request.goalCount() : button.getGoalCount();
        String goalComparisonType = request.goalComparisonType() != null ? request.goalComparisonType() : button.getGoalComparisonType();

        validateGoal(goalEnabled, goalPeriodUnit, goalCount, goalComparisonType);

        button.setGoalEnabled(goalEnabled);
        if (Boolean.TRUE.equals(goalEnabled)) {
            button.setGoalName(goalName);
            button.setGoalPeriodUnit(goalPeriodUnit);
            button.setGoalCount(goalCount);
            button.setGoalComparisonType(goalComparisonType);
        } else {
            button.setGoalName(null);
            button.setGoalPeriodUnit(null);
            button.setGoalCount(null);
            button.setGoalComparisonType(null);
        }
    }

    private void applyExpiryUpdate(Button button, UpdateButtonRequestDto request) {
        Boolean expiryEnabled = request.expiryEnabled() != null ? request.expiryEnabled() : button.getExpiryEnabled();
        LocalDate expiredAt = request.expiredAt() != null ? request.expiredAt() : button.getExpiredAt();

        validateExpiry(expiryEnabled, expiredAt);

        button.setExpiryEnabled(expiryEnabled);
        button.setExpiredAt(Boolean.TRUE.equals(expiryEnabled) ? expiredAt : null);
    }

    private Button getOwnedActiveButton(Long userId, Long buttonId) {
        Button button = buttonRepository.findById(buttonId)
                .orElseThrow(() -> new ButtonException(HttpStatus.NOT_FOUND, "존재하지 않는 버튼입니다."));
        if (!button.getUserId().equals(userId)) {
            throw new ButtonException(HttpStatus.FORBIDDEN, "본인 버튼이 아닙니다.");
        }
        if (!Boolean.TRUE.equals(button.getIsActive())) {
            throw new ButtonException(HttpStatus.NOT_FOUND, "존재하지 않거나 이미 삭제된 버튼입니다.");
        }
        return button;
    }

    private void validateExpiry(Boolean expiryEnabled, LocalDate expiredAt) {
        if (Boolean.TRUE.equals(expiryEnabled)) {
            if (expiredAt == null || !expiredAt.isAfter(LocalDate.now())) {
                throw new ButtonException(HttpStatus.BAD_REQUEST, "만료일은 오늘 이후 날짜여야 합니다.");
            }
        }
    }

    private void validateGoal(Boolean goalEnabled, String periodUnit, Integer count, String comparisonType) {
        if (!Boolean.TRUE.equals(goalEnabled)) {
            return;
        }
        if (periodUnit != null && !ALLOWED_PERIOD_UNITS.contains(periodUnit)) {
            throw new ButtonException(HttpStatus.BAD_REQUEST, "goalPeriodUnit 값이 올바르지 않습니다.");
        }
        if (count != null && (count < 1 || count > 50)) {
            throw new ButtonException(HttpStatus.BAD_REQUEST, "goalCount는 1~50 사이여야 합니다.");
        }
        if (comparisonType != null && !ALLOWED_COMPARISON_TYPES.contains(comparisonType)) {
            throw new ButtonException(HttpStatus.BAD_REQUEST, "goalComparisonType 값이 올바르지 않습니다.");
        }
    }

    private void validateCategoryOwnership(Long userId, Long categoryId) {
        if (categoryId == null) {
            return;
        }
        boolean exists = buttonCategoryRepository.existsByCategoryIdAndUserId(categoryId, userId);
        if (!exists) {
            throw new ButtonException(HttpStatus.NOT_FOUND, "존재하지 않거나 본인 소유가 아닌 카테고리입니다.");
        }
    }

    private String resolveButtonName(Long userId, String requestedName) {
        if (requestedName != null && !requestedName.isBlank()) {
            return requestedName;
        }
        long count = buttonRepository.countByUserId(userId);
        return "새로운 버튼 " + (count + 1);
    }

    private String resolveIconName(String requestedIconName) {
        if (requestedIconName != null && !requestedIconName.isBlank()) {
            return requestedIconName;
        }
        return DEFAULT_ICON_NAMES.get(random.nextInt(DEFAULT_ICON_NAMES.size()));
    }

    private String resolveIconColor(String requestedIconColor) {
        if (requestedIconColor != null && !requestedIconColor.isBlank()) {
            return requestedIconColor;
        }
        return DEFAULT_ICON_COLORS.get(random.nextInt(DEFAULT_ICON_COLORS.size()));
    }

    @Transactional(readOnly = true)
    public List<FavoriteButtonItemDto> searchButtons(Long userId, String keyword) {
        List<Button> buttons = buttonRepository.findByUserIdAndIsActiveTrueAndButtonNameContainingIgnoreCase(userId, keyword);
        Map<Long, LocalDateTime> lastRecordedMap = fetchLastRecordedAtMap(buttons);
        Map<Long, ButtonCategory> categoryMap = loadCategoriesForButtons(buttons);

        return buttons.stream()
                .map(b -> {
                    ButtonCategory category = b.getCategoryId() != null ? categoryMap.get(b.getCategoryId()) : null;
                    return new FavoriteButtonItemDto(
                            b.getButtonId(), b.getButtonName(), b.getIconName(), b.getIconColor(),
                            b.getCategoryId(), category != null ? category.getCategoryName() : null,
                            b.getIsFavorite(), b.getFavoriteOrder(),
                            b.getGoalEnabled(), b.getGoalName(), b.getGoalPeriodUnit(), b.getGoalCount(), b.getGoalComparisonType(),
                            b.getExpiryEnabled(), b.getExpiredAt(), b.getIsActive(),
                            lastRecordedMap.get(b.getButtonId()), b.getCreatedAt()
                    );
                })
                .toList();
    }

    private ButtonResponseDto toResponseDto(Button button) {
        return new ButtonResponseDto(
                button.getButtonId(),
                button.getButtonName(),
                button.getIconName(),
                button.getIconColor(),
                button.getCategoryId(),
                button.getGoalEnabled(),
                button.getGoalName(),
                button.getGoalPeriodUnit(),
                button.getGoalCount(),
                button.getGoalComparisonType(),
                button.getExpiryEnabled(),
                button.getExpiredAt(),
                button.getIsFavorite(),
                button.getIsActive(),
                null,
                button.getCreatedAt()
        );
    }
}