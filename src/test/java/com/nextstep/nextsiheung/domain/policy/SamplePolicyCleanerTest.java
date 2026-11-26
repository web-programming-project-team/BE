package com.nextstep.nextsiheung.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.nextstep.nextsiheung.domain.policy.service.SamplePolicyCleaner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

// 예전 샘플 정책과 참조 데이터를 지우고, 실제 정책·사용자 게시글은 남긴다 (로컬 MySQL 필요, 테스트마다 롤백)
@SpringBootTest
@Transactional
class SamplePolicyCleanerTest {

    @Autowired
    SamplePolicyCleaner cleaner;
    @Autowired
    JdbcTemplate jdbc;

    @Test
    void 샘플_정책과_참조_데이터를_지우고_실제_정책은_남긴다() {
        long sample = insertPolicy("https://example.com/sample/123", "샘플 정책");
        long real = insertPolicy("https://www.work24.go.kr", "실제 정책");
        jdbc.update("insert into policy_tag (policy_id, tag) values (?, '테스트')", sample);
        jdbc.update("insert into policy_life_stage (policy_id, life_stage) values (?, 'JOB_SEEKING')", sample);

        jdbc.update("""
                insert into users (email, password, nickname, role, onboarding_completed, agree_terms, agree_privacy,
                                   created_at, updated_at)
                values ('cleaner@test.kr', 'x', '정리', 'USER', 1, 1, 1, now(), now())""");
        long userId = jdbc.queryForObject("select last_insert_id()", Long.class);
        jdbc.update("""
                insert into roadmap (user_id, template_name, archived, created_at)
                values (?, '테스트', 0, now())""", userId);
        long roadmapId = jdbc.queryForObject("select last_insert_id()", Long.class);
        jdbc.update("insert into roadmap_step (roadmap_id, step_order, title) values (?, 1, '단계')", roadmapId);
        long stepId = jdbc.queryForObject("select last_insert_id()", Long.class);
        jdbc.update("""
                insert into roadmap_item (roadmap_step_id, policy_id, status, user_added, created_at, updated_at)
                values (?, ?, 'TO_CHECK', 0, now(), now())""", stepId, sample);
        long itemId = jdbc.queryForObject("select last_insert_id()", Long.class);
        jdbc.update("""
                insert into calendar_event (user_id, type, event_date, title, source_type, policy_id, item_id, created_at)
                values (?, 'APPLY_END', curdate(), '샘플 마감', 'POLICY', ?, ?, now())""", userId, sample, itemId);
        jdbc.update("""
                insert into roadmap_item (roadmap_step_id, policy_id, status, user_added, created_at, updated_at)
                values (?, ?, 'TO_CHECK', 0, now(), now())""", stepId, real);

        cleaner.run(new DefaultApplicationArguments());

        assertThat(count("select count(*) from policy where policy_id = ?", sample)).isZero();
        assertThat(count("select count(*) from policy_tag where policy_id = ?", sample)).isZero();
        assertThat(count("select count(*) from roadmap_item where policy_id = ?", sample)).isZero();
        assertThat(count("select count(*) from calendar_event where policy_id = ?", sample)).isZero();
        // 실제 정책과 그 로드맵 항목은 그대로
        assertThat(count("select count(*) from policy where policy_id = ?", real)).isOne();
        assertThat(count("select count(*) from roadmap_item where policy_id = ?", real)).isOne();
        // 다시 돌려도 아무 일 없음
        cleaner.run(new DefaultApplicationArguments());
        assertThat(count("select count(*) from policy where policy_id = ?", real)).isOne();
    }

    private long insertPolicy(String url, String title) {
        jdbc.update("""
                insert into policy (source, title, field, agency, target, content, recruit_status, source_url, apply_url,
                                    last_verified_at, created_at, updated_at)
                values ('ADMIN', ?, 'EMPLOYMENT', '기관', '청년', '내용', 'OPEN', ?, ?, curdate(), now(), now())""",
                title, url, url);
        return jdbc.queryForObject("select last_insert_id()", Long.class);
    }

    private long count(String sql, long id) {
        return jdbc.queryForObject(sql, Long.class, id);
    }
}
