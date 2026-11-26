package com.nextstep.nextsiheung.domain.roadmap.dto;

import com.nextstep.nextsiheung.global.common.enums.ProgressStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record RoadmapItemAddResponse(
        @Schema(example = "230") Long itemId,
        @Schema(example = "21") Long policyId,
        @Schema(description = "정책 분야가 맞는 템플릿 단계", example = "3") int stepOrder,
        ProgressStatus status,
        @Schema(description = "자동 등록된 접수 시작·마감 일정", example = "[880, 881]") List<Long> calendarEventIds
) {
}
