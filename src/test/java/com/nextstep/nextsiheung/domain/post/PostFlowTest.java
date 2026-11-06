package com.nextstep.nextsiheung.domain.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nextstep.nextsiheung.domain.policy.entity.Policy;
import com.nextstep.nextsiheung.domain.policy.repository.PolicyRepository;
import com.nextstep.nextsiheung.domain.user.entity.User;
import com.nextstep.nextsiheung.domain.user.repository.UserRepository;
import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.LifeStage;
import com.nextstep.nextsiheung.global.common.enums.PolicySource;
import com.nextstep.nextsiheung.global.common.enums.Role;
import com.nextstep.nextsiheung.global.security.JwtProvider;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

// 커뮤니티: 작성 → 목록·상세 → 좋아요 → 수정·삭제 → 신고 5건 자동 숨김 → 관리자 검토 (로컬 MySQL 필요, 테스트마다 롤백)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostFlowTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    JsonMapper json;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    JwtProvider jwtProvider;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PolicyRepository policyRepository;

    Long policyId;
    String author;

    @BeforeEach
    void setUp() {
        policyId = policyRepository.save(Policy.builder()
                .source(PolicySource.ADMIN).title("청년 직무역량 아카데미").field(InterestField.EMPLOYMENT)
                .agency("시흥시").target("19~24세").content("직무 교육")
                .sourceUrl("https://example.kr").applyUrl("https://example.kr/apply")
                .today(LocalDate.of(2026, 11, 1)).build()).getId();
        author = token(LifeStage.GRADUATING);
    }

    @Test
    void 참여_후기를_쓰면_목록과_상세에_나온다() throws Exception {
        Long postId = createReview(author, "실습 연계까지 돼서 좋았어요");

        mvc.perform(get("/api/v1/posts").param("type", "PROGRAM_REVIEW").param("policyId", policyId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].postId").value(postId))
                .andExpect(jsonPath("$.data.content[0].policy.title").value("청년 직무역량 아카데미"))
                .andExpect(jsonPath("$.data.content[0].helpfulPoints[0]").value("실무 과제"))
                .andExpect(jsonPath("$.data.content[0].authorStage").value("GRADUATING"))
                .andExpect(jsonPath("$.data.content[0].likeCount").value(0))
                .andExpect(jsonPath("$.data.content[0].liked").value(false));

        mvc.perform(get("/api/v1/posts/{id}", postId).header("Authorization", bearer(author)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("8주 동안 배웠어요"))
                .andExpect(jsonPath("$.data.mine").value(true));
        mvc.perform(get("/api/v1/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mine").value(false));

        mvc.perform(get("/api/v1/policies/{id}/reviews", policyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].postId").value(postId));
        mvc.perform(get("/api/v1/policies/{id}/reviews", 999_999_999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POLICY_NOT_FOUND"));
    }

    @Test
    void 작성_입력값을_검사한다() throws Exception {
        // 참여 후기인데 정책 없음
        write(author, """
                {"type":"PROGRAM_REVIEW","rating":5,"oneLiner":"좋아요","prepPeriod":"MONTH_1"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        // 없는 정책
        write(author, """
                {"type":"PROGRAM_REVIEW","policyId":999999999,"rating":5,"oneLiner":"좋아요","prepPeriod":"MONTH_1"}""")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POLICY_NOT_FOUND"));
        // 연락처
        write(author, """
                {"type":"PROGRAM_REVIEW","policyId":%d,"rating":5,"oneLiner":"궁금하면 010-1234-5678","prepPeriod":"MONTH_1"}"""
                .formatted(policyId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CONTACT_INFO_DETECTED"));
        // 포트폴리오는 링크 필수, 별점은 생략 가능
        write(author, """
                {"type":"PORTFOLIO","oneLiner":"첫 포트폴리오","prepPeriod":"MONTH_3"}""")
                .andExpect(status().isBadRequest());
        write(author, """
                {"type":"PORTFOLIO","oneLiner":"첫 포트폴리오","prepPeriod":"MONTH_3","portfolioUrl":"https://notion.so/me","jobCategory":"IT"}""")
                .andExpect(status().isCreated());
        // 상황 등록 전
        write(token(), """
                {"type":"PASS_REVIEW","jobCategory":"OFFICE","rating":4,"oneLiner":"합격했어요","prepPeriod":"MONTH_1"}""")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ONBOARDING_REQUIRED"));
        // 비로그인
        mvc.perform(post("/api/v1/posts").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 같은_상황_필터는_작성_당시나_바뀐_상황으로_고른다() throws Exception {
        createReview(author, "졸업 예정자 후기");
        createReview(token(LifeStage.MILITARY), "군 복무 중 후기");

        mvc.perform(get("/api/v1/posts").param("policyId", policyId.toString()).param("sameStage", "true")
                        .header("Authorization", bearer(token(LifeStage.GRADUATING, LifeStage.JOB_SEEKING))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].oneLiner").value("졸업 예정자 후기"));

        mvc.perform(get("/api/v1/posts").param("sameStage", "true"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 정렬은_최신순이_기본이고_도움돼요_많은순을_고를_수_있다() throws Exception {
        Long older = createReview(author, "좋아요 많은 예전 글");
        Long newer = createReview(author, "좋아요 없는 새 글");
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/api/v1/posts/{id}/likes", older).header("Authorization", bearer(token(LifeStage.ENROLLED))))
                    .andExpect(status().isCreated());
        }

        mvc.perform(get("/api/v1/posts").param("policyId", policyId.toString()))
                .andExpect(jsonPath("$.data.content[0].postId").value(newer))
                .andExpect(jsonPath("$.data.content[1].postId").value(older));
        mvc.perform(get("/api/v1/posts").param("policyId", policyId.toString()).param("sort", "likes"))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].postId").value(older))
                .andExpect(jsonPath("$.data.content[0].likeCount").value(2))
                .andExpect(jsonPath("$.data.content[1].postId").value(newer));
        mvc.perform(get("/api/v1/posts").param("sort", "popular"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void 좋아요는_한_번만_누를_수_있고_취소할_수_있다() throws Exception {
        Long postId = createReview(author, "좋아요 테스트");
        String fan = token(LifeStage.ENROLLED);

        mvc.perform(post("/api/v1/posts/{id}/likes", postId).header("Authorization", bearer(fan)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.liked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(1));
        mvc.perform(post("/api/v1/posts/{id}/likes", postId).header("Authorization", bearer(fan)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ALREADY_EXISTS"));

        mvc.perform(get("/api/v1/posts").param("policyId", policyId.toString()).header("Authorization", bearer(fan)))
                .andExpect(jsonPath("$.data.content[0].likeCount").value(1))
                .andExpect(jsonPath("$.data.content[0].liked").value(true));

        for (int i = 0; i < 2; i++) { // 두 번째 취소도 같은 응답
            mvc.perform(delete("/api/v1/posts/{id}/likes", postId).header("Authorization", bearer(fan)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.liked").value(false))
                    .andExpect(jsonPath("$.data.likeCount").value(0));
        }
        mvc.perform(post("/api/v1/posts/{id}/likes", 999_999_999L).header("Authorization", bearer(fan)))
                .andExpect(status().isNotFound());
    }

    @Test
    void 수정과_삭제는_작성자만_할_수_있다() throws Exception {
        Long postId = createReview(author, "처음 한 줄 평");
        String other = token(LifeStage.ENROLLED);

        mvc.perform(patch("/api/v1/posts/{id}", postId).header("Authorization", bearer(other))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"oneLiner":"남의 글"}"""))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/v1/posts/{id}", postId).header("Authorization", bearer(author))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"oneLiner":"바꾼 한 줄 평","content":"","helpfulPoints":["멘토 피드백"]}"""))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/posts/{id}", postId))
                .andExpect(jsonPath("$.data.oneLiner").value("바꾼 한 줄 평"))
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.rating").value(5))
                .andExpect(jsonPath("$.data.helpfulPoints.length()").value(1));

        mvc.perform(delete("/api/v1/posts/{id}", postId).header("Authorization", bearer(other)))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/posts/{id}", postId).header("Authorization", bearer(author)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/posts/{id}", postId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"));
        mvc.perform(get("/api/v1/users/me/posts").header("Authorization", bearer(author)))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void 신고가_5건_쌓이면_숨기고_관리자가_복구한다() throws Exception {
        Long postId = createReview(author, "신고될 글");

        for (int i = 1; i <= 5; i++) {
            mvc.perform(post("/api/v1/posts/{id}/reports", postId).header("Authorization", bearer(token(LifeStage.ENROLLED)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(i == 5 ? "{\"reason\":\"SPAM\"}" : "{\"reason\":\"PERSONAL_INFO\",\"detail\":\"실명\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.autoHidden").value(i == 5));
        }

        // 목록·상세에서 빠지고, 작성자에게는 보인다
        mvc.perform(get("/api/v1/posts").param("policyId", policyId.toString()))
                .andExpect(jsonPath("$.data.totalElements").value(0));
        mvc.perform(get("/api/v1/posts/{id}", postId)).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/posts/{id}", postId).header("Authorization", bearer(author)))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/users/me/posts").header("Authorization", bearer(author)))
                .andExpect(jsonPath("$.data.content[0].hidden").value(true));

        String admin = adminToken();
        mvc.perform(get("/api/v1/admin/reports").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].postId").value(postId))
                .andExpect(jsonPath("$.data.content[0].reportCount").value(5))
                .andExpect(jsonPath("$.data.content[0].reasons.PERSONAL_INFO").value(4))
                .andExpect(jsonPath("$.data.content[0].reasons.SPAM").value(1))
                .andExpect(jsonPath("$.data.content[0].autoHidden").value(true))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING"));

        mvc.perform(patch("/api/v1/admin/posts/{id}/visibility", postId).header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"hidden\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hidden").value(false));

        mvc.perform(get("/api/v1/posts/{id}", postId)).andExpect(status().isOk());
        assertThat(jdbc.queryForObject("select count(*) from post_report where post_id = ? and status = 'RESOLVED'",
                Integer.class, postId)).isEqualTo(5);
        mvc.perform(get("/api/v1/admin/reports").param("status", "RESOLVED").header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$.data.content[0].postId").value(postId))
                .andExpect(jsonPath("$.data.content[0].autoHidden").value(false));
        mvc.perform(get("/api/v1/admin/reports").param("status", "NOPE").header("Authorization", bearer(admin)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 같은_글은_한_번만_신고할_수_있다() throws Exception {
        Long postId = createReview(author, "중복 신고");
        String reporter = token(LifeStage.ENROLLED);
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/api/v1/posts/{id}/reports", postId).header("Authorization", bearer(reporter))
                            .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"ABUSE\"}"))
                    .andExpect(i == 0 ? status().isCreated() : status().isConflict());
        }
    }

    private Long createReview(String token, String oneLiner) throws Exception {
        String body = write(token, """
                {"type":"PROGRAM_REVIEW","policyId":%d,"jobCategory":"IT","rating":5,"oneLiner":"%s",
                 "prepPeriod":"MONTH_1","helpfulPoints":["실무 과제"," 실무 과제 ",""],"content":"8주 동안 배웠어요"}"""
                .formatted(policyId, oneLiner))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).path("data").path("postId").asLong();
    }

    private ResultActions write(String token, String body) throws Exception {
        return mvc.perform(post("/api/v1/posts").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private String token(LifeStage... stages) {
        return jwtProvider.createAccessToken(saveUser(Role.USER, stages).getId(), Role.USER);
    }

    private String adminToken() {
        return jwtProvider.createAccessToken(saveUser(Role.ADMIN).getId(), Role.ADMIN);
    }

    private User saveUser(Role role, LifeStage... stages) {
        User user = User.builder()
                .email(UUID.randomUUID().toString().substring(0, 12) + "@post.test")
                .password("x").nickname("tester").role(role).agreeTerms(true).agreePrivacy(true)
                .build();
        user.changeStatus(Set.of(stages), Set.of());
        return userRepository.saveAndFlush(user);
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
