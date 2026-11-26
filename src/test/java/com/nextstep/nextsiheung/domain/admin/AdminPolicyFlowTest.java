package com.nextstep.nextsiheung.domain.admin;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nextstep.nextsiheung.domain.batch.entity.SyncLog;
import com.nextstep.nextsiheung.domain.batch.repository.SyncLogRepository;
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

// 관리자 정책 등록·수정·종료·확인 완료, 운영 지표, 수집 기록 (로컬 MySQL 필요, 테스트마다 롤백)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminPolicyFlowTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    JsonMapper json;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    EntityManager em;
    @Autowired
    SyncLogRepository syncLogRepository;
    @Value("${app.jwt.secret}")
    String secret;

    @Test
    void 정책을_등록하고_수정_종료_확인할_수_있다() throws Exception {
        String admin = adminToken();
        String body = """
                {"title":"관리자 테스트 정책","field":"EMPLOYMENT","agency":"시흥 청년스테이션",
                 "target":"시흥 청년","content":"내용","applyStartDate":"%s","applyEndDate":"%s",
                 "sourceUrl":"https://example.com/admin-test","lifeStages":["JOB_SEEKING"],"tags":["IT"]}"""
                .formatted(Kst.today().minusDays(1), Kst.today().plusDays(10));

        String created = mvc.perform(post("/api/v1/admin/policies").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.recruitStatus").value("OPEN"))
                .andExpect(jsonPath("$.data.lastVerifiedAt").value(Kst.today().toString()))
                .andReturn().getResponse().getContentAsString();
        long policyId = json.readTree(created).path("data").path("policyId").asLong();

        // 출처 URL은 필수
        mvc.perform(post("/api/v1/admin/policies").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\",\"field\":\"EMPLOYMENT\",\"agency\":\"y\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));

        // 신청 URL을 안 주면 출처 URL로
        mvc.perform(get("/api/v1/policies/" + policyId))
                .andExpect(jsonPath("$.data.applyUrl").value("https://example.com/admin-test"))
                .andExpect(jsonPath("$.data.source").value("ADMIN"));

        mvc.perform(put("/api/v1/admin/policies/" + policyId).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(body.replace("관리자 테스트 정책", "수정된 정책")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/admin/policies").header("Authorization", admin)
                        .param("source", "ADMIN").param("keyword", "수정된"))
                .andExpect(jsonPath("$.data.content[0].policyId").value(policyId))
                .andExpect(jsonPath("$.data.content[0].stale").value(false))
                .andExpect(jsonPath("$.data.content[0].roadmapItemCount").value(0));

        // 마지막 확인일이 30일 넘으면 stale, 확인 완료하면 오늘로
        em.flush();
        jdbc.update("update policy set last_verified_at = ? where policy_id = ?", Kst.today().minusDays(31),
                policyId);
        em.clear();
        mvc.perform(get("/api/v1/admin/policies").header("Authorization", admin).param("stale", "true"))
                .andExpect(jsonPath("$.data.content[*].policyId", hasItem((int) policyId)));
        mvc.perform(patch("/api/v1/admin/policies/" + policyId + "/verify").header("Authorization", admin))
                .andExpect(jsonPath("$.data.lastVerifiedAt").value(Kst.today().toString()));

        // 종료하면 기본 목록에서 빠지고 ENDED로 보인다
        mvc.perform(patch("/api/v1/admin/policies/" + policyId + "/end").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"사업 종료\"}"))
                .andExpect(jsonPath("$.data.recruitStatus").value("ENDED"));
        mvc.perform(get("/api/v1/policies").param("keyword", "수정된"))
                .andExpect(jsonPath("$.data.content[*].policyId", not(hasItem((int) policyId))));
        mvc.perform(get("/api/v1/policies").param("keyword", "수정된").param("recruitStatus", "ENDED"))
                .andExpect(jsonPath("$.data.content[0].policyId").value(policyId));

        mvc.perform(put("/api/v1/admin/policies/999999999").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POLICY_NOT_FOUND"));
    }

    @Test
    void 운영_지표와_수집_기록() throws Exception {
        String admin = adminToken();
        mvc.perform(get("/api/v1/admin/stats").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.period.to").value(Kst.today().toString()))
                .andExpect(jsonPath("$.data.period.from").value(Kst.today().minusDays(6).toString()));
        mvc.perform(get("/api/v1/admin/stats").header("Authorization", admin)
                        .param("from", "2026-11-10").param("to", "2026-11-01"))
                .andExpect(status().isBadRequest());

        SyncLog log = SyncLog.start(SyncLog.SOURCE_JOBABA, SyncLog.TRIGGER_SCHEDULED, Kst.now().plusYears(1));
        log.succeed(214, 12, 30, 0, Kst.now().plusYears(1));
        syncLogRepository.save(log);
        mvc.perform(get("/api/v1/admin/sync/logs").header("Authorization", admin).param("source", "JOBABA"))
                .andExpect(jsonPath("$.data.content[0].syncLogId").value(log.getId()))
                .andExpect(jsonPath("$.data.content[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].fetched").value(214));
        mvc.perform(get("/api/v1/admin/sync/logs").header("Authorization", admin).param("source", "SARAMIN"))
                .andExpect(status().isBadRequest());
    }

    private String adminToken() {
        // 테스트 트랜잭션이 커넥션 하나를 쓰므로 last_insert_id()로 방금 넣은 ID를 받는다
        jdbc.update("""
                insert into users (email, password, nickname, role, onboarding_completed, agree_terms, agree_privacy,
                                   created_at, updated_at)
                values ('admin-flow@test.kr', 'x', '관리자', 'ADMIN', 0, 1, 1, now(), now())""");
        Long adminId = jdbc.queryForObject("select last_insert_id()", Long.class);
        return "Bearer " + new JwtProvider(secret, 1800).createAccessToken(adminId, Role.ADMIN);
    }
}
