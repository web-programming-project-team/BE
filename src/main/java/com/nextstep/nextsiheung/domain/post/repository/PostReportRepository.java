package com.nextstep.nextsiheung.domain.post.repository;

import com.nextstep.nextsiheung.domain.post.entity.PostReport;
import com.nextstep.nextsiheung.domain.post.entity.ReportStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {

    boolean existsByPostIdAndReporterId(Long postId, Long reporterId);

    long countByPostIdAndStatus(Long postId, ReportStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update PostReport r set r.status = com.nextstep.nextsiheung.domain.post.entity.ReportStatus.RESOLVED,
                                    r.resolvedAt = :now
            where r.postId = :postId
              and r.status = com.nextstep.nextsiheung.domain.post.entity.ReportStatus.PENDING""")
    int resolveAll(@Param("postId") Long postId, @Param("now") LocalDateTime now);

    // 게시글별로 묶는다. 자동 숨김된 글이 먼저, 그다음 최근 신고순. 삭제된 글은 뺀다
    @Query(value = """
            select new com.nextstep.nextsiheung.domain.post.repository.ReportGroupRow(
                       p.id, p.oneLiner, count(r), max(r.createdAt), p.hidden, p.hiddenReason)
            from PostReport r join Post p on p.id = r.postId
            where r.status = :status and p.deletedAt is null
            group by p.id, p.oneLiner, p.hidden, p.hiddenReason
            order by case when p.hidden = true and p.hiddenReason = :autoReason then 0 else 1 end,
                     max(r.createdAt) desc, p.id desc""",
            countQuery = """
            select count(distinct r.postId)
            from PostReport r join Post p on p.id = r.postId
            where r.status = :status and p.deletedAt is null""")
    Page<ReportGroupRow> findGroups(@Param("status") ReportStatus status, @Param("autoReason") String autoReason,
                                    Pageable pageable);

    // [postId, reason, count]
    @Query("""
            select r.postId, r.reason, count(r) from PostReport r
            where r.status = :status and r.postId in :postIds
            group by r.postId, r.reason""")
    List<Object[]> countReasons(@Param("status") ReportStatus status, @Param("postIds") Collection<Long> postIds);
}
