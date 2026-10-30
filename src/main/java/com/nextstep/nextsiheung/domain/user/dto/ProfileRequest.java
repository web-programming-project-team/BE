package com.nextstep.nextsiheung.domain.user.dto;

import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.LifeStage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ProfileRequest(
        @Schema(example = "[\"GRADUATING\", \"JOB_SEEKING\"]") @NotEmpty List<LifeStage> lifeStages,
        @Schema(example = "[\"EMPLOYMENT\", \"HOUSING\"]") @NotEmpty List<InterestField> interests,
        @Schema(description = "생략 가능(맞춤 채용에서 나중에 입력)") @Valid JobPreferenceDto jobPreference
) {
}
