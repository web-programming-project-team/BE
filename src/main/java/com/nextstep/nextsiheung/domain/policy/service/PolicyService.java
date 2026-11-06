package com.nextstep.nextsiheung.domain.policy.service;

import com.nextstep.nextsiheung.domain.policy.dto.PolicyDetailResponse;
import com.nextstep.nextsiheung.domain.policy.dto.PolicySummaryResponse;
import com.nextstep.nextsiheung.domain.policy.entity.Policy;
import com.nextstep.nextsiheung.domain.policy.repository.PolicyRepository;
import com.nextstep.nextsiheung.domain.policy.repository.PolicySpecs;
import com.nextstep.nextsiheung.domain.post.repository.PostRepository;
import com.nextstep.nextsiheung.domain.roadmap.repository.RoadmapItemRepository;
import com.nextstep.nextsiheung.global.common.Kst;
import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.ProgressStatus;
import com.nextstep.nextsiheung.global.common.enums.RecruitStatus;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import com.nextstep.nextsiheung.global.response.PageResponse;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 정책·프로그램 목록·상세 (로그인 선택: 로그인하면 내 진행상태가 함께 간다)
@Service
@RequiredArgsConstructor
public class PolicyService {

    static final int MAX_PAGE_SIZE = 50;

    private final PolicyRepository policyRepository;
    private final RoadmapItemRepository roadmapItemRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public PageResponse<PolicySummaryResponse> list(Long userIdOrNull, InterestField field,
                                                    RecruitStatus recruitStatus, String keyword, String sort,
                                                    int page, int size) {
        LocalDate today = Kst.today();
        // 모집상태를 안 주면 접수 중·접수 예정 둘 다
        List<RecruitStatus> statuses = recruitStatus == null
                ? List.of(RecruitStatus.OPEN, RecruitStatus.UPCOMING)
                : List.of(recruitStatus);
        Specification<Policy> spec = and(PolicySpecs.statusIn(statuses, today), PolicySpecs.field(field),
                PolicySpecs.keyword(keyword));
        Page<Policy> result = policyRepository.findAll(spec, pageRequest(page, size, sortOf(sort)));

        Map<Long, ProgressStatus> myStatus = myStatuses(userIdOrNull, result.getContent());
        return PageResponse.of(result.map(p -> new PolicySummaryResponse(p.getId(), p.getTitle(), p.getField(),
                p.getAgency(), p.getApplyStartDate(), p.getApplyEndDate(), p.currentStatus(today),
                Kst.dDay(p.getApplyEndDate()), myStatus.get(p.getId()))));
    }

    @Transactional(readOnly = true)
    public PolicyDetailResponse detail(Long userIdOrNull, Long policyId) {
        Policy p = policyRepository.findById(policyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POLICY_NOT_FOUND));
        PolicyDetailResponse.MyItem myItem = userIdOrNull == null ? null
                : roadmapItemRepository.findCurrent(userIdOrNull, policyId)
                        .map(i -> new PolicyDetailResponse.MyItem(i.getId(), i.getStatus()))
                        .orElse(null);
        List<PolicyDetailResponse.ReviewSummary> reviews = postRepository
                .findTop3ByPolicyIdAndDeletedAtIsNullAndHiddenFalseOrderByCreatedAtDescIdDesc(policyId).stream()
                .map(r -> new PolicyDetailResponse.ReviewSummary(r.getId(), r.getType(), r.getRating(),
                        r.getOneLiner(), r.getPrepPeriod(), r.getAuthorStage(), r.getCreatedAt()))
                .toList();
        long reviewCount = postRepository.countByPolicyIdAndDeletedAtIsNullAndHiddenFalse(policyId);
        // TODO(채용): job_policy_match 엔티티가 생기면 연결 채용공고 최대 2개를 채운다
        return new PolicyDetailResponse(p.getId(), p.getTitle(), p.getField(), p.getAgency(), p.getTarget(),
                p.getContent(), p.getApplyStartDate(), p.getApplyEndDate(), Kst.dDay(p.getApplyEndDate()),
                p.currentStatus(Kst.today()), p.getContact(), p.getSource(), p.getSourceUrl(), p.getApplyUrl(),
                p.getLastVerifiedAt(), myItem, reviews, reviewCount, List.of());
    }

    private Map<Long, ProgressStatus> myStatuses(Long userIdOrNull, List<Policy> policies) {
        Map<Long, ProgressStatus> map = new HashMap<>();
        if (userIdOrNull == null || policies.isEmpty()) {
            return map;
        }
        List<Long> ids = policies.stream().map(Policy::getId).toList();
        for (Object[] row : roadmapItemRepository.findCurrentStatuses(userIdOrNull, ids)) {
            map.put((Long) row[0], (ProgressStatus) row[1]);
        }
        return map;
    }

    // deadline(기본): 마감 가까운 순, 상시 모집(마감일 없음)은 뒤로 · latest: 최근 등록순
    private static Sort sortOf(String sort) {
        if ("latest".equals(sort)) {
            return Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        }
        if (sort == null || "deadline".equals(sort)) {
            return Sort.by(Sort.Order.asc("applyEndDate").nullsLast(), Sort.Order.asc("id"));
        }
        throw new BusinessException(ErrorCode.INVALID_INPUT);
    }

    static PageRequest pageRequest(int page, int size, Sort sort) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return PageRequest.of(page, size, sort);
    }

    // null인 조건은 빼고 AND로 묶는다
    @SafeVarargs
    public static Specification<Policy> and(Specification<Policy>... specs) {
        Specification<Policy> result = PolicySpecs.all();
        for (Specification<Policy> s : specs) {
            if (s != null) {
                result = result.and(s);
            }
        }
        return result;
    }
}
