package com.nextstep.nextsiheung.domain.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record BookmarkedJobResponse(
        @Schema(example = "5512") Long jobId,
        @Schema(example = "(주)시흥테크") String company,
        @Schema(example = "사무직 신입 채용") String title,
        @Schema(example = "2026-11-01") LocalDate deadline,
        @Schema(example = "7") Integer dDay,
        @Schema(description = "마감된 공고면 true", example = "false") boolean closed
) {
}
