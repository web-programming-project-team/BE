package com.nextstep.nextsiheung.domain.post.controller;

import com.nextstep.nextsiheung.domain.post.dto.LikeResponse;
import com.nextstep.nextsiheung.domain.post.dto.MyPostResponse;
import com.nextstep.nextsiheung.domain.post.dto.PostCreateRequest;
import com.nextstep.nextsiheung.domain.post.dto.PostDetailResponse;
import com.nextstep.nextsiheung.domain.post.dto.PostIdResponse;
import com.nextstep.nextsiheung.domain.post.dto.PostSummaryResponse;
import com.nextstep.nextsiheung.domain.post.dto.PostUpdateRequest;
import com.nextstep.nextsiheung.domain.post.dto.ReportRequest;
import com.nextstep.nextsiheung.domain.post.dto.ReportResponse;
import com.nextstep.nextsiheung.domain.post.service.PostLikeService;
import com.nextstep.nextsiheung.domain.post.service.PostReportService;
import com.nextstep.nextsiheung.domain.post.service.PostService;
import com.nextstep.nextsiheung.domain.post.service.PostSort;
import com.nextstep.nextsiheung.global.common.enums.JobCategory;
import com.nextstep.nextsiheung.global.common.enums.PostType;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import com.nextstep.nextsiheung.global.response.PageResponse;
import com.nextstep.nextsiheung.global.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PostController implements PostApiDocs {

    private final PostService postService;
    private final PostLikeService postLikeService;
    private final PostReportService postReportService;

    @Override
    @GetMapping("/posts")
    public ApiResponse<PageResponse<PostSummaryResponse>> list(
            @RequestParam(required = false) PostType type,
            @RequestParam(required = false) Long policyId,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) JobCategory jobCategory,
            @RequestParam(required = false) Boolean sameStage,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(postService.list(type, policyId, jobId, jobCategory, Boolean.TRUE.equals(sameStage),
                PostSort.from(sort), CurrentUser.idOrNull(), page, size));
    }

    @Override
    @GetMapping("/posts/{postId}")
    public ApiResponse<PostDetailResponse> detail(@PathVariable Long postId) {
        return ApiResponse.ok(postService.detail(postId, CurrentUser.idOrNull()));
    }

    @Override
    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostIdResponse> create(@RequestBody PostCreateRequest request) {
        return ApiResponse.ok(postService.create(CurrentUser.id(), request));
    }

    @Override
    @PatchMapping("/posts/{postId}")
    public ApiResponse<PostIdResponse> update(@PathVariable Long postId, @RequestBody PostUpdateRequest request) {
        return ApiResponse.ok(postService.update(CurrentUser.id(), postId, request));
    }

    @Override
    @DeleteMapping("/posts/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long postId) {
        postService.delete(CurrentUser.id(), postId);
    }

    @Override
    @GetMapping("/users/me/posts")
    public ApiResponse<PageResponse<MyPostResponse>> myPosts(
            @RequestParam(required = false) PostType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(postService.myPosts(CurrentUser.id(), type, page, size));
    }

    @Override
    @PostMapping("/posts/{postId}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReportResponse> report(@PathVariable Long postId, @RequestBody ReportRequest request) {
        return ApiResponse.ok(postReportService.report(CurrentUser.id(), postId, request));
    }

    @Override
    @PostMapping("/posts/{postId}/likes")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LikeResponse> like(@PathVariable Long postId) {
        return ApiResponse.ok(postLikeService.like(CurrentUser.id(), postId));
    }

    @Override
    @DeleteMapping("/posts/{postId}/likes")
    public ApiResponse<LikeResponse> unlike(@PathVariable Long postId) {
        return ApiResponse.ok(postLikeService.unlike(CurrentUser.id(), postId));
    }
}
