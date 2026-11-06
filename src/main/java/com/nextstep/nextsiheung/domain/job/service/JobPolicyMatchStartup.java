package com.nextstep.nextsiheung.domain.job.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// 서버가 뜨면 공고-정책 매칭을 한 번 계산한다. 샘플 데이터·시작 시 수집(ApplicationRunner)이 끝난 뒤에 돈다
// (수집이 꺼진 로컬에서도 정책 변경이 매칭에 반영되도록)
@Slf4j
@Component
@RequiredArgsConstructor
public class JobPolicyMatchStartup {

    private final JobPolicyMatchService jobPolicyMatchService;

    @EventListener(ApplicationReadyEvent.class)
    public void rebuild() {
        try {
            jobPolicyMatchService.rebuildAll();
        } catch (RuntimeException e) {
            log.error("[공고-정책 매칭] 시작 시 계산 실패", e);
        }
    }
}
