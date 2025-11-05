# Write-Ahead Log Pattern - 선행 기록 로그

**목적**: 상태 변경 전에 변경 의도를 먼저 기록하여 크래시 후 복구 가능성 보장

**관련 문서**:
- [Orchestration Pattern Overview](./00_orchestration-pattern-overview.md)
- [Recovery Mechanisms](./06_recovery-mechanisms.md)
- [State Machine Pattern](./05_state-machine-pattern.md)

**필수 버전**: Java 21+, Spring Framework 6.0+

---

## 📌 핵심 원칙

### Write-Ahead Log (WAL)란?

**정의**: 실제 상태 변경 전에 변경 의도를 먼저 로그에 기록하는 패턴

```
시간 순서:
1. WAL 기록: "opId=123, outcome=COMPLETED" (PENDING)
   ⚠️ 크래시 가능 지점
2. State 변경: operation_state = COMPLETED
3. WAL 완료: "opId=123" (COMPLETED)
```

**핵심 보장**:
- 크래시 발생 시 WAL을 스캔하여 미완료 작업 복구
- 최종 일관성 보장

### 왜 필요한가?

```java
// ❌ 문제 상황 - WAL 없는 경우
@Service
public class PaymentService {

    @Transactional
    public void completePayment(OpId opId, Outcome outcome) {
        // 1. 외부 API 호출 성공
        PaymentApiResponse response = paymentGateway.getResult(opId);

        // 2. DB 상태 업데이트
        operationRepository.updateState(opId, OperationState.COMPLETED);

        // ⚠️ 크래시 발생 지점
        //    → DB 업데이트 전에 크래시
        //    → 외부 API는 성공했지만 DB는 IN_PROGRESS 상태로 남음
        //    → 복구 불가능 ❌
    }
}

// ✅ WAL 패턴 적용
@Service
public class PaymentServiceWithWal {

    @Transactional
    public void completePayment(OpId opId, Outcome outcome) {
        // 1. WAL 기록 (먼저 기록)
        walRepository.writeAhead(opId, outcome, WriteAheadState.PENDING);

        // ⚠️ 여기서 크래시 발생해도 WAL 있음 → 복구 가능 ✅

        // 2. DB 상태 업데이트
        operationRepository.updateState(opId, OperationState.COMPLETED);

        // 3. WAL 완료 표시
        walRepository.markCompleted(opId);
    }
}
```

---

## 🏗️ WAL 데이터 모델

### 1. WriteAheadState Enum

```java
/**
 * Write-Ahead Log 상태
 *
 * @author development-team
 * @since 1.0.0
 */
public enum WriteAheadState {

    /**
     * WAL 기록됨, finalize() 대기 중
     *
     * <p><strong>Finalizer 스캔 대상</strong></p>
     */
    PENDING,

    /**
     * finalize() 완료됨 (최종 상태)
     *
     * <p><strong>정리 대상</strong></p>
     */
    COMPLETED;

    public boolean requiresFinalization() {
        return this == PENDING;
    }

    public boolean isFinalized() {
        return this == COMPLETED;
    }
}
```

### 2. 데이터베이스 스키마

```sql
/**
 * Write-Ahead Log 테이블
 */
CREATE TABLE write_ahead_log (
    id BIGSERIAL PRIMARY KEY,
    op_id VARCHAR(255) NOT NULL,
    outcome_type VARCHAR(20) NOT NULL,  -- OK, RETRY, FAIL
    outcome_data TEXT,                   -- JSON 형식의 outcome 상세 정보
    state VARCHAR(20) NOT NULL,          -- PENDING, COMPLETED
    written_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,

    -- ✅ OpId 기준 조회 최적화
    CONSTRAINT uk_wal_op_id UNIQUE (op_id)
);

/**
 * 인덱스 생성
 */
CREATE INDEX idx_wal_state ON write_ahead_log(state);
CREATE INDEX idx_wal_written_at ON write_ahead_log(written_at);

/**
 * PENDING 상태 조회 최적화 (Finalizer용)
 */
CREATE INDEX idx_wal_pending ON write_ahead_log(state, written_at)
    WHERE state = 'PENDING';
```

