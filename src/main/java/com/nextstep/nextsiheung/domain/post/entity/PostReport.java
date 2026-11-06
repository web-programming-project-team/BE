package com.nextstep.nextsiheung.domain.post.entity;

import com.nextstep.nextsiheung.global.common.enums.ReportReason;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// ERD post_report. 한 사람이 같은 글을 한 번만 신고 (post_id + reporter_id UNIQUE). 탈퇴하면 reporter_id는 null로 남는다
@Getter
@Entity
@Table(name = "post_report", uniqueConstraints = @UniqueConstraint(
        name = "UQ_POST_REPORT_POST_REPORTER", columnNames = {"post_id", "reporter_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "reporter_id")
    private Long reporterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportReason reason;

    @Column(length = 200)
    private String detail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    public PostReport(Long postId, Long reporterId, ReportReason reason, String detail, LocalDateTime now) {
        this.postId = postId;
        this.reporterId = reporterId;
        this.reason = reason;
        this.detail = detail;
        this.status = ReportStatus.PENDING;
        this.createdAt = now;
    }
}
