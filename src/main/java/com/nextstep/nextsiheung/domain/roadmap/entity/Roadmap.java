package com.nextstep.nextsiheung.domain.roadmap.entity;

import com.nextstep.nextsiheung.domain.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

// ERD roadmap. 회원마다 현재 로드맵(archived=false)은 1개, 상황 변경으로 보관된 로드맵은 활동 기록이 된다
@Getter
@Entity
@Table(name = "roadmap")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Roadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roadmap_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private RoadmapTemplate template;

    // 템플릿 이름이 나중에 바뀌어도 만들 때 이름을 남긴다
    @Column(nullable = false, length = 100)
    private String templateName;

    @Column(nullable = false)
    private boolean archived;

    private LocalDateTime archivedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "roadmap", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    private List<RoadmapStep> steps = new ArrayList<>();

    public Roadmap(User user, RoadmapTemplate template, String templateName) {
        this.user = user;
        this.template = template;
        this.templateName = templateName;
    }

    public RoadmapStep addStep(String title) {
        RoadmapStep step = new RoadmapStep(this, steps.size() + 1, title);
        steps.add(step);
        return step;
    }

    public void archive(LocalDateTime now) {
        this.archived = true;
        this.archivedAt = now;
    }

    public boolean isOwnedBy(Long userId) {
        return user.getId().equals(userId);
    }

    public Stream<RoadmapItem> items() {
        return steps.stream().flatMap(s -> s.getItems().stream());
    }
}
