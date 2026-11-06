package com.nextstep.nextsiheung.domain.job.controller;

import com.nextstep.nextsiheung.domain.job.dto.BookmarkResponse;
import com.nextstep.nextsiheung.domain.job.dto.BookmarkedJobResponse;
import com.nextstep.nextsiheung.domain.job.dto.JobDetailResponse;
import com.nextstep.nextsiheung.domain.job.dto.RecommendedJobsResponse;
import com.nextstep.nextsiheung.domain.job.service.JobService;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import com.nextstep.nextsiheung.global.response.PageResponse;
import com.nextstep.nextsiheung.global.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/jobs")
public class JobController implements JobApiDocs {

    private final JobService jobService;

    @Override
    @GetMapping("/recommended")
    public ApiResponse<RecommendedJobsResponse> recommended(
            @RequestParam(defaultValue = "deadline") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(jobService.recommended(CurrentUser.id(), sort, page, size));
    }

    @Override
    @GetMapping("/{jobId}")
    public ApiResponse<JobDetailResponse> detail(@PathVariable Long jobId) {
        return ApiResponse.ok(jobService.detail(CurrentUser.idOrNull(), jobId));
    }

    @Override
    @PostMapping("/{jobId}/bookmark")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BookmarkResponse> bookmark(@PathVariable Long jobId) {
        return ApiResponse.ok(jobService.bookmark(CurrentUser.id(), jobId));
    }

    @Override
    @DeleteMapping("/{jobId}/bookmark")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unbookmark(@PathVariable Long jobId) {
        jobService.unbookmark(CurrentUser.id(), jobId);
    }

    @Override
    @GetMapping("/bookmarks")
    public ApiResponse<PageResponse<BookmarkedJobResponse>> bookmarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(jobService.bookmarks(CurrentUser.id(), page, size));
    }
}
