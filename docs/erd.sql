-- NEXT 시흥 ERD (ERDCloud 가져오기용, MySQL)
-- 기준: API 명세 v1.1, 공통 enum 15개. enum 컬럼은 VARCHAR(30) + @Enumerated(EnumType.STRING)
-- 주민등록번호·소득 정보는 어떤 테이블에도 두지 않는다. UNIQUE 조건은 COMMENT에 표시

-- ───────── 회원 ─────────
CREATE TABLE `users` (
  `user_id`              BIGINT       NOT NULL COMMENT '회원 ID',
  `email`                VARCHAR(100) NOT NULL COMMENT '이메일 (UNIQUE)',
  `password`             VARCHAR(100) NOT NULL COMMENT 'BCrypt 해시',
  `nickname`             VARCHAR(20)  NOT NULL COMMENT '닉네임',
  `birth_year`           SMALLINT     NULL     COMMENT '출생연도 (선택)',
  `role`                 VARCHAR(30)  NOT NULL DEFAULT 'USER' COMMENT 'Role: USER, ADMIN',
  `onboarding_completed` TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '상태 등록 완료 여부',
  `agree_terms`          TINYINT(1)   NOT NULL COMMENT '이용약관 동의',
  `agree_privacy`        TINYINT(1)   NOT NULL COMMENT '개인정보 수집 동의',
  `last_login_at`        DATETIME     NULL     COMMENT '마지막 접속 (새 공고 isNew 기준)',
  `created_at`           DATETIME     NOT NULL COMMENT '가입일',
  `updated_at`           DATETIME     NOT NULL COMMENT '수정일'
) COMMENT = '회원';

CREATE TABLE `refresh_token` (
  `token_id`   BIGINT       NOT NULL COMMENT '토큰 ID',
  `user_id`    BIGINT       NOT NULL COMMENT '회원 ID',
  `token`      VARCHAR(500) NOT NULL COMMENT 'Refresh 토큰 (UNIQUE)',
  `expires_at` DATETIME     NOT NULL COMMENT '만료 일시 (14일)',
  `created_at` DATETIME     NOT NULL COMMENT '발급일'
) COMMENT = 'Refresh 토큰';

CREATE TABLE `user_visit` (
  `user_id`    BIGINT NOT NULL COMMENT '회원 ID',
  `visit_date` DATE   NOT NULL COMMENT '접속일'
) COMMENT = '일별 접속 기록 (7일 재방문율 집계)';

CREATE TABLE `user_life_stage` (
  `user_id`    BIGINT      NOT NULL COMMENT '회원 ID',
  `life_stage` VARCHAR(30) NOT NULL COMMENT 'LifeStage: ENROLLED, GRADUATING, ON_LEAVE, MILITARY, JOB_SEEKING, FIRST_JOB, INDEPENDENCE'
) COMMENT = '회원 현재 상황 (복수)';

CREATE TABLE `user_interest` (
  `user_id`        BIGINT      NOT NULL COMMENT '회원 ID',
  `interest_field` VARCHAR(30) NOT NULL COMMENT 'InterestField: EMPLOYMENT, EDUCATION, HOUSING, FINANCE, COUNSELING, CULTURE_LIFE'
) COMMENT = '회원 관심분야 (복수)';

CREATE TABLE `job_preference` (
  `user_id`      BIGINT      NOT NULL COMMENT '회원 ID',
  `education`    VARCHAR(30) NULL     COMMENT 'Education: HIGH_SCHOOL, COLLEGE, UNIVERSITY, ANY',
  `career_level` VARCHAR(30) NULL     COMMENT '경력: NEW(신입)',
  `updated_at`   DATETIME    NOT NULL COMMENT '수정일'
) COMMENT = '희망 채용 조건';

CREATE TABLE `job_preference_region` (
  `user_id` BIGINT      NOT NULL COMMENT '회원 ID',
  `region`  VARCHAR(30) NOT NULL COMMENT 'Region: SIHEUNG, ANSAN, INCHEON, BUCHEON, GWANGMYEONG'
) COMMENT = '희망 지역 (복수)';

