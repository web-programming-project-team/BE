package com.nextstep.nextsiheung.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LifeStageChangeResponse(
        @Schema(example = "31") Long archivedRoadmapId,
        @Schema(example = "47") Long newRoadmapId,
        @Schema(example = "첫 취업 · 주거/금융") String templateName,
        @Schema(description = "FIRST_JOB으로 바뀌면 true → 화면이 합격 후기 작성을 제안", example = "true") boolean suggestReview
) {
}
