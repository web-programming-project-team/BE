package com.nextstep.nextsiheung.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UnreadCountResponse(
        @Schema(example = "4") long unreadCount
) {
}
