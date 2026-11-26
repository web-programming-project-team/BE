package com.nextstep.nextsiheung.domain.roadmap.service;

import com.nextstep.nextsiheung.domain.policy.entity.Policy;
import com.nextstep.nextsiheung.domain.policy.repository.PolicyRepository;
import com.nextstep.nextsiheung.domain.roadmap.entity.RoadmapTemplate;
import com.nextstep.nextsiheung.domain.roadmap.entity.TemplateStep;
import com.nextstep.nextsiheung.domain.roadmap.repository.RoadmapTemplateRepository;
import com.nextstep.nextsiheung.domain.roadmap.service.RoadmapTemplateCatalog.StepDef;
import com.nextstep.nextsiheung.domain.roadmap.service.RoadmapTemplateCatalog.TemplateDef;
import com.nextstep.nextsiheung.global.common.Kst;
import com.nextstep.nextsiheung.global.common.enums.RecruitStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 로드맵 템플릿 단계에 실제 정책을 연결한다 (RoadmapTemplateCatalog의 단계 규칙)
// 1) 후보: 종료·마감되지 않은 정책 중 단계 분야에 맞는 것
// 2) 점수: 키워드가 제목에 있으면 3점, 태그 2점, 본문 1점 + 시흥 기관 3점·경기 기관 1점. 제목·태그에 키워드가 없으면 제외
// 3) 단계마다 최대 3개, 점수 → 마감 임박 → ID 순. 같은 템플릿 안에서는 한 정책(같은 이름 포함)을 한 단계에만 둔다
// 카탈로그에 없는 템플릿(테스트·직접 넣은 템플릿)은 건드리지 않는다. 이미 만든 사용자 로드맵에는 영향이 없다
@Slf4j
@Service
@RequiredArgsConstructor
public class RoadmapTemplatePolicyLinker {

    static final int MAX_PER_STEP = 3;
    private static final Set<RecruitStatus> ACTIVE = EnumSet.of(RecruitStatus.UPCOMING, RecruitStatus.OPEN);
    // 온통청년이 성격이 다른 정책에도 두루 붙이는 키워드 (예: 버스킹·주택 사업에도 '맞춤형상담서비스'). 점수에서 뺀다
    private static final Set<String> GENERIC_TAGS = Set.of("맞춤형상담서비스", "교육지원", "보조금");

    private final RoadmapTemplateRepository templateRepository;
    private final PolicyRepository policyRepository;

    @Transactional
    public void relinkAll() {
        LocalDate today = Kst.today();
        List<Policy> candidates = policyRepository.findAll().stream()
                .filter(p -> ACTIVE.contains(p.currentStatus(today)))
                .toList();

        int linked = 0;
        for (RoadmapTemplate template : templateRepository.findAll()) {
            Optional<TemplateDef> def = RoadmapTemplateCatalog.find(template.getName());
            if (def.isEmpty()) {
                continue;
            }
            Set<Long> used = new HashSet<>();
            Set<String> usedTitles = new HashSet<>();
            for (TemplateStep step : template.getSteps()) {
                Optional<StepDef> rule = def.get().step(step.getTitle());
                if (rule.isEmpty()) {
                    continue;
                }
                List<Policy> chosen = choose(rule.get(), candidates, used, usedTitles);
                chosen.forEach(p -> used.add(p.getId()));
                step.replacePolicies(chosen);
                linked += chosen.size();
            }
        }
        log.info("[로드맵 템플릿] 단계별 정책 다시 연결: {}건", linked);
    }

    public static List<Policy> choose(StepDef rule, List<Policy> candidates, Set<Long> used,
                                      Set<String> usedTitles) {
        Comparator<Scored> order = Comparator.comparingInt(Scored::score).reversed()
                .thenComparing(s -> s.policy().getApplyEndDate(), Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(s -> s.policy().getId());
        List<Scored> ranked = candidates.stream()
                .filter(p -> rule.fields().contains(p.getField()) && !used.contains(p.getId()))
                .map(p -> new Scored(p, score(rule, p)))
                .filter(s -> s.score() > 0)
                .sorted(order)
                .toList();
        // 같은 사업을 여러 기관이 각각 올린 경우(예: 전세보증금반환보증 보증료 지원 — 경기도·국토부)는 한 번만.
        // 이름은 공백을 빼고 비교하고, 같은 템플릿의 앞 단계에서 넣은 이름도 피한다(usedTitles)
        List<Policy> chosen = new ArrayList<>();
        for (Scored s : ranked) {
            if (chosen.size() == MAX_PER_STEP) {
                break;
            }
            if (usedTitles.add(titleKey(s.policy()))) {
                chosen.add(s.policy());
            }
        }
        return chosen;
    }

    static String titleKey(Policy p) {
        return p.getTitle().replaceAll("\\s+", "");
    }

    // 제목·태그에 키워드가 하나도 없으면 0(제외). 본문에만 있는 키워드는 가산점으로만 쓴다
    // (본문 한 줄에 '컨설팅'이 있다고 주택 사업이 상담 단계에 들어가지 않도록)
    static int score(StepDef rule, Policy p) {
        String title = p.getTitle();
        String tags = String.join(" ", p.getTags().stream().filter(t -> !GENERIC_TAGS.contains(t)).toList());
        String content = p.getContent();
        int keyword = 0;
        boolean main = false;
        for (String word : rule.keywords()) {
            if (title.contains(word)) {
                keyword += 3;
                main = true;
            } else if (tags.contains(word)) {
                keyword += 2;
                main = true;
            } else if (content.contains(word)) {
                keyword += 1;
            }
        }
        if (!main) {
            return 0;
        }
        String agency = p.getAgency();
        int local = agency.contains("시흥") ? 3 : agency.contains("경기") ? 1 : 0;
        return keyword + local;
    }

    private record Scored(Policy policy, int score) {
    }
}
