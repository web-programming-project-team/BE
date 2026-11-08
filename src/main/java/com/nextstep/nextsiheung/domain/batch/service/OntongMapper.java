package com.nextstep.nextsiheung.domain.batch.service;

import com.nextstep.nextsiheung.domain.batch.dto.OntongPolicyRow;
import com.nextstep.nextsiheung.global.common.enums.InterestField;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

// 온통청년 정책 1건 → policy에 넣을 값. 마감된 정책·필수값이 없는 정책은 버린다
@Component
public class OntongMapper {

    static final String PERIOD_ALWAYS = "0057002";
    static final String PERIOD_CLOSED = "0057003";
    // 신청·참고 URL이 모두 없을 때 보내는 온통청년 정책 상세 화면
    static final String DETAIL_URL = "https://www.youthcenter.go.kr/youthPolicy/ythPlcyTotalSearch/ythPlcyDetail/";
    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.BASIC_ISO_DATE;
    private static final int TITLE_MAX = 200;
    private static final int AGENCY_MAX = 100;
    private static final int TARGET_MAX = 500;
    private static final int URL_MAX = 500;
    private static final int TAG_MAX = 30;
    private static final int EXTERNAL_ID_MAX = 50;

    public Optional<PolicyFields> toFields(OntongPolicyRow row, LocalDate today) {
        if (isBlank(row.policyNo()) || row.policyNo().trim().length() > EXTERNAL_ID_MAX || isBlank(row.name())) {
            return Optional.empty();
        }
        if (PERIOD_CLOSED.equals(row.applyPeriodType()) || isOtherRegion(row.topRegisteringAgency())
                || isOtherRegion(row.supervisingAgency()) || isOtherRegion(row.registeringAgency())) {
            return Optional.empty();
        }
        LocalDate[] period = period(row);
        if (period[1] != null && period[1].isBefore(today)) {
            return Optional.empty();
        }
        String policyNo = row.policyNo().trim();
        String sourceUrl = url(row.referenceUrl()).or(() -> url(row.applyUrl())).orElse(DETAIL_URL + policyNo);
        String applyUrl = url(row.applyUrl()).orElse(sourceUrl);
        return Optional.of(new PolicyFields(
                policyNo,
                cut(oneLine(row.name()), TITLE_MAX),
                field(row),
                cut(agency(row), AGENCY_MAX),
                cut(target(row), TARGET_MAX),
                content(row),
                period[0],
                period[1],
                sourceUrl,
                applyUrl,
                tags(row)));
    }

    // 경기도 밖 광역지자체(와 그 소속 기관)가 등록·주관하는 정책. 예: 대구광역시, 전남광주통합특별시, 세종특별자치시
    // 지역 코드에 시흥이 걸려 있어도 그 지역 거주·이주 청년 대상이라 뺀다. 중앙부처·공공기관·경기도 시군은 유지
    private static final List<String> OTHER_REGIONS = List.of("서울", "부산", "대구", "인천", "광주", "대전", "울산",
            "세종", "강원", "충청", "충북", "충남", "전라", "전북", "전남", "경상", "경북", "경남", "제주");

    static boolean isOtherRegion(String agency) {
        if (isBlank(agency)) {
            return false;
        }
        String name = agency.trim();
        return OTHER_REGIONS.stream().anyMatch(name::startsWith);
    }

    // 대분류(일자리·교육･직업훈련·주거·금융･복지·문화·참여･기반)와 중분류로 앱의 관심분야를 고른다
    static InterestField field(OntongPolicyRow row) {
        String large = nullToEmpty(row.largeCategory());
        String middle = nullToEmpty(row.middleCategory());
        if (nullToEmpty(row.name()).contains("상담")) {
            return InterestField.COUNSELING;
        }
        if (large.startsWith("일자리")) {
            return InterestField.EMPLOYMENT;
        }
        if (large.startsWith("교육")) {
            return InterestField.EDUCATION;
        }
        if (large.startsWith("주거")) {
            return InterestField.HOUSING;
        }
        if (large.startsWith("금융") && middle.contains("금융")) {
            return InterestField.FINANCE;
        }
        // 금융･복지･문화의 건강·문화활동·예술인지원, 참여･기반
        return InterestField.CULTURE_LIFE;
    }

