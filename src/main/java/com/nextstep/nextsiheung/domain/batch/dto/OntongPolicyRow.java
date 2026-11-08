package com.nextstep.nextsiheung.domain.batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// 온통청년 「청년정책 API」(getPlcy) 응답 youthPolicyList의 1건. 쓰는 항목만 받는다(항목명은 API 명세 그대로)
@JsonIgnoreProperties(ignoreUnknown = true)
public record OntongPolicyRow(
        @JsonProperty("plcyNo") String policyNo,
        @JsonProperty("plcyNm") String name,
        @JsonProperty("plcyKywdNm") String keywords,
        @JsonProperty("plcyExplnCn") String explanation,
        @JsonProperty("plcySprtCn") String support,
        @JsonProperty("plcyAplyMthdCn") String applyMethod,
        @JsonProperty("lclsfNm") String largeCategory,
        @JsonProperty("mclsfNm") String middleCategory,
        @JsonProperty("sprvsnInstCdNm") String supervisingAgency,
        @JsonProperty("operInstCdNm") String operatingAgency,
        @JsonProperty("rgtrInstCdNm") String registeringAgency,
        // 신청기간 구분: 0057001 특정기간 · 0057002 상시 · 0057003 마감
        @JsonProperty("aplyPrdSeCd") String applyPeriodType,
        // "20260303 ~ 20260315". 상시면 빈 문자열
        @JsonProperty("aplyYmd") String applyPeriod,
        @JsonProperty("aplyUrlAddr") String applyUrl,
        @JsonProperty("refUrlAddr1") String referenceUrl,
        @JsonProperty("sprtTrgtMinAge") String minAge,
        @JsonProperty("sprtTrgtMaxAge") String maxAge,
        @JsonProperty("addAplyQlfcCndCn") String qualification,
        // 등록기관의 최상위 기관 (경기도 · 대구광역시 · 국방부 · 정부산하기관및위원회 등)
        @JsonProperty("rgtrHghrkInstCdNm") String topRegisteringAgency
) {
}
