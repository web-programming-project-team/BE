package com.nextstep.nextsiheung.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

// 개인 식별 정보 없이 집계만 담는다
public record StatsResponse(
        Period period,
        @Schema(description = "기간 안에 가입한 사용자 중 가입 다음 날부터 7일 안에 user_visit 기록이 있는 비율", example = "0.42") double revisitRate7d,
        @Schema(description = "관심 공고 저장 수", example = "57") long bookmarkedJobCount,
        @Schema(description = "후기 작성 수", example = "31") long reviewCount,
        @Schema(description = "프로그램별 관심 등록 수·후기 수 (지역 기관 제공용)") List<TopProgram> topPrograms
) {
    public record Period(
            @Schema(example = "2026-10-19") LocalDate from,
            @Schema(example = "2026-10-25") LocalDate to
    ) {
    }

    public record TopProgram(
            @Schema(example = "12") Long policyId,
            @Schema(example = "청년 직무역량 아카데미") String title,
            @Schema(example = "38") long roadmapItemCount,
            @Schema(example = "7") long reviewCount
    ) {
    }
}