CREATE TABLE `job_preference_category` (
  `user_id`      BIGINT      NOT NULL COMMENT '회원 ID',
  `job_category` VARCHAR(30) NOT NULL COMMENT 'JobCategory: OFFICE, IT, MANUFACTURING, SERVICE, DESIGN, ETC'
) COMMENT = '희망 직무 (복수)';

CREATE TABLE `job_preference_employment_type` (
  `user_id`         BIGINT      NOT NULL COMMENT '회원 ID',
  `employment_type` VARCHAR(30) NOT NULL COMMENT 'EmploymentType: FULL_TIME, CONTRACT, INTERN, PART_TIME'
) COMMENT = '희망 고용형태 (복수)';

-- ───────── 정책·프로그램 ─────────
CREATE TABLE `policy` (
  `policy_id`        BIGINT       NOT NULL COMMENT '정책 ID',
  `source`           VARCHAR(30)  NOT NULL COMMENT 'PolicySource: ONTONG, ADMIN',
  `external_id`      VARCHAR(50)  NULL     COMMENT '온통청년 정책 ID (UNIQUE, ADMIN 등록이면 NULL)',
  `title`            VARCHAR(200) NOT NULL COMMENT '정책명',
  `field`            VARCHAR(30)  NOT NULL COMMENT 'InterestField',
  `agency`           VARCHAR(100) NOT NULL COMMENT '운영기관',
  `target`           VARCHAR(500) NOT NULL COMMENT '지원대상',
  `content`          TEXT         NOT NULL COMMENT '지원내용',
  `contact`          VARCHAR(200) NULL     COMMENT '문의처',
  `apply_start_date` DATE         NULL     COMMENT '접수 시작일',
  `apply_end_date`   DATE         NULL     COMMENT '접수 마감일 (상시면 NULL)',
  `recruit_status`   VARCHAR(30)  NOT NULL COMMENT 'RecruitStatus: UPCOMING, OPEN, CLOSED, ENDED',
  `source_url`       VARCHAR(500) NOT NULL COMMENT '공식 출처 URL',
  `apply_url`        VARCHAR(500) NOT NULL COMMENT '공식 신청 URL (서비스 안 신청 없음)',
  `last_verified_at` DATE         NOT NULL COMMENT '마지막 정보 확인일 (30일 경과 시 확인 필요)',
  `end_reason`       VARCHAR(200) NULL     COMMENT '종료 사유',
  `created_by`       BIGINT       NULL     COMMENT '등록한 관리자 ID (ONTONG이면 NULL)',
  `created_at`       DATETIME     NOT NULL COMMENT '등록일',
  `updated_at`       DATETIME     NOT NULL COMMENT '수정일'
) COMMENT = '정책·프로그램';

CREATE TABLE `policy_life_stage` (
  `policy_id`  BIGINT      NOT NULL COMMENT '정책 ID',
  `life_stage` VARCHAR(30) NOT NULL COMMENT 'LifeStage'
) COMMENT = '정책 대상 상황 (복수)';

CREATE TABLE `policy_tag` (
  `policy_id` BIGINT      NOT NULL COMMENT '정책 ID',
  `tag`       VARCHAR(30) NOT NULL COMMENT '태그 (공고의 도움 되는 지원 매칭용)'
) COMMENT = '정책 태그 (복수)';

-- ───────── 로드맵 템플릿 (관리자) ─────────
CREATE TABLE `roadmap_template` (
  `template_id` BIGINT       NOT NULL COMMENT '템플릿 ID',
  `name`        VARCHAR(100) NOT NULL COMMENT '템플릿 이름',
  `created_at`  DATETIME     NOT NULL COMMENT '등록일',
  `updated_at`  DATETIME     NOT NULL COMMENT '수정일'
) COMMENT = '로드맵 템플릿';

