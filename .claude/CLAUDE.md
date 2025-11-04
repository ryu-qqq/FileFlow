# Spring Standards Project - Claude Code Configuration

이 프로젝트는 **Spring Boot 3.5.x + Java 21** 기반의 헥사고날 아키텍처 엔터프라이즈 표준 프로젝트입니다.

---

## 🚀 혁신: Dynamic Hooks + Cache 시스템 (100% Zero-Tolerance 달성)

이 프로젝트의 핵심 차별점은 **검증된 자동화 시스템**입니다:

**A/B 테스트 검증 완료** (2025-11-04):
- **Hook ON**: 0 violations (100% Zero-Tolerance) ✅
- **Hook OFF**: 40 violations (0% Zero-Tolerance) ❌
- **효과**: 컨벤션 자동 준수율 100% 달성

### 시스템 아키텍처

```
docs/coding_convention/ (98개 마크다운 규칙)
         ↓
build-rule-cache.py (Cache 빌드)
         ↓
.claude/cache/rules/ (98개 JSON + index.json)
         ↓
user-prompt-submit.sh (Hook 실행)
         ├─ 키워드 감지: "domain", "entity", "usecase" 등
         ├─ Layer 매핑: domain, application, persistence, adapter-rest
         └─ inject-rules.py: Layer별 규칙 자동 주입
         ↓
Claude Code (규칙 준수 코드 생성)
         - Cache 규칙 기반 코드 생성 (100% 준수)
         ↓
after-tool-use.sh (생성 직후 검증)
         ├─ Write/Edit 도구 감지
         └─ validation-helper.py 자동 호출
         ↓
validation-helper.py (실시간 검증)
         ├─ Cache 기반 고속 검증 (148ms)
         ├─ Zero-Tolerance 규칙 체크
         └─ 위반 시 구체적 수정 방법 제시
         ↓
LangFuse (효율 측정)
         ├─ Hook 실행 로그
         ├─ 토큰 사용량 추적
         └─ 위반 건수 모니터링
```

### 성능 메트릭 (A/B 테스트 검증 완료)

| 메트릭 | 기존 방식 | Hook OFF | Hook ON | 최종 개선율 |
|--------|----------|----------|---------|-------------|
| **컨벤션 위반** | - | 40회 | **0회** | **100% 제거** |
| 토큰 사용량 | 50,000 | - | 500-1,000 | **90% 절감** |
| 검증 속도 | 561ms | - | 148ms | **73.6% 향상** |
| 문서 로딩 | 2-3초 | - | <100ms | **95% 향상** |
| Zero-Tolerance 준수율 | - | 0% | **100%** | **완벽 달성** |

**핵심 성과**: Hook 시스템이 활성화되면 컨벤션 위반이 **40회 → 0회**로 감소하여 **100% Zero-Tolerance** 달성

### LangFuse 통합 (메트릭 추적)

**목적**: Claude Code 로그를 LangFuse로 전송하여 개발 효율 측정

**파이프라인**:
1. `scripts/langfuse/aggregate-logs.py` - 로그 집계 및 변환
2. `scripts/langfuse/upload-to-langfuse.py` - LangFuse Ingestion API 업로드

**추적 메트릭**:
- Traces: Claude Code 세션별 추적
- Observations: Hook 실행, IDE 통합 작업
- 토큰 사용량, 실행 시간, 위반 건수

**사용법**:
```bash
# 환경 변수 설정
export LANGFUSE_PUBLIC_KEY="pk-lf-..."
export LANGFUSE_SECRET_KEY="sk-lf-..."
export LANGFUSE_HOST="https://us.cloud.langfuse.com"

# Claude Code 워크플로우 사용
/upload-langfuse

# 또는 직접 실행
bash tools/pipeline/upload_langfuse.sh
```

**참고**: LangFuse Python SDK는 필요 없음. `requests` 라이브러리만 사용.

---

## 📚 코딩 규칙 (docs/coding_convention/)

### 레이어별 규칙 구조

