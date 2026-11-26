package com.nextstep.nextsiheung.domain.policy.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 예전 샘플 시더(SampleDataSeeder, 삭제됨)가 넣은 화면 확인용 정책을 지운다. 출처 URL이 example.com/sample/인 정책
// 온통청년 수집으로 실제 정책이 들어오면서 샘플 기능을 없앴다. 각자 로컬 DB에 남은 샘플을 서버 시작 시 한 번 정리한다
// 샘플이 없으면 아무 일도 하지 않으므로 운영 서버에서 돌아도 안전하다 (팀원 DB가 모두 정리되면 이 클래스는 지워도 된다)
// 참조 데이터 순서: 로드맵 항목의 자동 일정·변경 이력 → 로드맵 항목 → 정책에 걸린 일정·템플릿·매칭·대상 상황·태그 → 정책
// 게시글은 사용자가 쓴 글이라 지우지 않고 연결 정책만 비운다
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class SamplePolicyCleaner implements ApplicationRunner {

    static final String SAMPLE_URL_LIKE = "https://example.com/sample/%";

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Long> ids = jdbc.queryForList("select policy_id from policy where source_url like :url",
                Map.of("url", SAMPLE_URL_LIKE), Long.class);
        if (ids.isEmpty()) {
            return;
        }
        Map<String, Object> p = Map.of("ids", ids);
        String items = "select item_id from roadmap_item where policy_id in (:ids)";
        int events = jdbc.update("delete from calendar_event where item_id in (" + items + ")", p)
                + jdbc.update("delete from calendar_event where policy_id in (:ids)", p);
        jdbc.update("delete from roadmap_item_history where item_id in (" + items + ")", p);
        int roadmapItems = jdbc.update("delete from roadmap_item where policy_id in (:ids)", p);
        jdbc.update("delete from template_step_policy where policy_id in (:ids)", p);
        jdbc.update("delete from job_policy_match where policy_id in (:ids)", p);
        jdbc.update("update post set policy_id = null where policy_id in (:ids)", p);
        jdbc.update("delete from policy_life_stage where policy_id in (:ids)", p);
        jdbc.update("delete from policy_tag where policy_id in (:ids)", p);
        int policies = jdbc.update("delete from policy where policy_id in (:ids)", p);
        log.info("[샘플 정리] 샘플 정책 {}건 삭제 (로드맵 항목 {}건, 캘린더 일정 {}건 함께 삭제)", policies, roadmapItems,
                events);
    }
}
