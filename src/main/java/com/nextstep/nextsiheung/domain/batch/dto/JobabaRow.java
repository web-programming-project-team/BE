package com.nextstep.nextsiheung.domain.batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// 경기데이터드림 「경기도일자리재단 잡아바 채용정보」(GGJOBABARECRUSTM) 응답의 row 1건. 항목명은 오픈API 명세서 그대로
@JsonIgnoreProperties(ignoreUnknown = true)
public record JobabaRow(
        @JsonProperty("ENTRPRS_NM") String companyName,
        @JsonProperty("PBANC_CONT") String title,
        @JsonProperty("SALARY_COND") String salary,
        @JsonProperty("PBANC_FORM_CD_NM") String formCode,
        @JsonProperty("PBANC_FORM_DIV") String form,
        @JsonProperty("WORK_REGION_CD_CONT") String regionCode,
        @JsonProperty("WORK_REGION_CONT") String regionName,
        @JsonProperty("CAREER_CD_NM") String careerCode,
        @JsonProperty("CAREER_DIV") String career,
        @JsonProperty("ACDMCR_CD_NM") String educationCode,
        @JsonProperty("ACDMCR_DIV") String education,
        @JsonProperty("RECRUT_FIELD_CD_NM") String fieldCode,
        @JsonProperty("RECRUT_FIELD_NM") String field,
        @JsonProperty("EMPLMNT_PSNCNT") Integer headcount,
        @JsonProperty("RCPT_BGNG_DE") String receiptStart,
        @JsonProperty("RCPT_END_DE") String receiptEnd,
        @JsonProperty("URL") String url
) {
}
