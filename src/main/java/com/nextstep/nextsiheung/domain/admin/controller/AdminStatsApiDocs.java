package com.nextstep.nextsiheung.domain.admin.controller;

import com.nextstep.nextsiheung.domain.admin.dto.StatsResponse;
import com.nextstep.nextsiheung.domain.admin.dto.SyncLogResponse;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import com.nextstep.nextsiheung.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;

@Tag(name = "관리자")
public interface AdminStatsApiDocs {

    @Operation(summary = "운영 지표 (2순위)", description = """
            제안서 8-3 운영 지표와 8-2 데이터 활용에 대응한다. 개인 식별 정보 없이 집계만 돌려준다.
            - revisitRate7d: 기간 안에 가입한 사용자 중 가입 다음 날부터 7일 안에 user_visit 기록이 있는 비율
            - topPrograms: 프로그램별 관심 등록 수·후기 수 (지역 기관 제공용)""")
    ApiResponse<StatsResponse> stats(
            @Parameter(description = "시작일, 기본 최근 7일") LocalDate from,
            @Parameter(description = "종료일") LocalDate to);

    @Operation(summary = "수집 기록 (2순위)", description = "스케줄러 자동 수집 결과를 최신순으로 보여준다(ERD sync_log). 수동 수집 API는 없다.")
    ApiResponse<PageResponse<SyncLogResponse>> syncLogs(
            @Parameter(description = "ONTONG · SARAMIN") String source,
            @Parameter(description = "페이지 (0부터)") @Min(0) int page,
            @Parameter(description = "페이지 크기 (최대 50)") @Min(1) @Max(50) int size);
}
