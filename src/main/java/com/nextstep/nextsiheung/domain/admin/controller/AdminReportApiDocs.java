package com.nextstep.nextsiheung.domain.admin.controller;

import static com.nextstep.nextsiheung.global.exception.ErrorCode.POST_NOT_FOUND;

import com.nextstep.nextsiheung.domain.admin.dto.PostVisibilityRequest;
import com.nextstep.nextsiheung.domain.admin.dto.PostVisibilityResponse;
import com.nextstep.nextsiheung.domain.admin.dto.ReportGroupResponse;
import com.nextstep.nextsiheung.global.exception.ApiErrorCodes;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import com.nextstep.nextsiheung.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Tag(name = "관리자")
public interface AdminReportApiDocs {

    @Operation(summary = "신고 목록 (2순위)", description = "게시글별로 묶어 보여준다. 자동 숨김된 글이 먼저 온다.")
    ApiResponse<PageResponse<ReportGroupResponse>> reports(
            @Parameter(description = "PENDING(기본) · RESOLVED") String status,
            @Parameter(description = "페이지 (0부터)") @Min(0) int page,
            @Parameter(description = "페이지 크기 (최대 50)") @Min(1) @Max(50) int size);

    @Operation(summary = "게시글 숨김·복구 (2순위)", description = "검토 결과에 따라 숨기거나 다시 보이게 한다. 처리하면 해당 글의 신고는 RESOLVED가 된다.")
    @ApiErrorCodes({POST_NOT_FOUND})
    ApiResponse<PostVisibilityResponse> changeVisibility(@Parameter(description = "게시글 ID") Long postId,
                                                         @Valid PostVisibilityRequest request);
}
