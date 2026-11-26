package com.nextstep.nextsiheung.domain.job.entity;

import com.nextstep.nextsiheung.domain.policy.entity.Policy;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

// ERD job_policy_match. 공고별 '이 공고에 도움 되는 지원' (공고당 최대 3개)
// 행은 JobPolicyMatchService가 규칙대로 다시 계산해 통째로 넣는다(JdbcTemplate). 엔티티는 테이블 정의와 조회용
@Getter
@Entity
@Table(name = "job_policy_match")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobPolicyMatch {

    @EmbeddedId
    private Id id;

    @MapsId("jobId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id")
    private JobPosting job;

    @MapsId("policyId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id")
    private Policy policy;

    @Getter
    @Embeddable
    @EqualsAndHashCode
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Id implements Serializable {

        @Column(name = "job_id")
        private Long jobId;

        @Column(name = "policy_id")
        private Long policyId;
    }
}
