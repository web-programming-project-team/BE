package com.nextstep.nextsiheung.domain.policy.dto;

import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.ProgressStatus;
import com.nextstep.nextsiheung.global.common.enums.RecruitStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record PolicySummaryResponse(
        @Schema(example = "12") Long policyId,
        @Schema(example = "청년 직무역량 아카데미") String title,
        InterestField field,
        @Schema(example = "시흥시청소년청년재단") String agency,
        @Schema(example = "2026-10-20") LocalDate applyStartDate,
        @Schema(example = "2026-11-05") LocalDate applyEndDate,
        RecruitStatus recruitStatus,
        @Schema(example = "11") Integer dDay,
        @Schema(description = "로그인했고 로드맵에 있으면 내 진행상태, 아니면 null") ProgressStatus myStatus
) {
}