### 3. JPA Entity

```java
/**
 * Write-Ahead Log Entity
 *
 * @author development-team
 * @since 1.0.0
 */
@Entity
@Table(name = "write_ahead_log")
public class WriteAheadLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String opId;

    @Column(nullable = false)
    private String outcomeType;  // OK, RETRY, FAIL

    @Column(columnDefinition = "TEXT")
    private String outcomeData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WriteAheadState state;

    @Column(nullable = false)
    private Instant writtenAt;

    @Column
    private Instant completedAt;

    // Constructors, getters, setters...

    /**
     * WAL 기록 생성 (PENDING 상태)
     */
    public static WriteAheadLogEntity create(OpId opId, Outcome outcome) {
        WriteAheadLogEntity entity = new WriteAheadLogEntity();
        entity.opId = opId.getValue();
        entity.outcomeType = getOutcomeType(outcome);
        entity.outcomeData = serializeOutcome(outcome);
        entity.state = WriteAheadState.PENDING;
        entity.writtenAt = Instant.now();
        return entity;
    }

    /**
     * WAL 완료 표시
     */
    public void markCompleted() {
        this.state = WriteAheadState.COMPLETED;
        this.completedAt = Instant.now();
    }

    private static String getOutcomeType(Outcome outcome) {
        return switch (outcome) {
            case Ok ok -> "OK";
            case Retry retry -> "RETRY";
            case Fail fail -> "FAIL";
        };
    }

    private static String serializeOutcome(Outcome outcome) {
        // JSON 직렬화 로직
        return switch (outcome) {
            case Ok ok -> """
                {"message": "%s"}
                """.formatted(ok.message());
            case Retry retry -> """
                {"reason": "%s", "attemptCount": %d, "nextRetryAfterMillis": %d}
                """.formatted(retry.reason(), retry.attemptCount(), retry.nextRetryAfterMillis());
            case Fail fail -> """
                {"errorCode": "%s", "message": "%s", "cause": "%s"}
                """.formatted(fail.errorCode(), fail.message(), fail.cause());
        };
    }
}
```

---

## ✅ WAL 구현 패턴

### 패턴 1: QueueWorker에서 WAL 기록

```java
/**
 * QueueWorker - WAL 기록 포함
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class QueueWorker {

    private final Executor executor;
    private final Store store;
    private final WalRepository walRepository;

    /**
     * ✅ Queue 메시지 처리 (WAL 포함)
     */
    @Transactional
    public void process(Envelope envelope) {
        try {
            // 1. Executor 실행 (외부 API 호출)
            Outcome outcome = executor.execute(envelope, Map.of());

            // 2. WAL 기록 (먼저 기록)
            walRepository.writeAhead(envelope.opId(), outcome);

            // ⚠️ 여기서 크래시 발생해도 WAL 있음 → Finalizer가 복구

            // 3. Finalize (상태 전이)
            store.finalize(envelope.opId(), outcome, Instant.now());

            // 4. WAL 완료 표시
            walRepository.markCompleted(envelope.opId());

        } catch (Exception e) {
            // ❌ Executor 실패 시 WAL 기록 안 함
            log.error("Executor failed for opId={}", envelope.opId(), e);
            throw e;
        }
    }
}
```

### 패턴 2: WAL Repository 구현

