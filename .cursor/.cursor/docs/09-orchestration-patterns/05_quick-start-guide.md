# 05. Quick Start Guide: 10분 만에 Orchestration Pattern 적용하기

## 1. 개요

이 가이드는 **10분 안에** Orchestration Pattern을 기존 프로젝트에 적용하는 방법을 제공합니다.

**목표**: 결제 시스템에 멱등성, 크래시 복구, 재시도 기능 추가

---

## 2. Step-by-Step 구현

### Step 1: Gradle Dependency 추가 (1분)

```gradle
// build.gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-scheduling'

    // Optional: Observability (Micrometer)
    implementation 'io.micrometer:micrometer-registry-prometheus'
}
```

**설정 활성화**:
```yaml
# application.yml
spring:
  task:
    scheduling:
      pool:
        size: 5  # Finalizer, Reaper 스레드 풀
```

---

### Step 2: 핵심 클래스 생성 (3분)

#### 2.1 Command 정의

```java
package com.example.payment.application.command;

import com.example.common.orchestration.*;

/**
 * 결제 요청 Command
 *
 * @param orderId 주문 ID
 * @param idempotencyKey 멱등성 키 (클라이언트 생성)
 * @param amount 결제 금액
 */
public record PaymentCommand(
    String orderId,
    String idempotencyKey,
    BigDecimal amount
) {
    // Validation은 Compact Constructor에서
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

#### 2.2 Orchestrator 구현

```java
package com.example.payment.application.orchestrator;

import com.example.common.orchestration.*;
import com.example.payment.domain.PaymentGateway;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentOrchestrator extends BaseOrchestrator<PaymentCommand> {

    private final PaymentGateway paymentGateway;

    public PaymentOrchestrator(
        OperationRepository operationRepository,
        WriteAheadLogRepository walRepository,
        PaymentGateway paymentGateway
    ) {
        super(operationRepository, walRepository);
        this.paymentGateway = paymentGateway;
    }

    @Override
    protected Domain domain() {
        return Domain.PAYMENT;
    }

    @Override
    protected EventType eventType() {
        return EventType.PAYMENT_REQUESTED;
    }

    @Override
    @Async  // 비동기 실행 (Accept와 분리)
    protected Outcome executeInternal(OpId opId, PaymentCommand cmd) {
        try {
            // 외부 결제 게이트웨이 호출 (트랜잭션 밖)
            String transactionId = paymentGateway.charge(
                cmd.orderId(),
                cmd.amount()
            );

            return Outcome.ok(opId, "Payment completed: " + transactionId);

        } catch (PaymentGatewayException e) {
            if (e.isRetryable()) {
                // 일시적 오류 → 재시도
                return Outcome.retry(
                    e.getMessage(),
                    1,  // attemptCount
                    calculateBackoff(1)  // 5초 후 재시도
                );
            } else {
                // 영구적 오류 → 실패
                return Outcome.fail(
                    e.getErrorCode(),
                    e.getMessage(),
                    e.getCause() != null ? e.getCause().toString() : "N/A"
                );
            }
        }
    }

    private long calculateBackoff(int attemptCount) {
        // Exponential backoff: 5초, 10초, 20초, ...
        return 5000L * (long) Math.pow(2, attemptCount - 1);
    }
}
```

---

### Step 3: Controller 연동 (2분)

```java
package com.example.payment.adapter.in.web;

import com.example.common.orchestration.Outcome;
import com.example.payment.application.orchestrator.PaymentOrchestrator;
import com.example.payment.application.command.PaymentCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentOrchestrator paymentOrchestrator;

    public PaymentController(PaymentOrchestrator paymentOrchestrator) {
        this.paymentOrchestrator = paymentOrchestrator;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
        @RequestBody PaymentRequest request
    ) {
        // 1️⃣ Command 생성
        PaymentCommand command = new PaymentCommand(
            request.orderId(),
            request.idempotencyKey(),  // 클라이언트가 생성한 멱등성 키
            request.amount()
        );

        // 2️⃣ Accept (멱등성 보장)
        Outcome outcome = paymentOrchestrator.accept(command);

        // 3️⃣ Response 반환 (Accept 즉시 완료)
        return switch (outcome) {
            case Outcome.Ok(var opId, var msg) ->
                ResponseEntity.accepted()  // 202 Accepted
                    .body(new PaymentResponse(opId.value(), "ACCEPTED", msg));

            case Outcome.Fail(var code, var msg, var cause) ->
                ResponseEntity.badRequest()
                    .body(new PaymentResponse(null, "FAILED", msg));

            default ->
                ResponseEntity.internalServerError()
                    .body(new PaymentResponse(null, "ERROR", "Unexpected outcome"));
        };
    }
}
```

---

### Step 4: 스케줄러 활성화 (1분)

```java
package com.example.common.orchestration.scheduler;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class OrchestrationConfig {
    // Finalizer, Reaper 자동 활성화
}
```

---

### Step 5: 테스트 (3분)

#### 5.1 Idempotency 테스트

```java
@SpringBootTest
class PaymentOrchestratorTest {

    @Autowired
    private PaymentOrchestrator orchestrator;

