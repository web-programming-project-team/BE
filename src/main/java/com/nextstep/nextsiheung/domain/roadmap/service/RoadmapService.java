package com.nextstep.nextsiheung.domain.roadmap.service;

import com.nextstep.nextsiheung.domain.calendar.repository.CalendarEventRepository;
import com.nextstep.nextsiheung.domain.calendar.service.CalendarService;
import com.nextstep.nextsiheung.domain.job.service.JobService;
import com.nextstep.nextsiheung.domain.notification.service.NotificationService;
import com.nextstep.nextsiheung.domain.policy.entity.Policy;
import com.nextstep.nextsiheung.domain.policy.repository.PolicyRepository;
import com.nextstep.nextsiheung.domain.roadmap.dto.HomeSummaryResponse;
import com.nextstep.nextsiheung.domain.roadmap.dto.ProgressDto;
import com.nextstep.nextsiheung.domain.roadmap.dto.RoadmapHistoryResponse;
import com.nextstep.nextsiheung.domain.roadmap.dto.RoadmapItemAddResponse;
import com.nextstep.nextsiheung.domain.roadmap.dto.RoadmapResponse;
import com.nextstep.nextsiheung.domain.roadmap.dto.StatusChangeResponse;
import com.nextstep.nextsiheung.domain.roadmap.entity.Roadmap;
import com.nextstep.nextsiheung.domain.roadmap.entity.RoadmapItem;
import com.nextstep.nextsiheung.domain.roadmap.entity.RoadmapItemHistory;
import com.nextstep.nextsiheung.domain.roadmap.entity.RoadmapStep;
import com.nextstep.nextsiheung.domain.roadmap.entity.RoadmapTemplate;
import com.nextstep.nextsiheung.domain.roadmap.entity.TemplateStep;
import com.nextstep.nextsiheung.domain.roadmap.repository.RoadmapItemHistoryRepository;
import com.nextstep.nextsiheung.domain.roadmap.repository.RoadmapItemRepository;
import com.nextstep.nextsiheung.domain.roadmap.repository.RoadmapRepository;
import com.nextstep.nextsiheung.domain.roadmap.repository.RoadmapTemplateRepository;
import com.nextstep.nextsiheung.domain.user.entity.User;
import com.nextstep.nextsiheung.global.common.EnumLists;
import com.nextstep.nextsiheung.global.common.Kst;
import com.nextstep.nextsiheung.global.common.enums.EventType;
import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.ProgressStatus;
import com.nextstep.nextsiheung.global.common.enums.RecruitStatus;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 내 로드맵: 규칙 기반 생성, 조회, 진행상태 변경, 정책 추가·삭제, 활동 기록, 메인 요약
@Slf4j
@Service
@RequiredArgsConstructor
public class RoadmapService {

    static final String DEFAULT_TEMPLATE_NAME = "기본 로드맵";
    static final String USER_ADDED_STEP_TITLE = "직접 추가한 정책";
    private static final int NEXT_ACTION_LIMIT = 3;
    private static final int DEADLINE_LIMIT = 5;
    private static final int DEADLINE_DAYS = 14;
    // 다음 할 일 우선순위: 신청 예정 → 관심 → 확인 필요
    private static final Map<ProgressStatus, Integer> NEXT_ACTION_ORDER = Map.of(
            ProgressStatus.PLANNED, 0, ProgressStatus.INTERESTED, 1, ProgressStatus.TO_CHECK, 2);

    private final RoadmapRepository roadmapRepository;
    private final RoadmapTemplateRepository templateRepository;
    private final RoadmapItemRepository itemRepository;
    private final RoadmapItemHistoryRepository historyRepository;
    private final PolicyRepository policyRepository;
    private final CalendarService calendarService;
    private final CalendarEventRepository calendarEventRepository;
    private final JobService jobService;
    private final NotificationService notificationService;

    // ───────── 생성 ─────────

