# Transactional Outbox Pattern: 외부 API 호출의 안전한 조율

**목적**: 비즈니스 로직과 외부 API 호출을 동일한 트랜잭션으로 처리하여 강력한 원자성 보장

**관련 문서**:
- [Orchestration Pattern Overview](./00_orchestration-pattern-overview.md)
- [Idempotency Handling](./02_idempotency-handling.md)
- [Domain Events](../07-enterprise-patterns/event-driven/01_domain-events.md)

**필수 버전**: Spring Boot 3.0+, Java 21+

---

## 📌 핵심 문제: 외부 API 호출의 불확실성

### ❌ 기존 방식의 문제점

```java
// ❌ Before - 외부 API가 트랜잭션 내부 (위험!)
@Service
public class PaymentService {

    /**
     * ❌ 문제점:
     * 1. Payment 저장 성공 → PG 호출 실패 → 불일치 상태
     * 2. PG 호출 타임아웃 → 재시도 시 중복 결제
     * 3. 크래시 발생 시 복구 불가능
     */
    @Transactional
    public void processPayment(PaymentRequest request) {
        // 1. DB에 결제 기록 저장
        Payment payment = paymentRepository.save(Payment.create(request));

        // 2. 외부 PG API 호출 (트랜잭션 내부!)
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
- 🔴 **중복 요청**: 타임아웃 후 재시도 시 동일 결제가 2번 실행
- 🔴 **부분 실패**: DB는 저장되었지만 API 호출 실패 (불일치)
- 🔴 **복구 불가**: 실패 후 재시도 지점 불명확
- 🔴 **크래시 유실**: 앱 재시작 시 진행 중이던 요청 유실

---

## 🎯 Transactional Outbox Pattern의 해결책

### 핵심 아이디어

**"외부 API 호출을 DB에 먼저 기록하고, 별도 프로세스가 안전하게 처리"**

```
┌──────────────────────────────────────────────────┐
│ 1. 동일 트랜잭션                                 │
│    Payment 생성 + Outbox 기록                    │
│    → 둘 다 성공 or 둘 다 실패 (원자성)          │
└──────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────┐
│ 2. 트랜잭션 커밋 후 이벤트 발행                  │
│    → @TransactionalEventListener(AFTER_COMMIT)   │
└──────────────────────────────────────────────────┘
           ↓
┌──────────────────────────────────────────────────┐
│ 3. 별도 스레드/프로세스에서 외부 API 호출        │
│    → 실패 시 재시도                              │
│    → 최종 실패 시 DLQ (Dead Letter Queue)       │
└──────────────────────────────────────────────────┘
```

---

## 🏗️ 3가지 구현 패턴 비교

### 패턴 A: Direct Event (In-Proc) - ⚠️ 지양

**흐름**: Payment + Outbox 저장 → 커밋 → `@Async` 리스너가 **바로** PG 호출

```java
// ⚠️ Pattern A - 단순하지만 결제엔 부적합
@Component
public class PaymentEventHandler {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentCreated(PaymentCreated event) {
        // ⚠️ 이벤트 리스너가 직접 외부 API 호출
        paymentGateway.charge(event.paymentId(), event.amount());
    }
}
```

| 항목 | 평가 |
|------|------|
| **레이턴시** | ⚡ 최소 (즉시 호출) |
| **구현 복잡도** | ✅ 단순 |
| **크래시 안전성** | ❌ 이벤트 유실 위험 |
| **재시도/DLQ** | ❌ 없음 (직접 구현 필요) |
| **백프레셔** | ❌ 없음 |
| **적합 케이스** | 🟡 **저위험 호출** (알림, 로그) |
| **결제 도메인** | ❌ **부적합** |

**문제점**:
- ❌ 앱 크래시/재시작 시 **메모리 이벤트 유실**
- ❌ 재시도 로직 직접 구현 필요
- ❌ DLQ (Dead Letter Queue) 부재
- ❌ 동시 처리량 제어 어려움

**언제 사용?**:
- ✅ Push 알림, 로그 전송 등 **유실 허용 가능한 호출**
- ✅ 프로토타입, PoC 단계
- ❌ 결제, 주문, 재고 등 **critical 호출에는 부적합**

---

### 패턴 B: Outbox + Event Wake-up (Hybrid) - ✅ **권장 기본 패턴**

**흐름**: Payment + Outbox 저장 → 커밋 → 이벤트가 **Relay를 깨움** → Relay가 Outbox 조회 → 외부 API 호출

```java
// ✅ Pattern B - 프로덕션 표준 패턴
@Service
public class PaymentService {

