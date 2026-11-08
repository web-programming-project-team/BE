package com.nextstep.nextsiheung.domain.admin.dto;

import com.nextstep.nextsiheung.global.common.enums.Education;
import com.nextstep.nextsiheung.global.common.enums.EmploymentType;
import com.nextstep.nextsiheung.global.common.enums.JobCategory;
import com.nextstep.nextsiheung.global.common.enums.JobSource;
import com.nextstep.nextsiheung.global.common.enums.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

// 관리자 공고 목록 한 행. 수정 폼을 바로 채울 수 있도록 등록 본문(AdminJobRequest)의 필드를 모두 담는다
public record AdminJobListResponse(
        @Schema(example = "5600") Long jobId,
        JobSource source,
        @Schema(example = "(주)시흥테크") String company,
        @Schema(example = "사무직 신입 채용") String title,
        Region region,
        @Schema(example = "경기 시흥시 정왕동") String address,
        Education education,
        @Schema(example = "NEW") String careerLevel,
        EmploymentType employmentType,
        JobCategory jobCategory,
        @Schema(example = "회사 내규") String salary,
        @Schema(description = "마감일. 상시채용이면 null", example = "2026-11-01") LocalDate deadline,
        @Schema(description = "마감까지 남은 일수. 상시채용이면 null", example = "7") Integer dDay,
        @Schema(example = "https://...") String originalUrl,
        @Schema(description = "관리자 마감이거나 마감일이 지났으면 true", example = "false") boolean closed,
        @Schema(description = "등록(수집) 일시", example = "2026-11-07T10:00:00") LocalDateTime collectedAt
) {
}
