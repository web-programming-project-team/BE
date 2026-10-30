package com.nextstep.nextsiheung.domain.admin.controller;

import com.nextstep.nextsiheung.domain.admin.dto.PostVisibilityRequest;
import com.nextstep.nextsiheung.domain.admin.dto.PostVisibilityResponse;
import com.nextstep.nextsiheung.domain.admin.dto.ReportGroupResponse;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import com.nextstep.nextsiheung.global.response.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminReportController implements AdminReportApiDocs {

    @Override
    @GetMapping("/reports")
    public ApiResponse<PageResponse<ReportGroupResponse>> reports(
            @RequestParam(defaultValue = "PENDING") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @PatchMapping("/posts/{postId}/visibility")
    public ApiResponse<PostVisibilityResponse> changeVisibility(@PathVariable Long postId,
                                                                @RequestBody PostVisibilityRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
