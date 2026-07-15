package com.taptap.backend.record.service;

import com.taptap.backend.button.entity.Button;
import com.taptap.backend.button.exception.ButtonException;
import com.taptap.backend.button.repository.ButtonRepository;
import com.taptap.backend.record.dto.RecordCreateResponseDto;
import com.taptap.backend.record.dto.RecordLatestResponseDto;
import com.taptap.backend.record.entity.ButtonRecord;
import com.taptap.backend.record.repository.ButtonRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

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
     * - 기록 자체가 없거나(이미 소프트 삭제 포함), button_id가 안 맞으면 404
     * - 기록은 있지만 내 버튼이 아니면 403
     * - 생성 후 3초가 지났으면 409
     * - 취소는 "없었던 일로" 되돌리는 개념이라 소프트 삭제가 아니라 완전 삭제한다.
     */
    @Transactional
    public void cancelRecord(Long userId, Long buttonId, Long recordId) {
        ButtonRecord record = buttonRecordRepository.findById(recordId)
                .filter(r -> r.getDeletedAt() == null)
                .filter(r -> r.getButtonId().equals(buttonId))
                .orElseThrow(() -> new ButtonException(HttpStatus.NOT_FOUND, "존재하지 않는 기록입니다."));

        Button button = buttonRepository.findById(record.getButtonId())
                .orElseThrow(() -> new ButtonException(HttpStatus.NOT_FOUND, "존재하지 않는 기록입니다."));

        if (!button.getUserId().equals(userId)) {
            throw new ButtonException(HttpStatus.FORBIDDEN, "본인 기록이 아닙니다.");
        }

        LocalDateTime cancelDeadline = record.getCreatedAt().plusSeconds(3);
        if (LocalDateTime.now().isAfter(cancelDeadline)) {
            throw new ButtonException(HttpStatus.CONFLICT, "취소 가능 시간(3초)이 지났습니다.");
        }

        buttonRecordRepository.delete(record);
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
}