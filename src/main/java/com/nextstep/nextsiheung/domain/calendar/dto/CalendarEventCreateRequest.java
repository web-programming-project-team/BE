package com.nextstep.nextsiheung.domain.calendar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nextstep.nextsiheung.global.common.enums.EventType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

// policyId를 보내면 그 정책의 상담일·참여일, 없으면 title과 date로 CUSTOM 일정
public record CalendarEventCreateRequest(
        @Schema(example = "COUNSELING") @NotNull EventType type,
        @Schema(example = "2026-11-03") @NotNull LocalDate date,
        @Schema(type = "string", pattern = "HH:mm", description = "선택. 생략하면 종일 일정", example = "15:00")
        @JsonFormat(pattern = "HH:mm") LocalTime eventTime,
        @Schema(example = "청년스테이션 진로 상담") @Size(max = 100) String title,
        @Schema(example = "7") Long policyId,
        @Schema(example = "신분증 지참") @Size(max = 200) String memo
) {
}
