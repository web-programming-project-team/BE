package com.nextstep.nextsiheung.domain.job.dto;

import com.nextstep.nextsiheung.global.common.enums.Education;
import com.nextstep.nextsiheung.global.common.enums.EmploymentType;
import com.nextstep.nextsiheung.global.common.enums.JobCategory;
import com.nextstep.nextsiheung.global.common.enums.JobSource;
import com.nextstep.nextsiheung.global.common.enums.PostType;
import com.nextstep.nextsiheung.global.common.enums.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

public record JobDetailResponse(
        @Schema(example = "5512") Long jobId,
        @Schema(example = "(주)시흥테크") String company,
        @Schema(example = "사무직 신입 채용") String title,
        Region region,
        @Schema(example = "경기 시흥시 정왕동") String address,
        Education education,
        @Schema(example = "NEW") String careerLevel,
        EmploymentType employmentType,
        JobCategory jobCategory,
        @Schema(example = "회사 내규") String salary,
        @Schema(example = "2026-11-01") LocalDate deadline,
        @Schema(example = "7") Integer dDay,
        @Schema(description = "공고 원문 링크", example = "https://www.saramin.co.kr/...") String originalUrl,
        JobSource dataSource,
        @Schema(example = "true") boolean bookmarked,
        List<HelpfulPolicyDto> helpfulPolicies,
        @Schema(description = "같은 직무의 합격 후기") List<JobReview> reviews
) {
    public record JobReview(
            @Schema(example = "322") Long postId,
            PostType type,
            @Schema(example = "4") Integer rating,
            @Schema(example = "컴활 1급이 도움됐어요") String oneLiner,
            JobCategory jobCategory
    ) {
    }
}
