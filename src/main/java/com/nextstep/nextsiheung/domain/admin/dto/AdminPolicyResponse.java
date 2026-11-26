package com.nextstep.nextsiheung.domain.admin.dto;

import com.nextstep.nextsiheung.global.common.enums.PolicySource;
import com.nextstep.nextsiheung.global.common.enums.RecruitStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record AdminPolicyResponse(
        @Schema(example = "12") Long policyId,
        @Schema(example = "청년 직무역량 아카데미") String title,
        @Schema(example = "시흥시청소년청년재단") String agency,
        PolicySource source,
        RecruitStatus recruitStatus,
        @Schema(example = "2026-11-05") LocalDate applyEndDate,
        @Schema(description = "등록일", example = "2026-10-01") LocalDate createdAt,
        @Schema(example = "2026-10-20") LocalDate lastVerifiedAt,
        @Schema(description = "마지막 확인일이 30일 넘었으면 true", example = "false") boolean stale,
        @Schema(description = "이 정책을 로드맵에 담은 사용자 수", example = "38") long roadmapItemCount
) {
}
