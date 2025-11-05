# /cc:load - 최신 코딩 컨벤션 로드 (2025)

Spring Standards 프로젝트의 **핵심 컨벤션 요약본**을 Serena 메모리에서 빠르게 로드합니다.

**cc** = **C**oding **C**onvention

---

## 🎯 시스템 아키텍처 (중요!)

이 프로젝트는 **3-Tier 컨벤션 로딩 시스템**을 사용합니다:

### Tier 1: Serena Memory (핵심 요약본) ⭐ `/cc:load`
- **10개 Memory 파일** (레이어별 요약본)
- **목적**: 세션 초기화, 전체 레이어 핵심 패턴 빠른 참조
- **로딩 시간**: 5-10초 (10개 파일 순차 로드)
- **내용**: 전체 9개 레이어 (Core, REST API, Domain, Application, Persistence, Testing, Java21, Enterprise, Error-Handling, Orchestration)

### Tier 2: Dynamic Hooks (실시간 자동 주입) 🔥
- **146개 Cache Rules** (98개 규칙 → JSON)
- **목적**: 키워드 감지 → 필요한 규칙만 실시간 주입
- **로딩 시간**: 즉시 (O(1) Cache 검색)
- **내용**: 전체 레이어별 상세 규칙 (Domain, Application, Persistence, REST API 등)

### Tier 3: 원본 문서 (참조용)
- **152개 Markdown 파일** (docs/coding_convention/)
- **목적**: 상세 규칙 참조, 수동 검색
- **로딩 시간**: 불필요 (Hook이 자동 주입)
- **내용**: 전체 규칙 상세 설명

**핵심**: `/cc:load`는 **요약본만** 로드! **전체 규칙**은 **Hook이 자동 주입**합니다!

---

## 🚀 실행 흐름

1. **프로젝트 활성화**: Serena MCP에 프로젝트 등록
2. **핵심 요약본 로드**: 10개 Memory 파일 (전체 9개 레이어 요약본)
3. **세션 준비**: Spring Standards 개발 환경 활성화
4. **Hook 활성화**: 이후 키워드 감지 시 자동으로 상세 규칙 주입 (146개 Cache Rules)

---

## 📝 사용법

```bash
/cc:load
```

---

## 📚 자동 로드되는 메모리 (10개 파일)

### 0️⃣ Core Conventions (2025-11-05) 🏗️
**메모리**: `00-core-conventions-2025`

**포함 내용**:
- ✅ **8개 Zero-Tolerance 규칙** (Lombok, Law of Demeter, Long FK, Transaction, Spring Proxy, Orchestration, Javadoc, Scope)
- ✅ **전체 레이어 통계** (150 파일, 146 cache rules, 9 layers)
- ✅ **아키텍처 원칙** (Hexagonal, DDD, CQRS)
- ✅ **Hook 시스템 개요** (A/B 테스트 메트릭 포함)

### 1️⃣ REST API Layer (2025-11-05) 🌐
**메모리**: `01-adapter-rest-api-rules`

**포함 내용**:
- ✅ **27개 규칙**: Controller 설계, DTO 패턴, Exception 처리, Mapper, Testing
- ✅ **Thin Controller**: 비즈니스 로직 금지
- ✅ **GlobalExceptionHandler**: 중앙 집중식 예외 처리
- ✅ **OpenAPI/Swagger**: API 문서 자동화

### 2️⃣ Domain Layer (2025-11-05) 🏛️
**메모리**: `02-domain-layer-rules`

**포함 내용**:
- ✅ **17개 규칙**: Aggregate 설계, Law of Demeter, Value Objects, Domain Events
- ✅ **Law of Demeter 엄격 적용**: Getter 체이닝 절대 금지
- ✅ **Tell, Don't Ask**: 행동 중심 메서드 설계
- ✅ **Lombok 금지**: Pure Java getter/setter

### 3️⃣ Application Layer (2025-11-05) ⚙️
**메모리**: `03-application-layer-rules`

**포함 내용**:
- ✅ **20개 규칙**: UseCase 설계, Assembler, Transaction 관리, Facade, Manager, Component
- ✅ **Transaction 경계 엄격 관리**: `@Transactional` 내 외부 API 호출 절대 금지
- ✅ **Assembler 패턴**: DTO ↔ Domain 변환
- ✅ **Command/Query 분리**: CQRS 패턴

