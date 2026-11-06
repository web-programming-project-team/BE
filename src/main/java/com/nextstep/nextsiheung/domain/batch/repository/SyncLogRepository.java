package com.nextstep.nextsiheung.domain.batch.repository;

import com.nextstep.nextsiheung.domain.batch.entity.SyncLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncLogRepository extends JpaRepository<SyncLog, Long> {

    Page<SyncLog> findAllByOrderByStartedAtDescIdDesc(Pageable pageable);

    Page<SyncLog> findAllBySourceOrderByStartedAtDescIdDesc(String source, Pageable pageable);
}