```java
/**
 * Write-Ahead Log Repository
 *
 * @author development-team
 * @since 1.0.0
 */
@Repository
public class JpaWalRepository implements WalRepository {

    private final WriteAheadLogJpaRepository jpaRepository;

    @Override
    @Transactional
    public void writeAhead(OpId opId, Outcome outcome) {
        // 1. 기존 WAL 확인
        Optional<WriteAheadLogEntity> existing = jpaRepository.findByOpId(opId.getValue());
        if (existing.isPresent()) {
            // ✅ 멱등성 보장: 이미 기록되어 있으면 스킵
            return;
        }

        // 2. WAL 기록 (PENDING 상태)
        WriteAheadLogEntity entity = WriteAheadLogEntity.create(opId, outcome);
        jpaRepository.save(entity);
    }

    @Override
    @Transactional
    public void markCompleted(OpId opId) {
        WriteAheadLogEntity entity = jpaRepository.findByOpId(opId.getValue())
            .orElseThrow(() -> new IllegalStateException("WAL not found: " + opId));

        entity.markCompleted();
        jpaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalEntry> findPendingEntries(Duration olderThan) {
        Instant cutoff = Instant.now().minus(olderThan);

        return jpaRepository.findByStateAndWrittenAtBefore(
                WriteAheadState.PENDING,
                cutoff
            )
            .stream()
            .map(this::toWalEntry)
            .toList();
    }

    private WalEntry toWalEntry(WriteAheadLogEntity entity) {
        return new WalEntry(
            OpId.of(entity.getOpId()),
            deserializeOutcome(entity.getOutcomeType(), entity.getOutcomeData()),
            entity.getWrittenAt()
        );
    }
}
```

---

## 🔄 복구 메커니즘

### Finalizer 스캔

```java
/**
 * Finalizer - PENDING 상태 WAL 스캔 및 완료 처리
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class Finalizer {

    private final WalRepository walRepository;
    private final Store store;

    /**
     * ✅ 주기적으로 PENDING 상태 WAL 스캔
     *
     * 5초 이상 PENDING 상태인 항목 처리
     */
    @Scheduled(fixedRate = 5000)  // 5초마다 실행
    @Transactional
    public void scanAndFinalize() {
        // 1. PENDING 상태 WAL 조회 (5초 이상 된 것만)
        List<WalEntry> pendingEntries = walRepository.findPendingEntries(Duration.ofSeconds(5));

        if (pendingEntries.isEmpty()) {
            return;
        }

        log.info("Found {} pending WAL entries", pendingEntries.size());

        // 2. 각 항목 완료 처리
        for (WalEntry entry : pendingEntries) {
            try {
                finalizePendingEntry(entry);
            } catch (Exception e) {
                log.error("Failed to finalize WAL entry: opId={}", entry.opId(), e);
            }
        }
    }

    private void finalizePendingEntry(WalEntry entry) {
        // 1. Store에서 현재 상태 확인
        OperationState currentState = store.getState(entry.opId());

        // 2. 이미 종료 상태면 WAL만 완료 표시
        if (currentState.isTerminal()) {
            walRepository.markCompleted(entry.opId());
            log.info("WAL completed for already finalized operation: opId={}", entry.opId());
            return;
        }

        // 3. 아직 미완료면 finalize() 실행
        store.finalize(entry.opId(), entry.outcome(), Instant.now());
        walRepository.markCompleted(entry.opId());

        log.info("Finalized pending operation: opId={}, outcome={}",
            entry.opId(), entry.outcome());
    }
}

/**
 * WAL Entry DTO
 */
record WalEntry(
    OpId opId,
    Outcome outcome,
    Instant writtenAt
) {}
```

---

## 🎯 실전 예제

### 예제 1: 결제 처리 with WAL

