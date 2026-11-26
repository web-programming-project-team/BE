package com.nextstep.nextsiheung.domain.roadmap.controller;

import static com.nextstep.nextsiheung.global.exception.ErrorCode.ALREADY_EXISTS;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.FORBIDDEN;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.ONBOARDING_REQUIRED;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.POLICY_ENDED;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.POLICY_NOT_FOUND;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.ROADMAP_ITEM_NOT_FOUND;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.ROADMAP_NOT_FOUND;

import com.nextstep.nextsiheung.domain.roadmap.dto.HomeSummaryResponse;
import com.nextstep.nextsiheung.domain.roadmap.dto.RoadmapHistoryResponse;
import com.nextstep.nextsiheung.domain.roadmap.dto.RoadmapItemAddRequest;
import com.nextstep.nextsiheung.domain.roadmap.dto.RoadmapItemAddResponse;
import com.nextstep.nextsiheung.domain.roadmap.dto.RoadmapResponse;
import com.nextstep.nextsiheung.domain.roadmap.dto.StatusChangeRequest;
import com.nextstep.nextsiheung.domain.roadmap.dto.StatusChangeResponse;
import com.nextstep.nextsiheung.global.exception.ApiErrorCodes;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "로드맵·진행상태", description = "메인 화면 데이터와 로드맵 항목의 6단계 진행상태. 담당: 강준(로드맵 8주차) · 한동혁(진행상태 9주차)")
public interface RoadmapApiDocs {

    @Operation(summary = "메인 요약: 다음 할 일 · 다가오는 마감 (1순위)", description = "내 로드맵 화면의 상단 요약 카드와 오른쪽 패널을 한 번에 채운다. 메인에는 이 두 가지만 보여준다는 설계 원칙에 맞춘 API.")
    @ApiErrorCodes({ONBOARDING_REQUIRED})
    ApiResponse<HomeSummaryResponse> homeSummary();

    @Operation(summary = "내 로드맵 조회 (1순위)", description = "세로 타임라인에 그릴 단계와 단계별 정책 카드를 돌려준다. currentStep은 미완료 항목이 있는 첫 단계.")
    @ApiErrorCodes({ONBOARDING_REQUIRED})
    ApiResponse<RoadmapResponse> myRoadmap();

    @Operation(summary = "진행상태 변경 (1순위)", description = """
            진행상태 배지 드롭다운에서 호출한다. 바뀐 진행률을 같이 돌려주므로 화면은 다시 조회하지 않아도 된다.
            - 본인 로드맵 항목만 변경 가능
            - 단계를 건너뛰거나 되돌리는 것도 허용
            - suggestReview는 COMPLETED로 바뀔 때만 true""")
    @ApiErrorCodes({ROADMAP_ITEM_NOT_FOUND, FORBIDDEN})
    ApiResponse<StatusChangeResponse> changeStatus(@Parameter(description = "로드맵 항목 ID") Long itemId,
                                                   @Valid StatusChangeRequest request);

    @Operation(summary = "로드맵에 정책 추가 (관심 등록, 1순위)", description = """
            제안서 3-1 '프로그램을 개인 로드맵에 추가'. 정책 상세에서 로드맵에 없는 정책의 진행상태를 처음 고를 때 호출한다.
            템플릿 단계 중 정책 분야가 맞는 단계에 들어가고, 접수 시작·마감일이 캘린더에 자동 등록된다.""")
    @ApiErrorCodes({POLICY_NOT_FOUND, ALREADY_EXISTS, POLICY_ENDED})
    ApiResponse<RoadmapItemAddResponse> addItem(@Valid RoadmapItemAddRequest request);

    @Operation(summary = "로드맵 항목 삭제 (3순위)", description = "직접 추가한 항목만 뺀다. 제안서에서 '로드맵 직접 편집'은 3순위라 2순위까지 끝난 뒤 구현한다. 연결된 캘린더 일정도 함께 지운다.")
    @ApiErrorCodes({ROADMAP_ITEM_NOT_FOUND, FORBIDDEN})
    void deleteItem(@Parameter(description = "로드맵 항목 ID") Long itemId);

    @Operation(summary = "지난 로드맵 목록 (활동 기록, 2순위)", description = "상황 변경으로 보관된 로드맵 목록. 상세는 GET /roadmaps/{roadmapId}.")
    ApiResponse<List<RoadmapHistoryResponse>> history();

    @Operation(summary = "지난 로드맵 상세 (2순위)", description = """
            활동 기록에서 보관된 로드맵 하나를 연다. 응답 형식은 GET /roadmaps/me와 같고 archived, archivedAt이 더해진다. 보관된 로드맵은 읽기 전용.
            - 본인 로드맵만 조회 가능
            - 현재 로드맵 ID를 넣어도 같은 형식으로 응답(archived: false)""")
    @ApiErrorCodes({ROADMAP_NOT_FOUND, FORBIDDEN})
    ApiResponse<RoadmapResponse> roadmapDetail(@Parameter(description = "로드맵 ID") Long roadmapId);
}
