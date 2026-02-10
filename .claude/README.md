# Claude Code Command System

이 프로젝트에서 사용하는 Claude Code 커맨드, 에이전트, 파이프라인에 대한 가이드입니다.

---

## 디렉토리 구조

```
.claude/
├── CLAUDE.md                  # 프로젝트 설정 (아키텍처, MCP, Zero-Tolerance)
├── README.md                  # 이 파일 (커맨드 시스템 가이드)
├── commands/                  # 사용자가 /command-name 으로 호출하는 커맨드
│   ├── api-endpoints.md
│   ├── api-flow.md
│   ├── test-scenario.md
│   ├── test-e2e.md
│   ├── test-api.md
│   ├── test-repository.md
│   ├── legacy-endpoints.md
│   ├── legacy-flow.md
│   ├── legacy-convert.md
│   ├── legacy-query.md
│   ├── legacy-service.md
│   ├── legacy-controller.md
│   ├── epic.md
│   ├── plan.md
│   ├── design.md
│   ├── work.md
│   ├── next.md
│   ├── ship.md
│   ├── check.md
│   ├── review.md
│   ├── recovery.md
│   └── ...
├── agents/                    # 커맨드가 위임하는 전문가 에이전트 프로필
│   ├── api-endpoints-analyzer.md
│   ├── api-flow-analyzer.md
│   ├── test-scenario-designer.md
│   ├── e2e-test-generator.md
│   ├── api-tester.md
│   ├── repository-tester.md
│   ├── legacy-endpoints-analyzer.md
│   ├── legacy-flow-analyzer.md
│   └── ...
├── rules/                     # 프로젝트 규칙
│   └── convention-guide.md
├── plans/                     # 작업 계획 문서
└── skills/                    # 스킬 정의
```

### 커맨드 vs 에이전트

| 구분 | 위치 | 역할 | 호출 방법 |
|------|------|------|-----------|
| **커맨드** | `commands/*.md` | 사용자 인터페이스. 사용법, 옵션, 출력 형식 정의 | `/command-name args` |
| **에이전트** | `agents/*.md` | 실행 로직. Phase별 도구 사용법, 파싱 규칙 정의 | 커맨드가 내부적으로 위임 |

커맨드는 "무엇을 하는가"를, 에이전트는 "어떻게 하는가"를 정의합니다.

---

## 파이프라인 개요

이 프로젝트의 커맨드들은 **4개 파이프라인**으로 구성됩니다.

```
┌──────────────────────────────────────────────────────────────┐
│  1. E2E Test Pipeline        (엔드포인트 → 통합 테스트)       │
│  2. Legacy Migration Pipeline (레거시 → 새 아키텍처)          │
│  3. Unit Test Pipeline       (모듈 → 단위 테스트)             │
│  4. Development Pipeline     (기획 → 구현 → 배포)            │
└──────────────────────────────────────────────────────────────┘
```

---

## 1. E2E Test Pipeline

엔드포인트를 분석하고 E2E 통합 테스트를 자동 생성하는 파이프라인입니다.

### 흐름도

```
/api-endpoints          /api-flow              /test-scenario         /test-e2e
─────────────          ─────────              ──────────────         ─────────
엔드포인트 분류    →    플로우 분석        →    시나리오 설계     →    코드 생성
(Query/Command)        (4-Layer 추적)         (케이스 도출)          (RestAssured)
     │                      │                      │                     │
     ▼                      ▼                      ▼                     ▼
 claudedocs/            claudedocs/            claudedocs/           integration-test/
 api-endpoints/         api-flows/             test-scenarios/       src/test/.../e2e/
```

### 단계별 사용법

#### Step 1: 엔드포인트 분류

```bash
/api-endpoints admin:seller
/api-endpoints web:product
```

대상 모듈의 Controller를 탐색하여 모든 엔드포인트를 Query(GET)와 Command(POST/PUT/PATCH/DELETE)로 분류합니다.

**입력**: 모듈명 (접두사로 admin/web 구분)
**출력**: `claudedocs/api-endpoints/{admin|web}/{module}_endpoints.md`

