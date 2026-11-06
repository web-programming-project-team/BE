package com.nextstep.nextsiheung.domain.policy.repository;

import com.nextstep.nextsiheung.domain.policy.entity.Policy;
import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.PolicySource;
import com.nextstep.nextsiheung.global.common.enums.RecruitStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.Collection;
import org.springframework.data.jpa.domain.Specification;

// 정책 목록 검색 조건. 모집상태는 저장된 값이 아니라 접수기간과 오늘 날짜로 거른다(Policy.currentStatus와 같은 규칙)
public final class PolicySpecs {

    private PolicySpecs() {
    }

    public static Specification<Policy> all() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<Policy> field(InterestField field) {
        return field == null ? null : (root, query, cb) -> cb.equal(root.get("field"), field);
    }

    public static Specification<Policy> source(PolicySource source) {
        return source == null ? null : (root, query, cb) -> cb.equal(root.get("source"), source);
    }

    // 정책명·기관명 부분 일치
    public static Specification<Policy> keyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        String like = "%" + keyword.trim() + "%";
        return (root, query, cb) -> cb.or(cb.like(root.get("title"), like), cb.like(root.get("agency"), like));
    }

    public static Specification<Policy> statusIn(Collection<RecruitStatus> statuses, LocalDate today) {
        return (root, query, cb) -> cb.or(statuses.stream()
                .map(s -> status(root, cb, s, today))
                .toArray(Predicate[]::new));
    }

    // 마지막 확인일이 30일 넘었는지
    public static Specification<Policy> stale(Boolean stale, LocalDate today) {
        if (stale == null) {
            return null;
        }
        LocalDate limit = today.minusDays(Policy.STALE_DAYS);
        return (root, query, cb) -> stale
                ? cb.lessThan(root.get("lastVerifiedAt"), limit)
                : cb.greaterThanOrEqualTo(root.get("lastVerifiedAt"), limit);
    }

    private static Predicate status(Root<Policy> root, CriteriaBuilder cb, RecruitStatus status, LocalDate today) {
        Predicate ended = cb.equal(root.get("recruitStatus"), RecruitStatus.ENDED);
        Predicate notEnded = cb.notEqual(root.get("recruitStatus"), RecruitStatus.ENDED);
        Predicate notStarted = cb.and(cb.isNotNull(root.get("applyStartDate")),
                cb.greaterThan(root.get("applyStartDate"), today));
        Predicate passed = cb.and(cb.isNotNull(root.get("applyEndDate")),
                cb.lessThan(root.get("applyEndDate"), today));
        return switch (status) {
            case ENDED -> ended;
            case UPCOMING -> cb.and(notEnded, notStarted);
            case CLOSED -> cb.and(notEnded, cb.not(notStarted), passed);
            case OPEN -> cb.and(notEnded, cb.not(notStarted), cb.not(passed));
        };
    }
}
