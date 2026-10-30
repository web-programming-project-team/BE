package com.nextstep.nextsiheung.domain.post.dto;

import com.nextstep.nextsiheung.global.common.enums.PostType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record MyPostResponse(
        @Schema(example = "301") Long postId,
        PostType type,
        @Schema(example = "실습 연계까지 돼서 좋았어요") String oneLiner,
        PolicyRef policy,
        @Schema(description = "숨김 처리된 글도 본인에게는 보여준다", example = "false") boolean hidden,
        @Schema(example = "2026-09-30T20:11:00") LocalDateTime createdAt
) {
}
