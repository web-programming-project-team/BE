package com.nextstep.nextsiheung.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// 게시글에 태그된 정책
public record PolicyRef(
        @Schema(example = "12") Long policyId,
        @Schema(example = "청년 직무역량 아카데미") String title
) {
}
