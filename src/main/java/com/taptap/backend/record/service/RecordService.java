package com.taptap.backend.record.service;

import com.taptap.backend.button.entity.Button;
import com.taptap.backend.button.exception.ButtonException;
import com.taptap.backend.button.repository.ButtonRepository;
import com.taptap.backend.record.dto.RecordCreateResponseDto;
import com.taptap.backend.record.dto.RecordDetailResponseDto;
import com.taptap.backend.record.dto.RecordDetailUpdateRequestDto;
import com.taptap.backend.record.dto.RecordLatestResponseDto;
import com.taptap.backend.record.dto.RecordRecentResponseDto;
import com.taptap.backend.record.dto.RecordSummaryResponseDto;
import com.taptap.backend.record.dto.RecordTimelineItemDto;
import com.taptap.backend.record.dto.RecordTimelineResponseDto;
import com.taptap.backend.record.entity.ButtonRecord;
import com.taptap.backend.record.repository.ButtonRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecordService {

    private final ButtonRepository buttonRepository;
    private final ButtonRecordRepository buttonRecordRepository;

    /**
     * 4.2 행동 기록 (탭)
     * - 버튼이 없거나 "내 버튼"이 아니면 404로 통일한다 (명세서 에러 케이스에 403이 없음 -
     *   존재 여부를 노출하지 않기 위해 권한 없음도 404로 응답).
     * - 버튼이 존재하지만 비활성 상태(is_active=false)면 400.
     */
    @Transactional
    public RecordCreateResponseDto createRecord(Long userId, Long buttonId) {
        Button button = findOwnedButton(userId, buttonId);

        if (!Boolean.TRUE.equals(button.getIsActive())) {
            throw new ButtonException(HttpStatus.BAD_REQUEST, "비활성화된 버튼입니다.");
        }

        ButtonRecord record = new ButtonRecord();
        record.setButtonId(buttonId);
        // recordedAt은 ButtonRecord의 @PrePersist가 null일 때 자동으로 채워준다.
        ButtonRecord saved = buttonRecordRepository.save(record);

        return RecordCreateResponseDto.builder()
                .recordId(saved.getRecordId())
                .buttonId(saved.getButtonId())
                .recordedAt(saved.getRecordedAt())
                .build();
    }

    private Button findOwnedButton(Long userId, Long buttonId) {
        return buttonRepository.findById(buttonId)
                .filter(b -> b.getUserId().equals(userId))
                .orElseThrow(() -> new ButtonException(HttpStatus.NOT_FOUND, "존재하지 않는 버튼입니다."));
    }

    /**
     * 4.2-2 기록 취소 (팝업 3초 이내)
     * - 생성 후 3초가 지났으면 409
     * - 취소는 "없었던 일로" 되돌리는 개념이라 소프트 삭제가 아니라 완전 삭제한다.
     */
    @Transactional
    public void cancelRecord(Long userId, Long buttonId, Long recordId) {
        ButtonRecord record = findOwnedRecord(userId, buttonId, recordId);

        LocalDateTime cancelDeadline = record.getCreatedAt().plusSeconds(3);
        if (LocalDateTime.now().isAfter(cancelDeadline)) {
            throw new ButtonException(HttpStatus.CONFLICT, "취소 가능 시간(3초)이 지났습니다.");
        }

        buttonRecordRepository.delete(record);
    }

    /**
     * 기록 1건을 찾고, 존재/소유 여부를 검증하는 공용 헬퍼.
     * - 기록 자체가 없거나(이미 소프트 삭제 포함), button_id가 안 맞으면 404
     * - 기록은 있지만 내 버튼이 아니면 403
     * (기록 취소, 타임라인 상세 추가, 타임라인 삭제 등에서 공통으로 재사용한다.)
     */
    private ButtonRecord findOwnedRecord(Long userId, Long buttonId, Long recordId) {
        ButtonRecord record = buttonRecordRepository.findById(recordId)
                .filter(r -> r.getDeletedAt() == null)
                .filter(r -> r.getButtonId().equals(buttonId))
                .orElseThrow(() -> new ButtonException(HttpStatus.NOT_FOUND, "존재하지 않는 기록입니다."));

        Button button = buttonRepository.findById(record.getButtonId())
                .orElseThrow(() -> new ButtonException(HttpStatus.NOT_FOUND, "존재하지 않는 기록입니다."));

        if (!button.getUserId().equals(userId)) {
            throw new ButtonException(HttpStatus.FORBIDDEN, "본인 기록이 아닙니다.");
        }

        return record;
    }

    /**
     * 4.7 마지막 기록 시간 조회
     * - 버튼이 없거나(내 버튼이 아니거나) 기록이 하나도 없으면 404로 통일한다 (명세서 기준).
     */
    public RecordLatestResponseDto getLatestRecord(Long userId, Long buttonId) {
        Button button = findOwnedButton(userId, buttonId);

        ButtonRecord latest = buttonRecordRepository
                .findTopByButtonIdAndDeletedAtIsNullOrderByRecordedAtDesc(button.getButtonId())
                .orElseThrow(() -> new ButtonException(HttpStatus.NOT_FOUND, "기록이 없습니다."));

        long elapsedSeconds = Duration.between(latest.getRecordedAt(), LocalDateTime.now()).getSeconds();

        return RecordLatestResponseDto.builder()
                .buttonId(button.getButtonId())
                .lastRecordedAt(latest.getRecordedAt())
                .elapsedSeconds(elapsedSeconds)
                .build();
    }

    /**
     * 5.1 최근 기록 조회 (홈)
     * - 특정 버튼이 아니라, 내가 가진 활성 버튼 전체 중 가장 최근 기록을 찾는다.
     * - 활성 버튼이 하나도 없거나, 기록이 하나도 없으면 404.
     */
    public RecordRecentResponseDto getRecentRecord(Long userId) {
        List<Long> buttonIds = buttonRepository.findActiveButtonIdsByUserId(userId);
        if (buttonIds.isEmpty()) {
            throw new ButtonException(HttpStatus.NOT_FOUND, "기록이 없습니다.");
        }

        ButtonRecord latest = buttonRecordRepository
                .findTopByButtonIdInAndDeletedAtIsNullOrderByRecordedAtDesc(buttonIds)
                .orElseThrow(() -> new ButtonException(HttpStatus.NOT_FOUND, "기록이 없습니다."));

        Button button = buttonRepository.findById(latest.getButtonId())
                .orElseThrow(() -> new ButtonException(HttpStatus.NOT_FOUND, "기록이 없습니다."));

        return RecordRecentResponseDto.builder()
                .buttonId(button.getButtonId())
                .buttonName(button.getButtonName())
                .iconName(button.getIconName())
                .iconColor(button.getIconColor())
                .lastRecordedAt(latest.getRecordedAt())
                .build();
    }

    /**
     * 5.2 최근 기록 조회 (버튼 상세)
     * - 버튼이 없으면(또는 내 버튼이 아니면) 404.
     * - 3번 API(마지막 기록 시간 조회)와 달리, 기록이 0건이어도 에러가 아니라
     *   lastRecordedAt=null, count=0으로 정상 응답한다 (버튼 상세 화면은 항상 떠야 하니까).
     */
    public RecordSummaryResponseDto getButtonSummary(Long userId, Long buttonId) {
        Button button = findOwnedButton(userId, buttonId);

        LocalDateTime lastRecordedAt = buttonRecordRepository
                .findTopByButtonIdAndDeletedAtIsNullOrderByRecordedAtDesc(button.getButtonId())
                .map(ButtonRecord::getRecordedAt)
                .orElse(null);

        LocalDate today = LocalDate.now();
        long todayCount = buttonRecordRepository.countTodayByButtonId(
                button.getButtonId(),
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay()
        );

        long totalCount = buttonRecordRepository.countByButtonIdAndDeletedAtIsNull(button.getButtonId());

        return RecordSummaryResponseDto.builder()
                .buttonId(button.getButtonId())
                .lastRecordedAt(lastRecordedAt)
                .todayCount(todayCount)
                .totalCount(totalCount)
                .build();
    }

    /**
     * 5.3 타임라인 조회
     * - recordId 기준 커서 페이지네이션, 최신순(recordId 내림차순).
     * - limit보다 1개 더 가져와서, 넘치면 "더 있다(hasMore=true)"로 판단하는 방식.
     * - limit이 없거나 범위를 벗어나면 기본값 30으로 보정한다.
     */
    public RecordTimelineResponseDto getTimeline(Long userId, Long buttonId, Long cursor, Integer limit) {
        Button button = findOwnedButton(userId, buttonId);

        int pageSize = (limit == null || limit <= 0 || limit > 30) ? 30 : limit;
        Pageable pageable = PageRequest.of(0, pageSize + 1);

        List<ButtonRecord> fetched = buttonRecordRepository.findTimeline(button.getButtonId(), cursor, pageable);

        boolean hasMore = fetched.size() > pageSize;
        List<ButtonRecord> pageItems = hasMore ? fetched.subList(0, pageSize) : fetched;

        List<RecordTimelineItemDto> items = pageItems.stream()
                .map(r -> RecordTimelineItemDto.builder()
                        .recordId(r.getRecordId())
                        .recordedAt(r.getRecordedAt())
                        .memo(r.getMemo())
                        .emoji(r.getEmoji())
                        .build())
                .toList();

        Long nextCursor = (hasMore && !pageItems.isEmpty())
                ? pageItems.get(pageItems.size() - 1).getRecordId()
                : null;

        return RecordTimelineResponseDto.builder()
                .records(items)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }

    /**
     * 5.4 타임라인 상세 기록 추가 (메모·이모지)
     * - request.getMemo()/getEmoji()가 자바에서 null이면 "JSON에 그 키 자체가 없었다"는 뜻이라
     *   건드리지 않는다. Optional.empty()면 명시적으로 null이 와서 삭제, Optional.of(값)이면 수정.
     * - memo, emoji 둘 다 키 자체가 없으면(둘 다 null) 400.
     */
    @Transactional
    public RecordDetailResponseDto updateDetail(
            Long userId, Long buttonId, Long recordId, RecordDetailUpdateRequestDto request
    ) {
        if (request.getMemo() == null && request.getEmoji() == null) {
            throw new ButtonException(HttpStatus.BAD_REQUEST, "memo, emoji 중 하나는 포함되어야 합니다.");
        }

        ButtonRecord record = findOwnedRecord(userId, buttonId, recordId);

        if (request.getMemo() != null) {
            record.setMemo(request.getMemo().orElse(null));
        }
        if (request.getEmoji() != null) {
            record.setEmoji(request.getEmoji().orElse(null));
        }

        return RecordDetailResponseDto.builder()
                .recordId(record.getRecordId())
                .memo(record.getMemo())
                .emoji(record.getEmoji())
                .recordedAt(record.getRecordedAt())
                .build();
    }
}