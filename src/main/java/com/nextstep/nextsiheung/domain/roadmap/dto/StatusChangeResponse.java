package com.nextstep.nextsiheung.domain.roadmap.dto;

import com.nextstep.nextsiheung.global.common.enums.ProgressStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record StatusChangeResponse(
        @Schema(example = "205") Long itemId,
        ProgressStatus status,
        @Schema(description = "바뀐 진행률. 화면은 다시 조회하지 않아도 됨") ProgressDto progress,
        @Schema(description = "COMPLETED로 바뀔 때만 true → 1분 후기 작성 제안", example = "true") boolean suggestReview
) {
}
