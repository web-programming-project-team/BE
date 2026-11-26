package com.nextstep.nextsiheung.domain.post.repository;

import com.nextstep.nextsiheung.domain.post.entity.Post;
import com.nextstep.nextsiheung.global.common.enums.JobCategory;
import com.nextstep.nextsiheung.global.common.enums.LifeStage;
import com.nextstep.nextsiheung.global.common.enums.PostType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByIdAndDeletedAtIsNull(Long id);

    // 커뮤니티 목록·정책 후기 목록의 조건. 삭제·숨김 글은 뺀다
    // filterStage가 true면 작성 당시 상황이나 바뀐 상황 중 하나가 stages에 있는 글만 (stages는 비어 있으면 안 됨)
    String SEARCH_FROM = """
            from Post p
            where p.deletedAt is null and p.hidden = false
              and (:type is null or p.type = :type)
              and (:policyId is null or p.policyId = :policyId)
              and (:jobId is null or p.jobId = :jobId)
              and (:jobCategory is null or p.jobCategory = :jobCategory)
              and (:filterStage = false or p.authorStage in :stages or p.authorStageAfter in :stages)
            """;

    // 최신순
    @Query(value = "select p " + SEARCH_FROM + " order by p.createdAt desc, p.id desc",
            countQuery = "select count(p) " + SEARCH_FROM)
    Page<Post> search(@Param("type") PostType type,
                      @Param("policyId") Long policyId,
                      @Param("jobId") Long jobId,
                      @Param("jobCategory") JobCategory jobCategory,
                      @Param("filterStage") boolean filterStage,
                      @Param("stages") Collection<LifeStage> stages,
                      Pageable pageable);

    // 도움돼요(좋아요) 많은순. 같으면 최신순
    @Query(value = "select p " + SEARCH_FROM + """
            order by (select count(l) from PostLike l where l.id.postId = p.id) desc, p.createdAt desc, p.id desc""",
            countQuery = "select count(p) " + SEARCH_FROM)
    Page<Post> searchByLikes(@Param("type") PostType type,
                             @Param("policyId") Long policyId,
                             @Param("jobId") Long jobId,
                             @Param("jobCategory") JobCategory jobCategory,
                             @Param("filterStage") boolean filterStage,
                             @Param("stages") Collection<LifeStage> stages,
                             Pageable pageable);

    // 정책 상세의 관련 후기 최신 3개 (삭제·숨김 제외)
    List<Post> findTop3ByPolicyIdAndDeletedAtIsNullAndHiddenFalseOrderByCreatedAtDescIdDesc(Long policyId);

    long countByPolicyIdAndDeletedAtIsNullAndHiddenFalse(Long policyId);

    // 채용 상세의 합격 후기: 이 공고에 달렸거나 같은 직무인 합격 후기 (삭제·숨김 제외, 최신순)
    @Query("""
            select p from Post p
            where p.type = com.nextstep.nextsiheung.global.common.enums.PostType.PASS_REVIEW
              and p.deletedAt is null and p.hidden = false
              and (p.jobId = :jobId or (:jobCategory is not null and p.jobCategory = :jobCategory))
            order by p.createdAt desc, p.id desc""")
    List<Post> findPassReviewsForJob(@Param("jobId") Long jobId, @Param("jobCategory") JobCategory jobCategory,
                                     Pageable pageable);

    // 운영 지표의 프로그램별 후기 수. [policyId, count] (삭제·숨김 제외)
    @Query("""
            select p.policyId, count(p) from Post p
            where p.policyId in :policyIds and p.deletedAt is null and p.hidden = false
            group by p.policyId""")
    List<Object[]> countVisibleByPolicyIds(@Param("policyIds") Collection<Long> policyIds);

    // 운영 지표의 기간 안 후기 작성 수. 작성 활동 지표라 이후 삭제·숨김된 글도 센다
    long countByTypeInAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(Collection<PostType> types,
                                                                       LocalDateTime from, LocalDateTime to);

    // 내가 쓴 글. 숨김 글도 포함
    @Query("""
            select p from Post p
            where p.userId = :userId and p.deletedAt is null
              and (:type is null or p.type = :type)
            order by p.createdAt desc, p.id desc""")
    Page<Post> findMine(@Param("userId") Long userId, @Param("type") PostType type, Pageable pageable);
}
