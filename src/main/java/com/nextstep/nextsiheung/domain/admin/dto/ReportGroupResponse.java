package com.nextstep.nextsiheung.domain.admin.dto;

import com.nextstep.nextsiheung.global.common.enums.ReportReason;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;

// 신고를 게시글별로 묶은 한 줄
public record ReportGroupResponse(
        @Schema(example = "322") Long postId,
        @Schema(example = "...") String oneLiner,
        @Schema(example = "5") long reportCount,
        @Schema(description = "사유별 건수", example = "{\"PERSONAL_INFO\": 4, \"SPAM\": 1}") Map<ReportReason, Long> reasons,
        @Schema(example = "true") boolean autoHidden,
        @Schema(description = "PENDING · RESOLVED", example = "PENDING") String status,
        @Schema(example = "2026-10-24T22:10:00") LocalDateTime lastReportedAt
) {
}
