package com.nextstep.nextsiheung.domain.post.dto;

import com.nextstep.nextsiheung.global.common.enums.JobCategory;
import com.nextstep.nextsiheung.global.common.enums.LifeStage;
import com.nextstep.nextsiheung.global.common.enums.PostType;
import com.nextstep.nextsiheung.global.common.enums.PrepPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        @Schema(example = "301") Long postId,
        PostType type,
        @Schema(example = "5") Integer rating,
        @Schema(example = "실습 연계까지 돼서 좋았어요") String oneLiner,
        PrepPeriod prepPeriod,
        @Schema(example = "[\"실무 경험\", \"이력서 첨삭\"]") List<String> helpfulPoints,
        @Schema(description = "자유 의견 본문", example = "8주 동안 엑셀, 문서작성 위주로 배웠고...") String content,
        @Schema(description = "포트폴리오 링크") String portfolioUrl,
        PolicyRef policy,
        JobRef job,
        JobCategory jobCategory,
        LifeStage authorStage,
        LifeStage authorStageAfter,
        @Schema(example = "12") long likeCount,
        @Schema(example = "false") boolean liked,
        @Schema(description = "내가 쓴 글인지", example = "false") boolean mine,
        @Schema(example = "2026-09-30T20:11:00") LocalDateTime createdAt
) {
}
