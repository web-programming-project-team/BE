package com.nextstep.nextsiheung.domain.calendar.controller;

import static com.nextstep.nextsiheung.global.exception.ErrorCode.EVENT_NOT_FOUND;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.FORBIDDEN;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.INVALID_INPUT;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.POLICY_NOT_FOUND;

import com.nextstep.nextsiheung.domain.calendar.dto.CalendarEventCreateRequest;
import com.nextstep.nextsiheung.domain.calendar.dto.CalendarEventResponse;
import com.nextstep.nextsiheung.domain.calendar.dto.EventIdResponse;
import com.nextstep.nextsiheung.global.common.enums.EventType;
import com.nextstep.nextsiheung.global.exception.ApiErrorCodes;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

@Tag(name = "NEXT 캘린더", description = "로드맵 항목과 관심 공고의 일정을 한 캘린더로 모은다. 담당: 한동혁 · 9주차")
public interface CalendarApiDocs {

    @Operation(summary = "월별 일정 (2순위)", description = """
            월간 캘린더에 찍을 일정을 돌려준다. 로드맵 항목과 관심 공고에서 자동으로 생긴 일정, 직접 추가한 일정이 모두 포함된다.
            - eventTime은 HH:mm, 시간이 없는 일정(접수 마감 등)은 null""")
    @ApiErrorCodes({INVALID_INPUT})
    ApiResponse<List<CalendarEventResponse>> monthly(
            @Parameter(description = "예: 2026", required = true) int year,
            @Parameter(description = "1~12", required = true) @Min(1) @Max(12) int month,
            @Parameter(description = "일정 종류 필터, 쉼표 구분") List<EventType> types);

    @Operation(summary = "다가오는 일정 (2순위)", description = "캘린더 오른쪽 패널의 '이번 주 마감'에 쓴다.")
    ApiResponse<List<CalendarEventResponse>> upcoming(@Parameter(description = "기본 7, 최대 30") @Min(1) @Max(30) int days);

    @Operation(summary = "일정 추가 (2순위)", description = """
            정책 상세의 '캘린더에 추가'나 직접 입력한 일정을 저장한다. policyId를 보내면 그 정책의 상담일·참여일을, 없으면 title과 date로 CUSTOM 일정을 만든다.
            - eventTime은 선택(HH:mm). 생략하면 종일 일정""")
    @ApiErrorCodes({INVALID_INPUT, POLICY_NOT_FOUND})
    ApiResponse<EventIdResponse> create(@Valid CalendarEventCreateRequest request);

    @Operation(summary = "일정 삭제 (2순위)", description = "직접 추가한 일정만 지울 수 있다. 자동 일정은 로드맵 항목이나 관심 공고를 지우면 함께 사라진다.")
    @ApiErrorCodes({EVENT_NOT_FOUND, FORBIDDEN})
    void delete(@Parameter(description = "일정 ID") Long eventId);
}
