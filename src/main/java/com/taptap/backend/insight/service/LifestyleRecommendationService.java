package com.taptap.backend.insight.service;

import com.taptap.backend.button.entity.Button;
import com.taptap.backend.button.entity.ButtonCategory;
import com.taptap.backend.button.exception.ButtonException;
import com.taptap.backend.button.repository.ButtonCategoryRepository;
import com.taptap.backend.button.repository.ButtonRepository;
import com.taptap.backend.insight.client.GeminiClient;
import com.taptap.backend.insight.dto.*;
import com.taptap.backend.insight.entity.LifestyleRecommendation;
import com.taptap.backend.insight.repository.LifestyleRecommendationRepository;
import com.taptap.backend.insight.util.TimeSlotResolver;
import com.taptap.backend.record.entity.ButtonRecord;
import com.taptap.backend.record.repository.ButtonRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LifestyleRecommendationService {

    // 아이콘 목록 (총 44개)
    private static final List<String> ICON_CANDIDATES = List.of(
            "book", "calendar", "call", "camera", "car", "celebrate", "chat", "clean", "clothes", "cup",
            "dessert", "dog", "door", "drink", "fire", "flower", "food", "fruit", "gym", "health",
            "labtop", "lightbulb", "lightning", "liquid", "lock", "mask", "medicine", "music1", "music2", "note",
            "pay", "pencil", "person", "plant", "selfcare", "shoe", "shopping1", "shopping2", "shower", "sleep",
            "sport1", "sport2", "sun", "travel"
    );
    // ⚠️ HEX가 아니라 디자인 시스템에서 쓰는 색상 이름 그대로.
    private static final List<String> COLOR_CANDIDATES = List.of(
            "red", "orange", "yellow", "green", "cyan", "blue", "indigo", "purple", "pink", "grey", "darkgrey", "black"
    );
    private static final int RECOMMENDATION_VALID_DAYS = 30; // ADD 추천 만료 여유값(안전망 용도, 실제 재생성 기준은 "이번 달에 이미 만들었는지"임)
    private static final int ADD_RECOMMENDATION_COUNT = 5;
    private static final int DELETE_RECOMMENDATION_COUNT = 3;

    // 기획 확정 (신규): 라이프스타일 분석/AI 추천 활성화 조건
    private static final int ANALYSIS_MIN_DAYS = 7;          // 라이프스타일 분석(라벨) 활성화: 이번 달 기록한 날이 7일 이상
    private static final int ANALYSIS_MIN_TAP_COUNT = 20;    // 라이프스타일 분석(라벨) 활성화: 이번 달 총 기록이 20회 이상
    private static final int AI_RECOMMENDATION_START_DAY_OF_MONTH = 20; // AI 버튼 추천(ADD)은 매달 20일이 지나야 활성화

    private static final int REGULAR_MIN_TAP_COUNT = 3; // 규칙적인 기억(간격): 간격 판단하려면 최소 3번(간격 2개)은 있어야 함
    private static final double REGULAR_INTERVAL_CV_THRESHOLD = 0.3; // 규칙적인 기억(간격): 탭 간격의 변동계수 30% 이하면 "규칙적"
    private static final double REGULAR_TIME_SLOT_CONCENTRATION_THRESHOLD = 0.7; // 규칙적인 기억(시간대): 특정 시간대 집중도 70% 이상
    private static final int CONSISTENT_DAYS_THRESHOLD = 20;   // 꾸준한 기억: 최근 30일 중 20일 이상
    private static final int CONSISTENT_STREAK_THRESHOLD = 7;  // 꾸준한 기억: 7일 연속
    private static final double MOM_DAILY_AVG_THRESHOLD = 5.0; // 꼼꼼한 기억 "후보" 조건: 일평균 5회 이상
    private static final double MOM_TOP5_SHARE_THRESHOLD = 0.7; // 꼼꼼한 기억 "후보" 조건: top5가 전체의 70% 미만(=고르게 사용)
    private static final double MOM_OVERRIDE_DAILY_AVG_THRESHOLD = 20.0; // 카테고리가 있어도 이 이상이면 균형보다 꼼꼼 우선(기획서 예시)
    private static final double BALANCED_MAX_CATEGORY_RATIO_THRESHOLD = 0.4; // 균형적인 기억: 1위 카테고리 비중 40% 미만
    private static final double FOCUSED_TOP_BUTTON_RATIO_THRESHOLD = 0.4; // 집중하는 기억: top1 버튼 비중 40% 이상
    private static final double FOCUSED_TOP_CATEGORY_RATIO_THRESHOLD = 0.6; // 집중하는 기억: top1 카테고리 비중 60% 이상

    private final ButtonRepository buttonRepository;
    private final ButtonCategoryRepository buttonCategoryRepository;
    private final ButtonRecordRepository buttonRecordRepository;
    private final LifestyleRecommendationRepository lifestyleRecommendationRepository;
    private final GeminiClient geminiClient;

    /**
     * 라이프스타일 라벨(규칙 기반, 이번 달 기록이 7일↑ & 20회↑일 때만 활성화) +
     * ADD 추천(AI 필요 -> 매달 20일 이후, 그 달에 딱 1번만 생성) +
     * DELETE 추천(AI 불필요, 쿼리만 하면 되니 -> 매번 라이브로 재계산해서 최신 상태 유지)
     */
    @Transactional
    public LifestyleRecommendationsResponseDto getRecommendations(Long userId) {
        List<Long> buttonIds = buttonRepository.findActiveButtonIdsByUserId(userId);
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDateTime monthStartAt = monthStart.atStartOfDay();
        LocalDateTime monthEndAt = monthStart.plusMonths(1).atStartOfDay();

        List<ButtonRecord> thisMonthRecords = buttonIds.isEmpty()
                ? List.of()
                : buttonRecordRepository.findRecordsInRange(buttonIds, monthStartAt, today.plusDays(1).atStartOfDay());
        Map<Long, Button> buttonMap = buttonIds.isEmpty()
                ? Map.of()
                : buttonRepository.findAllById(buttonIds).stream().collect(Collectors.toMap(Button::getButtonId, b -> b));

        List<Map.Entry<Long, Long>> top5ButtonEntries = topButtonEntries(thisMonthRecords, 5);
        List<AnalysisButtonDto> analysisButtons = top5ButtonEntries.stream()
                .map(e -> {
                    Button btn = buttonMap.get(e.getKey());
                    return AnalysisButtonDto.builder()
                            .buttonId(btn.getButtonId())
                            .buttonName(btn.getButtonName())
                            .iconName(btn.getIconName())
                            .iconColor(btn.getIconColor())
                            .build();
                })
                .toList();

        // 라이프스타일 분석(라벨)은 이번 달 기록이 7일 이상 & 20회 이상일 때만 활성화
        boolean analysisAvailable = hasEnoughDataForAnalysis(thisMonthRecords);
        LifestyleLabel label = analysisAvailable
                ? computeLifestyleLabel(thisMonthRecords, buttonMap, top5ButtonEntries)
                : null;

        List<LifestyleRecommendation> allActive = lifestyleRecommendationRepository.findActiveByUserId(userId);

        // AI 추천(ADD)은 매달 20일이 지나야 활성화되고, 그 달에 딱 1번만 생성한다.
        boolean aiWindowOpen = today.getDayOfMonth() >= AI_RECOMMENDATION_START_DAY_OF_MONTH;
        boolean alreadyGeneratedThisMonth = lifestyleRecommendationRepository
                .existsByUserIdAndRecTypeAndCreatedAtBetween(userId, "ADD", monthStartAt, monthEndAt);

        List<LifestyleRecommendation> activeAdd = allActive.stream()
                .filter(r -> "ADD".equals(r.getRecType()))
                .collect(Collectors.toCollection(ArrayList::new));

        if (!alreadyGeneratedThisMonth && aiWindowOpen && analysisAvailable) {
            List<LifestyleRecommendation> newAdd = generateAddRecommendations(userId, thisMonthRecords, buttonMap, top5ButtonEntries);
            if (!newAdd.isEmpty()) {
                activeAdd = lifestyleRecommendationRepository.saveAll(newAdd);
            }
        }

        List<LifestyleRecommendation> existingDelete = allActive.stream()
                .filter(r -> "DELETE".equals(r.getRecType()))
                .collect(Collectors.toCollection(ArrayList::new));
        List<LifestyleRecommendation> activeDelete = refreshDeleteRecommendations(userId, buttonIds, existingDelete);

        List<LifestyleRecommendationDto> recommendations = new ArrayList<>();
        activeAdd.forEach(r -> recommendations.add(toDto(r)));
        activeDelete.forEach(r -> recommendations.add(toDto(r)));

        return LifestyleRecommendationsResponseDto.builder()
                .analysisAvailable(analysisAvailable)
                .lifestyleLabel(label != null ? label.getLabelName() : null)
                .lifestyleCaption(label != null ? label.getCaption() : "아직 라이프스타일 분석에 필요한 기록이 부족해요.")
                .analysisButtons(analysisButtons)
                .recommendations(recommendations)
                .build();
    }

    /** 라이프스타일 분석(라벨) 활성화 조건: 이번 달 기록한 날이 7일 이상 & 총 기록이 20회 이상 */
    private boolean hasEnoughDataForAnalysis(List<ButtonRecord> thisMonthRecords) {
        if (thisMonthRecords.size() < ANALYSIS_MIN_TAP_COUNT) {
            return false;
        }
        long distinctDays = thisMonthRecords.stream()
                .map(r -> r.getRecordedAt().toLocalDate())
                .distinct()
                .count();
        return distinctDays >= ANALYSIS_MIN_DAYS;
    }

    /**
     * DELETE 추천은 AI가 필요 없는 순수 쿼리라, 캐시하지 않고 매번 라이브로 다시 계산한다.
     * - 더 이상 조건에 안 맞는(다시 쓰였거나, 다른 경로로 비활성화된) 기존 추천은 자동 거절 처리.
     * - 새로 조건을 만족하게 된 버튼은 새 추천으로 추가.
     * - 여전히 유효한 기존 추천은 중복 생성하지 않고 그대로 재사용.
     * - 최종적으로 오래된 것부터 상위 3개만 반환.
     */
    private List<LifestyleRecommendation> refreshDeleteRecommendations(
            Long userId, List<Long> buttonIds, List<LifestyleRecommendation> existingDelete
    ) {
        if (buttonIds.isEmpty()) {
            existingDelete.forEach(LifestyleRecommendation::dismiss);
            return List.of();
        }

        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<Button> buttons = buttonRepository.findAllById(buttonIds);

        Map<Long, LifestyleRecommendation> existingByButton = existingDelete.stream()
                .collect(Collectors.toMap(LifestyleRecommendation::getTargetButtonId, r -> r));

        // 지금 이 순간 기준으로 "미사용 1달 이상"인 버튼들을 다시 계산
        List<Map.Entry<Button, LocalDateTime>> candidates = new ArrayList<>();
        for (Button button : buttons) {
            Optional<ButtonRecord> latest = buttonRecordRepository
                    .findTopByButtonIdAndDeletedAtIsNullOrderByRecordedAtDesc(button.getButtonId());
            LocalDateTime referenceTime = latest.map(ButtonRecord::getRecordedAt).orElse(button.getCreatedAt());
            if (referenceTime.isBefore(oneMonthAgo)) {
                candidates.add(Map.entry(button, referenceTime));
            }
        }
        Set<Long> candidateButtonIds = candidates.stream()
                .map(e -> e.getKey().getButtonId())
                .collect(Collectors.toSet());

        // 더 이상 후보가 아닌 기존 추천(다시 쓰였거나 비활성화됨)은 자동 거절
        for (LifestyleRecommendation existing : existingDelete) {
            if (!candidateButtonIds.contains(existing.getTargetButtonId())) {
                existing.dismiss();
            }
        }

        List<Map.Entry<Button, LocalDateTime>> topCandidates = candidates.stream()
                .sorted(Comparator.comparing(Map.Entry::getValue)) // 오래된 것부터
                .limit(DELETE_RECOMMENDATION_COUNT)
                .toList();

        List<LifestyleRecommendation> result = new ArrayList<>();
        List<LifestyleRecommendation> toSave = new ArrayList<>();

        for (Map.Entry<Button, LocalDateTime> entry : topCandidates) {
            Long buttonId = entry.getKey().getButtonId();
            LifestyleRecommendation existing = existingByButton.get(buttonId);

            boolean reusable = existing != null
                    && !Boolean.TRUE.equals(existing.getIsDismissed())
                    && !Boolean.TRUE.equals(existing.getIsAccepted());

            if (reusable) {
                result.add(existing);
            } else {
                toSave.add(LifestyleRecommendation.builder()
                        .userId(userId)
                        .recType("DELETE")
                        .targetButtonId(buttonId)
                        .isAccepted(false)
                        .isDismissed(false)
                        .expiresAt(LocalDateTime.now().plusDays(RECOMMENDATION_VALID_DAYS))
                        .build());
            }
        }

        if (!toSave.isEmpty()) {
            result.addAll(lifestyleRecommendationRepository.saveAll(toSave));
        }

        return result;
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

            LocalDateTime lastRecordedAt = buttonRecordRepository
                    .findTopByButtonIdAndDeletedAtIsNullOrderByRecordedAtDesc(rec.getTargetButtonId())
                    .map(ButtonRecord::getRecordedAt)
                    .orElse(null); // 기록이 아예 없던 버튼일 수도 있음

            builder.lastRecordedAt(lastRecordedAt);
        }

        return builder.build();
    }

    // ================= 라이프스타일 라벨 계산 =================

    private enum LifestyleLabel {
        REGULAR("규칙적인 기억", "규칙적으로 기록하며 리듬을 만들어가고 있어요."),
        CONSISTENT("꾸준한 기억", "하루하루 기록을 놓치지 않고 꾸준한 기억을 이어가고 있어요."),
        METICULOUS("꼼꼼한 기억", "다양한 활동을 빠뜨리지 않고 기록하며 하루를 세심하게 기억하고 있어요."),
        BALANCED("균형적인 기억", "여러 활동을 기록하며 균형 있는 일상을 만들어가고 있어요."),
        FOCUSED("집중하는 기억", "중요한 활동을 중심으로 기록하며 의미 있는 순간들을 기억하고 있어요."),
        DIVERSE("다채로운 기억", "다양한 활동을 기록하며 다채로운 일상을 만들어가고 있어요.");

        private final String labelName;
        private final String caption;

        LifestyleLabel(String labelName, String caption) {
            this.labelName = labelName;
            this.caption = caption;
        }

        public String getLabelName() {
            return labelName;
        }

        public String getCaption() {
            return caption;
        }
    }

    /**
     * 우선순위: 규칙적인 > 꾸준한 > (꼼꼼한 / 균형적인, 둘 다 해당하면 꼼꼼한 우선) > 집중하는 > 다채로운(기본값)
     */
    private LifestyleLabel computeLifestyleLabel(
            List<ButtonRecord> thisMonthRecords, Map<Long, Button> buttonMap, List<Map.Entry<Long, Long>> top5ButtonEntries
    ) {
        if (thisMonthRecords.isEmpty()) {
            return LifestyleLabel.DIVERSE;
        }

        if (isRegular(thisMonthRecords, top5ButtonEntries)) {
            return LifestyleLabel.REGULAR;
        }
        if (isConsistent(thisMonthRecords)) {
            return LifestyleLabel.CONSISTENT;
        }

        LifestyleLabel balancedOrMeticulous = resolveBalancedOrMeticulous(thisMonthRecords, buttonMap, top5ButtonEntries);
        if (balancedOrMeticulous != null) {
            return balancedOrMeticulous;
        }

        if (isFocused(thisMonthRecords, buttonMap)) {
            return LifestyleLabel.FOCUSED;
        }
        return LifestyleLabel.DIVERSE;
    }

    /**
     * 기획서 부가설명 3줄을 정리한 규칙:
     * - 카테고리가 아예 없으면 → 균형은 후보가 될 수 없으니, 꼼꼼 후보 조건만 본다.
     * - 카테고리가 있으면 → 기본은 균형이 꼼꼼보다 우선인데,
     *   단 하루 평균 사용량이 "압도적으로" 높으면(기획서 예시: 20회 이상) 그때는 꼼꼼이 뒤집는다.
     * 둘 다 후보 조건을 만족 못 하면 null을 반환해서, 다음 순위(집중하는 기억)로 넘어가게 한다.
     */
    private LifestyleLabel resolveBalancedOrMeticulous(
            List<ButtonRecord> records, Map<Long, Button> buttonMap, List<Map.Entry<Long, Long>> top5ButtonEntries
    ) {
        boolean meticulousCandidate = isMeticulous(records, top5ButtonEntries);
        boolean hasCategory = records.stream()
                .anyMatch(r -> buttonMap.get(r.getButtonId()) != null && buttonMap.get(r.getButtonId()).getCategoryId() != null);

        if (!hasCategory) {
            return meticulousCandidate ? LifestyleLabel.METICULOUS : null;
        }

        boolean balancedCandidate = isBalanced(records, buttonMap);
        if (balancedCandidate) {
            double dailyAvg = (double) records.size() / LocalDate.now().getDayOfMonth();
            if (dailyAvg >= MOM_OVERRIDE_DAILY_AVG_THRESHOLD) {
                return LifestyleLabel.METICULOUS; // 사용량이 압도적으로 높으면 균형을 뒤집는다
            }
            return LifestyleLabel.BALANCED;
        }

        return meticulousCandidate ? LifestyleLabel.METICULOUS : null;
    }

    /**
     * top5 버튼 중 하나라도, 아래 둘 중 하나만 만족하면 "규칙적"이라고 판단한다 (기획 확정: 두 기준 다 반영).
     * (A) 특정 시간대(새벽/아침/점심/저녁/밤)에 70% 이상 몰려있다
     * (B) 탭 간격이 일정하다 (간격들의 변동계수가 30% 이하)
     */
    private boolean isRegular(List<ButtonRecord> records, List<Map.Entry<Long, Long>> top5ButtonEntries) {
        for (Map.Entry<Long, Long> entry : top5ButtonEntries) {
            List<ButtonRecord> buttonRecords = records.stream()
                    .filter(r -> r.getButtonId().equals(entry.getKey()))
                    .sorted(Comparator.comparing(ButtonRecord::getRecordedAt))
                    .toList();

            if (buttonRecords.isEmpty()) {
                continue;
            }

            if (hasHighTimeSlotConcentration(buttonRecords) || hasConsistentInterval(buttonRecords)) {
                return true;
            }
        }
        return false;
    }

    /** (A) 특정 시간대 집중도 70% 이상 */
    private boolean hasHighTimeSlotConcentration(List<ButtonRecord> buttonRecords) {
        Map<String, Long> slotCounts = buttonRecords.stream()
                .collect(Collectors.groupingBy(r -> TimeSlotResolver.resolve(r.getRecordedAt().toLocalTime()), Collectors.counting()));
        long maxSlotCount = slotCounts.values().stream().max(Long::compareTo).orElse(0L);
        double concentration = (double) maxSlotCount / buttonRecords.size();
        return concentration >= REGULAR_TIME_SLOT_CONCENTRATION_THRESHOLD;
    }

    /** (B) 탭 간격의 변동계수(표준편차/평균) 30% 이하 */
    private boolean hasConsistentInterval(List<ButtonRecord> buttonRecords) {
        if (buttonRecords.size() < REGULAR_MIN_TAP_COUNT) {
            return false; // 간격을 판단하기엔 데이터가 너무 적음
        }

        List<Double> intervalHours = new ArrayList<>();
        for (int i = 1; i < buttonRecords.size(); i++) {
            LocalDateTime prev = buttonRecords.get(i - 1).getRecordedAt();
            LocalDateTime curr = buttonRecords.get(i).getRecordedAt();
            intervalHours.add(Duration.between(prev, curr).toMinutes() / 60.0);
        }

        double mean = intervalHours.stream().mapToDouble(d -> d).average().orElse(0.0);
        if (mean == 0.0) {
            return false; // 같은 시각에 여러 번 찍힌 경우 등 예외적인 데이터는 제외
        }

        double variance = intervalHours.stream()
                .mapToDouble(d -> Math.pow(d - mean, 2))
                .average().orElse(0.0);
        double coefficientOfVariation = Math.sqrt(variance) / mean;

        return coefficientOfVariation <= REGULAR_INTERVAL_CV_THRESHOLD;
    }

    /** 최근 30일 중 20일 이상 기록했거나, 7일 이상 연속 기록했으면 "꾸준함" */
    private boolean isConsistent(List<ButtonRecord> records) {
        LocalDate windowStart = LocalDate.now().minusDays(29);
        Set<LocalDate> recordDates = records.stream()
                .map(r -> r.getRecordedAt().toLocalDate())
                .filter(d -> !d.isBefore(windowStart))
                .collect(Collectors.toSet());

        if (recordDates.size() >= CONSISTENT_DAYS_THRESHOLD) {
            return true;
        }
        return hasConsecutiveStreak(recordDates, CONSISTENT_STREAK_THRESHOLD);
    }

    private boolean hasConsecutiveStreak(Set<LocalDate> dates, int streakLength) {
        int streak = 0;
        LocalDate cursor = LocalDate.now();
        for (int i = 0; i < 30; i++) {
            if (dates.contains(cursor)) {
                streak++;
                if (streak >= streakLength) {
                    return true;
                }
            } else {
                streak = 0;
            }
            cursor = cursor.minusDays(1);
        }
        return false;
    }

    /** 일평균 기록량이 많고(5회 이상), top5 버튼 비중이 낮으면(70% 미만 = 다양한 버튼을 고르게 사용) "꼼꼼함" */
    private boolean isMeticulous(List<ButtonRecord> records, List<Map.Entry<Long, Long>> top5ButtonEntries) {
        double dailyAvg = (double) records.size() / LocalDate.now().getDayOfMonth();
        if (dailyAvg < MOM_DAILY_AVG_THRESHOLD) {
            return false;
        }
        long top5Total = top5ButtonEntries.stream().mapToLong(Map.Entry::getValue).sum();
        double top5Share = (double) top5Total / records.size();
        return top5Share < MOM_TOP5_SHARE_THRESHOLD;
    }

    /** 카테고리가 있고, 1위 카테고리 비중이 50% 미만이면(고르게 분포) "균형적" */
    private boolean isBalanced(List<ButtonRecord> records, Map<Long, Button> buttonMap) {
        Map<Long, Long> categoryCounts = records.stream()
                .filter(r -> buttonMap.get(r.getButtonId()) != null && buttonMap.get(r.getButtonId()).getCategoryId() != null)
                .collect(Collectors.groupingBy(r -> buttonMap.get(r.getButtonId()).getCategoryId(), Collectors.counting()));

        if (categoryCounts.isEmpty()) {
            return false;
        }
        long totalCategorized = categoryCounts.values().stream().mapToLong(Long::longValue).sum();
        long maxCategoryCount = categoryCounts.values().stream().max(Long::compareTo).orElse(0L);
        double maxCategoryRatio = (double) maxCategoryCount / totalCategorized;
        return maxCategoryRatio < BALANCED_MAX_CATEGORY_RATIO_THRESHOLD;
    }

    /** top1 버튼 비중 40% 이상, 또는 top1 카테고리 비중 60% 이상이면 "집중" */
    private boolean isFocused(List<ButtonRecord> records, Map<Long, Button> buttonMap) {
        Map<Long, Long> buttonCounts = records.stream()
                .collect(Collectors.groupingBy(ButtonRecord::getButtonId, Collectors.counting()));
        long maxButtonCount = buttonCounts.values().stream().max(Long::compareTo).orElse(0L);
        double topButtonRatio = (double) maxButtonCount / records.size();
        if (topButtonRatio >= FOCUSED_TOP_BUTTON_RATIO_THRESHOLD) {
            return true;
        }

        Map<Long, Long> categoryCounts = records.stream()
                .filter(r -> buttonMap.get(r.getButtonId()) != null && buttonMap.get(r.getButtonId()).getCategoryId() != null)
                .collect(Collectors.groupingBy(r -> buttonMap.get(r.getButtonId()).getCategoryId(), Collectors.counting()));
        if (categoryCounts.isEmpty()) {
            return false;
        }
        long maxCategoryCount = categoryCounts.values().stream().max(Long::compareTo).orElse(0L);
        double topCategoryRatio = (double) maxCategoryCount / records.size();
        return topCategoryRatio >= FOCUSED_TOP_CATEGORY_RATIO_THRESHOLD;
    }

    private List<Map.Entry<Long, Long>> topButtonEntries(List<ButtonRecord> records, int limit) {
        Map<Long, Long> buttonCounts = records.stream()
                .collect(Collectors.groupingBy(ButtonRecord::getButtonId, Collectors.counting()));
        return buttonCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .toList();
    }

    // ================= 추천 생성 =================

    /**
     * 이번 달 top5 버튼 정보를 AI에게 알려주고, 새 버튼 이름을 최대 5개 추천받는다.
     * AI 호출이 실패하거나 응답이 이상하면, 그냥 빈 리스트를 반환한다 (전체를 막지 않음).
     */
    private List<LifestyleRecommendation> generateAddRecommendations(
            Long userId, List<ButtonRecord> thisMonthRecords, Map<Long, Button> buttonMap, List<Map.Entry<Long, Long>> top5ButtonEntries
    ) {
        if (thisMonthRecords.isEmpty() || top5ButtonEntries.isEmpty()) {
            return List.of();
        }

        List<String> top5Descriptions = top5ButtonEntries.stream()
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
     * 마지막 사용(기록)으로부터 1달이 지난 활성 버튼들 중, 가장 오래된 것부터 상위 3개만 삭제 제안으로 만든다.
     * 기록이 아예 없는 버튼은, 생성된 지 1달이 넘었을 때만 대상으로 삼는다(막 만든 새 버튼 오탐 방지).
     */
    // (참고: DELETE 추천 생성은 이제 refreshDeleteRecommendations()에서 매번 라이브로 처리한다.
    //  AI가 필요 없는 순수 쿼리라 캐시할 이유가 없어서, ADD와 다르게 매번 재계산한다.)

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
                + ". 이 사용자에게 새로 추천하면 좋을 습관 버튼 이름을 한국어로 최대 "
                + ADD_RECOMMENDATION_COUNT + "개 알려줘. "
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
                .limit(ADD_RECOMMENDATION_COUNT)
                .toList();
    }

    private String pickRandom(List<String> candidates) {
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }
}