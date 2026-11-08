package com.nextstep.nextsiheung.domain.roadmap;

import static org.assertj.core.api.Assertions.assertThat;

import com.nextstep.nextsiheung.domain.policy.entity.Policy;
import com.nextstep.nextsiheung.domain.roadmap.service.RoadmapTemplateCatalog;
import com.nextstep.nextsiheung.domain.roadmap.service.RoadmapTemplateCatalog.StepDef;
import com.nextstep.nextsiheung.domain.roadmap.service.RoadmapTemplatePolicyLinker;
import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.PolicySource;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

// DB 없이 템플릿 단계에 정책을 고르는 규칙을 확인한다
class RoadmapTemplatePolicyLinkerTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 8);
    private static final StepDef HOUSING = RoadmapTemplateCatalog.find("졸업 예정 · 취업 준비 · 취업/주거").orElseThrow()
            .step("주거지원").orElseThrow();

    @Test
    void 카탈로그의_템플릿과_단계_이름이_기존_DB_템플릿과_같다() {
        assertThat(RoadmapTemplateCatalog.TEMPLATES).extracting(RoadmapTemplateCatalog.TemplateDef::name)
                .containsExactly("졸업 예정 · 취업 준비 · 취업/주거", "대학 재학 · 휴학 · 진로/교육",
                        "첫 취업 · 독립 준비 · 주거/금융", "군 복무·전역 · 사회 복귀");
        assertThat(RoadmapTemplateCatalog.find("대학 재학 · 휴학 · 진로/교육").orElseThrow().steps())
                .extracting(StepDef::title).containsExactly("진로 탐색", "자격증·교육", "문화·생활");
    }

    @Test
    void 분야가_맞고_키워드가_있는_정책을_점수순으로_최대_3개() {
        Policy siheungRent = policy(1, "시흥시 청년 월세 지원", InterestField.HOUSING, "경기도 시흥시", null);
        Policy nationalRent = policy(2, "청년 월세 한시 특별지원", InterestField.HOUSING, "국토교통부", TODAY.plusDays(20));
        Policy deposit = policy(3, "청년 전세보증금 반환보증", InterestField.HOUSING, "주택도시보증공사", TODAY.plusDays(5));
        Policy dorm = policy(4, "기숙사 입사 지원", InterestField.HOUSING, "교육부", null);
        Policy loan = policy(5, "청년 월세 대출", InterestField.FINANCE, "국토교통부", null);
        Policy extra = policy(6, "청년 주택 드림 청약", InterestField.HOUSING, "국토교통부", TODAY.plusDays(40));

        List<Policy> chosen = RoadmapTemplatePolicyLinker.choose(HOUSING,
                List.of(dorm, loan, extra, deposit, nationalRent, siheungRent), new HashSet<>(), new HashSet<>());

        // 전세보증금(전세+보증금 6점) = 시흥 월세(월세 3 + 시흥 기관 3) → 같은 점수면 마감일 있는 쪽 먼저
        // 그다음 3점끼리 마감 임박순(월세 특별지원 D-20 > 주택 청약 D-40). 키워드 없는 기숙사, 분야 다른 대출은 제외
        assertThat(chosen).containsExactly(deposit, siheungRent, nationalRent);
    }

    @Test
    void 같은_템플릿의_다른_단계에_이미_넣은_정책은_빼고_고른다() {
        Policy rent = policy(1, "청년 월세 지원", InterestField.HOUSING, "경기도", null);
        Policy deposit = policy(2, "청년 전세 지원", InterestField.HOUSING, "경기도", null);
        List<Policy> chosen = RoadmapTemplatePolicyLinker.choose(HOUSING, List.of(rent, deposit),
                new HashSet<>(Set.of(1L)), new HashSet<>());
        assertThat(chosen).containsExactly(deposit);
    }

    private static Policy policy(long id, String title, InterestField field, String agency, LocalDate end) {
        Policy p = Policy.builder()
                .source(PolicySource.ONTONG).externalId("T" + id)
                .title(title).field(field).agency(agency).target("청년").content("내용")
                .applyEndDate(end).sourceUrl("https://www.gov.kr").today(TODAY)
                .build();
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }
}
