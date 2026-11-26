package com.nextstep.nextsiheung.domain.job.service;

import com.nextstep.nextsiheung.domain.job.entity.JobPosting;
import com.nextstep.nextsiheung.domain.job.repository.JobPostingRepository;
import com.nextstep.nextsiheung.domain.policy.entity.Policy;
import com.nextstep.nextsiheung.domain.policy.repository.PolicyRepository;
import com.nextstep.nextsiheung.global.common.Kst;
import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.JobCategory;
import com.nextstep.nextsiheung.global.common.enums.RecruitStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 공고별 '이 공고에 도움 되는 지원'(job_policy_match)을 규칙으로 계산한다
// 1) 대상 정책: 분야가 취업(EMPLOYMENT)·교육(EDUCATION)이고, 종료(ENDED)·접수 마감(CLOSED)이 아닌 정책
// 2) 정책 태그에 직무 키워드가 있으면 그 직무 공고에만, 없으면 모든 공고에 도움 되는 정책으로 본다
//    단, 모든 공고용은 제목·태그에 취업 준비 키워드(취업·직무·인턴·자격·훈련·채용·일자리·면접)가 있는 것만
//    (학자금 대출처럼 교육 분야지만 취업 준비와 거리가 먼 정책이 붙지 않도록)
// 3) 공고 하나에 최대 3개. 직무가 맞는 정책 먼저, 그다음 시흥·경기 기관, 그다음 신청 마감이 가까운 순
// 다시 계산하는 때: 서버 시작 직후·잡아바 수집 직후(전체), 관리자 공고 등록·수정(그 공고만)
@Slf4j
@Service
@RequiredArgsConstructor
public class JobPolicyMatchService {

    static final int MAX_PER_JOB = 3;
    private static final Set<InterestField> FIELDS = EnumSet.of(InterestField.EMPLOYMENT, InterestField.EDUCATION);
    private static final Set<RecruitStatus> ACTIVE = EnumSet.of(RecruitStatus.UPCOMING, RecruitStatus.OPEN);
    // 정책 태그에서 찾는 직무 키워드 (태그에 이 글자가 들어 있으면 그 직무 정책)
    private static final Map<JobCategory, List<String>> KEYWORDS = new EnumMap<>(Map.of(
            JobCategory.OFFICE, List.of("사무"),
            JobCategory.IT, List.of("it"),
            JobCategory.MANUFACTURING, List.of("제조"),
            JobCategory.SERVICE, List.of("서비스"),
            JobCategory.DESIGN, List.of("디자인")));
    // 모든 공고용 정책으로 쓸 수 있는 취업 준비 키워드
    private static final List<String> JOB_PREP_WORDS =
            List.of("취업", "직무", "인턴", "자격", "훈련", "채용", "일자리", "면접", "구직");

    private final PolicyRepository policyRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JdbcTemplate jdbc;

    // 접수 중인 공고 전체를 다시 계산한다. 결과는 직무별로 같으므로 직무마다 한 번만 고른다
    @Transactional
    public void rebuildAll() {
        LocalDate today = Kst.today();
        Map<JobCategory, List<Long>> byCategory = picksByCategory(today);
        List<Object[]> rows = new ArrayList<>();
        for (Object[] job : jobPostingRepository.findOpenJobCategories(today)) {
            for (Long policyId : byCategory.getOrDefault((JobCategory) job[1], List.of())) {
                rows.add(new Object[]{job[0], policyId});
            }
        }
        jdbc.update("delete from job_policy_match");
        jdbc.batchUpdate("insert into job_policy_match (job_id, policy_id) values (?, ?)", rows);
        log.info("[공고-정책 매칭] 전체 다시 계산: {}건", rows.size());
    }

    @Transactional
    public void rebuildFor(JobPosting job) {
        jdbc.update("delete from job_policy_match where job_id = ?", job.getId());
        List<Object[]> rows = picksByCategory(Kst.today()).getOrDefault(job.getJobCategory(), List.of()).stream()
                .map(policyId -> new Object[]{job.getId(), policyId})
                .toList();
        jdbc.batchUpdate("insert into job_policy_match (job_id, policy_id) values (?, ?)", rows);
    }

    private Map<JobCategory, List<Long>> picksByCategory(LocalDate today) {
        Comparator<Policy> order = Comparator.comparingInt(JobPolicyMatchService::localScore).reversed()
                .thenComparing(Policy::getApplyEndDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Policy::getId);
        List<Policy> candidates = policyRepository.findAll().stream()
                .filter(p -> FIELDS.contains(p.getField()) && ACTIVE.contains(p.currentStatus(today)))
                .sorted(order)
                .toList();

        Map<JobCategory, List<Long>> picks = new EnumMap<>(JobCategory.class);
        for (JobCategory category : JobCategory.values()) {
            List<Long> chosen = new ArrayList<>();
            // 같은 사업을 여러 기관이 각각 올린 정책은 이름(공백 제외)으로 한 번만
            Set<String> titles = new HashSet<>();
            // 직무가 맞는 정책 먼저, 남는 자리는 직무 태그가 없는(모든 공고용) 정책
            candidates.stream().filter(p -> categoriesOf(p).contains(category))
                    .forEach(p -> addUpTo(chosen, titles, p));
            candidates.stream().filter(p -> categoriesOf(p).isEmpty() && isJobPrep(p))
                    .forEach(p -> addUpTo(chosen, titles, p));
            picks.put(category, chosen);
        }
        return picks;
    }

    // 시흥 기관 2, 경기 기관 1, 그 외 0. 같은 조건이면 지역 정책을 먼저 보여 준다
    public static int localScore(Policy p) {
        String agency = p.getAgency();
        return agency.contains("시흥") ? 2 : agency.contains("경기") ? 1 : 0;
    }

    static boolean isJobPrep(Policy p) {
        String text = p.getTitle() + " " + String.join(" ", p.getTags());
        return JOB_PREP_WORDS.stream().anyMatch(text::contains);
    }

    // 정책 태그에 들어 있는 직무 키워드 → 직무. 비어 있으면 모든 공고에 도움 되는 정책
    public static Set<JobCategory> categoriesOf(Policy policy) {
        Set<JobCategory> found = EnumSet.noneOf(JobCategory.class);
        for (String tag : policy.getTags()) {
            String lower = tag.toLowerCase(Locale.ROOT);
            KEYWORDS.forEach((category, words) -> {
                if (words.stream().anyMatch(lower::contains)) {
                    found.add(category);
                }
            });
        }
        return found;
    }

    private static void addUpTo(List<Long> chosen, Set<String> titles, Policy policy) {
        if (chosen.size() < MAX_PER_JOB && !chosen.contains(policy.getId())
                && titles.add(policy.getTitle().replaceAll("\\s+", ""))) {
            chosen.add(policy.getId());
        }
    }
}
