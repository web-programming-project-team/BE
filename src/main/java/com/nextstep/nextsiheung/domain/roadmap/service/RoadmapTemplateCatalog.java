package com.nextstep.nextsiheung.domain.roadmap.service;

import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.LifeStage;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// 로드맵 템플릿 정의 (명세 v1.3: 관리자 화면 없이 seed로 관리)
// 단계마다 '어떤 정책을 담을지' 규칙(분야 + 키워드)을 둔다. 실제 정책은 RoadmapTemplatePolicyLinker가
// 정책 수집 직후·서버 시작 시 이 규칙으로 골라 단계에 연결한다 (정책 ID를 코드에 박지 않는다)
public final class RoadmapTemplateCatalog {

    private RoadmapTemplateCatalog() {
    }

    public record StepDef(String title, Set<InterestField> fields, List<String> keywords) {
    }

    public record TemplateDef(String name, List<LifeStage> lifeStages, List<InterestField> interests,
                              List<StepDef> steps) {

        public Optional<StepDef> step(String title) {
            return steps.stream().filter(s -> s.title().equals(title)).findFirst();
        }
    }

    private static final List<String> COUNSELING_WORDS = List.of("상담", "진로", "컨설팅", "멘토", "코칭");
    private static final List<String> HOUSING_WORDS = List.of("월세", "전세", "보증금", "주거", "임대", "주택");

    public static final List<TemplateDef> TEMPLATES = List.of(
            new TemplateDef("졸업 예정 · 취업 준비 · 취업/주거",
                    List.of(LifeStage.GRADUATING, LifeStage.JOB_SEEKING),
                    List.of(InterestField.EMPLOYMENT, InterestField.HOUSING),
                    List.of(
                            new StepDef("진로·취업 상담", Set.of(InterestField.COUNSELING, InterestField.EMPLOYMENT),
                                    COUNSELING_WORDS),
                            new StepDef("직무역량 프로그램", Set.of(InterestField.EDUCATION, InterestField.EMPLOYMENT),
                                    List.of("직무", "역량", "아카데미", "훈련", "교육", "자격")),
                            new StepDef("취업지원사업", Set.of(InterestField.EMPLOYMENT),
                                    List.of("취업", "인턴", "일자리", "구직", "면접", "채용")),
                            new StepDef("주거지원", Set.of(InterestField.HOUSING), HOUSING_WORDS),
                            new StepDef("금융·생활지원", Set.of(InterestField.FINANCE, InterestField.CULTURE_LIFE),
                                    List.of("저축", "통장", "자산", "적금", "금융", "생활")))),
            new TemplateDef("대학 재학 · 휴학 · 진로/교육",
                    List.of(LifeStage.ENROLLED, LifeStage.ON_LEAVE),
                    List.of(InterestField.EDUCATION, InterestField.EMPLOYMENT, InterestField.CULTURE_LIFE),
                    List.of(
                            new StepDef("진로 탐색", Set.of(InterestField.COUNSELING, InterestField.EMPLOYMENT),
                                    List.of("진로", "탐색", "상담", "멘토", "대학생")),
                            new StepDef("자격증·교육", Set.of(InterestField.EDUCATION),
                                    List.of("자격", "교육", "역량", "훈련", "강좌", "장학")),
                            new StepDef("문화·생활", Set.of(InterestField.CULTURE_LIFE),
                                    List.of("문화", "예술", "여가", "체육", "생활")))),
            new TemplateDef("첫 취업 · 독립 준비 · 주거/금융",
                    List.of(LifeStage.FIRST_JOB, LifeStage.INDEPENDENCE),
                    List.of(InterestField.HOUSING, InterestField.FINANCE),
                    List.of(
                            new StepDef("주거 마련", Set.of(InterestField.HOUSING), HOUSING_WORDS),
                            new StepDef("자산 형성", Set.of(InterestField.FINANCE),
                                    List.of("저축", "통장", "자산", "적금", "금리", "대출")),
                            new StepDef("마음건강·생활", Set.of(InterestField.COUNSELING, InterestField.CULTURE_LIFE),
                                    List.of("마음", "심리", "건강", "상담", "문화")))),
            new TemplateDef("군 복무·전역 · 사회 복귀",
                    List.of(LifeStage.MILITARY),
                    List.of(InterestField.EMPLOYMENT, InterestField.COUNSELING),
                    List.of(
                            new StepDef("진로 상담", Set.of(InterestField.COUNSELING, InterestField.EMPLOYMENT),
                                    List.of("전역", "제대", "장병", "상담", "진로")),
                            new StepDef("취업 준비", Set.of(InterestField.EMPLOYMENT, InterestField.EDUCATION),
                                    List.of("전역", "장병", "취업", "직무", "인턴", "훈련")))));

    public static Optional<TemplateDef> find(String name) {
        return TEMPLATES.stream().filter(t -> t.name().equals(name)).findFirst();
    }
}
