package com.taptap.backend.insight.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * ⚠️ name을 명시적으로 지정한 이유: 팀 인사이트 도메인(정민님 파트)에도 같은 이름의
 *    클래스가 있어서, springdoc이 클래스명만 보고 스웨거 문서 스키마를 등록하다가
 *    둘이 충돌해서 예시값이 서로 섞여 보이는 문제가 있었음. name을 다르게 지정해서 해결.
 */
@Getter
@Builder
@Schema(name = "PersonalInsightTimelineItemDto")
public class InsightTimelineItemDto {
    private Long recordId;
    private Long buttonId;
    private String buttonName;
    private LocalDateTime recordedAt;
    private String memo;
    private String emoji;
}