    /**
     * ✅ 1. 동일 트랜잭션으로 Payment + Outbox 저장
     */
    @Transactional
    public PaymentId createPayment(CreatePaymentCommand command) {
        // Payment 저장
        Payment payment = paymentRepository.save(
            Payment.create(command)
        );

        // Outbox 저장 (동일 트랜잭션!)
        PaymentOutbox outbox = PaymentOutbox.builder()
            .aggregateType("PAYMENT")
            .aggregateId(payment.getId().toString())
            .eventType("PAYMENT_CREATED")
            .payload(toJson(payment))
            .status(OutboxStatus.PENDING)
            .idemKey(command.idemKey())  // 멱등성 키
            .build();

        outboxRepository.save(outbox);

        // ✅ 둘 다 성공 or 둘 다 실패 (원자성 보장)
        return payment.getId();
    }
}

/**
 * ✅ 2. 트랜잭션 커밋 후 이벤트 발행 (Wake-up 신호)
 */
@Component
public class OutboxWakeupPublisher {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOutboxCreated(OutboxCreatedEvent event) {
        // ✅ Relay에게 "픽업 해!" 신호만 전송
        applicationEventPublisher.publishEvent(
            new OutboxWakeupSignal(event.outboxId())
        );
    }
}

/**
 * ✅ 3. Relay가 Outbox 조회 후 외부 API 호출
 */
@Component
public class OutboxRelay {

    @EventListener
    @Async
    public void onWakeupSignal(OutboxWakeupSignal signal) {
        // ✅ Outbox 조회 (FOR UPDATE SKIP LOCKED)
        List<PaymentOutbox> pending = outboxRepository
            .findPendingWithLock(OutboxStatus.PENDING, 10);

        pending.forEach(this::processOutbox);
    }

    /**
     * ✅ 폴백: 주기적 폴링 (이벤트 유실 대비)
     */
    @Scheduled(fixedDelay = 5000)  // 5초마다 (느린 주기)
    public void pollOutbox() {
        List<PaymentOutbox> stuck = outboxRepository
            .findPendingWithLock(OutboxStatus.PENDING, 10);

        stuck.forEach(this::processOutbox);
    }

