package com.nextstep.nextsiheung.domain.batch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// ERD sync_log. 외부 데이터 자동 수집 1회 결과 (관리자 GET /admin/sync/logs)
// 수집 서비스에서 start(...)로 RUNNING 행을 저장하고, 끝나면 succeed(...) 또는 fail(...)을 부른다
@Getter
@Entity
@Table(name = "sync_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SyncLog {

    public static final String SOURCE_ONTONG = "ONTONG";
    public static final String SOURCE_JOBABA = "JOBABA";
    public static final String TRIGGER_SCHEDULED = "SCHEDULED";
    public static final String TRIGGER_MANUAL = "MANUAL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sync_log_id")
    private Long id;

    // ONTONG · JOBABA
    @Column(nullable = false, length = 30)
    private String source;

    // SCHEDULED · MANUAL
    @Column(nullable = false, length = 30)
    private String triggerType;

    // RUNNING · SUCCESS · FAILED
    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false)
    private int fetched;

    @Column(nullable = false)
    private int inserted;

    @Column(nullable = false)
    private int updated;

    @Column(nullable = false)
    private int failed;

    @Column(length = 500)
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    public static SyncLog start(String source, String triggerType, LocalDateTime now) {
        SyncLog log = new SyncLog();
        log.source = source;
        log.triggerType = triggerType;
        log.status = "RUNNING";
        log.startedAt = now;
        return log;
    }

    public void succeed(int fetched, int inserted, int updated, int failed, LocalDateTime now) {
        this.status = "SUCCESS";
        this.fetched = fetched;
        this.inserted = inserted;
        this.updated = updated;
        this.failed = failed;
        this.finishedAt = now;
    }

    public void fail(String errorMessage, LocalDateTime now) {
        this.status = "FAILED";
        this.errorMessage = errorMessage == null ? null
                : errorMessage.substring(0, Math.min(errorMessage.length(), 500));
        this.finishedAt = now;
    }
}
