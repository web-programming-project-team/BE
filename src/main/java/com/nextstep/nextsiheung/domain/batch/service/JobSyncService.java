package com.nextstep.nextsiheung.domain.batch.service;

import com.nextstep.nextsiheung.domain.batch.dto.JobSyncResult;
import com.nextstep.nextsiheung.domain.batch.dto.JobabaRow;
import com.nextstep.nextsiheung.domain.job.entity.JobPosting;
import com.nextstep.nextsiheung.domain.job.service.JobPostingUpsertService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// 잡아바 채용정보 수집
// 잡아바는 40만 건 넘게 쌓여 있고 오래된 공고(마감)가 앞 페이지, 최신 공고가 마지막 페이지에 있다.
// 그래서 마지막 페이지부터 거꾸로 읽고, 접수 중인 공고가 하나도 없는 페이지를 만나면 멈춘다.
@Slf4j
@Service
public class JobSyncService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final JobabaClient jobabaClient;
    private final JobabaMapper jobabaMapper;
    private final JobPostingUpsertService upsertService;
    private final int pageSize;
    private final int maxPages;

    public JobSyncService(JobabaClient jobabaClient, JobabaMapper jobabaMapper, JobPostingUpsertService upsertService,
                          @Value("${app.batch.job-sync-page-size:1000}") int pageSize,
                          @Value("${app.batch.job-sync-max-pages:30}") int maxPages) {
        this.jobabaClient = jobabaClient;
        this.jobabaMapper = jobabaMapper;
        this.upsertService = upsertService;
        this.pageSize = pageSize;
        this.maxPages = maxPages;
    }

    public JobSyncResult sync() {
        long startedAt = System.currentTimeMillis();
        LocalDate today = LocalDate.now(SEOUL);
        LocalDateTime collectedAt = LocalDateTime.now(SEOUL);

        int totalCount = jobabaClient.fetch(1, 1).totalCount();
        int lastPage = (totalCount + pageSize - 1) / pageSize;
        log.info("[잡아바 수집] 시작: 전체 {}건, 마지막 페이지 {}", totalCount, lastPage);

        int pages = 0;
        int fetched = 0;
        int open = 0;
        int matched = 0;
        int inserted = 0;
        int updated = 0;
        for (int page = lastPage; page >= 1 && pages < maxPages; page--) {
            List<JobabaRow> rows = jobabaClient.fetch(page, pageSize).rows();
            pages++;
            fetched += rows.size();

            int openInPage = 0;
            List<JobPosting> collected = new ArrayList<>();
            for (JobabaRow row : rows) {
                if (jobabaMapper.isOpen(row, today)) {
                    openInPage++;
                }
                jobabaMapper.toEntity(row, today, collectedAt).ifPresent(collected::add);
            }
            open += openInPage;
            matched += collected.size();

            JobPostingUpsertService.Result saved = upsertService.upsertAll(collected);
            inserted += saved.inserted();
            updated += saved.updated();
            log.info("[잡아바 수집] {}페이지: {}건 중 접수 중 {}건, 대상 지역 {}건 (추가 {}, 갱신 {})",
                    page, rows.size(), openInPage, collected.size(), saved.inserted(), saved.updated());

            if (openInPage == 0) {
                break;
            }
        }

        JobSyncResult result = new JobSyncResult(pages, fetched, open, matched, inserted, updated,
                System.currentTimeMillis() - startedAt);
        log.info("[잡아바 수집] 끝: {}", result);
        return result;
    }
}
