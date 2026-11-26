package com.nextstep.nextsiheung.domain.notification.service;

import com.nextstep.nextsiheung.domain.calendar.entity.CalendarEvent;
import com.nextstep.nextsiheung.domain.calendar.repository.CalendarEventRepository;
import com.nextstep.nextsiheung.domain.notification.entity.Notification;
import com.nextstep.nextsiheung.domain.notification.repository.NotificationRepository;
import com.nextstep.nextsiheung.global.common.Kst;
import com.nextstep.nextsiheung.global.common.enums.EventType;
import com.nextstep.nextsiheung.global.common.enums.NotificationType;
import com.nextstep.nextsiheung.global.common.enums.ProgressStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 마감 임박 알림(DEADLINE). 매일 09:00에 캘린더 일정 중 D-7, D-3, D-day인 것을 알린다
// 캘린더에는 로드맵 항목(접수 마감)·관심 공고(채용 마감)·직접 추가한 일정이 모두 들어 있다
//  - 접수 시작(APPLY_START)은 마감이 아니라 뺀다
//  - 로드맵 항목을 이미 신청(APPLIED 이후)했으면 접수 마감 알림을 보내지 않는다
//  - 같은 날 다시 돌려도 같은 알림은 한 번만 간다
@Slf4j
@Service
@RequiredArgsConstructor
public class DeadlineNotificationService {

    static final List<Integer> NOTIFY_DAYS = List.of(7, 3, 0);
    private static final Set<EventType> TYPES = EnumSet.complementOf(EnumSet.of(EventType.APPLY_START));
    private static final Set<ProgressStatus> ALREADY_APPLIED =
            EnumSet.of(ProgressStatus.APPLIED, ProgressStatus.IN_PROGRESS, ProgressStatus.COMPLETED);

    private final CalendarEventRepository calendarEventRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    // 보낸 알림 수를 돌려준다
    @Transactional
    public int send() {
        LocalDate today = Kst.today();
        LocalDateTime startOfToday = today.atStartOfDay();
        List<LocalDate> dates = NOTIFY_DAYS.stream().map(today::plusDays).toList();

        int sent = 0;
        for (CalendarEvent event : calendarEventRepository.findAllOnDates(dates, TYPES)) {
            if (event.getItem() != null && ALREADY_APPLIED.contains(event.getItem().getStatus())) {
                continue;
            }
            Long userId = event.getUser().getId();
            int dDay = (int) ChronoUnit.DAYS.between(today, event.getDate());
            String title = event.getTitle() + (dDay == 0 ? " D-day" : " D-" + dDay);
            if (notificationRepository.existsByUserIdAndTypeAndTitleAndCreatedAtGreaterThanEqual(
                    userId, NotificationType.DEADLINE, title, startOfToday)) {
                continue;
            }
            String body = dDay == 0 ? "오늘이 마감일이에요." : "마감까지 " + dDay + "일 남았어요.";
            notificationService.create(userId, NotificationType.DEADLINE, title, body, targetType(event),
                    targetId(event));
            sent++;
        }
        log.info("[마감 임박 알림] {}건 보냄", sent);
        return sent;
    }

    // 정책 일정 → 정책 상세, 관심 공고 → 공고 상세, 직접 추가 → 캘린더
    private static String targetType(CalendarEvent event) {
        return switch (event.getSourceType()) {
            case CalendarEvent.SOURCE_POLICY -> Notification.TARGET_POLICY;
            case CalendarEvent.SOURCE_JOB -> Notification.TARGET_JOB;
            default -> Notification.TARGET_CALENDAR;
        };
    }

    private static Long targetId(CalendarEvent event) {
        return CalendarEvent.SOURCE_CUSTOM.equals(event.getSourceType()) ? null : event.sourceId();
    }
}
