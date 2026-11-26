package com.nextstep.nextsiheung.domain.roadmap.dto;

import com.nextstep.nextsiheung.global.common.enums.EventType;
import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.LifeStage;
import com.nextstep.nextsiheung.global.common.enums.ProgressStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

public record HomeSummaryResponse(
        @Schema(example = "A") String nickname,
        List<LifeStage> lifeStages,
        List<InterestField> interests,
        ProgressDto progress,
        @Schema(description = "최대 3개: 마감이 가까운 PLANNED → INTERESTED → TO_CHECK 순") List<NextAction> nextActions,
        @Schema(description = "최대 5개, dDay 0~14 범위") List<UpcomingDeadline> upcomingDeadlines,
        @Schema(example = "3") int newJobCount,
        @Schema(example = "4") int unreadNotificationCount
) {
    public record NextAction(
            @Schema(example = "205") Long itemId,
            @Schema(example = "12") Long policyId,
            @Schema(example = "청년 직무역량 아카데미") String title,
            ProgressStatus status,
            @Schema(example = "공식 사이트에서 신청하기") String actionLabel,
            @Schema(description = "마감까지 남은 일수, 마감일 없으면 null", example = "11") Integer dDay
    ) {
    }

    public record UpcomingDeadline(
            EventType type,
            @Schema(example = "청년 자격증 응시료 지원") String title,
            @Schema(example = "2026-10-28") LocalDate date,
            @Schema(example = "3") Integer dDay,
            @Schema(description = "POLICY · JOB", example = "POLICY") String sourceType,
            @Schema(example = "21") Long sourceId
    ) {
    }
}
