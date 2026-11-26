package com.nextstep.nextsiheung.domain.roadmap.dto;

import com.nextstep.nextsiheung.global.common.enums.ProgressStatus;
import com.nextstep.nextsiheung.global.common.enums.RecruitStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// 내 로드맵(GET /roadmaps/me)과 지난 로드맵 상세(GET /roadmaps/{roadmapId})가 같이 쓴다
public record RoadmapResponse(
        @Schema(example = "31") Long roadmapId,
        @Schema(example = "졸업 예정 · 취업 준비 · 취업/주거") String templateName,
        @Schema(description = "미완료 항목이 있는 첫 단계", example = "2") Integer currentStep,
        @Schema(description = "보관된 로드맵이면 true (읽기 전용)", example = "false") boolean archived,
        @Schema(description = "보관 시각, 현재 로드맵이면 null") LocalDateTime archivedAt,
        ProgressDto progress,
        List<Step> steps
) {
    public record Step(
            @Schema(example = "2") int stepOrder,
            @Schema(example = "직무역량 프로그램") String title,
            @Schema(example = "false") boolean completed,
            List<Item> items
    ) {
    }

    public record Item(
            @Schema(example = "205") Long itemId,
            @Schema(example = "12") Long policyId,
            @Schema(example = "청년 직무역량 아카데미") String title,
            @Schema(example = "시흥시청소년청년재단") String agency,
            ProgressStatus status,
            @Schema(example = "2026-10-20") LocalDate applyStartDate,
            @Schema(example = "2026-11-05") LocalDate applyEndDate,
            @Schema(example = "11") Integer dDay,
            RecruitStatus recruitStatus,
            @Schema(example = "https://...") String applyUrl
    ) {
    }
}