CREATE TABLE `template_life_stage` (
  `template_id` BIGINT      NOT NULL COMMENT '템플릿 ID',
  `life_stage`  VARCHAR(30) NOT NULL COMMENT 'LifeStage'
) COMMENT = '템플릿 대상 상황 (복수)';

CREATE TABLE `template_interest` (
  `template_id`    BIGINT      NOT NULL COMMENT '템플릿 ID',
  `interest_field` VARCHAR(30) NOT NULL COMMENT 'InterestField'
) COMMENT = '템플릿 대상 관심분야 (복수)';

CREATE TABLE `template_step` (
  `template_step_id` BIGINT       NOT NULL COMMENT '템플릿 단계 ID',
  `template_id`      BIGINT       NOT NULL COMMENT '템플릿 ID',
  `step_order`       INT          NOT NULL COMMENT '단계 순서',
  `title`            VARCHAR(100) NOT NULL COMMENT '단계 이름'
) COMMENT = '템플릿 단계';

CREATE TABLE `template_step_policy` (
  `template_step_id` BIGINT NOT NULL COMMENT '템플릿 단계 ID',
  `policy_id`        BIGINT NOT NULL COMMENT '정책 ID'
) COMMENT = '템플릿 단계별 정책';

-- ───────── 내 로드맵·진행상태 ─────────
CREATE TABLE `roadmap` (
  `roadmap_id`    BIGINT       NOT NULL COMMENT '로드맵 ID',
  `user_id`       BIGINT       NOT NULL COMMENT '회원 ID',
  `template_id`   BIGINT       NULL     COMMENT '만든 템플릿 ID',
  `template_name` VARCHAR(100) NOT NULL COMMENT '생성 당시 템플릿 이름',
  `archived`      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '상황 변경으로 보관됨 (지난 로드맵)',
  `archived_at`   DATETIME     NULL     COMMENT '보관 일시',
  `created_at`    DATETIME     NOT NULL COMMENT '생성일'
) COMMENT = '내 로드맵';

CREATE TABLE `roadmap_step` (
  `roadmap_step_id` BIGINT       NOT NULL COMMENT '로드맵 단계 ID',
  `roadmap_id`      BIGINT       NOT NULL COMMENT '로드맵 ID',
  `step_order`      INT          NOT NULL COMMENT '단계 순서',
  `title`           VARCHAR(100) NOT NULL COMMENT '단계 이름'
) COMMENT = '로드맵 단계';

CREATE TABLE `roadmap_item` (
  `item_id`         BIGINT      NOT NULL COMMENT '로드맵 항목 ID',
  `roadmap_step_id` BIGINT      NOT NULL COMMENT '로드맵 단계 ID',
  `policy_id`       BIGINT      NOT NULL COMMENT '정책 ID',
  `status`          VARCHAR(30) NOT NULL DEFAULT 'TO_CHECK' COMMENT 'ProgressStatus: TO_CHECK, INTERESTED, PLANNED, APPLIED, IN_PROGRESS, COMPLETED',
  `user_added`      TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '사용자가 직접 추가 (템플릿 항목이면 0)',
  `created_at`      DATETIME    NOT NULL COMMENT '추가일',
  `updated_at`      DATETIME    NOT NULL COMMENT '수정일'
) COMMENT = '로드맵 항목';

CREATE TABLE `roadmap_item_history` (
  `history_id`  BIGINT      NOT NULL COMMENT '기록 ID',
  `item_id`     BIGINT      NOT NULL COMMENT '로드맵 항목 ID',
  `from_status` VARCHAR(30) NULL     COMMENT '이전 ProgressStatus',
  `to_status`   VARCHAR(30) NOT NULL COMMENT '바뀐 ProgressStatus',
  `changed_at`  DATETIME    NOT NULL COMMENT '변경 일시'
) COMMENT = '진행상태 활동 기록';

