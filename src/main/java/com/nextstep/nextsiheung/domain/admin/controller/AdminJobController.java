package com.nextstep.nextsiheung.domain.admin.controller;

import com.nextstep.nextsiheung.domain.admin.dto.AdminJobRequest;
import com.nextstep.nextsiheung.domain.admin.dto.AdminJobResponse;
import com.nextstep.nextsiheung.domain.admin.service.AdminJobService;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import com.nextstep.nextsiheung.global.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/jobs")
public class AdminJobController implements AdminJobApiDocs {

    private final AdminJobService adminJobService;

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AdminJobResponse> create(@RequestBody AdminJobRequest request) {
        return ApiResponse.ok(adminJobService.create(CurrentUser.id(), request));
    }

    @Override
    @PutMapping("/{jobId}")
    public ApiResponse<AdminJobResponse> update(@PathVariable Long jobId, @RequestBody AdminJobRequest request) {
        return ApiResponse.ok(adminJobService.update(jobId, request));
    }

    @Override
    @PatchMapping("/{jobId}/close")
    public ApiResponse<AdminJobResponse> close(@PathVariable Long jobId) {
        return ApiResponse.ok(adminJobService.close(jobId));
    }
}