### 4️⃣ Persistence Layer (2025-11-05) 💾
**메모리**: `04-persistence-layer-rules`

**포함 내용**:
- ✅ **27개 규칙**: JPA Entity 설계, Long FK 전략, QueryDSL, Repository, Configuration
- ✅ **Long FK 전략**: JPA 관계 어노테이션 금지 (`@ManyToOne`, `@OneToMany` 등)
- ✅ **Command/Query Adapter 분리**: Write/Read 분리
- ✅ **N+1 방지**: QueryDSL fetch join 최적화

### 5️⃣ Testing (2025-11-05) 🧪
**메모리**: `05-testing-rules`

**포함 내용**:
- ✅ **14개 규칙**: ArchUnit, Integration Testing (Testcontainers), Multi-Module Testing
- ✅ **ArchUnit**: Layer 의존성, 네이밍 규칙, Annotation 규칙, JPA Entity 규칙 자동 검증
- ✅ **Testcontainers**: Real DB 테스트 환경
- ✅ **Test Fixture + Object Mother**: 테스트 데이터 관리

### 6️⃣ Java 21 Patterns (2025-11-05) ☕
**메모리**: `06-java21-patterns`

**포함 내용**:
- ✅ **15개 규칙**: Record, Sealed Classes, Virtual Threads
- ✅ **Record 패턴**: DTO, Value Object, Command (Compact Constructor)
- ✅ **Sealed Classes**: Domain Modeling, Event Modeling, Result Types
- ✅ **Virtual Threads**: `@Async` + `spring.threads.virtual.enabled=true`

### 7️⃣ Enterprise Patterns (2025-11-05) 🏢
**메모리**: `07-enterprise-patterns`

**포함 내용**:
- ✅ **10개 규칙**: Caching, Event-Driven, Resilience (Circuit Breaker, Retry, Bulkhead)
- ✅ **Domain Events**: `AbstractAggregateRoot.registerEvent()`
- ✅ **Event Sourcing**: Event Store 기반 상태 복원
- ✅ **Circuit Breaker**: Resilience4j 외부 API 장애 격리

### 8️⃣ Error Handling (2025-11-05) ⚠️
**메모리**: `08-error-handling-patterns`

**포함 내용**:
- ✅ **5개 규칙**: Error Handling Strategy, Domain Exception, Global Handler, Error Response, ErrorCode
- ✅ **GlobalExceptionHandler**: `@RestControllerAdvice` 중앙 집중식 예외 처리
- ✅ **Sealed Exception**: Domain Exception 계층 구조
- ✅ **ErrorResponse 표준 포맷**: code, message, details, timestamp

### 9️⃣ Orchestration Patterns (2025-11-05) ⭐ NEW
**메모리**: `09-orchestration-patterns`

**포함 내용**:
- ✅ **11개 규칙**: Command (Record), Idempotency, WAL, Outcome, Orchestrator, Finalizer, Reaper
- ✅ **3-Phase Lifecycle**: WAL → Execution (executeInternal) → Finalization (Finalizer/Reaper)
- ✅ **Idempotency**: IdemKey + `@UniqueConstraint` + Race Condition 방지
- ✅ **Outcome Modeling**: Sealed interface (Ok/Retry/Fail) + Pattern matching
- ✅ **`@Async` 필수**: `executeInternal()`은 비동기, 외부 API 호출은 트랜잭션 밖에서
- ✅ **자동화 성과**: 10개 파일 80-85% 자동 생성, 생성 시간 75% 단축 (8분 → 2분)

---

## 🔧 실행 내용

아래 작업들이 자동으로 수행됩니다:

