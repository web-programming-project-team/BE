package com.nextstep.nextsiheung.domain.post.repository;

import java.time.LocalDateTime;

// 관리자 신고 목록의 게시글별 집계 한 줄
public record ReportGroupRow(Long postId, String oneLiner, long reportCount, LocalDateTime lastReportedAt,
                             boolean hidden, String hiddenReason) {
}
