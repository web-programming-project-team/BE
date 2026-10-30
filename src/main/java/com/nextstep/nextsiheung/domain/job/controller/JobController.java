package com.nextstep.nextsiheung.domain.job.controller;

import com.nextstep.nextsiheung.domain.job.dto.BookmarkResponse;
import com.nextstep.nextsiheung.domain.job.dto.BookmarkedJobResponse;
import com.nextstep.nextsiheung.domain.job.dto.JobDetailResponse;
import com.nextstep.nextsiheung.domain.job.dto.RecommendedJobsResponse;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import com.nextstep.nextsiheung.global.response.PageResponse;
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
@RequestMapping("/api/v1/jobs")
public class JobController implements JobApiDocs {

    @Override
    @GetMapping("/recommended")
    public ApiResponse<RecommendedJobsResponse> recommended(
            @RequestParam(defaultValue = "deadline") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @GetMapping("/{jobId}")
    public ApiResponse<JobDetailResponse> detail(@PathVariable Long jobId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @PostMapping("/{jobId}/bookmark")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BookmarkResponse> bookmark(@PathVariable Long jobId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @DeleteMapping("/{jobId}/bookmark")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unbookmark(@PathVariable Long jobId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @GetMapping("/bookmarks")
    public ApiResponse<PageResponse<BookmarkedJobResponse>> bookmarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
