package com.nextstep.nextsiheung.domain.policy.entity;

import com.nextstep.nextsiheung.global.common.BaseTimeEntity;
import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.PolicySource;
import com.nextstep.nextsiheung.global.common.enums.RecruitStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// ERD policy. 커뮤니티(후기 정책 태그)에서 먼저 필요해 컬럼만 매핑해 둠.
// 대상 상황(policy_life_stage)·태그(policy_tag)와 수정 메서드는 정책 담당이 이어서 추가한다
@Getter
@Entity
@Table(name = "policy")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Policy extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PolicySource source;

    // 온통청년 정책 ID. ADMIN 등록이면 null
    @Column(unique = true, length = 50)
    private String externalId;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InterestField field;

    @Column(nullable = false, length = 100)
    private String agency;

    @Column(nullable = false, length = 500)
    private String target;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 200)
    private String contact;

    private LocalDate applyStartDate;

    // 상시 모집이면 null
    private LocalDate applyEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RecruitStatus recruitStatus;

    @Column(nullable = false, length = 500)
    private String sourceUrl;

    @Column(nullable = false, length = 500)
    private String applyUrl;

    @Column(nullable = false)
    private LocalDate lastVerifiedAt;

    @Column(length = 200)
    private String endReason;

    // 등록한 관리자 ID. ONTONG이면 null
    private Long createdBy;

    @Builder
    private Policy(PolicySource source, String externalId, String title, InterestField field, String agency,
                   String target, String content, String contact, LocalDate applyStartDate,
                   LocalDate applyEndDate, RecruitStatus recruitStatus, String sourceUrl, String applyUrl,
                   LocalDate lastVerifiedAt, Long createdBy) {
        this.source = source;
        this.externalId = externalId;
        this.title = title;
        this.field = field;
        this.agency = agency;
        this.target = target;
        this.content = content;
        this.contact = contact;
        this.applyStartDate = applyStartDate;
        this.applyEndDate = applyEndDate;
        this.recruitStatus = recruitStatus;
        this.sourceUrl = sourceUrl;
        this.applyUrl = applyUrl;
        this.lastVerifiedAt = lastVerifiedAt;
        this.createdBy = createdBy;
    }
}
