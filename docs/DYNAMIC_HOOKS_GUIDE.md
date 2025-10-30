# Dynamic Hooks Guide - Cache 기반 실시간 규칙 주입 시스템

> **최종 업데이트**: 2025-10-17
> **시스템**: High-Performance Cache + Dynamic Hooks + Slash Commands
> **버전**: 2.0 (Cache-based)

---

## 📋 목차

1. [시스템 개요](#시스템-개요)
2. [아키텍처](#아키텍처)
3. [Cache 시스템](#cache-시스템)
4. [Dynamic Hooks](#dynamic-hooks)
5. [Slash Commands](#slash-commands)
6. [성능 메트릭](#성능-메트릭)
7. [사용 가이드](#사용-가이드)
8. [트러블슈팅](#트러블슈팅)
9. [보안 고려사항](#보안-고려사항)

---

## 시스템 개요

### 🎯 목적

**자동화된 규칙 주입 + 실시간 검증**으로 일관된 코드 품질 보장

### 핵심 기능

1. **컨텍스트 인식**: 사용자 입력 분석 → 관련 규칙만 선택적 주입
2. **Cache 기반**: 90개 문서 → JSON Cache 변환 (90% 토큰 절약)
3. **실시간 검증**: 코드 생성 직후 자동 검증 (73.6% 속도 향상)
4. **Slash Commands**: `/code-gen-domain`, `/code-gen-usecase`, `/code-gen-controller`

### 성능 지표

| 메트릭 | 기존 방식 | Cache 시스템 | 개선율 |
|--------|----------|-------------|--------|
| 토큰 사용량 | 50,000 | 500-1,000 | **90% 절감** |
| 검증 속도 | 561ms | 148ms | **73.6% 향상** |
| 문서 로딩 | 2-3초 | <100ms | **95% 향상** |
| 캐시 빌드 | N/A | 5초 | N/A |

---

## 아키텍처

### 3-Tier Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   User Input / Task                     │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────┴──────────────────────────────────┐
│  Tier 1: Context Analysis (<100ms)                      │
│  ────────────────────────────────────                   │
│  - Keyword Detection                                     │
│  - Layer Mapping (domain, application, adapter-rest)    │
│  - Priority Scoring (Critical/High/Medium/Low)           │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────┴──────────────────────────────────┐
│  Tier 2: Rule Injection (<50ms)                         │
│  ────────────────────────────────────                   │
│  - Cache Lookup (O(1) index)                            │
│  - Layer-Specific Rules                                 │
│  - Priority Filtering                                   │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────┴──────────────────────────────────┐
│  Tier 3: Code Generation + Validation (<500ms)          │
│  ──────────────────────────────────────────             │
│  - Claude Code Generation                               │
│  - Real-time Validation (after-tool-use hook)           │
│  - Feedback Loop                                        │
└─────────────────────────────────────────────────────────┘
```

### 데이터 흐름

```
docs/coding_convention/    ──build-rule-cache.py──>    .claude/cache/rules/
(90 .md files)                                          (90 .json + index.json)
                                                                 │
User Input ──> user-prompt-submit.sh ──> inject-rules.py ──────┤
                                                                 │
Code Generated ──> after-tool-use.sh ──> validation-helper.py ──┘
```

---

## Cache 시스템

### 디렉토리 구조

```
.claude/
├── cache/
│   └── rules/
│       ├── index.json                                    # O(1) 검색 인덱스
│       ├── domain-*.json                                 # Domain 레이어 규칙 (13개)
│       ├── application-*.json                            # Application 레이어 (13개)
│       ├── adapter-rest-*.json                           # Adapter-REST (15개)
│       ├── adapter-persistence-*.json                    # Adapter-Persistence (9개)
│       ├── testing-*.json                                # Testing (12개)
│       ├── java21-*.json                                 # Java21 Patterns (7개)
│       ├── enterprise-*.json                             # Enterprise Patterns (11개)
│       └── error-handling-*.json                         # Error Handling (10개)
├── hooks/
│   └── scripts/
│       ├── build-rule-cache.py                           # Cache 빌더
│       └── validation-helper.py                          # 검증 헬퍼
└── commands/
    └── lib/
        └── inject-rules.py                               # 규칙 주입 스크립트
```

### 캐시 빌드

**실행 방법**:
```bash
python3 .claude/hooks/scripts/build-rule-cache.py
```

**결과**:
```
✅ Success: 90 rules generated, 2 skipped
📊 Cache Stats:
   - Total Rules: 90
   - Domain: 13
   - Application: 13
   - Adapter-REST: 15
   - ... (생략)
⚡ Build Time: ~5 seconds
```

### JSON Cache 구조

**index.json** (O(1) 검색):
```json
{
  "version": "1.0.0",
  "totalRules": 90,
  "keywordIndex": {
    "aggregate": ["domain-aggregate-boundaries", ...],
    "getter": ["domain-law-of-demeter-getter-chaining"],
    "controller": ["adapter-rest-controller-design", ...]
  },
  "layerIndex": {
    "domain": [13 rule IDs],
    "application": [13 rule IDs],
    "adapter-rest": [15 rule IDs]
  }
}
```

**개별 규칙 파일** (예: `domain-law-of-demeter-getter-chaining.json`):
```json
{
  "id": "domain-layer-law-of-demeter-01_getter-chaining-prohibition",
  "metadata": {
    "keywords": {
      "primary": ["getter", "chaining", "prohibition"],
      "secondary": ["law", "of", "demeter"],
      "anti": ["order.getCustomer().getAddress().getZip()"]
    },
    "layer": "domain",
    "priority": "critical",
    "tokenEstimate": 605
  },
  "rules": {
    "prohibited": [
      "❌ `order.getCustomer().getAddress().getZip()`",
      "❌ Getter 체이닝"
    ],
    "allowed": [
      "✅ `order.isReadyForShipment()` (Tell, Don't Ask)",
      "✅ `order.calculateTotalAmount()`"
    ]
  },
  "documentation": {
    "path": "docs/coding_convention/02-domain-layer/law-of-demeter/01_getter-chaining-prohibition.md",
    "summary": "Getter Chaining Prohibition"
  }
}
```

---

## Dynamic Hooks

### Hook 실행 흐름

**2가지 트리거**:

1. **UserPromptSubmit Hook** - 사용자가 일반 프롬프트를 입력할 때
2. **PreToolUse Hook** - Slash Command 실행 직전

#### Slash Command와 Hook의 관계

**문제**: Slash Command는 SlashCommand Tool을 통해 처리되므로 UserPromptSubmit Hook을 우회합니다.

**해결**: PreToolUse Hook을 추가하여 SlashCommand Tool 실행 **직전**에 규칙을 주입합니다.

**hooks.json 설정**:
```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "SlashCommand",
        "hooks": [
          {
            "type": "command",
            "command": "bash .claude/hooks/user-prompt-submit.sh"
          }
        ]
      }
    ],
    "UserPromptSubmit": [
      {
        "hooks": [
          {
            "type": "command",
            "command": "bash .claude/hooks/user-prompt-submit.sh"
          }
        ]
      }
    ]
  }
}
```

**동작 비교**:

| 입력 방식 | Hook 트리거 | 규칙 주입 |
|----------|------------|----------|
| 일반 프롬프트: "domain aggregate Order" | UserPromptSubmit | ✅ |
| Slash Command: `/domain Order` | PreToolUse (SlashCommand 매처) | ✅ |

### user-prompt-submit.sh

**Trigger**:
- UserPromptSubmit Hook (일반 프롬프트)
- PreToolUse Hook (Slash Command)

**동작 흐름**:

```
User Input
   │
   ├─> Keyword Detection
   │   ├─ "aggregate" → domain (30점)
   │   ├─ "controller" → adapter-rest (30점)
   │   ├─ "usecase" → application (30점)
   │   └─ "domain" → general (15점)
   │
   ├─> Layer Mapping
   │   └─ Detected Layers: [domain, application]
   │
   ├─> Priority Filter
   │   └─ "lombok", "zero-tolerance" → critical
   │
   └─> inject-rules.py 호출
       └─ Layer별 규칙 주입
```

**Keyword → Layer 매핑 테이블**:

| Keyword | Layer | Score |
|---------|-------|-------|
| aggregate, 애그리게이트 | domain | 30 |
| controller, 컨트롤러 | adapter-rest | 30 |
| usecase, service | application | 30 |
| repository, jpa | adapter-persistence | 30 |
| test, 테스트 | testing | 25 |
| record, sealed | java21 | 20 |
| dto, mapper | enterprise | 20 |
| exception, error | error-handling | 25 |

**Context Score 계산**:
- Primary Keyword: +30점
- Secondary Keyword: +15점
- Zero-Tolerance Keyword: +20점
- **Threshold**: 25점 (키워드 1개 이상)

**예시**:

```bash
# Input: "Create an Order aggregate"
# Detection:
#   - "aggregate" → domain (+30)
#   - "order" → domain context (+15)
# Context Score: 45
# Strategy: CACHE_BASED
# Inject: domain 레이어 규칙
```

### after-tool-use.sh

**Trigger**: Write/Edit 도구 사용 직후

**동작 흐름**:

```
File Written (Order.java)
   │
   ├─> Layer Detection (파일 경로 기반)
   │   └─ "domain/model/" → domain
   │
   ├─> validation-helper.py 호출
   │   ├─ Critical 규칙만 검증 (성능 최적화)
   │   ├─ Anti-pattern 검사
   │   └─ Prohibited 항목 검사
   │
   └─> Validation Result
       ├─ PASS: ✅ 모든 규칙 준수
       └─ FAIL: ⚠️ 위반 항목 + 수정 가이드
```

**검증 항목**:

1. **Critical Validators** (모든 레이어):
   - Lombok 금지
   - Javadoc @author/@since 필수

2. **Layer-Specific Validators**:
   - **Domain**: Spring/JPA annotation 금지, Law of Demeter
   - **Application**: @Transactional 제약사항
   - **Adapter-REST**: @RestController, @Valid 필수

**Fallback 로직**:
- `validation-helper.py`가 없으면 기본 검증 실행
- Unknown layer는 Critical 규칙만 검증

---

## Slash Commands

### 개요

**위치**: `.claude/commands/`

**커맨드 종류**:

1. **코드 생성 커맨드** (전체 구조 생성):
   - `/code-gen-domain [Aggregate] [PRD]`
   - `/code-gen-usecase [UseCase] [PRD]`
   - `/code-gen-controller [Resource] [PRD]`

2. **레이어별 작업 모드** (기존 코드 수정):
   - `/domain [작업 내용]`
   - `/application [작업 내용]`
   - `/rest [작업 내용]`
   - `/persistence [작업 내용]`
   - `/test [작업 내용]`

### Slash Command 규칙 주입 메커니즘

**커맨드 정의 방식** (간결한 키워드 기반):
```markdown
---
description: Domain layer 작업 (Aggregate, Entity, Value Object, Domain Event 등)
---

domain aggregate entity {{args}}
```

**PreToolUse Hook 처리**:
1. 사용자가 `/domain Product` 입력
2. SlashCommand Tool이 "domain aggregate entity Product"로 확장
3. PreToolUse Hook이 **Tool 실행 직전** 트리거
4. user-prompt-submit.sh가 키워드 분석:
   - "domain" (30점) + "aggregate" (30점) + "entity" (30점) = 90점
5. Domain layer 규칙 13개 자동 주입
6. SlashCommand Tool 실행 → Claude가 규칙 준수 코드 생성

**핵심**: 커맨드는 간결하게 유지하고, Hook이 규칙 주입을 담당합니다.

### /code-gen-domain

**목적**: DDD Aggregate 자동 생성

**사용법**:
```bash
/code-gen-domain Order
/code-gen-domain Payment @prd/payment-feature.md
```

**생성 파일**:
```
domain/src/main/java/com/company/template/domain/model/
├── Order.java                    # Aggregate Root
├── OrderId.java                  # Typed ID (record)
├── OrderStatus.java              # Status Enum
└── OrderLineItem.java            # 내부 Entity (필요 시)
```

**자동 주입 규칙**:
- ❌ Lombok 금지
- ✅ Law of Demeter
- ✅ Tell, Don't Ask 패턴
- ✅ Pure Java (Spring/JPA 없음)

**코드 템플릿**:
```java
/**
 * Order Aggregate Root
 *
 * <p><strong>규칙 준수:</strong></p>
 * <ul>
 *   <li>❌ Lombok 사용 안함 - Pure Java</li>
 *   <li>✅ Law of Demeter - Getter 체이닝 방지</li>
 *   <li>✅ Tell, Don't Ask 패턴 적용</li>
 * </ul>
 *
 * @author Claude
 * @since 2025-10-17
 */
public class Order {
    private final OrderId id;
    private OrderStatus status;

    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("...");
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public boolean isConfirmed() {  // Law of Demeter 준수
        return this.status == OrderStatus.CONFIRMED;
    }
}
```

### /code-gen-usecase

**목적**: Application UseCase 자동 생성

**사용법**:
```bash
/code-gen-usecase PlaceOrder
/code-gen-usecase CancelOrder @prd/order-management.md
```

**생성 파일**:
```
application/src/main/java/com/company/template/application/
├── usecase/
│   └── PlaceOrderUseCase.java
├── port/in/
│   ├── PlaceOrderCommand.java    # Input DTO (record)
│   └── PlaceOrderResult.java     # Output DTO (record)
└── assembler/
    └── OrderAssembler.java        # Domain ↔ DTO 변환
```

**자동 주입 규칙**:
- ❌ @Transactional 내 외부 API 호출 금지
- ❌ Private/Final 메서드에 @Transactional 금지
- ✅ DTO 변환 패턴
- ✅ 트랜잭션 짧게 유지

**코드 템플릿**:
```java
@Service
public class PlaceOrderUseCase {
    public PlaceOrderResult execute(PlaceOrderCommand command) {
        // 1. 외부 API 호출 (트랜잭션 밖)
        ExternalData data = externalApiPort.fetchData(command.externalId());

        // 2. 트랜잭션 내 Domain 로직
        Order order = executeInTransaction(command, data);

        // 3. DTO 변환
        return assembler.toResult(order);
    }

    @Transactional
    protected Order executeInTransaction(/*...*/) {
        // ⚠️ 외부 API 호출 금지
    }
}
```

### /code-gen-controller

**목적**: REST API Controller 자동 생성

**사용법**:
```bash
/code-gen-controller Order
/code-gen-controller Payment @prd/payment-api.md
```

**생성 파일**:
```
adapter/in/web/src/main/java/com/company/template/adapter/in/web/
├── controller/
│   └── OrderController.java
├── dto/
│   ├── OrderCreateRequest.java
│   ├── OrderResponse.java
│   └── ErrorResponse.java
└── mapper/
    └── OrderApiMapper.java
```

**자동 주입 규칙**:
- ✅ @RestController 사용
- ✅ @Valid 유효성 검증
- ✅ HTTP 상태 코드 표준화
- ❌ Domain 객체 직접 노출 금지

**코드 템플릿**:
```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
        @Valid @RequestBody OrderCreateRequest request
    ) {
        // 1. API Request → Command
        PlaceOrderCommand command = mapper.toCommand(request);

        // 2. UseCase 실행
        PlaceOrderResult result = useCase.execute(command);

        // 3. Result → Response
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapper.toResponse(result));
    }
}
```

---

## 성능 메트릭

### 토큰 효율성

**Before (Markdown 직접 로딩)**:
```
Law of Demeter 문서: 2,150 tokens
Aggregate Design 문서: 3,250 tokens
Controller Design 문서: 2,800 tokens
────────────────────────────────
Total: 8,200 tokens (단 3개 문서)
```

**After (Cache 시스템)**:
```
Law of Demeter 규칙: 215 tokens
Aggregate Design 규칙: 325 tokens
Controller Design 규칙: 280 tokens
────────────────────────────────
Total: 820 tokens (90% 절감)
```

### 속도 벤치마크

**Sequential 검증** (기존 방식):
```bash
Validator 1: 182ms
Validator 2: 195ms
Validator 3: 184ms
────────────────
Total: 561ms
```

**Parallel 검증** (Cache 시스템):
```bash
Validator 1: 148ms  ]
Validator 2: 142ms  ] - Parallel
Validator 3: 145ms  ]
────────────────
Total: 148ms (73.6% 향상)
```

### 캐시 빌드 성능

```bash
Input: 90 .md files (총 ~250KB)
Output: 90 .json files + index.json
────────────────
Build Time: 4.8 seconds
Cache Size: 1.2MB
Lookup Speed: <10ms (O(1) index)
```

---

## 사용 가이드

### 초기 설정

**1. 캐시 빌드**:
```bash
cd /path/to/project
python3 .claude/hooks/scripts/build-rule-cache.py
```

**2. Hook 권한 설정**:
```bash
chmod +x .claude/hooks/user-prompt-submit.sh
chmod +x .claude/hooks/after-tool-use.sh
chmod +x .claude/hooks/scripts/*.py
```

**3. 검증**:
```bash
# Cache 생성 확인
ls .claude/cache/rules/ | wc -l
# 결과: 91 (90 rules + index.json)

# Hook 동작 확인
cat .claude/hooks/logs/hook-execution.log
```

### 일반 워크플로우

**시나리오 1: Domain Aggregate 생성**

```bash
# 1. Slash Command 실행
/code-gen-domain Order

# 2. Hook 자동 동작
#    - user-prompt-submit.sh: domain 규칙 주입
#    - Claude: Order.java 생성
#    - after-tool-use.sh: 검증 실행

# 3. 결과
✅ Validation Passed
   - No Lombok
   - Javadoc present
   - Pure Java (no Spring/JPA)
```

**시나리오 2: UseCase 생성**

```bash
# 1. Slash Command 실행
/code-gen-usecase PlaceOrder @prd/order-management.md

# 2. Hook 자동 동작
#    - user-prompt-submit.sh: application 규칙 주입
#    - Claude: PlaceOrderUseCase.java 생성
#    - after-tool-use.sh: 트랜잭션 경계 검증

# 3. 결과
✅ Validation Passed
   - @Transactional 위치 올바름
   - 외부 API 호출 트랜잭션 밖
```

**시나리오 3: 수동 코드 작성**

```bash
# 1. 코드 작성 요청
User: "Create an Order aggregate with status management"

# 2. Hook 자동 동작
#    - user-prompt-submit.sh:
#      * "aggregate" → domain (+30점)
#      * "order" → domain context (+15점)
#      * Context Score: 45 → CACHE_BASED
#      * domain 규칙 주입
#    - Claude: Order.java 생성
#    - after-tool-use.sh: domain 검증 실행

# 3. 검증 결과
✅ PASSED: No Lombok
✅ PASSED: Javadoc @author present
✅ PASSED: Pure Java (no Spring/JPA)
```

### 캐시 업데이트

**문서 변경 시**:
```bash
# 1. docs/coding_convention/ 수정

# 2. 캐시 재빌드
python3 .claude/hooks/scripts/build-rule-cache.py

# 3. 결과 확인
cat .claude/cache/rules/index.json
```

---

## 트러블슈팅

### 문제 1: Hook이 실행되지 않음

**증상**:
```
규칙이 주입되지 않음
검증이 실행되지 않음
```

**해결**:
```bash
# 1. Hook 권한 확인
ls -la .claude/hooks/*.sh
# 결과: -rwxr-xr-x (실행 권한 있어야 함)

# 2. 권한 부여
chmod +x .claude/hooks/user-prompt-submit.sh
chmod +x .claude/hooks/after-tool-use.sh

# 3. 로그 확인
cat .claude/hooks/logs/hook-execution.log
```

### 문제 2: Cache를 찾을 수 없음

**증상**:
```
ERROR: inject-rules.py not found
FileNotFoundError: index.json
```

**해결**:
```bash
# 1. Cache 존재 확인
ls .claude/cache/rules/index.json

# 2. 없으면 빌드
python3 .claude/hooks/scripts/build-rule-cache.py

# 3. 경로 확인
# inject-rules.py에서 PROJECT_ROOT 경로 확인
```

### 문제 3: Layer가 감지되지 않음

**증상**:
```
Detected Layer: unknown
Fallback to basic validation
```

**해결**:
```bash
# 1. 파일 경로 확인
# Domain: domain/*/model/
# Application: application/
# Adapter-REST: adapter/in/web/