```
docs/coding_convention/
├── 01-adapter-rest-api-layer/  (18개 규칙)
│   ├── controller-design/
│   ├── dto-patterns/
│   ├── exception-handling/
│   ├── mapper-patterns/
│   ├── package-guide/
│   └── testing/
│
├── 02-domain-layer/  (15개 규칙)
│   ├── aggregate-design/
│   ├── law-of-demeter/  ⭐ Law of Demeter 엄격 적용
│   ├── package-guide/
│   └── testing/
│
├── 03-application-layer/  (18개 규칙)
│   ├── assembler-pattern/
│   ├── dto-patterns/
│   ├── package-guide/
│   ├── testing/
│   ├── transaction-management/  ⭐ Transaction 경계 엄격 관리
│   └── usecase-design/
│
├── 04-persistence-layer/  (10개 규칙)
│   ├── jpa-entity-design/  ⭐ Long FK 전략 (관계 어노테이션 금지)
│   ├── package-guide/
│   ├── querydsl-optimization/
│   ├── repository-patterns/
│   └── testing/
│
├── 05-testing/  (12개 규칙)
│   ├── archunit-rules/
│   └── integration-testing/
│
├── 06-java21-patterns/  (8개 규칙)
│   ├── record-patterns/
│   ├── sealed-classes/
│   └── virtual-threads/
│
├── 07-enterprise-patterns/  (5개 규칙)
│   ├── caching/
│   ├── event-driven/
│   └── resilience/
│
├── 08-error-handling/  (5개 규칙)
│   ├── error-handling-strategy/
│   ├── domain-exception-design/
│   ├── global-exception-handler/
│   ├── error-response-format/
│   └── errorcode-management/
│
└── 09-orchestration-patterns/  (8개 규칙) ⭐ NEW
    ├── overview/  (3-Phase Lifecycle, Idempotency, WAL)
    ├── command-pattern/  (Record 패턴, Compact Constructor)
    ├── idempotency-handling/  (IdemKey, Race Condition 방지)
    ├── write-ahead-log/  (크래시 복구, Finalizer/Reaper)
    ├── outcome-modeling/  (Sealed interface, Pattern matching)
    ├── quick-start-guide/  (10분 실습)
    ├── security-guide/  (Rate Limiting, DoS 방지)
    └── automation-analysis/  (80-85% 자동화)
```

**총 98개 규칙 (기존 90개 + Orchestration 8개) → JSON Cache로 변환 → O(1) 검색 및 주입**

---

## 🏗️ 프로젝트 핵심 원칙

### 1. 아키텍처 패턴
- **헥사고날 아키텍처** (Ports & Adapters) - 의존성 역전
- **도메인 주도 설계** (DDD) - Aggregate 중심 설계
- **CQRS** - Command/Query 분리

### 2. 코드 품질 규칙 (Zero-Tolerance)
- **Lombok 금지** - Plain Java 사용 (Domain layer에서 특히 엄격)
- **Law of Demeter** - Getter 체이닝 금지 (`order.getCustomer().getAddress()` ❌)
- **Long FK 전략** - JPA 관계 어노테이션 금지, Long userId 사용
- **Transaction 경계** - `@Transactional` 내 외부 API 호출 절대 금지

### 3. Spring 프록시 제약사항 (중요!)
⚠️ **다음 경우 `@Transactional`이 작동하지 않습니다:**
- Private 메서드
- Final 클래스/메서드
- 같은 클래스 내부 호출 (`this.method()`)

---

## 🔧 자동화 시스템

### 1. Dynamic Hooks + Cache 

**위치**: `.claude/hooks/`, `.claude/cache/`, `.claude/commands/lib/`

#### Cache 빌드
```bash
# 90개 마크다운 → 90개 JSON + index.json (약 5초)
python3 .claude/hooks/scripts/build-rule-cache.py
```

#### 자동 규칙 주입 (user-prompt-submit.sh)
- **키워드 감지**: "domain", "usecase", "controller", "entity" 등
- **Layer 매핑**: domain, application, adapter-rest, adapter-persistence
- **inject-rules.py 호출**: Layer별 JSON 규칙 자동 주입

#### 실시간 검증 (after-tool-use.sh)
- **코드 생성 직후 검증**: Write/Edit 도구 사용 후 즉시 실행
- **validation-helper.py 호출**: Cache 기반 고속 검증
- **위반 시 경고**: 구체적인 수정 방법 제시

