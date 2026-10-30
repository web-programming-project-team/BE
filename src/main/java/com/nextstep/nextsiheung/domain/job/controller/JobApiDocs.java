package com.nextstep.nextsiheung.domain.job.controller;

import static com.nextstep.nextsiheung.global.exception.ErrorCode.ALREADY_EXISTS;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.JOB_NOT_FOUND;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.ONBOARDING_REQUIRED;

import com.nextstep.nextsiheung.domain.job.dto.BookmarkResponse;
import com.nextstep.nextsiheung.domain.job.dto.BookmarkedJobResponse;
import com.nextstep.nextsiheung.domain.job.dto.JobDetailResponse;
import com.nextstep.nextsiheung.domain.job.dto.RecommendedJobsResponse;
import com.nextstep.nextsiheung.global.exception.ApiErrorCodes;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import com.nextstep.nextsiheung.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Tag(name = "맞춤 채용", description = "사람인 수집 공고와 관리자 등록 공고(승인 전 시연용)를 사용자 조건으로 거른 목록과, 공고마다 도움 되는 정책 연결. 담당: 강준 · 10주차")
public interface JobApiDocs {

    @Operation(summary = "맞춤 채용공고 목록 (2순위)", description = """
            저장된 채용 조건과 일치하는 공고를 돌려준다. 공고마다 '이 공고에 도움 되는 지원'과 그 정책의 내 진행상태가 붙어 온다.
            - isNew: 직전 방문일(user_visit) 이후 수집된 공고
            - dataSource: 공고의 source 값(SARAMIN · ADMIN). 출처 표시 문구는 프론트에서 매핑""")
    @ApiErrorCodes({ONBOARDING_REQUIRED})
    ApiResponse<RecommendedJobsResponse> recommended(
            @Parameter(description = "deadline(기본) · latest") String sort,
            @Parameter(description = "페이지 (0부터)") @Min(0) int page,
            @Parameter(description = "페이지 크기 (최대 50)") @Min(1) @Max(50) int size);

    @Operation(summary = "채용공고 상세 (2순위, 로그인 선택)", description = "공고 원문 링크와 함께, 도움 되는 정책과 같은 직무의 합격 후기를 준다.")
    @ApiErrorCodes({JOB_NOT_FOUND})
    ApiResponse<JobDetailResponse> detail(@Parameter(description = "공고 ID") Long jobId);

    @Operation(summary = "관심 공고 저장 (2순위)", description = "북마크하면 공고 마감일이 캘린더에 JOB_DEADLINE으로 등록된다.")
    @ApiErrorCodes({JOB_NOT_FOUND, ALREADY_EXISTS})
    ApiResponse<BookmarkResponse> bookmark(@Parameter(description = "공고 ID") Long jobId);

    @Operation(summary = "관심 공고 해제 (2순위)", description = "캘린더의 채용 마감 일정도 함께 지운다.")
    void unbookmark(@Parameter(description = "공고 ID") Long jobId);

    @Operation(summary = "관심 공고 목록 (2순위)", description = "저장한 공고를 마감 임박순으로 돌려준다. 마감된 공고는 closed: true로 표시한다.")
    ApiResponse<PageResponse<BookmarkedJobResponse>> bookmarks(
            @Parameter(description = "페이지 (0부터)") @Min(0) int page,
            @Parameter(description = "페이지 크기 (최대 50)") @Min(1) @Max(50) int size);
}
