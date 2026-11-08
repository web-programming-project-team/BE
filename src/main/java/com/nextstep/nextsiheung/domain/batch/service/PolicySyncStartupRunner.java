package com.nextstep.nextsiheung.domain.batch.service;

import com.nextstep.nextsiheung.domain.batch.entity.SyncLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// 로컬 테스트용: 서버가 뜬 직후 온통청년 수집을 1회 실행한다. app.batch.policy-sync-on-startup=true일 때만 등록된다
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.batch.policy-sync-on-startup", havingValue = "true")
public class PolicySyncStartupRunner implements ApplicationRunner {

    private final PolicySyncService policySyncService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            policySyncService.sync(SyncLog.TRIGGER_MANUAL);
        } catch (RuntimeException e) {
            log.error("[온통청년 수집] 시작 시 수집 실패", e);
        }
    }
}
