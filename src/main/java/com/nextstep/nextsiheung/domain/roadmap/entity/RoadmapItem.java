package com.nextstep.nextsiheung.domain.roadmap.entity;

import com.nextstep.nextsiheung.domain.policy.entity.Policy;
import com.nextstep.nextsiheung.global.common.BaseTimeEntity;
import com.nextstep.nextsiheung.global.common.enums.ProgressStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

// ERD roadmap_item. 로드맵 단계에 담긴 정책 1개와 내 진행상태
@Getter
@Entity
@Table(name = "roadmap_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoadmapItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roadmap_step_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private RoadmapStep step;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProgressStatus status;

    // 사용자가 정책 상세에서 직접 추가했으면 true (템플릿에서 복사한 항목은 false). 삭제는 이 항목만 된다
    @Column(nullable = false)
    private boolean userAdded;

    RoadmapItem(RoadmapStep step, Policy policy, ProgressStatus status, boolean userAdded) {
        this.step = step;
        this.policy = policy;
        this.status = status;
        this.userAdded = userAdded;
    }

    public void changeStatus(ProgressStatus status) {
        this.status = status;
    }

    public Roadmap roadmap() {
        return step.getRoadmap();
    }
}
