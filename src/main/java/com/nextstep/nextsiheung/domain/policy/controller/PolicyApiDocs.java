package com.nextstep.nextsiheung.domain.policy.controller;

import static com.nextstep.nextsiheung.global.exception.ErrorCode.POLICY_NOT_FOUND;

import com.nextstep.nextsiheung.domain.policy.dto.PolicyDetailResponse;
import com.nextstep.nextsiheung.domain.policy.dto.PolicySummaryResponse;
import com.nextstep.nextsiheung.domain.post.dto.PostSummaryResponse;
import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.RecruitStatus;
import com.nextstep.nextsiheung.global.exception.ApiErrorCodes;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import com.nextstep.nextsiheung.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Tag(name = "정책·프로그램", description = "정책 목록과 상세. 상세에는 공식 신청 URL, 마지막 확인일, 관련 후기와 채용공고가 함께 온다. 담당: 강준 · 7주차")
public interface PolicyApiDocs {

    @Operation(summary = "정책·프로그램 목록 (1순위, 로그인 선택)", description = "분야·모집상태·키워드로 정책을 찾는다. 로그인하면 각 항목에 내 진행상태(myStatus)가 함께 온다.")
    ApiResponse<PageResponse<PolicySummaryResponse>> list(
            @Parameter(description = "분야") InterestField field,
            @Parameter(description = "모집상태. 생략하면 OPEN, UPCOMING 둘 다") RecruitStatus recruitStatus,
            @Parameter(description = "정책명·기관명 검색") String keyword,
            @Parameter(description = "deadline(기본) · latest") String sort,
            @Parameter(description = "페이지 (0부터)") @Min(0) int page,
            @Parameter(description = "페이지 크기 (최대 50)") @Min(1) @Max(50) int size);

    @Operation(summary = "정책·프로그램 상세 (1순위, 로그인 선택)", description = """
            상세 화면 한 장에 필요한 데이터를 모두 준다. 관련 후기는 최신 3개, 연결 채용공고는 2개까지.
            - 비로그인이면 myItem은 null
            - myItem이 null이면 상태 드롭다운 선택 시 POST /roadmaps/me/items 호출""")
    @ApiErrorCodes({POLICY_NOT_FOUND})
    ApiResponse<PolicyDetailResponse> detail(@Parameter(description = "정책 ID") Long policyId);

    @Operation(summary = "정책 관련 후기 목록 (2순위)", description = "상세의 '후기 더 보기'. 응답 항목 형식은 커뮤니티 목록(GET /posts)과 같다.")
    @SecurityRequirements
    @ApiErrorCodes({POLICY_NOT_FOUND})
    ApiResponse<PageResponse<PostSummaryResponse>> reviews(
            @Parameter(description = "정책 ID") Long policyId,
            @Parameter(description = "페이지 (0부터)") @Min(0) int page,
            @Parameter(description = "페이지 크기 (최대 50)") @Min(1) @Max(50) int size);
}