```python
# 1. Serena 프로젝트 활성화
mcp__serena__activate_project("/Users/sangwon-ryu/claude-spring-standards")

# 2. 사용 가능한 메모리 목록 확인
memories = mcp__serena__list_memories()
# Result: [
#   '00-core-conventions-2025',
#   '01-adapter-rest-api-rules', '02-domain-layer-rules',
#   '03-application-layer-rules', '04-persistence-layer-rules',
#   '05-testing-rules', '06-java21-patterns',
#   '07-enterprise-patterns', '08-error-handling-patterns',
#   '09-orchestration-patterns'
# ]

# 3. 전체 10개 Memory 파일 로드 (순차 로드, 5-10초 소요)
core_conventions = mcp__serena__read_memory("00-core-conventions-2025")
rest_api_rules = mcp__serena__read_memory("01-adapter-rest-api-rules")
domain_rules = mcp__serena__read_memory("02-domain-layer-rules")
application_rules = mcp__serena__read_memory("03-application-layer-rules")
persistence_rules = mcp__serena__read_memory("04-persistence-layer-rules")
testing_rules = mcp__serena__read_memory("05-testing-rules")
java21_patterns = mcp__serena__read_memory("06-java21-patterns")
enterprise_patterns = mcp__serena__read_memory("07-enterprise-patterns")
error_handling = mcp__serena__read_memory("08-error-handling-patterns")
orchestration_patterns = mcp__serena__read_memory("09-orchestration-patterns")

# 4. 세션 컨텍스트 확인 및 복원
onboarding_status = mcp__serena__check_onboarding_performed()
```

---

## ✅ 출력 예시

```
🚀 프로젝트 활성화: claude-spring-standards
✅ 사용 가능한 메모리: 10개

📚 전체 레이어 코딩 컨벤션 로드 완료 (5-10초 소요):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

0️⃣ Core Conventions (8 Zero-Tolerance, 전체 레이어 통계)
1️⃣ REST API Layer (27 규칙) - Controller, DTO, Exception, Mapper
2️⃣ Domain Layer (17 규칙) - Aggregate, Law of Demeter, Value Objects
3️⃣ Application Layer (20 규칙) - UseCase, Transaction, Assembler, Facade
4️⃣ Persistence Layer (27 규칙) - JPA Entity, Long FK, QueryDSL, Repository
5️⃣ Testing (14 규칙) - ArchUnit, Testcontainers, Multi-Module
6️⃣ Java 21 Patterns (15 규칙) - Record, Sealed Classes, Virtual Threads
7️⃣ Enterprise Patterns (10 규칙) - Caching, Event-Driven, Resilience
8️⃣ Error Handling (5 규칙) - Domain Exception, Global Handler, ErrorCode
9️⃣ Orchestration Patterns (11 규칙) - Command, Idempotency, WAL, Outcome ⭐ NEW

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

🎯 Zero-Tolerance 규칙 (8개, 전체 레이어):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

❌ 절대 금지:
1. Lombok 사용 (전체 레이어) - @Data, @Builder, @Getter, @Setter 등 모두 금지
2. Law of Demeter 위반 (Domain) - order.getCustomer().getAddress() 금지
3. JPA 관계 어노테이션 (Persistence) - @ManyToOne, @OneToMany 등 금지
4. @Transactional 내 외부 API (Application) - RestTemplate, WebClient 금지
5. Private/Final 메서드에 @Transactional (Application) - Spring Proxy 제약
6. Orchestrator에서 @Transactional (Orchestration) - @Async 필수
7. Command에 Lombok (Orchestration) - Record 패턴 사용
8. Javadoc 누락 (전체 레이어) - public 클래스/메서드 필수

✅ 반드시 준수:
1. Pure Java getter/setter 직접 작성
2. Tell, Don't Ask 원칙 (order.getCustomerZipCode())
3. Long FK 전략 (private Long userId;)
4. 트랜잭션 경계 분리 (Outbox Pattern 사용)
5. Assembler를 통한 DTO ↔ Domain 변환
6. Record 패턴으로 Command/Value Object 정의
7. Outcome (Ok/Retry/Fail) 반환

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📊 Hook 시스템 자동 주입 (사용자 개입 불필요):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

✅ 146개 Cache Rules → 키워드 감지 시 자동 주입 (O(1) 검색)
✅ 토큰 사용량: 90% 절감 (전체 로드 vs Hook 주입)
✅ 검증 속도: 73.6% 향상 (Cache O(1) 검색)
✅ 컨벤션 위반: 40회 → 0회 (A/B 테스트 검증 완료)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

---

## 🎯 다음 단계

세션 로드 후 다음 작업을 수행할 수 있습니다:

### 1️⃣ 코드 생성 (Claude Code 직접 요청)

```bash
# 예시: Domain Aggregate 요청
"Order Aggregate를 생성해줘. 주문 생성, 취소, 상태 변경 기능이 필요해."

