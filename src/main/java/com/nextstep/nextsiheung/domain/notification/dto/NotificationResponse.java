package com.nextstep.nextsiheung.domain.notification.dto;

import com.nextstep.nextsiheung.global.common.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record NotificationResponse(
        @Schema(example = "1203") Long notificationId,
        NotificationType type,
        @Schema(example = "자격증 응시료 지원 신청 마감 D-3") String title,
        @Schema(example = "청년 자격증 응시료 지원 신청이 3일 남았어요.") String body,
        @Schema(description = "누르면 이동할 화면") Link link,
        @Schema(example = "false") boolean read,
        @Schema(example = "2026-10-25T09:00:00") LocalDateTime createdAt
) {
    public record Link(
            @Schema(description = "POLICY · JOB · JOB_LIST · POST", example = "POLICY") String targetType,
            @Schema(description = "목록 화면이면 null", example = "21") Long targetId
    ) {
    }
}