    // 명세 '로드맵 생성 규칙': 상황·관심분야가 가장 많이 겹치는 템플릿을 고르고(같으면 먼저 만든 것),
    // 단계마다 연결된 정책 중 ENDED가 아닌 것을 TO_CHECK 항목으로 복사한다. 항목마다 접수 일정도 캘린더에 넣는다
    @Transactional
    public Roadmap createFor(User user) {
        Optional<RoadmapTemplate> best = templateRepository.findAll().stream()
                .max(Comparator.comparingInt((RoadmapTemplate t) -> t.matchScore(user.getLifeStages(),
                                user.getInterests()))
                        .thenComparing(RoadmapTemplate::getId, Comparator.reverseOrder()));
        if (best.isEmpty()) {
            log.warn("로드맵 템플릿이 없어 빈 로드맵을 만듭니다. app.seed.roadmap-templates 설정을 확인하세요");
        }
        Roadmap roadmap = new Roadmap(user, best.orElse(null),
                best.map(RoadmapTemplate::getName).orElse(DEFAULT_TEMPLATE_NAME));
        best.ifPresent(t -> {
            for (TemplateStep ts : t.getSteps()) {
                RoadmapStep step = roadmap.addStep(ts.getTitle());
                ts.getPolicies().stream()
                        .filter(p -> !p.isEnded())
                        .sorted(Comparator.comparing(Policy::getId))
                        .forEach(p -> step.addItem(p, ProgressStatus.TO_CHECK, false));
            }
        });
        roadmapRepository.save(roadmap);
        roadmap.items().forEach(item -> calendarService.addRoadmapItemEvents(user, item));
        return roadmap;
    }

    // 상황 변경: 현재 로드맵을 활동 기록으로 보관하고(자동 일정은 캘린더에서 뺌) 새 로드맵을 만든다
    @Transactional
    public Roadmap archiveAndRecreate(User user, Roadmap current) {
        current.archive(Kst.now());
        calendarService.removeRoadmapEvents(current.getId());
        return createFor(user);
    }

    // ───────── 조회 ─────────

    @Transactional(readOnly = true)
    public Optional<Roadmap> findCurrent(Long userId) {
        return roadmapRepository.findFirstByUser_IdAndArchivedFalseOrderByIdDesc(userId);
    }

    // 상태 등록 전이면 ONBOARDING_REQUIRED
    @Transactional(readOnly = true)
    public Roadmap current(Long userId) {
        return findCurrent(userId).orElseThrow(() -> new BusinessException(ErrorCode.ONBOARDING_REQUIRED));
    }

    @Transactional(readOnly = true)
    public RoadmapResponse myRoadmap(Long userId) {
        return toResponse(current(userId));
    }

