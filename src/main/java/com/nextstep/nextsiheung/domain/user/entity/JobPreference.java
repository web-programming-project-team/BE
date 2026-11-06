package com.nextstep.nextsiheung.domain.user.entity;

import com.nextstep.nextsiheung.global.common.enums.Education;
import com.nextstep.nextsiheung.global.common.enums.EmploymentType;
import com.nextstep.nextsiheung.global.common.enums.JobCategory;
import com.nextstep.nextsiheung.global.common.enums.Region;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

// ERD job_preference (+ region·category·employment_type 복수 테이블). 회원 1명에 1행, 맞춤 채용 필터에 쓴다
// 회원 탈퇴 시 DB가 함께 지운다(ON DELETE CASCADE)
@Getter
@Entity
@Table(name = "job_preference")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobPreference {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Education education;

    // 경력 수준. 값은 NEW(신입) 하나
    @Column(length = 30)
    private String careerLevel;

    @ElementCollection
    @CollectionTable(name = "job_preference_region", joinColumns = @JoinColumn(name = "user_id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Enumerated(EnumType.STRING)
    @Column(name = "region", nullable = false, length = 30)
    private Set<Region> regions = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "job_preference_category", joinColumns = @JoinColumn(name = "user_id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Enumerated(EnumType.STRING)
    @Column(name = "job_category", nullable = false, length = 30)
    private Set<JobCategory> jobCategories = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "job_preference_employment_type", joinColumns = @JoinColumn(name = "user_id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 30)
    private Set<EmploymentType> employmentTypes = new LinkedHashSet<>();

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public JobPreference(User user) {
        this.user = user;
    }

    // 보낸 값(null 아닌 것)만 바꾼다. 온보딩 등록과 PATCH /users/me/job-preference가 같이 쓴다
    public void apply(Collection<Region> regions, Education education, String careerLevel,
                      Collection<JobCategory> jobCategories, Collection<EmploymentType> employmentTypes) {
        if (regions != null) {
            replace(this.regions, regions);
        }
        if (education != null) {
            this.education = education;
        }
        if (careerLevel != null) {
            this.careerLevel = careerLevel;
        }
        if (jobCategories != null) {
            replace(this.jobCategories, jobCategories);
        }
        if (employmentTypes != null) {
            replace(this.employmentTypes, employmentTypes);
        }
        // 컬렉션만 바뀌면 이 행은 dirty가 아니라서 @LastModifiedDate가 안 돈다
        this.updatedAt = LocalDateTime.now();
    }

    private static <T> void replace(Set<T> target, Collection<T> values) {
        target.clear();
        target.addAll(values);
    }
}
