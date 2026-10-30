package com.nextstep.nextsiheung.domain.user.controller;

import static com.nextstep.nextsiheung.global.exception.ErrorCode.INVALID_INPUT;

import com.nextstep.nextsiheung.domain.user.dto.JobPreferenceDto;
import com.nextstep.nextsiheung.domain.user.dto.LifeStageChangeRequest;
import com.nextstep.nextsiheung.domain.user.dto.LifeStageChangeResponse;
import com.nextstep.nextsiheung.domain.user.dto.ProfileRequest;
import com.nextstep.nextsiheung.domain.user.dto.ProfileResponse;
import com.nextstep.nextsiheung.domain.user.dto.ProfileSaveResponse;
import com.nextstep.nextsiheung.global.exception.ApiErrorCodes;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "상태 등록·온보딩", description = "현재 상황, 관심분야, 희망 채용 조건을 선택형으로 등록한다. 등록하면 로드맵이 바로 만들어진다. 담당: 한동혁 · 6주차")
public interface UserProfileApiDocs {

    @Operation(summary = "상태·관심분야 등록 (온보딩 완료, 1순위)", description = """
            온보딩 3단계에서 고른 값을 한 번에 저장하고, 규칙에 맞는 로드맵 템플릿으로 내 로드맵을 만든다.
            이미 등록했다면 값만 수정하고 로드맵은 그대로 둔다(상황 변경은 별도 API).
            - lifeStages, interests는 1개 이상 필수
            - jobPreference는 생략 가능(맞춤 채용에서 나중에 입력)""")
    @ApiErrorCodes({INVALID_INPUT})
    ApiResponse<ProfileSaveResponse> saveProfile(@Valid ProfileRequest request);

    @Operation(summary = "내 상태·관심분야 조회 (1순위)", description = "메인 상단 요약의 상태 배지·관심분야 칩과 조건 수정 화면에 쓴다.")
    ApiResponse<ProfileResponse> getProfile();

    @Operation(summary = "채용 조건 수정 (2순위)", description = "맞춤 채용 화면의 '조건 수정'에서 호출한다. 보낸 필드만 바뀐다.")
    @ApiErrorCodes({INVALID_INPUT})
    ApiResponse<JobPreferenceDto> updateJobPreference(@Valid JobPreferenceDto request);

    @Operation(summary = "상황 변경 (새 로드맵, 1순위)", description = """
            졸업 예정 → 첫 취업처럼 상황이 바뀌면 기존 로드맵을 활동 기록으로 보관하고 새 로드맵을 만든다. 상황 변경 모달의 확인 버튼이 호출한다.
            - FIRST_JOB으로 바뀌면 suggestReview: true → 화면이 합격 후기 작성을 제안""")
    @ApiErrorCodes({INVALID_INPUT})
    ApiResponse<LifeStageChangeResponse> changeLifeStage(@Valid LifeStageChangeRequest request);
}