    private void processOutbox(PaymentOutbox outbox) {
        try {
            // ✅ 외부 API 호출 (트랜잭션 밖!)
            paymentGateway.charge(outbox);

            // 성공 처리
            outbox.markCompleted();
            outboxRepository.save(outbox);

        } catch (RetryableException e) {
            // 재시도 가능한 오류
            outbox.scheduleRetry();
            outboxRepository.save(outbox);

        } catch (Exception e) {
            // 영구적 실패
            outbox.markFailed(e.getMessage());
            outboxRepository.save(outbox);
        }
    }
}
```

| 항목 | 평가 |
|------|------|
| **레이턴시** | ⚡ 빠름 (이벤트로 즉시 깨움) |
| **구현 복잡도** | 🟡 중간 |
| **크래시 안전성** | ✅ **Outbox가 DB에 저장됨** |
| **재시도/DLQ** | ✅ **Outbox Status로 관리** |
| **백프레셔** | 🟡 제한적 (SKIP LOCKED로 일부 가능) |
| **적합 케이스** | ✅ **대부분의 프로덕션 환경** |
| **결제 도메인** | ✅ **권장** |

**핵심 장점**:
- ✅ **즉시성**: 이벤트가 Relay를 깨워서 즉시 처리 (패턴 A 수준)
- ✅ **안전성**: Outbox가 DB에 저장되어 크래시 복구 가능
- ✅ **폴백 보장**: 주기적 폴링으로 이벤트 유실 대비
- ✅ **멱등성**: `idemKey`로 중복 요청 방지
- ✅ **재시도**: Outbox Status로 재시도 관리
- ✅ **추가 인프라 불필요**: MQ 없이 MySQL만으로 구현

**언제 사용?**:
- ✅ **결제, 주문, 재고 등 critical 도메인**
- ✅ MQ 도입 전 초기 단계
- ✅ 중간 규모 처리량 (<10,000 msg/min)

---

### 패턴 C: MQ 통합 (Event → MQ → Worker) - 🚀 **MQ 도입 시 권장**

**흐름**: Payment + Outbox 저장 → 커밋 → `@Async` 리스너가 **SQS/Kafka 발행** → 워커가 MQ 소비 → 외부 API 호출

```java
// 🚀 Pattern C - MQ 고도화 패턴
@Service
public class PaymentService {

    /**
     * ✅ 1. 동일 트랜잭션으로 Payment + Outbox 저장 (패턴 B와 동일)
     */
    @Transactional
    public PaymentId createPayment(CreatePaymentCommand command) {
        Payment payment = paymentRepository.save(Payment.create(command));

        PaymentOutbox outbox = PaymentOutbox.builder()
            .aggregateType("PAYMENT")
            .aggregateId(payment.getId().toString())
            .eventType("PAYMENT_CREATED")
            .payload(toJson(payment))
            .status(OutboxStatus.PENDING)
            .idemKey(command.idemKey())
            .build();

        outboxRepository.save(outbox);

        return payment.getId();
    }
}

/**
 * 🚀 2. 트랜잭션 커밋 후 MQ 발행
 */
@Component
public class OutboxMqPublisher {

    private final SqsTemplate sqsTemplate;  // or KafkaTemplate

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOutboxCreated(OutboxCreatedEvent event) {
        // ✅ SQS/Kafka에 메시지 발행
        PaymentOutbox outbox = outboxRepository.findById(event.outboxId())
            .orElseThrow();

        sqsTemplate.send("payment-queue", OutboxMessage.of(outbox));

        // ✅ Outbox 상태 업데이트 (PENDING → PUBLISHED)
        outbox.markPublished();
        outboxRepository.save(outbox);
    }
}

/**
 * 🚀 3. 별도 워커가 MQ 소비 후 외부 API 호출
 */
@Component
public class PaymentMqWorker {

    @SqsListener(value = "payment-queue", deletionPolicy = ON_SUCCESS)
    public void processPayment(OutboxMessage message) {
        try {
            // ✅ 외부 API 호출
            paymentGateway.charge(message.paymentId(), message.amount());

            // ✅ Outbox 완료 처리
            PaymentOutbox outbox = outboxRepository.findById(message.outboxId())
                .orElseThrow();

            outbox.markCompleted();
            outboxRepository.save(outbox);

        } catch (RetryableException e) {
            // ⚠️ SQS가 자동 재시도 (Visibility Timeout)
            throw e;

        } catch (Exception e) {
            // ❌ DLQ로 이동 (SQS Dead Letter Queue)
            PaymentOutbox outbox = outboxRepository.findById(message.outboxId())
                .orElseThrow();

            outbox.markFailed(e.getMessage());
            outboxRepository.save(outbox);
        }
    }
}

/**
 * 🚀 폴백: 스케줄러가 PENDING 상태를 MQ 재발행
 */
@Component
public class OutboxRecoveryScheduler {