### 2. Hook 로깅 시스템

**위치**: `.claude/hooks/logs/hook-execution.jsonl`

**목적**: Hook 실행 이력 추적 및 A/B 테스트 데이터 수집

#### 작동 원리

```
사용자: "domain aggregate 작업"
    ↓
user-prompt-submit.sh (Hook 실행)
    ├─ 키워드 분석: "aggregate" (30점)
    ├─ Layer 매핑: domain
    ├─ 로그 기록: hook-execution.jsonl
    └─ inject-rules.py: Cache 기반 규칙 주입
         ↓
Claude Code (규칙 준수 코드 생성)
    └─ Cache 규칙 100% 준수
         ↓
after-tool-use.sh (실시간 검증)
    ├─ validation-helper.py 호출
    ├─ 검증 결과 로그 기록
    └─ 위반 시 경고 표시
         ↓
LangFuse (효율 측정)
    ├─ Hook 로그 집계
    ├─ 토큰 사용량 추적
    └─ 위반 건수 모니터링
```

#### A/B 테스트 검증 방법

```bash
# 1. Hook 로그 요약
python3 .claude/hooks/scripts/summarize-hook-logs.py

# 출력 예시:
# 🔍 Hook 로그 요약
# ============================================================
#
# ## A/B 테스트 결과
# ------------------------------------------------------------
# Hook ON:  0 violations (100% Zero-Tolerance)
# Hook OFF: 40 violations (0% Zero-Tolerance)
#
# ## Cache 규칙 주입 통계
# ------------------------------------------------------------
# 총 주입된 규칙 수: 546개
# 레이어별 주입 횟수:
#   - application: 15회
#   - domain: 12회
#   - persistence: 8회
#   - adapter-rest: 7회
```

### 3. Slash Commands

**코드 생성**:
- `/code-gen-domain <name>` - Domain Aggregate 생성 (규칙 자동 주입 + 검증)
- `/code-gen-usecase <name>` - Application UseCase 생성
- `/code-gen-controller <name>` - REST Controller 생성

**검증**:
- `/validate-domain <file>` - Domain layer 파일 검증
- `/validate-architecture [dir]` - 전체 또는 특정 모듈 아키텍처 검증

**AI 리뷰**:
- `/ai-review [pr-number]` - 통합 AI 리뷰 (Gemini + CodeRabbit + Codex, 병렬 실행)
- `/gemini-review [pr-number]` - Gemini 전용 (Deprecated, `/ai-review --bots gemini` 사용 권장)

**기타**:
- `/jira-task` - Jira 태스크 분석 및 브랜치 생성

### 4. 시스템 검증 도구

**목적**: Hook 시스템, Cache 통합 검증 및 A/B 테스트 모니터링

#### 4.1 Hook 로그 요약 도구

**위치**: `.claude/hooks/scripts/summarize-hook-logs.py`

**기능**:
- ✅ `hook-execution.jsonl` 로그 파싱
- ✅ 세션별 통계 (컨텍스트 점수, 감지된 레이어)
- ✅ Cache 규칙 주입 통계
- ✅ A/B 테스트 결과 분석
- ✅ 권장 사항 및 문제 해결

**사용법**:
```bash
# 기본 (최근 5개 세션)
python3 .claude/hooks/scripts/summarize-hook-logs.py

# 최근 10개 세션, 상세 정보
python3 .claude/hooks/scripts/summarize-hook-logs.py --sessions 10 --verbose

# 출력:
# 🔍 Hook 로그 요약
# ============================================================
#
# ## 1. 전체 통계
# ------------------------------------------------------------
# 총 로그 수: 236
# 이벤트 분포:
#   - session_start: 21
#   - keyword_analysis: 21
#   - cache_injection: 42
#   - validation_result: 38
#
# ## 2. A/B 테스트 결과
# ------------------------------------------------------------
# Hook ON:  0 violations (100% Zero-Tolerance)
# Hook OFF: 40 violations (0% Zero-Tolerance)
#
# ## 3. Cache 규칙 주입 통계
# ------------------------------------------------------------
# ✅ Cache 규칙 주입 이벤트: 42회
#    총 주입된 규칙 수: 546개
#
# 레이어별 주입 횟수:
#   - application: 15회
#   - domain: 12회
#   - persistence: 8회
#   - adapter-rest: 7회
```

