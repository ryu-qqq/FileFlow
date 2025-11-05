# Orchestration Patterns - 외부 API 호출 조율 패턴 개요

**목적**: 외부 API 호출(결제, 파일, 써드파티)을 수반하는 업무 플로우에서 업무 원자성과 최종 일관성 보장

**관련 문서**:
- [Command Pattern](./01_command-pattern.md)
- [Idempotency Handling](./02_idempotency-handling.md)
- [Write-Ahead Log Pattern](./03_write-ahead-log-pattern.md)
- [Transactional Outbox Pattern](./04_outbox-pattern.md) - 트랜잭션 경계 외부 API 호출
- [Outcome Modeling](./04_outcome-modeling.md)
- [Quick Start Guide](./05_quick-start-guide.md)
- [Security Guide](./06_security-guide.md)
- [Automation Analysis](./07_automation-analysis.md)

**필수 버전**: Java 21+, Spring Framework 6.0+

---

## 📌 핵심 문제

### 문제 상황: 외부 API 호출의 불확실성

```java
// ❌ Before - 단순한 외부 API 호출 (문제 많음)
@Service
public class PaymentService {

    /**
     * ❌ 문제점:
     * - 네트워크 실패 시 요청이 도착했는지 알 수 없음 (멱등성 없음)
     * - 타임아웃 후 재시도 시 중복 결제 위험
     * - 실패 시 복구 불가능 (수동 처리 필요)
     * - 부분 실패 시 불일치 상태 (DB는 성공, API는 실패)
     */
    @Transactional
    public void processPayment(PaymentRequest request) {
        // 1. DB에 결제 기록 저장
        Payment payment = paymentRepository.save(Payment.create(request));

        // 2. 외부 결제 API 호출
        PaymentApiResponse response = paymentGateway.charge(
            request.amount(),
            request.cardNumber()
        );  // ⚠️ 네트워크 실패? 타임아웃? 중복 요청?

        // 3. 결과 업데이트
        payment.markAsCompleted(response.transactionId());
        paymentRepository.save(payment);
    }
}
```

**발생 가능한 문제**:
1. **중복 요청**: 타임아웃 후 재시도 시 동일 결제가 2번 실행
2. **부분 실패**: DB는 저장되었지만 API 호출 실패
3. **복구 불가**: 실패 후 어디서부터 재시도해야 하는지 알 수 없음
4. **상태 불일치**: DB 상태와 외부 시스템 상태가 다름

---

## 🎯 Orchestration Patterns의 해결책

### 3단계 수명주기 (3-Phase Lifecycle)

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  S1: Accept        S2: Execute       S3: Finalize         │
│  (수락)            (실행)             (종결)                │
│                                                             │
│  ┌──────────┐     ┌──────────┐     ┌──────────┐          │
│  │  멱등성  │ →   │ 외부 API │ →   │   WAL    │          │
│  │  검사    │     │  호출    │     │  완료    │          │
│  └──────────┘     └──────────┘     └──────────┘          │
│       ↓                ↓                 ↓                 │
│  PENDING      IN_PROGRESS        COMPLETED/FAILED         │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 핵심 패턴 조합

| 패턴 | 목적 | 해결하는 문제 |
|------|------|--------------|
| **Command Pattern** | 실행 요청 캡슐화 | 실행 정보를 명확하게 표현 |
| **Idempotency Handling** | 중복 요청 방지 | 동일 요청의 중복 실행 방지 |
| **Write-Ahead Log** | 실패 복구 | 크래시 후 복구 가능성 보장 |
| **Outcome Modeling** | 결과 타입 안전성 | Ok/Retry/Fail 명확한 구분 |
| **State Machine** | 상태 전이 보장 | 허용된 전이만 실행 |
| **Recovery Mechanisms** | 자동 복구 | Finalizer/Reaper로 자동 처리 |

---

## 🔍 언제 이 패턴들을 사용하는가?

### ✅ 사용해야 하는 경우

1. **외부 API 호출이 있는 경우**
   - 결제 게이트웨이 (PG사)
   - 파일 업로드 (S3, Cloud Storage)
   - 써드파티 서비스 (SMS, Email, Push)
   - 레거시 시스템 연동

2. **중복 실행 시 문제가 되는 경우**
   - 결제 (중복 결제 방지)
   - 파일 생성 (중복 파일 방지)
   - 외부 시스템 상태 변경

3. **실패 시 자동 복구가 필요한 경우**
   - 일시적 네트워크 오류
   - 외부 시스템 일시 장애
   - 타임아웃 발생

4. **추적 가능성이 중요한 경우**
   - 금융 거래
   - 법적 요구사항
   - 감사 추적 (Audit Trail)

### ❌ 사용하지 않아도 되는 경우

1. **단순 CRUD 작업**
   - 단일 DB 트랜잭션으로 충분
   - 외부 시스템 호출 없음

2. **내부 서비스 간 통신**
   - 동일 트랜잭션 경계 내
   - 롤백 가능한 작업

