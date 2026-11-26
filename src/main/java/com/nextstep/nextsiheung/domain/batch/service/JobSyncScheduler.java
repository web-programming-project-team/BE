package com.nextstep.nextsiheung.domain.batch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// 매일 job-sync-cron(04:00)에 잡아바 수집. app.batch.enabled=true일 때만 등록된다
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.batch.enabled", havingValue = "true")
public class JobSyncScheduler {

    private final JobSyncService jobSyncService;

    @Scheduled(cron = "${app.batch.job-sync-cron}", zone = "Asia/Seoul")
    public void run() {
        try {
            jobSyncService.sync();
        } catch (RuntimeException e) {
            log.error("[잡아바 수집] 실패", e);
        }
    }
}
