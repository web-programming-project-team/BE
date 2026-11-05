package com.nextstep.nextsiheung.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

// 회원가입 → 로그인 → 내 정보 → 재발급 → 로그아웃 → 탈퇴 흐름과 인증 실패 응답 (로컬 MySQL 필요, 테스트마다 롤백)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthFlowTest {

    private static final String PASSWORD = "next1234!";

    @Autowired
    MockMvc mvc;
    @Autowired
    JsonMapper json;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    EntityManager em;
    @Value("${app.jwt.secret}")
    String secret;

    @Test
    void 가입하고_로그인하면_내_정보를_볼_수_있다() throws Exception {
        signup("Flow@Test.kr")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("flow@test.kr"))
                .andExpect(jsonPath("$.data.onboardingCompleted").value(false));

        JsonNode login = login("flow@test.kr");
        assertThat(login.path("expiresIn").asLong()).isEqualTo(1800);
        assertThat(login.path("user").path("role").asString()).isEqualTo("USER");

        mvc.perform(get("/api/v1/users/me").header("Authorization", bearer(login.path("accessToken").asString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("flow@test.kr"));

        Long userId = login.path("user").path("userId").asLong();
        assertThat(jdbc.queryForObject("select count(*) from user_visit where user_id = ?", Integer.class, userId)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select last_login_at is not null from users where user_id = ?", Boolean.class, userId)).isTrue();
    }

    @Test
    void 가입_입력값과_중복_이메일을_막는다() throws Exception {
        signup("dup@test.kr").andExpect(status().isCreated());
        signup("DUP@test.kr")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"));

        mvc.perform(post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"weak@test.kr","password":"1234","nickname":"A","agreeTerms":true,"agreePrivacy":true}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.data.errors[0].field").value("password"));
    }

    @Test
    void 비밀번호가_틀리면_LOGIN_FAILED() throws Exception {
        signup("wrong@test.kr");
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"wrong@test.kr\",\"password\":\"nope1234!\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("LOGIN_FAILED"));
    }

    @Test
    void 토큰이_없거나_틀리거나_만료되면_401과_코드를_준다() throws Exception {
        mvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mvc.perform(get("/api/v1/users/me").header("Authorization", bearer("not-a-jwt")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mvc.perform(get("/api/v1/users/me").header("Authorization", bearer(expiredToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_EXPIRED"));
    }

    @Test
    void 로그인_선택_API와_재발급은_만료된_토큰이_붙어도_통과한다() throws Exception {
        // GET /jobs/{jobId}는 명세 권한 U? → 인증 단계에서 막히지 않고 컨트롤러까지 간다 (아직 501)
        mvc.perform(get("/api/v1/jobs/1")).andExpect(status().isNotImplemented());
        mvc.perform(get("/api/v1/jobs/1").header("Authorization", bearer(expiredToken())))
                .andExpect(status().isNotImplemented());
        mvc.perform(get("/api/v1/jobs/recommended")).andExpect(status().isUnauthorized());

        signup("refresh-expired@test.kr");
        String refreshToken = login("refresh-expired@test.kr").path("refreshToken").asString();
        mvc.perform(post("/api/v1/auth/refresh").header("Authorization", bearer(expiredToken()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void 재발급하면_이전_Refresh_토큰은_다시_쓸_수_없다() throws Exception {
        signup("rotate@test.kr");
        String first = login("rotate@test.kr").path("refreshToken").asString();

        JsonNode rotated = data(mvc.perform(refresh(first)).andExpect(status().isOk()).andReturn());
        assertThat(rotated.path("refreshToken").asString()).isNotEqualTo(first);
        assertThat(rotated.path("accessToken").asString()).isNotBlank();

        mvc.perform(refresh(first))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void 로그아웃하면_Refresh_토큰이_지워진다() throws Exception {
        signup("logout@test.kr");
        JsonNode login = login("logout@test.kr");

        mvc.perform(post("/api/v1/auth/logout")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/logout").header("Authorization", bearer(login.path("accessToken").asString())))
                .andExpect(status().isNoContent());
        mvc.perform(refresh(login.path("refreshToken").asString())).andExpect(status().isUnauthorized());
    }

    @Test
    void 일반_사용자는_관리자_API에서_403() throws Exception {
        signup("user@test.kr");
        String access = login("user@test.kr").path("accessToken").asString();
        mvc.perform(get("/api/v1/admin/stats").header("Authorization", bearer(access)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void 탈퇴하면_계정과_토큰_접속기록이_함께_지워진다() throws Exception {
        signup("bye@test.kr");
        JsonNode login = login("bye@test.kr");
        String access = login.path("accessToken").asString();
        Long userId = login.path("user").path("userId").asLong();

        mvc.perform(delete("/api/v1/users/me").header("Authorization", bearer(access)))
                .andExpect(status().isNoContent());

        em.flush(); // 테스트가 한 트랜잭션이라 DELETE를 DB로 보낸 뒤 확인한다
        assertThat(jdbc.queryForObject("select count(*) from users where user_id = ?", Integer.class, userId)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from refresh_token where user_id = ?", Integer.class, userId)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from user_visit where user_id = ?", Integer.class, userId)).isZero();
        mvc.perform(get("/api/v1/users/me").header("Authorization", bearer(access)))
                .andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.ResultActions signup(String email) throws Exception {
        return mvc.perform(post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"%s","password":"%s","nickname":"테스터","birthYear":2003,"agreeTerms":true,"agreePrivacy":true}"""
                .formatted(email, PASSWORD)));
    }

    private JsonNode login(String email) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return data(result);
    }

    private org.springframework.test.web.servlet.RequestBuilder refresh(String refreshToken) {
        return post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}");
    }

    private JsonNode data(MvcResult result) throws Exception {
        return json.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private String expiredToken() {
        return new JwtProvider(secret, -60).createAccessToken(1L, Role.USER);
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
