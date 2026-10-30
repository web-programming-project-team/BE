package com.nextstep.nextsiheung.domain.post.dto;

import com.nextstep.nextsiheung.global.common.enums.PrepPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;

// 보낸 필드만 바뀐다. type은 바꿀 수 없다
public record PostUpdateRequest(
        @Schema(example = "5") @Min(1) @Max(5) Integer rating,
        @Schema(example = "실습 연계 덕분에 첫 취업까지 했어요") @Size(max = 60) String oneLiner,
        PrepPeriod prepPeriod,
        List<String> helpfulPoints,
        String content,
        String portfolioUrl
) {
}
