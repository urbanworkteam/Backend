# Farmily Backend

Spring Boot 기반 Farmily 백엔드 API. IntelliJ IDEA 에서 열어 개발합니다.

## 스택
- Java 21 (Toolchain)
- Spring Boot 4.0.x
- Gradle (Kotlin DSL)
- PostgreSQL + Flyway
- Spring Security + Spring Data JPA + Validation
- Lombok, Actuator

## 처음 한 번
```bash
# 1) 클론 (이미 했다면 skip)
git clone https://github.com/urbanworkteam/Backend.git
cd Backend

# 2) IntelliJ IDEA 에서 Open → 이 폴더 선택 → Gradle 자동 import
#    JDK 21 설치 필요 (없으면 IntelliJ Project Structure 에서 다운로드)

# 3) 로컬 환경 파일 생성 (커밋 금지)
cp src/main/resources/application.yml src/main/resources/application-local.yml
# application-local.yml 에 DB/JWT/Kakao/AWS 시크릿 채움
```

## 실행
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
# Windows: gradlew.bat bootRun --args='--spring.profiles.active=local'
```

## 환경 변수
민감 정보는 `application-local.yml` 또는 환경변수로 주입. 전체 키 목록은 docs repo 의 `docs/ENV.md` 참조.

핵심 키:
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`
- `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`, `KAKAO_ADMIN_KEY`
- `KMA_SERVICE_KEY`, `KAKAO_LOCAL_REST_KEY`
- `S3_BUCKET`, `S3_REGION`, `AWS_REGION`
- `BEDROCK_AGENT_ID`, `BEDROCK_AGENT_ALIAS_ID`
- `FCM_SERVICE_ACCOUNT_JSON`
- `PORTONE_API_KEY`, `PORTONE_API_SECRET`

> **시크릿은 절대 git 에 커밋하지 않습니다.** `.gitignore` 가 `application-local.yml`, `.env`, FCM JSON, `.p8` 등을 차단합니다.

## 개발 가이드
- spec: docs repo 의 `docs/specs/be/` 폴더 (BE-000 ~ BE-WEATHER-001 등)
- 컨벤션: `docs/CONVENTIONS.md`
- API: `docs/API_SPEC.md`
- DB 스키마: `docs/DB_SCHEMA.md`

## 배포
TBD (INFRA-001 / INFRA-004 spec 참조).
