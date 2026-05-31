# Farmily Backend

Spring Boot 기반 Farmily 백엔드 API. 농부의 디지털 명함 + 영농일지 + AI 콘텐츠 + 정기결제를 제공합니다.

## 스택

- **언어 / 빌드**: Java 21, Gradle 9.5 (래퍼 포함)
- **프레임워크**: Spring Boot 4.0.x, Spring Security, Spring Data JPA
- **DB / 인프라**: PostgreSQL 16 + Flyway, Redis 7, MinIO (로컬 S3)
- **외부 연동**: 카카오 OAuth, 기상청 단기예보(KMA), PortOne(아임포트) V1, Firebase Cloud Messaging
- **AI**: AWS Bedrock (운영) / mock (로컬 기본)

---

## 🚀 빠른 시작 (Docker, 5분)

가장 빠른 셋업. 도커만 띄우면 끝.

```bash
# 1) 클론
git clone https://github.com/urbanworkteam/Backend.git
cd Backend
git checkout feat       # 최신 작업 브랜치

# 2) 로컬 시크릿 파일 생성 (gitignore 됨, 절대 커밋 X)
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
#    → 시크릿은 아래 "환경 변수" 섹션 참고

# 3) 도커 컴포즈로 백엔드 + DB + Redis + MinIO 한 번에 기동
cd infra/docker
docker compose up -d --build

# 4) 부팅 확인 (~10초 후)
curl http://localhost:8080/actuator/health
# → {"status":"UP", ...}
```

부팅 완료. **dev-master 우회 로그인**으로 카카오 키 없이도 바로 검증 가능:

```bash
curl -X POST http://localhost:8080/api/v1/auth/kakao \
  -H "Content-Type: application/json" \
  -d '{"code":"DEV_MASTER","redirectUri":"http://localhost:3000/oauth/kakao"}'
# → accessToken / refreshToken 발급
```

---

## 🛠 IDE 로 띄우기 (선택)

도커 대신 IntelliJ IDEA 로 직접 실행하려면:

```bash
# DB / Redis / MinIO 만 도커로 (백엔드 제외)
cd infra/docker
docker compose up -d postgres redis minio

# IntelliJ → Open → 프로젝트 루트
# → Gradle 자동 import → JDK 21 자동 설치 안내 따르기
# → Run/Debug Configuration → VM options 또는 args 에 추가:
#    --spring.profiles.active=local
```

또는 CLI:
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
# Windows: gradlew.bat bootRun --args='--spring.profiles.active=local'
```

---

## 🔐 환경 변수 / 시크릿 셋업

### `application-local.yml` 작성

`.example` 을 복사한 뒤 본인이 발급받은 값으로 채웁니다. **이 파일은 `.gitignore` 에 의해 커밋이 차단**되어 있습니다.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/farmily
    username: farmily
    password: farmily

farmily:
  jwt:
    secret: "32바이트 이상 랜덤 문자열"
  kakao:
    client-id: "..."          # 카카오 디벨로퍼스 REST API 키
    client-secret: "..."       # 클라이언트 시크릿 (사용함 ON)
    redirect-uri: "http://localhost:3000/oauth/kakao"
    admin-key: "..."           # 어드민 키 (탈퇴 unlink 용)
  kma:
    service-key: "..."         # data.go.kr "단기예보 조회서비스" Decoding 키
  ai:
    provider: mock             # 기본 mock. 실제 Bedrock 연동은 운영 환경에서
  portone:
    api-key: "..."             # PortOne V1 API 의 imp_key
    api-secret: "..."          # imp_secret
    imp-code: "imp00000000"    # 가맹점 식별코드
    test-mode: true
  fcm:
    service-account-json: |
      {
        ... Firebase 서비스 계정 JSON 전체 ...
      }

logging:
  level:
    kr.farmily: DEBUG
```

### 키 발급 위치

