package com.nextstep.nextsiheung.domain.post.service;

import com.nextstep.nextsiheung.domain.post.dto.ReportRequest;
import com.nextstep.nextsiheung.domain.post.dto.ReportResponse;
import com.nextstep.nextsiheung.domain.post.entity.Post;
import com.nextstep.nextsiheung.domain.post.entity.PostReport;
import com.nextstep.nextsiheung.domain.post.entity.ReportStatus;
import com.nextstep.nextsiheung.domain.post.repository.PostReportRepository;
import com.nextstep.nextsiheung.domain.post.repository.PostRepository;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 게시글 신고. 처리 대기(PENDING) 신고가 5건 쌓이면 자동 숨김
@Service
@RequiredArgsConstructor
@Transactional
public class PostReportService {

    static final int AUTO_HIDE_THRESHOLD = 5;
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final PostRepository postRepository;
    private final PostReportRepository postReportRepository;

    public ReportResponse report(Long userId, Long postId, ReportRequest request) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .filter(p -> !p.isHidden())
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        if (postReportRepository.existsByPostIdAndReporterId(postId, userId)) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS);
        }

        String detail = request.detail() == null || request.detail().isBlank() ? null : request.detail().trim();
        PostReport report;
        try {
            report = postReportRepository.saveAndFlush(
                    new PostReport(postId, userId, request.reason(), detail, LocalDateTime.now(SEOUL)));
        } catch (DataIntegrityViolationException e) {
            // 동시에 두 번 신고해 UNIQUE가 겹친 경우
            throw new BusinessException(ErrorCode.ALREADY_EXISTS);
        }

        boolean autoHidden = false;
        if (postReportRepository.countByPostIdAndStatus(postId, ReportStatus.PENDING) >= AUTO_HIDE_THRESHOLD) {
            post.hide(Post.AUTO_HIDDEN_REASON);
            autoHidden = true;
        }
        return new ReportResponse(report.getId(), autoHidden);
    }
}
