package com.nextstep.nextsiheung.domain.calendar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nextstep.nextsiheung.global.common.enums.EventType;
import com.nextstep.nextsiheung.global.common.enums.ProgressStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;

public record CalendarEventResponse(
        @Schema(example = "880") Long eventId,
        @Schema(example = "2026-11-05") LocalDate date,
        @Schema(type = "string", pattern = "HH:mm", description = "시간이 없는 일정(접수 마감 등)은 null", example = "15:00")
        @JsonFormat(pattern = "HH:mm") LocalTime eventTime,
        EventType type,
        @Schema(example = "청년 직무역량 아카데미 접수 마감") String title,
        @Schema(description = "POLICY · JOB · CUSTOM", example = "POLICY") String sourceType,
        @Schema(example = "12") Long sourceId,
        @Schema(description = "로드맵 항목에서 생긴 일정이면 그 진행상태") ProgressStatus itemStatus,
        @Schema(example = "11") Integer dDay
) {
}
