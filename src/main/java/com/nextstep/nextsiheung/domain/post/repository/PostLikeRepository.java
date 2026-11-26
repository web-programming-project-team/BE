package com.nextstep.nextsiheung.domain.post.repository;

import com.nextstep.nextsiheung.domain.post.entity.PostLike;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLike.Id> {

    long countByIdPostId(Long postId);

    // [postId, count]
    @Query("select l.id.postId, count(l) from PostLike l where l.id.postId in :postIds group by l.id.postId")
    List<Object[]> countByPostIds(@Param("postIds") Collection<Long> postIds);

    @Query("select l.id.postId from PostLike l where l.id.userId = :userId and l.id.postId in :postIds")
    List<Long> findLikedPostIds(@Param("userId") Long userId, @Param("postIds") Collection<Long> postIds);
}
