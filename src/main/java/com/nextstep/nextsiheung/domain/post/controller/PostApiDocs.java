package com.nextstep.nextsiheung.domain.post.controller;

import static com.nextstep.nextsiheung.global.exception.ErrorCode.ALREADY_EXISTS;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.CONTACT_INFO_DETECTED;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.FORBIDDEN;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.INVALID_INPUT;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.JOB_NOT_FOUND;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.POLICY_NOT_FOUND;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.POST_NOT_FOUND;

import com.nextstep.nextsiheung.domain.post.dto.LikeResponse;
import com.nextstep.nextsiheung.domain.post.dto.MyPostResponse;
import com.nextstep.nextsiheung.domain.post.dto.PostCreateRequest;
import com.nextstep.nextsiheung.domain.post.dto.PostDetailResponse;
import com.nextstep.nextsiheung.domain.post.dto.PostIdResponse;
import com.nextstep.nextsiheung.domain.post.dto.PostSummaryResponse;
import com.nextstep.nextsiheung.domain.post.dto.PostUpdateRequest;
import com.nextstep.nextsiheung.domain.post.dto.ReportRequest;
import com.nextstep.nextsiheung.domain.post.dto.ReportResponse;
import com.nextstep.nextsiheung.global.common.enums.JobCategory;
import com.nextstep.nextsiheung.global.common.enums.PostType;
import com.nextstep.nextsiheung.global.exception.ApiErrorCodes;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import com.nextstep.nextsiheung.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Tag(name = "커뮤니티", description = "합격 후기, 참여 후기, 포트폴리오. 글은 정책·공고·직무 태그로 연결된다. 담당: 한동혁 · 11주차")
public interface PostApiDocs {

    @Operation(summary = "후기·포트폴리오 목록 (2순위, 로그인 선택)", description = """
            커뮤니티 탭과 필터에 대응한다. 최신순.
            - 작성자 닉네임·실명은 내려주지 않음. 상황(authorStage)만 표시
            - liked는 비로그인이면 false""")
    ApiResponse<PageResponse<PostSummaryResponse>> list(
            @Parameter(description = "탭") PostType type,
            @Parameter(description = "정책 태그") Long policyId,
            @Parameter(description = "공고 태그") Long jobId,
            @Parameter(description = "직무 태그") JobCategory jobCategory,
            @Parameter(description = "true면 내 상황과 같은 작성자의 글만 (로그인 필요)") Boolean sameStage,
            @Parameter(description = "페이지 (0부터)") @Min(0) int page,
            @Parameter(description = "페이지 크기 (최대 50)") @Min(1) @Max(50) int size);

    @Operation(summary = "게시글 상세 (2순위, 로그인 선택)", description = "목록 항목에 자유 의견 본문, 포트폴리오 링크, 내가 쓴 글인지(mine)를 더해 준다.")
    @ApiErrorCodes({POST_NOT_FOUND})
    ApiResponse<PostDetailResponse> detail(@Parameter(description = "게시글 ID") Long postId);

    @Operation(summary = "1분 후기 작성 (2순위)", description = """
            선택형 위주의 1분 후기 양식을 저장한다. 저장 후 연결된 정책·공고에 관심을 둔 사용자에게 새 후기 알림이 간다.
            - type, rating(1~5), oneLiner(최대 60자), prepPeriod 필수
            - PROGRAM_REVIEW는 policyId, PASS_REVIEW는 jobId 또는 jobCategory 중 하나 필수
            - PORTFOLIO는 portfolioUrl(링크) 필수, rating 생략 가능. 파일 첨부·피드백 댓글은 3순위라 제외
            - 본문에 전화번호·이메일 패턴이 있으면 CONTACT_INFO_DETECTED""")
    @ApiErrorCodes({INVALID_INPUT, CONTACT_INFO_DETECTED, POLICY_NOT_FOUND, JOB_NOT_FOUND})
    ApiResponse<PostIdResponse> create(@Valid PostCreateRequest request);

    @Operation(summary = "게시글 수정 (2순위)", description = "작성자만 수정할 수 있다. 보낸 필드만 바뀌고, type은 바꿀 수 없다.")
    @ApiErrorCodes({POST_NOT_FOUND, FORBIDDEN, CONTACT_INFO_DETECTED})
    ApiResponse<PostIdResponse> update(@Parameter(description = "게시글 ID") Long postId, @Valid PostUpdateRequest request);

    @Operation(summary = "게시글 삭제 (2순위)", description = "작성자만 삭제할 수 있다. 소프트 삭제(deleted_at)로 처리한다.")
    @ApiErrorCodes({POST_NOT_FOUND, FORBIDDEN})
    void delete(@Parameter(description = "게시글 ID") Long postId);

    @Operation(summary = "내가 쓴 후기·포트폴리오 (2순위)", description = "제안서 3-3 '내 기록'(내가 남긴 후기). 숨김 처리된 글도 본인에게는 hidden: true로 보여준다.")
    ApiResponse<PageResponse<MyPostResponse>> myPosts(
            @Parameter(description = "종류") PostType type,
            @Parameter(description = "페이지 (0부터)") @Min(0) int page,
            @Parameter(description = "페이지 크기 (최대 50)") @Min(1) @Max(50) int size);

    @Operation(summary = "게시글 신고 (2순위)", description = "한 사람이 같은 글을 한 번만 신고할 수 있다. 신고가 5건 쌓이면 자동으로 숨기고 관리자 검토 목록에 올린다.")
    @ApiErrorCodes({ALREADY_EXISTS, POST_NOT_FOUND})
    ApiResponse<ReportResponse> report(@Parameter(description = "게시글 ID") Long postId, @Valid ReportRequest request);

    @Operation(summary = "좋아요 (2순위)", description = "게시글에 좋아요를 누른다. 한 사람이 한 글에 한 번만 누를 수 있다(ERD post_like PK: post_id + user_id).")
    @ApiErrorCodes({ALREADY_EXISTS, POST_NOT_FOUND})
    ApiResponse<LikeResponse> like(@Parameter(description = "게시글 ID") Long postId);

    @Operation(summary = "좋아요 취소 (2순위)", description = "내가 누른 좋아요를 취소한다. 누르지 않은 글이면 아무것도 바뀌지 않고 같은 응답을 준다.")
    @ApiErrorCodes({POST_NOT_FOUND})
    ApiResponse<LikeResponse> unlike(@Parameter(description = "게시글 ID") Long postId);
}
