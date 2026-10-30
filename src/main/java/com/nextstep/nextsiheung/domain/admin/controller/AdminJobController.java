package com.nextstep.nextsiheung.domain.admin.controller;

import com.nextstep.nextsiheung.domain.admin.dto.AdminJobRequest;
import com.nextstep.nextsiheung.domain.admin.dto.AdminJobResponse;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import com.nextstep.nextsiheung.global.response.ApiResponse;
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
@RequestMapping("/api/v1/admin/jobs")
public class AdminJobController implements AdminJobApiDocs {

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AdminJobResponse> create(@RequestBody AdminJobRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @PutMapping("/{jobId}")
    public ApiResponse<AdminJobResponse> update(@PathVariable Long jobId, @RequestBody AdminJobRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @PatchMapping("/{jobId}/close")
    public ApiResponse<AdminJobResponse> close(@PathVariable Long jobId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
