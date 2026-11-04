package com.nextstep.nextsiheung.domain.job.entity;

import com.nextstep.nextsiheung.global.common.enums.Education;
import com.nextstep.nextsiheung.global.common.enums.EmploymentType;
import com.nextstep.nextsiheung.global.common.enums.JobCategory;
import com.nextstep.nextsiheung.global.common.enums.JobSource;
import com.nextstep.nextsiheung.global.common.enums.Region;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

// ERD job_posting. 잡아바 수집 공고(JOBABA)와 관리자 등록 공고(ADMIN)를 같이 둔다
@Getter
@Entity
@Table(name = "job_posting")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobPosting {

    // career_level 값은 NEW(신입 가능) 하나. 잡아바는 신입 가능 공고만 수집한다
    public static final String CAREER_NEW = "NEW";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private JobSource source;

    // 잡아바는 공고 ID가 없어서 원문 URL의 SHA-256으로 같은 공고를 구분한다
    @Column(unique = true, length = 64)
    private String externalId;

    @Column(nullable = false, length = 100)
    private String company;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Region region;

    @Column(length = 200)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Education education;

    @Column(nullable = false, length = 30)
    private String careerLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private JobCategory jobCategory;

    @Column(length = 100)
    private String salary;

    // 상시채용이면 null
    private LocalDate deadline;

    @Column(nullable = false, length = 500)
    private String originalUrl;

    // 관리자 마감 처리. 마감일 경과는 deadline으로 판단한다
    @Column(nullable = false)
    private boolean closed;

    private Long createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime collectedAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private JobPosting(JobSource source, String externalId, String company, String title, Region region,
                       String address, Education education, String careerLevel, EmploymentType employmentType,
                       JobCategory jobCategory, String salary, LocalDate deadline, String originalUrl,
                       Long createdBy, LocalDateTime collectedAt) {
        this.source = source;
        this.externalId = externalId;
        this.company = company;
        this.title = title;
        this.region = region;
        this.address = address;
        this.education = education;
        this.careerLevel = careerLevel;
        this.employmentType = employmentType;
        this.jobCategory = jobCategory;
        this.salary = salary;
        this.deadline = deadline;
        this.originalUrl = originalUrl;
        this.createdBy = createdBy;
        this.collectedAt = collectedAt;
    }

    public void close() {
        this.closed = true;
    }

    // 다시 수집된 공고의 내용으로 갱신한다. collectedAt(최초 수집 일시)은 isNew 판단용, closed는 관리자 처리라 유지
    public void refreshFrom(JobPosting latest) {
        this.company = latest.company;
        this.title = latest.title;
        this.region = latest.region;
        this.address = latest.address;
        this.education = latest.education;
        this.careerLevel = latest.careerLevel;
        this.employmentType = latest.employmentType;
        this.jobCategory = latest.jobCategory;
        this.salary = latest.salary;
        this.deadline = latest.deadline;
        this.originalUrl = latest.originalUrl;
    }
}
