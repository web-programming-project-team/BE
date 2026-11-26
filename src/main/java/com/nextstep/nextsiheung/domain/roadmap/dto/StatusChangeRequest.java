package com.nextstep.nextsiheung.domain.roadmap.dto;

import com.nextstep.nextsiheung.global.common.enums.ProgressStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record StatusChangeRequest(
        @Schema(example = "COMPLETED") @NotNull ProgressStatus status
) {
}
