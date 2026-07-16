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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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

    private List<CategoryTapCountDto> buildCategoryTapCounts(Map<Long, Long> categoryCounts) {
        if (categoryCounts.isEmpty()) {
            return List.of();
        }

        Map<Long, ButtonCategory> categoryMap = buttonCategoryRepository.findAllById(categoryCounts.keySet()).stream()
                .collect(Collectors.toMap(ButtonCategory::getCategoryId, c -> c));

        List<CategoryTapCountDto> result = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : categoryCounts.entrySet()) {
            ButtonCategory category = categoryMap.get(entry.getKey());
            if (category == null) {
                continue;
            }
            result.add(CategoryTapCountDto.builder()
                    .categoryId(category.getCategoryId())
                    .categoryName(category.getCategoryName())
                    .count(entry.getValue().intValue())
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
}