    @Test
    void accept_ShouldReturnSameOpId_WhenDuplicateIdemKey() {
        // Given
        PaymentCommand command = new PaymentCommand(
            "ORDER-001",
            "IDEM-12345",
            BigDecimal.valueOf(10000)
        );

        // When: 동일 IdemKey로 2번 요청
        Outcome first = orchestrator.accept(command);
        Outcome second = orchestrator.accept(command);

        // Then: 같은 OpId 반환
        assertThat(first).isInstanceOf(Outcome.Ok.class);
        assertThat(second).isInstanceOf(Outcome.Ok.class);

        OpId firstOpId = ((Outcome.Ok) first).opId();
        OpId secondOpId = ((Outcome.Ok) second).opId();

        assertThat(firstOpId).isEqualTo(secondOpId);
    }
}
```

#### 5.2 Retry 테스트

```java
@Test
void execute_ShouldRetry_WhenTransientError() {
    // Given: PaymentGateway가 일시적 오류
    PaymentGatewayMock gateway = new PaymentGatewayMock()
        .failTimes(2, new TransientException("Timeout"))
        .thenSucceed();

    // When: Accept → Execute
    PaymentCommand command = new PaymentCommand(
        "ORDER-002", "IDEM-67890", BigDecimal.valueOf(5000)
    );
    Outcome acceptOutcome = orchestrator.accept(command);
    OpId opId = ((Outcome.Ok) acceptOutcome).opId();

    // Execute는 비동기 → Finalizer가 처리
    await().atMost(30, SECONDS).until(() ->
        operationRepository.findById(opId)
            .map(op -> op.state() == OperationState.COMPLETED)
            .orElse(false)
    );

    // Then: 3번째 시도에서 성공
    verify(gateway, times(3)).charge(anyString(), any(BigDecimal.class));
}
```

---

## 3. 동작 확인

### 3.1 첫 번째 요청 (정상)

```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORDER-001",
    "idempotencyKey": "IDEM-12345",
    "amount": 10000
  }'
```

**Response (202 Accepted)**:
```json
{
  "opId": "OP-a1b2c3d4",
  "status": "ACCEPTED",
  "message": "Operation accepted"
}
```

### 3.2 중복 요청 (멱등성 보장)

```bash
# 동일한 idempotencyKey로 재요청
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORDER-001",
    "idempotencyKey": "IDEM-12345",
    "amount": 10000
  }'
```

**Response (202 Accepted - 같은 opId)**:
```json
{
  "opId": "OP-a1b2c3d4",  // 동일한 OpId
  "status": "ACCEPTED",
  "message": "Operation already accepted"
}
```

---

## 4. 핵심 포인트

### ✅ 달성한 것들

1. **멱등성 보장**: 동일 `idempotencyKey` → 중복 결제 차단
2. **크래시 복구**: 서버 재시작 시 Finalizer가 PENDING WAL 재처리
3. **재시도 로직**: 일시적 오류 시 Exponential Backoff로 재시도
4. **비동기 처리**: Accept는 빠르게 응답, Execute는 백그라운드 실행

### 🎯 트랜잭션 경계

```
[Accept 단계]
- @Transactional (짧게 유지)
- DB에 Operation 저장만
- IdemKey 중복 체크

[Execute 단계]
- @Async (비동기)
- 트랜잭션 없음
- 외부 API 호출

[Finalize 단계]
- @Transactional (짧게 유지)
- WAL 기록 + 상태 변경
```

---

## 5. 다음 단계

### 5.1 모니터링 추가

```java
@Component
public class OrchestrationMetrics {

    private final MeterRegistry registry;

    public void recordAccept(Domain domain, String result) {
        registry.counter("orchestration.accept.total",
            "domain", domain.name(),
            "result", result
        ).increment();
    }
}
```

### 5.2 보안 강화

```java
@Component
public class IdemKeyValidator {

    public void validate(String idemKey, String clientIp) {
        // Rate Limiting: 동일 IdemKey로 5분에 5회 초과 시 차단
        if (rateLimiter.isExceeded(idemKey, clientIp)) {
            throw new TooManyRequestsException("Too many attempts");
        }
    }
}
```

---

## 6. FAQ

### Q1: Accept는 왜 202 Accepted를 반환하나요?

**A**: Accept는 요청을 **받아들였다는 것만** 의미합니다. 실제 결제는 비동기로 처리되므로 200 OK가 아닌 202를 반환합니다.

### Q2: 클라이언트는 결과를 어떻게 확인하나요?

**A**: 두 가지 방법이 있습니다:
1. **Polling**: `GET /api/operations/{opId}` 엔드포인트 제공
2. **Webhook**: 결제 완료 시 클라이언트에 콜백

### Q3: IdemKey는 누가 생성하나요?

**A**: **클라이언트**가 생성합니다. 일반적으로 `{userId}:{orderId}:{timestamp}` 형태의 UUID를 사용합니다.

### Q4: Retry는 몇 번까지 하나요?

**A**: `maxAttempts` 설정에 따라 다릅니다. 기본값은 3회이며, Reaper가 MAX_ATTEMPTS 초과 시 TIMEOUT 처리합니다.

---

## 7. 요약

```
✅ 10분 소요
✅ 5개 파일 작성 (Command, Orchestrator, Controller, Config, Test)
✅ 멱등성 + 크래시 복구 + 재시도 모두 지원
✅ 프로덕션 Ready 코드
```

**다음 단계**: [06_security-guide.md](./06_security-guide.md)에서 보안 강화 방법을 확인하세요.
