package com.nextstep.nextsiheung.domain.calendar.repository;

import com.nextstep.nextsiheung.domain.calendar.entity.CalendarEvent;
import com.nextstep.nextsiheung.global.common.enums.EventType;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    @Query("""
            select e from CalendarEvent e left join fetch e.item
            where e.user.id = :userId and e.date between :from and :to
            order by e.date asc, e.eventTime asc nulls first, e.id asc
            """)
    List<CalendarEvent> findInRange(@Param("userId") Long userId, @Param("from") LocalDate from,
                                    @Param("to") LocalDate to);

    @Query("""
            select e from CalendarEvent e left join fetch e.item
            where e.user.id = :userId and e.date between :from and :to and e.type in :types
            order by e.date asc, e.eventTime asc nulls first, e.id asc
            """)
    List<CalendarEvent> findInRange(@Param("userId") Long userId, @Param("from") LocalDate from,
                                    @Param("to") LocalDate to, @Param("types") Collection<EventType> types);

    // 메인 '다가오는 마감'
    @Query("""
            select e from CalendarEvent e
            where e.user.id = :userId and e.date between :from and :to and e.type in :types
            order by e.date asc, e.id asc
            """)
    List<CalendarEvent> findDeadlines(@Param("userId") Long userId, @Param("from") LocalDate from,
                                      @Param("to") LocalDate to, @Param("types") Collection<EventType> types,
                                      Pageable pageable);

    // 상황 변경으로 보관한 로드맵의 자동 일정은 캘린더에서 뺀다
    // 영속성 컨텍스트는 비우지 않는다(상황 변경 중인 회원·로드맵 엔티티를 계속 쓰므로)
    @Modifying(flushAutomatically = true)
    @Query("""
            delete from CalendarEvent e
            where e.item.id in (select i.id from RoadmapItem i where i.step.roadmap.id = :roadmapId)
            """)
    int deleteByRoadmapId(@Param("roadmapId") Long roadmapId);
}
