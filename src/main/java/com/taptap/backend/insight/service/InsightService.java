package com.taptap.backend.insight.service;

import com.taptap.backend.button.entity.Button;
import com.taptap.backend.button.entity.ButtonCategory;
import com.taptap.backend.button.exception.ButtonException;
import com.taptap.backend.button.repository.ButtonCategoryRepository;
import com.taptap.backend.button.repository.ButtonRepository;
import com.taptap.backend.insight.dto.*;
import com.taptap.backend.record.entity.ButtonRecord;
import com.taptap.backend.record.repository.ButtonRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ⚠️ 지금은 캐시 없이 매번 직접 집계하는 버전이다.
 *    로직이 정확히 검증되면, 다음 단계에서 insight_daily_cache 등을 붙여
 *    hit/miss 캐시 계층을 추가할 예정.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InsightService {

    private final ButtonRepository buttonRepository;
    private final ButtonCategoryRepository buttonCategoryRepository;
    private final ButtonRecordRepository buttonRecordRepository;

    /**
     * 9. 데일리 인사이트 조회
     */
    public InsightDailyResponseDto getDailyInsight(Long userId, LocalDate targetDate) {
        LocalDate date = (targetDate != null) ? targetDate : LocalDate.now();

        if (date.isAfter(LocalDate.now())) {
            throw new ButtonException(HttpStatus.BAD_REQUEST, "미래 날짜는 조회할 수 없습니다.");
        }

        List<Long> buttonIds = buttonRepository.findActiveButtonIdsByUserId(userId);

        if (buttonIds.isEmpty()) {
            return emptyDailyResponse(date);
        }

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<ButtonRecord> records = buttonRecordRepository.findRecordsInRange(buttonIds, start, end);

        if (records.isEmpty()) {
            return emptyDailyResponse(date);
        }

        Map<Long, Button> buttonMap = buttonRepository.findAllById(buttonIds).stream()
                .collect(Collectors.toMap(Button::getButtonId, b -> b));

        // 버튼별 탭 횟수
        Map<Long, Long> buttonCounts = records.stream()
                .collect(Collectors.groupingBy(ButtonRecord::getButtonId, Collectors.counting()));

        // 1위 버튼
        Map.Entry<Long, Long> topEntry = buttonCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow();
        Button topBtn = buttonMap.get(topEntry.getKey());
        TopButtonDto topButton = TopButtonDto.builder()
                .buttonId(topBtn.getButtonId())
                .buttonName(topBtn.getButtonName())
                .iconName(topBtn.getIconName())
                .iconColor(topBtn.getIconColor())
                .count(topEntry.getValue().intValue())
                .build();

        // 피크 시간대
        Map<String, Long> slotCounts = records.stream()
                .collect(Collectors.groupingBy(r -> resolveTimeSlot(r.getRecordedAt().toLocalTime()), Collectors.counting()));
        String peakTimeSlot = slotCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // 카테고리별 탭 횟수 (카테고리 없는 버튼은 제외)
        Map<Long, Long> categoryCounts = records.stream()
                .filter(r -> buttonMap.get(r.getButtonId()) != null && buttonMap.get(r.getButtonId()).getCategoryId() != null)
                .collect(Collectors.groupingBy(r -> buttonMap.get(r.getButtonId()).getCategoryId(), Collectors.counting()));

        List<CategoryTapCountDto> categoryTapCounts = buildCategoryTapCounts(categoryCounts);

        // 버튼별 탭 횟수 (내림차순 + 비율)
        long maxCount = buttonCounts.values().stream().max(Long::compareTo).orElse(0L);
        List<ButtonTapCountDto> buttonTapCounts = buttonCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(e -> {
                    Button btn = buttonMap.get(e.getKey());
                    double ratio = (maxCount == 0) ? 0.0 : Math.round((double) e.getValue() / maxCount * 1000) / 1000.0;
                    return ButtonTapCountDto.builder()
                            .buttonId(btn.getButtonId())
                            .buttonName(btn.getButtonName())
                            .count(e.getValue().intValue())
                            .ratio(ratio)
                            .build();
                })
                .toList();

        // 타임라인 (최신순, findRecordsInRange가 이미 정렬해서 줌)
        List<InsightTimelineItemDto> timeline = records.stream()
                .map(r -> {
                    Button btn = buttonMap.get(r.getButtonId());
                    return InsightTimelineItemDto.builder()
                            .recordId(r.getRecordId())
                            .buttonId(r.getButtonId())
                            .buttonName(btn != null ? btn.getButtonName() : null)
                            .recordedAt(r.getRecordedAt())
                            .memo(r.getMemo())
                            .emoji(r.getEmoji())
                            .build();
                })
                .toList();

        return InsightDailyResponseDto.builder()
                .targetDate(date.toString())
                .totalTapCount(records.size())
                .topButton(topButton)
                .peakTimeSlot(peakTimeSlot)
                .categoryTapCounts(categoryTapCounts)
                .buttonTapCounts(buttonTapCounts)
                .timeline(timeline)
                .build();
    }

    /**
     * ratio는 "카테고리가 매겨진 기록들의 합" 대비 비율이다 (전체 totalTapCount 대비가 아님).
     * 카테고리 없는 버튼의 기록은 애초에 categoryCounts에 안 들어오니, 이렇게 계산해야
     * 프론트에서 ratio들을 다 더했을 때 정확히 1.0(100%)이 된다.
     */
    private List<CategoryTapCountDto> buildCategoryTapCounts(Map<Long, Long> categoryCounts) {
        if (categoryCounts.isEmpty()) {
            return List.of();
        }

        long totalCategorized = categoryCounts.values().stream().mapToLong(Long::longValue).sum();

        Map<Long, ButtonCategory> categoryMap = buttonCategoryRepository.findAllById(categoryCounts.keySet()).stream()
                .collect(Collectors.toMap(ButtonCategory::getCategoryId, c -> c));

        List<CategoryTapCountDto> result = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : categoryCounts.entrySet()) {
            ButtonCategory category = categoryMap.get(entry.getKey());
            if (category == null) {
                continue;
            }
            double ratio = (totalCategorized == 0) ? 0.0
                    : Math.round((double) entry.getValue() / totalCategorized * 1000) / 1000.0;
            result.add(CategoryTapCountDto.builder()
                    .categoryId(category.getCategoryId())
                    .categoryName(category.getCategoryName())
                    .count(entry.getValue().intValue())
                    .ratio(ratio)
                    .build());
        }
        result.sort((a, b) -> b.getCount() - a.getCount());
        return result;
    }

    /**
     * 새벽 00:00~05:59 / 아침 06:00~09:59 / 점심 10:00~13:59 / 저녁 14:00~18:59 / 밤 19:00~23:59
     * (명세서에 정확한 시간 기준이 없어서 팀 협의로 임시로 정한 기준. 나중에 조정 가능.)
     */
    private String resolveTimeSlot(LocalTime time) {
        if (time.isBefore(LocalTime.of(6, 0))) return "새벽";
        if (time.isBefore(LocalTime.of(10, 0))) return "아침";
        if (time.isBefore(LocalTime.of(14, 0))) return "점심";
        if (time.isBefore(LocalTime.of(19, 0))) return "저녁";
        return "밤";
    }

    private InsightDailyResponseDto emptyDailyResponse(LocalDate date) {
        return InsightDailyResponseDto.builder()
                .targetDate(date.toString())
                .totalTapCount(0)
                .topButton(null)
                .peakTimeSlot(null)
                .categoryTapCounts(List.of())
                .buttonTapCounts(List.of())
                .timeline(List.of())
                .build();
    }

    /**
     * 10. 위클리 인사이트 조회
     * - weekStart가 없으면 이번 주 월요일을 기본값으로 사용.
     * - 요일 7개는 기록 유무와 무관하게 항상 다 채워서 반환한다(그래프가 7일치를 항상 보여줘야 하니까).
     * - buttonTapCounts(그래프용)는 상위 5개만, topButton(요약카드용)은 5개 제한 없이 진짜 1위.
     */
    public InsightWeeklyResponseDto getWeeklyInsight(Long userId, LocalDate weekStart) {
        LocalDate start = (weekStart != null)
                ? weekStart
                : LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = start.plusDays(6);

        List<Long> buttonIds = buttonRepository.findActiveButtonIdsByUserId(userId);

        List<ButtonRecord> records = buttonIds.isEmpty()
                ? List.of()
                : buttonRecordRepository.findRecordsInRange(buttonIds, start.atStartOfDay(), start.plusDays(7).atStartOfDay());

        Map<Long, Button> buttonMap = buttonIds.isEmpty()
                ? Map.of()
                : buttonRepository.findAllById(buttonIds).stream().collect(Collectors.toMap(Button::getButtonId, b -> b));

        List<DailyTapCountDto> dailyTapCounts = buildDailyTapCounts(records, buttonMap, start);

        if (records.isEmpty()) {
            PrevWeekComparisonDto prevWeekComparison = buildPrevWeekComparison(buttonIds, start, 0);
            return InsightWeeklyResponseDto.builder()
                    .weekStart(start.toString())
                    .weekEnd(end.toString())
                    .totalTapCount(0)
                    .dailyTapCounts(dailyTapCounts)
                    .categoryTapCounts(List.of())
                    .buttonTapCounts(List.of())
                    .peakDay(null)
                    .peakTimeSlot(null)
                    .topButton(null)
                    .prevWeekComparison(prevWeekComparison)
                    .build();
        }

        Map<Long, Long> categoryCounts = records.stream()
                .filter(r -> buttonMap.get(r.getButtonId()) != null && buttonMap.get(r.getButtonId()).getCategoryId() != null)
                .collect(Collectors.groupingBy(r -> buttonMap.get(r.getButtonId()).getCategoryId(), Collectors.counting()));
        List<CategoryTapCountDto> categoryTapCounts = buildCategoryTapCounts(categoryCounts);

        Map<Long, Long> buttonCounts = records.stream()
                .collect(Collectors.groupingBy(ButtonRecord::getButtonId, Collectors.counting()));
        long maxButtonCount = buttonCounts.values().stream().max(Long::compareTo).orElse(0L);

        List<ButtonTapCountDto> buttonTapCounts = buttonCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(e -> {
                    Button btn = buttonMap.get(e.getKey());
                    double ratio = (maxButtonCount == 0) ? 0.0 : Math.round((double) e.getValue() / maxButtonCount * 1000) / 1000.0;
                    return ButtonTapCountDto.builder()
                            .buttonId(btn.getButtonId())
                            .buttonName(btn.getButtonName())
                            .count(e.getValue().intValue())
                            .ratio(ratio)
                            .build();
                })
                .toList();

        String peakDay = dailyTapCounts.stream()
                .max(Comparator.comparingInt(DailyTapCountDto::getTotal))
                .filter(d -> d.getTotal() > 0)
                .map(d -> toKoreanDayName(LocalDate.parse(d.getDate()).getDayOfWeek()))
                .orElse(null);

        Map<String, Long> slotCounts = records.stream()
                .collect(Collectors.groupingBy(r -> resolveTimeSlot(r.getRecordedAt().toLocalTime()), Collectors.counting()));
        String peakTimeSlot = slotCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        Map.Entry<Long, Long> topEntry = buttonCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow();
        Button topBtn = buttonMap.get(topEntry.getKey());
        WeeklyTopButtonDto topButton = WeeklyTopButtonDto.builder()
                .buttonId(topBtn.getButtonId())
                .buttonName(topBtn.getButtonName())
                .count(topEntry.getValue().intValue())
                .build();

        PrevWeekComparisonDto prevWeekComparison = buildPrevWeekComparison(buttonIds, start, records.size());

        return InsightWeeklyResponseDto.builder()
                .weekStart(start.toString())
                .weekEnd(end.toString())
                .totalTapCount(records.size())
                .dailyTapCounts(dailyTapCounts)
                .categoryTapCounts(categoryTapCounts)
                .buttonTapCounts(buttonTapCounts)
                .peakDay(peakDay)
                .peakTimeSlot(peakTimeSlot)
                .topButton(topButton)
                .prevWeekComparison(prevWeekComparison)
                .build();
    }

    private List<DailyTapCountDto> buildDailyTapCounts(List<ButtonRecord> records, Map<Long, Button> buttonMap, LocalDate weekStart) {
        List<DailyTapCountDto> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            List<ButtonRecord> dayRecords = records.stream()
                    .filter(r -> r.getRecordedAt().toLocalDate().equals(day))
                    .toList();

            Map<Long, Integer> categoriesForDay = dayRecords.stream()
                    .filter(r -> buttonMap.get(r.getButtonId()) != null && buttonMap.get(r.getButtonId()).getCategoryId() != null)
                    .collect(Collectors.groupingBy(
                            r -> buttonMap.get(r.getButtonId()).getCategoryId(),
                            Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                    ));

            result.add(DailyTapCountDto.builder()
                    .date(day.toString())
                    .total(dayRecords.size())
                    .categories(categoriesForDay)
                    .build());
        }
        return result;
    }

    private PrevWeekComparisonDto buildPrevWeekComparison(List<Long> buttonIds, LocalDate currentWeekStart, int currentTotal) {
        LocalDate prevStart = currentWeekStart.minusWeeks(1);

        List<ButtonRecord> prevRecords = buttonIds.isEmpty()
                ? List.of()
                : buttonRecordRepository.findRecordsInRange(buttonIds, prevStart.atStartOfDay(), prevStart.plusDays(7).atStartOfDay());

        int prevTotal = prevRecords.size();

        // 지난주 기록이 0이면 변화율을 나눗셈할 수 없으니 0으로 처리한다.
        double changeRate = 0.0;
        if (prevTotal > 0) {
            changeRate = Math.round((double) (currentTotal - prevTotal) / prevTotal * 1000) / 1000.0;
        }

        WeeklyTopButtonDto prevTopButton = null;
        if (!prevRecords.isEmpty()) {
            Map<Long, Button> buttonMap = buttonRepository.findAllById(buttonIds).stream()
                    .collect(Collectors.toMap(Button::getButtonId, b -> b));
            Map<Long, Long> prevButtonCounts = prevRecords.stream()
                    .collect(Collectors.groupingBy(ButtonRecord::getButtonId, Collectors.counting()));
            Map.Entry<Long, Long> prevTopEntry = prevButtonCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElseThrow();
            Button prevTopBtn = buttonMap.get(prevTopEntry.getKey());
            prevTopButton = WeeklyTopButtonDto.builder()
                    .buttonId(prevTopBtn.getButtonId())
                    .buttonName(prevTopBtn.getButtonName())
                    .count(prevTopEntry.getValue().intValue())
                    .build();
        }

        return PrevWeekComparisonDto.builder()
                .prevTotalTapCount(prevTotal)
                .changeRate(changeRate)
                .prevTopButton(prevTopButton)
                .build();
    }

    private String toKoreanDayName(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "월요일";
            case TUESDAY -> "화요일";
            case WEDNESDAY -> "수요일";
            case THURSDAY -> "목요일";
            case FRIDAY -> "금요일";
            case SATURDAY -> "토요일";
            case SUNDAY -> "일요일";
        };
    }

    /**
     * 11. 먼슬리 인사이트 조회 (통계 부분만. 라이프스타일 추천은 /api/lifestyle-recommendations로 분리)
     * - year/month가 없으면 이번 달을 기본값으로 사용.
     * - dailyTapCounts는 그 달의 모든 날짜를 항상 포함한다(기록 없는 날은 0), 히트맵이 매달 같은 형태로 그려져야 하니까.
     * - buttonTapCounts는 위클리와 달리 5개로 안 자르고 전체를 내림차순으로 반환한다(명세서 기준).
     */
    public InsightMonthlyResponseDto getMonthlyInsight(Long userId, Integer year, Integer month) {
        if (month != null && (month < 1 || month > 12)) {
            throw new ButtonException(HttpStatus.BAD_REQUEST, "월(month)은 1~12 사이여야 합니다.");
        }

        YearMonth targetMonth = (year != null && month != null)
                ? YearMonth.of(year, month)
                : YearMonth.now();

        List<Long> buttonIds = buttonRepository.findActiveButtonIdsByUserId(userId);

        List<ButtonRecord> records = buttonIds.isEmpty()
                ? List.of()
                : buttonRecordRepository.findRecordsInRange(
                buttonIds,
                targetMonth.atDay(1).atStartOfDay(),
                targetMonth.plusMonths(1).atDay(1).atStartOfDay()
        );

        Map<Long, Button> buttonMap = buttonIds.isEmpty()
                ? Map.of()
                : buttonRepository.findAllById(buttonIds).stream().collect(Collectors.toMap(Button::getButtonId, b -> b));

        Map<String, Integer> dailyTapCounts = buildMonthlyDailyTapCounts(records, targetMonth);

        if (records.isEmpty()) {
            PrevMonthComparisonDto prevMonthComparison = buildPrevMonthComparison(buttonIds, targetMonth, 0);
            return InsightMonthlyResponseDto.builder()
                    .year(targetMonth.getYear())
                    .month(targetMonth.getMonthValue())
                    .totalTapCount(0)
                    .dailyTapCounts(dailyTapCounts)
                    .categoryTapCounts(List.of())
                    .buttonTapCounts(List.of())
                    .top3Buttons(List.of())
                    .topCategory(null)
                    .busiestDay(null)
                    .weekdayRatio(0.0)
                    .weekendRatio(0.0)
                    .timeSlotCategory(buildTimeSlotCategory(records, buttonMap))
                    .prevMonthComparison(prevMonthComparison)
                    .build();
        }

        // 카테고리별 집계
        Map<Long, Long> categoryCounts = records.stream()
                .filter(r -> buttonMap.get(r.getButtonId()) != null && buttonMap.get(r.getButtonId()).getCategoryId() != null)
                .collect(Collectors.groupingBy(r -> buttonMap.get(r.getButtonId()).getCategoryId(), Collectors.counting()));
        List<CategoryTapCountDto> categoryTapCounts = buildCategoryTapCounts(categoryCounts);
        TopCategoryDto topCategory = categoryTapCounts.isEmpty() ? null
                : TopCategoryDto.builder()
                .categoryId(categoryTapCounts.get(0).getCategoryId())
                .categoryName(categoryTapCounts.get(0).getCategoryName())
                .count(categoryTapCounts.get(0).getCount())
                .build();

        // 버튼별 집계 (전체, 내림차순)
        Map<Long, Long> buttonCounts = records.stream()
                .collect(Collectors.groupingBy(ButtonRecord::getButtonId, Collectors.counting()));
        long maxButtonCount = buttonCounts.values().stream().max(Long::compareTo).orElse(0L);

        List<Map.Entry<Long, Long>> sortedButtonEntries = buttonCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .toList();

        List<ButtonTapCountDto> buttonTapCounts = sortedButtonEntries.stream()
                .map(e -> {
                    Button btn = buttonMap.get(e.getKey());
                    double ratio = (maxButtonCount == 0) ? 0.0 : Math.round((double) e.getValue() / maxButtonCount * 1000) / 1000.0;
                    return ButtonTapCountDto.builder()
                            .buttonId(btn.getButtonId())
                            .buttonName(btn.getButtonName())
                            .count(e.getValue().intValue())
                            .ratio(ratio)
                            .build();
                })
                .toList();

        List<RankedButtonDto> top3Buttons = new ArrayList<>();
        for (int i = 0; i < Math.min(3, sortedButtonEntries.size()); i++) {
            Map.Entry<Long, Long> e = sortedButtonEntries.get(i);
            Button btn = buttonMap.get(e.getKey());
            top3Buttons.add(RankedButtonDto.builder()
                    .rank(i + 1)
                    .buttonId(btn.getButtonId())
                    .buttonName(btn.getButtonName())
                    .count(e.getValue().intValue())
                    .build());
        }

        // 가장 기록 많았던 날
        String busiestDay = dailyTapCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .filter(e -> e.getValue() > 0)
                .map(Map.Entry::getKey)
                .orElse(null);

        // 주중/주말 비율
        long weekdayCount = records.stream()
                .filter(r -> !isWeekend(r.getRecordedAt().toLocalDate()))
                .count();
        long weekendCount = records.size() - weekdayCount;
        double weekdayRatio = Math.round((double) weekdayCount / records.size() * 1000) / 1000.0;
        double weekendRatio = Math.round((double) weekendCount / records.size() * 1000) / 1000.0;

        // 시간대별 최다 카테고리
        Map<String, TimeSlotCategoryEntryDto> timeSlotCategory = buildTimeSlotCategory(records, buttonMap);

        PrevMonthComparisonDto prevMonthComparison = buildPrevMonthComparison(buttonIds, targetMonth, records.size());

        return InsightMonthlyResponseDto.builder()
                .year(targetMonth.getYear())
                .month(targetMonth.getMonthValue())
                .totalTapCount(records.size())
                .dailyTapCounts(dailyTapCounts)
                .categoryTapCounts(categoryTapCounts)
                .buttonTapCounts(buttonTapCounts)
                .top3Buttons(top3Buttons)
                .topCategory(topCategory)
                .busiestDay(busiestDay)
                .weekdayRatio(weekdayRatio)
                .weekendRatio(weekendRatio)
                .timeSlotCategory(timeSlotCategory)
                .prevMonthComparison(prevMonthComparison)
                .build();
    }

    private Map<String, Integer> buildMonthlyDailyTapCounts(List<ButtonRecord> records, YearMonth targetMonth) {
        Map<String, Integer> result = new LinkedHashMap<>();
        int daysInMonth = targetMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            result.put(targetMonth.atDay(day).toString(), 0);
        }
        for (ButtonRecord record : records) {
            String key = record.getRecordedAt().toLocalDate().toString();
            result.merge(key, 1, Integer::sum);
        }
        return result;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    /**
     * 5개 시간대(새벽/아침/점심/저녁/밤)를 항상 다 포함해서 반환한다 (기록 없는 시간대도 count=0, ratio=0.0으로).
     * 각 시간대마다:
     * - count/ratio: 그 시간대에 전체 기록 중 몇 건/몇 %가 있었는지 (피그마의 "9%, 28%..." 그래프용)
     * - categoryId/categoryName: 그 시간대에서 제일 많이 눌린 카테고리 (카테고리 태그된 기록이 하나도 없으면 null)
     */
    private Map<String, TimeSlotCategoryEntryDto> buildTimeSlotCategory(List<ButtonRecord> records, Map<Long, Button> buttonMap) {
        List<String> slotOrder = List.of("새벽", "아침", "점심", "저녁", "밤");

        Map<String, Long> slotTotalCounts = new HashMap<>();
        Map<String, Map<Long, Long>> slotCategoryCounts = new HashMap<>();

        for (ButtonRecord record : records) {
            String slot = resolveTimeSlot(record.getRecordedAt().toLocalTime());
            slotTotalCounts.merge(slot, 1L, Long::sum);

            Button button = buttonMap.get(record.getButtonId());
            if (button != null && button.getCategoryId() != null) {
                slotCategoryCounts
                        .computeIfAbsent(slot, s -> new HashMap<>())
                        .merge(button.getCategoryId(), 1L, Long::sum);
            }
        }

        int totalCount = records.size();

        Map<Long, ButtonCategory> categoryMap = buttonCategoryRepository.findAllById(
                slotCategoryCounts.values().stream()
                        .flatMap(m -> m.keySet().stream())
                        .collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(ButtonCategory::getCategoryId, c -> c));

        Map<String, TimeSlotCategoryEntryDto> result = new LinkedHashMap<>();
        for (String slot : slotOrder) {
            long slotCount = slotTotalCounts.getOrDefault(slot, 0L);
            double ratio = (totalCount == 0) ? 0.0 : Math.round((double) slotCount / totalCount * 1000) / 1000.0;

            Long topCategoryId = null;
            String topCategoryName = null;
            Map<Long, Long> catCounts = slotCategoryCounts.get(slot);
            if (catCounts != null && !catCounts.isEmpty()) {
                Map.Entry<Long, Long> topCategoryEntry = catCounts.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .orElse(null);
                if (topCategoryEntry != null) {
                    ButtonCategory category = categoryMap.get(topCategoryEntry.getKey());
                    if (category != null) {
                        topCategoryId = category.getCategoryId();
                        topCategoryName = category.getCategoryName();
                    }
                }
            }

            result.put(slot, TimeSlotCategoryEntryDto.builder()
                    .count((int) slotCount)
                    .ratio(ratio)
                    .categoryId(topCategoryId)
                    .categoryName(topCategoryName)
                    .build());
        }
        return result;
    }

    private PrevMonthComparisonDto buildPrevMonthComparison(List<Long> buttonIds, YearMonth currentMonth, int currentTotal) {
        YearMonth prevMonth = currentMonth.minusMonths(1);

        List<ButtonRecord> prevRecords = buttonIds.isEmpty()
                ? List.of()
                : buttonRecordRepository.findRecordsInRange(
                buttonIds,
                prevMonth.atDay(1).atStartOfDay(),
                prevMonth.plusMonths(1).atDay(1).atStartOfDay()
        );

        int prevTotal = prevRecords.size();

        double changeRate = 0.0;
        if (prevTotal > 0) {
            changeRate = Math.round((double) (currentTotal - prevTotal) / prevTotal * 1000) / 1000.0;
        }

        List<RankedButtonDto> prevTop5Buttons = List.of();
        if (!prevRecords.isEmpty()) {
            Map<Long, Button> buttonMap = buttonRepository.findAllById(buttonIds).stream()
                    .collect(Collectors.toMap(Button::getButtonId, b -> b));
            Map<Long, Long> prevButtonCounts = prevRecords.stream()
                    .collect(Collectors.groupingBy(ButtonRecord::getButtonId, Collectors.counting()));

            List<Map.Entry<Long, Long>> sortedPrev = prevButtonCounts.entrySet().stream()
                    .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                    .toList();

            List<RankedButtonDto> ranked = new ArrayList<>();
            for (int i = 0; i < Math.min(5, sortedPrev.size()); i++) {
                Map.Entry<Long, Long> e = sortedPrev.get(i);
                Button btn = buttonMap.get(e.getKey());
                ranked.add(RankedButtonDto.builder()
                        .rank(i + 1)
                        .buttonId(btn.getButtonId())
                        .buttonName(btn.getButtonName())
                        .count(e.getValue().intValue())
                        .build());
            }
            prevTop5Buttons = ranked;
        }

        return PrevMonthComparisonDto.builder()
                .prevTotalTapCount(prevTotal)
                .changeRate(changeRate)
                .prevTop5Buttons(prevTop5Buttons)
                .build();
    }
}