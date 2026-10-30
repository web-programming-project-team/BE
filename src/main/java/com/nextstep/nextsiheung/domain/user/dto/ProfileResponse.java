package com.nextstep.nextsiheung.domain.user.dto;

import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.LifeStage;
import java.util.List;

public record ProfileResponse(
        List<LifeStage> lifeStages,
        List<InterestField> interests,
        JobPreferenceDto jobPreference
) {
}
