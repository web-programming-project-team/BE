package com.nextstep.nextsiheung.domain.batch.service;

import com.nextstep.nextsiheung.domain.notification.service.DeadlineNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// 매일 deadline-notify-cron(09:00)에 마감 임박 알림. app.batch.enabled=true일 때만 등록된다
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.batch.enabled", havingValue = "true")
public class DeadlineNotifyScheduler {

    private final DeadlineNotificationService deadlineNotificationService;

    @Scheduled(cron = "${app.batch.deadline-notify-cron}", zone = "Asia/Seoul")
    public void run() {
        try {
            deadlineNotificationService.send();
        } catch (RuntimeException e) {
            log.error("[마감 임박 알림] 실패", e);
        }
    }
}
