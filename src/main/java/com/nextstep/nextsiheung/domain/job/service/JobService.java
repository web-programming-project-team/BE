package com.nextstep.nextsiheung.domain.job.service;

import com.nextstep.nextsiheung.domain.calendar.entity.CalendarEvent;
import com.nextstep.nextsiheung.domain.calendar.repository.CalendarEventRepository;
import com.nextstep.nextsiheung.domain.job.dto.BookmarkResponse;
import com.nextstep.nextsiheung.domain.job.dto.BookmarkedJobResponse;
import com.nextstep.nextsiheung.domain.job.dto.HelpfulPolicyDto;
import com.nextstep.nextsiheung.domain.job.dto.JobDetailResponse;
import com.nextstep.nextsiheung.domain.job.dto.JobSummaryResponse;
import com.nextstep.nextsiheung.domain.job.dto.RecommendedJobsResponse;
import com.nextstep.nextsiheung.domain.job.entity.JobBookmark;
import com.nextstep.nextsiheung.domain.job.entity.JobPosting;
import com.nextstep.nextsiheung.domain.job.repository.JobBookmarkRepository;
import com.nextstep.nextsiheung.domain.job.repository.JobPolicyMatchRepository;
import com.nextstep.nextsiheung.domain.job.repository.JobPostingRepository;
import com.nextstep.nextsiheung.domain.policy.entity.Policy;
import com.nextstep.nextsiheung.domain.roadmap.repository.RoadmapItemRepository;
import com.nextstep.nextsiheung.domain.user.dto.JobPreferenceDto;
import com.nextstep.nextsiheung.domain.user.entity.JobPreference;
import com.nextstep.nextsiheung.domain.user.entity.User;
import com.nextstep.nextsiheung.domain.user.repository.JobPreferenceRepository;
import com.nextstep.nextsiheung.domain.user.repository.UserRepository;
import com.nextstep.nextsiheung.domain.user.repository.UserVisitRepository;
import com.nextstep.nextsiheung.global.common.Kst;
import com.nextstep.nextsiheung.global.common.enums.JobCategory;
import com.nextstep.nextsiheung.global.common.enums.ProgressStatus;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import com.nextstep.nextsiheung.global.response.PageResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 맞춤 채용: 조건 일치 공고 목록, 상세, 관심 공고 저장·해제·목록
@Service
@RequiredArgsConstructor
public class JobService {

    private static final int MAX_PAGE_SIZE = 50;

    private final JobPostingRepository jobPostingRepository;
    private final JobBookmarkRepository bookmarkRepository;
    private final JobPolicyMatchRepository matchRepository;
    private final JobPreferenceRepository jobPreferenceRepository;
    private final UserRepository userRepository;
    private final UserVisitRepository userVisitRepository;
    private final RoadmapItemRepository roadmapItemRepository;
    private final CalendarEventRepository calendarEventRepository;

    // 상태 등록(온보딩)을 마치지 않았거나 채용 조건이 없으면 ONBOARDING_REQUIRED
    @Transactional(readOnly = true)
    public RecommendedJobsResponse recommended(Long userId, String sort, int page, int size) {
        validatePage(page, size);
        if (!"deadline".equals(sort) && !"latest".equals(sort)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        JobPreference preference = preferenceOf(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ONBOARDING_REQUIRED));
        JobFilter f = JobFilter.of(preference);
        LocalDate today = Kst.today();
        PageRequest pageable = PageRequest.of(page, size);
        Page<JobPosting> jobs = "latest".equals(sort)
                ? jobPostingRepository.findMatchesByLatest(today, f.regions(), f.educations(), f.categories(),
                        f.employmentTypes(), pageable)
                : jobPostingRepository.findMatchesByDeadline(today, f.regions(), f.educations(), f.categories(),
                        f.employmentTypes(), pageable);

        List<Long> ids = jobs.getContent().stream().map(JobPosting::getId).toList();
        Set<Long> bookmarked = bookmarkedIds(userId, ids);
        Map<Long, List<HelpfulPolicyDto>> helpful = helpfulPolicies(userId, ids);
        LocalDateTime newSince = newSince(userId, today);

        List<JobSummaryResponse> content = jobs.getContent().stream()
                .map(j -> new JobSummaryResponse(j.getId(), j.getCompany(), j.getTitle(), j.getRegion(),
                        j.getEducation(), j.getCareerLevel(), j.getEmploymentType(), j.getJobCategory(),
                        j.getDeadline(), Kst.dDay(j.getDeadline()), bookmarked.contains(j.getId()),
                        newSince != null && !j.getCollectedAt().isBefore(newSince), j.getSource(),
                        helpful.getOrDefault(j.getId(), List.of())))
                .toList();
        return new RecommendedJobsResponse(toDto(preference), content, jobs.getNumber(), jobs.getSize(),
                jobs.getTotalElements(), jobs.getTotalPages(), jobs.hasNext());
    }

