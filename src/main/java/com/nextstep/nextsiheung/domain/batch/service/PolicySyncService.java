package com.nextstep.nextsiheung.domain.batch.service;

import com.nextstep.nextsiheung.domain.batch.dto.OntongPolicyRow;
import com.nextstep.nextsiheung.domain.batch.dto.PolicySyncResult;
import com.nextstep.nextsiheung.domain.batch.entity.SyncLog;
import com.nextstep.nextsiheung.domain.batch.repository.SyncLogRepository;
import com.nextstep.nextsiheung.domain.batch.service.OntongMapper.PolicyFields;
import com.nextstep.nextsiheung.domain.job.service.JobPolicyMatchService;
import com.nextstep.nextsiheung.domain.policy.service.PolicyUpsertService;
import com.nextstep.nextsiheung.domain.roadmap.service.RoadmapTemplatePolicyLinker;
import com.nextstep.nextsiheung.global.common.Kst;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// 온통청년 청년정책 수집. 시흥시(zipCd 41390) 청년이 신청할 수 있는 전국·경기도·시흥시 정책을 페이지 단위로 받아
// policy(source=ONTONG)에 넣고, 결과를 sync_log에 남긴 뒤 공고-정책 매칭과 로드맵 템플릿 정책을 다시 연결한다
@Slf4j
@Service
public class PolicySyncService {

    private final OntongClient ontongClient;
    private final OntongMapper ontongMapper;
    private final PolicyUpsertService upsertService;
    private final SyncLogRepository syncLogRepository;
    private final JobPolicyMatchService jobPolicyMatchService;
    private final RoadmapTemplatePolicyLinker templatePolicyLinker;
    private final String zipCd;
    private final int pageSize;
    private final int maxPages;

    public PolicySyncService(OntongClient ontongClient, OntongMapper ontongMapper, PolicyUpsertService upsertService,
                             SyncLogRepository syncLogRepository, JobPolicyMatchService jobPolicyMatchService,
                             RoadmapTemplatePolicyLinker templatePolicyLinker,
                             @Value("${app.batch.policy-sync-zip-code:41390}") String zipCd,
                             @Value("${app.batch.policy-sync-page-size:100}") int pageSize,
                             @Value("${app.batch.policy-sync-max-pages:30}") int maxPages) {
        this.ontongClient = ontongClient;
        this.ontongMapper = ontongMapper;
        this.upsertService = upsertService;
        this.syncLogRepository = syncLogRepository;
        this.jobPolicyMatchService = jobPolicyMatchService;
        this.templatePolicyLinker = templatePolicyLinker;
        this.zipCd = zipCd;
        this.pageSize = pageSize;
        this.maxPages = maxPages;
    }

    // triggerType: SyncLog.TRIGGER_SCHEDULED · TRIGGER_MANUAL. 실패하면 sync_log를 FAILED로 남기고 예외를 다시 던진다
    public PolicySyncResult sync(String triggerType) {
        long startedAt = System.currentTimeMillis();
        LocalDate today = Kst.today();
        SyncLog syncLog = syncLogRepository.save(SyncLog.start(SyncLog.SOURCE_ONTONG, triggerType, Kst.now()));
        int pages = 0;
        int fetched = 0;
        int skipped = 0;
        int inserted = 0;
        int updated = 0;
        int ended;
        Set<String> accepted = new HashSet<>();
        try {
            int totalCount = Integer.MAX_VALUE;
            for (int page = 1; page <= maxPages && fetched < totalCount; page++) {
                OntongClient.Page result = ontongClient.fetch(page, pageSize, zipCd);
                totalCount = result.totalCount();
                if (result.rows().isEmpty()) {
                    break;
                }
                pages++;
                fetched += result.rows().size();
                List<PolicyFields> collected = new ArrayList<>();
                for (OntongPolicyRow row : result.rows()) {
                    ontongMapper.toFields(row, today).ifPresent(collected::add);
                }
                skipped += result.rows().size() - collected.size();
                collected.forEach(p -> accepted.add(p.externalId()));
                PolicyUpsertService.Result saved = upsertService.upsertAll(collected, today);
                inserted += saved.inserted();
                updated += saved.updated();
                log.info("[온통청년 수집] {}페이지: {}건 중 신청 가능 {}건 (추가 {}, 갱신 {})",
                        page, result.rows().size(), collected.size(), saved.inserted(), saved.updated());
            }
            // 전체 페이지를 다 받았을 때만: 이번에 오지 않은 접수 중 정책은 종료 처리
            // (max-pages에서 끊겼거나 빈 응답이면 정책을 잘못 종료하지 않도록 건너뛴다)
            ended = fetched > 0 && fetched >= totalCount ? upsertService.endMissing(accepted, today) : 0;
        } catch (RuntimeException e) {
            syncLog.fail(e.getMessage(), Kst.now());
            syncLogRepository.save(syncLog);
            throw e;
        }
        // failed는 저장에 실패한 건수(지금은 페이지 단위로 실패하면 FAILED). 마감·필수값 없음은 skipped로 로그에만 남긴다
        syncLog.succeed(fetched, inserted, updated, 0, Kst.now());
        syncLogRepository.save(syncLog);
        PolicySyncResult result = new PolicySyncResult(pages, fetched, skipped, inserted, updated,
                System.currentTimeMillis() - startedAt);
        log.info("[온통청년 수집] 끝: {} (수집 대상에서 빠져 종료 {}건)", result, ended);
        rebuildMatches();
        return result;
    }

    // 새 정책이 채용공고의 '도움 되는 지원'과 로드맵 템플릿 단계에 붙도록. 실패해도 수집 결과는 그대로 둔다
    private void rebuildMatches() {
        try {
            jobPolicyMatchService.rebuildAll();
        } catch (RuntimeException e) {
            log.error("[공고-정책 매칭] 정책 수집 후 계산 실패", e);
        }
        try {
            templatePolicyLinker.relinkAll();
        } catch (RuntimeException e) {
            log.error("[로드맵 템플릿] 정책 수집 후 연결 실패", e);
        }
    }
}
