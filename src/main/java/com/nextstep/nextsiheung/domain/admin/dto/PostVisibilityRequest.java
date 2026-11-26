package com.nextstep.nextsiheung.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostVisibilityRequest(
        @Schema(example = "true") @NotNull Boolean hidden,
        @Schema(example = "실명 노출") @Size(max = 200) String reason
) {
}