    @Scheduled(fixedDelay = 60000)  // 1분마다
    public void recoverPendingOutbox() {
        // ✅ 5분 이상 PENDING 상태인 항목 재발행
        List<PaymentOutbox> stuck = outboxRepository
            .findStuckPending(Duration.ofMinutes(5));

        stuck.forEach(outbox -> {
            sqsTemplate.send("payment-queue", OutboxMessage.of(outbox));
            outbox.markPublished();
            outboxRepository.save(outbox);
        });
    }
}
```

| 항목 | 평가 |
|------|------|
| **레이턴시** | ⚡ 매우 빠름 (MQ 버퍼링) |
| **구현 복잡도** | 🔴 높음 (MQ 인프라 필요) |
| **크래시 안전성** | ✅ **MQ + Outbox 이중 보장** |
| **재시도/DLQ** | ✅ **MQ 네이티브 지원** |
| **백프레셔** | ✅ **MQ가 자동 조절** |
| **순서 보장** | ✅ Kafka Partition / SQS FIFO |
| **중복 제거** | ✅ MQ Deduplication + IdemKey |
| **적합 케이스** | ✅ **대규모 처리량** (>10,000 msg/min) |
| **결제 도메인** | ✅ **최고 수준 안정성** |

**핵심 장점**:
- ✅ **MQ 이점 총집합**: 백프레셔, 재시도, DLQ, 순서/중복 제어
- ✅ **이벤트 유실 없음**: MQ 내구성 보장
- ✅ **확장성**: 워커 수평 확장 (Consumer Group)
- ✅ **모니터링**: CloudWatch Metrics, Kafka Dashboard

**언제 사용?**:
- ✅ **MQ 인프라가 이미 있는 경우**
- ✅ 대규모 처리량 (>10,000 msg/min)
- ✅ 여러 Consumer가 필요한 경우 (Fan-out)
- ✅ 지리적 분산 처리 필요

---

## 📊 패턴 선택 가이드

### Decision Tree

```
외부 API 호출이 필요한가?
├─ Yes → Outbox Pattern 적용
│   │
│   ├─ MQ 인프라 있음? → ✅ **패턴 C (MQ 통합)**
│   │   - SQS, Kafka 등 활용
│   │   - 최고 수준 안정성
│   │
│   ├─ MQ 없음 + Critical 도메인? → ✅ **패턴 B (Hybrid)** ⭐ **권장**
│   │   - 결제, 주문, 재고
│   │   - 크래시 안전 + 재시도
│   │
│   └─ 저위험 + 단순함 우선? → ⚠️ 패턴 A (Direct)
│       - Push 알림, 로그
│       - ⚠️ 결제엔 부적합
│
└─ No → 일반 트랜잭션 패턴
    - @Transactional만으로 충분
