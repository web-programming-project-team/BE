package com.nextstep.nextsiheung.domain.admin.dto;

import com.nextstep.nextsiheung.global.common.enums.RecruitStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

// 등록·수정·확인 완료가 같이 쓴다
public record AdminPolicySaveResponse(
        @Schema(example = "12") Long policyId,
        RecruitStatus recruitStatus,
        @Schema(description = "저장하면 오늘로 갱신", example = "2026-10-25") LocalDate lastVerifiedAt
) {
}
