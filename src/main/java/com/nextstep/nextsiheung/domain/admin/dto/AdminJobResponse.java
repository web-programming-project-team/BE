package com.nextstep.nextsiheung.domain.admin.dto;

import com.nextstep.nextsiheung.global.common.enums.JobSource;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminJobResponse(
        @Schema(example = "5600") Long jobId,
        @Schema(example = "ADMIN") JobSource source,
        @Schema(example = "false") boolean closed
) {
}
