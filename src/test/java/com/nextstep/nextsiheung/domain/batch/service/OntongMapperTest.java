package com.nextstep.nextsiheung.domain.batch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nextstep.nextsiheung.domain.batch.dto.OntongPolicyRow;
import com.nextstep.nextsiheung.domain.batch.service.OntongMapper.PolicyFields;
import com.nextstep.nextsiheung.global.common.enums.InterestField;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

// DB·네트워크 없이 온통청년 응답 파싱과 변환 규칙을 확인한다 (응답 예시는 2026-11 실제 응답에서 발췌)
class OntongMapperTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 8);

    private final OntongClient client = new OntongClient(JsonMapper.builder().build(), "");
    private final OntongMapper mapper = new OntongMapper();

    private static final String SAMPLE = """
            {"resultCode":200,"resultMessage":"성공적으로 데이터를 가지고 왔습니다.","result":{"pagging":{"totCount":817,"pageNum":1,"pageSize":3},"youthPolicyList":[
            {"plcyNo":"20261105005400214737","plcyNm":"동성로 청년버스킹 활성화 사업","plcyKywdNm":"맞춤형상담서비스","plcyExplnCn":"전국 청년·예비 예술가의 거리 공연 활동을 지원","lclsfNm":"금융･복지･문화","mclsfNm":"예술인지원","plcySprtCn":"공연 사례비, 음향 및 조명","sprvsnInstCdNm":"대구광역시 문화체육관광국 문화예술정책과","operInstCdNm":"","aplyPrdSeCd":"0057001","aplyYmd":"20261103 ~ 20261215","plcyAplyMthdCn":"홈페이지 신청서 다운로드 후 이메일 접수","aplyUrlAddr":"www.indie053.net","refUrlAddr1":"www.indie053.net","sprtTrgtMinAge":"19","sprtTrgtMaxAge":"39","addAplyQlfcCndCn":"국내·외 청년예술가 개인 또는 팀","rgtrInstCdNm":"대구광역시 청년여성교육국","rgtrHghrkInstCdNm":"대구광역시","zipCd":"41390"},
            {"plcyNo":"20260310000000000001","plcyNm":"경기 청년 일자리 사다리","plcyKywdNm":"인턴,중소기업","plcyExplnCn":"","lclsfNm":"일자리","mclsfNm":"취업","plcySprtCn":"","sprvsnInstCdNm":"","operInstCdNm":"경기도일자리재단","aplyPrdSeCd":"0057002","aplyYmd":"","plcyAplyMthdCn":"","aplyUrlAddr":"","refUrlAddr1":"","sprtTrgtMinAge":"0","sprtTrgtMaxAge":"0","addAplyQlfcCndCn":"","rgtrInstCdNm":"경기도","rgtrHghrkInstCdNm":"경기도","zipCd":"41390"},
            {"plcyNo":"20260101000000000002","plcyNm":"지난 청년 월세 지원","plcyKywdNm":"주거지원","lclsfNm":"주거","mclsfNm":"전월세 및 주거급여 지원","sprvsnInstCdNm":"경기도","aplyPrdSeCd":"0057001","aplyYmd":"20260101 ~ 20260228","aplyUrlAddr":"https://example.go.kr","refUrlAddr1":"","sprtTrgtMinAge":"19","sprtTrgtMaxAge":"34","zipCd":"41390"}
            ]}}
            """;

    @Test
    void 응답을_파싱한다() {
        OntongClient.Page page = client.parse(SAMPLE);
        assertThat(page.totalCount()).isEqualTo(817);
        assertThat(page.rows()).hasSize(3);
        assertThat(page.rows().get(0).policyNo()).isEqualTo("20261105005400214737");
        assertThat(page.rows().get(0).applyPeriod()).isEqualTo("20261103 ~ 20261215");
    }

    @Test
    void 오류_응답이면_예외() {
        assertThatThrownBy(() -> client.parse("{\"resultCode\":401,\"resultMessage\":\"인증키 오류\"}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("인증키 오류");
    }

    @Test
    void 특정기간_정책을_변환한다() {
        PolicyFields p = mapper.toFields(gyeonggi(row(0)), TODAY).orElseThrow();
        assertThat(p.externalId()).isEqualTo("20261105005400214737");
        assertThat(p.field()).isEqualTo(InterestField.CULTURE_LIFE);
        assertThat(p.agency()).isEqualTo("경기도 문화체육관광국");
        assertThat(p.applyStartDate()).isEqualTo(LocalDate.of(2026, 11, 3));
        assertThat(p.applyEndDate()).isEqualTo(LocalDate.of(2026, 12, 15));
        // scheme이 없는 URL은 https를 붙인다
        assertThat(p.applyUrl()).isEqualTo("https://www.indie053.net");
        assertThat(p.sourceUrl()).isEqualTo("https://www.indie053.net");
        assertThat(p.target()).isEqualTo("만 19~39세\n국내·외 청년예술가 개인 또는 팀");
        assertThat(p.content()).contains("[지원 내용]\n공연 사례비").contains("[신청 방법]");
        assertThat(p.tags()).containsExactly("맞춤형상담서비스", "예술인지원");
    }

    @Test
    void 상시_정책은_기간이_없고_URL이_없으면_온통청년_상세로_보낸다() {
        PolicyFields p = mapper.toFields(row(1), TODAY).orElseThrow();
        assertThat(p.field()).isEqualTo(InterestField.EMPLOYMENT);
        assertThat(p.applyStartDate()).isNull();
        assertThat(p.applyEndDate()).isNull();
        assertThat(p.sourceUrl()).isEqualTo(OntongMapper.DETAIL_URL + "20260310000000000001");
        assertThat(p.applyUrl()).isEqualTo(p.sourceUrl());
        // 주관기관이 비면 운영기관
        assertThat(p.agency()).isEqualTo("경기도일자리재단");
        // 연령 0~0이면 연령 문구 없이 기본값
        assertThat(p.target()).isEqualTo("청년");
        assertThat(p.content()).isEqualTo("경기 청년 일자리 사다리");
        assertThat(p.tags()).containsExactly("인턴", "중소기업", "취업");
    }

    @Test
    void 접수가_끝난_정책과_마감_코드_정책은_버린다() {
        assertThat(mapper.toFields(row(2), TODAY)).isEmpty();
        OntongPolicyRow closed = copy(gyeonggi(row(0)), "0057003", "20261103 ~ 20261215");
        assertThat(mapper.toFields(closed, TODAY)).isEmpty();
        // 마감일 당일은 넣는다
        assertThat(mapper.toFields(gyeonggi(row(0)), LocalDate.of(2026, 12, 15))).isPresent();
    }

    @Test
    void 경기도가_아닌_지자체가_등록한_정책은_버리고_중앙부처_정책은_넣는다() {
        assertThat(OntongMapper.isOtherRegion("대구광역시")).isTrue();
        assertThat(OntongMapper.isOtherRegion("전남광주통합특별시")).isTrue();
        assertThat(OntongMapper.isOtherRegion("세종특별자치시")).isTrue();
        assertThat(OntongMapper.isOtherRegion("경상북도")).isTrue();
        assertThat(OntongMapper.isOtherRegion("경기도")).isFalse();
        assertThat(OntongMapper.isOtherRegion("경기도 시흥시")).isFalse();
        // 경기도 시군이 직접 주관·등록한 정책은 유지
        assertThat(OntongMapper.isOtherRegion("시흥시 청년청소년과")).isFalse();
        assertThat(OntongMapper.isOtherRegion("광명시")).isFalse();
        assertThat(OntongMapper.isOtherRegion("인천광역시 청년정책과")).isTrue();
        assertThat(OntongMapper.isOtherRegion("국방부")).isFalse();
        assertThat(OntongMapper.isOtherRegion("정부산하기관및위원회")).isFalse();
        assertThat(OntongMapper.isOtherRegion("")).isFalse();
        // 예시 응답 1번(대구 버스킹)은 등록 최상위기관이 대구광역시
        assertThat(mapper.toFields(row(0), TODAY)).isEmpty();
    }

    @Test
    void 분야를_고른다() {
        assertThat(OntongMapper.field(category("교육･직업훈련", "미래역량강화", "AI 교육"))).isEqualTo(InterestField.EDUCATION);
        assertThat(OntongMapper.field(category("주거", "기숙사", "청년 기숙사"))).isEqualTo(InterestField.HOUSING);
        assertThat(OntongMapper.field(category("금융･복지･문화", "취약계층 및 금융지원", "청년 통장"))).isEqualTo(InterestField.FINANCE);
        assertThat(OntongMapper.field(category("금융･복지･문화", "건강", "마음건강"))).isEqualTo(InterestField.CULTURE_LIFE);
        assertThat(OntongMapper.field(category("참여･기반", "청년참여", "청년 서포터즈"))).isEqualTo(InterestField.CULTURE_LIFE);
        // 이름에 '상담'이 들어가면 분류와 상관없이 상담
        assertThat(OntongMapper.field(category("일자리", "취업", "청년 진로 상담"))).isEqualTo(InterestField.COUNSELING);
    }

    @Test
    void 잘못된_URL과_기간은_무시한다() {
        assertThat(OntongMapper.url("홈페이지 참조")).isEmpty();
        assertThat(OntongMapper.url("http://youth.gg.go.kr 에서 신청")).contains("http://youth.gg.go.kr");
        assertThat(OntongMapper.period(copy(row(0), "0057001", "2026.11.03 ~ 2026.12.15"))).containsOnlyNulls();
        assertThat(OntongMapper.period(copy(row(0), "0057001", "20261215 ~ 20261103"))).containsOnlyNulls();
    }

    private OntongPolicyRow row(int i) {
        return client.parse(SAMPLE).rows().get(i);
    }

    private static OntongPolicyRow category(String large, String middle, String name) {
        return new OntongPolicyRow("1", name, "", "", "", "", large, middle, "", "", "", "0057002", "", "", "", "0",
                "0", "", "");
    }

    // 예시 1번을 경기도가 주관·등록한 정책으로 바꾼 것 (변환 규칙 확인용)
    private static OntongPolicyRow gyeonggi(OntongPolicyRow r) {
        return new OntongPolicyRow(r.policyNo(), r.name(), r.keywords(), r.explanation(), r.support(),
                r.applyMethod(), r.largeCategory(), r.middleCategory(), "경기도 문화체육관광국", r.operatingAgency(),
                "경기도 청년기회과", r.applyPeriodType(), r.applyPeriod(), r.applyUrl(), r.referenceUrl(),
                r.minAge(), r.maxAge(), r.qualification(), "경기도");
    }

    private static OntongPolicyRow copy(OntongPolicyRow r, String periodType, String period) {
        return new OntongPolicyRow(r.policyNo(), r.name(), r.keywords(), r.explanation(), r.support(),
                r.applyMethod(), r.largeCategory(), r.middleCategory(), r.supervisingAgency(), r.operatingAgency(),
                r.registeringAgency(), periodType, period, r.applyUrl(), r.referenceUrl(), r.minAge(), r.maxAge(),
                r.qualification(), r.topRegisteringAgency());
    }
}
