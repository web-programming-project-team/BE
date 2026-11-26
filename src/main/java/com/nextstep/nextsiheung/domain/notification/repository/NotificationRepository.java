package com.nextstep.nextsiheung.domain.notification.repository;

import com.nextstep.nextsiheung.domain.notification.entity.Notification;
import com.nextstep.nextsiheung.global.common.enums.NotificationType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDescIdDesc(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDescIdDesc(Long userId, NotificationType type,
                                                                     Pageable pageable);

    long countByUserIdAndReadFalse(Long userId);

    // NEW_JOB 알림을 마지막으로 보낸 시점 (같은 공고를 두 번 세지 않도록)
    Optional<Notification> findFirstByUserIdAndTypeOrderByCreatedAtDesc(Long userId, NotificationType type);

    // 같은 날 배치가 두 번 돌아도 같은 알림을 다시 보내지 않도록
    boolean existsByUserIdAndTypeAndTitleAndCreatedAtGreaterThanEqual(Long userId, NotificationType type,
                                                                       String title, LocalDateTime since);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Notification n set n.read = true, n.readAt = :now where n.user.id = :userId and n.read = false")
    int markAllRead(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
