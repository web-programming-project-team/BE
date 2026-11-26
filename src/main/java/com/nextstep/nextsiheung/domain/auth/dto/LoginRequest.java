package com.nextstep.nextsiheung.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "a@next-siheung.kr") @NotBlank String email,
        @Schema(example = "next1234!") @NotBlank String password
) {
}
