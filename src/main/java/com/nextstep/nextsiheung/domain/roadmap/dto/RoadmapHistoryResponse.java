package com.nextstep.nextsiheung.domain.roadmap.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record RoadmapHistoryResponse(
        @Schema(example = "31") Long roadmapId,
        @Schema(example = "졸업 예정 · 취업 준비 · 취업/주거") String templateName,
        @Schema(example = "2027-02-10T14:00:00") LocalDateTime archivedAt,
        @Schema(example = "80") int progressRate,
        @Schema(example = "8") int completedCount
) {
}
