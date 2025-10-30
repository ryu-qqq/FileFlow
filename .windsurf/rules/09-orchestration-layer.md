# Orchestration Layer 규칙 (Windsurf 자동 로드)

**중요**: 이 파일은 Windsurf IDE(Cascade)가 자동으로 읽습니다. 6000자 이내로 작성되었습니다.

---

## 🎯 핵심 원칙

### 1. 3-Phase Lifecycle
- **Accept**: 요청 수락, IdemKey 중복 체크 (짧은 트랜잭션)
- **Execute**: 외부 API 호출 (비동기, 트랜잭션 밖)
- **Finalize**: 상태 변경 + WAL 기록 (짧은 트랜잭션)

### 2. Idempotency (멱등성)
- IdemKey로 중복 실행 방지
- DB Unique 제약으로 Race Condition 안전 처리
- 동일 IdemKey 재요청 시 기존 Operation 반환

### 3. Write-Ahead Log (WAL)
- Finalize 전 WAL에 PENDING 기록
- 상태 변경 완료 후 WAL을 COMPLETED로 변경
- Finalizer가 PENDING WAL 자동 복구 (5초마다)

### 4. Outcome Modeling
- Sealed interface: Ok, Retry, Fail
- Pattern matching으로 타입 안전 분기
- 컴파일 타임에 모든 케이스 강제 처리

### 5. Transaction 경계
- executeInternal()은 **반드시 @Async**
- 외부 API 호출은 트랜잭션 밖에서
- Accept/Finalize만 짧은 트랜잭션 유지

---

## ❌ 금지 규칙 (Zero-Tolerance)

### 1. executeInternal 내 @Transactional ❌
```java
// ❌ 금지: 트랜잭션 안에서 외부 API 호출
@Override
@Transactional
protected Outcome executeInternal(OpId opId, Command cmd) {
    paymentGateway.charge(...); // 위험!
}

// ✅ 올바름: 트랜잭션 밖에서 외부 API 호출
@Override
@Async
protected Outcome executeInternal(OpId opId, Command cmd) {
    paymentGateway.charge(...); // 안전
}
```

### 2. IdemKey 없이 Operation 생성 ❌
```java
// ❌ 금지
Operation.create(opId, bizKey, domain, eventType);

// ✅ 올바름
Operation.create(opId, idemKey, bizKey, domain, eventType);
```

### 3. Outcome 대신 boolean/Exception ❌
```java
// ❌ 금지
protected boolean executeInternal(...) throws Exception

// ✅ 올바름
protected Outcome executeInternal(...) {
    return Outcome.ok(opId, "Success");
}
```

### 4. Finalizer/Reaper 없이 Orchestrator ❌
```java
// ❌ 금지: Recovery 메커니즘 없음

// ✅ 올바름: Finalizer + Reaper 함께 구현
@Component
public class PaymentFinalizer {
    @Scheduled(fixedDelay = 5000)
    public void processPendingWal() { ... }
}
```

### 5. Lombok 사용 ❌
```java
// ❌ 금지
@Data
public class PaymentCommand { ... }

// ✅ 올바름: Record 패턴
public record PaymentCommand(String orderId, String idemKey) {
    public PaymentCommand {
        Objects.requireNonNull(orderId);
    }
}
```

---

## ✅ 필수 규칙

### 1. BaseOrchestrator 상속
```java
@Service
public class PaymentOrchestrator extends BaseOrchestrator<PaymentCommand> {
    @Override
    protected Domain domain() { return Domain.PAYMENT; }

    @Override
    protected EventType eventType() { return EventType.PAYMENT_REQUESTED; }

    @Override
    @Async
    protected Outcome executeInternal(OpId opId, PaymentCommand cmd) { ... }
}
```

### 2. IdemKey Unique 제약
```sql
CREATE TABLE operations (
    op_id VARCHAR(255) PRIMARY KEY,
    idem_key VARCHAR(255) NOT NULL,
    CONSTRAINT uk_operations_idem_key UNIQUE (idem_key)
);
```

### 3. WAL 기록 순서
```java
// 1. WAL 기록 (PENDING)
walRepository.writeAhead(opId, outcome, WriteAheadState.PENDING);

// 2. DB 상태 업데이트
operationRepository.updateState(opId, OperationState.COMPLETED);

// 3. WAL 완료 표시
walRepository.markCompleted(opId);
```

### 4. Retry 전략 (Exponential Backoff)
```java
private long calculateBackoff(int attemptCount) {
    return 5000L * (long) Math.pow(2, attemptCount - 1);
}
```

### 5. Error 분류
```java
try {
    // 외부 API 호출
} catch (TransientException e) {
    return Outcome.retry(e.getMessage(), attemptCount, calculateBackoff(attemptCount));
} catch (PermanentException e) {
    return Outcome.fail(e.getErrorCode(), e.getMessage(), "N/A");
}
```

---

## 📋 체크리스트

### Command 생성 시
- [ ] Record 패턴 사용 (Lombok 금지)
- [ ] Compact Constructor 검증 (Objects.requireNonNull)
- [ ] Javadoc 작성 (@author, @since)
- [ ] IdemKey 필드 포함

### Orchestrator 생성 시
- [ ] BaseOrchestrator 상속
- [ ] domain(), eventType() 오버라이드
- [ ] executeInternal() @Async 선언
- [ ] Retry 전략 구현
- [ ] Error 분류 (Retryable/Fatal)

### Entity 생성 시
- [ ] Long FK 전략 (JPA 관계 금지)
- [ ] IdemKey Unique 제약
- [ ] Javadoc 작성

### Scheduler 생성 시
- [ ] @Scheduled(fixedDelay = 5000)
- [ ] Finalizer: PENDING WAL 처리
- [ ] Reaper: TIMEOUT 처리

---

## 🔍 검증 항목

1. executeInternal이 @Async인가?
2. Record 패턴 사용했는가?
3. @author, @since 있는가?
4. Entity에 관계 어노테이션 없는가?
5. IdemKey Unique 제약 있는가?

---

**참고**:
- Claude Code: `/code-gen-orchestrator` 명령어
- 문서: `docs/coding_convention/09-orchestration-patterns/`
