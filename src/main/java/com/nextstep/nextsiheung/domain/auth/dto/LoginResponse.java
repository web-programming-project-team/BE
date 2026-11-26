package com.nextstep.nextsiheung.domain.auth.dto;

import com.nextstep.nextsiheung.global.common.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(example = "eyJhbGciOi...") String accessToken,
        @Schema(example = "eyJhbGciOi...") String refreshToken,
        @Schema(description = "Access 토큰 유효시간(초)", example = "1800") long expiresIn,
        LoginUser user
) {
    public record LoginUser(
            @Schema(example = "1") Long userId,
            @Schema(example = "A") String nickname,
            Role role,
            @Schema(description = "false면 화면은 온보딩으로 보냄", example = "true") boolean onboardingCompleted
    ) {
    }
}
