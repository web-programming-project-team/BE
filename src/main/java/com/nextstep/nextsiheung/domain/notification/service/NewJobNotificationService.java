package com.nextstep.nextsiheung.domain.notification.service;

import com.nextstep.nextsiheung.domain.job.service.JobService;
import com.nextstep.nextsiheung.domain.notification.entity.Notification;
import com.nextstep.nextsiheung.domain.notification.repository.NotificationRepository;
import com.nextstep.nextsiheung.domain.user.entity.JobPreference;
import com.nextstep.nextsiheung.domain.user.entity.User;
import com.nextstep.nextsiheung.domain.user.repository.JobPreferenceRepository;
import com.nextstep.nextsiheung.domain.user.repository.UserVisitRepository;
import com.nextstep.nextsiheung.global.common.Kst;
import com.nextstep.nextsiheung.global.common.enums.NotificationType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 새 맞춤 공고 알림(NEW_JOB). 잡아바 수집 직후 회원마다 저장 조건과 비교해, 새로 맞는 공고가 있으면 1건 보낸다
// 새 공고 기준: 마지막 방문일 다음 날 0시와 마지막 NEW_JOB 알림 시각 중 늦은 쪽 이후에 수집된 공고
//  → 며칠 접속하지 않아도 같은 공고를 매일 다시 세지 않는다
@Slf4j
@Service
@RequiredArgsConstructor
public class NewJobNotificationService {

    private final JobPreferenceRepository jobPreferenceRepository;
    private final UserVisitRepository userVisitRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final JobService jobService;

    @Transactional
    public int sendAfterSync() {
        LocalDate today = Kst.today();
        int sent = 0;
        for (JobPreference preference : jobPreferenceRepository.findAll()) {
            User user = preference.getUser();
            if (!user.isOnboardingCompleted()) {
                continue;
            }
            long count = jobService.countMatchesSince(preference, today, since(user, today));
            if (count > 0) {
                notificationService.create(user.getId(), NotificationType.NEW_JOB,
                        "조건에 맞는 새 공고 " + count + "건", "희망 조건에 맞는 신입 공고가 새로 올라왔어요.",
                        Notification.TARGET_JOB_LIST, null);
                sent++;
            }
        }
        log.info("[새 맞춤 공고 알림] {}명에게 보냄", sent);
        return sent;
    }

    private LocalDateTime since(User user, LocalDate today) {
        LocalDate lastVisit = userVisitRepository.findLastVisitBefore(user.getId(), today.plusDays(1));
        LocalDateTime since = lastVisit == null ? user.getCreatedAt() : lastVisit.plusDays(1).atStartOfDay();
        LocalDateTime lastSent = notificationRepository
                .findFirstByUserIdAndTypeOrderByCreatedAtDesc(user.getId(), NotificationType.NEW_JOB)
                .map(Notification::getCreatedAt).orElse(null);
        return lastSent != null && lastSent.isAfter(since) ? lastSent : since;
    }
}
