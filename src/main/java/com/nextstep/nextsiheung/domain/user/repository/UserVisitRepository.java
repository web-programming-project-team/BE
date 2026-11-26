package com.nextstep.nextsiheung.domain.user.repository;

import com.nextstep.nextsiheung.domain.user.entity.UserVisit;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserVisitRepository extends JpaRepository<UserVisit, UserVisit.Id> {

    // 하루 1행. 이미 있으면(동시 요청 포함) 무시한다
    @Modifying
    @Query(value = "INSERT IGNORE INTO user_visit (user_id, visit_date) VALUES (:userId, :visitDate)", nativeQuery = true)
    int insertIgnore(@Param("userId") Long userId, @Param("visitDate") LocalDate visitDate);

    // 직전 방문일: 기준일 이전 가장 최근 visit_date (없으면 null). isNew·새 맞춤 공고 판단에 쓴다
    @Query("select max(v.id.visitDate) from UserVisit v where v.id.userId = :userId and v.id.visitDate < :before")
    LocalDate findLastVisitBefore(@Param("userId") Long userId, @Param("before") LocalDate before);
}
