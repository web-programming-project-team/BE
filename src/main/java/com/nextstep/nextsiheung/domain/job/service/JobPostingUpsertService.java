package com.nextstep.nextsiheung.domain.job.service;

import com.nextstep.nextsiheung.domain.job.entity.JobPosting;
import com.nextstep.nextsiheung.domain.job.repository.JobPostingRepository;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 수집한 공고를 externalId 기준으로 새로 넣거나 갱신한다
@Service
@RequiredArgsConstructor
public class JobPostingUpsertService {

    private final JobPostingRepository jobPostingRepository;

    @Transactional
    public Result upsertAll(List<JobPosting> collected) {
        // 같은 페이지 안에 같은 URL이 두 번 나오면 마지막 것만 쓴다
        Map<String, JobPosting> byExternalId = new LinkedHashMap<>();
        collected.forEach(job -> byExternalId.put(job.getExternalId(), job));

        Map<String, JobPosting> existing = new HashMap<>();
        for (JobPosting saved : jobPostingRepository.findByExternalIdIn(byExternalId.keySet())) {
            existing.put(saved.getExternalId(), saved);
        }

        int inserted = 0;
        int updated = 0;
        for (JobPosting job : byExternalId.values()) {
            JobPosting saved = existing.get(job.getExternalId());
            if (saved == null) {
                jobPostingRepository.save(job);
                inserted++;
            } else {
                saved.refreshFrom(job);
                updated++;
            }
        }
        return new Result(inserted, updated);
    }

    public record Result(int inserted, int updated) {
    }
}
