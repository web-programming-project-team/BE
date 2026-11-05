package com.nextstep.nextsiheung.domain.auth.repository;

import com.nextstep.nextsiheung.domain.auth.entity.RefreshToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("delete from RefreshToken t where t.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("delete from RefreshToken t where t.user.id = :userId and t.expiresAt <= :now")
    int deleteExpiredByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
