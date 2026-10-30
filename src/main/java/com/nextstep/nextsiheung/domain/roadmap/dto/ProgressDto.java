package com.nextstep.nextsiheung.domain.roadmap.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProgressDto(
        @Schema(description = "진행률(%)", example = "40") int rate,
        @Schema(example = "2") int completed,
        @Schema(example = "3") int inProgress,
        @Schema(example = "5") int remaining
) {
}
