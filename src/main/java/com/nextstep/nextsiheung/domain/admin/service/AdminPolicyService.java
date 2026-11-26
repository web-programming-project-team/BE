package com.nextstep.nextsiheung.domain.admin.service;

import com.nextstep.nextsiheung.domain.admin.dto.AdminPolicyRequest;
import com.nextstep.nextsiheung.domain.admin.dto.AdminPolicyResponse;
import com.nextstep.nextsiheung.domain.admin.dto.AdminPolicySaveResponse;
import com.nextstep.nextsiheung.domain.admin.dto.PolicyEndResponse;
import com.nextstep.nextsiheung.domain.policy.entity.Policy;
import com.nextstep.nextsiheung.domain.policy.repository.PolicyRepository;
import com.nextstep.nextsiheung.domain.policy.repository.PolicySpecs;
import com.nextstep.nextsiheung.domain.policy.service.PolicyService;
import com.nextstep.nextsiheung.domain.roadmap.repository.RoadmapItemRepository;
import com.nextstep.nextsiheung.global.common.Kst;
import com.nextstep.nextsiheung.global.common.enums.PolicySource;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 관리자 정책·프로그램 관리: 목록, 등록, 수정, 종료, 정보 확인 완료
// 시흥 지역 프로그램처럼 API로 받을 수 없는 정책을 source=ADMIN으로 직접 등록한다
@Service
@RequiredArgsConstructor
public class AdminPolicyService {

    private final PolicyRepository policyRepository;
    private final RoadmapItemRepository roadmapItemRepository;

    @Transactional(readOnly = true)
    public PageResponse<AdminPolicyResponse> list(PolicySource source, RecruitStatus recruitStatus, Boolean stale,
                                                  String keyword, int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        LocalDate today = Kst.today();
        Page<Policy> result = policyRepository.findAll(
                PolicyService.and(PolicySpecs.source(source),
                        recruitStatus == null ? null : PolicySpecs.statusIn(List.of(recruitStatus), today),
                        PolicySpecs.stale(stale, today), PolicySpecs.keyword(keyword)),
                PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))));

        Map<Long, Long> counts = new HashMap<>();
        List<Long> ids = result.getContent().stream().map(Policy::getId).toList();
        if (!ids.isEmpty()) {
            for (Object[] row : roadmapItemRepository.countUsersByPolicy(ids)) {
                counts.put((Long) row[0], (Long) row[1]);
            }
        }
        return PageResponse.of(result.map(p -> new AdminPolicyResponse(p.getId(), p.getTitle(), p.getAgency(),
                p.getSource(), p.currentStatus(today), p.getApplyEndDate(),
                p.getCreatedAt() == null ? null : p.getCreatedAt().toLocalDate(), p.getLastVerifiedAt(),
                p.isStale(today), counts.getOrDefault(p.getId(), 0L))));
    }

    @Transactional
    public AdminPolicySaveResponse create(Long adminId, AdminPolicyRequest r) {
        validatePeriod(r);
        Policy policy = policyRepository.save(Policy.builder()
                .source(PolicySource.ADMIN)
                .title(r.title().trim()).field(r.field()).agency(r.agency().trim())
                .target(r.target()).content(r.content()).contact(r.contact())
                .applyStartDate(r.applyStartDate()).applyEndDate(r.applyEndDate())
                .sourceUrl(r.sourceUrl().trim()).applyUrl(r.applyUrl())
                .lifeStages(r.lifeStages()).tags(r.tags())
                .createdBy(adminId)
                .today(Kst.today())
                .build());
        return saved(policy);
    }

    // 본문 전체 교체. 저장하면 마지막 확인일이 오늘로 바뀌고, 로드맵 화면은 정책을 그대로 참조하므로 바로 반영된다
    @Transactional
    public AdminPolicySaveResponse update(Long policyId, AdminPolicyRequest r) {
        validatePeriod(r);
        Policy policy = find(policyId);
        policy.update(r.title().trim(), r.field(), r.agency().trim(), r.target(), r.content(), r.contact(),
                r.applyStartDate(), r.applyEndDate(), r.sourceUrl().trim(), r.applyUrl(), r.lifeStages(), r.tags(),
                Kst.today());
        return saved(policy);
    }

    // ENDED로 바꾼다. 새 로드맵에는 들어가지 않고, 이미 담은 사용자에게는 recruitStatus=ENDED로 보인다
    @Transactional
    public PolicyEndResponse end(Long policyId, String reason) {
        Policy policy = find(policyId);
        policy.end(reason);
        return new PolicyEndResponse(policy.getId(), policy.getRecruitStatus());
    }

    @Transactional
    public AdminPolicySaveResponse verify(Long policyId) {
        Policy policy = find(policyId);
        policy.verify(Kst.today());
        return saved(policy);
    }

    private Policy find(Long policyId) {
        return policyRepository.findById(policyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POLICY_NOT_FOUND));
    }

    private static AdminPolicySaveResponse saved(Policy p) {
        return new AdminPolicySaveResponse(p.getId(), p.currentStatus(Kst.today()), p.getLastVerifiedAt());
    }

    private static void validatePeriod(AdminPolicyRequest r) {
        if (r.applyStartDate() != null && r.applyEndDate() != null && r.applyEndDate().isBefore(r.applyStartDate())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
