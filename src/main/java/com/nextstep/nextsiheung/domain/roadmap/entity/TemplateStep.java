package com.nextstep.nextsiheung.domain.roadmap.entity;

import com.nextstep.nextsiheung.domain.policy.entity.Policy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// ERD template_step + template_step_policy. 템플릿 단계와 단계별로 연결된 정책
@Getter
@Entity
@Table(name = "template_step")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TemplateStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_step_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private RoadmapTemplate template;

    @Column(nullable = false)
    private int stepOrder;

    @Column(nullable = false, length = 100)
    private String title;

    @ManyToMany
    @JoinTable(name = "template_step_policy",
            joinColumns = @JoinColumn(name = "template_step_id"),
            inverseJoinColumns = @JoinColumn(name = "policy_id"))
    private Set<Policy> policies = new LinkedHashSet<>();

    TemplateStep(RoadmapTemplate template, int stepOrder, String title) {
        this.template = template;
        this.stepOrder = stepOrder;
        this.title = title;
    }

    public TemplateStep addPolicy(Policy policy) {
        policies.add(policy);
        return this;
    }

    // 템플릿 정책 연결(RoadmapTemplatePolicyLinker)이 규칙으로 고른 정책으로 통째로 바꾼다
    public void replacePolicies(Collection<Policy> chosen) {
        policies.clear();
        policies.addAll(chosen);
    }
}