-- ───────── 맞춤 채용 ─────────
CREATE TABLE `job_posting` (
  `job_id`          BIGINT       NOT NULL COMMENT '공고 ID',
  `external_id`     VARCHAR(50)  NOT NULL COMMENT '고용24 구인인증번호 (UNIQUE)',
  `company`         VARCHAR(100) NOT NULL COMMENT '회사명',
  `title`           VARCHAR(200) NOT NULL COMMENT '공고 제목',
  `region`          VARCHAR(30)  NOT NULL COMMENT 'Region',
  `address`         VARCHAR(200) NULL     COMMENT '근무지 주소',
  `education`       VARCHAR(30)  NOT NULL COMMENT 'Education',
  `career_level`    VARCHAR(30)  NOT NULL COMMENT '경력: NEW(신입)',
  `employment_type` VARCHAR(30)  NOT NULL COMMENT 'EmploymentType',
  `job_category`    VARCHAR(30)  NOT NULL COMMENT 'JobCategory (직무 사전으로 통일)',
  `salary`          VARCHAR(100) NULL     COMMENT '급여',
  `deadline`        DATE         NOT NULL COMMENT '마감일',
  `original_url`    VARCHAR(500) NOT NULL COMMENT '고용24 원문 URL',
  `collected_at`    DATETIME     NOT NULL COMMENT '수집 일시 (isNew 판단)',
  `updated_at`      DATETIME     NOT NULL COMMENT '수정일'
) COMMENT = '채용공고 (고용24 수집)';

CREATE TABLE `job_policy_match` (
  `job_id`    BIGINT NOT NULL COMMENT '공고 ID',
  `policy_id` BIGINT NOT NULL COMMENT '정책 ID'
) COMMENT = '공고별 도움 되는 지원';

CREATE TABLE `job_bookmark` (
  `bookmark_id` BIGINT   NOT NULL COMMENT '관심 공고 ID',
  `user_id`     BIGINT   NOT NULL COMMENT '회원 ID (user_id + job_id UNIQUE)',
  `job_id`      BIGINT   NOT NULL COMMENT '공고 ID',
  `created_at`  DATETIME NOT NULL COMMENT '저장일'
) COMMENT = '관심 공고';

-- ───────── 커뮤니티 ─────────
CREATE TABLE `post` (
  `post_id`            BIGINT       NOT NULL COMMENT '게시글 ID',
  `user_id`            BIGINT       NULL     COMMENT '작성자 ID (탈퇴 시 NULL, 화면에는 상황만 표시)',
  `type`               VARCHAR(30)  NOT NULL COMMENT 'PostType: PASS_REVIEW, PROGRAM_REVIEW, PORTFOLIO',
  `policy_id`          BIGINT       NULL     COMMENT '연결 정책 (PROGRAM_REVIEW 필수)',
  `job_id`             BIGINT       NULL     COMMENT '연결 공고',
  `job_category`       VARCHAR(30)  NULL     COMMENT 'JobCategory 직무 태그',
  `rating`             TINYINT      NULL     COMMENT '별점 1~5 (PORTFOLIO는 생략 가능)',
  `one_liner`          VARCHAR(60)  NOT NULL COMMENT '한 줄 평',
  `prep_period`        VARCHAR(30)  NOT NULL COMMENT 'PrepPeriod: WEEK_1, MONTH_1, MONTH_3, MONTH_6_PLUS',
  `content`            TEXT         NULL     COMMENT '자유 의견',
  `portfolio_url`      VARCHAR(500) NULL     COMMENT '포트폴리오 링크 (파일 첨부 없음)',
  `author_stage`       VARCHAR(30)  NOT NULL COMMENT '작성 당시 LifeStage',
  `author_stage_after` VARCHAR(30)  NULL     COMMENT '바뀐 LifeStage (예: FIRST_JOB)',
  `hidden`             TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '숨김 (신고 5건 자동 또는 관리자)',
  `hidden_reason`      VARCHAR(200) NULL     COMMENT '숨김 사유',
  `deleted_at`         DATETIME     NULL     COMMENT '소프트 삭제 일시',
  `created_at`         DATETIME     NOT NULL COMMENT '작성일',
  `updated_at`         DATETIME     NOT NULL COMMENT '수정일'
) COMMENT = '후기·포트폴리오';

