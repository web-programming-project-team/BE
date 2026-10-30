package com.nextstep.nextsiheung.domain.auth.dto;

import com.nextstep.nextsiheung.global.common.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record MeResponse(
        @Schema(example = "1") Long userId,
        @Schema(example = "a@next-siheung.kr") String email,
        @Schema(example = "A") String nickname,
        Role role,
        @Schema(example = "true") boolean onboardingCompleted,
        @Schema(example = "2026-10-25T10:12:00") LocalDateTime createdAt
) {
}
