package com.nextstep.nextsiheung.domain.policy.controller;

import com.nextstep.nextsiheung.domain.policy.dto.PolicyDetailResponse;
import com.nextstep.nextsiheung.domain.policy.dto.PolicySummaryResponse;
import com.nextstep.nextsiheung.domain.post.dto.PostSummaryResponse;
import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.RecruitStatus;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import com.nextstep.nextsiheung.global.response.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController implements PolicyApiDocs {

    @Override
    @GetMapping
    public ApiResponse<PageResponse<PolicySummaryResponse>> list(
            @RequestParam(required = false) InterestField field,
            @RequestParam(required = false) RecruitStatus recruitStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "deadline") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @GetMapping("/{policyId}")
    public ApiResponse<PolicyDetailResponse> detail(@PathVariable Long policyId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @GetMapping("/{policyId}/reviews")
    public ApiResponse<PageResponse<PostSummaryResponse>> reviews(
            @PathVariable Long policyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
