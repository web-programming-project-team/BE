package com.nextstep.nextsiheung.domain.batch.dto;

// 온통청년 수집 1회 결과 (로그용). skipped = 마감됐거나 필수값이 없어 넣지 않은 건수
public record PolicySyncResult(int pages, int fetched, int skipped, int inserted, int updated, long elapsedMillis) {
}
