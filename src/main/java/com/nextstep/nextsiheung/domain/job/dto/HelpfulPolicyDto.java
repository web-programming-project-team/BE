package com.nextstep.nextsiheung.domain.job.dto;

import com.nextstep.nextsiheung.global.common.enums.ProgressStatus;
import io.swagger.v3.oas.annotations.media.Schema;

// '이 공고에 도움 되는 지원' 한 건과 그 정책의 내 진행상태
public record HelpfulPolicyDto(
        @Schema(example = "21") Long policyId,
        @Schema(example = "청년 자격증 응시료 지원") String title,
        @Schema(description = "로드맵에 없으면 null") ProgressStatus myStatus
) {
}
