package com.nextstep.nextsiheung.domain.admin.controller;

import static com.nextstep.nextsiheung.global.exception.ErrorCode.INVALID_INPUT;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.POLICY_NOT_FOUND;

import com.nextstep.nextsiheung.domain.admin.dto.AdminPolicyRequest;
import com.nextstep.nextsiheung.domain.admin.dto.AdminPolicyResponse;
import com.nextstep.nextsiheung.domain.admin.dto.AdminPolicySaveResponse;
import com.nextstep.nextsiheung.domain.admin.dto.PolicyEndRequest;
import com.nextstep.nextsiheung.domain.admin.dto.PolicyEndResponse;
import com.nextstep.nextsiheung.global.common.enums.PolicySource;
import com.nextstep.nextsiheung.global.common.enums.RecruitStatus;
import com.nextstep.nextsiheung.global.exception.ApiErrorCodes;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import com.nextstep.nextsiheung.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Tag(name = "관리자", description = "정책·프로그램 관리, 채용공고 등록, 신고 처리, 운영 지표, 수집 기록 조회. ADMIN만 호출 가능(아니면 403 FORBIDDEN). 담당: 강준 · 7주차 (공고 10주차)")
public interface AdminPolicyApiDocs {

    @Operation(summary = "정책 관리 목록 (2순위)", description = "출처, 모집상태, 오래된 정보 여부로 거른다. stale=true는 마지막 확인일이 30일 넘은 정책.")
    ApiResponse<PageResponse<AdminPolicyResponse>> list(
            @Parameter(description = "출처") PolicySource source,
            @Parameter(description = "모집상태") RecruitStatus recruitStatus,
            @Parameter(description = "확인 필요한 정책만") Boolean stale,
            @Parameter(description = "정책명·기관명") String keyword,
            @Parameter(description = "페이지 (0부터)") @Min(0) int page,
            @Parameter(description = "페이지 크기 (최대 50)") @Min(1) @Max(50) int size);

    @Operation(summary = "정책·프로그램 등록 (2순위)", description = """
            시흥 지역 프로그램처럼 API로 받을 수 없는 정보를 직접 등록한다. 공식 출처 URL은 필수.
            - recruitStatus는 접수기간으로 서버가 계산
            - tags는 채용공고의 '도움 되는 지원' 매칭에 사용""")
    @ApiErrorCodes({INVALID_INPUT})
    ApiResponse<AdminPolicySaveResponse> create(@Valid AdminPolicyRequest request);

    @Operation(summary = "정책·프로그램 수정 (2순위)", description = "등록과 같은 본문 전체를 보낸다. 저장하면 마지막 확인일이 오늘로 바뀌고, 사용자 로드맵에 바로 반영된다.")
    @ApiErrorCodes({POLICY_NOT_FOUND, INVALID_INPUT})
    ApiResponse<AdminPolicySaveResponse> update(@Parameter(description = "정책 ID") Long policyId, @Valid AdminPolicyRequest request);

    @Operation(summary = "정책 종료 (2순위)", description = "사업이 끝난 정책을 ENDED로 바꾼다. 새 로드맵에는 들어가지 않고, 이미 담은 사용자에게는 '종료된 정책'으로 표시된다.")
    @ApiErrorCodes({POLICY_NOT_FOUND})
    ApiResponse<PolicyEndResponse> end(@Parameter(description = "정책 ID") Long policyId, @Valid PolicyEndRequest request);

    @Operation(summary = "정보 확인 완료 (2순위)", description = "내용 변경 없이 공식 사이트와 대조만 했을 때 마지막 확인일을 오늘로 갱신한다.")
    @ApiErrorCodes({POLICY_NOT_FOUND})
    ApiResponse<AdminPolicySaveResponse> verify(@Parameter(description = "정책 ID") Long policyId);
}
