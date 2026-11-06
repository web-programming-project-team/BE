package com.nextstep.nextsiheung.domain.job.repository;

import com.nextstep.nextsiheung.domain.job.entity.JobPolicyMatch;
import com.nextstep.nextsiheung.domain.job.entity.JobPosting;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobPolicyMatchRepository extends JpaRepository<JobPolicyMatch, JobPolicyMatch.Id> {

    // 공고별 도움 되는 지원: [jobId, policyId, title]. 신청 마감이 가까운 정책부터
    @Query("""
            select m.id.jobId, p.id, p.title from JobPolicyMatch m join m.policy p
            where m.id.jobId in :jobIds
            order by case when p.applyEndDate is null then 1 else 0 end, p.applyEndDate asc, p.id asc
            """)
    List<Object[]> findPolicies(@Param("jobIds") Collection<Long> jobIds);

    // 정책 상세의 연결 채용공고: 접수 중인 공고를 마감 임박순(상시채용은 뒤)
    @Query("""
            select j from JobPolicyMatch m join m.job j
            where m.id.policyId = :policyId and j.closed = false and (j.deadline is null or j.deadline >= :today)
            order by case when j.deadline is null then 1 else 0 end, j.deadline asc, j.id desc
            """)
    List<JobPosting> findOpenJobs(@Param("policyId") Long policyId, @Param("today") LocalDate today,
                                  Pageable pageable);
}