CREATE TABLE `post_helpful_point` (
  `post_id` BIGINT      NOT NULL COMMENT '게시글 ID',
  `point`   VARCHAR(30) NOT NULL COMMENT '도움이 된 점'
) COMMENT = '도움이 된 점 (복수)';

CREATE TABLE `post_report` (
  `report_id`   BIGINT       NOT NULL COMMENT '신고 ID',
  `post_id`     BIGINT       NOT NULL COMMENT '게시글 ID',
  `reporter_id` BIGINT       NOT NULL COMMENT '신고자 ID (post_id + reporter_id UNIQUE)',
  `reason`      VARCHAR(30)  NOT NULL COMMENT 'ReportReason: PERSONAL_INFO, SPAM, ABUSE, FALSE_INFO, ETC',
  `detail`      VARCHAR(200) NULL     COMMENT '상세 내용',
  `status`      VARCHAR(30)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, RESOLVED',
  `created_at`  DATETIME     NOT NULL COMMENT '신고일',
  `resolved_at` DATETIME     NULL     COMMENT '처리일'
) COMMENT = '게시글 신고';

-- ───────── 캘린더·알림 ─────────
CREATE TABLE `calendar_event` (
  `event_id`    BIGINT       NOT NULL COMMENT '일정 ID',
  `user_id`     BIGINT       NOT NULL COMMENT '회원 ID',
  `type`        VARCHAR(30)  NOT NULL COMMENT 'EventType: APPLY_START, APPLY_END, COUNSELING, PARTICIPATION, JOB_DEADLINE, CUSTOM',
  `event_date`  DATE         NOT NULL COMMENT '날짜',
  `title`       VARCHAR(200) NOT NULL COMMENT '일정 제목',
  `memo`        VARCHAR(200) NULL     COMMENT '메모',
  `source_type` VARCHAR(30)  NOT NULL COMMENT 'POLICY, JOB, CUSTOM',
  `policy_id`   BIGINT       NULL     COMMENT '연결 정책',
  `job_id`      BIGINT       NULL     COMMENT '연결 공고',
  `item_id`     BIGINT       NULL     COMMENT '자동 생성한 로드맵 항목 (항목 삭제 시 함께 삭제)',
  `bookmark_id` BIGINT       NULL     COMMENT '자동 생성한 관심 공고 (해제 시 함께 삭제)',
  `created_at`  DATETIME     NOT NULL COMMENT '등록일'
) COMMENT = 'NEXT 캘린더 일정';

CREATE TABLE `notification` (
  `notification_id` BIGINT       NOT NULL COMMENT '알림 ID',
  `user_id`         BIGINT       NOT NULL COMMENT '회원 ID',
  `type`            VARCHAR(30)  NOT NULL COMMENT 'NotificationType: NEW_JOB, DEADLINE, NEW_REVIEW, SYSTEM',
  `title`           VARCHAR(200) NOT NULL COMMENT '알림 제목',
  `body`            VARCHAR(300) NULL     COMMENT '알림 내용',
  `target_type`     VARCHAR(30)  NULL     COMMENT '이동 대상: POLICY, JOB, JOB_LIST, POST, CALENDAR',
  `target_id`       BIGINT       NULL     COMMENT '이동 대상 ID',
  `is_read`         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '읽음 여부',
  `read_at`         DATETIME     NULL     COMMENT '읽은 일시',
  `created_at`      DATETIME     NOT NULL COMMENT '생성일'
) COMMENT = '알림 센터';

