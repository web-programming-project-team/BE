package com.nextstep.nextsiheung.domain.admin.controller;

import static com.nextstep.nextsiheung.global.exception.ErrorCode.FORBIDDEN;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.INVALID_INPUT;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.JOB_NOT_FOUND;

import com.nextstep.nextsiheung.domain.admin.dto.AdminJobRequest;
import com.nextstep.nextsiheung.domain.admin.dto.AdminJobResponse;
import com.nextstep.nextsiheung.global.exception.ApiErrorCodes;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "관리자")
public interface AdminJobApiDocs {

    @Operation(summary = "채용공고 등록 (2순위)", description = """
            사람인 API 승인이 나지 않았거나 승인 전일 때 공고를 직접 넣는다. 저장된 공고는 source=ADMIN, created_by=관리자 ID로 맞춤 채용 목록에 수집 공고와 똑같이 나온다.
            - region은 Region, education은 Education, employmentType은 EmploymentType, jobCategory는 JobCategory enum 값
            - source와 created_by는 서버가 채움""")
    @ApiErrorCodes({INVALID_INPUT})
    ApiResponse<AdminJobResponse> create(@Valid AdminJobRequest request);

    @Operation(summary = "채용공고 수정 (2순위)", description = "등록과 같은 본문 전체를 보낸다. 관리자 등록 공고(source=ADMIN)만 수정할 수 있다.")
    @ApiErrorCodes({JOB_NOT_FOUND, INVALID_INPUT, FORBIDDEN})
    ApiResponse<AdminJobResponse> update(@Parameter(description = "공고 ID") Long jobId, @Valid AdminJobRequest request);

    @Operation(summary = "채용공고 마감 (2순위)", description = "마감일 전이라도 공고를 마감 처리한다. 맞춤 채용 목록에서 빠지고, 관심 공고 목록에는 closed: true로 남는다.")
    @ApiErrorCodes({JOB_NOT_FOUND})
    ApiResponse<AdminJobResponse> close(@Parameter(description = "공고 ID") Long jobId);
}