출력 예시:
```
📊 요약: Query 3개, Command 4개

📖 Query:
  GET /v2/admin/sellers           → searchSellers
  GET /v2/admin/sellers/{id}      → getSellerDetail

✏️ Command:
  POST /v2/admin/sellers          → createSeller
  PUT  /v2/admin/sellers/{id}     → updateSeller
```

#### Step 2: 플로우 분석

```bash
# 단일 엔드포인트
/api-flow admin:SellerQueryController.searchSellers

# 모듈 전체 (Step 1 결과 기반)
/api-flow admin:seller --all

# Query만
/api-flow admin:seller --query-only
```

Hexagonal 아키텍처 레이어별로 호출 흐름을 추적합니다:
```
Controller → ApiMapper → UseCase(Port) → Service → Domain Port → Adapter → Repository
```

**입력**: Controller.method 또는 모듈명(--all)
**출력**: `claudedocs/api-flows/{admin|web}/{Controller}_{method}.md`

추적 항목:
- Adapter-In: Request/Response DTO 구조, ApiMapper 변환 로직
- Application: UseCase 인터페이스, Service 구현, 트랜잭션 경계
- Domain: Domain Port, Aggregate, 비즈니스 규칙
- Adapter-Out: JPA Repository, QueryDSL 쿼리, Entity 매핑

#### Step 3: 테스트 시나리오 설계

```bash
/test-scenario admin:seller
/test-scenario admin:seller --p0-only      # 필수 시나리오만
/test-scenario admin:seller --query-only   # Query만
```

Step 1, 2의 분석 결과를 기반으로 테스트 시나리오를 자동 설계합니다.

**입력**: api-endpoints 문서 (필수), api-flow 문서 (권장)
**출력**: `claudedocs/test-scenarios/{admin|web}/{module}_scenarios.md`

설계되는 시나리오 유형:

| 유형 | 예시 | 우선순위 |
|------|------|----------|
| 정상 조회 | 데이터 있을 때 목록 반환 | P0 |
| 빈 결과 | 데이터 없을 때 빈 목록 | P0 |
| 상세 조회 성공 | ID로 단건 조회 | P0 |
| 리소스 없음 | 없는 ID → 404 | P0 |
| 생성 성공 + DB 검증 | POST → 201 → DB 확인 | P0 |
| Validation 실패 | 필수 필드 누락 → 400 | P0 |
| 검색 필터 | 조건별 필터링 | P1 |
| 페이징 | page/size 동작 | P1 |
| 중복 생성 | 비즈니스 규칙 → 409 | P1 |
| CRUD 전체 플로우 | 생성→조회→수정→삭제 | P0 |
| 상태 전이 플로우 | 생성→승인→상태확인 | P0 |

Fixture 요구사항도 함께 설계합니다:
- 필요 Repository 목록
- testFixtures 클래스
- setUp/tearDown 방법

#### Step 4: E2E 테스트 코드 생성

```bash
/test-e2e admin:seller
/test-e2e admin:seller --no-run    # 생성만 (실행 안 함)
/test-e2e admin:seller --dry-run   # 미리보기만
```

Step 3의 시나리오를 기반으로 실제 E2E 테스트 코드를 생성합니다.

**입력**: test-scenarios 문서
**출력**: `integration-test/src/test/.../e2e/{admin|web}/{domain}/{Domain}E2ETest.java`

생성 코드 특징:
- `AdminE2ETestBase` / `E2ETestBase` 상속
- RestAssured 패턴 (`givenAdmin()`, `givenAuthenticated()`)
- `@Nested` + `@DisplayName` 구조
- `@Tag(TestTags.{DOMAIN})` 도메인 태그
- DB 검증 (`repository.findById()`)
- Helper 메서드 (createRequest, updateRequest)

### 전체 예시

