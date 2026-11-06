package com.nextstep.nextsiheung.domain.post.repository;

import com.nextstep.nextsiheung.domain.post.entity.Post;
import com.nextstep.nextsiheung.global.common.enums.JobCategory;
import com.nextstep.nextsiheung.global.common.enums.LifeStage;
import com.nextstep.nextsiheung.global.common.enums.PostType;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByIdAndDeletedAtIsNull(Long id);

    // 커뮤니티 목록·정책 후기 목록. 삭제·숨김 글은 빼고 최신순
    // filterStage가 true면 작성 당시 상황이나 바뀐 상황 중 하나가 stages에 있는 글만 (stages는 비어 있으면 안 됨)
    @Query("""
            select p from Post p
            where p.deletedAt is null and p.hidden = false
              and (:type is null or p.type = :type)
              and (:policyId is null or p.policyId = :policyId)
              and (:jobId is null or p.jobId = :jobId)
              and (:jobCategory is null or p.jobCategory = :jobCategory)
              and (:filterStage = false or p.authorStage in :stages or p.authorStageAfter in :stages)
            order by p.createdAt desc, p.id desc""")
    Page<Post> search(@Param("type") PostType type,
                      @Param("policyId") Long policyId,
                      @Param("jobId") Long jobId,
                      @Param("jobCategory") JobCategory jobCategory,
                      @Param("filterStage") boolean filterStage,
                      @Param("stages") Collection<LifeStage> stages,
                      Pageable pageable);

    // 내가 쓴 글. 숨김 글도 포함
    @Query("""
            select p from Post p
            where p.userId = :userId and p.deletedAt is null
              and (:type is null or p.type = :type)
            order by p.createdAt desc, p.id desc""")
    Page<Post> findMine(@Param("userId") Long userId, @Param("type") PostType type, Pageable pageable);
}
