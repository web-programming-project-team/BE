package com.nextstep.nextsiheung.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

// ERD sync_log 한 행
public record SyncLogResponse(
        @Schema(example = "58") Long syncLogId,
        @Schema(description = "ONTONG · SARAMIN", example = "SARAMIN") String source,
        @Schema(example = "SCHEDULED") String triggerType,
        @Schema(description = "RUNNING · SUCCESS · FAILED", example = "SUCCESS") String status,
        @Schema(example = "214") int fetched,
        @Schema(example = "12") int inserted,
        @Schema(example = "30") int updated,
        @Schema(example = "0") int failed,
        @Schema(description = "실패했을 때만") String errorMessage,
        @Schema(example = "2026-10-26T04:00:00") LocalDateTime startedAt,
        @Schema(example = "2026-10-26T04:00:41") LocalDateTime finishedAt
) {
}
