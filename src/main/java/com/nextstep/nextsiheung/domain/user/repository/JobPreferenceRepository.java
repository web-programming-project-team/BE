package com.nextstep.nextsiheung.domain.user.repository;

import com.nextstep.nextsiheung.domain.user.entity.JobPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPreferenceRepository extends JpaRepository<JobPreference, Long> {
}
