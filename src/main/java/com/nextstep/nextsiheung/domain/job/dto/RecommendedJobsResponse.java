package com.nextstep.nextsiheung.domain.job.dto;

import com.nextstep.nextsiheung.domain.user.dto.JobPreferenceDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

// 페이지 필드에 적용된 채용 조건(appliedFilter)을 더한 형태
public record RecommendedJobsResponse(
        @Schema(description = "목록을 거른 내 채용 조건") JobPreferenceDto appliedFilter,
        List<JobSummaryResponse> content,
        @Schema(example = "0") int page,
        @Schema(example = "20") int size,
        @Schema(example = "23") long totalElements,
        @Schema(example = "2") int totalPages,
        @Schema(example = "true") boolean hasNext
) {
}
