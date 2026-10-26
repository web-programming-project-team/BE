# NEXT 시흥 — 초기 세팅 인수인계

> 팀 NextStep · 작성 2026-10-26 · 기준 문서: 제안서 v3, API 명세 v1.1
> API 명세: https://web-programming-project-team.github.io/API-/
> 저장소: [BE](https://github.com/web-programming-project-team/BE) · [FE](https://github.com/web-programming-project-team/FE) · [API-](https://github.com/web-programming-project-team/API-) (명세)

이 문서는 처음 계획한 모노레포 대신 **조직에 이미 있는 `BE` / `FE` 저장소 2개**를 쓰도록 고친 초기 세팅 문서입니다.
뼈대 코드는 이미 두 저장소에 커밋되어 있습니다. 새로 합류하거나 새 PC에서 받는 사람은 **3장 → 5장 → 6-6 → 7-7** 순서로 따라 하면 됩니다.

---

## 0. 작업 규칙 (먼저 읽기)

- 단계 순서를 바꾸지 않는다. 한 단계가 완료 기준을 통과하지 못하면 멈추고 원인을 공유한다.
- **비밀값은 커밋하지 않는다.** DB 비밀번호, JWT 시크릿, 공공 API 키는 `.env` / `application-local.yml`에만 둔다(둘 다 `.gitignore`에 있음). 저장소에는 `*.example` 파일만 올린다.
- 지금은 **뼈대만** 있다. 도메인 기능(로드맵 규칙, 채용 필터 등)은 아직 구현하지 않았다. 패키지와 빈 클래스까지만 있다.
- API 경로·응답 형식·enum 이름은 API 명세 v1.1을 그대로 따른다. 임의로 바꾸지 않는다.
- 서비스 안에서 정책 **신청** 기능을 만들지 않는다(항상 공식 사이트 링크). 주민등록번호·소득 정보는 어떤 테이블에도 두지 않는다.
- 3순위 기능(이메일·푸시 알림, 준비서류 체크리스트, 로드맵 직접 편집, 포트폴리오 파일 첨부, 인기 순위)은 만들지 않는다.

---

## 1. 프로젝트 한눈에

| 항목 | 내용 |
| --- | --- |
| 서비스 | 시흥시 19~24세 청년 사회진입 로드맵 웹서비스 |
| 핵심 흐름 | 상태 등록 → 규칙 기반 로드맵 → 정책·프로그램 연결 → 진행상태 관리 → 캘린더·알림 |
| 추가 기능 | 맞춤 채용공고(고용24), 경험 공유 커뮤니티, 알림 센터 |
| 프론트 | React (Vite) — `FE` 저장소 |
| 백엔드 | Spring Boot REST API (JSON) — `BE` 저장소 |
| DB | MySQL (로컬은 Docker, `BE`의 `docker-compose.yml`) |
| 외부 데이터 | 온통청년 Open API(정책), 고용24 채용정보 Open API(공고) |
| 배치 | Spring Scheduler (하루 1회 수집, 알림 생성) |

### 담당 (제안서 7장)

| 팀원 | 역할 | 세팅에서 맡은 것 |
| --- | --- | --- |
| 팀장 | PM / Backend | 저장소·Git 규칙, 백엔드 공통(global), 회원·커뮤니티·알림 패키지 |
| BE-데이터 | Backend / Data | DB(docker-compose), 정책·로드맵·채용·관리자 패키지, 스케줄러 뼈대, 공공 API 키 신청 |
| FE-UX | Frontend / UX | 프론트 프로젝트, 라우팅, 공통 레이아웃, API 클라이언트 |
| FE-테스트 | Frontend / Test | 프론트 폴더 구조, 공통 컴포넌트(배지·칩·D-day), 연결 확인 테스트 |

---

## 2. 확정 사항과 기본값

| 항목 | 값 | 상태 |
| --- | --- | --- |
| 프론트 / 백엔드 | React / Spring Boot | 확정 (팀 합의) |
| 저장소 구조 | **`BE`, `FE` 2개** (조직에 이미 있던 저장소 사용, 명세는 `API-`) | 확정 |
| DB | MySQL 8.4 (MariaDB도 가능하다고 제안서에 명시) | 기본값 |
| Java | 21 (LTS), Temurin 21로 확인 | 기본값 |
| Spring Boot | 4.1.2 (2026-10-26 Spring Initializr 기본값) | 기본값 |
| 빌드 도구 | Gradle (Groovy DSL), wrapper 9.8.2 | 기본값 |
| Node.js | 22 LTS 이상 (24.12에서 확인) | 기본값 |
| 프론트 언어 | JavaScript | 기본값 — 팀 확인 |
| 인증 | JWT (Access 30분, Refresh 14일) | API 명세 기준 |
| 배포 | 미정 (12주차 전 결정) | 미정 |

기본값을 바꾸면 이 표를 먼저 고치고 커밋한다.

---

## 3. 사전 준비 (각자 PC)

```bash
java -version        # 21.x   (없으면 Windows: winget install EclipseAdoptium.Temurin.21.JDK)
node -v              # v22 이상
npm -v
git --version
docker --version
docker compose version
```

**완료 기준**: 위 명령이 모두 버전을 출력한다. Docker Desktop이 실행 중이다(`docker ps`가 에러 없이 나온다).

---

## 4. Step 1 — 저장소 받기

```bash
git clone https://github.com/web-programming-project-team/BE.git
git clone https://github.com/web-programming-project-team/FE.git
cd BE && git switch develop && cd ..
cd FE && git switch develop && cd ..
```

두 저장소 모두 루트에 `.gitignore`, `.editorconfig`, `README.md`, `.github/`(PR·이슈 템플릿)가 있다. 이 문서는 `BE/docs/SETUP.md`에만 두고, FE README에서 링크한다.

**완료 기준**: 두 저장소 모두 `main`, `develop` 브랜치가 보인다.

---

## 5. Step 2 — 로컬 DB (`BE` 저장소)

`docker-compose.yml`은 MySQL 8.4 컨테이너 `next-siheung-mysql`을 띄우고 DB `next_siheung`을 만든다(utf8mb4, Asia/Seoul).

```bash
cd BE
cp .env.example .env      # DB_PASSWORD, DB_ROOT_PASSWORD 수정
docker compose up -d
docker exec -it next-siheung-mysql mysql -u nextstep -p -e "SHOW DATABASES;"
```

> **포트 충돌**: PC에 MySQL이 이미 설치되어 3306을 쓰고 있으면 `docker compose up`이 `ports are not available`로 실패한다.
> 기존 MySQL을 끄지 말고 `.env`에 `DB_PORT=3307`을 넣은 뒤, `application-local.yml`의 url도 `localhost:3307`로 바꾼다.

**완료 기준**: `next_siheung` 데이터베이스가 보인다.

---

## 6. Step 3 — 백엔드 (`BE` 저장소)

### 3-1. 프로젝트

Spring Initializr로 만들었다: Gradle · Java 21 · `com.nextstep` / `next-siheung` / `com.nextstep.nextsiheung`
의존성: web, data-jpa, validation, security, mysql, lombok, devtools, configuration-processor

`build.gradle`에 직접 추가한 것:

| 라이브러리 | 버전 | 비고 |
| --- | --- | --- |
| `io.jsonwebtoken:jjwt-api` / `jjwt-impl` / `jjwt-jackson` | 0.13.0 | JWT (6주차에 사용) |
| `org.springdoc:springdoc-openapi-starter-webmvc-ui` | 3.1.1 | Boot 4.x는 springdoc 3.x. 3.2.0은 세팅 이틀 전에 나와서 보류 |

### 3-2. 패키지 구조

API 명세의 도메인과 같은 이름으로 나눴다. 도메인마다 `controller / service / repository / entity / dto` 하위 패키지가 있고, 빈 패키지는 `package-info.java`로 유지한다.

```
com.nextstep.nextsiheung
├── NextSiheungApplication.java      (@EnableScheduling, @EnableJpaAuditing)
├── global
│   ├── config        SecurityConfig (CORS 포함) · CorsConfig, SwaggerConfig, JpaConfig (빈 클래스)
│   ├── security      JwtProvider, JwtAuthenticationFilter (빈 클래스, 6주차)
│   ├── response      ApiResponse, PageResponse
│   ├── exception     ErrorCode, BusinessException, GlobalExceptionHandler
│   └── common        BaseTimeEntity, enums/ (15개)
├── health            HealthController   (GET /api/v1/health)
└── domain
    ├── auth          회원가입·로그인·토큰        — 팀장
    ├── user          프로필·상태·채용 조건       — 팀장
    ├── roadmap       로드맵·진행상태·템플릿      — BE-데이터(로드맵) / 팀장(진행상태)
    ├── policy        정책·프로그램               — BE-데이터
    ├── job           채용공고·관심 공고          — BE-데이터
    ├── post          후기·포트폴리오·신고        — 팀장
    ├── calendar      일정                        — 팀장
    ├── notification  알림 센터                   — 팀장
    ├── admin         관리자 API·운영 지표        — BE-데이터
    └── batch         스케줄러(정책·공고 수집, 알림 생성) — BE-데이터
```

### 3-3. 설정 파일

| 파일 | 커밋 | 내용 |
| --- | --- | --- |
| `src/main/resources/application.yml` | O | 공통: 프로필 `local`, `ddl-auto: validate`, CORS(`http://localhost:5173`), JWT 유효기간, 배치 cron (`app.batch.enabled: false`) |
| `src/main/resources/application-local.yml.example` | O | 템플릿 |
| `src/main/resources/application-local.yml` | **X** | 예시를 복사해 DB 계정(.env와 같은 값), JWT 시크릿, 공공 API 키 입력 |

```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
# JWT 시크릿 생성 예: openssl rand -base64 32
```

배치 시각: 03:00 온통청년 수집, 04:00 고용24 수집, 09:00 마감 알림.

### 3-4. 공통 코드 (명세 v1.1 응답 형식)

- `ApiResponse<T>(success, code, message, data)`: `ok(data)`, `ok()`, `fail(ErrorCode, data)`
- `PageResponse<T>(content, page, size, totalElements, totalPages, hasNext)`: `of(Page)`
- `ErrorCode`: 명세 "공통 에러 코드" 표 기준 16개. 도메인별 NOT_FOUND는 필요할 때 추가한다.
  `POLICY_ENDED`는 422인데, Spring 7에서 `HttpStatus.UNPROCESSABLE_ENTITY`가 deprecated 되어 같은 값인 `UNPROCESSABLE_CONTENT`를 쓴다.
- `GlobalExceptionHandler`: `BusinessException` → 해당 상태코드. 검증 실패 → 400 `INVALID_INPUT` + `data.errors[{field, reason}]`. 그 외 → 500 `INTERNAL_ERROR`.
- `SecurityConfig`: 공개 경로는 `/api/v1/health`, `/api/v1/auth/**`, Swagger, GET `/api/v1/policies/**`·`/api/v1/posts/**`. `/api/v1/admin/**`은 ADMIN만 접근하고, 나머지는 인증이 필요하다. 인증이 없으면 401로 응답한다. JWT 필터는 6주차에 연결한다.

### 3-5. 공통 enum (`global/common/enums/`, 명세 "공통 enum" 표 그대로)

| enum | 값 |
| --- | --- |
| `ProgressStatus` | TO_CHECK, INTERESTED, PLANNED, APPLIED, IN_PROGRESS, COMPLETED |
| `LifeStage` | ENROLLED, GRADUATING, ON_LEAVE, MILITARY, JOB_SEEKING, FIRST_JOB, INDEPENDENCE |
| `InterestField` | EMPLOYMENT, EDUCATION, HOUSING, FINANCE, COUNSELING, CULTURE_LIFE |
| `JobCategory` | OFFICE, IT, MANUFACTURING, SERVICE, DESIGN, ETC |
| `Region` | SIHEUNG, ANSAN, INCHEON, BUCHEON, GWANGMYEONG |
| `Education` | HIGH_SCHOOL, COLLEGE, UNIVERSITY, ANY |
| `EmploymentType` | FULL_TIME, CONTRACT, INTERN, PART_TIME |
| `RecruitStatus` | UPCOMING, OPEN, CLOSED, ENDED |
| `PolicySource` | ONTONG, ADMIN |
| `PostType` | PASS_REVIEW, PROGRAM_REVIEW, PORTFOLIO |
| `PrepPeriod` | WEEK_1, MONTH_1, MONTH_3, MONTH_6_PLUS |
| `ReportReason` | PERSONAL_INFO, SPAM, ABUSE, FALSE_INFO, ETC |
| `EventType` | APPLY_START, APPLY_END, COUNSELING, PARTICIPATION, JOB_DEADLINE, CUSTOM |
| `NotificationType` | NEW_JOB, DEADLINE, NEW_REVIEW, SYSTEM |
| `Role` | USER, ADMIN |

화면 표시명은 FE의 `src/constants/enumLabels.js`에 있다. JPA 엔티티에서는 항상 `@Enumerated(EnumType.STRING)`을 쓴다.

### 3-6. 실행 확인

```bash
./gradlew bootRun
# 다른 터미널
curl -s http://localhost:8080/api/v1/health
curl -i http://localhost:8080/api/v1/roadmaps/me
```

**완료 기준**
- `{"success":true,"code":"OK","message":"요청이 성공했습니다.","data":{"status":"UP",...}}`가 나온다.
- http://localhost:8080/swagger-ui/index.html 이 열린다.
- `/api/v1/roadmaps/me`가 401을 돌려준다.

> 참고: 엔티티가 아직 없어서 서버는 그냥 뜬다. 엔티티를 만들기 시작하면 로컬 프로필의 `ddl-auto: update`로 테이블이 생긴다. ERD 확정(5주차 산출물) 후 테이블을 만든다.
> 기본 테스트 `NextSiheungApplicationTests`는 DB 연결이 필요하므로 `./gradlew build` 전에 MySQL을 띄워 둔다.

---

## 7. Step 4 — 프론트 (`FE` 저장소)

### 4-1. 프로젝트

`npm create vite@latest` (react 템플릿)로 만들고, 저장소 루트에 두었다. React 19 · Vite 8 · `react-router-dom` · `axios` · lint는 oxlint(`npm run lint`)를 쓴다.

```bash
cd FE
npm install
```

### 4-2. 폴더 구조

```
src
├── api/            client.js (axios 인스턴스). 도메인별 파일(auth.js, roadmap.js ...)은 기능 개발 때 추가
├── components/     공통 컴포넌트 자리 (StatusBadge, DdayTag, Chip, Button, Card ...) — FE-테스트
├── constants/      enumLabels.js (서버 enum → 화면 표시명, 명세 v1.1 기준)
├── hooks/
├── layouts/        AppLayout (헤더 + 메뉴 4개), AuthLayout
├── pages/          auth, onboarding, roadmap, policy, jobs, community, calendar, me, admin, HealthCheck, NotFound
├── styles/         tokens.css (색·글꼴 토큰)
├── App.jsx         라우터
└── main.jsx        BrowserRouter, tokens.css
```

### 4-3. 환경 변수와 개발 서버 프록시

```bash
cp .env.example .env      # VITE_API_BASE_URL=/api/v1
```

`vite.config.js`: 포트 5173, `/api` 요청은 `http://localhost:8080`으로 프록시한다.

### 4-4. API 클라이언트 (`src/api/client.js`)

- `localStorage.accessToken`이 있으면 `Authorization: Bearer ...`를 붙인다.
- 성공 응답은 `{ success, code, message, data }`에서 **`data`만** 돌려준다.
- 실패하면 `{ status, code, message, data }` 객체로 reject한다(네트워크 오류는 `code: 'NETWORK_ERROR'`).
- `TOKEN_EXPIRED`가 오면 `/auth/refresh`로 다시 시도하는 로직은 6주차에 추가한다.

### 4-5. 라우트

| 경로 | 페이지 | 메뉴 | 레이아웃 |
| --- | --- | --- | --- |
| `/login`, `/signup` | 로그인·회원가입 | — | AuthLayout |
| `/onboarding` | 상태·관심분야 등록 (3단계) | — | AuthLayout |
| `/` | 내 로드맵 (메인) | 내 로드맵 | AppLayout |
| `/policies/:policyId` | 정책·프로그램 상세 | — | AppLayout |
| `/jobs` | 맞춤 채용 | 맞춤 채용 | AppLayout |
| `/jobs/:jobId` | 채용공고 상세 | — | AppLayout |
| `/community` | 커뮤니티 | 커뮤니티 | AppLayout |
| `/community/:postId` | 게시글 상세 | — | AppLayout |
| `/calendar` | NEXT 캘린더 | NEXT 캘린더 | AppLayout |
| `/me` | 내 기록 (지난 로드맵, 내가 쓴 후기) | 프로필 메뉴 | AppLayout |
| `/admin/*` | 관리자 (정책 관리, 템플릿, 신고, 수집) | 관리자만 | AppLayout |
| `/health` | **임시** 연결 확인 | — | AppLayout |

메뉴는 4개(내 로드맵 / 맞춤 채용 / 커뮤니티 / NEXT 캘린더)로 고정한다(제안서 5-5). 알림 센터는 헤더의 종 아이콘 드롭다운이라 별도 경로가 없다. 1분 후기 작성과 상황 변경은 모달로 띄운다.
지금 각 페이지는 제목만 있는 빈 컴포넌트다.

### 4-6. 디자인 토큰

`src/styles/tokens.css`: 기본색 `#0F9D8A`, 배경·카드·선·글자 3단계, 위험(D-3 이하)·경고(D-4~D-7), 진행상태 배지 6종(글자/배경), 카드 radius 16px, Pretendard 글꼴 스택.

### 4-7. 연결 확인

```bash
npm run dev    # http://localhost:5173/health
```

**완료 기준**: 백엔드가 켜진 상태에서 `/health` 화면에 `UP (...)`이 보인다(프록시·CORS 정상). 다들 확인하고 나면 `pages/HealthCheck.jsx`와 라우트를 지운다.

---

## 8. Step 5 — Git 협업 규칙 (두 저장소 공통)

### 브랜치

```
main      발표·제출용. develop에서만 합친다.
develop   통합 브랜치. 모든 기능 PR의 대상.
feat/#<이슈번호>-<짧은설명>   예) feat/#12-roadmap-status
fix/#<이슈번호>-<짧은설명>
```

이슈 번호는 저장소마다 따로 매겨진다. 백엔드 작업은 `BE`, 화면 작업은 `FE`에 이슈를 연다.

### 커밋 메시지

```
<type>: <한 줄 요약>      type = feat | fix | docs | refactor | test | chore | style
예) feat: 진행상태 변경 API 추가 (#12)
```

### PR 규칙

- 대상 브랜치는 `develop`이고, 리뷰어 1명이 승인하면 머지한다(제안서 7-3).
- PR 제목은 커밋 규칙과 같게 쓰고, 본문에 이슈 번호를 넣는다(`Closes #12`).
- API를 바꾸는 PR은 API 명세(`API-` 저장소)도 같이 고친다.
- 템플릿: `.github/pull_request_template.md`, `.github/ISSUE_TEMPLATE/feature.md` (두 저장소에 같은 내용)

GitHub 라벨: `feature`, `bug`, `docs`, `backend`, `frontend`, `priority-1`, `priority-2`.
`main`, `develop` 브랜치 보호(직접 push 금지, PR 리뷰 1명 필수)를 두 저장소 모두 켠다.

**완료 기준**: 테스트 이슈 1개 → 브랜치 → PR → 리뷰 → `develop` 머지가 한 번 성공한다.

---

## 9. Step 6 — 외부 API 키 (BE-데이터, 6주차)

| 데이터 | 신청처 | 설정 키 (`application-local.yml`) |
| --- | --- | --- |
| 청년정책 | 온통청년 Open API (회원가입 후 인증키 신청·심사) | `app.external.ontong-api-key` |
| 채용공고 | 고용24 채용정보 Open API (인증키 신청) | `app.external.work24-api-key` |

- 키는 `application-local.yml`에만 넣는다. 팀 공유는 메신저 비공개 채팅으로 한다.
- 승인 전에는 `app.batch.enabled: false`로 두고, 공개된 공고·정책 몇 건을 샘플 데이터로 넣어 개발한다(제안서 6-3).
- 시흥 지역 프로그램(시흥시청소년청년재단, 청년스테이션)은 관리자 화면에서 직접 등록한다. 모든 데이터에 공식 출처 URL과 마지막 확인일을 저장한다.

---

## 10. 초기 세팅 완료 체크리스트

- [x] `BE`·`FE` 저장소에 뼈대 커밋, `develop` 브랜치
- [ ] `main`·`develop` 브랜치 보호 (두 저장소)
- [x] `.gitignore`, `.editorconfig`, `README.md`, `docs/SETUP.md`
- [x] `docker compose up -d` → MySQL `next_siheung` 생성
- [x] 백엔드 `GET /api/v1/health` 정상, Swagger 열림, 보호 경로 401
- [x] 공통 응답·에러·BaseTimeEntity·enum 15개
- [x] 도메인 패키지 10개(빈 패키지)
- [x] 프론트 실행, 라우트 빈 페이지, API 클라이언트, 토큰 CSS
- [x] 프론트 `/health`에서 백엔드 연결 확인
- [x] PR·이슈 템플릿
- [ ] 라벨, 테스트 PR 1회
- [ ] 온통청년·고용24 인증키 신청 완료(승인 대기 가능)

---

## 11. 다음 할 일 (제안서 7장 일정)

| 주차 | 백엔드 | 프론트 |
| --- | --- | --- |
| 6 | 회원가입·로그인·JWT, 상태 등록 (팀장) / 키 신청 (BE-데이터) | 로그인·온보딩 화면 |
| 7 | 정책 엔티티·목록·상세, 관리자 등록·수정·종료 (BE-데이터) | 정책 상세, 관리자 목록 |
| 8 | 로드맵 템플릿·규칙 (BE-데이터) | 내 로드맵 메인 |
| 9 | 진행상태·진행률·활동기록·캘린더 (팀장) | 진행상태 배지, 캘린더 (FE-테스트) |
| 10 | 고용24 수집 스케줄러·조건 필터·정책 연결 (BE-데이터) | 맞춤 채용 (FE-UX) |
| 11 | 커뮤니티·신고·알림 센터 (팀장) | 커뮤니티·알림 (FE-테스트) |
| 12 | 통합·반응형·사용자 테스트 (전원) | |
| 13 | 시연 데이터·발표 (전원) | |

---

## 12. 아직 정하지 않은 것

- 배포 환경(클라우드·도메인) — 12주차 전
- 프론트 TypeScript 사용 여부 — 지금은 JavaScript. 바꾸려면 기능 코드가 쌓이기 전에 결정
- DB 마이그레이션 도구(Flyway 등) 도입 여부 — ERD 확정 후
- 공공 API 응답 형식(XML/JSON)에 맞춘 파서 — 키 발급 후 실제 응답 확인
- 수집 시각·알림 시점·신고 자동 숨김 기준 — 명세 기본값, 팀 확인 필요
