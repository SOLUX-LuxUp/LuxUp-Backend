package com.taptap.backend.insight.service;

import com.taptap.backend.button.entity.Button;
import com.taptap.backend.button.entity.ButtonCategory;
import com.taptap.backend.button.exception.ButtonException;
import com.taptap.backend.button.repository.ButtonCategoryRepository;
import com.taptap.backend.button.repository.ButtonRepository;
import com.taptap.backend.insight.client.GeminiClient;
import com.taptap.backend.insight.dto.LifestyleRecommendationActionResponseDto;
import com.taptap.backend.insight.dto.LifestyleRecommendationDto;
import com.taptap.backend.insight.entity.LifestyleRecommendation;
import com.taptap.backend.insight.repository.LifestyleRecommendationRepository;
import com.taptap.backend.record.entity.ButtonRecord;
import com.taptap.backend.record.repository.ButtonRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LifestyleRecommendationService {

    private static final List<String> ICON_CANDIDATES = List.of(
            "star", "heart", "book", "dumbbell", "coffee", "moon", "sun", "leaf", "pencil", "music"
    );
    private static final List<String> COLOR_CANDIDATES = List.of(
            "#FF6B6B", "#4D96FF", "#6BCB77", "#FFD93D", "#C780FA", "#FF9F45"
    );
    private static final int RECOMMENDATION_VALID_DAYS = 30;

    private final ButtonRepository buttonRepository;
    private final ButtonCategoryRepository buttonCategoryRepository;
    private final ButtonRecordRepository buttonRecordRepository;
    private final LifestyleRecommendationRepository lifestyleRecommendationRepository;
    private final GeminiClient geminiClient;

    /**
     * 유효한(수락/거절 안 됐고 만료 안 된) 추천이 있으면 그대로 반환하고,
     * 하나도 없으면 이번에 새로 생성한다 (AI 호출은 이 경우에만 발생).
     */
    @Transactional
    public List<LifestyleRecommendationDto> getRecommendations(Long userId) {
        List<LifestyleRecommendation> active = lifestyleRecommendationRepository.findActiveByUserId(userId);

        if (active.isEmpty()) {
            active = generateRecommendations(userId);
        }

        return active.stream().map(this::toDto).toList();
    }

    /**
     * 12. 라이프스타일 추천 수락/거절
     */
    @Transactional
    public LifestyleRecommendationActionResponseDto processAction(Long userId, Long recId, String action) {
        if (!"accept".equals(action) && !"dismiss".equals(action)) {
            throw new ButtonException(HttpStatus.BAD_REQUEST, "action 값이 올바르지 않습니다.");
        }

        LifestyleRecommendation rec = lifestyleRecommendationRepository.findById(recId)
                .orElseThrow(() -> new ButtonException(HttpStatus.NOT_FOUND, "존재하지 않는 추천입니다."));

        if (!rec.getUserId().equals(userId)) {
            throw new ButtonException(HttpStatus.FORBIDDEN, "본인 추천이 아닙니다.");
        }

        if (rec.getIsAccepted() || rec.getIsDismissed()) {
            throw new ButtonException(HttpStatus.CONFLICT, "이미 수락/거절 처리된 추천입니다.");
        }

        Long createdButtonId = null;

        if ("accept".equals(action)) {
            rec.accept();

            if ("ADD".equals(rec.getRecType())) {
                Button newButton = new Button();
                newButton.setUserId(userId);
                newButton.setButtonName(rec.getSuggestedButtonName());
                newButton.setIconName(rec.getSuggestedIconName());
                newButton.setIconColor(rec.getSuggestedIconColor());
                Button saved = buttonRepository.save(newButton);
                createdButtonId = saved.getButtonId();
            } else if ("DELETE".equals(rec.getRecType()) && rec.getTargetButtonId() != null) {
                buttonRepository.findById(rec.getTargetButtonId())
                        .ifPresent(btn -> btn.setIsActive(false));
            }
        } else {
            rec.dismiss();
        }

        return LifestyleRecommendationActionResponseDto.builder()
                .recId(rec.getRecId())
                .recType(rec.getRecType())
                .action(action)
                .createdButtonId(createdButtonId)
                .build();
    }

    private LifestyleRecommendationDto toDto(LifestyleRecommendation rec) {
        LifestyleRecommendationDto.LifestyleRecommendationDtoBuilder builder = LifestyleRecommendationDto.builder()
                .recId(rec.getRecId())
                .recType(rec.getRecType());

        if ("ADD".equals(rec.getRecType())) {
            builder.suggestedButtonName(rec.getSuggestedButtonName())
                    .suggestedIconName(rec.getSuggestedIconName())
                    .suggestedIconColor(rec.getSuggestedIconColor());
        } else if ("DELETE".equals(rec.getRecType()) && rec.getTargetButtonId() != null) {
            buttonRepository.findById(rec.getTargetButtonId())
                    .ifPresent(btn -> builder.buttonId(btn.getButtonId()).buttonName(btn.getButtonName()));
        }

        return builder.build();
    }

    // ================= 추천 생성 =================

    private List<LifestyleRecommendation> generateRecommendations(Long userId) {
        List<LifestyleRecommendation> newRecs = new ArrayList<>();
        newRecs.addAll(generateAddRecommendations(userId));
        newRecs.addAll(generateDeleteRecommendations(userId));

        if (newRecs.isEmpty()) {
            return List.of();
        }
        return lifestyleRecommendationRepository.saveAll(newRecs);
    }

    /**
     * 이번 달 top5 버튼 정보를 AI에게 알려주고, 새 버튼 이름을 추천받는다.
     * AI 호출이 실패하거나 응답이 이상하면, 그냥 빈 리스트를 반환한다 (전체를 막지 않음).
     */
    private List<LifestyleRecommendation> generateAddRecommendations(Long userId) {
        List<Long> buttonIds = buttonRepository.findActiveButtonIdsByUserId(userId);
        if (buttonIds.isEmpty()) {
            return List.of();
        }

        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        List<ButtonRecord> records = buttonRecordRepository.findRecordsInRange(
                buttonIds, monthStart.atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay()
        );
        if (records.isEmpty()) {
            return List.of();
        }

        Map<Long, Button> buttonMap = buttonRepository.findAllById(buttonIds).stream()
                .collect(Collectors.toMap(Button::getButtonId, b -> b));

        Map<Long, Long> buttonCounts = records.stream()
                .collect(Collectors.groupingBy(ButtonRecord::getButtonId, Collectors.counting()));

        List<String> top5Descriptions = buttonCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(e -> describeButton(buttonMap.get(e.getKey())))
                .toList();

        Set<String> existingNames = buttonMap.values().stream()
                .map(Button::getButtonName)
                .collect(Collectors.toSet());

        String prompt = buildAddPrompt(top5Descriptions);
        String aiResponse = geminiClient.generateText(prompt);
        List<String> suggestedNames = parseSuggestedNames(aiResponse, existingNames);

        List<LifestyleRecommendation> recs = new ArrayList<>();
        for (String name : suggestedNames) {
            recs.add(LifestyleRecommendation.builder()
                    .userId(userId)
                    .recType("ADD")
                    .suggestedButtonName(name)
                    .suggestedIconName(pickRandom(ICON_CANDIDATES))
                    .suggestedIconColor(pickRandom(COLOR_CANDIDATES))
                    .isAccepted(false)
                    .isDismissed(false)
                    .expiresAt(LocalDateTime.now().plusDays(RECOMMENDATION_VALID_DAYS))
                    .build());
        }
        return recs;
    }

    /**
     * 마지막 사용(기록)으로부터 1달이 지난 활성 버튼들을 찾아 삭제 제안으로 만든다.
     * 기록이 아예 없는 버튼은, 생성된 지 1달이 넘었을 때만 대상으로 삼는다(막 만든 새 버튼 오탐 방지).
     */
    private List<LifestyleRecommendation> generateDeleteRecommendations(Long userId) {
        List<Long> buttonIds = buttonRepository.findActiveButtonIdsByUserId(userId);
        if (buttonIds.isEmpty()) {
            return List.of();
        }

        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<Button> buttons = buttonRepository.findAllById(buttonIds);

        List<LifestyleRecommendation> recs = new ArrayList<>();
        for (Button button : buttons) {
            Optional<ButtonRecord> latest = buttonRecordRepository
                    .findTopByButtonIdAndDeletedAtIsNullOrderByRecordedAtDesc(button.getButtonId());

            boolean unused = latest
                    .map(r -> r.getRecordedAt().isBefore(oneMonthAgo))
                    .orElseGet(() -> button.getCreatedAt().isBefore(oneMonthAgo));

            if (unused) {
                recs.add(LifestyleRecommendation.builder()
                        .userId(userId)
                        .recType("DELETE")
                        .targetButtonId(button.getButtonId())
                        .isAccepted(false)
                        .isDismissed(false)
                        .expiresAt(LocalDateTime.now().plusDays(RECOMMENDATION_VALID_DAYS))
                        .build());
            }
        }
        return recs;
    }

    private String describeButton(Button button) {
        String categoryName = resolveCategoryName(button.getCategoryId());
        return categoryName != null
                ? button.getButtonName() + "(" + categoryName + ")"
                : button.getButtonName();
    }

    private String resolveCategoryName(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return buttonCategoryRepository.findById(categoryId)
                .map(ButtonCategory::getCategoryName)
                .orElse(null);
    }

    private String buildAddPrompt(List<String> topButtonDescriptions) {
        return "사용자가 습관 관리 앱에서 이번 달에 자주 사용한 버튼: "
                + String.join(", ", topButtonDescriptions)
                + ". 이 사용자에게 새로 추천하면 좋을 습관 버튼 이름을 한국어로 최대 3개 알려줘. "
                + "각 이름은 2~10자의 짧은 명사형으로 만들고, 쉼표(,)로만 구분해서 한 줄로만 답해. "
                + "번호, 설명, 다른 문장은 절대 붙이지 마.";
    }

    private List<String> parseSuggestedNames(String aiResponse, Set<String> existingNames) {
        if (aiResponse == null || aiResponse.isBlank()) {
            return List.of();
        }

        Set<String> existingLower = existingNames.stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return Arrays.stream(aiResponse.split("[,\\n]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .filter(s -> s.length() <= 50) // DB 컬럼 길이(50) 초과 방지
                .filter(s -> !existingLower.contains(s.toLowerCase()))
                .distinct()
                .limit(3)
                .toList();
    }

    private String pickRandom(List<String> candidates) {
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }
}