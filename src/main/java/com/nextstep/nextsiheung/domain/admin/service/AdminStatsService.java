package com.nextstep.nextsiheung.domain.admin.service;

import com.nextstep.nextsiheung.domain.admin.dto.StatsResponse;
import com.nextstep.nextsiheung.domain.admin.dto.SyncLogResponse;
import com.nextstep.nextsiheung.domain.batch.entity.SyncLog;
import com.nextstep.nextsiheung.domain.batch.repository.SyncLogRepository;
import com.nextstep.nextsiheung.domain.roadmap.repository.RoadmapItemRepository;
import com.nextstep.nextsiheung.global.common.Kst;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import com.nextstep.nextsiheung.global.response.PageResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 운영 지표(제안서 8-3·8-2)와 수집 기록. 개인 식별 정보 없이 집계만 돌려준다
@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private static final int DEFAULT_PERIOD_DAYS = 7;
    private static final int TOP_PROGRAM_LIMIT = 5;
    private static final Set<String> SOURCES = Set.of(SyncLog.SOURCE_ONTONG, SyncLog.SOURCE_JOBABA);

    private final RoadmapItemRepository roadmapItemRepository;
    private final SyncLogRepository syncLogRepository;
    private final JdbcTemplate jdbc;

    // 기간 기본값: 오늘까지 최근 7일
    @Transactional(readOnly = true)
    public StatsResponse stats(LocalDate from, LocalDate to) {
        LocalDate end = to == null ? Kst.today() : to;
        LocalDate start = from == null ? end.minusDays(DEFAULT_PERIOD_DAYS - 1) : from;
        if (start.isAfter(end)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        List<StatsResponse.TopProgram> topPrograms = roadmapItemRepository
                .findTopPrograms(PageRequest.of(0, TOP_PROGRAM_LIMIT)).stream()
                // TODO(커뮤니티): post 엔티티가 생기면 정책별 후기 수
                .map(row -> new StatsResponse.TopProgram((Long) row[0], (String) row[1], (Long) row[2], 0))
                .toList();

        // TODO(채용): job_bookmark 엔티티가 생기면 기간 안 관심 공고 저장 수
        long bookmarkedJobCount = 0;
        // TODO(커뮤니티): post 엔티티가 생기면 기간 안 후기 작성 수
        long reviewCount = 0;

        return new StatsResponse(new StatsResponse.Period(start, end), revisitRate7d(start, end),
                bookmarkedJobCount, reviewCount, topPrograms);
    }

    // 명세 '접속 기록': 기간 안에 가입한 사용자 중 가입 다음 날부터 7일 안에 user_visit 기록이 있는 비율
    private double revisitRate7d(LocalDate start, LocalDate end) {
        Double rate = jdbc.queryForObject("""
                select avg(case when exists (
                           select 1 from user_visit v
                           where v.user_id = u.user_id
                             and v.visit_date between date(u.created_at) + interval 1 day
                                                  and date(u.created_at) + interval 7 day
                       ) then 1 else 0 end)
                from users u
                where date(u.created_at) between ? and ?
                """, Double.class, start, end);
        // 가입자가 없으면 avg가 null
        return rate == null ? 0 : Math.round(rate * 100) / 100.0;
    }

    @Transactional(readOnly = true)
    public PageResponse<SyncLogResponse> syncLogs(String source, int page, int size) {
        if (page < 0 || size < 1 || size > 50 || (source != null && !SOURCES.contains(source))) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        PageRequest pageable = PageRequest.of(page, size);
        Page<SyncLog> logs = source == null
                ? syncLogRepository.findAllByOrderByStartedAtDescIdDesc(pageable)
                : syncLogRepository.findAllBySourceOrderByStartedAtDescIdDesc(source, pageable);
        return PageResponse.of(logs.map(l -> new SyncLogResponse(l.getId(), l.getSource(), l.getTriggerType(),
                l.getStatus(), l.getFetched(), l.getInserted(), l.getUpdated(), l.getFailed(), l.getErrorMessage(),
                l.getStartedAt(), l.getFinishedAt())));
    }
}
