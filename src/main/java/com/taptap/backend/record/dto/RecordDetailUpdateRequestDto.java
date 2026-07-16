package com.taptap.backend.record.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

/**
 * 5.4 타임라인 상세 기록 추가(메모·이모지) 요청.
 *
 * Optional<String>로 3단계 상태를 구분한다 (jackson-datatype-jdk8 덕분에 가능):
 * - 필드가 null(자바 기본값) → JSON에 키 자체가 없었다는 뜻 → 기존 값 유지
 * - Optional.empty() → JSON에 값이 null로 명시됨 → 필드 삭제
 * - Optional.of(값) → 그 값으로 수정
 */
@Getter
@NoArgsConstructor
@Schema(description = "5.4 타임라인 상세 기록 추가 요청")
public class RecordDetailUpdateRequestDto {

    @Schema(description = "메모. 키 생략=유지, null=삭제, 값 있으면 수정", example = "오늘 컨디션 좋았음")
    private Optional<String> memo;

    @Schema(description = "이모지. 키 생략=유지, null=삭제, 값 있으면 수정", example = "😊")
    private Optional<String> emoji;
}