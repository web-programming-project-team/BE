package com.nextstep.nextsiheung.domain.admin.controller;

import com.nextstep.nextsiheung.domain.admin.dto.StatsResponse;
import com.nextstep.nextsiheung.domain.admin.dto.SyncLogResponse;
import com.nextstep.nextsiheung.domain.admin.service.AdminStatsService;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import com.nextstep.nextsiheung.global.response.PageResponse;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminStatsController implements AdminStatsApiDocs {

    private final AdminStatsService adminStatsService;

    @Override
    @GetMapping("/stats")
    public ApiResponse<StatsResponse> stats(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return ApiResponse.ok(adminStatsService.stats(from, to));
    }

    @Override
    @GetMapping("/sync/logs")
    public ApiResponse<PageResponse<SyncLogResponse>> syncLogs(
            @RequestParam(required = false) String source,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(adminStatsService.syncLogs(source, page, size));
    }
}
