package com.nextstep.nextsiheung.domain.user.dto;

import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.LifeStage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record LifeStageChangeRequest(
        @Schema(example = "[\"FIRST_JOB\"]") @NotEmpty List<LifeStage> lifeStages,
        @Schema(example = "[\"HOUSING\", \"FINANCE\"]") @NotEmpty List<InterestField> interests
) {
}
