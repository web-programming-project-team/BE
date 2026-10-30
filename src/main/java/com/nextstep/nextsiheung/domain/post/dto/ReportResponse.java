package com.nextstep.nextsiheung.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReportResponse(
        @Schema(example = "77") Long reportId,
        @Schema(description = "이 신고로 5건이 쌓여 자동 숨김됐으면 true", example = "false") boolean autoHidden
) {
}
