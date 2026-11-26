package com.nextstep.nextsiheung.domain.notification.service;

import com.nextstep.nextsiheung.domain.notification.dto.NotificationResponse;
import com.nextstep.nextsiheung.domain.notification.dto.ReadAllResponse;
import com.nextstep.nextsiheung.domain.notification.dto.ReadResponse;
import com.nextstep.nextsiheung.domain.notification.dto.UnreadCountResponse;
import com.nextstep.nextsiheung.domain.notification.entity.Notification;
import com.nextstep.nextsiheung.domain.notification.repository.NotificationRepository;
import com.nextstep.nextsiheung.domain.user.repository.UserRepository;
import com.nextstep.nextsiheung.global.common.Kst;
import com.nextstep.nextsiheung.global.common.enums.NotificationType;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import com.nextstep.nextsiheung.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 알림 센터: 목록·안 읽은 수·읽음 처리, 그리고 다른 도메인이 알림을 만들 때 쓰는 create
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int MAX_PAGE_SIZE = 50;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> list(Long userId, NotificationType type, int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        PageRequest pageable = PageRequest.of(page, size);
        Page<Notification> result = type == null
                ? notificationRepository.findByUserIdOrderByCreatedAtDescIdDesc(userId, pageable)
                : notificationRepository.findByUserIdAndTypeOrderByCreatedAtDescIdDesc(userId, type, pageable);
        return PageResponse.of(result.map(NotificationService::toResponse));
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse unreadCount(Long userId) {
        return new UnreadCountResponse(countUnread(userId));
    }

    // 홈 요약의 unreadNotificationCount에서도 쓴다
    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    // 이미 읽은 알림이면 그대로 둔다. 다른 사람 알림이면 FORBIDDEN
    @Transactional
    public ReadResponse read(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
        if (!notification.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        notification.markRead(Kst.now());
        return new ReadResponse(notification.getId(), true);
    }

    @Transactional
    public ReadAllResponse readAll(Long userId) {
        return new ReadAllResponse(notificationRepository.markAllRead(userId, Kst.now()));
    }

    // targetType은 Notification.TARGET_* (목록 화면이면 targetId=null)
    @Transactional
    public Notification create(Long userId, NotificationType type, String title, String body, String targetType,
                               Long targetId) {
        return notificationRepository.save(new Notification(userRepository.getReferenceById(userId), type, title,
                body, targetType, targetId));
    }

    private static NotificationResponse toResponse(Notification n) {
        NotificationResponse.Link link = n.getTargetType() == null ? null
                : new NotificationResponse.Link(n.getTargetType(), n.getTargetId());
        return new NotificationResponse(n.getId(), n.getType(), n.getTitle(), n.getBody(), link, n.isRead(),
                n.getCreatedAt());
    }
}
