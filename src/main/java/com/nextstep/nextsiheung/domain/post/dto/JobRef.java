package com.nextstep.nextsiheung.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// 게시글에 태그된 채용공고
public record JobRef(
        @Schema(example = "5512") Long jobId,
        @Schema(example = "(주)시흥테크") String company,
        @Schema(example = "사무직 신입 채용") String title
) {
}
