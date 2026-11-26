package com.nextstep.nextsiheung.domain.batch.service;

import com.nextstep.nextsiheung.domain.batch.entity.SyncLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// 매일 policy-sync-cron(03:00)에 온통청년 수집. app.batch.enabled=true일 때만 등록된다
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.batch.enabled", havingValue = "true")
public class PolicySyncScheduler {

    private final PolicySyncService policySyncService;

    @Scheduled(cron = "${app.batch.policy-sync-cron}", zone = "Asia/Seoul")
    public void run() {
        try {
            policySyncService.sync(SyncLog.TRIGGER_SCHEDULED);
        } catch (RuntimeException e) {
            log.error("[온통청년 수집] 실패", e);
        }
    }
}
