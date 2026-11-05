package com.nextstep.nextsiheung.domain.batch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nextstep.nextsiheung.domain.batch.dto.JobabaRow;
import com.nextstep.nextsiheung.domain.job.entity.JobPosting;
import com.nextstep.nextsiheung.global.common.enums.Education;
import com.nextstep.nextsiheung.global.common.enums.EmploymentType;
import com.nextstep.nextsiheung.global.common.enums.JobCategory;
import com.nextstep.nextsiheung.global.common.enums.JobSource;
import com.nextstep.nextsiheung.global.common.enums.Region;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

// DB·네트워크 없이 잡아바 응답 파싱과 변환 규칙을 확인한다 (응답 예시는 2026-11 실제 응답에서 발췌)
class JobabaMapperTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 4);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 4, 4, 0);

    private final JobabaClient client = new JobabaClient(JsonMapper.builder().build(), "");
    private final JobabaMapper mapper = new JobabaMapper();

    private static final String SAMPLE = """
            {"GGJOBABARECRUSTM":[{"head":[{"list_total_count":408552},{"RESULT":{"CODE":"INFO-000","MESSAGE":"정상 처리되었습니다."}},{"api_version":"1.0"}]},
            {"row":[
            {"ENTRPRS_NM":"(주)케이탑인터내셔널","PBANC_CONT":"신입 사무장님 모집 합니다.","SALARY_COND":"월급 220만원 ~ 230만원","PBANC_FORM_CD_NM":"01","PBANC_FORM_DIV":"정규직","WORK_REGION_CD_CONT":"4146","WORK_REGION_CONT":"용인시","CAREER_CD_NM":"02","CAREER_DIV":"신입","ACDMCR_CD_NM":"00","ACDMCR_DIV":"학력무관","RECRUT_FIELD_CD_NM":"624","RECRUT_FIELD_NM":"영업ㆍ판매ㆍ운전ㆍ운송직","EMPLMNT_PSNCNT":1,"RCPT_BGNG_DE":"20260529","RCPT_END_DE":"20260628","URL":"https://www.jobkorea.co.kr/Recruit/GI_Read/49276193?Oem_Code=C900&api=232"},
            {"ENTRPRS_NM":"(주)시흥정밀","PBANC_CONT":"생산직 사원 모집","SALARY_COND":"월급 215만원","PBANC_FORM_CD_NM":"01","PBANC_FORM_DIV":"기간의 정함이 없는 근로계약/ 파견근로 비희망/ 대체인력채용 비희망","WORK_REGION_CD_CONT":"4139","WORK_REGION_CONT":"시흥시","CAREER_CD_NM":"04","CAREER_DIV":"신입/경력","ACDMCR_CD_NM":"03,04","ACDMCR_DIV":"고졸,대졸(2~3년)","RECRUT_FIELD_CD_NM":"890006","RECRUT_FIELD_NM":"설치ㆍ정비ㆍ생산직","EMPLMNT_PSNCNT":2,"RCPT_BGNG_DE":"20261101","RCPT_END_DE":"20261130","URL":"http://www.work.go.kr/empInfo/empInfoSrch/detail/empDetailAuthView.do?wantedAuthNo=K1"},
            {"ENTRPRS_NM":"(주)에이텍씨앤","PBANC_CONT":"[일반] (주)에이텍씨앤_구매","SALARY_COND":null,"PBANC_FORM_CD_NM":"01","PBANC_FORM_DIV":"정규직","WORK_REGION_CD_CONT":"4127","WORK_REGION_CONT":"안산시","CAREER_CD_NM":"01","CAREER_DIV":"경력무관","ACDMCR_CD_NM":"05,06","ACDMCR_DIV":"대졸(4년),석사","RECRUT_FIELD_CD_NM":null,"RECRUT_FIELD_NM":null,"EMPLMNT_PSNCNT":1,"RCPT_BGNG_DE":"20261020","RCPT_END_DE":"20700101","URL":"https://job.gg.go.kr/empmn/jobEntDtl.do?seq=14590"}
            ]}]}
            """;

    @Test
    void 응답을_파싱한다() {
        JobabaClient.Page page = client.parse(SAMPLE);

        assertThat(page.totalCount()).isEqualTo(408552);
        assertThat(page.rows()).hasSize(3);
        assertThat(page.rows().get(0).companyName()).isEqualTo("(주)케이탑인터내셔널");
        assertThat(page.rows().get(2).salary()).isNull();
    }

    @Test
    void 데이터가_없으면_빈_페이지_오류면_예외() {
        assertThat(client.parse("{\"RESULT\":{\"CODE\":\"INFO-200\",\"MESSAGE\":\"해당하는 데이터가 없습니다.\"}}").rows()).isEmpty();
        assertThatThrownBy(() -> client.parse("{\"RESULT\":{\"CODE\":\"ERROR-290\",\"MESSAGE\":\"인증키가 유효하지 않습니다.\"}}"))
                .hasMessageContaining("ERROR-290");
    }

    @Test
    void 마감됐거나_대상_지역이_아니면_버린다() {
        JobabaRow yonginClosed = client.parse(SAMPLE).rows().get(0);

        assertThat(mapper.isOpen(yonginClosed, TODAY)).isFalse();
        assertThat(mapper.toEntity(yonginClosed, TODAY, NOW)).isEmpty();
    }

    @Test
    void 시흥_공고를_변환한다() {
        JobPosting job = mapper.toEntity(client.parse(SAMPLE).rows().get(1), TODAY, NOW).orElseThrow();

        assertThat(job.getSource()).isEqualTo(JobSource.JOBABA);
        assertThat(job.getRegion()).isEqualTo(Region.SIHEUNG);
        assertThat(job.getEducation()).isEqualTo(Education.HIGH_SCHOOL);
        assertThat(job.getCareerLevel()).isEqualTo(JobPosting.CAREER_NEW);
        assertThat(job.getEmploymentType()).isEqualTo(EmploymentType.FULL_TIME);
        assertThat(job.getJobCategory()).isEqualTo(JobCategory.MANUFACTURING);
        assertThat(job.getDeadline()).isEqualTo(LocalDate.of(2026, 11, 30));
        assertThat(job.getExternalId()).hasSize(64);
        assertThat(job.getCollectedAt()).isEqualTo(NOW);
    }

    @Test
    void 상시채용은_마감일이_없고_분야가_없으면_제목으로_직무를_고른다() {
        JobPosting job = mapper.toEntity(client.parse(SAMPLE).rows().get(2), TODAY, NOW).orElseThrow();

        assertThat(job.getRegion()).isEqualTo(Region.ANSAN);
        assertThat(job.getDeadline()).isNull();
        assertThat(job.getEducation()).isEqualTo(Education.UNIVERSITY);
        assertThat(job.getCareerLevel()).isEqualTo(JobPosting.CAREER_NEW);
        assertThat(job.getJobCategory()).isEqualTo(JobCategory.OFFICE);
    }

    @Test
    void 경력만_받는_공고는_수집하지_않는다() {
        assertThat(List.of("신입", "신입/경력", "경력무관", "무관"))
                .allMatch(mapper::acceptsNewcomer);
        assertThat(mapper.acceptsNewcomer("경력")).isFalse();
    }

    @Test
    void 공고형태_문구를_고용형태로_바꾼다() {
        assertThat(List.of("정규직", "계약직", "기간제 근로", "인턴", "시간제", "기간의 정함이 없는 근로계약"))
                .map(mapper::employmentType)
                .containsExactly(EmploymentType.FULL_TIME, EmploymentType.CONTRACT, EmploymentType.CONTRACT,
                        EmploymentType.INTERN, EmploymentType.PART_TIME, EmploymentType.FULL_TIME);
    }
}
