package com.nextstep.nextsiheung.domain.admin.service;

import com.nextstep.nextsiheung.domain.admin.dto.AdminJobRequest;
import com.nextstep.nextsiheung.domain.admin.dto.AdminJobResponse;
import com.nextstep.nextsiheung.domain.job.entity.JobPosting;
import com.nextstep.nextsiheung.domain.job.repository.JobPostingRepository;
import com.nextstep.nextsiheung.domain.job.service.JobPolicyMatchService;
import com.nextstep.nextsiheung.global.common.Kst;
import com.nextstep.nextsiheung.global.common.enums.JobSource;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 관리자 채용공고 등록·수정·마감. 잡아바에 없는 지역 공고나 시연용 공고를 source=ADMIN으로 넣는다
@Service
@RequiredArgsConstructor
public class AdminJobService {

    private final JobPostingRepository jobPostingRepository;
    private final JobPolicyMatchService jobPolicyMatchService;

    @Transactional
    public AdminJobResponse create(Long adminId, AdminJobRequest r) {
        JobPosting job = jobPostingRepository.save(JobPosting.builder()
                .source(JobSource.ADMIN)
                .company(r.company().trim()).title(r.title().trim())
                .region(r.region()).address(trimOrNull(r.address()))
                .education(r.education()).careerLevel(careerLevel(r.careerLevel()))
                .employmentType(r.employmentType()).jobCategory(r.jobCategory())
                .salary(trimOrNull(r.salary())).deadline(r.deadline())
                .originalUrl(originalUrl(r.originalUrl()))
                .createdBy(adminId)
                .collectedAt(Kst.now())
                .build());
        jobPolicyMatchService.rebuildFor(job);
        return toResponse(job);
    }

    // 본문 전체 교체. 잡아바 수집 공고는 다음 수집 때 덮어써지므로 관리자 등록 공고만 수정한다
    @Transactional
    public AdminJobResponse update(Long jobId, AdminJobRequest r) {
        JobPosting job = find(jobId);
        if (!job.isAdminPosted()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        job.updateByAdmin(r.company().trim(), r.title().trim(), r.region(), trimOrNull(r.address()), r.education(),
                careerLevel(r.careerLevel()), r.employmentType(), r.jobCategory(), trimOrNull(r.salary()),
                r.deadline(), originalUrl(r.originalUrl()));
        jobPostingRepository.flush();
        jobPolicyMatchService.rebuildFor(job);
        return toResponse(job);
    }

    // 맞춤 채용 목록에서 빠지고, 관심 공고 목록에는 closed=true로 남는다. 잡아바 공고도 마감할 수 있다
    @Transactional
    public AdminJobResponse close(Long jobId) {
        JobPosting job = find(jobId);
        job.close();
        return toResponse(job);
    }

    private JobPosting find(Long jobId) {
        return jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JOB_NOT_FOUND));
    }

    private static AdminJobResponse toResponse(JobPosting job) {
        return new AdminJobResponse(job.getId(), job.getSource(), job.isClosed());
    }

    // 값은 NEW(신입 가능) 하나. 비우면 NEW
    private static String careerLevel(String value) {
        if (value == null || value.isBlank()) {
            return JobPosting.CAREER_NEW;
        }
        if (!JobPosting.CAREER_NEW.equals(value.trim())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return JobPosting.CAREER_NEW;
    }

    // ERD original_url NOT NULL. 상세 화면의 '원문 보기' 링크라 비울 수 없다
    private static String originalUrl(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return value.trim();
    }

    private static String trimOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
