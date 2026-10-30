package com.nextstep.nextsiheung.domain.admin.dto;

import com.nextstep.nextsiheung.global.common.enums.RecruitStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record PolicyEndResponse(
        @Schema(example = "12") Long policyId,
        @Schema(example = "ENDED") RecruitStatus recruitStatus
) {
}
