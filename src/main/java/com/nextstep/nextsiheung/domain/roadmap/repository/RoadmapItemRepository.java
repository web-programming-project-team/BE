package com.nextstep.nextsiheung.domain.roadmap.repository;

import com.nextstep.nextsiheung.domain.roadmap.entity.RoadmapItem;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoadmapItemRepository extends JpaRepository<RoadmapItem, Long> {

    // 정책 상세의 myItem: 현재 로드맵에 이 정책이 있는지
    @Query("""
            select i from RoadmapItem i
            where i.step.roadmap.user.id = :userId and i.step.roadmap.archived = false and i.policy.id = :policyId
            """)
    Optional<RoadmapItem> findCurrent(@Param("userId") Long userId, @Param("policyId") Long policyId);

    // 정책 목록의 myStatus: [policyId, status]
    @Query("""
            select i.policy.id, i.status from RoadmapItem i
            where i.step.roadmap.user.id = :userId and i.step.roadmap.archived = false and i.policy.id in :policyIds
            """)
    List<Object[]> findCurrentStatuses(@Param("userId") Long userId,
                                       @Param("policyIds") Collection<Long> policyIds);

    // 관리자 정책 목록의 roadmapItemCount: [policyId, 담은 사용자 수]
    @Query("""
            select i.policy.id, count(distinct i.step.roadmap.user.id) from RoadmapItem i
            where i.policy.id in :policyIds group by i.policy.id
            """)
    List<Object[]> countUsersByPolicy(@Param("policyIds") Collection<Long> policyIds);

    // NEW_REVIEW 알림 대상: 현재 로드맵에 이 정책을 담은 사용자
    @Query("""
            select distinct i.step.roadmap.user.id from RoadmapItem i
            where i.policy.id = :policyId and i.step.roadmap.archived = false
            """)
    List<Long> findCurrentUserIdsByPolicyId(@Param("policyId") Long policyId);

    // 운영 지표 topPrograms: 진행상태를 한 번이라도 고른(TO_CHECK가 아닌) 사용자 수가 많은 정책
    // [policyId, title, 사용자 수]
    @Query("""
            select i.policy.id, i.policy.title, count(distinct i.step.roadmap.user.id) from RoadmapItem i
            where i.status <> com.nextstep.nextsiheung.global.common.enums.ProgressStatus.TO_CHECK
            group by i.policy.id, i.policy.title
            order by count(distinct i.step.roadmap.user.id) desc, i.policy.id asc
            """)
    List<Object[]> findTopPrograms(Pageable pageable);
}
