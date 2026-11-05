package com.nextstep.nextsiheung.domain.job.repository;

import com.nextstep.nextsiheung.domain.job.entity.JobPosting;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    List<JobPosting> findByExternalIdIn(Collection<String> externalIds);
}
