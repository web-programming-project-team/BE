package com.nextstep.nextsiheung.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReadResponse(
        @Schema(example = "1203") Long notificationId,
        @Schema(example = "true") boolean read
) {
}
