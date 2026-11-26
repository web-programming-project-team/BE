package com.nextstep.nextsiheung.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LikeResponse(
        @Schema(example = "301") Long postId,
        @Schema(example = "true") boolean liked,
        @Schema(example = "13") long likeCount
) {
}