    // [시작일, 마감일]. 상시이거나 형식이 다르면 null
    static LocalDate[] period(OntongPolicyRow row) {
        LocalDate[] none = {null, null};
        if (PERIOD_ALWAYS.equals(row.applyPeriodType()) || isBlank(row.applyPeriod())) {
            return none;
        }
        String[] parts = row.applyPeriod().split("~");
        if (parts.length != 2) {
            return none;
        }
        try {
            LocalDate start = LocalDate.parse(parts[0].trim(), YYYYMMDD);
            LocalDate end = LocalDate.parse(parts[1].trim(), YYYYMMDD);
            return end.isBefore(start) ? none : new LocalDate[]{start, end};
        } catch (DateTimeParseException e) {
            return none;
        }
    }

    // "www.indie053.net" 처럼 scheme이 빠진 값은 https를 붙인다. 공백 뒤 설명이 붙어 오면 첫 토큰만
    static Optional<String> url(String raw) {
        if (isBlank(raw)) {
            return Optional.empty();
        }
        String value = raw.trim().split("\\s+")[0];
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            value = "https://" + value;
        }
        try {
            URI uri = URI.create(value);
            if (uri.getHost() == null || !uri.getHost().contains(".") || value.length() > URL_MAX) {
                return Optional.empty();
            }
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    private static String agency(OntongPolicyRow row) {
        for (String name : List.of(nullToEmpty(row.supervisingAgency()), nullToEmpty(row.operatingAgency()),
                nullToEmpty(row.registeringAgency()))) {
            if (!name.isBlank()) {
                return name.trim();
            }
        }
        return "온통청년";
    }

    // "만 19~39세" + 추가 자격 조건. 연령이 0~0이면 연령 문구는 뺀다
    static String target(OntongPolicyRow row) {
        int min = toInt(row.minAge());
        int max = toInt(row.maxAge());
        List<String> lines = new ArrayList<>();
        if (min > 0 || max > 0) {
            lines.add(max > 0 ? "만 " + min + "~" + max + "세" : "만 " + min + "세 이상");
        }
        if (!isBlank(row.qualification())) {
            lines.add(row.qualification().trim());
        }
        return lines.isEmpty() ? "청년" : String.join("\n", lines);
    }

    private static String content(OntongPolicyRow row) {
        List<String> parts = new ArrayList<>();
        if (!isBlank(row.explanation())) {
            parts.add(row.explanation().trim());
        }
        if (!isBlank(row.support())) {
            parts.add("[지원 내용]\n" + row.support().trim());
        }
        if (!isBlank(row.applyMethod())) {
            parts.add("[신청 방법]\n" + row.applyMethod().trim());
        }
        return parts.isEmpty() ? oneLine(row.name()) : String.join("\n\n", parts);
    }

    // 정책 키워드 + 중분류. 공고-정책 매칭(직무 키워드)과 화면 태그에 쓴다
    static Set<String> tags(OntongPolicyRow row) {
        Set<String> tags = new LinkedHashSet<>();
        for (String raw : (nullToEmpty(row.keywords()) + "," + nullToEmpty(row.middleCategory())).split(",")) {
            String tag = raw.trim();
            if (!tag.isEmpty()) {
                tags.add(cut(tag, TAG_MAX));
            }
        }
        return tags;
    }

    private static int toInt(String s) {
        try {
            return isBlank(s) ? 0 : Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String oneLine(String s) {
        return s.trim().replaceAll("\\s+", " ");
    }

    private static String cut(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public record PolicyFields(String externalId, String title, InterestField field, String agency, String target,
                               String content, LocalDate applyStartDate, LocalDate applyEndDate, String sourceUrl,
                               String applyUrl, Set<String> tags) {
    }
}
