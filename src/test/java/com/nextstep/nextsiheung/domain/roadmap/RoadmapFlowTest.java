package com.nextstep.nextsiheung.domain.roadmap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nextstep.nextsiheung.domain.policy.entity.Policy;
import com.nextstep.nextsiheung.domain.policy.repository.PolicyRepository;
import com.nextstep.nextsiheung.domain.roadmap.entity.RoadmapTemplate;
import com.nextstep.nextsiheung.domain.roadmap.repository.RoadmapTemplateRepository;
import com.nextstep.nextsiheung.global.common.Kst;
import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.LifeStage;
import com.nextstep.nextsiheung.global.common.enums.PolicySource;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

// 온보딩 → 로드맵 생성 → 진행상태 변경 → 정책 추가·삭제 → 캘린더 → 메인 요약 → 상황 변경 → 탈퇴
// (로컬 MySQL 필요, 테스트마다 롤백)
@SpringBootTest(properties = "app.seed.sample-data=false")
@AutoConfigureMockMvc
@Transactional
class RoadmapFlowTest {

    private static final String PASSWORD = "next1234!";

    // seed 템플릿보다 점수가 높게 나오도록 상황 3개 + 관심분야 3개를 모두 겹치게 한다
    private static final String PROFILE = """
            {"lifeStages":["GRADUATING","JOB_SEEKING","ON_LEAVE"],
             "interests":["EMPLOYMENT","HOUSING","COUNSELING"],
             "jobPreference":{"regions":["SIHEUNG"],"education":"UNIVERSITY","careerLevel":"NEW",
                              "jobCategories":["IT"],"employmentTypes":["FULL_TIME"]}}""";

    @Autowired
    MockMvc mvc;
    @Autowired
    JsonMapper json;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    EntityManager em;
    @Autowired
    PolicyRepository policyRepository;
    @Autowired
    RoadmapTemplateRepository templateRepository;

    Policy counseling;
    Policy academy;
    Policy ended;
    Policy rent;
    Policy extra;
    Policy endedExtra;

    @BeforeEach
    void setUp() {
        LocalDate t = Kst.today();
        counseling = policy("테스트 진로 상담", InterestField.COUNSELING, null, null);
        academy = policy("테스트 직무 아카데미", InterestField.EMPLOYMENT, t.minusDays(3), t.plusDays(5));
        ended = policy("테스트 종료 정책", InterestField.EMPLOYMENT, t.minusDays(3), t.plusDays(5));
        ended.end("사업 종료");
        rent = policy("테스트 월세 지원", InterestField.HOUSING, t.plusDays(2), t.plusDays(30));
        extra = policy("테스트 추가 정책", InterestField.HOUSING, t.minusDays(1), t.plusDays(10));
        endedExtra = policy("테스트 종료된 추가 정책", InterestField.HOUSING, null, null);
        endedExtra.end("사업 종료");

        RoadmapTemplate template = new RoadmapTemplate("테스트 템플릿",
                List.of(LifeStage.GRADUATING, LifeStage.JOB_SEEKING, LifeStage.ON_LEAVE),
                List.of(InterestField.EMPLOYMENT, InterestField.HOUSING, InterestField.COUNSELING));
        template.addStep("상담").addPolicy(counseling);
        template.addStep("직무역량").addPolicy(academy).addPolicy(ended);
        template.addStep("주거").addPolicy(rent);
        templateRepository.save(template);
    }