-- ───────── 외부 데이터 수집 ─────────
CREATE TABLE `sync_log` (
  `sync_log_id`   BIGINT       NOT NULL COMMENT '수집 기록 ID',
  `source`        VARCHAR(30)  NOT NULL COMMENT 'ONTONG, WORK24',
  `trigger_type`  VARCHAR(30)  NOT NULL COMMENT 'SCHEDULED, MANUAL',
  `status`        VARCHAR(30)  NOT NULL COMMENT 'RUNNING, SUCCESS, FAILED',
  `fetched`       INT          NOT NULL DEFAULT 0 COMMENT '받은 건수',
  `inserted`      INT          NOT NULL DEFAULT 0 COMMENT '추가 건수',
  `updated`       INT          NOT NULL DEFAULT 0 COMMENT '갱신 건수',
  `failed`        INT          NOT NULL DEFAULT 0 COMMENT '실패 건수',
  `error_message` VARCHAR(500) NULL     COMMENT '오류 메시지',
  `started_at`    DATETIME     NOT NULL COMMENT '시작 일시',
  `finished_at`   DATETIME     NULL     COMMENT '종료 일시'
) COMMENT = '외부 데이터 수집 기록';

-- ───────── 기본키 ─────────
ALTER TABLE `users` ADD CONSTRAINT `PK_USERS` PRIMARY KEY (`user_id`);
ALTER TABLE `refresh_token` ADD CONSTRAINT `PK_REFRESH_TOKEN` PRIMARY KEY (`token_id`);
ALTER TABLE `user_visit` ADD CONSTRAINT `PK_USER_VISIT` PRIMARY KEY (`user_id`, `visit_date`);
ALTER TABLE `user_life_stage` ADD CONSTRAINT `PK_USER_LIFE_STAGE` PRIMARY KEY (`user_id`, `life_stage`);
ALTER TABLE `user_interest` ADD CONSTRAINT `PK_USER_INTEREST` PRIMARY KEY (`user_id`, `interest_field`);
ALTER TABLE `job_preference` ADD CONSTRAINT `PK_JOB_PREFERENCE` PRIMARY KEY (`user_id`);
ALTER TABLE `job_preference_region` ADD CONSTRAINT `PK_JOB_PREFERENCE_REGION` PRIMARY KEY (`user_id`, `region`);
ALTER TABLE `job_preference_category` ADD CONSTRAINT `PK_JOB_PREFERENCE_CATEGORY` PRIMARY KEY (`user_id`, `job_category`);
ALTER TABLE `job_preference_employment_type` ADD CONSTRAINT `PK_JOB_PREFERENCE_EMPLOYMENT_TYPE` PRIMARY KEY (`user_id`, `employment_type`);
ALTER TABLE `policy` ADD CONSTRAINT `PK_POLICY` PRIMARY KEY (`policy_id`);
ALTER TABLE `policy_life_stage` ADD CONSTRAINT `PK_POLICY_LIFE_STAGE` PRIMARY KEY (`policy_id`, `life_stage`);
ALTER TABLE `policy_tag` ADD CONSTRAINT `PK_POLICY_TAG` PRIMARY KEY (`policy_id`, `tag`);
ALTER TABLE `roadmap_template` ADD CONSTRAINT `PK_ROADMAP_TEMPLATE` PRIMARY KEY (`template_id`);
ALTER TABLE `template_life_stage` ADD CONSTRAINT `PK_TEMPLATE_LIFE_STAGE` PRIMARY KEY (`template_id`, `life_stage`);
ALTER TABLE `template_interest` ADD CONSTRAINT `PK_TEMPLATE_INTEREST` PRIMARY KEY (`template_id`, `interest_field`);
ALTER TABLE `template_step` ADD CONSTRAINT `PK_TEMPLATE_STEP` PRIMARY KEY (`template_step_id`);
ALTER TABLE `template_step_policy` ADD CONSTRAINT `PK_TEMPLATE_STEP_POLICY` PRIMARY KEY (`template_step_id`, `policy_id`);
ALTER TABLE `roadmap` ADD CONSTRAINT `PK_ROADMAP` PRIMARY KEY (`roadmap_id`);
ALTER TABLE `roadmap_step` ADD CONSTRAINT `PK_ROADMAP_STEP` PRIMARY KEY (`roadmap_step_id`);
ALTER TABLE `roadmap_item` ADD CONSTRAINT `PK_ROADMAP_ITEM` PRIMARY KEY (`item_id`);
ALTER TABLE `roadmap_item_history` ADD CONSTRAINT `PK_ROADMAP_ITEM_HISTORY` PRIMARY KEY (`history_id`);
ALTER TABLE `job_posting` ADD CONSTRAINT `PK_JOB_POSTING` PRIMARY KEY (`job_id`);
ALTER TABLE `job_policy_match` ADD CONSTRAINT `PK_JOB_POLICY_MATCH` PRIMARY KEY (`job_id`, `policy_id`);
ALTER TABLE `job_bookmark` ADD CONSTRAINT `PK_JOB_BOOKMARK` PRIMARY KEY (`bookmark_id`);
ALTER TABLE `post` ADD CONSTRAINT `PK_POST` PRIMARY KEY (`post_id`);
ALTER TABLE `post_helpful_point` ADD CONSTRAINT `PK_POST_HELPFUL_POINT` PRIMARY KEY (`post_id`, `point`);
ALTER TABLE `post_report` ADD CONSTRAINT `PK_POST_REPORT` PRIMARY KEY (`report_id`);
ALTER TABLE `calendar_event` ADD CONSTRAINT `PK_CALENDAR_EVENT` PRIMARY KEY (`event_id`);
ALTER TABLE `notification` ADD CONSTRAINT `PK_NOTIFICATION` PRIMARY KEY (`notification_id`);
ALTER TABLE `sync_log` ADD CONSTRAINT `PK_SYNC_LOG` PRIMARY KEY (`sync_log_id`);

