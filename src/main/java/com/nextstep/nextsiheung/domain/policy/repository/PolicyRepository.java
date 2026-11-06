package com.nextstep.nextsiheung.domain.policy.repository;

import com.nextstep.nextsiheung.domain.policy.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyRepository extends JpaRepository<Policy, Long> {
}
