# NEXT 시흥 — Backend
시흥시 19~24세 청년 사회진입 로드맵 서비스 — 팀 NextStep

- API 명세: https://web-programming-project-team.github.io/API-/
- 프론트엔드: https://github.com/web-programming-project-team/FE
- 로컬 실행·초기 세팅: [docs/SETUP.md](docs/SETUP.md)

## 빠른 실행

```bash
cp .env.example .env                                                   # DB 계정 값 채우기
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml   # .env와 같은 값
docker compose up -d
./gradlew bootRun
curl -s http://localhost:8080/api/v1/health
```

Spring Boot 4.1 · Java 21 · MySQL 8.4 · Swagger UI: http://localhost:8080/swagger-ui/index.html
