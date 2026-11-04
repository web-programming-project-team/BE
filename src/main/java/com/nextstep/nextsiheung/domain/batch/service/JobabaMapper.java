package com.nextstep.nextsiheung.domain.batch.service;

import com.nextstep.nextsiheung.domain.batch.dto.JobabaRow;
import com.nextstep.nextsiheung.domain.job.entity.JobPosting;
import com.nextstep.nextsiheung.global.common.enums.Education;
import com.nextstep.nextsiheung.global.common.enums.EmploymentType;
import com.nextstep.nextsiheung.global.common.enums.JobCategory;
import com.nextstep.nextsiheung.global.common.enums.JobSource;
import com.nextstep.nextsiheung.global.common.enums.Region;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

// 잡아바 row → JobPosting. 대상 지역(Region)이 아니거나 이미 마감된 공고는 버린다
@Component
public class JobabaMapper {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.BASIC_ISO_DATE;
    // 잡아바는 상시채용 마감일을 20700101로 준다
    private static final int ALWAYS_OPEN_YEAR = 2070;

    // 시·군 이름과 시군구 코드 앞 4자리(시흥 4139, 안산 4127, 부천 4119, 광명 4121, 인천 28)
    private static final Map<Region, List<String>> REGION_KEYS = new LinkedHashMap<>();

    static {
        REGION_KEYS.put(Region.SIHEUNG, List.of("시흥", "4139"));
        REGION_KEYS.put(Region.ANSAN, List.of("안산", "4127"));
        REGION_KEYS.put(Region.BUCHEON, List.of("부천", "4119"));
        REGION_KEYS.put(Region.GWANGMYEONG, List.of("광명", "4121"));
        REGION_KEYS.put(Region.INCHEON, List.of("인천", "28"));
    }

    // 모집분야명(없으면 공고 제목)에 들어 있는 단어로 직무를 고른다. 위에서부터 먼저 맞는 것
    private static final Map<JobCategory, List<String>> CATEGORY_KEYWORDS = new LinkedHashMap<>();

    static {
        CATEGORY_KEYWORDS.put(JobCategory.IT, List.of("정보통신", "소프트웨어", "프로그래", "개발자", "IT", "웹"));
        CATEGORY_KEYWORDS.put(JobCategory.DESIGN, List.of("디자인", "디자이너"));
        CATEGORY_KEYWORDS.put(JobCategory.OFFICE, List.of("경영", "사무", "회계", "경리", "금융", "보험", "행정", "총무", "인사", "구매"));
        CATEGORY_KEYWORDS.put(JobCategory.MANUFACTURING, List.of("생산", "제조", "설치", "정비", "기계", "건설", "용접", "가공", "포장", "MCT", "CNC"));
        CATEGORY_KEYWORDS.put(JobCategory.SERVICE, List.of("영업", "판매", "운전", "운송", "배송", "서비스", "음식", "조리", "미용", "돌봄", "경비", "청소", "매장", "고객", "상담"));
    }

    public boolean isOpen(JobabaRow row, LocalDate today) {
        Optional<LocalDate> end = parseDate(row.receiptEnd());
        return end.isPresent() && !end.get().isBefore(today);
    }

    public Optional<JobPosting> toEntity(JobabaRow row, LocalDate today, LocalDateTime collectedAt) {
        if (isBlank(row.url()) || row.url().length() > 500 || isBlank(row.companyName()) || isBlank(row.title())) {
            return Optional.empty();
        }
        if (!isOpen(row, today)) {
            return Optional.empty();
        }
        Optional<Region> region = region(row);
        if (region.isEmpty() || !acceptsNewcomer(row.career())) {
            return Optional.empty();
        }
        LocalDate deadline = parseDate(row.receiptEnd()).filter(d -> d.getYear() < ALWAYS_OPEN_YEAR).orElse(null);

        return Optional.of(JobPosting.builder()
                .source(JobSource.JOBABA)
                .externalId(sha256(row.url()))
                .company(cut(row.companyName(), 100))
                .title(cut(row.title(), 200))
                .region(region.get())
                .address(cut(row.regionName(), 200))
                .education(education(row.educationCode()))
                .careerLevel(JobPosting.CAREER_NEW)
                .employmentType(employmentType(row.form()))
                .jobCategory(jobCategory(row.field(), row.title()))
                .salary(cut(row.salary(), 100))
                .deadline(deadline)
                .originalUrl(row.url())
                .collectedAt(collectedAt)
                .build());
    }

    Optional<Region> region(JobabaRow row) {
        String name = row.regionName() == null ? "" : row.regionName();
        String code = row.regionCode() == null ? "" : row.regionCode();
        for (Map.Entry<Region, List<String>> e : REGION_KEYS.entrySet()) {
            if (name.contains(e.getValue().get(0)) || code.startsWith(e.getValue().get(1))) {
                return Optional.of(e.getKey());
            }
        }
        return Optional.empty();
    }

    // 학력 코드는 "03,04"처럼 여러 개가 올 수 있어서 가장 낮은 학력을 기준으로 한다
    // 00 무관, 01~03 초·중·고졸, 04 대졸(2~3년), 05 대졸(4년), 06 이상 대학원
    Education education(String codes) {
        if (isBlank(codes)) {
            return Education.ANY;
        }
        int min = Integer.MAX_VALUE;
        for (String code : codes.split(",")) {
            try {
                min = Math.min(min, Integer.parseInt(code.trim()));
            } catch (NumberFormatException ignored) {
                // 숫자가 아닌 코드는 건너뜀
            }
        }
        if (min == Integer.MAX_VALUE || min == 0) {
            return Education.ANY;
        }
        if (min <= 3) {
            return Education.HIGH_SCHOOL;
        }
        return min == 4 ? Education.COLLEGE : Education.UNIVERSITY;
    }

    // 명세 v1.4: 신입 가능 공고만 수집한다. 신입·신입/경력·무관은 통과, "경력"만 있으면 제외
    boolean acceptsNewcomer(String career) {
        return isBlank(career) || career.contains("신입") || career.contains("무관") || !career.contains("경력");
    }

    // 공고형태는 "정규직", "기간의 정함이 없는 근로계약/ 파견근로 비희망/ ..."처럼 자유 문구로 온다
    EmploymentType employmentType(String form) {
        if (isBlank(form)) {
            return EmploymentType.FULL_TIME;
        }
        if (form.contains("인턴")) {
            return EmploymentType.INTERN;
        }
        if (form.contains("시간제") || form.contains("파트") || form.contains("아르바이트")) {
            return EmploymentType.PART_TIME;
        }
        if (form.contains("정규") || form.contains("정함이 없는")) {
            return EmploymentType.FULL_TIME;
        }
        if (form.contains("계약") || form.contains("기간제") || form.contains("정함이 있는")) {
            return EmploymentType.CONTRACT;
        }
        return EmploymentType.FULL_TIME;
    }

    JobCategory jobCategory(String field, String title) {
        for (String text : new String[]{field, title}) {
            if (isBlank(text)) {
                continue;
            }
            for (Map.Entry<JobCategory, List<String>> e : CATEGORY_KEYWORDS.entrySet()) {
                if (e.getValue().stream().anyMatch(text::contains)) {
                    return e.getKey();
                }
            }
        }
        return JobCategory.ETC;
    }

    private Optional<LocalDate> parseDate(String yyyymmdd) {
        if (isBlank(yyyymmdd)) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(yyyymmdd.trim(), YYYYMMDD));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    private static String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String cut(String value, int max) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