-- ───────── 외래키 ─────────
ALTER TABLE `refresh_token` ADD CONSTRAINT `FK_users_TO_refresh_token` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);
ALTER TABLE `user_visit` ADD CONSTRAINT `FK_users_TO_user_visit` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);
ALTER TABLE `user_life_stage` ADD CONSTRAINT `FK_users_TO_user_life_stage` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);
ALTER TABLE `user_interest` ADD CONSTRAINT `FK_users_TO_user_interest` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);
ALTER TABLE `job_preference` ADD CONSTRAINT `FK_users_TO_job_preference` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);
ALTER TABLE `job_preference_region` ADD CONSTRAINT `FK_job_preference_TO_region` FOREIGN KEY (`user_id`) REFERENCES `job_preference` (`user_id`);
ALTER TABLE `job_preference_category` ADD CONSTRAINT `FK_job_preference_TO_category` FOREIGN KEY (`user_id`) REFERENCES `job_preference` (`user_id`);
ALTER TABLE `job_preference_employment_type` ADD CONSTRAINT `FK_job_preference_TO_employment_type` FOREIGN KEY (`user_id`) REFERENCES `job_preference` (`user_id`);

ALTER TABLE `policy` ADD CONSTRAINT `FK_users_TO_policy` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`);
ALTER TABLE `policy_life_stage` ADD CONSTRAINT `FK_policy_TO_policy_life_stage` FOREIGN KEY (`policy_id`) REFERENCES `policy` (`policy_id`);
ALTER TABLE `policy_tag` ADD CONSTRAINT `FK_policy_TO_policy_tag` FOREIGN KEY (`policy_id`) REFERENCES `policy` (`policy_id`);

ALTER TABLE `template_life_stage` ADD CONSTRAINT `FK_roadmap_template_TO_template_life_stage` FOREIGN KEY (`template_id`) REFERENCES `roadmap_template` (`template_id`);
ALTER TABLE `template_interest` ADD CONSTRAINT `FK_roadmap_template_TO_template_interest` FOREIGN KEY (`template_id`) REFERENCES `roadmap_template` (`template_id`);
ALTER TABLE `template_step` ADD CONSTRAINT `FK_roadmap_template_TO_template_step` FOREIGN KEY (`template_id`) REFERENCES `roadmap_template` (`template_id`);
ALTER TABLE `template_step_policy` ADD CONSTRAINT `FK_template_step_TO_template_step_policy` FOREIGN KEY (`template_step_id`) REFERENCES `template_step` (`template_step_id`);
ALTER TABLE `template_step_policy` ADD CONSTRAINT `FK_policy_TO_template_step_policy` FOREIGN KEY (`policy_id`) REFERENCES `policy` (`policy_id`);

ALTER TABLE `roadmap` ADD CONSTRAINT `FK_users_TO_roadmap` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);
ALTER TABLE `roadmap` ADD CONSTRAINT `FK_roadmap_template_TO_roadmap` FOREIGN KEY (`template_id`) REFERENCES `roadmap_template` (`template_id`);
ALTER TABLE `roadmap_step` ADD CONSTRAINT `FK_roadmap_TO_roadmap_step` FOREIGN KEY (`roadmap_id`) REFERENCES `roadmap` (`roadmap_id`);
ALTER TABLE `roadmap_item` ADD CONSTRAINT `FK_roadmap_step_TO_roadmap_item` FOREIGN KEY (`roadmap_step_id`) REFERENCES `roadmap_step` (`roadmap_step_id`);
ALTER TABLE `roadmap_item` ADD CONSTRAINT `FK_policy_TO_roadmap_item` FOREIGN KEY (`policy_id`) REFERENCES `policy` (`policy_id`);
ALTER TABLE `roadmap_item_history` ADD CONSTRAINT `FK_roadmap_item_TO_roadmap_item_history` FOREIGN KEY (`item_id`) REFERENCES `roadmap_item` (`item_id`);

ALTER TABLE `job_policy_match` ADD CONSTRAINT `FK_job_posting_TO_job_policy_match` FOREIGN KEY (`job_id`) REFERENCES `job_posting` (`job_id`);
ALTER TABLE `job_policy_match` ADD CONSTRAINT `FK_policy_TO_job_policy_match` FOREIGN KEY (`policy_id`) REFERENCES `policy` (`policy_id`);
ALTER TABLE `job_bookmark` ADD CONSTRAINT `FK_users_TO_job_bookmark` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);
ALTER TABLE `job_bookmark` ADD CONSTRAINT `FK_job_posting_TO_job_bookmark` FOREIGN KEY (`job_id`) REFERENCES `job_posting` (`job_id`);

ALTER TABLE `post` ADD CONSTRAINT `FK_users_TO_post` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);
ALTER TABLE `post` ADD CONSTRAINT `FK_policy_TO_post` FOREIGN KEY (`policy_id`) REFERENCES `policy` (`policy_id`);
ALTER TABLE `post` ADD CONSTRAINT `FK_job_posting_TO_post` FOREIGN KEY (`job_id`) REFERENCES `job_posting` (`job_id`);
ALTER TABLE `post_helpful_point` ADD CONSTRAINT `FK_post_TO_post_helpful_point` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`);
ALTER TABLE `post_report` ADD CONSTRAINT `FK_post_TO_post_report` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`);
ALTER TABLE `post_report` ADD CONSTRAINT `FK_users_TO_post_report` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`user_id`);

ALTER TABLE `calendar_event` ADD CONSTRAINT `FK_users_TO_calendar_event` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);
ALTER TABLE `calendar_event` ADD CONSTRAINT `FK_policy_TO_calendar_event` FOREIGN KEY (`policy_id`) REFERENCES `policy` (`policy_id`);
ALTER TABLE `calendar_event` ADD CONSTRAINT `FK_job_posting_TO_calendar_event` FOREIGN KEY (`job_id`) REFERENCES `job_posting` (`job_id`);
ALTER TABLE `calendar_event` ADD CONSTRAINT `FK_roadmap_item_TO_calendar_event` FOREIGN KEY (`item_id`) REFERENCES `roadmap_item` (`item_id`);
ALTER TABLE `calendar_event` ADD CONSTRAINT `FK_job_bookmark_TO_calendar_event` FOREIGN KEY (`bookmark_id`) REFERENCES `job_bookmark` (`bookmark_id`);

ALTER TABLE `notification` ADD CONSTRAINT `FK_users_TO_notification` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`);
