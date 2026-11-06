package com.nextstep.nextsiheung.domain.admin.service;

import com.nextstep.nextsiheung.domain.admin.dto.PostVisibilityRequest;
import com.nextstep.nextsiheung.domain.admin.dto.PostVisibilityResponse;
import com.nextstep.nextsiheung.domain.admin.dto.ReportGroupResponse;
import com.nextstep.nextsiheung.domain.post.entity.Post;
import com.nextstep.nextsiheung.domain.post.entity.ReportStatus;
import com.nextstep.nextsiheung.domain.post.repository.PostReportRepository;
import com.nextstep.nextsiheung.domain.post.repository.PostRepository;
import com.nextstep.nextsiheung.domain.post.repository.ReportGroupRow;
import com.nextstep.nextsiheung.global.common.enums.ReportReason;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import com.nextstep.nextsiheung.global.response.PageResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 관리자 신고 검토: 게시글별 신고 목록, 숨김·복구
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final PostRepository postRepository;
    private final PostReportRepository postReportRepository;

    public PageResponse<ReportGroupResponse> reports(String status, int page, int size) {
        ReportStatus reportStatus = parseStatus(status);
        Page<ReportGroupRow> rows = postReportRepository.findGroups(reportStatus, Post.AUTO_HIDDEN_REASON,
                PageRequest.of(page, size));

        Map<Long, Map<ReportReason, Long>> reasons = new HashMap<>();
        List<Long> postIds = rows.getContent().stream().map(ReportGroupRow::postId).toList();
        if (!postIds.isEmpty()) {
            for (Object[] row : postReportRepository.countReasons(reportStatus, postIds)) {
                reasons.computeIfAbsent((Long) row[0], id -> new EnumMap<>(ReportReason.class))
                        .put((ReportReason) row[1], (Long) row[2]);
            }
        }

        List<ReportGroupResponse> content = rows.getContent().stream()
                .map(r -> new ReportGroupResponse(
                        r.postId(), r.oneLiner(), r.reportCount(),
                        reasons.getOrDefault(r.postId(), Map.of()),
                        r.hidden() && Post.AUTO_HIDDEN_REASON.equals(r.hiddenReason()),
                        reportStatus.name(), r.lastReportedAt()))
                .toList();
        return PageResponse.of(new PageImpl<>(content, rows.getPageable(), rows.getTotalElements()));
    }

    // 숨기거나 다시 보이게 하고, 그 글의 대기 중 신고를 모두 RESOLVED로
    @Transactional
    public PostVisibilityResponse changeVisibility(Long postId, PostVisibilityRequest request) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        if (request.hidden()) {
            String reason = request.reason() == null || request.reason().isBlank() ? null : request.reason().trim();
            post.hide(reason);
        } else {
            post.show();
        }
        postRepository.flush();
        postReportRepository.resolveAll(postId, LocalDateTime.now(SEOUL));
        return new PostVisibilityResponse(postId, post.isHidden());
    }

    private static ReportStatus parseStatus(String status) {
        try {
            return ReportStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