    @Test
    void 온보딩부터_상황_변경까지_로드맵_흐름() throws Exception {
        String token = signupAndLogin("roadmap@test.kr");

        // 상태 등록 전
        mvc.perform(get("/api/v1/roadmaps/me").header("Authorization", token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ONBOARDING_REQUIRED"));

        // 온보딩: 가장 많이 겹치는 템플릿으로 로드맵 생성
        JsonNode saved = data(mvc.perform(put("/api/v1/users/me/profile").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(PROFILE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onboardingCompleted").value(true))
                .andExpect(jsonPath("$.data.templateName").value("테스트 템플릿")));
        long roadmapId = saved.path("roadmapId").asLong();

        // 다시 등록해도 로드맵은 그대로
        mvc.perform(put("/api/v1/users/me/profile").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(PROFILE))
                .andExpect(jsonPath("$.data.roadmapId").value(roadmapId));

        // 종료된 정책은 빠지고 나머지는 TO_CHECK
        JsonNode roadmap = data(mvc.perform(get("/api/v1/roadmaps/me").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.steps", hasSize(3)))
                .andExpect(jsonPath("$.data.steps[1].items", hasSize(1)))
                .andExpect(jsonPath("$.data.steps[1].items[0].title").value("테스트 직무 아카데미"))
                .andExpect(jsonPath("$.data.steps[1].items[0].dDay").value(5))
                .andExpect(jsonPath("$.data.steps[2].items[0].recruitStatus").value("UPCOMING"))
                .andExpect(jsonPath("$.data.progress.remaining").value(3))
                .andExpect(jsonPath("$.data.currentStep").value(1)));
        long counselingItem = roadmap.path("steps").get(0).path("items").get(0).path("itemId").asLong();
        long academyItem = roadmap.path("steps").get(1).path("items").get(0).path("itemId").asLong();

        // 프로필 조회와 채용 조건 부분 수정
        mvc.perform(get("/api/v1/users/me/profile").header("Authorization", token))
                .andExpect(jsonPath("$.data.lifeStages", hasSize(3)))
                .andExpect(jsonPath("$.data.lifeStages[0]").value("GRADUATING"))
                .andExpect(jsonPath("$.data.lifeStages[1]").value("ON_LEAVE"))
                .andExpect(jsonPath("$.data.jobPreference.education").value("UNIVERSITY"));
        mvc.perform(patch("/api/v1/users/me/job-preference").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"regions\":[\"ANSAN\",\"SIHEUNG\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.regions", hasSize(2)))
                // 복수 값은 보낸 순서·DB 순서와 상관없이 enum 선언 순서로 온다 (시흥 → 안산)
                .andExpect(jsonPath("$.data.regions[0]").value("SIHEUNG"))
                .andExpect(jsonPath("$.data.jobCategories[0]").value("IT"));

        // 진행상태: COMPLETED로 바뀔 때만 후기 제안
        mvc.perform(patch("/api/v1/roadmap-items/" + counselingItem + "/status").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.suggestReview").value(true))
                .andExpect(jsonPath("$.data.progress.completed").value(1))
                .andExpect(jsonPath("$.data.progress.rate").value(33));
        mvc.perform(patch("/api/v1/roadmap-items/" + academyItem + "/status").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PLANNED\"}"))
                .andExpect(jsonPath("$.data.suggestReview").value(false))
                .andExpect(jsonPath("$.data.progress.inProgress").value(1));
        mvc.perform(get("/api/v1/roadmaps/me").header("Authorization", token))
                .andExpect(jsonPath("$.data.currentStep").value(2))
                .andExpect(jsonPath("$.data.steps[0].completed").value(true));

        // 정책 추가: 분야(주거)가 같은 단계에 들어가고 접수 일정 2개가 캘린더에 생긴다
        JsonNode added = data(mvc.perform(post("/api/v1/roadmaps/me/items").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"policyId\":" + extra.getId() + ",\"status\":\"INTERESTED\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.stepOrder").value(3))
                .andExpect(jsonPath("$.data.calendarEventIds", hasSize(2))));
        long addedItem = added.path("itemId").asLong();
        mvc.perform(post("/api/v1/roadmaps/me/items").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"policyId\":" + extra.getId() + ",\"status\":\"INTERESTED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ALREADY_EXISTS"));
        mvc.perform(post("/api/v1/roadmaps/me/items").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"policyId\":" + endedExtra.getId() + ",\"status\":\"INTERESTED\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("POLICY_ENDED"));

        // 정책 상세·목록에 내 진행상태
        mvc.perform(get("/api/v1/policies/" + extra.getId()).header("Authorization", token))
                .andExpect(jsonPath("$.data.myItem.itemId").value(addedItem))
                .andExpect(jsonPath("$.data.myItem.status").value("INTERESTED"));
        mvc.perform(get("/api/v1/policies/" + extra.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.myItem").doesNotExist());
        mvc.perform(get("/api/v1/policies").param("keyword", "테스트 추가").header("Authorization", token))
                .andExpect(jsonPath("$.data.content[0].myStatus").value("INTERESTED"));

        // 캘린더: 자동 일정은 직접 못 지우고, 직접 추가한 일정은 지운다
        LocalDate end = extra.getApplyEndDate();
        JsonNode month = data(mvc.perform(get("/api/v1/calendar/events").header("Authorization", token)
                        .param("year", String.valueOf(end.getYear()))
                        .param("month", String.valueOf(end.getMonthValue()))
                        .param("types", "APPLY_END"))
                .andExpect(status().isOk()));
        JsonNode extraEnd = null;
        for (JsonNode e : month) {
            if (e.path("sourceId").asLong() == extra.getId()) {
                extraEnd = e;
            }
        }
        assertThat(extraEnd).isNotNull();
        assertThat(extraEnd.path("itemStatus").asString()).isEqualTo("INTERESTED");
        mvc.perform(delete("/api/v1/calendar/events/" + extraEnd.path("eventId").asLong())
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
        long customId = data(mvc.perform(post("/api/v1/calendar/events").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"CUSTOM\",\"date\":\"" + Kst.today() + "\",\"eventTime\":\"15:00\","
                                + "\"title\":\"면접 준비\"}"))
                .andExpect(status().isCreated())).path("eventId").asLong();
        mvc.perform(get("/api/v1/calendar/events/upcoming").header("Authorization", token))
                .andExpect(jsonPath("$.data[*].eventId", hasItem((int) customId)));
        mvc.perform(delete("/api/v1/calendar/events/" + customId).header("Authorization", token))
                .andExpect(status().isNoContent());

        // 메인 요약: 신청 예정이 먼저, 다가오는 마감은 14일 안
        mvc.perform(get("/api/v1/home/summary").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("테스터"))
                .andExpect(jsonPath("$.data.nextActions[0].itemId").value(academyItem))
                .andExpect(jsonPath("$.data.nextActions[0].actionLabel").value("공식 사이트에서 신청하기"))
                .andExpect(jsonPath("$.data.upcomingDeadlines[0].title").value("테스트 직무 아카데미 접수 마감"))
                .andExpect(jsonPath("$.data.upcomingDeadlines[0].dDay").value(5));

        // 템플릿 항목은 못 지우고, 직접 추가한 항목은 일정과 함께 지워진다
        mvc.perform(delete("/api/v1/roadmap-items/" + academyItem).header("Authorization", token))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/roadmap-items/" + addedItem).header("Authorization", token))
                .andExpect(status().isNoContent());
        em.flush();
        assertThat(jdbc.queryForObject("select count(*) from calendar_event where item_id = ?", Integer.class,
                addedItem)).isZero();

        // 상황 변경: 기존 로드맵은 보관(읽기 전용), 새 로드맵 생성, 첫 취업이면 후기 제안
        JsonNode changed = data(mvc.perform(post("/api/v1/users/me/life-stage-change").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lifeStages\":[\"FIRST_JOB\"],\"interests\":[\"HOUSING\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.archivedRoadmapId").value(roadmapId))
                .andExpect(jsonPath("$.data.suggestReview").value(true)));
        assertThat(changed.path("newRoadmapId").asLong()).isNotEqualTo(roadmapId);

        mvc.perform(get("/api/v1/roadmaps/me/history").header("Authorization", token))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].roadmapId").value(roadmapId))
                .andExpect(jsonPath("$.data[0].completedCount").value(1));
        mvc.perform(get("/api/v1/roadmaps/" + roadmapId).header("Authorization", token))
                .andExpect(jsonPath("$.data.archived").value(true))
                .andExpect(jsonPath("$.data.archivedAt").isNotEmpty());
        mvc.perform(patch("/api/v1/roadmap-items/" + academyItem + "/status").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"APPLIED\"}"))
                .andExpect(status().isForbidden());
        // 보관한 로드맵의 자동 일정은 캘린더에서 빠진다
        em.flush();
        assertThat(jdbc.queryForObject("select count(*) from calendar_event where item_id = ?", Integer.class,
                academyItem)).isZero();

        // 남의 로드맵·항목
        String other = signupAndLogin("other@test.kr");
        mvc.perform(get("/api/v1/roadmaps/" + roadmapId).header("Authorization", other))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/v1/roadmap-items/" + counselingItem + "/status").header("Authorization", other)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"APPLIED\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/roadmaps/999999999").header("Authorization", other))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ROADMAP_NOT_FOUND"));

        // 탈퇴하면 로드맵·항목·일정·채용 조건이 함께 지워진다 (DB ON DELETE CASCADE)
        long userId = jdbc.queryForObject("select user_id from users where email = 'roadmap@test.kr'", Long.class);
        mvc.perform(delete("/api/v1/users/me").header("Authorization", token)).andExpect(status().isNoContent());
        em.flush();
        assertThat(jdbc.queryForObject("select count(*) from roadmap where user_id = ?", Integer.class, userId))
                .isZero();
        assertThat(jdbc.queryForObject("select count(*) from calendar_event where user_id = ?", Integer.class,
                userId)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from job_preference_region where user_id = ?",
                Integer.class, userId)).isZero();
    }

    @Test
    void 입력값_오류와_상태_등록_전_요약() throws Exception {
        String token = signupAndLogin("invalid@test.kr");
        mvc.perform(put("/api/v1/users/me/profile").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"lifeStages\":[],\"interests\":[\"HOUSING\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        mvc.perform(get("/api/v1/home/summary").header("Authorization", token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ONBOARDING_REQUIRED"));
        mvc.perform(post("/api/v1/users/me/life-stage-change").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lifeStages\":[\"FIRST_JOB\"],\"interests\":[\"HOUSING\"]}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/calendar/events").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"type\":\"CUSTOM\",\"date\":\"2026-11-10\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/policies").param("sort", "popular"))
                .andExpect(status().isBadRequest());
        // 기본 목록은 접수 중·예정만 (종료 정책 제외)
        mvc.perform(get("/api/v1/policies").param("keyword", "테스트").param("size", "50"))
                .andExpect(jsonPath("$.data.content[*].title", not(hasItem("테스트 종료 정책"))))
                .andExpect(jsonPath("$.data.content[*].title", hasItem("테스트 월세 지원")));
    }

    private Policy policy(String title, InterestField field, LocalDate start, LocalDate end) {
        return policyRepository.save(Policy.builder()
                .source(PolicySource.ADMIN).title(title).field(field).agency("테스트 기관")
                .target("대상").content("내용").applyStartDate(start).applyEndDate(end)
                .sourceUrl("https://example.com/" + title.hashCode()).today(Kst.today())
                .build());
    }

    private String signupAndLogin(String email) throws Exception {
        mvc.perform(post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email":"%s","password":"%s","nickname":"테스터","agreeTerms":true,"agreePrivacy":true}"""
                        .formatted(email, PASSWORD)))
                .andExpect(status().isCreated());
        MvcResult login = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk()).andReturn();
        return "Bearer " + data(login).path("accessToken").asString();
    }

    private JsonNode data(ResultActions actions) throws Exception {
        return data(actions.andReturn());
    }

    private JsonNode data(MvcResult result) throws Exception {
        return json.readTree(result.getResponse().getContentAsString()).path("data");
    }
}
