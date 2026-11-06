package com.nextstep.nextsiheung.domain.policy.entity;

import com.nextstep.nextsiheung.global.common.BaseTimeEntity;
import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.LifeStage;
import com.nextstep.nextsiheung.global.common.enums.PolicySource;
import com.nextstep.nextsiheung.global.common.enums.RecruitStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// ERD policy. 온통청년 수집 정책(ONTONG)과 관리자 등록 시흥 지역 프로그램(ADMIN)을 같이 둔다
// 신청은 서비스 안에서 하지 않으므로 sourceUrl·applyUrl·lastVerifiedAt이 항상 있다
@Getter
@Entity
@Table(name = "policy")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Policy extends BaseTimeEntity {

    // 마지막 확인일이 이 기간을 넘으면 '확인 필요'(stale)
    public static final int STALE_DAYS = 30;

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

    // 저장할 때 접수기간으로 계산해 둔다. 읽을 때는 currentStatus(today)로 다시 계산한다
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

    // 등록한 관리자 ID (ONTONG이면 null). 관리자가 탈퇴해도 정책은 남는다(FK ON DELETE SET NULL)
    private Long createdBy;

    @ElementCollection
    @CollectionTable(name = "policy_life_stage", joinColumns = @JoinColumn(name = "policy_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "life_stage", nullable = false, length = 30)
    private Set<LifeStage> lifeStages = new LinkedHashSet<>();

    // 채용공고의 '도움 되는 지원' 매칭용
    @ElementCollection
    @CollectionTable(name = "policy_tag", joinColumns = @JoinColumn(name = "policy_id"))
    @Column(name = "tag", nullable = false, length = 30)
    private Set<String> tags = new LinkedHashSet<>();

    @Builder
    private Policy(PolicySource source, String externalId, String title, InterestField field, String agency,
                   String target, String content, String contact, LocalDate applyStartDate, LocalDate applyEndDate,
                   String sourceUrl, String applyUrl, Long createdBy, Collection<LifeStage> lifeStages,
                   Collection<String> tags, LocalDate today) {
        this.source = source;
        this.externalId = externalId;
        this.createdBy = createdBy;
        update(title, field, agency, target, content, contact, applyStartDate, applyEndDate, sourceUrl, applyUrl,
                lifeStages, tags, today);
    }

    // 관리자 수정(PUT, 본문 전체). 저장하면 마지막 확인일이 오늘로 바뀐다
    public void update(String title, InterestField field, String agency, String target, String content,
                       String contact, LocalDate applyStartDate, LocalDate applyEndDate, String sourceUrl,
                       String applyUrl, Collection<LifeStage> lifeStages, Collection<String> tags, LocalDate today) {
        this.title = title;
        this.field = field;
        this.agency = agency;
        this.target = target == null ? "" : target;
        this.content = content == null ? "" : content;
        this.contact = contact;
        this.applyStartDate = applyStartDate;
        this.applyEndDate = applyEndDate;
        this.sourceUrl = sourceUrl;
        // 신청 URL을 따로 안 주면 공식 출처로 보낸다
        this.applyUrl = applyUrl == null || applyUrl.isBlank() ? sourceUrl : applyUrl;
        // 대상 상황·태그는 보낸 경우에만 바꾼다(null이면 기존 값 유지). 관리자 화면 폼에 이 칸이 없어서 수정 때 지워지지 않게 한다
        if (lifeStages != null) {
            this.lifeStages.clear();
            this.lifeStages.addAll(lifeStages);
        }
        if (tags != null) {
            this.tags.clear();
            this.tags.addAll(tags);
        }
        this.recruitStatus = currentStatus(today);
        this.lastVerifiedAt = today;
    }

    public void end(String reason) {
        this.recruitStatus = RecruitStatus.ENDED;
        this.endReason = reason;
    }

    public void verify(LocalDate today) {
        this.recruitStatus = currentStatus(today);
        this.lastVerifiedAt = today;
    }

    // ENDED는 관리자만 정하고, 나머지는 접수기간과 오늘 날짜로 계산한다
    public RecruitStatus currentStatus(LocalDate today) {
        if (recruitStatus == RecruitStatus.ENDED) {
            return RecruitStatus.ENDED;
        }
        if (applyStartDate != null && today.isBefore(applyStartDate)) {
            return RecruitStatus.UPCOMING;
        }
        if (applyEndDate != null && today.isAfter(applyEndDate)) {
            return RecruitStatus.CLOSED;
        }
        return RecruitStatus.OPEN;
    }

    public boolean isEnded() {
        return recruitStatus == RecruitStatus.ENDED;
    }

    public boolean isStale(LocalDate today) {
        return lastVerifiedAt.isBefore(today.minusDays(STALE_DAYS));
    }
}