3. **읽기 전용 작업**
   - 조회만 하는 경우
   - 상태 변경 없음

---

## 🏗️ 아키텍처 개요

### 전체 구조

```
┌─────────────────────────────────────────────────────────────────┐
│                        Application Layer                        │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐     │
│  │ REST API     │ →  │ Orchestrator │ →  │ Store (DB)   │     │
│  │ Controller   │    │ (S1: Accept) │    │              │     │
│  └──────────────┘    └──────────────┘    └──────────────┘     │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│                         Queue Layer                             │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐     │
│  │ Message      │ →  │ Runtime      │ →  │ Executor     │     │
│  │ Queue        │    │ (S2: Pump)   │    │ (외부 API)   │     │
│  └──────────────┘    └──────────────┘    └──────────────┘     │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│                       Recovery Layer                            │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐     │
│  │ WAL          │ →  │ Finalizer    │    │ Reaper       │     │
│  │ (Write-Ahead)│    │ (S3: 완료)   │    │ (Timeout)    │     │
│  └──────────────┘    └──────────────┘    └──────────────┘     │
└─────────────────────────────────────────────────────────────────┘
```

### 데이터 흐름

```
1. API 요청 수신
   ↓
2. Command 생성 (IdemKey 포함)
   ↓
3. S1: Accept - 멱등성 검사 → OpId 발급
   ↓
4. Queue 발행 (Envelope)
   ↓
5. S2: Execute - 외부 API 호출 → Outcome 반환
   ↓
6. WAL 기록 (PENDING)
   ↓
7. S3: Finalize - 상태 전이 (COMPLETED/FAILED)
   ↓
8. WAL 완료 (COMPLETED)
```

---

## 📊 핵심 컴포넌트

### 1. Command (실행 명령)

```java
/**
 * 외부 API 호출 명령
 */
public record Command(
    Domain domain,        // 도메인 (예: ORDER, PAYMENT)
    EventType eventType,  // 이벤트 타입 (예: CREATE, UPDATE)
    BizKey bizKey,        // 비즈니스 키 (예: ORDER-123)
    IdemKey idemKey,      // 멱등성 키 (클라이언트 제공)
    Payload payload       // 업무 데이터 (JSON)
) {}
```

### 2. Envelope (실행 컨텍스트)

```java
/**
 * Command + 메타데이터
 */
public record Envelope(
    OpId opId,            // Operation ID
    Command command,      // 실행 명령
    long acceptedAt       // 수락 시각
) {}
```

### 3. Outcome (실행 결과)

```java
/**
 * 실행 결과 - Sealed Interface
 */
public sealed interface Outcome permits Ok, Retry, Fail {

    // Ok: 성공
    record Ok(OpId opId, String message) implements Outcome {}

    // Retry: 재시도 가능한 일시적 실패
    record Retry(String reason, int attemptCount, long nextRetryAfterMillis)
        implements Outcome {}

    // Fail: 영구적 실패 (재시도 불가)
    record Fail(String errorCode, String message, String cause)
        implements Outcome {}
}
```

### 4. Operation State (상태)

```java
/**
 * Operation 생명주기 상태
 */
public enum OperationState {
    PENDING,       // 대기 중
    IN_PROGRESS,   // 실행 중
    COMPLETED,     // 완료
    FAILED         // 실패
}
```

---

## 🎯 핵심 원칙

### 1. 멱등성 (Idempotency)

**원칙**: 동일한 요청을 여러 번 실행해도 결과는 동일해야 함

```java
// ✅ 멱등성 보장
String idemKey = UUID.randomUUID().toString();
Command cmd = Command.of(domain, eventType, bizKey, IdemKey.of(idemKey), payload);

// 첫 번째 요청
OpId opId1 = orchestrator.start(cmd, Duration.ofMinutes(5));

// 동일 IdemKey로 재시도
OpId opId2 = orchestrator.start(cmd, Duration.ofMinutes(5));

assert opId1.equals(opId2);  // ✅ 동일한 OpId 반환
```

### 2. 최종 일관성 (Eventual Consistency)

**원칙**: 일시적으로 불일치 상태가 있을 수 있지만, 결국 일관된 상태로 수렴

```
시간 →
─────────────────────────────────────────────────────────────
T1: Command 수락 (PENDING)
T2: Queue 발행 (IN_PROGRESS)
T3: 외부 API 호출 시작
T4: 네트워크 타임아웃 ⚠️
T5: Retry 큐 재발행
T6: 외부 API 호출 성공 ✅
T7: WAL 기록 (PENDING)
T8: Finalize (COMPLETED) ✅
```

### 3. 상태 전이 불변식 (State Transition Invariants)

**원칙**: 허용된 상태 전이만 실행 가능

```
허용된 전이:
  PENDING → IN_PROGRESS
  IN_PROGRESS → COMPLETED
  IN_PROGRESS → FAILED

금지된 전이:
  COMPLETED → IN_PROGRESS  ❌
  FAILED → IN_PROGRESS     ❌
  COMPLETED ↔ FAILED       ❌
```