#### 4.2 실시간 로그 모니터링

```bash
# 터미널 1: 로그 실시간 모니터링
tail -f .claude/hooks/logs/hook-execution.jsonl

# 터미널 2: Claude Code 실행
claude code
```

### 5. Git Pre-commit Hooks (별도 시스템)

**위치**: `hooks/pre-commit`, `hooks/validators/`

- **트랜잭션 경계 검증**: `@Transactional` 내 외부 API 호출 차단
- **프록시 제약사항 검증**: Private/Final 메서드 `@Transactional` 차단
- **최종 안전망 역할**: 커밋 시 강제 검증

### 6. ArchUnit Tests

**위치**: `application/src/test/java/com/company/template/architecture/`

- **아키텍처 규칙 자동 검증**: 레이어 의존성, 네이밍 규칙
- **빌드 시 자동 실행**: 위반 시 빌드 실패

---

## 🎯 개발 워크플로우 (Cache 시스템 활용)

### 1. 코드 생성 워크플로우

```bash
# 1. Slash Command로 코드 생성 (자동 규칙 주입)
/code-gen-domain Order

# 2. 자동 실행 흐름:
#    - inject-rules.py: Domain layer 규칙 주입
#    - Claude: 규칙 준수 코드 생성
#    - after-tool-use.sh: 즉시 검증
#    - validation-helper.py: Cache 기반 검증

# 3. 검증 결과 확인
# ✅ Validation Passed: 모든 규칙 준수
# ❌ Validation Failed: 위반 규칙 상세 표시
```

### 2. 수동 검증 워크플로우

```bash
# 특정 파일 검증
/validate-domain domain/src/main/java/.../Order.java

# 전체 프로젝트 검증
/validate-architecture

# 특정 모듈만 검증
/validate-architecture domain
```

### 3. Cache 업데이트 워크플로우

```bash
# 1. 규칙 문서 수정
vim docs/coding_convention/02-domain-layer/law-of-demeter/01_getter-chaining-prohibition.md

# 2. Cache 재빌드
python3 .claude/hooks/scripts/build-rule-cache.py

# 3. 확인
cat .claude/cache/rules/domain-layer-law-of-demeter-01_getter-chaining-prohibition.json
```

---

## 🚨 Zero-Tolerance 규칙

다음 규칙은 **예외 없이** 반드시 준수해야 합니다:

### 1. Lombok 금지
- ❌ `@Data`, `@Builder`, `@Getter`, `@Setter` 등 모두 금지
- ✅ Pure Java getter/setter 직접 작성
- **검증**: validation-helper.py가 자동 감지

