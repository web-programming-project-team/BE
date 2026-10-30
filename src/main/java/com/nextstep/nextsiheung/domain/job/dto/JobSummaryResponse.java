package com.nextstep.nextsiheung.domain.job.dto;

import com.nextstep.nextsiheung.global.common.enums.Education;
import com.nextstep.nextsiheung.global.common.enums.EmploymentType;
import com.nextstep.nextsiheung.global.common.enums.JobCategory;
import com.nextstep.nextsiheung.global.common.enums.JobSource;
import com.nextstep.nextsiheung.global.common.enums.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

public record JobSummaryResponse(
        @Schema(example = "5512") Long jobId,
        @Schema(example = "(주)시흥테크") String company,
        @Schema(example = "사무직 신입 채용") String title,
        Region region,
        Education education,
        @Schema(example = "NEW") String careerLevel,
        EmploymentType employmentType,
        JobCategory jobCategory,
        @Schema(example = "2026-11-01") LocalDate deadline,
        @Schema(example = "7") Integer dDay,
        @Schema(example = "false") boolean bookmarked,
        @Schema(description = "직전 방문일(user_visit) 이후 수집된 공고", example = "true") boolean isNew,
        @Schema(description = "공고 출처. 표시 문구는 프론트에서 매핑") JobSource dataSource,
        List<HelpfulPolicyDto> helpfulPolicies
) {
}
