package com.nextstep.nextsiheung.domain.admin.dto;

import com.nextstep.nextsiheung.global.common.enums.Education;
import com.nextstep.nextsiheung.global.common.enums.EmploymentType;
import com.nextstep.nextsiheung.global.common.enums.JobCategory;
import com.nextstep.nextsiheung.global.common.enums.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

// 등록(POST)과 수정(PUT, 본문 전체)이 같이 쓴다. source와 created_by는 서버가 채운다
public record AdminJobRequest(
        @Schema(example = "(주)시흥테크") @NotBlank String company,
        @Schema(example = "사무직 신입 채용") @NotBlank String title,
        @Schema(example = "SIHEUNG") @NotNull Region region,
        @Schema(example = "경기 시흥시 정왕동") String address,
        @Schema(example = "UNIVERSITY") @NotNull Education education,
        @Schema(example = "NEW") String careerLevel,
        @Schema(example = "FULL_TIME") @NotNull EmploymentType employmentType,
        @Schema(example = "OFFICE") @NotNull JobCategory jobCategory,
        @Schema(example = "회사 내규") String salary,
        @Schema(example = "2026-11-01") @NotNull LocalDate deadline,
        @Schema(example = "https://...") String originalUrl
) {
}
