package com.nextstep.nextsiheung.domain.post.service;

import com.nextstep.nextsiheung.domain.job.entity.JobPosting;
import com.nextstep.nextsiheung.domain.job.repository.JobPostingRepository;
import com.nextstep.nextsiheung.domain.notification.service.NewReviewNotificationService;
import com.nextstep.nextsiheung.domain.policy.repository.PolicyRepository;
import com.nextstep.nextsiheung.domain.post.dto.MyPostResponse;
import com.nextstep.nextsiheung.domain.post.dto.PostCreateRequest;
import com.nextstep.nextsiheung.domain.post.dto.PostDetailResponse;
import com.nextstep.nextsiheung.domain.post.dto.PostIdResponse;
import com.nextstep.nextsiheung.domain.post.dto.PostSummaryResponse;
import com.nextstep.nextsiheung.domain.post.dto.PostUpdateRequest;
import com.nextstep.nextsiheung.domain.post.entity.Post;
import com.nextstep.nextsiheung.domain.post.repository.PostRepository;
import com.nextstep.nextsiheung.domain.user.entity.User;
import com.nextstep.nextsiheung.domain.user.repository.UserRepository;
import com.nextstep.nextsiheung.global.common.enums.JobCategory;
import com.nextstep.nextsiheung.global.common.enums.LifeStage;
import com.nextstep.nextsiheung.global.common.enums.PostType;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import com.nextstep.nextsiheung.global.response.PageResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 커뮤니티 게시글 작성·조회·수정·삭제
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final int MAX_HELPFUL_POINTS = 10;
    private static final int MAX_HELPFUL_POINT_LENGTH = 30;
    private static final int MAX_CONTENT_LENGTH = 2000;
    private static final int MAX_URL_LENGTH = 500;

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PolicyRepository policyRepository;
    private final JobPostingRepository jobPostingRepository;
    private final PostResponseAssembler assembler;
    private final NewReviewNotificationService newReviewNotificationService;

    // viewerId: 로그인 선택 API라 비로그인이면 null
    public PageResponse<PostSummaryResponse> list(PostType type, Long policyId, Long jobId, JobCategory jobCategory,
                                                  boolean sameStage, PostSort sort, Long viewerId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Set<LifeStage> stages = EnumSet.allOf(LifeStage.class);
        if (sameStage) {
            if (viewerId == null) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }
            stages = findUser(viewerId).getLifeStages();
            if (stages.isEmpty()) {
                // 상황을 등록하지 않았으면 같은 상황 글이 없다
                return PageResponse.of(new PageImpl<>(List.of(), pageable, 0));
            }
        }
        Page<Post> posts = sort == PostSort.LIKES
                ? postRepository.searchByLikes(type, policyId, jobId, jobCategory, sameStage, stages, pageable)
                : postRepository.search(type, policyId, jobId, jobCategory, sameStage, stages, pageable);
        return toSummaryPage(posts, viewerId);
    }

    // 정책 상세의 '후기 더 보기'
    public PageResponse<PostSummaryResponse> policyReviews(Long policyId, Long viewerId, int page, int size) {
        if (!policyRepository.existsById(policyId)) {
            throw new BusinessException(ErrorCode.POLICY_NOT_FOUND);
        }
        return list(null, policyId, null, null, false, PostSort.LATEST, viewerId, page, size);
    }

    // 숨김 글은 작성자에게만 보여준다
    public PostDetailResponse detail(Long postId, Long viewerId) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .filter(p -> !p.isHidden() || p.isWrittenBy(viewerId))
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        return assembler.detail(post, viewerId);
    }

    public PageResponse<MyPostResponse> myPosts(Long userId, PostType type, int page, int size) {
        Page<Post> posts = postRepository.findMine(userId, type, PageRequest.of(page, size));
        return PageResponse.of(new PageImpl<>(assembler.myPosts(posts.getContent()), posts.getPageable(),
                posts.getTotalElements()));
    }

    @Transactional
    public PostIdResponse create(Long userId, PostCreateRequest request) {
        User user = findUser(userId);
        LifeStage authorStage = user.getLifeStages().stream()
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new BusinessException(ErrorCode.ONBOARDING_REQUIRED));

        PostType type = request.type();
        String oneLiner = request.oneLiner().trim();
        String content = blankToNull(request.content());
        String portfolioUrl = blankToNull(request.portfolioUrl());
        List<String> helpfulPoints = normalizePoints(request.helpfulPoints());

        if (type != PostType.PORTFOLIO && request.rating() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (type == PostType.PROGRAM_REVIEW && request.policyId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (type == PostType.PASS_REVIEW && request.jobId() == null && request.jobCategory() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (type == PostType.PORTFOLIO && portfolioUrl == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        validateText(content, portfolioUrl);
        if (ContactInfoDetector.containsContact(oneLiner, content)) {
            throw new BusinessException(ErrorCode.CONTACT_INFO_DETECTED);
        }

        if (request.policyId() != null && !policyRepository.existsById(request.policyId())) {
            throw new BusinessException(ErrorCode.POLICY_NOT_FOUND);
        }
        JobCategory jobCategory = request.jobCategory();
        if (request.jobId() != null) {
            JobPosting job = jobPostingRepository.findById(request.jobId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.JOB_NOT_FOUND));
            // 공고만 고르면 직무 태그는 공고의 직무로 채운다
            if (jobCategory == null) {
                jobCategory = job.getJobCategory();
            }
        }

        Post post = postRepository.save(Post.builder()
                .userId(userId)
                .type(type)
                .policyId(request.policyId())
                .jobId(request.jobId())
                .jobCategory(jobCategory)
                .rating(request.rating())
                .oneLiner(oneLiner)
                .prepPeriod(request.prepPeriod())
                .content(content)
                .portfolioUrl(portfolioUrl)
                .authorStage(authorStage)
                .helpfulPoints(helpfulPoints)
                .build());
        newReviewNotificationService.send(post);
        return new PostIdResponse(post.getId());
    }

    // 보낸 필드만 바꾼다. 빈 문자열을 보내면 content·portfolioUrl을 지운다
    @Transactional
    public PostIdResponse update(Long userId, Long postId, PostUpdateRequest request) {
        Post post = findOwnPost(userId, postId);

        String oneLiner = request.oneLiner() == null ? post.getOneLiner() : request.oneLiner().trim();
        if (oneLiner.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        String content = request.content() == null ? post.getContent() : blankToNull(request.content());
        String portfolioUrl = request.portfolioUrl() == null ? post.getPortfolioUrl()
                : blankToNull(request.portfolioUrl());
        if (post.getType() == PostType.PORTFOLIO && portfolioUrl == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        List<String> helpfulPoints = request.helpfulPoints() == null ? List.copyOf(post.getHelpfulPoints())
                : normalizePoints(request.helpfulPoints());
        validateText(content, portfolioUrl);
        if (ContactInfoDetector.containsContact(oneLiner, content)) {
            throw new BusinessException(ErrorCode.CONTACT_INFO_DETECTED);
        }

        post.update(
                request.rating() == null ? post.getRating() : request.rating(),
                oneLiner,
                request.prepPeriod() == null ? post.getPrepPeriod() : request.prepPeriod(),
                helpfulPoints, content, portfolioUrl);
        return new PostIdResponse(post.getId());
    }

    @Transactional
    public void delete(Long userId, Long postId) {
        findOwnPost(userId, postId).delete(LocalDateTime.now(SEOUL));
    }

    private Post findOwnPost(Long userId, Long postId) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        if (!post.isWrittenBy(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return post;
    }

    private User findUser(Long userId) {
        // 토큰은 유효하지만 이미 탈퇴한 회원
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }

    private PageResponse<PostSummaryResponse> toSummaryPage(Page<Post> posts, Long viewerId) {
        return PageResponse.of(new PageImpl<>(assembler.summaries(posts.getContent(), viewerId),
                posts.getPageable(), posts.getTotalElements()));
    }

    // 앞뒤 공백 제거, 빈 값 제외, 중복 제거 (순서 유지)
    private static List<String> normalizePoints(List<String> points) {
        if (points == null) {
            return List.of();
        }
        Set<String> result = new LinkedHashSet<>();
        for (String point : points) {
            if (point == null || point.isBlank()) {
                continue;
            }
            String trimmed = point.trim();
            if (trimmed.length() > MAX_HELPFUL_POINT_LENGTH) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }
            result.add(trimmed);
        }
        if (result.size() > MAX_HELPFUL_POINTS) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return List.copyOf(result);
    }

    private static void validateText(String content, String portfolioUrl) {
        if (content != null && content.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (portfolioUrl != null && (portfolioUrl.length() > MAX_URL_LENGTH
                || !(portfolioUrl.startsWith("http://") || portfolioUrl.startsWith("https://")))) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
