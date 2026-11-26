package com.nextstep.nextsiheung.domain.batch.dto;

// 잡아바 수집 1회 결과 (ERD sync_log의 fetched/inserted/updated와 같은 의미)
public record JobSyncResult(
        int pages,
        int fetched,
        int open,
        int matched,
        int inserted,
        int updated,
        long elapsedMillis
) {
}
