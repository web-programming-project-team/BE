package com.nextstep.nextsiheung.domain.batch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// 로컬 테스트용: 서버가 뜬 직후 잡아바 수집을 1회 실행한다. app.batch.job-sync-on-startup=true일 때만 등록된다
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.batch.job-sync-on-startup", havingValue = "true")
public class JobSyncStartupRunner implements ApplicationRunner {

    private final JobSyncService jobSyncService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jobSyncService.sync();
        } catch (RuntimeException e) {
            log.error("[잡아바 수집] 시작 시 수집 실패", e);
        }
    }
}
