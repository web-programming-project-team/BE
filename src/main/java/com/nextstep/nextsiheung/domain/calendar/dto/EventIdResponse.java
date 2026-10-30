package com.nextstep.nextsiheung.domain.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record EventIdResponse(
        @Schema(example = "915") Long eventId
) {
}
