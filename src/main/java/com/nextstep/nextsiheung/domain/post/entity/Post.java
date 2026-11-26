package com.nextstep.nextsiheung.domain.post.entity;

import com.nextstep.nextsiheung.global.common.BaseTimeEntity;
import com.nextstep.nextsiheung.global.common.enums.JobCategory;
import com.nextstep.nextsiheung.global.common.enums.LifeStage;
import com.nextstep.nextsiheung.global.common.enums.PostType;
import com.nextstep.nextsiheung.global.common.enums.PrepPeriod;
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
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

// ERD post. 합격 후기·참여 후기·포트폴리오. 작성자 닉네임은 내려주지 않고 상황(authorStage)만 보여준다
// user·policy·job은 다른 도메인이라 ID로만 들고 있다 (FK는 ERD에서 건다. 탈퇴 시 user_id는 SET NULL)
@Getter
@Entity
@Table(name = "post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    // 신고 5건 자동 숨김에 쓰는 사유. 관리자 숨김과 구분해 신고 목록의 autoHidden을 판단한다
    public static final String AUTO_HIDDEN_REASON = "신고 누적 자동 숨김";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    // 탈퇴하면 null
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PostType type;

    private Long policyId;

    private Long jobId;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private JobCategory jobCategory;

    @Column(columnDefinition = "TINYINT")
    private Integer rating;

    @Column(nullable = false, length = 60)
    private String oneLiner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PrepPeriod prepPeriod;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String portfolioUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LifeStage authorStage;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private LifeStage authorStageAfter;

    @Column(nullable = false)
    private boolean hidden;

    @Column(length = 200)
    private String hiddenReason;

    private LocalDateTime deletedAt;

    @BatchSize(size = 50)
    @ElementCollection
    @CollectionTable(name = "post_helpful_point", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "point", length = 30, nullable = false)
    private Set<String> helpfulPoints = new LinkedHashSet<>();

    @Builder
    private Post(Long userId, PostType type, Long policyId, Long jobId, JobCategory jobCategory, Integer rating,
                 String oneLiner, PrepPeriod prepPeriod, String content, String portfolioUrl,
                 LifeStage authorStage, Collection<String> helpfulPoints) {
        this.userId = userId;
        this.type = type;
        this.policyId = policyId;
        this.jobId = jobId;
        this.jobCategory = jobCategory;
        this.rating = rating;
        this.oneLiner = oneLiner;
        this.prepPeriod = prepPeriod;
        this.content = content;
        this.portfolioUrl = portfolioUrl;
        this.authorStage = authorStage;
        if (helpfulPoints != null) {
            this.helpfulPoints.addAll(helpfulPoints);
        }
    }

    public boolean isWrittenBy(Long userId) {
        return userId != null && Objects.equals(this.userId, userId);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isAutoHidden() {
        return hidden && AUTO_HIDDEN_REASON.equals(hiddenReason);
    }

    // 서비스가 기존 값과 보낸 값을 합쳐 넘긴다. type·태그·작성자 상황은 바꾸지 않는다
    public void update(Integer rating, String oneLiner, PrepPeriod prepPeriod, Collection<String> helpfulPoints,
                       String content, String portfolioUrl) {
        this.rating = rating;
        this.oneLiner = oneLiner;
        this.prepPeriod = prepPeriod;
        this.helpfulPoints.clear();
        this.helpfulPoints.addAll(helpfulPoints);
        this.content = content;
        this.portfolioUrl = portfolioUrl;
    }

    public void delete(LocalDateTime now) {
        this.deletedAt = now;
    }

    public void hide(String reason) {
        this.hidden = true;
        this.hiddenReason = reason;
    }

    public void show() {
        this.hidden = false;
        this.hiddenReason = null;
    }
}
