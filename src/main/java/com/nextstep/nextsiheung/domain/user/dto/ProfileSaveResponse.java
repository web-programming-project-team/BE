package com.nextstep.nextsiheung.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProfileSaveResponse(
        @Schema(example = "true") boolean onboardingCompleted,
        @Schema(example = "31") Long roadmapId,
        @Schema(example = "졸업 예정 · 취업 준비 · 취업/주거") String templateName
) {
}
