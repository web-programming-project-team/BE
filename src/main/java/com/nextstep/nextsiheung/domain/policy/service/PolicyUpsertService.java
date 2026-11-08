package com.nextstep.nextsiheung.domain.policy.service;

import com.nextstep.nextsiheung.domain.batch.service.OntongMapper.PolicyFields;
import com.nextstep.nextsiheung.domain.policy.entity.Policy;
import com.nextstep.nextsiheung.domain.policy.repository.PolicyRepository;
import com.nextstep.nextsiheung.global.common.enums.PolicySource;
import com.nextstep.nextsiheung.global.common.enums.RecruitStatus;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 수집한 온통청년 정책을 externalId(plcyNo) 기준으로 새로 넣거나 갱신한다
// 갱신하면 마지막 확인일이 오늘로 바뀌고, 관리자가 종료(ENDED)한 정책은 종료 상태를 유지한다(Policy.update 규칙)
@Service
@RequiredArgsConstructor
public class PolicyUpsertService {

    private final PolicyRepository policyRepository;

    @Transactional
    public Result upsertAll(List<PolicyFields> collected, LocalDate today) {
        Map<String, PolicyFields> byExternalId = new LinkedHashMap<>();
        collected.forEach(p -> byExternalId.put(p.externalId(), p));

        Map<String, Policy> existing = new HashMap<>();
        for (Policy saved : policyRepository.findByExternalIdIn(byExternalId.keySet())) {
            existing.put(saved.getExternalId(), saved);
        }

        int inserted = 0;
        int updated = 0;
        for (PolicyFields p : byExternalId.values()) {
            Policy saved = existing.get(p.externalId());
            if (saved == null) {
                policyRepository.save(Policy.builder()
                        .source(PolicySource.ONTONG)
                        .externalId(p.externalId())
                        .title(p.title()).field(p.field()).agency(p.agency())
                        .target(p.target()).content(p.content())
                        .applyStartDate(p.applyStartDate()).applyEndDate(p.applyEndDate())
                        .sourceUrl(p.sourceUrl()).applyUrl(p.applyUrl())
                        .tags(p.tags())
                        .today(today)
                        .build());
                inserted++;
            } else {
                // 대상 상황(lifeStages)은 null로 넘겨 기존 값을 그대로 둔다. 문의처도 기존 값 유지
                saved.update(p.title(), p.field(), p.agency(), p.target(), p.content(), saved.getContact(),
                        p.applyStartDate(), p.applyEndDate(), p.sourceUrl(), p.applyUrl(), null, p.tags(), today);
                updated++;
            }
        }
        return new Result(inserted, updated);
    }

    // 이번 수집에서 신청 가능 정책으로 오지 않은 온통청년 정책 중 아직 접수 중·예정인 것을 종료한다
    // (온통청년에서 내려갔거나 수집 대상에서 빠진 정책. 접수기간이 지나 CLOSED인 정책은 그대로 둔다)
    @Transactional
    public int endMissing(Set<String> acceptedExternalIds, LocalDate today) {
        int ended = 0;
        for (Policy p : policyRepository.findBySource(PolicySource.ONTONG)) {
            RecruitStatus status = p.currentStatus(today);
            boolean active = status == RecruitStatus.OPEN || status == RecruitStatus.UPCOMING;
            if (active && !acceptedExternalIds.contains(p.getExternalId())) {
                p.end("온통청년 수집 대상에서 빠짐");
                ended++;
            }
        }
        return ended;
    }

    public record Result(int inserted, int updated) {
    }
}
