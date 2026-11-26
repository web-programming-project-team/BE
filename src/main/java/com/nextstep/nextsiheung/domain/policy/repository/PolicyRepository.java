package com.nextstep.nextsiheung.domain.policy.repository;

import com.nextstep.nextsiheung.domain.policy.entity.Policy;
import com.nextstep.nextsiheung.global.common.enums.PolicySource;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PolicyRepository extends JpaRepository<Policy, Long>, JpaSpecificationExecutor<Policy> {

    // 온통청년 수집: 이미 넣은 정책(plcyNo)을 찾아 갱신한다
    List<Policy> findByExternalIdIn(Collection<String> externalIds);

    List<Policy> findBySource(PolicySource source);
}
