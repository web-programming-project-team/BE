package com.nextstep.nextsiheung.domain.post.service;

import com.nextstep.nextsiheung.domain.post.dto.LikeResponse;
import com.nextstep.nextsiheung.domain.post.entity.PostLike;
import com.nextstep.nextsiheung.domain.post.repository.PostLikeRepository;
import com.nextstep.nextsiheung.domain.post.repository.PostRepository;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 게시글 좋아요 (한 사람이 한 글에 한 번)
@Service
@RequiredArgsConstructor
@Transactional
public class PostLikeService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    public LikeResponse like(Long userId, Long postId) {
        checkVisible(postId);
        PostLike.Id id = new PostLike.Id(postId, userId);
        if (postLikeRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS);
        }
        try {
            postLikeRepository.saveAndFlush(new PostLike(postId, userId, LocalDateTime.now(SEOUL)));
        } catch (DataIntegrityViolationException e) {
            // 동시에 두 번 눌러 PK가 겹친 경우
            throw new BusinessException(ErrorCode.ALREADY_EXISTS);
        }
        return new LikeResponse(postId, true, postLikeRepository.countByIdPostId(postId));
    }

    // 누르지 않은 글이어도 같은 응답
    public LikeResponse unlike(Long userId, Long postId) {
        checkVisible(postId);
        postLikeRepository.findById(new PostLike.Id(postId, userId)).ifPresent(postLikeRepository::delete);
        postLikeRepository.flush();
        return new LikeResponse(postId, false, postLikeRepository.countByIdPostId(postId));
    }

    private void checkVisible(Long postId) {
        postRepository.findByIdAndDeletedAtIsNull(postId)
                .filter(p -> !p.isHidden())
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }
}
