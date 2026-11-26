package com.nextstep.nextsiheung.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SignupResponse(
        @Schema(example = "1") Long userId,
        @Schema(example = "a@next-siheung.kr") String email,
        @Schema(example = "A") String nickname,
        @Schema(description = "가입 직후 false → 화면은 온보딩으로 이동", example = "false") boolean onboardingCompleted
) {
}
