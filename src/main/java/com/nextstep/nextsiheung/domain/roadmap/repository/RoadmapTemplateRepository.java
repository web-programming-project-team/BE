package com.nextstep.nextsiheung.domain.roadmap.repository;

import com.nextstep.nextsiheung.domain.roadmap.entity.RoadmapTemplate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapTemplateRepository extends JpaRepository<RoadmapTemplate, Long> {

    // 템플릿 카탈로그(RoadmapTemplateCatalog)의 템플릿을 이름으로 찾는다
    Optional<RoadmapTemplate> findByName(String name);
}
