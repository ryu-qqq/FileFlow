# 07. Automation Analysis: Orchestration Pattern 자동화 분석

## 📋 목차
1. [개요](#1-개요)
2. [Claude Code 자동화 가능성](#2-claude-code-자동화-가능성)
3. [Windsurf 워크플로우 가능성](#3-windsurf-워크플로우-가능성)
4. [컨벤션 강제 메커니즘](#4-컨벤션-강제-메커니즘)
5. [구현 제안](#5-구현-제안)

---

## 1. 개요

### 1.1 분석 목표

Orchestration Pattern (09)의 **Boilerplate 자동 생성** 및 **컨벤션 강제** 가능성을 분석합니다:

1. **Claude Code Slash Command**: `/code-gen-orchestrator` 명령어 개발
2. **Windsurf Workflow**: `.windsurf/workflows/09-orchestration/` YAML 작성
3. **컨벤션 강제**: Cache + Serena Memory + Validation

### 1.2 자동화 범위

| 자동화 가능 | 컴포넌트 | 복잡도 |
|-------------|---------|--------|
| ✅ 매우 높음 | Command Record | 🟢 낮음 |
| ✅ 매우 높음 | Orchestrator 골격 | 🟡 중간 |
| ✅ 높음 | Operation Entity | 🟡 중간 |
| ✅ 높음 | WriteAheadLog Entity | 🟡 중간 |
| ✅ 높음 | Finalizer Scheduler | 🟡 중간 |
| ✅ 높음 | Reaper Scheduler | 🟡 중간 |
| ⚠️ 중간 | executeInternal() 비즈니스 로직 | 🔴 높음 |
| ⚠️ 중간 | Retry 전략 (Backoff) | 🟡 중간 |
| ⚠️ 중간 | Error 분류 (Retryable/Fatal) | 🟡 중간 |

**결론**: 골격 코드 **70-80%** 자동 생성 가능, 비즈니스 로직은 개발자 작성 필요

---

## 2. Claude Code 자동화 가능성

### 2.1 기존 명령어 패턴 분석

#### 현재 지원되는 명령어

```bash
# Domain Layer
/test-gen-domain Order        # Domain 테스트 자동 생성

# Application Layer
/test-gen-usecase PlaceOrder  # UseCase 테스트 자동 생성

# Persistence Layer
/test-gen-repository-integration OrderRepository  # Repository 통합 테스트

# REST API Layer
/test-gen-e2e OrderApi         # E2E 테스트
/test-gen-api-docs OrderApi    # API 문서
```

#### 명령어 구조 분석

```markdown
# /test-gen-domain.md 예시

목적: Domain 계층 단위 테스트 자동 생성

사용법:
```bash
/test-gen-domain <AggregateName>
```

생성 내용:
- Happy Path (성공 케이스)
- Edge Cases (경계값)
- Exception Cases (예외 처리)
```

**패턴**: `목적 → 사용법 → 생성 내용 → 예제`

### 2.2 Orchestration Pattern용 명령어 설계

#### A. `/code-gen-orchestrator` 명령어

```markdown
# .claude/commands/code-gen-orchestrator.md

## 목적
Orchestration Pattern Boilerplate 자동 생성

## 사용법
```bash
/code-gen-orchestrator <DomainName> <EventType>
```

**예시**:
```bash
/code-gen-orchestrator Payment PaymentRequested
/code-gen-orchestrator FileUpload FileUploadRequested
/code-gen-orchestrator Notification NotificationSent
```

## 생성 내용

### 1. Command Record (자동 생성)
- `{Domain}Command.java`
- Compact Constructor 검증
- Javadoc 포함

### 2. Orchestrator (골격 생성)
- `{Domain}Orchestrator.java`
- accept(), execute(), finalize() 메서드
- executeInternal() 템플릿 (개발자 구현 필요)
- Retry 전략 템플릿

### 3. Entities (자동 생성)
- `Operation.java`
- `WriteAheadLog.java`
- JPA 매핑, Long FK 전략

### 4. Repositories (자동 생성)
- `OperationRepository.java`
- `WriteAheadLogRepository.java`
- 필수 쿼리 메서드

### 5. Schedulers (자동 생성)
- `{Domain}Finalizer.java`
- `{Domain}Reaper.java`
- @Scheduled 설정

### 6. Controller (골격 생성)
- `{Domain}Controller.java`
- POST 엔드포인트
- Response 매핑

### 7. Tests (자동 생성)
- `{Domain}OrchestratorTest.java`
- Idempotency 테스트
- Retry 테스트
- WAL 테스트

## 실행 흐름

1️⃣ **키워드 감지**: user-prompt-submit.sh가 "orchestrator" 키워드 감지
2️⃣ **Layer 매핑**: enterprise 또는 application layer
3️⃣ **Serena 메모리 로드**: orchestration_convention 자동 로드
4️⃣ **Cache 규칙 주입**: inject-rules.py가 Orchestration 규칙 주입
5️⃣ **코드 생성**: Claude Code가 Boilerplate 생성
6️⃣ **실시간 검증**: validation-helper.py가 즉시 검증

## 예상 소요 시간
- Boilerplate 생성: **30초**
- 개발자 비즈니스 로직 구현: **10-20분**
- 테스트 작성: **자동 생성됨**

## 생성 파일 구조

```
application/
├── orchestrator/
│   ├── payment/
│   │   ├── PaymentCommand.java         ✅ 자동
│   │   ├── PaymentOrchestrator.java    ⚠️ 골격 (executeInternal 구현 필요)
│   │   ├── PaymentFinalizer.java       ✅ 자동
│   │   └── PaymentReaper.java          ✅ 자동
│
adapter-out/
├── persistence-mysql/
│   ├── orchestration/
│   │   ├── OperationEntity.java        ✅ 자동
│   │   ├── WriteAheadLogEntity.java    ✅ 자동
│   │   ├── OperationRepository.java    ✅ 자동
│   │   └── WriteAheadLogRepository.java ✅ 자동
│
adapter-in/
├── web/
│   ├── payment/
│   │   └── PaymentController.java      ⚠️ 골격 (Response DTO 매핑 필요)
│
tests/
├── orchestrator/
│   ├── PaymentOrchestratorTest.java    ✅ 자동 (Idempotency, Retry, WAL)
```

## 자동 생성 예제

### PaymentCommand.java (자동 생성)

```java
package com.example.application.orchestrator.payment;

import com.example.common.orchestration.*;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * 결제 요청 Command
 *
 * @param orderId 주문 ID
 * @param idempotencyKey 멱등성 키
 * @param amount 결제 금액
 * @author coding-convention-09
 * @since 1.0
 */
public record PaymentCommand(
    String orderId,
    String idempotencyKey,
    BigDecimal amount
) {
    public PaymentCommand {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey must not be null");
        Objects.requireNonNull(amount, "amount must not be null");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
```

### PaymentOrchestrator.java (골격 생성)

```java
package com.example.application.orchestrator.payment;

import com.example.common.orchestration.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 결제 Orchestrator
 *
 * <p>결제 요청을 Accept → Execute → Finalize 3단계로 처리합니다.</p>
 *
 * @author coding-convention-09
 * @since 1.0
 */
@Service
public class PaymentOrchestrator extends BaseOrchestrator<PaymentCommand> {

    // TODO: Inject dependencies (PaymentGateway, etc.)

    public PaymentOrchestrator(
        OperationRepository operationRepository,
        WriteAheadLogRepository walRepository
    ) {
        super(operationRepository, walRepository);
    }

    @Override
    protected Domain domain() {
        return Domain.PAYMENT;
    }

    @Override
    protected EventType eventType() {
        return EventType.PAYMENT_REQUESTED;
    }

    /**
     * 결제 실행 (외부 API 호출)
     *
     * <p><b>⚠️ 개발자 구현 필요</b>:</p>
     * <ul>
     *   <li>외부 결제 게이트웨이 호출</li>
     *   <li>성공 시: Outcome.ok() 반환</li>
     *   <li>일시적 오류 시: Outcome.retry() 반환</li>
     *   <li>영구적 오류 시: Outcome.fail() 반환</li>
     * </ul>
     *
     * @param opId Operation ID
     * @param cmd 결제 Command
     * @return 실행 결과
     */
    @Override
    @Async
    protected Outcome executeInternal(OpId opId, PaymentCommand cmd) {
        // TODO: Implement business logic
        //
        // Example:
        // try {
        //     String txId = paymentGateway.charge(cmd.orderId(), cmd.amount());
        //     return Outcome.ok(opId, "Payment completed: " + txId);
        // } catch (TransientException e) {
        //     return Outcome.retry(e.getMessage(), 1, 5000);
        // } catch (PermanentException e) {
        //     return Outcome.fail(e.getErrorCode(), e.getMessage(), "N/A");
        // }

        throw new UnsupportedOperationException(
            "executeInternal() must be implemented by developer"
        );
    }
}
```

### PaymentOrchestratorTest.java (자동 생성)

```java
package com.example.application.orchestrator.payment;

import com.example.common.orchestration.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * PaymentOrchestrator 테스트
 *
 * @author coding-convention-09
 * @since 1.0
 */
@SpringBootTest
class PaymentOrchestratorTest {

    @Autowired
    private PaymentOrchestrator orchestrator;

    @Autowired
    private OperationRepository operationRepository;

    @Test
    void accept_ShouldReturnOk_WhenValidCommand() {
        // Given
        PaymentCommand command = new PaymentCommand(
            "ORDER-001",
            "IDEM-12345",
            BigDecimal.valueOf(10000)
        );

        // When
        Outcome outcome = orchestrator.accept(command);

        // Then
        assertThat(outcome).isInstanceOf(Outcome.Ok.class);
        OpId opId = ((Outcome.Ok) outcome).opId();
        assertThat(opId).isNotNull();
    }

    @Test
    void accept_ShouldReturnSameOpId_WhenDuplicateIdemKey() {
        // Given
        PaymentCommand command = new PaymentCommand(
            "ORDER-002",
            "IDEM-67890",
            BigDecimal.valueOf(5000)
        );

        // When
        Outcome first = orchestrator.accept(command);
        Outcome second = orchestrator.accept(command);

        // Then
        assertThat(first).isInstanceOf(Outcome.Ok.class);
        assertThat(second).isInstanceOf(Outcome.Ok.class);

        OpId firstOpId = ((Outcome.Ok) first).opId();
        OpId secondOpId = ((Outcome.Ok) second).opId();

        assertThat(firstOpId).isEqualTo(secondOpId);
    }

    // TODO: Add more tests for:
    // - Retry logic
    // - WAL recovery
    // - Timeout handling
    // - Concurrent requests
}
```

## 개발자 작업 (TODO)

1. **executeInternal() 구현** (필수):
   - 외부 API 호출 로직
   - Error 분류 (Retryable/Fatal)
   - Outcome 반환

2. **Dependencies 주입** (필수):
   - PaymentGateway, FileStorageClient 등

3. **Response DTO 매핑** (선택):
   - Controller에서 Outcome → Response 변환

4. **추가 테스트** (권장):
   - 비즈니스 로직 엣지 케이스
   - 보안 테스트 (Rate Limiting)

## 컨벤션 자동 검증

생성된 코드는 즉시 검증됩니다:

✅ **Transaction 경계**: executeInternal()이 @Async인지 확인
✅ **Lombok 금지**: Record 패턴 사용 확인
✅ **Javadoc 필수**: 모든 public 클래스/메서드 확인
✅ **Long FK 전략**: Entity에 관계 어노테이션 없는지 확인
✅ **Idempotency**: IdemKey Unique 제약 확인

## 다음 단계

1. `/code-gen-orchestrator Payment PaymentRequested` 실행
2. 생성된 파일 확인
3. `executeInternal()` 구현
4. `/validate-architecture` 실행
5. 테스트 실행: `./gradlew test`
```

---

### 2.3 실행 흐름 상세 분석

#### Step 1: 키워드 감지 (user-prompt-submit.sh)

```bash
# user-prompt-submit.sh (일부)

# 키워드 점수 계산
declare -A LAYER_KEYWORDS

# Orchestration Pattern 키워드 추가
LAYER_KEYWORDS["orchestrator"]=40
LAYER_KEYWORDS["orchestration"]=40
LAYER_KEYWORDS["idempotency"]=35
LAYER_KEYWORDS["retry"]=30
LAYER_KEYWORDS["wal"]=30
LAYER_KEYWORDS["write-ahead-log"]=35
LAYER_KEYWORDS["outcome"]=25
LAYER_KEYWORDS["finalizer"]=30
LAYER_KEYWORDS["reaper"]=30

# 사용자 입력: "/code-gen-orchestrator Payment PaymentRequested"
# → "orchestrator" 키워드 감지 (40점)
# → Layer: "orchestration" 매핑
```

#### Step 2: Serena Memory 로드

```bash
# Serena Memory 자동 로드
read_memory("coding_convention_orchestration_layer")

# 메모리 내용:
# - 3-Phase Lifecycle (Accept → Execute → Finalize)
# - Idempotency 보장 (IdemKey Unique 제약)
# - WAL 패턴 (Crash Recovery)
# - Outcome Modeling (Sealed interface)
# - Transaction 경계 (executeInternal @Async)
```

#### Step 3: Cache 규칙 주입 (inject-rules.py)

```python
# inject-rules.py 호출

# Orchestration Pattern 규칙 로드
rules = [
    "orchestration-pattern-overview",
    "command-pattern",
    "idempotency-handling",
    "write-ahead-log-pattern",
    "outcome-modeling"
]

# Claude에게 규칙 주입
for rule in rules:
    rule_content = cache.load(f"orchestration-{rule}.json")
    inject_to_claude(rule_content)
```

#### Step 4: 코드 생성 (Claude Code)

```
Claude Code 실행 흐름:
1. Serena Memory 우선 참조 (컨텍스트 유지)
2. Cache 규칙 보조 참조 (고속 검색)
3. 템플릿 기반 코드 생성:
   - Command.java (100% 자동)
   - Orchestrator.java (70% 자동, executeInternal TODO)
   - Entities (100% 자동)
   - Repositories (100% 자동)
   - Schedulers (100% 자동)
   - Tests (90% 자동, 비즈니스 로직 테스트 TODO)
```

#### Step 5: 실시간 검증 (after-tool-use.sh)

```bash
# after-tool-use.sh 호출

# validation-helper.py 실행
python3 .claude/hooks/scripts/validation-helper.py \
    --file application/orchestrator/payment/PaymentOrchestrator.java \
    --layer orchestration

# 검증 항목:
# ✅ Transaction 경계 (@Async executeInternal)
# ✅ Lombok 금지 (Record 패턴 사용)
# ✅ Javadoc 필수 (모든 public 클래스/메서드)
# ✅ Long FK 전략 (Entity에 관계 어노테이션 없음)
# ✅ Idempotency (IdemKey Unique 제약)
```

---

## 3. Windsurf 워크플로우 가능성

### 3.1 Windsurf 워크플로우 설계

#### A. 워크플로우 파일 생성

```yaml
# .windsurf/workflows/09-orchestration/create-orchestrator.yaml

name: Create Orchestration Pattern
description: Orchestration Pattern Boilerplate 자동 생성

metadata:
  author: coding-convention-09
  version: 1.0
  last_updated: 2025-01-15

# 워크플로우 실행 조건
triggers:
  keywords:
    - "orchestrator 생성"
    - "orchestration pattern 적용"
    - "멱등성 추가"
    - "외부 API 호출 안전하게"

# 사용자 입력
inputs:
  - name: domain
    type: string
    description: "Domain 이름 (예: Payment, FileUpload)"
    required: true
    validation:
      pattern: "^[A-Z][a-zA-Z0-9]*$"
      message: "PascalCase 형식이어야 합니다 (예: Payment)"

  - name: eventType
    type: string
    description: "Event 타입 (예: PaymentRequested, FileUploadRequested)"
    required: true
    validation:
      pattern: "^[A-Z][a-zA-Z0-9]*$"
      message: "PascalCase 형식이어야 합니다"

  - name: externalApi
    type: string
    description: "외부 API 이름 (예: PaymentGateway, S3Client)"
    required: true

# 워크플로우 단계
steps:
  # Step 1: Command 생성
  - id: generate_command
    name: "Command Record 생성"
    action: generate_file
    template: "@templates/orchestration/Command.java.template"
    output: "application/orchestrator/${domain.toLowerCase()/${domain}Command.java"
    variables:
      - domain: ${domain}
      - package: "com.example.application.orchestrator.${domain.toLowerCase()}"
    validation:
      - rule: "no_lombok"
        message: "Lombok 사용 금지. Record 패턴을 사용하세요."
      - rule: "javadoc_required"
        message: "@author, @since 필수"

  # Step 2: Orchestrator 골격 생성
  - id: generate_orchestrator
    name: "Orchestrator 골격 생성"
    action: generate_file
    template: "@templates/orchestration/Orchestrator.java.template"
    output: "application/orchestrator/${domain.toLowerCase()}/${domain}Orchestrator.java"
    variables:
      - domain: ${domain}
      - eventType: ${eventType}
      - externalApi: ${externalApi}
    validation:
      - rule: "transaction_boundary"
        message: "executeInternal()은 @Async여야 합니다"
      - rule: "javadoc_required"

  # Step 3: Entities 생성
  - id: generate_entities
    name: "Operation & WAL Entity 생성"
    action: generate_files
    templates:
      - "@templates/orchestration/OperationEntity.java.template"
      - "@templates/orchestration/WriteAheadLogEntity.java.template"
    output_dir: "adapter-out/persistence-mysql/orchestration/"
    validation:
      - rule: "long_fk_strategy"
        message: "JPA 관계 어노테이션 금지"
      - rule: "no_lombok"

  # Step 4: Repositories 생성
  - id: generate_repositories
    name: "Repository Interface 생성"
    action: generate_files
    templates:
      - "@templates/orchestration/OperationRepository.java.template"
      - "@templates/orchestration/WriteAheadLogRepository.java.template"
    output_dir: "adapter-out/persistence-mysql/orchestration/"

  # Step 5: Schedulers 생성
  - id: generate_schedulers
    name: "Finalizer & Reaper 생성"
    action: generate_files
    templates:
      - "@templates/orchestration/Finalizer.java.template"
      - "@templates/orchestration/Reaper.java.template"
    output_dir: "application/orchestrator/${domain.toLowerCase()}/"
    variables:
      - domain: ${domain}

  # Step 6: Controller 골격 생성
  - id: generate_controller
    name: "REST Controller 골격 생성"
    action: generate_file
    template: "@templates/orchestration/Controller.java.template"
    output: "adapter-in/web/${domain.toLowerCase()}/${domain}Controller.java"
    variables:
      - domain: ${domain}

  # Step 7: Tests 생성
  - id: generate_tests
    name: "Orchestrator 테스트 생성"
    action: generate_file
    template: "@templates/orchestration/OrchestratorTest.java.template"
    output: "application/src/test/java/orchestrator/${domain.toLowerCase()}/${domain}OrchestratorTest.java"
    variables:
      - domain: ${domain}

# 워크플로우 완료 후 작업
post_steps:
  - id: validate_architecture
    name: "ArchUnit 검증"
    action: run_command
    command: "./gradlew test --tests=*ArchitectureTest"

  - id: show_todos
    name: "개발자 TODO 표시"
    action: display_message
    message: |
      ✅ Orchestration Pattern Boilerplate 생성 완료!

      📋 개발자 TODO:
      1. ${domain}Orchestrator.executeInternal() 구현 (필수)
         - 외부 API 호출 로직
         - Error 분류 (Retryable/Fatal)
         - Outcome 반환

      2. Dependencies 주입 (필수)
         - ${externalApi} 주입

      3. Response DTO 매핑 (선택)
         - ${domain}Controller에서 Outcome → Response 변환

      4. 추가 테스트 작성 (권장)
         - 비즈니스 로직 엣지 케이스
         - 보안 테스트 (Rate Limiting)

      📝 다음 단계:
      1. executeInternal() 구현
      2. ./gradlew test 실행
      3. /validate-architecture 실행

# 에러 처리
error_handling:
  - on_validation_failure:
      action: rollback
      message: "검증 실패. 모든 변경 사항을 롤백합니다."

  - on_file_conflict:
      action: prompt_user
      message: "파일이 이미 존재합니다. 덮어쓸까요?"
      options:
        - "덮어쓰기"
        - "건너뛰기"
        - "새 이름으로 생성"
```

#### B. 템플릿 파일 생성

```java
// .windsurf/templates/orchestration/Command.java.template

package {{package}};

import com.example.common.orchestration.*;
import java.util.Objects;

/**
 * {{domain}} 요청 Command
 *
 * @author coding-convention-09
 * @since 1.0
 */
public record {{domain}}Command(
    // TODO: Add command fields
    String idempotencyKey
) {
    public {{domain}}Command {
        Objects.requireNonNull(idempotencyKey, "idempotencyKey must not be null");
        // TODO: Add validation
    }
}
```

```java
// .windsurf/templates/orchestration/Orchestrator.java.template

package {{package}};

import com.example.common.orchestration.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * {{domain}} Orchestrator
 *
 * @author coding-convention-09
 * @since 1.0
 */
@Service
public class {{domain}}Orchestrator extends BaseOrchestrator<{{domain}}Command> {

    // TODO: Inject {{externalApi}}

    @Override
    protected Domain domain() {
        return Domain.{{domain.toUpperCase()}};
    }

    @Override
    protected EventType eventType() {
        return EventType.{{eventType.toUpperCase()}};
    }

    @Override
    @Async
    protected Outcome executeInternal(OpId opId, {{domain}}Command cmd) {
        // TODO: Implement business logic
        throw new UnsupportedOperationException("executeInternal() must be implemented");
    }
}
```

### 3.2 Windsurf Rules 생성

```markdown
# .windsurf/rules/09-orchestration-layer.md

# Orchestration Layer 규칙 (Windsurf 자동 로드)

## 🎯 핵심 원칙

1. **3-Phase Lifecycle**: Accept → Execute → Finalize
2. **Idempotency 보장**: IdemKey Unique 제약
3. **WAL 패턴**: Crash Recovery
4. **Outcome Modeling**: Sealed interface (Ok/Retry/Fail)
5. **Transaction 경계**: executeInternal은 @Async

## ❌ 금지 규칙

- ❌ **executeInternal 내 @Transactional**: 트랜잭션 밖에서 실행
- ❌ **IdemKey 없이 Operation 생성**: 멱등성 필수
- ❌ **Outcome 대신 boolean/Exception**: Sealed interface 사용
- ❌ **Finalizer/Reaper 없이 Orchestrator**: Recovery 필수

## ✅ 필수 규칙

- ✅ **Command Record**: 불변 객체, Compact Constructor 검증
- ✅ **BaseOrchestrator 상속**: accept/execute/finalize 재사용
- ✅ **IdemKey Unique 제약**: DB level 중복 차단
- ✅ **WAL PENDING 처리**: Finalizer가 자동 복구
- ✅ **Timeout 처리**: Reaper가 MAX_ATTEMPTS 초과 시 TIMEOUT

## 📋 체크리스트

### Command 생성 시
- [ ] Record 패턴 사용 (Lombok 금지)
- [ ] Compact Constructor 검증 (Objects.requireNonNull)
- [ ] Javadoc 작성 (@author, @since)

### Orchestrator 생성 시
- [ ] BaseOrchestrator 상속
- [ ] domain(), eventType() 오버라이드
- [ ] executeInternal() @Async 선언
- [ ] Retry 전략 구현 (Exponential Backoff)
- [ ] Error 분류 (Retryable/Fatal)

### Entity 생성 시
- [ ] Long FK 전략 (JPA 관계 금지)
- [ ] IdemKey Unique 제약
- [ ] Javadoc 작성

### Scheduler 생성 시
- [ ] @Scheduled(fixedDelay = 5000)
- [ ] Finalizer: PENDING WAL 처리
- [ ] Reaper: TIMEOUT 처리

## 🔍 검증 항목

- ✅ Transaction 경계: executeInternal이 @Async인가?
- ✅ Lombok 금지: Record 패턴 사용했는가?
- ✅ Javadoc 필수: @author, @since 있는가?
- ✅ Long FK 전략: Entity에 관계 어노테이션 없는가?
- ✅ Idempotency: IdemKey Unique 제약 있는가?
```

---

## 4. 컨벤션 강제 메커니즘

### 4.1 3단계 방어선

#### 1️⃣ 사전 방어 (Pre-generation)

```bash
# user-prompt-submit.sh + inject-rules.py

# Serena Memory 로드 (최우선)
read_memory("coding_convention_orchestration_layer")

# Cache 규칙 주입 (보조)
inject_rules \
    --layer orchestration \
    --rules "command-pattern,idempotency,wal,outcome"
```

#### 2️⃣ 실시간 방어 (Real-time)

```bash
# after-tool-use.sh + validation-helper.py

# 코드 생성 직후 즉시 검증
validation-helper.py \
    --file PaymentOrchestrator.java \
    --layer orchestration \
    --rules "transaction-boundary,lombok-prohibited,javadoc-required"

# 위반 시 즉시 경고 + 수정 제안
```

#### 3️⃣ 사후 방어 (Post-generation)

```bash
# Git Pre-commit Hook + ArchUnit

# Commit 전 강제 검증
hooks/pre-commit
    → validate-transaction-boundary.sh
    → validate-proxy-constraints.sh

# 빌드 시 강제 검증
./gradlew test
    → ArchitectureTest.java (ArchUnit)
    → OrchestrationConventionTest.java (Custom)
```

### 4.2 컨벤션 강제 검증 코드

#### A. validation-helper.py 확장

```python
# .claude/hooks/scripts/validation-helper.py (일부)

def validate_orchestration_layer(file_path, content):
    """Orchestration Layer 컨벤션 검증"""
    violations = []

    # 1. Transaction 경계 검증
    if "executeInternal" in content and "@Async" not in content:
        violations.append({
            "rule": "transaction-boundary",
            "severity": "CRITICAL",
            "message": "executeInternal()은 @Async여야 합니다",
            "file": file_path,
            "suggestion": "@Async 어노테이션을 추가하세요"
        })

    # 2. Lombok 금지 검증
    if any(lombok in content for lombok in ["@Data", "@Builder", "@Getter", "@Setter"]):
        violations.append({
            "rule": "lombok-prohibited",
            "severity": "CRITICAL",
            "message": "Lombok 사용 금지",
            "file": file_path,
            "suggestion": "Record 패턴을 사용하세요"
        })

    # 3. Idempotency 검증 (IdemKey)
    if "Operation.create" in content and "idemKey" not in content.lower():
        violations.append({
            "rule": "idempotency-required",
            "severity": "CRITICAL",
            "message": "IdemKey 누락",
            "file": file_path,
            "suggestion": "Command에 idempotencyKey 필드를 추가하세요"
        })

    # 4. WAL 패턴 검증
    if "finalize" in content.lower() and "WriteAheadLog" not in content:
        violations.append({
            "rule": "wal-pattern-required",
            "severity": "HIGH",
            "message": "WAL 패턴 누락",
            "file": file_path,
            "suggestion": "finalize() 전에 WAL을 기록하세요"
        })

    # 5. Outcome Modeling 검증
    if "executeInternal" in content and "Outcome" not in content:
        violations.append({
            "rule": "outcome-modeling-required",
            "severity": "HIGH",
            "message": "Outcome 반환 누락",
            "file": file_path,
            "suggestion": "Outcome.ok/retry/fail을 반환하세요"
        })

    return violations
```

#### B. ArchUnit 검증 추가

```java
package com.example.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * Orchestration Layer 아키텍처 규칙 검증
 *
 * @author coding-convention-09
 * @since 1.0
 */
class OrchestrationConventionTest {

    private final JavaClasses classes = new ClassFileImporter()
        .importPackages("com.example.application.orchestrator");

    @Test
    void orchestrator_ShouldExtendBaseOrchestrator() {
        classes()
            .that().haveSimpleNameEndingWith("Orchestrator")
            .should().beAssignableTo("BaseOrchestrator")
            .check(classes);
    }

    @Test
    void executeInternal_ShouldBeAsync() {
        methods()
            .that().haveName("executeInternal")
            .should().beAnnotatedWith("org.springframework.scheduling.annotation.Async")
            .check(classes);
    }

    @Test
    void command_ShouldBeRecord() {
        classes()
            .that().haveSimpleNameEndingWith("Command")
            .should().beRecords()  // Java 16+ Record
            .check(classes);
    }

    @Test
    void entities_ShouldNotUseJpaRelations() {
        classes()
            .that().resideInPackage("..orchestration..")
            .and().haveSimpleNameEndingWith("Entity")
            .should().notBeAnnotatedWith("javax.persistence.ManyToOne")
            .andShould().notBeAnnotatedWith("javax.persistence.OneToMany")
            .check(classes);
    }

    @Test
    void operation_ShouldHaveIdemKeyUniqueConstraint() {
        // TODO: Check @Table(uniqueConstraints = @UniqueConstraint(columnNames = "idem_key"))
    }
}
```

#### C. Git Pre-commit Hook 추가

```bash
#!/bin/bash
# hooks/pre-commit (일부)

# Orchestration Pattern 트랜잭션 경계 검증
check_orchestration_transaction_boundary() {
    local files=$(git diff --cached --name-only --diff-filter=ACM | grep "Orchestrator\.java$")

    for file in $files; do
        if grep -q "executeInternal" "$file"; then
            if ! grep -q "@Async" "$file"; then
                echo "❌ Transaction 경계 위반: $file"
                echo "   executeInternal()은 @Async여야 합니다"
                exit 1
            fi
        fi
    done
}

check_orchestration_transaction_boundary
```

---

## 5. 구현 제안

### 5.1 Phase 1: Claude Code Slash Command (우선순위: 높음)

#### 작업 항목

1. **명령어 파일 생성** (1시간)
   - `.claude/commands/code-gen-orchestrator.md`
   - 사용법, 생성 내용, 예제 작성

2. **Serena Memory 생성** (30분)
   - `coding_convention_orchestration_layer` 메모리
   - 5개 문서 요약 (Overview, Command, Idempotency, WAL, Outcome)

3. **Cache 규칙 추가** (30분)
   - `orchestration-pattern-overview.json`
   - `command-pattern.json`
   - `idempotency-handling.json`
   - `write-ahead-log-pattern.json`
   - `outcome-modeling.json`

4. **Hook 스크립트 확장** (1시간)
   - `user-prompt-submit.sh`: "orchestrator" 키워드 추가
   - `validation-helper.py`: Orchestration 검증 로직 추가

5. **테스트** (1시간)
   - `/code-gen-orchestrator Payment PaymentRequested` 실행
   - 생성 파일 검증
   - 컨벤션 준수 확인

**예상 소요 시간**: **4시간**

---

### 5.2 Phase 2: Windsurf Workflow (우선순위: 중간)

#### 작업 항목

1. **워크플로우 YAML 작성** (2시간)
   - `.windsurf/workflows/09-orchestration/create-orchestrator.yaml`
   - inputs, steps, validation, error_handling

2. **템플릿 파일 생성** (2시간)
   - Command.java.template
   - Orchestrator.java.template
   - OperationEntity.java.template
   - WriteAheadLogEntity.java.template
   - Repositories.java.template
   - Schedulers.java.template
   - Controller.java.template
   - OrchestratorTest.java.template

3. **Windsurf Rules 작성** (1시간)
   - `.windsurf/rules/09-orchestration-layer.md`
   - 핵심 원칙, 금지 규칙, 필수 규칙, 체크리스트

4. **테스트** (1시간)
   - IntelliJ Cascade에서 워크플로우 실행
   - 생성 파일 검증
   - Rule 자동 로드 확인

**예상 소요 시간**: **6시간**

---

### 5.3 Phase 3: 컨벤션 강제 (우선순위: 높음)

#### 작업 항목

1. **validation-helper.py 확장** (2시간)
   - Orchestration Layer 검증 로직 추가
   - Transaction 경계, Lombok, Idempotency, WAL, Outcome 검증

2. **ArchUnit 테스트 추가** (2시간)
   - `OrchestrationConventionTest.java` 작성
   - BaseOrchestrator 상속, @Async, Record, JPA 관계 금지

3. **Git Pre-commit Hook 추가** (1시간)
   - `check_orchestration_transaction_boundary()` 함수

4. **테스트** (1시간)
   - 위반 코드 작성 → 검증 실패 확인
   - 정상 코드 작성 → 검증 통과 확인

**예상 소요 시간**: **6시간**

---

### 5.4 전체 일정 요약

| Phase | 작업 | 소요 시간 | 우선순위 |
|-------|------|----------|---------|
| Phase 1 | Claude Code Slash Command | 4시간 | 🔴 높음 |
| Phase 2 | Windsurf Workflow | 6시간 | 🟡 중간 |
| Phase 3 | 컨벤션 강제 | 6시간 | 🔴 높음 |
| **총계** | | **16시간** (2일) | |

---

## 6. 결론

### 6.1 자동화 가능성 평가

| 항목 | 자동화 가능 | 이유 |
|------|-------------|------|
| **Command Record** | ✅ 100% | Record 패턴, 검증 로직 표준화 |
| **Orchestrator 골격** | ✅ 70% | executeInternal()만 개발자 작성 |
| **Entities** | ✅ 100% | JPA 매핑, Long FK 전략 표준화 |
| **Repositories** | ✅ 100% | 쿼리 메서드 표준화 |
| **Schedulers** | ✅ 100% | @Scheduled 설정 표준화 |
| **Controller 골격** | ✅ 60% | Response DTO 매핑은 개발자 작성 |
| **Tests** | ✅ 90% | 비즈니스 로직 테스트만 개발자 작성 |

**전체 자동화율**: **80-85%**

### 6.2 컨벤션 강제 가능성 평가

| 메커니즘 | 강제 가능 | 효과 |
|----------|----------|------|
| **Serena Memory** | ✅ 최우선 | 세션 간 컨텍스트 유지 (78% 위반 감소) |
| **Cache 규칙** | ✅ 보조 | 고속 검색 (90% 토큰 절감) |
| **validation-helper.py** | ✅ 실시간 | 코드 생성 직후 즉시 검증 |
| **ArchUnit** | ✅ 빌드 시 | 아키텍처 규칙 강제 |
| **Git Pre-commit Hook** | ✅ 커밋 시 | 트랜잭션 경계 강제 |

**컨벤션 준수율**: **90-95%** (기존 78% → 90-95%)

### 6.3 최종 권장 사항

#### ✅ 권장 사항 (Recommended)

1. **Phase 1 (Claude Code) 우선 구현**: 4시간으로 80% 자동화 달성
2. **Phase 3 (컨벤션 강제) 즉시 구현**: 6시간으로 90-95% 준수율 달성
3. **Phase 2 (Windsurf) 선택 구현**: 6시간 추가 투자 시 Windsurf 통합

#### 💡 핵심 이점

- **개발 시간 70% 단축**: Boilerplate 자동 생성
- **컨벤션 위반 90% 감소**: 자동 검증 3단계 방어선
- **생산성 300% 향상**: 반복 작업 자동화 + 비즈니스 로직 집중

---

## 7. 다음 단계

### 즉시 실행 가능 (Quick Win)

```bash
# 1. Claude Code 명령어 생성
cat > .claude/commands/code-gen-orchestrator.md << 'EOF'
[위 명령어 내용 복사]
EOF

# 2. Serena Memory 생성
bash .claude/hooks/scripts/setup-serena-conventions.sh --add orchestration

# 3. Cache 규칙 추가
python3 .claude/hooks/scripts/build-rule-cache.py \
    --source docs/coding_convention/09-orchestration-patterns/ \
    --output .claude/cache/rules/

# 4. 테스트
/code-gen-orchestrator Payment PaymentRequested
```

### 중장기 계획 (Roadmap)

- **1주차**: Phase 1 (Claude Code) 완료
- **2주차**: Phase 3 (컨벤션 강제) 완료
- **3주차**: Phase 2 (Windsurf) 완료
- **4주차**: 프로덕션 적용 및 피드백 수집

---

**✅ 결론**: Orchestration Pattern은 **80-85% 자동화 가능**하며, **90-95% 컨벤션 준수율**을 달성할 수 있습니다.
