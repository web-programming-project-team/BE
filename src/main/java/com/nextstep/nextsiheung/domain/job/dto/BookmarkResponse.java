package com.nextstep.nextsiheung.domain.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record BookmarkResponse(
        @Schema(example = "5512") Long jobId,
        @Schema(example = "true") boolean bookmarked,
        @Schema(description = "캘린더에 등록된 JOB_DEADLINE 일정", example = "902") Long calendarEventId
) {
}
