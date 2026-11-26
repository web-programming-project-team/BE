package com.nextstep.nextsiheung.domain.post.service;

import com.nextstep.nextsiheung.domain.job.entity.JobPosting;
import com.nextstep.nextsiheung.domain.job.repository.JobPostingRepository;
import com.nextstep.nextsiheung.domain.policy.entity.Policy;
import com.nextstep.nextsiheung.domain.policy.repository.PolicyRepository;
import com.nextstep.nextsiheung.domain.post.dto.JobRef;
import com.nextstep.nextsiheung.domain.post.dto.MyPostResponse;
import com.nextstep.nextsiheung.domain.post.dto.PolicyRef;
import com.nextstep.nextsiheung.domain.post.dto.PostDetailResponse;
import com.nextstep.nextsiheung.domain.post.dto.PostSummaryResponse;
import com.nextstep.nextsiheung.domain.post.entity.Post;
import com.nextstep.nextsiheung.domain.post.repository.PostLikeRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 게시글 엔티티를 응답으로 바꾼다. 목록은 정책·공고·좋아요를 한 번에 모아 조회한다 (게시글마다 조회하지 않음)
@Component
@RequiredArgsConstructor
class PostResponseAssembler {

    private final PolicyRepository policyRepository;
    private final JobPostingRepository jobPostingRepository;
    private final PostLikeRepository postLikeRepository;

    List<PostSummaryResponse> summaries(List<Post> posts, Long viewerId) {
        if (posts.isEmpty()) {
            return List.of();
        }
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Map<Long, PolicyRef> policies = policyRefs(posts);
        Map<Long, JobRef> jobs = jobRefs(posts);
        Map<Long, Long> likeCounts = postLikeRepository.countByPostIds(postIds).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
        Set<Long> liked = viewerId == null ? Set.of()
                : new HashSet<>(postLikeRepository.findLikedPostIds(viewerId, postIds));

        return posts.stream()
                .map(p -> new PostSummaryResponse(
                        p.getId(), p.getType(), p.getRating(), p.getOneLiner(), p.getPrepPeriod(),
                        List.copyOf(p.getHelpfulPoints()),
                        lookup(policies, p.getPolicyId()), lookup(jobs, p.getJobId()), p.getJobCategory(),
                        p.getAuthorStage(), p.getAuthorStageAfter(),
                        likeCounts.getOrDefault(p.getId(), 0L), liked.contains(p.getId()), p.getCreatedAt()))
                .toList();
    }

    PostDetailResponse detail(Post p, Long viewerId) {
        PolicyRef policy = p.getPolicyId() == null ? null : policyRepository.findById(p.getPolicyId())
                .map(PostResponseAssembler::toRef).orElse(null);
        JobRef job = p.getJobId() == null ? null : jobPostingRepository.findById(p.getJobId())
                .map(PostResponseAssembler::toRef).orElse(null);
        long likeCount = postLikeRepository.countByIdPostId(p.getId());
        boolean liked = viewerId != null
                && !postLikeRepository.findLikedPostIds(viewerId, List.of(p.getId())).isEmpty();

        return new PostDetailResponse(
                p.getId(), p.getType(), p.getRating(), p.getOneLiner(), p.getPrepPeriod(),
                List.copyOf(p.getHelpfulPoints()), p.getContent(), p.getPortfolioUrl(),
                policy, job, p.getJobCategory(), p.getAuthorStage(), p.getAuthorStageAfter(),
                likeCount, liked, p.isWrittenBy(viewerId), p.getCreatedAt());
    }

    List<MyPostResponse> myPosts(List<Post> posts) {
        Map<Long, PolicyRef> policies = policyRefs(posts);
        return posts.stream()
                .map(p -> new MyPostResponse(p.getId(), p.getType(), p.getOneLiner(),
                        lookup(policies, p.getPolicyId()), p.isHidden(), p.getCreatedAt()))
                .toList();
    }

    private Map<Long, PolicyRef> policyRefs(List<Post> posts) {
        Set<Long> ids = posts.stream().map(Post::getPolicyId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return policyRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Policy::getId, PostResponseAssembler::toRef));
    }

    private Map<Long, JobRef> jobRefs(List<Post> posts) {
        Set<Long> ids = posts.stream().map(Post::getJobId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        return jobPostingRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(JobPosting::getId, PostResponseAssembler::toRef));
    }

    // Map.of()는 null 키 조회를 막으므로 태그가 없는 글(policyId·jobId null)은 여기서 거른다
    private static <V> V lookup(Map<Long, V> map, Long id) {
        return id == null ? null : map.get(id);
    }

    private static PolicyRef toRef(Policy policy) {
        return new PolicyRef(policy.getId(), policy.getTitle());
    }

    private static JobRef toRef(JobPosting job) {
        return new JobRef(job.getId(), job.getCompany(), job.getTitle());
    }
}