```bash
# seller 도메인 E2E 테스트 전체 생성
/api-endpoints admin:seller           # → seller_endpoints.md
/api-flow admin:seller --all          # → seller_all_flows.md
/test-scenario admin:seller           # → seller_scenarios.md
/test-e2e admin:seller                # → SellerAdminE2ETest.java + 실행

# 특정 엔드포인트만 분석 후 테스트
/api-flow admin:SellerQueryController.searchSellers
/test-scenario admin:seller --endpoints "searchSellers"
/test-e2e admin:seller --query-only
```

### 옵션 정리

| 옵션 | 사용 가능 커맨드 | 설명 |
|------|-----------------|------|
| `--all` | api-flow | 모듈 전체 엔드포인트 분석 |
| `--query-only` | api-flow, test-scenario, test-e2e | Query만 대상 |
| `--command-only` | api-flow, test-scenario, test-e2e | Command만 대상 |
| `--endpoints "a,b"` | test-scenario | 특정 엔드포인트만 |
| `--p0-only` | test-scenario | 필수 시나리오만 |
| `--no-run` | test-e2e | 테스트 실행 안 함 |
| `--dry-run` | test-e2e | 파일 생성 없이 미리보기 |
| `--no-db` | api-flow | DB 쿼리 분석 생략 |
| `--save-memory` | api-endpoints, api-flow, test-scenario | Serena memory 저장 |

### 산출물 구조

```
claudedocs/
├── api-endpoints/
│   ├── admin/
│   │   ├── seller_endpoints.md
│   │   └── sellerapplication_endpoints.md
│   └── web/
│       └── product_endpoints.md
├── api-flows/
│   ├── admin/
│   │   ├── SellerQueryController_searchSellers.md
│   │   ├── SellerCommandController_createSeller.md
│   │   └── seller_all_flows.md
│   └── web/
│       └── ProductQueryController_getProduct.md
└── test-scenarios/
    ├── admin/
    │   └── seller_scenarios.md
    └── web/
        └── product_scenarios.md

integration-test/src/test/java/.../e2e/
├── admin/
│   ├── seller/
│   │   └── SellerAdminE2ETest.java
│   └── sellerapplication/
│       └── SellerApplicationAdminE2ETest.java
└── web/
    └── product/
        └── ProductE2ETest.java
```

---

## 2. Legacy Migration Pipeline

레거시 시스템의 API를 분석하여 새 Hexagonal 아키텍처로 마이그레이션하는 파이프라인입니다.

### 흐름도

```
/legacy-endpoints    /legacy-flow      /legacy-convert    /legacy-query     /legacy-service    /legacy-controller
────────────────    ────────────      ───────────────    ─────────────     ───────────────    ──────────────────
엔드포인트 분류  →  흐름 문서화   →   DTO 변환       →  Persistence    →  Application    →  Adapter-In
                                      (record 타입)     Layer 생성        Layer 생성        Layer 생성
```

### 사용법

```bash
# 1. 엔드포인트 분류
/legacy-endpoints admin:product

# 2. 특정 엔드포인트 흐름 분석
/legacy-flow admin:ProductController.fetchProductGroups

# 3. Request/Response DTO 변환 (record 타입)
/legacy-convert admin:ProductController.fetchProductGroups

# 4. Persistence Layer 생성 (QueryDSL Repository)
/legacy-query admin:ProductController.fetchProductGroups

# 5. Application Layer 생성 (Port, Service, Manager)
/legacy-service admin:ProductController.fetchProductGroups

# 6. Controller + ApiMapper 생성
/legacy-controller admin:ProductController.fetchProductGroups
```

각 단계는 이전 단계의 산출물에 의존합니다. 반드시 순서대로 실행하세요.

---

## 3. Unit Test Pipeline

모듈별 단위 테스트를 생성하는 파이프라인입니다.

### 커맨드

| 커맨드 | 대상 | 생성물 |
|--------|------|--------|
| `/test-api` | adapter-in 모듈 | Fixtures + MapperTest + RestDocsTest |
| `/test-repository` | adapter-out 모듈 | Fixtures + RepositoryTest |

### /test-api 사용법

