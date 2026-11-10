package com.nextstep.nextsiheung.domain.job.repository;

import com.nextstep.nextsiheung.domain.job.entity.JobBookmark;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobBookmarkRepository extends JpaRepository<JobBookmark, Long> {

    Optional<JobBookmark> findByUserIdAndJobId(Long userId, Long jobId);

    boolean existsByUserIdAndJobId(Long userId, Long jobId);

    // 목록 화면의 bookmarked 표시: 이 페이지 공고 중 저장한 것
    @Query("select b.job.id from JobBookmark b where b.user.id = :userId and b.job.id in :jobIds")
    List<Long> findBookmarkedJobIds(@Param("userId") Long userId, @Param("jobIds") Collection<Long> jobIds);

    // 관심 공고 목록: 접수 중인 공고를 마감 임박순(상시채용은 뒤), 마감된 공고는 맨 뒤
    @Query(value = """
            select b from JobBookmark b join fetch b.job j
            where b.user.id = :userId
            order by case when j.closed = true or j.deadline < :today then 1 else 0 end,
                     case when j.deadline is null then 1 else 0 end,
                     j.deadline asc, b.id desc
            """,
            countQuery = "select count(b) from JobBookmark b where b.user.id = :userId")
    Page<JobBookmark> findPageByUserId(@Param("userId") Long userId, @Param("today") LocalDate today,
                                       Pageable pageable);

    // NEW_REVIEW 알림 대상: 이 공고를 관심 저장한 사용자
    @Query("select b.user.id from JobBookmark b where b.job.id = :jobId")
    List<Long> findUserIdsByJobId(@Param("jobId") Long jobId);

    // 운영 지표: 기간 안 관심 공고 저장 수
    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime from, LocalDateTime to);
}