# 예시: Application UseCase 요청
"PlaceOrderUseCase를 생성해줘. Order를 생성하고 저장하는 로직이 필요해."

# 예시: REST Controller 요청
"OrderController를 생성해줘. POST /orders, GET /orders/{id} API가 필요해."

# Orchestration Pattern 생성 (실제 Slash Command)
/code-gen-orchestrator Order PlacementConfirmed
```

**Hook 시스템이 자동 적용**:
- ✅ 키워드 감지 ("domain", "usecase", "controller") → 해당 레이어 규칙 자동 주입
- ✅ Plain Java Constructor (Lombok 없음)
- ✅ Law of Demeter (Tell, Don't Ask)
- ✅ Long FK 전략
- ✅ Transaction 경계 분리
- ✅ Assembler 패턴

### 2️⃣ 코드 검증

```bash
# Domain layer 검증
/validate-domain domain/src/.../Order.java

# 전체 아키텍처 검증 (ArchUnit)
/validate-architecture

# 특정 모듈 검증
/validate-architecture application
```

### 3️⃣ AI 리뷰

```bash
# 통합 AI 리뷰 (Gemini + CodeRabbit + Codex)
/ai-review [pr-number]

# Jira Task 분석
/jira-task
```

### 4️⃣ 분석

```bash
# 모듈 분석
/sc:analyze adapter-out/persistence-mysql

# 아키텍처 설계
/sc:design business-model
```

---

## 📦 패키지 구조 (최신)

로드된 컨벤션을 따르면 다음과 같은 구조가 생성됩니다:

```
application/
├── order/
│   ├── port/
│   │   ├── in/                     # UseCase 인터페이스
│   │   └── out/                    # Port 인터페이스
│   ├── service/                    # UseCase 구현체 (접미사: Service)
│   ├── manager/                    # ⭐ 상태 관리 및 조율 계층 (NEW)
│   │   ├── OrderStateManager.java      # 단일 Context 상태 관리
│   │   ├── PaymentStateManager.java
│   │   ├── OutboxStateManager.java     # Outbox 상태 관리
│   │   └── OrderPaymentManager.java    # 2-3개 StateManager 조율
│   ├── facade/                     # 복잡한 워크플로우
│   │   └── CheckoutFacade.java
│   ├── assembler/                  # DTO ↔ Domain 변환
│   └── dto/
│       ├── command/                # Command DTO (Write)
│       ├── query/                  # Query DTO (Read)
│       └── result/                 # Response DTO
```

---

## 🔄 계층 구조 (최신)

```
UseCase Service (접미사: Service)
    ↓ 의존
Facade (복잡한 워크플로우)
    ↓ 의존
Manager (2-3개 StateManager 조율)
    ↓ 의존
StateManager (단일 Bounded Context 상태 관리)
    ↓ 의존
Port (Out)
```

---

## 📊 Hook 시스템 통합 (전체 규칙 자동 로딩)

`/cc:load` 실행 후, **사용자가 코드를 작성할 때** 자동으로 작동:

### 실시간 규칙 주입 (Dynamic Hooks)
```
사용자: "domain aggregate 작업"
    ↓
Hook: "domain" 키워드 감지 (30점)
    ↓
Cache: Domain Layer 규칙 15개 자동 주입 (O(1) 검색)
    ↓
