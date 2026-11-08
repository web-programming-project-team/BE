package com.nextstep.nextsiheung.domain.roadmap.service;

import com.nextstep.nextsiheung.domain.roadmap.entity.RoadmapTemplate;
import com.nextstep.nextsiheung.domain.roadmap.repository.RoadmapTemplateRepository;
import com.nextstep.nextsiheung.domain.roadmap.service.RoadmapTemplateCatalog.StepDef;
import com.nextstep.nextsiheung.domain.roadmap.service.RoadmapTemplateCatalog.TemplateDef;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 로드맵 템플릿 4개를 넣는다(이름이 같은 템플릿이 없을 때만). 샘플 데이터와 상관없이 운영 환경에서도 돈다
// 단계에 담을 정책은 여기서 정하지 않고 RoadmapTemplatePolicyLinker가 실제 정책으로 연결한다
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.roadmap-templates", havingValue = "true", matchIfMissing = true)
public class RoadmapTemplateSeeder implements ApplicationRunner {

    private final RoadmapTemplateRepository templateRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int created = 0;
        for (TemplateDef def : RoadmapTemplateCatalog.TEMPLATES) {
            if (templateRepository.findByName(def.name()).isPresent()) {
                continue;
            }
            RoadmapTemplate template = new RoadmapTemplate(def.name(), def.lifeStages(), def.interests());
            for (StepDef step : def.steps()) {
                template.addStep(step.title());
            }
            templateRepository.save(template);
            created++;
        }
        if (created > 0) {
            log.info("로드맵 템플릿 {}개를 넣었습니다", created);
        }
    }
}
