package com.nextstep.nextsiheung.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
        @Schema(example = "eyJhbGciOi...") @NotBlank String refreshToken
) {
}