### 2. Law of Demeter (Getter 체이닝 금지)
- ❌ `order.getCustomer().getAddress().getZip()`
- ✅ `order.getCustomerZipCode()` (Tell, Don't Ask)
- **검증**: Anti-pattern 정규식 매칭

### 3. Long FK 전략 (JPA 관계 금지)
- ❌ `@ManyToOne`, `@OneToMany`, `@OneToOne`, `@ManyToMany`
- ✅ `private Long userId;` (Long FK 사용)
- **검증**: JPA 관계 어노테이션 감지

### 4. Transaction 경계
- ❌ `@Transactional` 내 외부 API 호출 (RestTemplate, WebClient 등)
- ✅ 트랜잭션은 짧게 유지, 외부 호출은 트랜잭션 밖에서
- **검증**: Git pre-commit hook

### 5. Javadoc 필수
- ❌ `@author`, `@since` 없는 public 클래스/메서드
- ✅ 모든 public 클래스/메서드에 Javadoc 포함
- **검증**: Checkstyle

### 6. Scope 준수
- ❌ 요청하지 않은 추가 기능 구현
- ✅ 요청된 코드만 정확히 작성
- **검증**: 수동 코드 리뷰

### 7. Orchestration Pattern (NEW) ⭐
- ❌ `executeInternal()`에 `@Transactional` 사용
- ✅ `executeInternal()`에 `@Async` 필수, 트랜잭션 밖에서 외부 API 호출
- ❌ Command에 Lombok (`@Data`, `@Builder` 등)
- ✅ Command는 Record 패턴 사용 (`public record XxxCommand`)
- ❌ Operation Entity에 IdemKey Unique 제약 없음
- ✅ `@UniqueConstraint(columnNames = {"idem_key"})` 필수
- ❌ Orchestrator가 `boolean`/`void` 반환 또는 Exception throw
- ✅ Orchestrator는 `Outcome` (Ok/Retry/Fail) 반환
- **검증**: validation-helper.py, ArchUnit, Git pre-commit hook

---

## 🔧 통합 워크플로우: Claude Code + Cursor IDE

이 프로젝트는 **Claude Code**와 **Cursor IDE**를 통합하여 사용하도록 설계되었습니다.

### 🎯 설계 의도 (Design Intent)

```
┌─────────────────────────────────────────────────────────────┐
│ 1️⃣ Claude Code: 빠른 분석 & 설계                             │
├─────────────────────────────────────────────────────────────┤
│ - PRD (Product Requirements Document) 작성                   │
│ - Jira Task 분석 및 브랜치 생성                               │
│ - Technical Spec 작성 (Domain 모델, API 명세)                │
│ - 아키텍처 설계                                               │
└─────────────────────────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────────────────┐
│ 2️⃣ Cursor IDE: 빠른 Boilerplate 생성                         │
├─────────────────────────────────────────────────────────────┤
│ - .cursorrules 자동 로드 (Zero-Tolerance 규칙)               │
│ - 프로젝트 컨벤션 학습                                         │
│ - 코딩 표준 패턴 적용                                          │
│ - 반복적인 구조 코드 빠른 생성                                │
└─────────────────────────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────────────────┐
│ 3️⃣ Claude Code: 중요한 비즈니스 로직 구현                     │
├─────────────────────────────────────────────────────────────┤
│ - Serena Memory 기반 컨텍스트 유지                            │
│ - Domain 비즈니스 메서드 구현                                 │
│ - UseCase Transaction 경계 관리                              │
│ - 복잡한 Query 최적화                                         │
└─────────────────────────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────────────────┐
│ 4️⃣ Claude Code: 자동 검증 & PR 생성                          │
├─────────────────────────────────────────────────────────────┤
│ - /validate-architecture (ArchUnit)                         │
│ - Git Pre-commit Hooks (Transaction 경계)                   │
│ - gh pr create (자동 PR 생성)                                │
└─────────────────────────────────────────────────────────────┘
```

### 📁 시스템 역할 분리

#### Claude Code (`.claude/`) ⭐

**역할**: 설계, 비즈니스 로직, 검증, 자동화

**자동화 시스템**:
- **Dynamic Hooks**: 키워드 감지 → Layer 매핑 → 규칙 자동 주입 (A/B 테스트 검증 완료)
- **Cache 시스템**: 98개 규칙 → JSON → O(1) 검색 (90% 토큰 절감)
- **Hook 로깅**: hook-execution.jsonl → A/B 테스트 데이터 수집
- **Git Pre-commit Hooks**: 트랜잭션 경계 + Orchestration 자동 검증
- **ArchUnit**: 빌드 시 아키텍처 + Orchestration 자동 검증 (12개 규칙)
- **Orchestration 자동화**: 10개 파일 80-85% 자동 생성 (75% 시간 단축)

**Slash Commands**:
```bash
/code-gen-domain <name>          # Domain Aggregate 생성
/code-gen-usecase <name>         # Application UseCase 생성
/code-gen-controller <name>      # REST Controller 생성
/code-gen-orchestrator <Domain> <EventType>  # Orchestration Pattern 생성
/validate-domain <file>          # Domain layer 검증
/validate-architecture           # 전체 아키텍처 검증
/ai-review [pr-number]           # 통합 AI 리뷰 (Gemini + CodeRabbit + Codex)
/jira-task                       # Jira Task 분석 및 브랜치 생성
```

**성능 (A/B 테스트 검증)**:
- **컨벤션 위반**: 40회 → 0회 (100% 제거) ✅
- **Zero-Tolerance 준수율**: 0% → 100% ✅
- 토큰 사용량: 90% 절감
- 검증 속도: 73.6% 향상
- Orchestration 생성: 75% 시간 단축 (8분 → 2분)
- Orchestration 위반: 83-100% 감소 (12회 → 0-2회)

#### Cursor IDE 🚀

**역할**: Boilerplate 빠른 생성

**통합 방식**:
- **.cursorrules**: Cursor IDE가 자동으로 읽는 프로젝트 규칙
- **프로젝트 컨텍스트**: 기존 코드 패턴 자동 학습
- **AI 기반 생성**: 규칙 준수 코드 자동 생성

**사용 방법** (Cursor IDE에서):
```
사용자: "Order Aggregate를 생성해줘"

Cursor IDE:
1. .cursorrules 자동 로드
   → Lombok 금지, Law of Demeter 등 자동 적용

2. 프로젝트 기존 코드 패턴 분석
   → 프로젝트 표준 스타일 학습

3. AI 기반 코드 생성
   → 규칙 준수 Boilerplate 생성
```

### 🔄 실제 워크플로우 예시

#### 예시 1: Order Aggregate 개발

```bash
# 1. Claude Code: PRD 작성
"Order Aggregate PRD를 작성해줘. 주문 생성, 취소, 상태 변경이 필요해."
→ PRD 문서 생성: docs/prd/order-aggregate.md

# 2. Claude Code: Jira Task 분석
/jira-task
→ PROJ-123 분석 → feature/PROJ-123-order 브랜치 생성

# 3. Cursor IDE: Boilerplate 생성
"Order Aggregate를 생성해줘"
→ OrderDomain.java, OrderId.java, OrderStatus.java 등 생성

# 4. Claude Code: 비즈니스 로직 구현
"Order Domain에 비즈니스 메서드를 구현해줘:
- placeOrder(): 주문 생성
- cancelOrder(): 주문 취소 (PLACED 상태만 가능)
- confirmOrder(): 주문 확인"
→ 비즈니스 로직 구현 (Hook이 자동으로 Law of Demeter, Tell Don't Ask 규칙 주입)

# 5. Claude Code: 검증 및 PR
/validate-architecture
→ ArchUnit 테스트 통과
→ gh pr create 자동 실행
```

#### 예시 2: UseCase 개발

```bash
# 1. Cursor IDE: UseCase Boilerplate
"PlaceOrderUseCase를 생성해줘"
→ PlaceOrderUseCase.java (port/in/)
→ PlaceOrderCommand.java (dto/command/)
→ OrderResponse.java (dto/response/)

# 2. Claude Code: Transaction 경계 관리
"PlaceOrderUseCase에 비즈니스 로직을 구현해줘.
외부 결제 API 호출이 필요해."
→ executeInTransaction() 분리
→ 외부 API 호출은 트랜잭션 밖에서
→ Git Pre-commit Hook 자동 검증
```

#### 예시 3: Orchestration Pattern 개발 (NEW) ⭐

```bash
# 1. Claude Code: Orchestrator 자동 생성
/code-gen-orchestrator Order PlacementConfirmed

# 자동 생성 결과 (10개 파일, 80-85% 완성):
# application/
#   └── orchestration/
#       └── order/
#           ├── command/
#           │   └── OrderPlacementConfirmedCommand.java (Record)
#           ├── entity/
#           │   └── OrderPlacementConfirmedOperationEntity.java (@UniqueConstraint)
#           ├── finalizer/
#           │   └── OrderPlacementConfirmedFinalizer.java (@Scheduled)
#           ├── mapper/
#           │   └── OrderPlacementConfirmedMapper.java
#           ├── orchestrator/
#           │   └── OrderPlacementConfirmedOrchestrator.java (@Async)
#           ├── outcome/
#           │   └── OrderPlacementConfirmedOutcome.java (Sealed)
#           ├── reaper/
#           │   └── OrderPlacementConfirmedReaper.java (@Scheduled)
#           ├── repository/
#           │   └── OrderPlacementConfirmedOperationRepository.java
#           ├── status/
#           │   └── OrderPlacementConfirmedOperationStatus.java (Enum)
#           └── wal/
#               └── OrderPlacementConfirmedWriteAheadLog.java

# 2. 개발자 작업 (15-20% 비즈니스 로직):
# - executeInternal() 구현: 외부 API 호출 로직
# - Mapper 구현: Command → Domain Entity 변환
# - Outcome 구현: 성공/재시도/실패 조건

# 3. 자동 검증 (3-Tier):
# Tier 1: validation-helper.py (실시간)
# Tier 2: Git pre-commit hook (커밋 시)
# Tier 3: ArchUnit (빌드 시)

# 예상 효율:
# - 생성 시간: 8분 → 2분 (75% 단축)
# - 컨벤션 위반: 평균 12회 → 0-2회 (83-100% 감소)
# - 개발자 집중: Boilerplate → 비즈니스 로직
```

### 📊 비교표

| 항목 | Claude Code | Cursor IDE |
|------|-------------|------------|
| **역할** | 설계, 로직, 검증 | Boilerplate 생성 |
| **자동화** | Hooks, Cache, Serena | .cursorrules (자동 로드) |
| **강점** | 컨텍스트 유지, 복잡한 로직 | 빠른 구조 생성 |
| **검증** | Pre-commit, ArchUnit | AI 지원 검증 |
| **사용 시점** | 분석, 로직, 검증 | 반복 구조 생성 |

### 💡 핵심 원칙

1. **Claude Code First**: 항상 Claude Code로 분석 및 설계 시작
2. **Cursor IDE for Speed**: 반복 구조는 Cursor IDE로 빠르게 생성
3. **Claude Code for Logic**: 중요한 비즈니스 로직은 Claude Code에 위임
4. **Automatic Validation**: 모든 코드는 자동 검증 통과 필수

**✅ 이 워크플로우는 두 도구의 강점을 최대한 활용하도록 설계되었습니다.**

---

## 📖 참고 문서

### 튜토리얼
- [Getting Started](../docs/tutorials/01-getting-started.md) - 시작 가이드 (5분)

### Dynamic Hooks 시스템
- [DYNAMIC_HOOKS_GUIDE.md](../docs/DYNAMIC_HOOKS_GUIDE.md) - 전체 시스템 가이드
- [Cache README](./.claude/cache/rules/README.md) - Cache 시스템 상세
- [Validation Helper](./hooks/scripts/validation-helper.py) - 검증 엔진

### Hook 로깅 + LangFuse
- [Hook 로그 요약](./hooks/scripts/summarize-hook-logs.py) - A/B 테스트 분석
- [LangFuse 통합 가이드](../docs/LANGFUSE_USAGE_GUIDE.md) - 효율 측정 및 모니터링

### Slash Commands
- [Commands README](./commands/README.md) - 모든 명령어 설명
- [Code Gen Domain](./commands/code-gen-domain.md) - Domain 생성
- [Code Gen Orchestrator](./commands/code-gen-orchestrator.md) - Orchestration Pattern 생성 (NEW) ⭐
- [Validate Domain](./commands/validate-domain.md) - Domain 검증

### 코딩 규칙
- [Coding Convention](../docs/coding_convention/) - 98개 규칙 (Layer별, Orchestration 포함)

---

## 🎓 학습 경로

### Day 1: 시스템 이해
1. README.md 읽기 (프로젝트 개요)
2. docs/tutorials/01-getting-started.md (실습)
3. Cache 빌드 및 첫 코드 생성 테스트

### Week 1: 핵심 규칙 숙지
1. Domain Layer 규칙 (Law of Demeter, Lombok 금지)
2. Application Layer 규칙 (Transaction 경계)
3. Persistence Layer 규칙 (Long FK 전략)
4. Orchestration Pattern 기초 (3-Phase Lifecycle, Idempotency) ⭐ NEW

### Month 1: 고급 패턴
1. DDD Aggregate 설계
2. CQRS 패턴 적용
3. Event-Driven Architecture
4. Orchestration Pattern 실전 (WAL, Outcome Modeling, Crash Recovery) ⭐ NEW

---

**✅ 이 프로젝트의 모든 코드는 위 표준을 따라야 합니다.**

**💡 핵심**: Dynamic Hooks + Cache 시스템이 자동으로 규칙을 주입하고 검증하므로, 개발자는 비즈니스 로직에 집중할 수 있습니다!
