package com.nextstep.nextsiheung.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record PolicyEndRequest(
        @Schema(example = "2026년 사업 종료") @Size(max = 200) String reason
) {
}