```java
/**
 * 결제 처리 - WAL 패턴 적용
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class PaymentQueueWorker {

    private final PaymentExecutor executor;
    private final Store store;
    private final WalRepository walRepository;

    @KafkaListener(topics = "payment-queue")
    @Transactional
    public void processPayment(Envelope envelope) {
        try {
            // 1. 결제 API 호출
            Outcome outcome = executor.execute(envelope, Map.of());

            // 2. WAL 기록 (PENDING)
            walRepository.writeAhead(envelope.opId(), outcome);
            log.info("WAL written: opId={}, outcome={}", envelope.opId(), outcome);

            // ⚠️ 크래시 가능 지점 (WAL 있으면 복구 가능)

            // 3. Finalize (상태 전이)
            boolean finalized = store.finalize(envelope.opId(), outcome, Instant.now());
            if (!finalized) {
                log.warn("Finalize failed: opId={}", envelope.opId());
                return;
            }

            // 4. WAL 완료 표시
            walRepository.markCompleted(envelope.opId());
            log.info("Payment completed: opId={}", envelope.opId());

        } catch (Exception e) {
            log.error("Payment processing failed: opId={}", envelope.opId(), e);
            throw e;
        }
    }
}

/**
 * 복구 시나리오:
 *
 * T1: WAL 기록 (PENDING)
 * T2: ⚠️ 크래시 발생 (finalize 전)
 * T3: Finalizer 스캔 (5초 후)
 *     → PENDING WAL 발견
 *     → finalize() 실행
 *     → WAL 완료 (COMPLETED)
 * T4: ✅ 복구 완료
 */
```

### 예제 2: 파일 업로드 with WAL

```java
/**
 * 파일 업로드 처리 - WAL 패턴
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class FileUploadQueueWorker {

    private final FileUploadExecutor executor;
    private final Store store;
    private final WalRepository walRepository;

    @SqsListener(queueName = "file-upload-queue")
    @Transactional
    public void processFileUpload(Envelope envelope) {
        try {
            // 1. S3 업로드
            Outcome outcome = executor.execute(envelope, Map.of());

            // 2. WAL 기록
            walRepository.writeAhead(envelope.opId(), outcome);

            // 3. Finalize
            store.finalize(envelope.opId(), outcome, Instant.now());

            // 4. WAL 완료
            walRepository.markCompleted(envelope.opId());

            log.info("File upload completed: opId={}", envelope.opId());

        } catch (Exception e) {
            log.error("File upload failed: opId={}", envelope.opId(), e);
            throw e;
        }
    }
}
```

---

## 🧪 테스트 전략

### 1. WAL 기록 테스트

```java
@SpringBootTest
class WalTest {

    @Autowired
    private WalRepository walRepository;

    @Autowired
    private Store store;

    @Test
    void WAL_기록_후_복구() {
        // given
        OpId opId = OpId.of(UUID.randomUUID().toString());
        Outcome outcome = Ok.of(opId, "Success");

        // when: WAL 기록
        walRepository.writeAhead(opId, outcome);

        // then: PENDING 상태로 조회 가능
        List<WalEntry> pending = walRepository.findPendingEntries(Duration.ZERO);
        assertThat(pending).hasSize(1);
        assertThat(pending.get(0).opId()).isEqualTo(opId);
        assertThat(pending.get(0).outcome()).isInstanceOf(Ok.class);

        // when: Finalize 실행
        store.finalize(opId, outcome, Instant.now());
        walRepository.markCompleted(opId);

        // then: PENDING 목록에서 제거됨
        List<WalEntry> afterFinalize = walRepository.findPendingEntries(Duration.ZERO);
        assertThat(afterFinalize).isEmpty();
    }
}
```

### 2. Finalizer 복구 테스트

```java
@SpringBootTest
class FinalizerTest {

    @Autowired
    private Finalizer finalizer;

    @Autowired
    private WalRepository walRepository;

    @Autowired
    private Store store;

    @Test
    void Finalizer가_PENDING_WAL_처리() throws InterruptedException {
        // given: WAL 기록만 하고 finalize 안 함
        OpId opId = OpId.of(UUID.randomUUID().toString());
        Outcome outcome = Ok.of(opId, "Success");

        walRepository.writeAhead(opId, outcome);

        // 5초 대기 (Finalizer 스캔 대상이 되도록)
        Thread.sleep(6000);

        // when: Finalizer 실행
        finalizer.scanAndFinalize();

        // then: Operation이 COMPLETED 상태로 전이됨
        OperationState state = store.getState(opId);
        assertThat(state).isEqualTo(OperationState.COMPLETED);

        // then: WAL이 COMPLETED 상태로 변경됨
        List<WalEntry> pending = walRepository.findPendingEntries(Duration.ZERO);
        assertThat(pending).isEmpty();
    }
}
```

