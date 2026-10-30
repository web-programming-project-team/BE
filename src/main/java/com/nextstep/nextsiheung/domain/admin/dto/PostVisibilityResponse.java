package com.nextstep.nextsiheung.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostVisibilityResponse(
        @Schema(example = "322") Long postId,
        @Schema(example = "true") boolean hidden
) {
}