```bash
/test-api rest-api-admin selleradmin         # Admin API 전체
/test-api rest-api-admin v2/seller           # 버전 명시
/test-api rest-api brand                     # Public API
/test-api rest-api-admin auth --mapper-only  # Mapper 테스트만
/test-api rest-api-admin category --restdocs-only  # RestDocs만
```

생성 파일:
- `testFixtures/{Domain}ApiFixtures.java`
- `test/.../mapper/{Domain}QueryApiMapperTest.java`
- `test/.../mapper/{Domain}CommandApiMapperTest.java`
- `test/.../controller/{Domain}QueryControllerRestDocsTest.java`
- `test/.../controller/{Domain}CommandControllerRestDocsTest.java`

### /test-repository 사용법

```bash
/test-repository seller              # seller 도메인
/test-repository sellerapplication   # sellerapplication 도메인
```

### /test-api vs /test-e2e 차이

| 항목 | /test-api | /test-e2e |
|------|-----------|-----------|
| 테스트 유형 | 단위 테스트 | 통합 테스트 (E2E) |
| 프레임워크 | MockMvc | RestAssured |
| DB | Mock (@MockBean) | 실제 DB (H2) |
| 컨텍스트 | @WebMvcTest (슬라이스) | @SpringBootTest (전체) |
| 의존성 | UseCase, Mapper Mock | 전체 스택 |
| 목적 | API 스펙 문서화 (RestDocs) | 전체 플로우 검증 |
| 속도 | 빠름 (1-3초) | 느림 (5-30초) |

---

## 4. Development Pipeline

기획부터 배포까지의 개발 워크플로우 커맨드입니다.

### 흐름도

```
/epic → /plan → /design → /work → /next → /check → /review → /ship
```

| 커맨드 | 역할 |
|--------|------|
| `/epic` | Epic 기획 + Task 분해 |
| `/plan` | 기능 분석 및 구현 계획 수립 |
| `/design` | 컴포넌트 설계 및 체크리스트 생성 |
| `/work` | Epic 작업 시작 (브랜치 생성, Task 큐 로드) |
| `/next` | 현재 Task 완료 → 다음 Task로 이동 |
| `/check` | 정적 분석 (Checkstyle, PMD, SpotBugs, ArchUnit) |
| `/review` | 코드 리뷰 (Knowledge Base 기반 컨벤션 검토) |
| `/ship` | Epic 완료 (커밋 정리, PR 생성) |
| `/recovery` | 중단된 작업 재개 |

---

## 커맨드 전체 카탈로그

### E2E Test Pipeline

| 커맨드 | 설명 | 에이전트 |
|--------|------|----------|
| `/api-endpoints` | 엔드포인트 Query/Command 분류 | api-endpoints-analyzer |
| `/api-flow` | Hexagonal 레이어별 플로우 추적 | api-flow-analyzer |
| `/test-scenario` | E2E 테스트 시나리오 설계 | test-scenario-designer |
| `/test-e2e` | E2E 테스트 코드 생성 + 실행 | e2e-test-generator |

### Legacy Migration Pipeline

| 커맨드 | 설명 | 에이전트 |
|--------|------|----------|
| `/legacy-endpoints` | 레거시 엔드포인트 분류 | legacy-endpoints-analyzer |
| `/legacy-flow` | 레거시 API 흐름 문서화 | legacy-flow-analyzer |
| `/legacy-convert` | DTO 변환 (record 타입) | legacy-dto-converter |
| `/legacy-query` | Persistence Layer 생성 | legacy-query-generator |
| `/legacy-service` | Application Layer 생성 | legacy-service-generator |
| `/legacy-controller` | Controller Layer 생성 | legacy-controller-generator |

### Unit Test Pipeline

| 커맨드 | 설명 | 에이전트 |
|--------|------|----------|
| `/test-api` | RestDocs + Mapper 단위 테스트 | api-tester |
| `/test-repository` | Repository 통합 테스트 | repository-tester |

### Development Pipeline

