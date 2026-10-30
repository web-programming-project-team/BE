package com.nextstep.nextsiheung.domain.admin.controller;

import com.nextstep.nextsiheung.domain.admin.dto.StatsResponse;
import com.nextstep.nextsiheung.domain.admin.dto.SyncLogResponse;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import com.nextstep.nextsiheung.global.response.PageResponse;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminStatsController implements AdminStatsApiDocs {

    @Override
    @GetMapping("/stats")
    public ApiResponse<StatsResponse> stats(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @GetMapping("/sync/logs")
    public ApiResponse<PageResponse<SyncLogResponse>> syncLogs(
            @RequestParam(required = false) String source,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