---

## 📋 Best Practices

### 1. WAL은 트랜잭션 내에서 기록

```java
// ✅ Good - 트랜잭션 내에서 WAL 기록
@Transactional
public void process(Envelope envelope) {
    Outcome outcome = executor.execute(envelope, Map.of());
    walRepository.writeAhead(envelope.opId(), outcome);  // ✅ 트랜잭션 보장
    store.finalize(envelope.opId(), outcome, Instant.now());
    walRepository.markCompleted(envelope.opId());
}

// ❌ Bad - 트랜잭션 없이 WAL 기록
public void process(Envelope envelope) {
    Outcome outcome = executor.execute(envelope, Map.of());
    walRepository.writeAhead(envelope.opId(), outcome);  // ❌ 트랜잭션 보장 안 됨
}
```

### 2. WAL 멱등성 보장

```java
// ✅ Good - 멱등성 보장
@Override
@Transactional
public void writeAhead(OpId opId, Outcome outcome) {
    Optional<WriteAheadLogEntity> existing = jpaRepository.findByOpId(opId.getValue());
    if (existing.isPresent()) {
        return;  // ✅ 이미 기록되어 있으면 스킵
    }

    WriteAheadLogEntity entity = WriteAheadLogEntity.create(opId, outcome);
    jpaRepository.save(entity);
}
```

### 3. Finalizer 스캔 주기 설정

```java
// ✅ Good - 적절한 스캔 주기
@Scheduled(fixedRate = 5000)  // 5초마다
public void scanAndFinalize() {
    List<WalEntry> pending = walRepository.findPendingEntries(Duration.ofSeconds(5));
    // ...
}

// ❌ Bad - 너무 짧은 주기
@Scheduled(fixedRate = 100)  // 100ms마다 → DB 부하
```

### 4. WAL 정리

```sql
-- ✅ COMPLETED 상태 WAL 정리 (30일 이상 된 것)
DELETE FROM write_ahead_log
WHERE state = 'COMPLETED'
  AND completed_at < NOW() - INTERVAL '30 days';
```

---

## 📚 Common Pitfalls

### ❌ Pitfall 1: WAL 기록 없이 finalize

```java
// ❌ Bad - WAL 없이 바로 finalize
public void process(Envelope envelope) {
    Outcome outcome = executor.execute(envelope, Map.of());
    store.finalize(envelope.opId(), outcome, Instant.now());  // ❌ 크래시 시 복구 불가
}

// ✅ Good - WAL 먼저 기록
public void process(Envelope envelope) {
    Outcome outcome = executor.execute(envelope, Map.of());
    walRepository.writeAhead(envelope.opId(), outcome);  // ✅ 먼저 기록
    store.finalize(envelope.opId(), outcome, Instant.now());
    walRepository.markCompleted(envelope.opId());
}
```

### ❌ Pitfall 2: WAL 완료 표시 누락

```java
// ❌ Bad - WAL 완료 표시 안 함
public void process(Envelope envelope) {
    Outcome outcome = executor.execute(envelope, Map.of());
    walRepository.writeAhead(envelope.opId(), outcome);
    store.finalize(envelope.opId(), outcome, Instant.now());
    // ❌ markCompleted() 누락 → PENDING 상태로 남음
}

// ✅ Good - WAL 완료 표시
public void process(Envelope envelope) {
    Outcome outcome = executor.execute(envelope, Map.of());
    walRepository.writeAhead(envelope.opId(), outcome);
    store.finalize(envelope.opId(), outcome, Instant.now());
    walRepository.markCompleted(envelope.opId());  // ✅ 완료 표시
}
```

---

**작성자**: Development Team
**최종 수정일**: 2025-10-30
**버전**: 1.0.0
