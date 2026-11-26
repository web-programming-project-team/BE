package com.nextstep.nextsiheung.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostIdResponse(
        @Schema(example = "341") Long postId
) {
}
