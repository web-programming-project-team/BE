package com.nextstep.nextsiheung.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nextstep.nextsiheung.domain.notification.service.DeadlineNotificationService;
import com.nextstep.nextsiheung.domain.policy.entity.Policy;
import com.nextstep.nextsiheung.domain.policy.repository.PolicyRepository;
import com.nextstep.nextsiheung.domain.user.entity.User;
import com.nextstep.nextsiheung.domain.user.repository.UserRepository;
import com.nextstep.nextsiheung.global.common.Kst;
import com.nextstep.nextsiheung.global.common.enums.InterestField;
import com.nextstep.nextsiheung.global.common.enums.LifeStage;
import com.nextstep.nextsiheung.global.common.enums.PolicySource;
import com.nextstep.nextsiheung.global.common.enums.Role;
import com.nextstep.nextsiheung.global.security.JwtProvider;
import java.time.LocalDate;
import java.util.List;
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
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

// 새 후기 알림(NEW_REVIEW)·채용 상세 합격 후기·마감 임박 알림(DEADLINE)·없는 주소 404 (로컬 MySQL 필요, 테스트마다 롤백)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReviewDeadlineNotificationFlowTest {

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
    @Autowired
    DeadlineNotificationService deadlineNotificationService;

    Long policyId;

    @BeforeEach
    void setUp() {
        policyId = policyRepository.save(Policy.builder()
                .source(PolicySource.ADMIN).title("청년 직무역량 아카데미").field(InterestField.EMPLOYMENT)
                .agency("시흥시").target("19~24세").content("직무 교육")
                .sourceUrl("https://example.kr").applyUrl("https://example.kr/apply")
                .today(LocalDate.of(2026, 11, 1)).build()).getId();
    }

    @Test
    void 후기를_쓰면_정책을_담았거나_공고를_저장한_사용자에게_알림이_간다() throws Exception {
        long jobId = createJob("IT");

        long roadmapUser = saveUser(Role.USER, LifeStage.JOB_SEEKING);
        insertRoadmapItem(roadmapUser, policyId, false);
        long bookmarkUser = saveUser(Role.USER, LifeStage.JOB_SEEKING);
        mvc.perform(post("/api/v1/jobs/" + jobId + "/bookmark").header("Authorization", bearer(bookmarkUser, Role.USER)))
                .andExpect(status().isCreated());
        // 정책도 담고 공고도 저장한 사용자는 1건만 받는다
        long bothUser = saveUser(Role.USER, LifeStage.JOB_SEEKING);
        insertRoadmapItem(bothUser, policyId, false);
        mvc.perform(post("/api/v1/jobs/" + jobId + "/bookmark").header("Authorization", bearer(bothUser, Role.USER)))
                .andExpect(status().isCreated());
        // 보관한(지난) 로드맵에만 있는 사용자는 받지 않는다
        long archivedUser = saveUser(Role.USER, LifeStage.JOB_SEEKING);
        insertRoadmapItem(archivedUser, policyId, true);
        // 작성자 본인도 정책을 담았지만 받지 않는다
        long author = saveUser(Role.USER, LifeStage.GRADUATING);
        insertRoadmapItem(author, policyId, false);

        long postId = writePost(author, """
                {"type":"PROGRAM_REVIEW","policyId":%d,"jobId":%d,"rating":5,"oneLiner":"실습 연계까지 돼서 좋았어요",
                 "prepPeriod":"MONTH_1","helpfulPoints":[]}""".formatted(policyId, jobId));

        assertThat(newReviews(roadmapUser)).containsExactly("청년 직무역량 아카데미에 새 후기가 올라왔어요");
        assertThat(newReviews(bookmarkUser)).containsExactly("(주)알림테스트에 새 후기가 올라왔어요");
        assertThat(newReviews(bothUser)).containsExactly("청년 직무역량 아카데미에 새 후기가 올라왔어요");
        assertThat(newReviews(archivedUser)).isEmpty();
        assertThat(newReviews(author)).isEmpty();

        // 알림을 누르면 게시글로 이동
        mvc.perform(get("/api/v1/notifications").param("type", "NEW_REVIEW")
                        .header("Authorization", bearer(roadmapUser, Role.USER)))
                .andExpect(jsonPath("$.data.content[0].body").value("실습 연계까지 돼서 좋았어요"))
                .andExpect(jsonPath("$.data.content[0].link.targetType").value("POST"))
                .andExpect(jsonPath("$.data.content[0].link.targetId").value(postId));
    }

    @Test
    void 채용_상세에_이_공고나_같은_직무의_합격_후기가_최신_3개까지_나온다() throws Exception {
        long itJob = createJob("IT");
        long officeJob = createJob("OFFICE");
        long author = saveUser(Role.USER, LifeStage.JOB_SEEKING);

        // 같은 직무(IT) 합격 후기 4개 중 최신 3개, 다른 직무·참여 후기는 빠진다
        for (int i = 1; i <= 4; i++) {
            writePost(author, passReview("IT", "IT 합격 후기 " + i));
        }
        writePost(author, passReview("OFFICE", "사무 합격 후기"));
        writePost(author, """
                {"type":"PROGRAM_REVIEW","policyId":%d,"jobCategory":"IT","rating":5,"oneLiner":"참여 후기",
                 "prepPeriod":"MONTH_1"}""".formatted(policyId));
        // 다른 직무 태그여도 이 공고에 직접 단 합격 후기는 나온다
        long onOffice = writePost(author, """
                {"type":"PASS_REVIEW","jobId":%d,"jobCategory":"DESIGN","rating":4,"oneLiner":"이 공고 합격",
                 "prepPeriod":"MONTH_3"}""".formatted(officeJob));

        mvc.perform(get("/api/v1/jobs/" + itJob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviews.length()").value(3))
                .andExpect(jsonPath("$.data.reviews[0].oneLiner").value("IT 합격 후기 4"))
                .andExpect(jsonPath("$.data.reviews[0].type").value("PASS_REVIEW"))
                .andExpect(jsonPath("$.data.reviews[0].jobCategory").value("IT"))
                .andExpect(jsonPath("$.data.reviews[2].oneLiner").value("IT 합격 후기 2"));

        // 숨긴 글은 빠진다
        long admin = saveUser(Role.ADMIN);
        mvc.perform(patch("/api/v1/admin/posts/" + onOffice + "/visibility")
                        .header("Authorization", bearer(admin, Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"hidden\":true,\"reason\":\"테스트\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/jobs/" + officeJob))
                .andExpect(jsonPath("$.data.reviews.length()").value(1))
                .andExpect(jsonPath("$.data.reviews[0].oneLiner").value("사무 합격 후기"));
    }

    @Test
    void 마감_D7_D3_Dday_일정만_하루에_한_번_알린다() throws Exception {
        long userId = saveUser(Role.USER, LifeStage.JOB_SEEKING);
        LocalDate today = Kst.today();
        long applied = insertRoadmapItem(userId, policyId, false);
        jdbc.update("update roadmap_item set status = 'APPLIED' where item_id = ?", applied);
        long checking = insertRoadmapItem(userId, policyId, false);

        insertEvent(userId, "CUSTOM", today, "면접", "CUSTOM", null, null);
        insertEvent(userId, "CUSTOM", today.plusDays(3), "서류 마감", "CUSTOM", null, null);
        insertEvent(userId, "APPLY_END", today.plusDays(7), "아카데미 접수 마감", "POLICY", policyId, checking);
        // 알리지 않는 것: 다른 날짜, 접수 시작, 이미 신청한 항목의 접수 마감
        insertEvent(userId, "CUSTOM", today.plusDays(1), "내일 일정", "CUSTOM", null, null);
        insertEvent(userId, "APPLY_START", today.plusDays(3), "아카데미 접수 시작", "POLICY", policyId, checking);
        insertEvent(userId, "APPLY_END", today.plusDays(3), "신청한 정책 접수 마감", "POLICY", policyId, applied);

        deadlineNotificationService.send();
        deadlineNotificationService.send();

        assertThat(titles(userId, "DEADLINE"))
                .containsExactlyInAnyOrder("면접 D-day", "서류 마감 D-3", "아카데미 접수 마감 D-7");
        assertThat(jdbc.queryForList("""
                select target_type from notification where user_id = ? and title = '아카데미 접수 마감 D-7'""",
                String.class, userId)).containsExactly("POLICY");
        assertThat(jdbc.queryForList("""
                select concat(target_type, ':', coalesce(target_id, 'null'), ':', body) from notification
                where user_id = ? and title = '면접 D-day'""", String.class, userId))
                .containsExactly("CALENDAR:null:오늘이 마감일이에요.");
    }

    @Test
    void 없는_주소는_404_지원하지_않는_메서드는_405() throws Exception {
        String user = bearer(saveUser(Role.USER, LifeStage.JOB_SEEKING), Role.USER);
        mvc.perform(get("/api/v1/nope").header("Authorization", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
        mvc.perform(patch("/api/v1/notifications/unread-count").header("Authorization", user))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    private static String passReview(String category, String oneLiner) {
        return """
                {"type":"PASS_REVIEW","jobCategory":"%s","rating":4,"oneLiner":"%s","prepPeriod":"MONTH_3"}"""
                .formatted(category, oneLiner);
    }

    private List<String> newReviews(long userId) {
        return titles(userId, "NEW_REVIEW");
    }

    private List<String> titles(long userId, String type) {
        return jdbc.queryForList("select title from notification where user_id = ? and type = ? order by notification_id",
                String.class, userId, type);
    }

    private long writePost(long userId, String body) throws Exception {
        String res = mvc.perform(post("/api/v1/posts").header("Authorization", bearer(userId, Role.USER))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        // 같은 초에 쓴 글도 최신순이 정해지도록 작성 시각을 조금씩 띄운다
        long postId = json.readTree(res).path("data").path("postId").asLong();
        jdbc.update("update post set created_at = date_add(now(), interval ? second) where post_id = ?", postId, postId);
        return postId;
    }

    private long createJob(String category) throws Exception {
        long admin = saveUser(Role.ADMIN);
        String res = mvc.perform(post("/api/v1/admin/jobs").header("Authorization", bearer(admin, Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"company":"(주)알림테스트","title":"신입 채용","region":"SIHEUNG","address":"경기 시흥시",
                                 "education":"COLLEGE","careerLevel":"NEW","employmentType":"FULL_TIME",
                                 "jobCategory":"%s","salary":"회사 내규","deadline":"%s",
                                 "originalUrl":"https://example.com/job"}""".formatted(category, Kst.today().plusDays(20))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(res).path("data").path("jobId").asLong();
    }

    // 로드맵 → 단계 → 항목을 바로 넣는다 (템플릿 추천 흐름은 RoadmapFlowTest가 본다)
    private long insertRoadmapItem(long userId, long policyId, boolean archived) {
        jdbc.update("""
                insert into roadmap (user_id, template_name, archived, archived_at, created_at)
                values (?, '테스트 로드맵', ?, ?, now())""", userId, archived, archived ? Kst.now() : null);
        long roadmapId = jdbc.queryForObject("select last_insert_id()", Long.class);
        jdbc.update("insert into roadmap_step (roadmap_id, step_order, title) values (?, 1, '1단계')", roadmapId);
        long stepId = jdbc.queryForObject("select last_insert_id()", Long.class);
        jdbc.update("""
                insert into roadmap_item (roadmap_step_id, policy_id, status, user_added, created_at, updated_at)
                values (?, ?, 'TO_CHECK', 0, now(), now())""", stepId, policyId);
        return jdbc.queryForObject("select last_insert_id()", Long.class);
    }

    private void insertEvent(long userId, String type, LocalDate date, String title, String sourceType,
                             Long policyId, Long itemId) {
        jdbc.update("""
                insert into calendar_event (user_id, type, event_date, title, source_type, policy_id, item_id, created_at)
                values (?, ?, ?, ?, ?, ?, ?, now())""", userId, type, date, title, sourceType, policyId, itemId);
    }

    private long saveUser(Role role, LifeStage... stages) {
        User user = User.builder()
                .email(UUID.randomUUID().toString().substring(0, 12) + "@noti.test")
                .password("x").nickname("tester").role(role).agreeTerms(true).agreePrivacy(true)
                .build();
        user.changeStatus(Set.of(stages), Set.of());
        return userRepository.saveAndFlush(user).getId();
    }

    private String bearer(long userId, Role role) {
        return "Bearer " + jwtProvider.createAccessToken(userId, role);
    }
}
