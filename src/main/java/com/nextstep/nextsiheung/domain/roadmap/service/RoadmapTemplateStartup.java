package com.nextstep.nextsiheung.domain.roadmap.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// 서버가 뜨면 템플릿 단계에 실제 정책을 다시 연결한다. 템플릿·샘플 seed와 시작 시 수집(ApplicationRunner)이 끝난 뒤에 돈다
@Slf4j
@Component
@RequiredArgsConstructor
public class RoadmapTemplateStartup {

    private final RoadmapTemplatePolicyLinker linker;

    @EventListener(ApplicationReadyEvent.class)
    public void relink() {
        try {
            linker.relinkAll();
        } catch (RuntimeException e) {
            log.error("[로드맵 템플릿] 시작 시 정책 연결 실패", e);
        }
    }
}
