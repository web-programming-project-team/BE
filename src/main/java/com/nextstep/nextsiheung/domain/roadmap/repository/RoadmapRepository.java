package com.nextstep.nextsiheung.domain.roadmap.repository;

import com.nextstep.nextsiheung.domain.roadmap.entity.Roadmap;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {

    // 현재 로드맵 (회원당 1개)
    Optional<Roadmap> findFirstByUser_IdAndArchivedFalseOrderByIdDesc(Long userId);

    // 활동 기록: 상황 변경으로 보관된 로드맵, 최신순
    List<Roadmap> findAllByUser_IdAndArchivedTrueOrderByArchivedAtDesc(Long userId);
}
