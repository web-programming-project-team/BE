package com.nextstep.nextsiheung.domain.post.dto;

import com.nextstep.nextsiheung.global.common.enums.JobCategory;
import com.nextstep.nextsiheung.global.common.enums.LifeStage;
import com.nextstep.nextsiheung.global.common.enums.PostType;
import com.nextstep.nextsiheung.global.common.enums.PrepPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

// 커뮤니티 목록, 정책 상세의 '후기 더 보기'가 같이 쓴다. 작성자 닉네임·실명은 내려주지 않는다
public record PostSummaryResponse(
        @Schema(example = "301") Long postId,
        PostType type,
        @Schema(example = "5") Integer rating,
        @Schema(example = "실습 연계까지 돼서 좋았어요") String oneLiner,
        PrepPeriod prepPeriod,
        @Schema(example = "[\"실무 경험\", \"이력서 첨삭\"]") List<String> helpfulPoints,
        PolicyRef policy,
        JobRef job,
        JobCategory jobCategory,
        @Schema(description = "작성 당시 상황") LifeStage authorStage,
        @Schema(description = "참여·합격 후 상황") LifeStage authorStageAfter,
        @Schema(example = "12") long likeCount,
        @Schema(description = "비로그인이면 false", example = "false") boolean liked,
        @Schema(example = "2026-09-30T20:11:00") LocalDateTime createdAt
) {
}
