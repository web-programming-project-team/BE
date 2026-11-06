package com.nextstep.nextsiheung.domain.roadmap.entity;

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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

// ERD roadmap_item_history. 진행상태가 바뀔 때마다 1행 (처음 추가는 fromStatus = null)
@Getter
@Entity
@Table(name = "roadmap_item_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoadmapItemHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private RoadmapItem item;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ProgressStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProgressStatus toStatus;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    public RoadmapItemHistory(RoadmapItem item, ProgressStatus fromStatus, ProgressStatus toStatus,
                              LocalDateTime changedAt) {
        this.item = item;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedAt = changedAt;
    }
}