```

### 처리량별 권장사항

| 처리량 | 권장 패턴 | 이유 |
|--------|----------|------|
| **< 100 msg/min** | 패턴 B | Outbox + 이벤트로 충분 |
| **100 - 1,000 msg/min** | 패턴 B | MySQL 성능 범위 내 |
| **1,000 - 10,000 msg/min** | 패턴 B or C | B로 시작, 병목 시 C로 |
| **> 10,000 msg/min** | **패턴 C** | MQ 필수 |

### 도메인별 권장사항

| 도메인 | 권장 패턴 | 이유 |
|--------|----------|------|
| **결제 (Payment)** | B 또는 C | 중복 방지 + 재시도 필수 |
| **주문 (Order)** | B 또는 C | 상태 일관성 중요 |
| **재고 (Inventory)** | B 또는 C | 동시성 제어 필요 |
| **알림 (Notification)** | A 또는 B | 유실 허용 가능하면 A |
| **로그 (Logging)** | A | 단순 + 빠름 |

---

## 🗄️ Outbox Schema 설계

### Outbox 테이블

```sql
-- Outbox: 외부 API 호출 대기열
CREATE TABLE payment_outbox (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Aggregate 정보
    aggregate_type VARCHAR(50) NOT NULL COMMENT '집합 타입 (PAYMENT, ORDER 등)',
    aggregate_id VARCHAR(255) NOT NULL COMMENT '집합 ID',
    event_type VARCHAR(50) NOT NULL COMMENT '이벤트 타입 (CREATED, COMPLETED 등)',

    -- Payload
    payload JSON NOT NULL COMMENT '이벤트 페이로드 (JSON)',

    -- 멱등성
    idem_key VARCHAR(255) NOT NULL COMMENT '멱등성 키 (중복 방지)',

    -- 상태 관리
    status VARCHAR(20) NOT NULL COMMENT '처리 상태 (PENDING, PUBLISHED, COMPLETED, FAILED)',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    max_retries INT NOT NULL DEFAULT 3 COMMENT '최대 재시도 횟수',

    -- 시각 정보
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    published_at DATETIME(6) NULL COMMENT 'MQ 발행 시각 (패턴 C)',
    processed_at DATETIME(6) NULL COMMENT '처리 완료 시각',

    -- 에러 정보
    error_message TEXT NULL COMMENT '에러 메시지 (실패 시)',

    -- 인덱스
    UNIQUE INDEX idx_outbox_idem_key (idem_key),
    INDEX idx_outbox_status_created (status, created_at),
    INDEX idx_outbox_aggregate (aggregate_type, aggregate_id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Outbox Status 전이

```
PENDING      초기 생성 (트랜잭션 커밋 완료)
  ↓
PUBLISHED    MQ 발행 완료 (패턴 C 전용)
  ↓
COMPLETED    외부 API 호출 성공
  ↓
(종료)

또는

PENDING → FAILED   (max_retries 초과 or 영구적 실패)
```

---

## 🔧 스케줄러 락 전략: FOR UPDATE SKIP LOCKED

### 문제: 중복 처리 방지

**시나리오**:
- 스케줄러가 1초마다 Outbox 조회
- 여러 인스턴스가 동시에 실행 중
- **동일한 Outbox를 중복 처리하면 안 됨!**

### ✅ 해결책: FOR UPDATE SKIP LOCKED

```java
/**
 * ✅ 중복 처리 방지: FOR UPDATE SKIP LOCKED
 */
@Repository
public interface PaymentOutboxRepository extends JpaRepository<PaymentOutbox, Long> {

    /**
     * PENDING 엔트리 조회 with Lock
     *
     * <p>FOR UPDATE SKIP LOCKED 전략:</p>
     * <ul>
     *   <li>다른 인스턴스가 Lock 보유 중이면 <strong>스킵</strong></li>
     *   <li>중복 처리 원천 차단</li>
     *   <li>동시성 높은 환경에 최적</li>
     * </ul>
     */
    @Query(value = """
        SELECT * FROM payment_outbox
        WHERE status = 'PENDING'
          AND created_at < :cutoff
        ORDER BY created_at ASC
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<PaymentOutbox> findPendingWithLock(
        @Param("cutoff") Instant cutoff,
        @Param("limit") int limit
    );
}
```

### FOR UPDATE SKIP LOCKED vs FOR UPDATE

| 전략 | 동작 | 장점 | 단점 |
|------|------|------|------|
| **FOR UPDATE** | Lock 대기 | 순서 보장 | 대기 시간 발생 (성능 저하) |
| **FOR UPDATE SKIP LOCKED** | Lock 스킵 | ⚡ 빠름, 중복 없음 | 순서 보장 안 됨 |

**권장**: Outbox는 **순서보다 처리량이 중요**하므로 `SKIP LOCKED` 사용

---

## 🎯 실전 예제: 결제 시스템

### 패턴 B: Outbox + Event Wake-up

```java
/**
 * Payment Service - Transactional Outbox Pattern
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentOutboxRepository outboxRepository;

    /**
     * ✅ 결제 생성: Payment + Outbox 동일 트랜잭션
     */
    @Transactional
    public PaymentId createPayment(CreatePaymentCommand command) {
        // 1. 멱등성 검사
        if (outboxRepository.existsByIdemKey(command.idemKey())) {
            PaymentOutbox existing = outboxRepository.findByIdemKey(command.idemKey())
                .orElseThrow();
            return PaymentId.of(existing.getAggregateId());
        }

        // 2. Payment 생성
        Payment payment = Payment.create(
            command.customerId(),
            command.amount(),
            command.cardInfo()
        );
        paymentRepository.save(payment);

        // 3. Outbox 기록 (동일 트랜잭션!)
        PaymentOutbox outbox = PaymentOutbox.builder()
            .aggregateType("PAYMENT")
            .aggregateId(payment.getId().toString())
            .eventType("PAYMENT_CREATED")
            .payload(toJson(payment))
            .status(OutboxStatus.PENDING)
            .idemKey(command.idemKey())
            .build();

        outboxRepository.save(outbox);

        // ✅ 둘 다 성공 or 둘 다 실패 (원자성)
        return payment.getId();
    }
}

/**
 * Outbox Relay - Event Wake-up + Fallback Polling
 */
@Component
public class PaymentOutboxRelay {

    private final PaymentOutboxRepository outboxRepository;
    private final PaymentGateway paymentGateway;

    /**
     * ✅ 1차: 이벤트로 즉시 처리 (Wake-up)
     */
    @EventListener
    @Async
    public void onWakeupSignal(OutboxWakeupSignal signal) {
        processOutbox();
    }

    /**
     * ✅ 2차: 주기적 폴링 (Fallback, 이벤트 유실 대비)
     */
    @Scheduled(fixedDelay = 5000)  // 5초마다
    public void pollOutbox() {
        processOutbox();
    }

    private void processOutbox() {
        Instant cutoff = Instant.now().minus(3, ChronoUnit.SECONDS);

        // ✅ FOR UPDATE SKIP LOCKED (중복 처리 방지)
        List<PaymentOutbox> pending = outboxRepository
            .findPendingWithLock(cutoff, 10);

        if (pending.isEmpty()) {
            return;  // 조용히 종료
        }

        log.info("Processing {} pending outbox entries", pending.size());

        pending.forEach(this::processEntry);
    }

    private void processEntry(PaymentOutbox outbox) {
        try {
            // 1. 상태 전이: PENDING → IN_PROGRESS
            outbox.markInProgress();
            outboxRepository.save(outbox);

            // 2. 외부 PG API 호출 (트랜잭션 밖!)
            PaymentResponse response = paymentGateway.charge(
                PaymentId.of(outbox.getAggregateId()),
                parsePayload(outbox.getPayload())
            );

            // 3. 성공 처리: IN_PROGRESS → COMPLETED
            outbox.markCompleted(response.transactionId());
            outboxRepository.save(outbox);

            log.info("Payment processed successfully: outboxId={}, paymentId={}",
                outbox.getId(), outbox.getAggregateId());

        } catch (RetryableException e) {
            // 재시도 가능한 오류 (네트워크 타임아웃 등)
            handleRetry(outbox, e);

        } catch (Exception e) {
            // 영구적 실패 (카드 한도 초과 등)
            handleFailure(outbox, e);
        }
    }

    private void handleRetry(PaymentOutbox outbox, Exception e) {
        boolean canRetry = outbox.retry(e.getMessage());

        if (canRetry) {
            // PENDING으로 되돌림 (재시도 대기)
            outboxRepository.save(outbox);
            log.warn("Payment will retry ({}/{}): outboxId={}, error={}",
                outbox.getRetryCount(), outbox.getMaxRetries(),
                outbox.getId(), e.getMessage());
        } else {
            // Max retries 초과 → FAILED
            outboxRepository.save(outbox);
            log.error("Payment failed after {} retries: outboxId={}",
                outbox.getMaxRetries(), outbox.getId(), e);
        }
    }

    private void handleFailure(PaymentOutbox outbox, Exception e) {
        outbox.markFailed(e.getMessage());
        outboxRepository.save(outbox);

        log.error("Payment permanently failed: outboxId={}, error={}",
            outbox.getId(), e.getMessage(), e);
    }
}
```

---

## 📋 Transactional Outbox 체크리스트

### 설계
- [ ] Outbox 테이블 생성 (`idem_key` UNIQUE 제약)
- [ ] `FOR UPDATE SKIP LOCKED` 쿼리 작성
- [ ] OutboxStatus Enum 정의 (PENDING/PUBLISHED/COMPLETED/FAILED)

### 구현
- [ ] Service: Payment + Outbox 동일 트랜잭션 (`@Transactional`)
- [ ] Publisher: `@TransactionalEventListener(AFTER_COMMIT)`
- [ ] Relay: `@EventListener` (Wake-up) + `@Scheduled` (Fallback)
- [ ] 외부 API 호출은 트랜잭션 밖 (`@Async` or 별도 워커)

### 안전성
- [ ] 멱등성 검사 (`idemKey` 중복 체크)
- [ ] 재시도 로직 (RetryableException vs PermanentException)
- [ ] Max retries 설정 (기본 3회)
- [ ] DLQ 처리 (FAILED 상태 모니터링)

### 모니터링
- [ ] PENDING 큐 길이 (< 1000)
- [ ] 평균 처리 시간 (< 3초)
- [ ] 실패율 (< 1%)
- [ ] Stuck 엔트리 (5분 이상 IN_PROGRESS)

---

## 🚀 패턴 B → C 마이그레이션 가이드

### Step 1: Outbox에 `published_at` 컬럼 추가

```sql
ALTER TABLE payment_outbox
ADD COLUMN published_at DATETIME(6) NULL COMMENT 'MQ 발행 시각';
```

### Step 2: MQ Publisher 추가

```java
@Component
public class OutboxMqPublisher {

    private final SqsTemplate sqsTemplate;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOutboxCreated(OutboxCreatedEvent event) {
        PaymentOutbox outbox = outboxRepository.findById(event.outboxId())
            .orElseThrow();

        // SQS 발행
        sqsTemplate.send("payment-queue", OutboxMessage.of(outbox));

        // PENDING → PUBLISHED
        outbox.markPublished();
        outboxRepository.save(outbox);
    }
}
```

### Step 3: MQ Worker 추가

```java
@Component
public class PaymentMqWorker {

    @SqsListener(value = "payment-queue", deletionPolicy = ON_SUCCESS)
    public void processPayment(OutboxMessage message) {
        // 외부 API 호출
        paymentGateway.charge(message);

        // PUBLISHED → COMPLETED
        PaymentOutbox outbox = outboxRepository.findById(message.outboxId())
            .orElseThrow();
        outbox.markCompleted();
        outboxRepository.save(outbox);
    }
}
```

### Step 4: 기존 Relay 제거 (점진적)

- MQ Worker 안정화 후 기존 `OutboxRelay` 제거
- Fallback Scheduler는 유지 (MQ 장애 대비)

---

## 📚 참고 자료

**패턴**:
- [Outbox Pattern (Martin Fowler)](https://microservices.io/patterns/data/transactional-outbox.html)
- [Orchestration Pattern Overview](./00_orchestration-pattern-overview.md)
- [Domain Events](../07-enterprise-patterns/event-driven/01_domain-events.md)

**구현**:
- [Spring TransactionalEventListener](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/event/TransactionalEventListener.html)
- [MySQL JSON Type](https://dev.mysql.com/doc/refman/8.0/en/json.html)
- [Spring @Scheduled](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/annotation/Scheduled.html)

---

**작성자**: Development Team
**최종 수정일**: 2025-11-05
**버전**: 2.0.0
**주요 변경사항**:
- Operation → Outbox (업계 표준 용어)
- 3가지 패턴 비교 추가 (A/B/C)
- 패턴 B를 기본 권장 패턴으로 설정
- MQ 고도화 (패턴 C) 가이드 추가
- FOR UPDATE SKIP LOCKED 락 전략 추가
- senario.txt 패턴과 100% 일치
