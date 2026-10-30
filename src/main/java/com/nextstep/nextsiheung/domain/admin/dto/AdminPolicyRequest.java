package com.nextstep.nextsiheung.domain.admin.dto;

import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.LifeStage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

// 등록(POST)과 수정(PUT, 본문 전체)이 같이 쓴다. recruitStatus는 접수기간으로 서버가 계산한다
public record AdminPolicyRequest(
        @Schema(example = "청년 직무역량 아카데미") @NotBlank String title,
        @Schema(example = "EMPLOYMENT") @NotNull InterestField field,
        @Schema(example = "시흥시청소년청년재단") @NotBlank String agency,
        @Schema(example = "시흥시 거주 19~24세 미취업 청년") String target,
        @Schema(example = "직무별 실무 교육 8주 과정") String content,
        @Schema(example = "2026-10-20") LocalDate applyStartDate,
        @Schema(example = "2026-11-05") LocalDate applyEndDate,
        @Schema(example = "031-000-0000") String contact,
        @Schema(description = "공식 출처 URL (필수)", example = "https://...") @NotBlank String sourceUrl,
        @Schema(example = "https://...") String applyUrl,
        @Schema(example = "[\"GRADUATING\", \"JOB_SEEKING\"]") List<LifeStage> lifeStages,
        @Schema(description = "채용공고의 '도움 되는 지원' 매칭에 사용", example = "[\"사무\", \"IT\", \"자격증\"]") List<String> tags
) {
}
