package com.nextstep.nextsiheung.domain.user.controller;

import com.nextstep.nextsiheung.domain.user.dto.JobPreferenceDto;
import com.nextstep.nextsiheung.domain.user.dto.LifeStageChangeRequest;
import com.nextstep.nextsiheung.domain.user.dto.LifeStageChangeResponse;
import com.nextstep.nextsiheung.domain.user.dto.ProfileRequest;
import com.nextstep.nextsiheung.domain.user.dto.ProfileResponse;
import com.nextstep.nextsiheung.domain.user.dto.ProfileSaveResponse;
import com.nextstep.nextsiheung.domain.user.service.UserProfileService;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import com.nextstep.nextsiheung.global.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me")
public class UserProfileController implements UserProfileApiDocs {

    private final UserProfileService userProfileService;

    @Override
    @PutMapping("/profile")
    public ApiResponse<ProfileSaveResponse> saveProfile(@RequestBody ProfileRequest request) {
        return ApiResponse.ok(userProfileService.saveProfile(CurrentUser.id(), request));
    }

    @Override
    @GetMapping("/profile")
    public ApiResponse<ProfileResponse> getProfile() {
        return ApiResponse.ok(userProfileService.getProfile(CurrentUser.id()));
    }

    @Override
    @PatchMapping("/job-preference")
    public ApiResponse<JobPreferenceDto> updateJobPreference(@RequestBody JobPreferenceDto request) {
        return ApiResponse.ok(userProfileService.updateJobPreference(CurrentUser.id(), request));
    }

    @Override
    @PostMapping("/life-stage-change")
    public ApiResponse<LifeStageChangeResponse> changeLifeStage(@RequestBody LifeStageChangeRequest request) {
        return ApiResponse.ok(userProfileService.changeLifeStage(CurrentUser.id(), request));
    }
}
