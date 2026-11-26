package com.nextstep.nextsiheung.domain.roadmap.entity;

import com.nextstep.nextsiheung.global.common.BaseTimeEntity;
import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.LifeStage;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// ERD roadmap_template. 관리자 화면 없이 DB seed 데이터로 관리한다(명세 v1.3)
@Getter
@Entity
@Table(name = "roadmap_template")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoadmapTemplate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ElementCollection
    @CollectionTable(name = "template_life_stage", joinColumns = @JoinColumn(name = "template_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "life_stage", nullable = false, length = 30)
    private Set<LifeStage> lifeStages = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "template_interest", joinColumns = @JoinColumn(name = "template_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "interest_field", nullable = false, length = 30)
    private Set<InterestField> interests = new LinkedHashSet<>();

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    private List<TemplateStep> steps = new ArrayList<>();

    public RoadmapTemplate(String name, Collection<LifeStage> lifeStages, Collection<InterestField> interests) {
        this.name = name;
        this.lifeStages.addAll(lifeStages);
        this.interests.addAll(interests);
    }

    public TemplateStep addStep(String title) {
        TemplateStep step = new TemplateStep(this, steps.size() + 1, title);
        steps.add(step);
        return step;
    }

    // 로드맵 생성 규칙: 사용자의 상황·관심분야와 겹치는 개수 (많을수록 우선)
    public int matchScore(Collection<LifeStage> userStages, Collection<InterestField> userInterests) {
        int score = 0;
        for (LifeStage s : userStages) {
            if (lifeStages.contains(s)) {
                score++;
            }
        }
        for (InterestField f : userInterests) {
            if (interests.contains(f)) {
                score++;
            }
        }
        return score;
    }
}