# 2. after-tool-use.sh 수정
# Layer 감지 패턴 추가

# 3. 로그 확인
cat .claude/hooks/logs/hook-execution.log
```

### 문제 4: 검증이 실패함

**증상**:
```
⚠️ Validation Failed: Lombok 사용 감지
```

**해결**:
```java
// ❌ Before
@Data
public class Order {
    private String id;
}

// ✅ After
public class Order {
    private final String id;

    public Order(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
```

### 문제 5: Context Score가 낮음

**증상**:
```
Context Score: 15
Strategy: SKIP
```

**해결**:
```bash
# 임계값: 25점
# Primary Keyword 1개 이상 필요

# ❌ Bad: "Create a class"
# Context Score: 0

# ✅ Good: "Create an Order aggregate"
# - "aggregate" → domain (+30)
# Context Score: 30
```

---

## 보안 고려사항

### 🔒 Security Principles

Dynamic Hook 스크립트는 **사용자 권한으로 실행**되므로 보안에 주의해야 합니다.

**중요 원칙**:
- ✅ **Hook scripts execute with your user permissions** - 파일 시스템 접근, 네트워크 호출 등 모든 권한 보유
- ✅ **Review all hook scripts before activation** - 실행 전 스크립트 내용을 반드시 검토
- ✅ **Never run hooks from untrusted sources** - 신뢰할 수 없는 출처의 Hook 절대 실행 금지
- ✅ **Validate script content before chmod +x** - 실행 권한 부여 전 스크립트 검증

### 🛡️ Best Practices

#### 1. Version Control
```bash
# Hook 스크립트를 버전 관리에 포함
git add .claude/hooks/*.sh
git commit -m "Add hook scripts"

# 변경 이력 추적
git log -- .claude/hooks/
```

#### 2. Code Review
```bash
# Hook 변경사항은 반드시 코드 리뷰
# Pull Request에서 다른 팀원의 승인 필요

# .claude/hooks/ 디렉토리 변경 시 알림 설정 권장
```

#### 3. Safe Testing Environment
```bash
# 1. 테스트 브랜치에서 먼저 검증
git checkout -b test/new-hook
# ... hook 수정 ...
# ... 안전성 검증 ...

# 2. 스크립트 문법 검증
bash -n .claude/hooks/user-prompt-submit.sh
shellcheck .claude/hooks/user-prompt-submit.sh

# 3. 안전한 환경에서 실행 테스트
# (예: 격리된 디렉토리, Docker 컨테이너 등)
```

#### 4. Principle of Least Privilege
```bash
# Hook 스크립트는 최소한의 권한만 사용
# - 읽기 전용 작업 선호
# - 파일 수정은 명시적 확인 후에만
# - 외부 네트워크 호출 최소화

# Bad Example: ❌
rm -rf /some/path  # Dangerous!

# Good Example: ✅
echo "Validation failed" >&2
exit 1
```

### 🚨 Security Warnings

**절대 하지 말아야 할 것**:
- ❌ Hook에서 민감한 정보(API 키, 비밀번호) 하드코딩
- ❌ Hook에서 외부 URL로 코드 다운로드 후 실행
- ❌ Hook에서 sudo 권한 요구
- ❌ 검증되지 않은 사용자 입력 직접 실행 (`eval`, `exec` 등)

**권장 사항**:
- ✅ Hook 스크립트는 읽기 전용 검증만 수행
- ✅ 환경 변수를 통한 설정 관리
- ✅ 로그 파일은 안전한 위치에 저장
- ✅ 정기적인 보안 감사

### 🔍 Hook Script Validation Checklist

Hook 스크립트를 추가/수정하기 전 확인:

```bash
# 1. 스크립트 문법 검증
bash -n script.sh

# 2. ShellCheck으로 잠재적 문제 탐지
shellcheck script.sh

# 3. 실행 권한 확인
ls -la .claude/hooks/*.sh

# 4. 스크립트 내용 리뷰
cat .claude/hooks/script.sh | less

# 5. 위험한 명령어 검색
grep -E "(rm -rf|sudo|curl.*\| bash|eval|exec)" .claude/hooks/*.sh
```

### 📋 Incident Response

Hook 스크립트에서 문제 발견 시:

1. **즉시 실행 권한 제거**
   ```bash
   chmod -x .claude/hooks/suspicious-script.sh
   ```

2. **Git에서 제거 (필요시)**
   ```bash
   git rm .claude/hooks/suspicious-script.sh
   git commit -m "Remove suspicious hook script"
   ```

3. **팀에 알림**
   - 다른 개발자들에게 즉시 공유
   - 잠재적 영향 범위 분석

4. **검증 후 재도입**
   - 문제 해결 후 코드 리뷰
   - 안전성 재확인 후 추가

---

## 부록

### A. 전체 디렉토리 구조

```
project/
├── .claude/
│   ├── cache/
│   │   └── rules/
│   │       ├── index.json
│   │       └── *.json (90 files)
│   ├── hooks/
│   │   ├── user-prompt-submit.sh
│   │   ├── after-tool-use.sh
│   │   ├── logs/
│   │   │   └── hook-execution.log
│   │   └── scripts/
│   │       ├── build-rule-cache.py
│   │       └── validation-helper.py
│   └── commands/
│       ├── README.md
│       ├── code-gen-domain.md
│       ├── code-gen-usecase.md
│       ├── code-gen-controller.md
│       └── lib/
│           └── inject-rules.py
└── docs/
    └── coding_convention/
        ├── 01-adapter-rest-api-layer/
        ├── 02-domain-layer/
        ├── 03-application-layer/
        ├── 04-persistence-layer/
        ├── 05-testing/
        ├── 06-java21-patterns/
        ├── 07-enterprise-patterns/
        └── 08-error-handling/
```

### B. 참고 문서

- [Coding Standards Summary](./CODING_STANDARDS_SUMMARY.md)
- [Enterprise Spring Standards](./ENTERPRISE_SPRING_STANDARDS_SUMMARY.md)
- [Slash Commands README](../.claude/commands/README.md)

---

**✅ Dynamic Hooks 시스템으로 일관된 코드 품질을 자동으로 보장합니다.**

**⚠️ 결론**: Dynamic Hook은 강력한 도구이지만, 보안에 항상 주의해야 합니다. 신뢰할 수 있는 소스의 스크립트만 사용하고, 정기적으로 검토하세요.
