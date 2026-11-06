package com.nextstep.nextsiheung.domain.job;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

import com.nextstep.nextsiheung.domain.notification.service.NewJobNotificationService;
import com.nextstep.nextsiheung.global.common.Kst;
import com.nextstep.nextsiheung.global.common.enums.Role;
import com.nextstep.nextsiheung.global.security.JwtProvider;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

// 관리자 공고·맞춤 채용·관심 공고·공고-정책 매칭·알림 센터 (로컬 MySQL 필요, 테스트마다 롤백)
// 로컬 DB에 잡아바 공고가 있을 수 있어 목록은 '내가 만든 공고가 들어 있는지'로 확인한다
@SpringBootTest(properties = "app.seed.sample-data=false")
@AutoConfigureMockMvc
@Transactional
class JobNotificationFlowTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    JsonMapper json;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    EntityManager em;
    @Autowired
    NewJobNotificationService newJobNotificationService;
    @Value("${app.jwt.secret}")
    String secret;

    @Test
    void 관리자_공고를_등록하면_맞춤_목록과_상세에_도움_되는_정책이_붙는다() throws Exception {
        String admin = token(insertUser("job-admin@test.kr", "ADMIN", false), Role.ADMIN);
        long policyId = createItPolicy(admin);
        long jobId = createJob(admin, "IT", Kst.today().plusDays(5));

        long userId = insertUser("job-user@test.kr", "USER", true);
        insertPreference(userId, "SIHEUNG", "IT", "FULL_TIME", "UNIVERSITY");
        jdbc.update("insert into user_visit (user_id, visit_date) values (?, ?)", userId, Kst.today().minusDays(1));
        String user = token(userId, Role.USER);

        mvc.perform(get("/api/v1/jobs/recommended").header("Authorization", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.appliedFilter.regions[0]").value("SIHEUNG"))
                .andExpect(jsonPath("$.data.content[*].jobId", hasItem((int) jobId)))
                .andExpect(jsonPath("$.data.content[?(@.jobId == " + jobId + ")].isNew").value(true))
                .andExpect(jsonPath("$.data.content[?(@.jobId == " + jobId + ")].dataSource").value("ADMIN"));

        // 직무 태그(IT)가 맞는 정책이 맨 앞
        mvc.perform(get("/api/v1/jobs/" + jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.helpfulPolicies[0].policyId").value(policyId))
                .andExpect(jsonPath("$.data.helpfulPolicies[0].myStatus").value(nullValue()))
                .andExpect(jsonPath("$.data.bookmarked").value(false))
                .andExpect(jsonPath("$.data.reviews").isEmpty());

        // 정책 상세의 연결 채용공고
        mvc.perform(get("/api/v1/policies/" + policyId))
                .andExpect(jsonPath("$.data.relatedJobs[*].jobId", hasItem((int) jobId)));

        // 조건이 다른 직무는 목록에 없다
        long officeJob = createJob(admin, "OFFICE", Kst.today().plusDays(5));
        mvc.perform(get("/api/v1/jobs/recommended").header("Authorization", user).param("sort", "latest"))
                .andExpect(jsonPath("$.data.content[*].jobId", not(hasItem((int) officeJob))));

        // 관리자가 마감하면 목록에서 빠진다
        mvc.perform(patch("/api/v1/admin/jobs/" + jobId + "/close").header("Authorization", admin))
                .andExpect(jsonPath("$.data.closed").value(true));
        mvc.perform(get("/api/v1/jobs/recommended").header("Authorization", user))
                .andExpect(jsonPath("$.data.content[*].jobId", not(hasItem((int) jobId))));
    }

    @Test
    void 관리자_공고_수정은_ADMIN_공고만_되고_입력을_검사한다() throws Exception {
        String admin = token(insertUser("job-admin2@test.kr", "ADMIN", false), Role.ADMIN);
        long jobId = createJob(admin, "IT", Kst.today().plusDays(5));

        mvc.perform(put("/api/v1/admin/jobs/" + jobId).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(jobBody("DESIGN", Kst.today().plusDays(9))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.source").value("ADMIN"));
        mvc.perform(get("/api/v1/jobs/" + jobId)).andExpect(jsonPath("$.data.jobCategory").value("DESIGN"));

        jdbc.update("""
                insert into job_posting (source, external_id, company, title, region, education, career_level,
                    employment_type, job_category, original_url, closed, collected_at, updated_at)
                values ('JOBABA', 'flow-test-ext', '잡아바회사', '잡아바 공고', 'SIHEUNG', 'ANY', 'NEW',
                    'FULL_TIME', 'IT', 'https://example.com/jobaba', 0, now(), now())""");
        Long jobabaId = jdbc.queryForObject("select last_insert_id()", Long.class);
        mvc.perform(put("/api/v1/admin/jobs/" + jobabaId).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(jobBody("IT", Kst.today().plusDays(9))))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/admin/jobs").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"company\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        mvc.perform(patch("/api/v1/admin/jobs/999999999/close").header("Authorization", admin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("JOB_NOT_FOUND"));
    }

    @Test
    void 관심_공고를_저장하면_캘린더에_마감일이_생기고_해제하면_함께_지워진다() throws Exception {
        String admin = token(insertUser("job-admin3@test.kr", "ADMIN", false), Role.ADMIN);
        long jobId = createJob(admin, "IT", Kst.today().plusDays(5));
        // 상시채용(마감일 없음)은 잡아바 공고에만 있다 (관리자 등록은 마감일 필수)
        jdbc.update("""
                insert into job_posting (source, external_id, company, title, region, education, career_level,
                    employment_type, job_category, original_url, closed, collected_at, updated_at)
                values ('JOBABA', 'flow-test-always', '상시회사', '상시 채용', 'SIHEUNG', 'ANY', 'NEW',
                    'FULL_TIME', 'IT', 'https://example.com/always', 0, now(), now())""");
        long alwaysOpen = jdbc.queryForObject("select last_insert_id()", Long.class);
        long userId = insertUser("bookmark-user@test.kr", "USER", true);
        String user = token(userId, Role.USER);

        String saved = mvc.perform(post("/api/v1/jobs/" + jobId + "/bookmark").header("Authorization", user))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bookmarked").value(true))
                .andReturn().getResponse().getContentAsString();
        long eventId = json.readTree(saved).path("data").path("calendarEventId").asLong();
        assertThat(eventId).isPositive();

        mvc.perform(post("/api/v1/jobs/" + jobId + "/bookmark").header("Authorization", user))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ALREADY_EXISTS"));
        // 상시채용은 일정 없이 저장
        mvc.perform(post("/api/v1/jobs/" + alwaysOpen + "/bookmark").header("Authorization", user))
                .andExpect(jsonPath("$.data.calendarEventId").value(nullValue()));

        // 자동 일정은 직접 지울 수 없다
        mvc.perform(delete("/api/v1/calendar/events/" + eventId).header("Authorization", user))
                .andExpect(status().isForbidden());

        // 마감 임박순, 상시채용은 뒤. 관리자 마감하면 closed=true로 맨 뒤
        mvc.perform(get("/api/v1/jobs/bookmarks").header("Authorization", user))
                .andExpect(jsonPath("$.data.content[0].jobId").value(jobId))
                .andExpect(jsonPath("$.data.content[0].dDay").value(5))
                .andExpect(jsonPath("$.data.content[1].jobId").value(alwaysOpen));
        mvc.perform(patch("/api/v1/admin/jobs/" + jobId + "/close").header("Authorization", admin));
        mvc.perform(get("/api/v1/jobs/bookmarks").header("Authorization", user))
                .andExpect(jsonPath("$.data.content[1].jobId").value(jobId))
                .andExpect(jsonPath("$.data.content[1].closed").value(true));

        mvc.perform(delete("/api/v1/jobs/" + jobId + "/bookmark").header("Authorization", user))
                .andExpect(status().isNoContent());
        em.flush();
        assertThat(jdbc.queryForObject("select count(*) from calendar_event where event_id = ?", Long.class,
                eventId)).isZero();
        mvc.perform(get("/api/v1/jobs/bookmarks").header("Authorization", user))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void 온보딩_전이면_맞춤_목록은_ONBOARDING_REQUIRED() throws Exception {
        String user = token(insertUser("not-onboarded@test.kr", "USER", false), Role.USER);
        mvc.perform(get("/api/v1/jobs/recommended").header("Authorization", user))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ONBOARDING_REQUIRED"));
        mvc.perform(get("/api/v1/jobs/recommended").header("Authorization", user).param("sort", "x"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 수집_후_새_맞춤_공고_알림을_보내고_읽음_처리한다() throws Exception {
        String admin = token(insertUser("noti-admin@test.kr", "ADMIN", false), Role.ADMIN);
        long userId = insertUser("noti-user@test.kr", "USER", true);
        insertPreference(userId, "SIHEUNG", "IT", "FULL_TIME", "UNIVERSITY");
        jdbc.update("insert into user_visit (user_id, visit_date) values (?, ?)", userId, Kst.today().minusDays(1));
        createJob(admin, "IT", Kst.today().plusDays(5));
        String user = token(userId, Role.USER);

        newJobNotificationService.sendAfterSync();
        em.flush();
        em.clear();

        String list = mvc.perform(get("/api/v1/notifications").header("Authorization", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].type").value("NEW_JOB"))
                .andExpect(jsonPath("$.data.content[0].link.targetType").value("JOB_LIST"))
                .andExpect(jsonPath("$.data.content[0].read").value(false))
                .andReturn().getResponse().getContentAsString();
        long notificationId = json.readTree(list).path("data").path("content").get(0).path("notificationId").asLong();

        // 같은 공고로 다시 돌려도 새로 세지 않는다
        newJobNotificationService.sendAfterSync();
        em.flush();
        mvc.perform(get("/api/v1/notifications/unread-count").header("Authorization", user))
                .andExpect(jsonPath("$.data.unreadCount").value(1));
        mvc.perform(get("/api/v1/notifications").header("Authorization", user).param("type", "DEADLINE"))
                .andExpect(jsonPath("$.data.totalElements").value(0));

        // 다른 사람 알림은 읽음 처리 불가
        String other = token(insertUser("noti-other@test.kr", "USER", true), Role.USER);
        mvc.perform(patch("/api/v1/notifications/" + notificationId + "/read").header("Authorization", other))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/v1/notifications/999999999/read").header("Authorization", user))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOTIFICATION_NOT_FOUND"));

        mvc.perform(patch("/api/v1/notifications/" + notificationId + "/read").header("Authorization", user))
                .andExpect(jsonPath("$.data.read").value(true));
        mvc.perform(get("/api/v1/notifications/unread-count").header("Authorization", user))
                .andExpect(jsonPath("$.data.unreadCount").value(0));
        mvc.perform(patch("/api/v1/notifications/read-all").header("Authorization", user))
                .andExpect(jsonPath("$.data.updatedCount").value(0));
    }

    private long createItPolicy(String admin) throws Exception {
        String body = """
                {"title":"IT 직무 부트캠프","field":"EMPLOYMENT","agency":"시흥 청년스테이션",
                 "target":"시흥 청년","content":"내용","applyStartDate":"%s","applyEndDate":"%s",
                 "sourceUrl":"https://example.com/it-bootcamp","lifeStages":["JOB_SEEKING"],"tags":["IT 개발"]}"""
                .formatted(Kst.today().minusDays(1), Kst.today().plusDays(30));
        String res = mvc.perform(post("/api/v1/admin/policies").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(res).path("data").path("policyId").asLong();
    }

    private long createJob(String admin, String category, java.time.LocalDate deadline) throws Exception {
        String res = mvc.perform(post("/api/v1/admin/jobs").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(jobBody(category, deadline)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.source").value("ADMIN"))
                .andExpect(jsonPath("$.data.closed").value(false))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(res).path("data").path("jobId").asLong();
    }

    private static String jobBody(String category, java.time.LocalDate deadline) {
        return """
                {"company":"(주)플로우테스트","title":"신입 채용","region":"SIHEUNG","address":"경기 시흥시",
                 "education":"COLLEGE","careerLevel":"NEW","employmentType":"FULL_TIME","jobCategory":"%s",
                 "salary":"회사 내규","deadline":%s,"originalUrl":"https://example.com/job"}"""
                .formatted(category, deadline == null ? "null" : "\"" + deadline + "\"");
    }

    // 테스트 트랜잭션이 커넥션 하나를 쓰므로 last_insert_id()로 방금 넣은 ID를 받는다
    private long insertUser(String email, String role, boolean onboarded) {
        jdbc.update("""
                insert into users (email, password, nickname, role, onboarding_completed, agree_terms, agree_privacy,
                                   created_at, updated_at)
                values (?, 'x', '테스트', ?, ?, 1, 1, now(), now())""", email, role, onboarded);
        return jdbc.queryForObject("select last_insert_id()", Long.class);
    }

    private void insertPreference(long userId, String region, String category, String type, String education) {
        jdbc.update("insert into job_preference (user_id, education, career_level, updated_at) values (?, ?, 'NEW', now())",
                userId, education);
        jdbc.update("insert into job_preference_region (user_id, region) values (?, ?)", userId, region);
        jdbc.update("insert into job_preference_category (user_id, job_category) values (?, ?)", userId, category);
        jdbc.update("insert into job_preference_employment_type (user_id, employment_type) values (?, ?)", userId, type);
    }

    private String token(long userId, Role role) {
        return "Bearer " + new JwtProvider(secret, 1800).createAccessToken(userId, role);
    }
}
