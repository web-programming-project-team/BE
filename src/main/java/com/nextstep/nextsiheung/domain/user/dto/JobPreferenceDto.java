package com.nextstep.nextsiheung.domain.user.dto;

import com.nextstep.nextsiheung.global.common.enums.Education;
import com.nextstep.nextsiheung.global.common.enums.EmploymentType;
import com.nextstep.nextsiheung.global.common.enums.JobCategory;
import com.nextstep.nextsiheung.global.common.enums.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

// 희망 채용 조건. 등록·조회·부분 수정(PATCH, 보낸 필드만 변경)에 같이 쓴다
public record JobPreferenceDto(
        @Schema(example = "[\"SIHEUNG\", \"ANSAN\"]") List<Region> regions,
        Education education,
        @Schema(description = "경력 수준", example = "NEW") String careerLevel,
        @Schema(example = "[\"OFFICE\", \"IT\"]") List<JobCategory> jobCategories,
        @Schema(example = "[\"FULL_TIME\"]") List<EmploymentType> employmentTypes
) {
}
