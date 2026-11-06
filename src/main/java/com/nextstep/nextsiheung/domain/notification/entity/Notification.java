package com.nextstep.nextsiheung.domain.notification.entity;

import com.nextstep.nextsiheung.domain.user.entity.User;
import com.nextstep.nextsiheung.global.common.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

// ERD notification. 서비스 안 알림 센터. 회원 탈퇴 시 DB가 함께 지운다(ON DELETE CASCADE)
@Getter
@Entity
@Table(name = "notification", indexes = @Index(name = "IX_NOTIFICATION_USER_READ", columnList = "user_id, is_read"))
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    // 이동 대상(target_type). 목록 화면(JOB_LIST)은 target_id가 null
    public static final String TARGET_POLICY = "POLICY";
    public static final String TARGET_JOB = "JOB";
    public static final String TARGET_JOB_LIST = "JOB_LIST";
    public static final String TARGET_POST = "POST";
    public static final String TARGET_CALENDAR = "CALENDAR";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 300)
    private String body;

    @Column(length = 30)
    private String targetType;

    private Long targetId;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    private LocalDateTime readAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Notification(User user, NotificationType type, String title, String body, String targetType,
                        Long targetId) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.body = body;
        this.targetType = targetType;
        this.targetId = targetId;
    }

    public void markRead(LocalDateTime now) {
        if (!read) {
            this.read = true;
            this.readAt = now;
        }
    }
}