    // 본인 로드맵만. 현재 로드맵 ID를 넣어도 같은 형식(archived=false)
    @Transactional(readOnly = true)
    public RoadmapResponse detail(Long userId, Long roadmapId) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROADMAP_NOT_FOUND));
        if (!roadmap.isOwnedBy(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return toResponse(roadmap);
    }

    @Transactional(readOnly = true)
    public List<RoadmapHistoryResponse> history(Long userId) {
        return roadmapRepository.findAllByUser_IdAndArchivedTrueOrderByArchivedAtDesc(userId).stream()
                .map(r -> {
                    ProgressDto progress = progress(r);
                    return new RoadmapHistoryResponse(r.getId(), r.getTemplateName(), r.getArchivedAt(),
                            progress.rate(), progress.completed());
                })
                .toList();
    }

    // ───────── 변경 ─────────

    // 단계 건너뛰기·되돌리기 모두 허용. COMPLETED로 바뀔 때만 suggestReview=true
    @Transactional
    public StatusChangeResponse changeStatus(Long userId, Long itemId, ProgressStatus status) {
        RoadmapItem item = ownedEditableItem(userId, itemId);
        ProgressStatus before = item.getStatus();
        if (before != status) {
            item.changeStatus(status);
            historyRepository.save(new RoadmapItemHistory(item, before, status, Kst.now()));
        }
        boolean suggestReview = status == ProgressStatus.COMPLETED && before != ProgressStatus.COMPLETED;
        return new StatusChangeResponse(item.getId(), item.getStatus(), progress(item.roadmap()), suggestReview);
    }

    // 정책 상세에서 로드맵에 없는 정책의 진행상태를 처음 고를 때. 분야가 맞는 단계에 넣고 접수 일정을 캘린더에 등록한다
    @Transactional
    public RoadmapItemAddResponse addItem(Long userId, Long policyId, ProgressStatus status) {
        Roadmap roadmap = current(userId);
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POLICY_NOT_FOUND));
        if (policy.isEnded()) {
            throw new BusinessException(ErrorCode.POLICY_ENDED);
        }
        if (roadmap.items().anyMatch(i -> i.getPolicy().getId().equals(policyId))) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS);
        }
        RoadmapStep step = stepFor(roadmap, policy.getField());
        RoadmapItem item = step.addItem(policy, status, true);
        // 새 단계가 생겼을 수도 있어서 로드맵 쪽 cascade로 저장한다 (flush 후 item ID가 생김)
        roadmapRepository.flush();
        historyRepository.save(new RoadmapItemHistory(item, null, status, Kst.now()));
        List<Long> eventIds = calendarService.addRoadmapItemEvents(roadmap.getUser(), item);
        return new RoadmapItemAddResponse(item.getId(), policyId, step.getStepOrder(), item.getStatus(), eventIds);
    }

    // 직접 추가한 항목만 뺀다(3순위). 연결된 캘린더 일정·활동 기록은 DB가 함께 지운다(ON DELETE CASCADE)
    @Transactional
    public void deleteItem(Long userId, Long itemId) {
        RoadmapItem item = ownedEditableItem(userId, itemId);
        if (!item.isUserAdded()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        item.getStep().removeItem(item);
        itemRepository.delete(item);
    }

    // ───────── 메인 요약 ─────────

    @Transactional(readOnly = true)
    public HomeSummaryResponse homeSummary(Long userId) {
        Roadmap roadmap = current(userId);
        User user = roadmap.getUser();
        LocalDate today = Kst.today();

        List<HomeSummaryResponse.NextAction> nextActions = roadmap.items()
                .filter(i -> NEXT_ACTION_ORDER.containsKey(i.getStatus()))
                .filter(i -> isApplicable(i.getPolicy().currentStatus(today)))
                .sorted(Comparator.comparing((RoadmapItem i) -> NEXT_ACTION_ORDER.get(i.getStatus()))
                        .thenComparing(i -> i.getPolicy().getApplyEndDate(),
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(NEXT_ACTION_LIMIT)
                .map(i -> new HomeSummaryResponse.NextAction(i.getId(), i.getPolicy().getId(),
                        i.getPolicy().getTitle(), i.getStatus(), actionLabel(i),
                        Kst.dDay(i.getPolicy().getApplyEndDate())))
                .toList();

        List<HomeSummaryResponse.UpcomingDeadline> deadlines = calendarEventRepository
                .findDeadlines(userId, today, today.plusDays(DEADLINE_DAYS),
                        List.of(EventType.APPLY_END, EventType.JOB_DEADLINE), PageRequest.of(0, DEADLINE_LIMIT))
                .stream()
                .map(e -> new HomeSummaryResponse.UpcomingDeadline(e.getType(), e.getTitle(), e.getDate(),
                        Kst.dDay(e.getDate()), e.getSourceType(), e.sourceId()))
                .toList();

        // 직전 방문일 이후 조건에 맞는 새 공고 수 (GET /jobs/recommended의 isNew와 같은 기준)
        int newJobCount = jobService.countNewMatches(userId);
        int unreadNotificationCount = (int) notificationService.countUnread(userId);

        return new HomeSummaryResponse(user.getNickname(), EnumLists.sorted(user.getLifeStages()),
                EnumLists.sorted(user.getInterests()), progress(roadmap), nextActions, deadlines, newJobCount,
                unreadNotificationCount);
    }

    // ───────── 응답 조립 ─────────

    RoadmapResponse toResponse(Roadmap roadmap) {
        LocalDate today = Kst.today();
        List<RoadmapResponse.Step> steps = new ArrayList<>();
        Integer currentStep = null;
        for (RoadmapStep s : roadmap.getSteps()) {
            boolean hasOpenItem = s.getItems().stream().anyMatch(i -> i.getStatus() != ProgressStatus.COMPLETED);
            if (currentStep == null && hasOpenItem) {
                currentStep = s.getStepOrder();
            }
            steps.add(new RoadmapResponse.Step(s.getStepOrder(), s.getTitle(), s.isCompleted(),
                    s.getItems().stream().map(i -> toItem(i, today)).toList()));
        }
        // 모든 항목을 끝냈으면 마지막 단계
        if (currentStep == null && !steps.isEmpty()) {
            currentStep = steps.getLast().stepOrder();
        }
        return new RoadmapResponse(roadmap.getId(), roadmap.getTemplateName(), currentStep, roadmap.isArchived(),
                roadmap.getArchivedAt(), progress(roadmap), steps);
    }

    // 진행률 = 완료 항목 / 전체 항목. 완료=COMPLETED, 남음=TO_CHECK(아직 손대지 않음), 진행 중=그 사이 상태
    static ProgressDto progress(Roadmap roadmap) {
        int completed = 0;
        int remaining = 0;
        int inProgress = 0;
        for (RoadmapItem i : roadmap.items().toList()) {
            switch (i.getStatus()) {
                case COMPLETED -> completed++;
                case TO_CHECK -> remaining++;
                default -> inProgress++;
            }
        }
        int total = completed + remaining + inProgress;
        int rate = total == 0 ? 0 : Math.round(completed * 100f / total);
        return new ProgressDto(rate, completed, inProgress, remaining);
    }

    private static RoadmapResponse.Item toItem(RoadmapItem i, LocalDate today) {
        Policy p = i.getPolicy();
        return new RoadmapResponse.Item(i.getId(), p.getId(), p.getTitle(), p.getAgency(), i.getStatus(),
                p.getApplyStartDate(), p.getApplyEndDate(), Kst.dDay(p.getApplyEndDate()), p.currentStatus(today),
                p.getApplyUrl());
    }

    private RoadmapItem ownedEditableItem(Long userId, Long itemId) {
        RoadmapItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROADMAP_ITEM_NOT_FOUND));
        Roadmap roadmap = item.roadmap();
        // 남의 항목, 보관된(읽기 전용) 로드맵의 항목은 바꿀 수 없다
        if (!roadmap.isOwnedBy(userId) || roadmap.isArchived()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return item;
    }

    // 단계에는 분야 칸이 없어서, 같은 분야 정책이 이미 들어 있는 첫 단계 → 템플릿에서 같은 분야 정책이 연결된 단계
    // → 마지막 단계 순으로 고른다. 단계가 하나도 없으면 '직접 추가한 정책' 단계를 만든다
    private static RoadmapStep stepFor(Roadmap roadmap, InterestField field) {
        for (RoadmapStep s : roadmap.getSteps()) {
            if (s.getItems().stream().anyMatch(i -> i.getPolicy().getField() == field)) {
                return s;
            }
        }
        if (roadmap.getTemplate() != null) {
            for (TemplateStep ts : roadmap.getTemplate().getSteps()) {
                if (ts.getPolicies().stream().anyMatch(p -> p.getField() == field)) {
                    int order = ts.getStepOrder();
                    Optional<RoadmapStep> match = roadmap.getSteps().stream()
                            .filter(s -> s.getStepOrder() == order).findFirst();
                    if (match.isPresent()) {
                        return match.get();
                    }
                }
            }
        }
        if (roadmap.getSteps().isEmpty()) {
            return roadmap.addStep(USER_ADDED_STEP_TITLE);
        }
        return roadmap.getSteps().getLast();
    }

    private static boolean isApplicable(RecruitStatus status) {
        return status == RecruitStatus.OPEN || status == RecruitStatus.UPCOMING;
    }

    // 다음 할 일 카드의 버튼 문구
    private static String actionLabel(RoadmapItem i) {
        return switch (i.getStatus()) {
            case PLANNED -> "공식 사이트에서 신청하기";
            case INTERESTED -> i.getPolicy().getField() == InterestField.COUNSELING
                    ? "상담 일정 확인하기" : "신청 기간 확인하기";
            default -> "지원 대상 확인하기";
        };
    }
}
