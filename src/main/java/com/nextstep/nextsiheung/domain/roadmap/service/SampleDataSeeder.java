package com.nextstep.nextsiheung.domain.roadmap.service;

import com.nextstep.nextsiheung.domain.policy.entity.Policy;
import com.nextstep.nextsiheung.domain.policy.repository.PolicyRepository;
import com.nextstep.nextsiheung.domain.roadmap.entity.RoadmapTemplate;
import com.nextstep.nextsiheung.domain.roadmap.repository.RoadmapTemplateRepository;
import com.nextstep.nextsiheung.global.common.Kst;
import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.LifeStage;
import com.nextstep.nextsiheung.global.common.enums.PolicySource;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 로컬 개발·시연용 샘플 정책과 로드맵 템플릿 (app.seed.sample-data=true일 때, 템플릿이 하나도 없을 때만)
// 정책은 실제 정책이 아니라 화면 확인용 예시다. 출처 URL이 example.com이고 지원대상에 [샘플]이 붙는다
// 실제 정책은 온통청년 수집과 관리자 등록(POST /admin/policies)으로 넣는다
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.sample-data", havingValue = "true")
public class SampleDataSeeder implements ApplicationRunner {

    private final PolicyRepository policyRepository;
    private final RoadmapTemplateRepository templateRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (templateRepository.count() > 0) {
            return;
        }
        LocalDate t = Kst.today();
        Policy academy = policy("청년 직무역량 아카데미", InterestField.EMPLOYMENT, "시흥시청소년청년재단",
                t.minusDays(5), t.plusDays(11), List.of(LifeStage.GRADUATING, LifeStage.JOB_SEEKING));
        Policy certificate = policy("청년 자격증 응시료 지원", InterestField.EDUCATION, "시흥시청소년청년재단",
                t.minusDays(20), t.plusDays(3), List.of(LifeStage.ENROLLED, LifeStage.JOB_SEEKING));
        Policy rent = policy("청년 월세 지원", InterestField.HOUSING, "경기도",
                t.plusDays(8), t.plusDays(36), List.of(LifeStage.FIRST_JOB, LifeStage.INDEPENDENCE));
        Policy resume = policy("이력서 클리닉", InterestField.EMPLOYMENT, "시흥 청년스테이션",
                t.minusDays(10), t.plusDays(5), List.of(LifeStage.GRADUATING, LifeStage.JOB_SEEKING));
        Policy counseling = policy("청년 상담", InterestField.COUNSELING, "시흥 청년스테이션",
                null, null, List.of());
        Policy career = policy("진로 탐색 상담", InterestField.COUNSELING, "시흥 청년스테이션",
                null, null, List.of(LifeStage.ENROLLED, LifeStage.ON_LEAVE, LifeStage.MILITARY));
        Policy deposit = policy("청년 전세보증금 이자 지원", InterestField.HOUSING, "경기도",
                t.minusDays(30), t.plusDays(60), List.of(LifeStage.FIRST_JOB, LifeStage.INDEPENDENCE));
        Policy saving = policy("청년 저축 지원", InterestField.FINANCE, "시흥시",
                t.minusDays(3), t.plusDays(25), List.of(LifeStage.FIRST_JOB));
        Policy culture = policy("청년 문화패스", InterestField.CULTURE_LIFE, "경기도",
                t.minusDays(1), t.plusDays(40), List.of());

        RoadmapTemplate graduating = new RoadmapTemplate("졸업 예정 · 취업 준비 · 취업/주거",
                List.of(LifeStage.GRADUATING, LifeStage.JOB_SEEKING),
                List.of(InterestField.EMPLOYMENT, InterestField.HOUSING));
        graduating.addStep("진로·취업 상담").addPolicy(career).addPolicy(counseling);
        graduating.addStep("직무역량 프로그램").addPolicy(academy).addPolicy(certificate);
        graduating.addStep("취업지원사업").addPolicy(resume);
        graduating.addStep("주거지원").addPolicy(rent).addPolicy(deposit);
        graduating.addStep("금융·생활지원").addPolicy(saving).addPolicy(culture);

        RoadmapTemplate student = new RoadmapTemplate("대학 재학 · 휴학 · 진로/교육",
                List.of(LifeStage.ENROLLED, LifeStage.ON_LEAVE),
                List.of(InterestField.EDUCATION, InterestField.EMPLOYMENT, InterestField.CULTURE_LIFE));
        student.addStep("진로 탐색").addPolicy(career);
        student.addStep("자격증·교육").addPolicy(certificate).addPolicy(academy);
        student.addStep("문화·생활").addPolicy(culture);

        RoadmapTemplate firstJob = new RoadmapTemplate("첫 취업 · 독립 준비 · 주거/금융",
                List.of(LifeStage.FIRST_JOB, LifeStage.INDEPENDENCE),
                List.of(InterestField.HOUSING, InterestField.FINANCE));
        firstJob.addStep("주거 마련").addPolicy(rent).addPolicy(deposit);
        firstJob.addStep("자산 형성").addPolicy(saving);
        firstJob.addStep("마음건강·생활").addPolicy(counseling).addPolicy(culture);

        RoadmapTemplate military = new RoadmapTemplate("군 복무·전역 · 사회 복귀",
                List.of(LifeStage.MILITARY),
                List.of(InterestField.EMPLOYMENT, InterestField.COUNSELING));
        military.addStep("진로 상담").addPolicy(career).addPolicy(counseling);
        military.addStep("취업 준비").addPolicy(resume).addPolicy(academy);

        templateRepository.saveAll(List.of(graduating, student, firstJob, military));
        log.info("샘플 정책 {}건, 로드맵 템플릿 4개를 넣었습니다", policyRepository.count());
    }

    private Policy policy(String title, InterestField field, String agency, LocalDate start, LocalDate end,
                          List<LifeStage> stages) {
        String url = "https://example.com/sample/" + Math.abs(title.hashCode());
        return policyRepository.save(Policy.builder()
                .source(PolicySource.ADMIN)
                .title(title).field(field).agency(agency)
                .target("[샘플] 시흥시 거주 19~24세 청년")
                .content("[샘플] 화면 확인용 예시 정책입니다.")
                .applyStartDate(start).applyEndDate(end)
                .sourceUrl(url).applyUrl(url)
                .lifeStages(stages)
                .today(Kst.today())
                .build());
    }
}
