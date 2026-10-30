package com.nextstep.nextsiheung.domain.policy.dto;

import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.LifeStage;
import com.nextstep.nextsiheung.global.common.enums.PolicySource;
import com.nextstep.nextsiheung.global.common.enums.PostType;
import com.nextstep.nextsiheung.global.common.enums.PrepPeriod;
import com.nextstep.nextsiheung.global.common.enums.ProgressStatus;
import com.nextstep.nextsiheung.global.common.enums.RecruitStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// 상세 화면 한 장에 필요한 데이터 전부. 신청은 서비스 안에서 하지 않으므로 applyUrl과 lastVerifiedAt이 항상 온다
public record PolicyDetailResponse(
        @Schema(example = "12") Long policyId,
        @Schema(example = "청년 직무역량 아카데미") String title,
        InterestField field,
        @Schema(example = "시흥시청소년청년재단") String agency,
        @Schema(example = "시흥시 거주 19~24세 미취업 청년") String target,
        @Schema(example = "직무별 실무 교육 8주 과정, 수료 시 현장실습 연계") String content,
        @Schema(example = "2026-10-20") LocalDate applyStartDate,
        @Schema(example = "2026-11-05") LocalDate applyEndDate,
        @Schema(example = "11") Integer dDay,
        RecruitStatus recruitStatus,
        @Schema(example = "031-000-0000") String contact,
        PolicySource source,
        @Schema(description = "공식 출처", example = "https://...") String sourceUrl,
        @Schema(description = "공식 신청 사이트", example = "https://...") String applyUrl,
        @Schema(description = "마지막 정보 확인일", example = "2026-10-20") LocalDate lastVerifiedAt,
        @Schema(description = "비로그인이거나 로드맵에 없으면 null → 상태 드롭다운 선택 시 POST /roadmaps/me/items 호출") MyItem myItem,
        @Schema(description = "관련 후기 최신 3개") List<ReviewSummary> reviews,
        @Schema(example = "7") long reviewCount,
        @Schema(description = "연결 채용공고 최대 2개") List<RelatedJob> relatedJobs
) {
    public record MyItem(
            @Schema(example = "205") Long itemId,
            ProgressStatus status
    ) {
    }

    public record ReviewSummary(
            @Schema(example = "301") Long postId,
            PostType type,
            @Schema(example = "5") Integer rating,
            @Schema(example = "실습 연계까지 돼서 좋았어요") String oneLiner,
            PrepPeriod prepPeriod,
            LifeStage authorStage,
            @Schema(example = "2026-09-30T20:11:00") LocalDateTime createdAt
    ) {
    }

    public record RelatedJob(
            @Schema(example = "5512") Long jobId,
            @Schema(example = "(주)시흥테크") String company,
            @Schema(example = "사무직 신입") String title,
            @Schema(example = "7") Integer dDay
    ) {
    }
}