    // 로그인 선택. 비로그인이면 bookmarked=false, myStatus=null
    @Transactional(readOnly = true)
    public JobDetailResponse detail(Long userIdOrNull, Long jobId) {
        JobPosting j = findJob(jobId);
        boolean bookmarked = userIdOrNull != null && bookmarkRepository.existsByUserIdAndJobId(userIdOrNull, jobId);
        List<HelpfulPolicyDto> helpful = helpfulPolicies(userIdOrNull, List.of(jobId))
                .getOrDefault(jobId, List.of());
        // TODO(커뮤니티): post 엔티티가 생기면 같은 직무(jobCategory)의 합격 후기를 채운다
        return new JobDetailResponse(j.getId(), j.getCompany(), j.getTitle(), j.getRegion(), j.getAddress(),
                j.getEducation(), j.getCareerLevel(), j.getEmploymentType(), j.getJobCategory(), j.getSalary(),
                j.getDeadline(), Kst.dDay(j.getDeadline()), j.getOriginalUrl(), j.getSource(), bookmarked, helpful,
                List.of());
    }

    // 마감일이 있으면 캘린더에 JOB_DEADLINE 일정을 같이 만든다 (상시채용이면 calendarEventId=null)
    @Transactional
    public BookmarkResponse bookmark(Long userId, Long jobId) {
        JobPosting job = findJob(jobId);
        if (bookmarkRepository.existsByUserIdAndJobId(userId, jobId)) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS);
        }
        User user = userRepository.getReferenceById(userId);
        JobBookmark bookmark;
        try {
            bookmark = bookmarkRepository.saveAndFlush(new JobBookmark(user, job));
        } catch (DataIntegrityViolationException e) {
            // 같은 공고를 동시에 두 번 저장한 경우 (user_id + job_id UNIQUE)
            throw new BusinessException(ErrorCode.ALREADY_EXISTS);
        }
        Long eventId = null;
        if (job.getDeadline() != null) {
            eventId = calendarEventRepository.save(CalendarEvent.forBookmark(user, bookmark, job.getDeadline(),
                    job.getCompany() + " 채용 마감")).getId();
        }
        return new BookmarkResponse(jobId, true, eventId);
    }

    // 저장하지 않은 공고여도 204. 캘린더 일정은 DB가 함께 지운다(calendar_event.bookmark_id ON DELETE CASCADE)
    @Transactional
    public void unbookmark(Long userId, Long jobId) {
        bookmarkRepository.findByUserIdAndJobId(userId, jobId).ifPresent(bookmarkRepository::delete);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookmarkedJobResponse> bookmarks(Long userId, int page, int size) {
        validatePage(page, size);
        LocalDate today = Kst.today();
        return PageResponse.of(bookmarkRepository.findPageByUserId(userId, today, PageRequest.of(page, size))
                .map(b -> {
                    JobPosting j = b.getJob();
                    return new BookmarkedJobResponse(j.getId(), j.getCompany(), j.getTitle(), j.getDeadline(),
                            Kst.dDay(j.getDeadline()), j.isClosedOn(today));
                }));
    }

    // 홈 '새 맞춤 공고 N건': GET /jobs/recommended의 isNew와 같은 기준. 조건이 없거나 첫 방문이면 0
    @Transactional(readOnly = true)
    public int countNewMatches(Long userId) {
        LocalDate today = Kst.today();
        LocalDateTime since = newSince(userId, today);
        if (since == null) {
            return 0;
        }
        return preferenceOf(userId).map(p -> (int) countMatchesSince(p, today, since)).orElse(0);
    }

    // NEW_JOB 알림에서도 쓴다
    public long countMatchesSince(JobPreference preference, LocalDate today, LocalDateTime since) {
        JobFilter f = JobFilter.of(preference);
        return jobPostingRepository.countMatchesSince(today, f.regions(), f.educations(), f.categories(),
                f.employmentTypes(), since);
    }

    private Optional<JobPreference> preferenceOf(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        if (!user.isOnboardingCompleted()) {
            return Optional.empty();
        }
        return jobPreferenceRepository.findById(userId);
    }

    // 직전 방문일(오늘 이전 가장 최근 visit_date) 다음 날 0시부터 수집된 공고가 새 공고. 직전 방문이 없으면 null
    private LocalDateTime newSince(Long userId, LocalDate today) {
        LocalDate lastVisit = userVisitRepository.findLastVisitBefore(userId, today);
        return lastVisit == null ? null : lastVisit.plusDays(1).atStartOfDay();
    }

    private Set<Long> bookmarkedIds(Long userId, Collection<Long> jobIds) {
        if (userId == null || jobIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(bookmarkRepository.findBookmarkedJobIds(userId, jobIds));
    }

    // 공고별 도움 되는 지원 + 그 정책의 내 진행상태(현재 로드맵에 없거나 비로그인이면 null)
    private Map<Long, List<HelpfulPolicyDto>> helpfulPolicies(Long userIdOrNull, Collection<Long> jobIds) {
        if (jobIds.isEmpty()) {
            return Map.of();
        }
        // 매칭을 고를 때와 같은 순서: 직무가 맞는 정책 → 시흥·경기 기관 → 신청 마감 순(쿼리 순서 유지, 안정 정렬)
        List<Object[]> rows = new ArrayList<>(matchRepository.findPolicies(jobIds));
        rows.sort(Comparator.<Object[]>comparingInt(r -> JobPolicyMatchService.categoriesOf((Policy) r[2])
                        .contains((JobCategory) r[1]) ? 0 : 1)
                .thenComparingInt(r -> -JobPolicyMatchService.localScore((Policy) r[2])));
        Map<Long, ProgressStatus> statuses = new HashMap<>();
        if (userIdOrNull != null && !rows.isEmpty()) {
            Set<Long> policyIds = new LinkedHashSet<>();
            rows.forEach(r -> policyIds.add(((Policy) r[2]).getId()));
            for (Object[] s : roadmapItemRepository.findCurrentStatuses(userIdOrNull, policyIds)) {
                statuses.put((Long) s[0], (ProgressStatus) s[1]);
            }
        }
        Map<Long, List<HelpfulPolicyDto>> result = new LinkedHashMap<>();
        for (Object[] r : rows) {
            Policy policy = (Policy) r[2];
            result.computeIfAbsent((Long) r[0], k -> new ArrayList<>())
                    .add(new HelpfulPolicyDto(policy.getId(), policy.getTitle(), statuses.get(policy.getId())));
        }
        return result;
    }

    private JobPosting findJob(Long jobId) {
        return jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JOB_NOT_FOUND));
    }

    private static JobPreferenceDto toDto(JobPreference p) {
        return new JobPreferenceDto(List.copyOf(p.getRegions()), p.getEducation(), p.getCareerLevel(),
                List.copyOf(p.getJobCategories()), List.copyOf(p.getEmploymentTypes()));
    }

    private static void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
