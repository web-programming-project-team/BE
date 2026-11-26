package com.nextstep.nextsiheung.domain.post.dto;

import com.nextstep.nextsiheung.global.common.enums.ReportReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportRequest(
        @Schema(example = "PERSONAL_INFO") @NotNull ReportReason reason,
        @Schema(example = "본문에 실명이 있어요") @Size(max = 200) String detail
) {
}
