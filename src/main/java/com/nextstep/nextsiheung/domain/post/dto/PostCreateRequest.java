package com.nextstep.nextsiheung.domain.post.dto;

import com.nextstep.nextsiheung.global.common.enums.JobCategory;
import com.nextstep.nextsiheung.global.common.enums.PostType;
import com.nextstep.nextsiheung.global.common.enums.PrepPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

// 종류별 필수값(PROGRAM_REVIEW는 policyId, PASS_REVIEW는 jobId 또는 jobCategory, PORTFOLIO는 portfolioUrl)은 서비스에서 검사한다
public record PostCreateRequest(
        @Schema(example = "PROGRAM_REVIEW") @NotNull PostType type,
        @Schema(description = "PROGRAM_REVIEW면 필수", example = "12") Long policyId,
        @Schema(description = "PASS_REVIEW면 jobId 또는 jobCategory 중 하나 필수") Long jobId,
        @Schema(example = "OFFICE") JobCategory jobCategory,
        @Schema(description = "1~5, PORTFOLIO는 생략 가능", example = "5") @Min(1) @Max(5) Integer rating,
        @Schema(example = "실습 연계까지 돼서 좋았어요") @NotBlank @Size(max = 60) String oneLiner,
        @Schema(example = "MONTH_1") @NotNull PrepPeriod prepPeriod,
        @Schema(example = "[\"실무 경험\", \"이력서 첨삭\"]") List<String> helpfulPoints,
        @Schema(description = "전화번호·이메일 패턴이 있으면 CONTACT_INFO_DETECTED", example = "8주 동안 엑셀, 문서작성 위주로 배웠고...") String content,
        @Schema(description = "PORTFOLIO면 필수(링크). 파일 첨부는 3순위라 제외") String portfolioUrl
) {
}
