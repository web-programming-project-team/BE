package com.nextstep.nextsiheung.domain.post.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

// ERD post_like. 한 사람이 한 글에 한 번 (PK post_id + user_id)
@Getter
@Entity
@Table(name = "post_like")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike {

    @EmbeddedId
    private Id id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PostLike(Long postId, Long userId, LocalDateTime now) {
        this.id = new Id(postId, userId);
        this.createdAt = now;
    }

    @Getter
    @Embeddable
    @EqualsAndHashCode
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Id implements Serializable {

        @Column(name = "post_id")
        private Long postId;

        @Column(name = "user_id")
        private Long userId;

        public Id(Long postId, Long userId) {
            this.postId = postId;
            this.userId = userId;
        }
    }
}
