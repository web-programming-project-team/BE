package com.nextstep.nextsiheung.domain.user.controller;

import com.nextstep.nextsiheung.domain.user.dto.JobPreferenceDto;
import com.nextstep.nextsiheung.domain.user.dto.LifeStageChangeRequest;
import com.nextstep.nextsiheung.domain.user.dto.LifeStageChangeResponse;
import com.nextstep.nextsiheung.domain.user.dto.ProfileRequest;
import com.nextstep.nextsiheung.domain.user.dto.ProfileResponse;
import com.nextstep.nextsiheung.domain.user.dto.ProfileSaveResponse;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
public class UserProfileController implements UserProfileApiDocs {

    @Override
    @PutMapping("/profile")
    public ApiResponse<ProfileSaveResponse> saveProfile(@RequestBody ProfileRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @GetMapping("/profile")
    public ApiResponse<ProfileResponse> getProfile() {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @PatchMapping("/job-preference")
    public ApiResponse<JobPreferenceDto> updateJobPreference(@RequestBody JobPreferenceDto request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @PostMapping("/life-stage-change")
    public ApiResponse<LifeStageChangeResponse> changeLifeStage(@RequestBody LifeStageChangeRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
