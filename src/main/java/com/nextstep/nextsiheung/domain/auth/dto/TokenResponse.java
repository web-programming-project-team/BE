package com.nextstep.nextsiheung.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(
        @Schema(example = "eyJhbGciOi...") String accessToken,
        @Schema(description = "Refresh 토큰도 함께 교체(rotation)", example = "eyJhbGciOi...") String refreshToken,
        @Schema(example = "1800") long expiresIn
) {
}
