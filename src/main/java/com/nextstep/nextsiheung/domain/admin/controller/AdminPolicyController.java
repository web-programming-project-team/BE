package com.nextstep.nextsiheung.domain.admin.controller;

import com.nextstep.nextsiheung.domain.admin.dto.AdminPolicyRequest;
import com.nextstep.nextsiheung.domain.admin.dto.AdminPolicyResponse;
import com.nextstep.nextsiheung.domain.admin.dto.AdminPolicySaveResponse;
import com.nextstep.nextsiheung.domain.admin.dto.PolicyEndRequest;
import com.nextstep.nextsiheung.domain.admin.dto.PolicyEndResponse;
import com.nextstep.nextsiheung.global.common.enums.PolicySource;
import com.nextstep.nextsiheung.global.common.enums.RecruitStatus;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import com.nextstep.nextsiheung.global.response.PageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/policies")
public class AdminPolicyController implements AdminPolicyApiDocs {

    @Override
    @GetMapping
    public ApiResponse<PageResponse<AdminPolicyResponse>> list(
            @RequestParam(required = false) PolicySource source,
            @RequestParam(required = false) RecruitStatus recruitStatus,
            @RequestParam(required = false) Boolean stale,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AdminPolicySaveResponse> create(@RequestBody AdminPolicyRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @PutMapping("/{policyId}")
    public ApiResponse<AdminPolicySaveResponse> update(@PathVariable Long policyId,
                                                       @RequestBody AdminPolicyRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @PatchMapping("/{policyId}/end")
    public ApiResponse<PolicyEndResponse> end(@PathVariable Long policyId, @RequestBody PolicyEndRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @PatchMapping("/{policyId}/verify")
    public ApiResponse<AdminPolicySaveResponse> verify(@PathVariable Long policyId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
