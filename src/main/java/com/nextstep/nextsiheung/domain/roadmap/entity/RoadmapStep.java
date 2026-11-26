package com.nextstep.nextsiheung.domain.roadmap.entity;

import com.nextstep.nextsiheung.domain.policy.entity.Policy;
import com.nextstep.nextsiheung.global.common.enums.ProgressStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

// ERD roadmap_step. 템플릿 단계를 로드맵을 만들 때 복사해 둔다
@Getter
@Entity
@Table(name = "roadmap_step")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoadmapStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roadmap_step_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roadmap_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Roadmap roadmap;

    @Column(nullable = false)
    private int stepOrder;

    @Column(nullable = false, length = 100)
    private String title;

    @OneToMany(mappedBy = "step", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<RoadmapItem> items = new ArrayList<>();

    RoadmapStep(Roadmap roadmap, int stepOrder, String title) {
        this.roadmap = roadmap;
        this.stepOrder = stepOrder;
        this.title = title;
    }

    public RoadmapItem addItem(Policy policy, ProgressStatus status, boolean userAdded) {
        RoadmapItem item = new RoadmapItem(this, policy, status, userAdded);
        items.add(item);
        return item;
    }

    public void removeItem(RoadmapItem item) {
        items.remove(item);
    }

    // 항목이 1개 이상 있고 전부 완료면 완료. 빈 단계는 미완료로 둔다(명세 예시)
    public boolean isCompleted() {
        return !items.isEmpty() && items.stream().allMatch(i -> i.getStatus() == ProgressStatus.COMPLETED);
    }
}