### 4. Write-Ahead Log (선행 기록)

**원칙**: 상태 변경 전에 변경 의도를 먼저 기록

```
1. WAL 기록: "opId=123, outcome=COMPLETED" (PENDING)
   ⚠️ 크래시 발생 가능 지점
2. State 변경: operation_state = COMPLETED
3. WAL 완료: "opId=123" (COMPLETED)
```

### 5. 자동 복구 (Automatic Recovery)

**원칙**: 실패 후 자동으로 복구 시도

- **Finalizer**: WAL에서 PENDING 상태인 항목을 주기적으로 스캔하여 완료 처리
- **Reaper**: 시간 예산 초과한 Operation을 자동으로 FAILED 처리

---

## 🔄 실행 플로우 예시

### 성공 케이스

```java
// 1. Command 생성
Command cmd = Command.of(
    Domain.of("PAYMENT"),
    EventType.of("CHARGE"),
    BizKey.of("ORDER-123"),
    IdemKey.of(UUID.randomUUID().toString()),
    Payload.of("{\"amount\":50000}")
);

// 2. S1: Accept
OpId opId = orchestrator.start(cmd, Duration.ofMinutes(5));
// → DB 저장: operation_id, state=PENDING, idem_key

// 3. Queue 발행
// → Envelope(opId, cmd, acceptedAt)

// 4. S2: Execute (QueueWorker)
Outcome outcome = executor.execute(envelope, headers);
// → 외부 API 호출
// → Ok 반환

// 5. WAL 기록
// → write_ahead_log: opId, outcome, state=PENDING

// 6. S3: Finalize
// → operation_state: PENDING → COMPLETED
// → write_ahead_log: state=COMPLETED
```

### 실패 후 복구 케이스

```java
// 1-5. (위와 동일)

// 6. ⚠️ 크래시 발생 (Finalize 전)
//    → WAL: PENDING 상태로 남음

// 7. Finalizer 스캔 (주기적 실행)
List<WalEntry> pending = walRepository.findPendingEntries();
for (WalEntry entry : pending) {
    // ✅ 완료 처리
    store.finalize(entry.opId(), entry.outcome());
}
```

---

## 📋 패턴 선택 가이드

### Decision Tree

```
외부 API 호출이 있는가?
├─ Yes → Orchestration Patterns 적용 ✅
│   ├─ 중복 실행 방지 필요? → Idempotency Pattern
│   ├─ 실패 복구 필요? → WAL + Recovery Mechanisms
│   ├─ 상태 추적 필요? → State Machine Pattern
│   └─ 타입 안전성 필요? → Outcome Modeling
│
└─ No → 일반 트랜잭션 패턴
    ├─ 단일 Aggregate? → @Transactional
    ├─ 여러 Aggregate? → Domain Events
    └─ 분산 트랜잭션? → Saga Pattern
```

### 복잡도별 선택

| 복잡도 | 패턴 조합 | 사용 예시 |
|--------|----------|----------|
| **낮음** | Command + Idempotency | 단순 외부 API 호출 (1회) |
| **중간** | + WAL + Outcome | 재시도 필요한 API 호출 |
| **높음** | + State Machine + Recovery | 복잡한 플로우, 자동 복구 필요 |
| **매우 높음** | 전체 패턴 + Saga | 여러 외부 API 조율 |

---

## 🎓 학습 순서

### 1단계: 기본 개념 이해
1. [Command Pattern](./01_command-pattern.md) - 실행 요청 캡슐화
2. [Idempotency Handling](./02_idempotency-handling.md) - 중복 요청 방지

### 2단계: 안전성 패턴
3. [Write-Ahead Log Pattern](./03_write-ahead-log-pattern.md) - 실패 복구
4. [Outcome Modeling](./04_outcome-modeling.md) - 결과 타입 안전성

### 3단계: 상태 관리
5. [State Machine Pattern](./05_state-machine-pattern.md) - 상태 전이 보장

### 4단계: 자동 복구
6. [Recovery Mechanisms](./06_recovery-mechanisms.md) - Finalizer/Reaper

### 5단계: 실전 적용
7. [Implementation Guide](./07_implementation-guide.md) - 단계별 구현

---

## 📚 추가 참고 자료

### 관련 패턴
- [Saga Pattern](../07-enterprise-patterns/event-driven/03_saga-pattern.md) - 분산 트랜잭션 조율
- [Domain Events](../07-enterprise-patterns/event-driven/01_domain-events.md) - 이벤트 기반 아키텍처
- [Circuit Breaker](../07-enterprise-patterns/resilience/01_circuit-breaker.md) - 장애 격리

### 외부 참고
- [AWS Step Functions](https://aws.amazon.com/step-functions/) - Orchestration as a Service
- [Temporal.io](https://temporal.io/) - Workflow Orchestration Platform
- [Camunda](https://camunda.com/) - Business Process Orchestration

---

**작성자**: Development Team
**최종 수정일**: 2025-10-30
**버전**: 1.0.0