| 커맨드 | 설명 | 에이전트 |
|--------|------|----------|
| `/epic` | Epic 기획 + Task 분해 | - |
| `/plan` | 구현 계획 수립 | planner |
| `/design` | 컴포넌트 설계 | - |
| `/work` | 작업 시작 | - |
| `/next` | 다음 Task 이동 | - |
| `/check` | 정적 분석 | - |
| `/review` | 코드 리뷰 | reviewer |
| `/ship` | Epic 완료 + PR | shipper |
| `/recovery` | 작업 재개 | - |

### 기타

| 커맨드 | 설명 |
|--------|------|
| `/create-prd` | 대화형 PRD 생성 |
| `/jira-create` | Jira Epic/Task 등록 |
| `/jira-fetch` | Jira 정보 가져오기 |

---

## 접두사 규칙

여러 커맨드에서 사용하는 공통 접두사 규칙:

| 접두사 | 대상 | 예시 |
|--------|------|------|
| `admin:` | Admin API (`adapter-in/rest-api-admin`) | `/api-endpoints admin:seller` |
| `web:` | Public API (`adapter-in/rest-api`) | `/api-endpoints web:product` |
| (없음) | 기본값 admin | `/api-endpoints seller` = `admin:seller` |

---

## 다른 프로젝트에 적용하기

이 커맨드 시스템을 다른 프로젝트에 적용하려면:

### 1. 디렉토리 구조 복사

```bash
# .claude 디렉토리 복사
cp -r .claude/commands/ your-project/.claude/commands/
cp -r .claude/agents/ your-project/.claude/agents/
```

### 2. 프로젝트 구조에 맞게 수정

커맨드와 에이전트에서 수정이 필요한 부분:

| 항목 | 현재 프로젝트 값 | 수정 필요 |
|------|-----------------|-----------|
| 모듈 경로 | `adapter-in/rest-api[-admin]` | 프로젝트의 Controller 모듈 경로 |
| Base Class | `AdminE2ETestBase`, `E2ETestBase` | 프로젝트의 테스트 Base Class |
| RestAssured 메서드 | `givenAdmin()`, `givenAuthenticated()` | 프로젝트의 인증 헬퍼 |
| 테스트 경로 | `integration-test/src/test/` | 프로젝트의 테스트 모듈 경로 |
| 패키지명 | `com.ryuqq.setof` | 프로젝트 패키지명 |
| 문서 경로 | `claudedocs/` | 원하는 문서 저장 경로 |
| TestTags | `TestTags.SELLER` 등 | 프로젝트의 테스트 태그 |
| testFixtures | `*JpaEntityFixtures` | 프로젝트의 Fixture 패턴 |

### 3. 필수 인프라

E2E Test Pipeline 사용을 위해 필요한 인프라:

```
프로젝트/
├── integration-test/                    # 통합 테스트 모듈
│   ├── build.gradle                     # REST Assured, H2 의존성
│   └── src/test/
│       ├── java/
│       │   └── common/
│       │       ├── base/
│       │       │   ├── E2ETestBase.java       # Public API 베이스
│       │       │   └── AdminE2ETestBase.java  # Admin API 베이스
│       │       └── tag/
│       │           └── TestTags.java          # 태그 상수
│       └── resources/
│           └── application-test.yml           # H2 테스트 설정
└── claudedocs/                          # 분석 문서 저장 (gitignore 가능)
```

### 4. 핵심 의존성

```gradle
// integration-test/build.gradle
dependencies {
    testImplementation 'io.rest-assured:rest-assured'
    testImplementation 'com.h2database:h2'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

---

## 관련 문서

| 문서 | 위치 | 설명 |
|------|------|------|
| 프로젝트 설정 | `.claude/CLAUDE.md` | 아키텍처, MCP, Zero-Tolerance 규칙 |
| 컨벤션 가이드 | `.claude/rules/convention-guide.md` | 코딩 컨벤션 규칙 조회 방법 |
| 통합 테스트 가이드 | `docs/INTEGRATION_TEST_GUIDE.md` | 테스트 인프라 상세 (Base Class, Gradle Task, CI) |