| 키 | 발급 위치 | 비고 |
|---|---|---|
| `kakao.*` | [Kakao Developers](https://developers.kakao.com/console/app) | 앱 키 + 클라이언트 시크릿 (V1) |
| `kma.service-key` | [공공데이터포털](https://www.data.go.kr) → "기상청_단기예보 조회서비스" 활용신청 | Decoding 키 사용 |
| `portone.*` | [PortOne 어드민](https://admin.portone.io) → 결제 연동 → 식별코드·API Keys → **V1 API 탭** | ⚠️ V2 키 잘못 가져오면 안 됨 |
| `fcm.service-account-json` | [Firebase 콘솔](https://console.firebase.google.com) → 프로젝트 설정 → 서비스 계정 → "새 비공개 키 생성" | JSON 통째로 yml literal block |

> 🔑 **시크릿 보안 원칙**:
> - `application-local.yml` 은 `.gitignore` 됨. 커밋 안전.
> - 시크릿을 채팅·슬랙·PR·이슈에 절대 붙여넣지 말 것.
> - 운영은 AWS Secrets Manager 또는 환경변수 주입 (별도).

---

## 🧪 빠른 검증

도커 또는 IDE 로 띄운 후 다음 명령으로 핵심 흐름 확인:

```bash
# 1) dev-master 토큰 발급
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/kakao \
  -H "Content-Type: application/json" \
  -d '{"code":"DEV_MASTER","redirectUri":"http://localhost:3000/oauth/kakao"}' \
  | python -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])")

# 2) 마이페이지 조회
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/mypage

# 3) 농장 위치 등록 (GPS lat/lng 직접 전달)
curl -X POST http://localhost:8080/api/v1/farm-locations \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"label":"main","address":"Seoul","lat":37.5665,"lng":126.978}'

# 4) 날씨 조회 (KMA 키 적용 시)
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/weather?farmLocationId=1&date=$(date +%Y-%m-%d)"

# 5) Swagger UI
open http://localhost:8080/swagger-ui.html
```

---

## 📡 외부 API 연동 상태

| API | 백엔드 통합 | 환경변수 키 | 비고 |
|---|---|---|---|
| 카카오 OAuth | ✅ | `KAKAO_CLIENT_ID/SECRET/ADMIN_KEY` | dev-master 우회 가능 (`code:"DEV_MASTER"`) |
| KMA 단기예보 | ✅ | `KMA_SERVICE_KEY` | 활성화까지 ~30분 지연 가능 |
| PortOne V1 | ✅ | `PORTONE_API_KEY/SECRET/IMP_CODE` | KG 이니시스 정기결제, 테스트 모드 |
| FCM (Android) | ✅ | `FCM_SERVICE_ACCOUNT_JSON` | iOS 푸시는 운영 진입 시 (Apple Developer 가입 필요) |
| AWS Bedrock | mock | `BEDROCK_AGENT_ID/ALIAS_ID` | INFRA-002/003 (별도 작업) |
| 카카오 Local (주소→좌표) | ❌ 제거됨 | — | 프론트 GPS 로 lat/lng 직접 전송 |

---

## 🌿 브랜치 전략

PR 마다 새 브랜치를 만들지 않고, **고정 4개 브랜치**만 사용합니다.

| 브랜치 | 용도 | 머지 대상 |
|---|---|---|
| `main` | 운영(릴리스) 기준. 직접 push 금지 | — |
| `dev` | 일반 개발/통합용 | `main` |
| `feat` | 신규 기능 작업 | `dev` 또는 `main` |
| `hotfix` | 운영 긴급 패치 | `main` (필요 시 `dev` 백포팅) |

### 작업 흐름

```bash
# 1) 작업할 고정 브랜치로 이동 + 최신 동기화
git checkout feat
git pull --ff-only origin feat

# 2) 작업 + 커밋
git add <변경 파일>
git commit -m "feat(SCOPE): 설명"

# 3) push + GitHub 에서 PR 생성 (feat → main)
git push origin feat
# PR 본문은 .github/PULL_REQUEST_TEMPLATE.md 양식 따름
```

임시 `chore/*`, `feature/*`, `fix/*` 브랜치는 만들지 않습니다.

### 커밋 메시지 (Conventional Commits)

```
feat(DIARY-002): 영농일지 작성 API 추가
fix(AI-007): 크레딧 환불 누락 버그 수정
docs(API_SPEC): 명함 블록 토글 엔드포인트 추가
```

타입 화이트리스트: `feat`, `fix`, `refactor`, `chore`, `docs`, `test`, `perf`, `build`, `ci`

---

## 📁 프로젝트 구조

```
src/main/java/kr/farmily/api/
├── ai/                # AI 콘텐츠 생성 (Bedrock 연동)
├── auth/              # 카카오 OAuth + JWT + dev-master 우회
├── common/            # 공통 — ApiResponse, ErrorCode, JWT, S3, Upload
├── crop/              # 작물 CRUD
├── diary/             # 영농일지 CRUD + 캘린더
├── farmlocation/      # 농장 위치 (GPS lat/lng + KMA 격자 자동 변환)
├── notification/      # FCM 푸시 + 알림 설정
├── profile/           # 디지털 명함
├── subscription/      # PortOne 결제 + 구독 + 빌링
├── user/              # 사용자 + 마이페이지
├── weather/           # KMA 단기예보 (TMX/TMN 누락 시 TMP 폴백)
└── FarmilyApplication.java

src/main/resources/
├── application.yml             # 공통 + env placeholder
├── application-local.yml       # 로컬 시크릿 (gitignored)
├── application-local.yml.example
└── db/migration/               # Flyway V1, V2, V3, V4 ...

infra/docker/
├── Dockerfile                  # JDK 21 builder + JRE 21 runtime
└── docker-compose.yml          # backend + postgres + redis + minio
```

---

## 🆘 트러블슈팅

| 증상 | 원인 / 해결 |
|---|---|
| 백엔드 부팅 시 `FCM service account 미설정` 워닝 | `application-local.yml` 의 `farmily.fcm.service-account-json` 비어있음. FCM 푸시 사용 안 할 거면 무시 가능 |
| 카카오 로그인 `KOE006 invalid_grant` | 카카오 콘솔의 Redirect URI 등록값과 요청의 `redirectUri` 불일치. trailing slash / 포트 / 스킴 모두 정확히 일치해야 함 |
| KMA 호출 401 Unauthorized | data.go.kr 활성화 지연 (보통 30분~수시간). 시간 두고 재시도 |
| PortOne 토큰 발급 401 | V2 키 잘못 가져왔을 가능성. 콘솔의 **V1 API 탭** 에서 키 재확인 |
| 한글 payload curl 호출 시 500 | bash 의 cp949 ↔ UTF-8 인코딩 문제. ASCII 페이로드로 테스트 또는 PowerShell 사용 |
| `application-local.yml` 변경 후 도커 반영 안 됨 | `docker compose up -d --build backend` (`--build` 필수, yml 이 jar 안에 패키징되므로) |
| GPS lat/lng 누락 422 | 5/31 PR #21 이후 농장 위치 등록은 `lat`, `lng` 필수. 프론트가 GPS 로 채워야 함 |

---

## 📚 문서

- `docs/API_SPEC.md` — API 명세 (소스 오브 트루스)
- `docs/CONVENTIONS.md` — 코드/PR/커밋 컨벤션
- `docs/DB_SCHEMA.md` — DB 스키마
- `docs/ENV.md` — 환경변수 전체 목록
- `docs/ARCHITECTURE.md` — 아키텍처 개요
- `docs/specs/be/` — 백엔드 spec (BE-AUTH-001 등)
- `docs/MM-DD/WORKLOG*.md` — 일자별 작업 로그

---

## 🚢 배포

TBD (INFRA-001 / INFRA-004 spec 참조). EC2 스모크 → ECS 본 배포 단계로 진행 예정.

---

## 🤝 기여 / 도움

- PR 템플릿: `.github/PULL_REQUEST_TEMPLATE.md`
- 이슈 템플릿: `.github/ISSUE_TEMPLATE/{bug,feature}.md`
- 막힘 / 질문 → 팀 채널에서 공유