Claude: Domain Layer 규칙 100% 준수 코드 생성
```

**자동으로 주입되는 규칙**:
- ✅ **Domain Layer**: Law of Demeter, Lombok 금지, Aggregate 설계 등 (15개 규칙)
- ✅ **Application Layer**: Transaction 경계, Manager 패턴, Assembler 등 (18개 규칙)
- ✅ **Persistence Layer**: Long FK 전략, QueryDSL, N+1 방지 등 (10개 규칙)
- ✅ **REST API Layer**: Controller 설계, DTO 패턴, Exception 처리 등 (18개 규칙)
- ✅ **Orchestration**: Outbox Pattern, Idempotency, WAL 등 (8개 규칙)

**총 146개 Cache Rules → 필요한 규칙만 실시간 주입!**

**성능 (A/B 테스트 검증)**:
- 컨벤션 위반: 40회 → 0회 (100% 제거) ✅
- 토큰 사용량: 90% 절감 (전체 로드 vs Hook 주입)
- 검증 속도: 73.6% 향상 (Cache O(1) 검색)

---

## 🧪 LangFuse 통합 (선택 사항)

LangFuse로 세션 메트릭을 추적하려면 다음 환경 변수를 설정하세요:

```bash
export LANGFUSE_PUBLIC_KEY="pk-lf-..."
export LANGFUSE_SECRET_KEY="sk-lf-..."
export LANGFUSE_HOST="https://us.cloud.langfuse.com"
```

**추적되는 메트릭**:
- 메모리 로드 시간
- 컨벤션 참조 횟수
- 토큰 사용량
- 컨벤션 위반 건수
- Manager/StateManager 패턴 적용 횟수

---

## 📚 참고 문서

### 최신 Skills 문서
- `.claude/skills/application-expert/SKILL.md` - Application Layer 전문가 가이드
- `.claude/skills/domain-expert/SKILL.md` - Domain Layer 전문가 가이드

### 코딩 컨벤션 문서
- `docs/coding_convention/03-application-layer/` - Application Layer 상세 규칙 (18개)
- `docs/coding_convention/09-orchestration-patterns/` - Orchestration Pattern (8개)

### Hook 시스템
- `.claude/hooks/user-prompt-submit.sh` - 키워드 감지 및 규칙 자동 주입
- `.claude/hooks/after-tool-use.sh` - 실시간 검증
- `.claude/hooks/scripts/build-rule-cache.py` - Cache 빌드 도구

---

## 💡 중요 참고사항

### 📌 핵심 개념

**Q: `/cc:load`는 3개만 로드하는데 전체 규칙은?**
A: **Hook이 자동으로 146개 규칙 주입!** 수동 로드 불필요 ✅

**Q: 152개 마크다운 파일은 언제 읽어?**
A: **읽지 않음!** Cache 시스템이 이미 JSON으로 변환 완료 ✅

**Q: 느린 이유는?**
A: **Hook이 실시간 주입 중**. 정상 동작! (3-5초) ✅

### 🎯 사용 가이드

1. **이 명령어는 세션 시작 시 한 번만 실행**하면 됩니다
2. **Serena 메모리는 세션 간 지속**되므로 재로드 불필요
3. **전체 146개 규칙은 Hook이 자동 주입** (사용자 개입 불필요)
4. **실시간 검증**이 자동으로 실행되어 위반 시 즉시 경고

### ⚡ 성능 최적화

**로딩 시간**:
- **Serena Memory 로드**: 3-5초 (3개 요약본 순차 로드)
- **Cache Rules 주입**: 즉시 (Hook이 O(1) 검색으로 자동 주입)
- **총 예상 시간**: 5초 이내

**3-Tier 시스템의 장점**:
- ✅ **Tier 1 (요약본)**: 3-5초 로드 vs 전체 로드 시 30초+
- ✅ **Tier 2 (Hook 주입)**: 필요한 규칙만 주입 → 토큰 90% 절감
- ✅ **Tier 3 (원본 문서)**: 참조용으로만 사용 → 로드 불필요

**권장 워크플로우**:
```bash
# 1. 세션 시작 시 한 번만 실행
/cc:load

# 2. 로딩 완료 대기 (3-5초)
# ...

# 3. 작업 시작 (Hook이 자동으로 규칙 주입)
/code-gen-domain Order
# → Hook이 자동으로 Domain Layer 15개 규칙 주입!
```

---

**✅ 이 명령어는 2025-11-05 기준 최신 컨벤션을 로드합니다.**

**🔥 전체 9개 레이어 (REST API, Domain, Application, Persistence, Testing, Java21, Enterprise, Error-Handling, Orchestration) 규칙이 모두 포함되어 있습니다!**

**📊 총 146개 Cache Rules, 10개 Memory 요약본, A/B 테스트 검증 완료 (40 violations → 0 violations)!**
