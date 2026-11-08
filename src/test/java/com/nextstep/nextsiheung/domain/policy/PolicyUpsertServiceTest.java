package com.nextstep.nextsiheung.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.nextstep.nextsiheung.domain.batch.service.OntongMapper.PolicyFields;
import com.nextstep.nextsiheung.domain.policy.entity.Policy;
import com.nextstep.nextsiheung.domain.policy.repository.PolicyRepository;
import com.nextstep.nextsiheung.domain.policy.service.PolicyUpsertService;
import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.PolicySource;
import com.nextstep.nextsiheung.global.common.enums.RecruitStatus;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

// 온통청년 수집 저장: 같은 plcyNo는 갱신, 관리자 종료(ENDED)는 유지 (로컬 MySQL 필요, 테스트마다 롤백)
@SpringBootTest
@Transactional
class PolicyUpsertServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 8);

    @Autowired
    PolicyUpsertService upsertService;
    @Autowired
    PolicyRepository policyRepository;
    @Autowired
    EntityManager em;

    @Test
    void 같은_정책번호는_새로_넣지_않고_갱신한다() {
        PolicyUpsertService.Result first = upsertService.upsertAll(List.of(fields("TEST-ONTONG-1", "청년 취업 지원")), TODAY);
        assertThat(first.inserted()).isEqualTo(1);

        PolicyUpsertService.Result second = upsertService.upsertAll(
                List.of(fields("TEST-ONTONG-1", "청년 취업 지원 (변경)"), fields("TEST-ONTONG-2", "청년 교육 지원")),
                TODAY.plusDays(1));
        assertThat(second.inserted()).isEqualTo(1);
        assertThat(second.updated()).isEqualTo(1);

        em.flush();
        em.clear();
        Policy saved = policyRepository.findByExternalIdIn(List.of("TEST-ONTONG-1")).getFirst();
        assertThat(saved.getSource()).isEqualTo(PolicySource.ONTONG);
        assertThat(saved.getTitle()).isEqualTo("청년 취업 지원 (변경)");
        assertThat(saved.getLastVerifiedAt()).isEqualTo(TODAY.plusDays(1));
        assertThat(saved.getTags()).containsExactlyInAnyOrder("인턴", "취업");
    }

    @Test
    void 관리자가_종료한_정책은_다시_수집해도_종료_상태다() {
        upsertService.upsertAll(List.of(fields("TEST-ONTONG-3", "종료될 정책")), TODAY);
        Policy policy = policyRepository.findByExternalIdIn(List.of("TEST-ONTONG-3")).getFirst();
        policy.end("사업 종료");
        em.flush();

        upsertService.upsertAll(List.of(fields("TEST-ONTONG-3", "종료될 정책")), TODAY.plusDays(1));
        em.flush();
        em.clear();
        assertThat(policyRepository.findByExternalIdIn(List.of("TEST-ONTONG-3")).getFirst().getRecruitStatus())
                .isEqualTo(RecruitStatus.ENDED);
    }

    private static PolicyFields fields(String externalId, String title) {
        return new PolicyFields(externalId, title, InterestField.EMPLOYMENT, "고용노동부", "만 19~34세", "내용",
                TODAY.minusDays(1), TODAY.plusDays(30), "https://www.work24.go.kr", "https://www.work24.go.kr",
                Set.of("인턴", "취업"));
    }
}
