# Outbox Pattern: ECS 환경에 최적화된 메시지 발행 패턴

## 개요

Outbox Pattern은 비즈니스 로직과 메시지 발행을 동일한 트랜잭션으로 처리하여 강력한 원자성을 보장하는 패턴입니다.

ECS 환경에서는 별도의 Message Queue(SQS, Kafka) 없이 **MySQL + Scheduler Worker**만으로 구현 가능합니다.

## ✅ 왜 Outbox Pattern인가?

### SQS/Kafka 대신 Outbox를 사용하는 이유

| 측면 | SQS/Kafka | Outbox Pattern |
|------|-----------|----------------|
| **인프라** | 별도 Message Queue 필요 | MySQL만 사용 |
| **트랜잭션** | At-Least-Once (중복 가능성) | 강력한 원자성 보장 |
| **비용** | SQS 요금 발생 | 추가 비용 없음 |
| **레이턴시** | 네트워크 호출 (50-200ms) | DB 쿼리 (5-20ms) |
| **복잡도** | Producer/Consumer 분리 | 단순한 Scheduler |
| **ECS 통합** | 별도 Consumer Task 필요 | 동일 ECS Task 재사용 |
| **확장성** | 매우 높음 (메시지 큐 특화) | 중간 (DB 성능 의존) |
| **모니터링** | CloudWatch Metrics | MySQL Query + Logs |

### 적합한 경우

✅ **ECS 환경** (현재 환경)
✅ **중간 규모 처리량** (<10,000 msg/min)
✅ **강력한 트랜잭션 보장 필요**
✅ **인프라 단순화 선호**
✅ **MySQL 이미 사용 중**

### 부적합한 경우

❌ **대규모 처리량** (>100,000 msg/min)
❌ **지리적 분산** (Multi-Region)
❌ **다양한 Consumer 패턴** (Fan-out, Topic 분리)

---

## 🏗️ 아키텍처

### 전체 흐름

```
┌─────────────────────────────────────────────────┐
│ 1. 비즈니스 로직 + Outbox 저장 (동일 트랜잭션)   │
│    → Order 생성                                 │
│    → BoundedContextOutbox 기록 (PENDING)        │
└─────────────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────┐
│ 2. Outbox Scheduler (ECS Worker Task)           │
│    → PENDING 엔트리 폴링 (1초마다)              │
│    → 외부 API 호출                              │
│    → COMPLETED/FAILED 상태 전이                 │
└─────────────────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────┐
│ 3. 크래시 복구 (Reaper)                         │
│    → PROCESSING 상태가 5분 이상 → PENDING 복구  │
│    → FAILED 엔트리 정리 (7일 후)                │
└─────────────────────────────────────────────────┘
```

### 트랜잭션 원자성 보장

```java
@Transactional
public void createOrder(CreateOrderCommand cmd) {
    // 1. Order 생성
    Order order = orderRepository.save(cmd.toEntity());

    // 2. Outbox 기록 (동일 트랜잭션)
    BoundedContextOutboxEntry outbox = BoundedContextOutboxEntry.builder()
        .aggregateType("ORDER")
        .aggregateId(order.getId().toString())
        .eventType("ORDER_CREATED")
        .payload(toJson(order))
        .status(OutboxStatus.PENDING)
        .build();

    outboxRepository.save(outbox);

    // ✅ 둘 다 성공하거나 둘 다 실패 (원자성 보장)
}
```

**핵심**: Order 생성과 Outbox 기록이 **동일 트랜잭션**이므로, 하나라도 실패하면 전체 롤백됩니다.

---

## 🗄️ MySQL Schema 설계

### BoundedContextOutbox 테이블

```sql
-- BoundedContextOutbox: 도메인 이벤트 발행 대기열
CREATE TABLE bounded_context_outbox (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL COMMENT '집합 루트 타입 (ORDER, PAYMENT 등)',
    aggregate_id VARCHAR(255) NOT NULL COMMENT '집합 루트 ID',
    event_type VARCHAR(50) NOT NULL COMMENT '이벤트 타입 (ORDER_CREATED, PAYMENT_COMPLETED 등)',
    payload JSON NOT NULL COMMENT '이벤트 페이로드 (JSON)',
    status VARCHAR(20) NOT NULL COMMENT '처리 상태 (PENDING, PROCESSING, COMPLETED, FAILED)',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    max_retries INT NOT NULL DEFAULT 3 COMMENT '최대 재시도 횟수',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    processed_at DATETIME(6) NULL COMMENT '처리 완료 시각',
    error_message TEXT NULL COMMENT '에러 메시지 (실패 시)',

    INDEX idx_outbox_status_created (status, created_at),
    INDEX idx_outbox_aggregate (aggregate_type, aggregate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bounded Context Outbox: 도메인 이벤트 발행 대기열';
```

### 설계 포인트

#### 1. 테이블명: `bounded_context_outbox`

**이유**: 나중에 여러 Bounded Context가 생길 때 명확한 구분
- `order_outbox`, `payment_outbox` 등으로 분리 가능
- 현재는 단일 테이블로 `aggregate_type`으로 구분

#### 2. MySQL JSON 타입

**PostgreSQL과의 차이**:
- PostgreSQL: `JSONB` (Binary JSON, 인덱싱 가능)
- MySQL: `JSON` (Native JSON, 8.0+ 인덱싱 가능)

```sql
-- MySQL JSON 인덱스 (8.0+)
ALTER TABLE bounded_context_outbox
ADD INDEX idx_payload_order_id ((CAST(payload->>'$.orderId' AS CHAR(255))));
```

#### 3. Index 전략

```sql
-- 1. 폴링 쿼리 최적화 (status, created_at)
INDEX idx_outbox_status_created (status, created_at)

-- 2. 집합 조회 최적화 (aggregate_type, aggregate_id)
INDEX idx_outbox_aggregate (aggregate_type, aggregate_id)
```

#### 4. `DATETIME(6)`: 마이크로초 정밀도

```sql
created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
```

**이유**: 동시성 높은 환경에서 정확한 순서 보장

---

## 📦 Entity 설계

### BoundedContextOutboxEntry.java

```java
package com.company.template.application.outbox.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * BoundedContextOutbox Entity
 *
 * <p>도메인 이벤트 발행을 위한 Outbox Pattern 구현체</p>
 *
 * <h3>책임</h3>
 * <ul>
 *   <li>비즈니스 로직과 동일한 트랜잭션으로 이벤트 저장</li>
 *   <li>Scheduler Worker가 폴링하여 이벤트 처리</li>
 *   <li>크래시 복구 및 재시도 지원</li>
 * </ul>
 *
 * <h3>상태 전이</h3>
 * <pre>
 * PENDING → PROCESSING → COMPLETED
 *                      ↘ FAILED (max retries 초과)
 * </pre>
 *
 * @author Your Name
 * @since 2024-01-01
 */
@Entity
@Table(
    name = "bounded_context_outbox",
    indexes = {
        @Index(name = "idx_outbox_status_created", columnList = "status, created_at"),
        @Index(name = "idx_outbox_aggregate", columnList = "aggregate_type, aggregate_id")
    }
)
public class BoundedContextOutboxEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 255)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "JSON")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "max_retries", nullable = false)
    private int maxRetries;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // ========================================
    // Constructor
    // ========================================

    protected BoundedContextOutboxEntry() {
        // JPA only
    }

    private BoundedContextOutboxEntry(Builder builder) {
        this.aggregateType = builder.aggregateType;
        this.aggregateId = builder.aggregateId;
        this.eventType = builder.eventType;
        this.payload = builder.payload;
        this.status = builder.status;
        this.retryCount = builder.retryCount;
        this.maxRetries = builder.maxRetries;
        this.createdAt = builder.createdAt;
        this.processedAt = builder.processedAt;
        this.errorMessage = builder.errorMessage;
    }

    // ========================================
    // Business Methods
    // ========================================

    /**
     * PROCESSING 상태로 전이
     *
     * @throws IllegalStateException PENDING 상태가 아닐 때
     */
    public void markProcessing() {
        if (this.status != OutboxStatus.PENDING) {
            throw new IllegalStateException(
                "PROCESSING 상태로 전이할 수 없습니다. 현재 상태: " + this.status
            );
        }
        this.status = OutboxStatus.PROCESSING;
    }

    /**
     * COMPLETED 상태로 전이
     *
     * @param processedAt 처리 완료 시각
     * @throws IllegalStateException PROCESSING 상태가 아닐 때
     */
    public void markCompleted(Instant processedAt) {
        if (this.status != OutboxStatus.PROCESSING) {
            throw new IllegalStateException(
                "COMPLETED 상태로 전이할 수 없습니다. 현재 상태: " + this.status
            );
        }
        this.status = OutboxStatus.COMPLETED;
        this.processedAt = processedAt;
    }

    /**
     * 재시도 처리 (PENDING 상태로 되돌림)
     *
     * @param errorMessage 에러 메시지
     * @return 재시도 가능 여부 (true: PENDING 복구, false: FAILED 전이)
     */
    public boolean retry(String errorMessage) {
        this.retryCount++;
        this.errorMessage = errorMessage;

        if (this.retryCount >= this.maxRetries) {
            // Max retries 초과 → FAILED
            this.status = OutboxStatus.FAILED;
            return false;
        } else {
            // 재시도 가능 → PENDING
            this.status = OutboxStatus.PENDING;
            return true;
        }
    }

    /**
     * FAILED 상태로 전이 (즉시 실패)
     *
     * @param errorMessage 에러 메시지
     */
    public void markFailed(String errorMessage) {
        this.status = OutboxStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    // ========================================
    // Getters
    // ========================================

    public Long getId() {
        return id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    // ========================================
    // Builder
    // ========================================

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String aggregateType;
        private String aggregateId;
        private String eventType;
        private String payload;
        private OutboxStatus status = OutboxStatus.PENDING;
        private int retryCount = 0;
        private int maxRetries = 3;
        private Instant createdAt = Instant.now();
        private Instant processedAt;
        private String errorMessage;

        public Builder aggregateType(String aggregateType) {
            this.aggregateType = aggregateType;
            return this;
        }

        public Builder aggregateId(String aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }

        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder status(OutboxStatus status) {
            this.status = status;
            return this;
        }

        public Builder retryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder processedAt(Instant processedAt) {
            this.processedAt = processedAt;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public BoundedContextOutboxEntry build() {
            if (aggregateType == null || aggregateType.isBlank()) {
                throw new IllegalArgumentException("aggregateType은 필수입니다.");
            }
            if (aggregateId == null || aggregateId.isBlank()) {
                throw new IllegalArgumentException("aggregateId는 필수입니다.");
            }
            if (eventType == null || eventType.isBlank()) {
                throw new IllegalArgumentException("eventType은 필수입니다.");
            }
            if (payload == null || payload.isBlank()) {
                throw new IllegalArgumentException("payload는 필수입니다.");
            }

            return new BoundedContextOutboxEntry(this);
        }
    }
}
```

### OutboxStatus.java

```java
package com.company.template.application.outbox.entity;

/**
 * Outbox 처리 상태
 *
 * <h3>상태 전이</h3>
 * <pre>
 * PENDING → PROCESSING → COMPLETED
 *                      ↘ FAILED (max retries 초과)
 * </pre>
 *
 * @author Your Name
 * @since 2024-01-01
 */
public enum OutboxStatus {

    /**
     * 처리 대기 중
     *
     * <p>Scheduler가 폴링하여 처리할 엔트리</p>
     */
    PENDING,

    /**
     * 처리 중
     *
     * <p>외부 API 호출 등 실제 처리가 진행 중</p>
     */
    PROCESSING,

    /**
     * 처리 완료
     *
     * <p>정상적으로 처리가 완료됨</p>
     */
    COMPLETED,

    /**
     * 처리 실패
     *
     * <p>최대 재시도 횟수를 초과하여 실패</p>
     */
    FAILED
}
```

---

## 🔧 Repository 설계

### BoundedContextOutboxRepository.java

```java
package com.company.template.application.outbox.repository;

import com.company.template.application.outbox.entity.BoundedContextOutboxEntry;
import com.company.template.application.outbox.entity.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * BoundedContextOutbox Repository
 *
 * @author Your Name
 * @since 2024-01-01
 */
@Repository
public interface BoundedContextOutboxRepository extends JpaRepository<BoundedContextOutboxEntry, Long> {

    /**
     * PENDING 엔트리 조회 (폴링용)
     *
     * <p>Scheduler가 1초마다 호출하여 처리할 엔트리 조회</p>
     *
     * @param status 상태 (PENDING)
     * @param cutoff 생성 시각 기준 (N초 이전 엔트리만 조회)
     * @return PENDING 엔트리 목록 (최대 100개)
     */
    @Query("SELECT o FROM BoundedContextOutboxEntry o " +
           "WHERE o.status = :status " +
           "AND o.createdAt < :cutoff " +
           "ORDER BY o.createdAt ASC")
    List<BoundedContextOutboxEntry> findPendingEntries(
        @Param("status") OutboxStatus status,
        @Param("cutoff") Instant cutoff
    );

    /**
     * FAILED 엔트리 조회 (정리용)
     *
     * <p>Reaper가 매일 자정에 호출하여 오래된 FAILED 엔트리 정리</p>
     *
     * @param status 상태 (FAILED)
     * @param cutoff 생성 시각 기준 (N일 이전 엔트리만 조회)
     * @return FAILED 엔트리 목록
     */
    List<BoundedContextOutboxEntry> findByStatusAndCreatedAtBefore(
        OutboxStatus status,
        Instant cutoff
    );

    /**
     * PROCESSING 엔트리 조회 (크래시 복구용)
     *
     * <p>Reaper가 5분마다 호출하여 5분 이상 PROCESSING 상태인 엔트리를 PENDING으로 복구</p>
     *
     * @param status 상태 (PROCESSING)
     * @param cutoff 생성 시각 기준 (N분 이전 엔트리만 조회)
     * @return PROCESSING 엔트리 목록 (stuck entries)
     */
    List<BoundedContextOutboxEntry> findByStatusAndCreatedAtBefore(
        OutboxStatus status,
        Instant cutoff
    );
}
```

---

## 🔄 Scheduler Worker 설계

### OutboxScheduler.java

```java
package com.company.template.application.outbox.scheduler;

import com.company.template.application.outbox.entity.BoundedContextOutboxEntry;
import com.company.template.application.outbox.entity.OutboxStatus;
import com.company.template.application.outbox.processor.OutboxProcessor;
import com.company.template.application.outbox.repository.BoundedContextOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Outbox Scheduler: Bounded Context Outbox 폴링 및 처리
 *
 * <h3>책임</h3>
 * <ul>
 *   <li>PENDING 엔트리 폴링 (1초마다)</li>
 *   <li>OutboxProcessor에 처리 위임</li>
 *   <li>크래시 복구 (PROCESSING → PENDING)</li>
 *   <li>FAILED 엔트리 정리 (7일 후)</li>
 * </ul>
 *
 * <h3>ECS 배포</h3>
 * <ul>
 *   <li>Main Application과 별도의 ECS Task로 실행</li>
 *   <li>Replica 1개만 실행 (중복 처리 방지)</li>
 *   <li>spring.profiles.active=production,scheduler</li>
 * </ul>
 *
 * @author Your Name
 * @since 2024-01-01
 */
@Component
public class OutboxScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxScheduler.class);

    private final BoundedContextOutboxRepository outboxRepository;
    private final OutboxProcessor outboxProcessor;

    public OutboxScheduler(
            BoundedContextOutboxRepository outboxRepository,
            OutboxProcessor outboxProcessor) {
        this.outboxRepository = outboxRepository;
        this.outboxProcessor = outboxProcessor;
    }

    /**
     * Outbox 폴링 스케줄러
     *
     * <p>1초마다 실행하여 5초 이상 된 PENDING 엔트리 처리</p>
     *
     * <h4>왜 5초 기준?</h4>
     * <ul>
     *   <li>트랜잭션 커밋 후 즉시 폴링하면 DB Replication Lag 발생 가능</li>
     *   <li>5초 버퍼로 DB 동기화 보장</li>
     * </ul>
     */
    @Scheduled(fixedRate = 1000)  // 1초마다
    @Transactional
    public void pollOutbox() {
        Instant cutoff = Instant.now().minus(5, ChronoUnit.SECONDS);

        // 1. PENDING 엔트리 조회 (최대 100개)
        List<BoundedContextOutboxEntry> entries = outboxRepository.findPendingEntries(
            OutboxStatus.PENDING,
            cutoff
        );

        if (entries.isEmpty()) {
            return;  // 조용히 종료 (로그 불필요)
        }

        log.info("Outbox polling: found {} PENDING entries", entries.size());

        // 2. 각 엔트리 처리
        for (BoundedContextOutboxEntry entry : entries) {
            processOutboxEntry(entry);
        }
    }

    /**
     * 크래시 복구 스케줄러 (Reaper)
     *
     * <p>5분마다 실행하여 5분 이상 PROCESSING 상태인 엔트리를 PENDING으로 복구</p>
     *
     * <h4>크래시 시나리오</h4>
     * <pre>
     * T1: PENDING → PROCESSING 전이
     * T2: ⚠️ ECS Task 크래시 (OOM, 재배포 등)
     * T3: Reaper가 5분 후 PROCESSING → PENDING 복구
     * T4: ✅ 정상 재처리
     * </pre>
     */
    @Scheduled(fixedRate = 300000)  // 5분마다
    @Transactional
    public void recoverStuckEntries() {
        Instant cutoff = Instant.now().minus(5, ChronoUnit.MINUTES);

        List<BoundedContextOutboxEntry> stuckEntries = outboxRepository
            .findByStatusAndCreatedAtBefore(OutboxStatus.PROCESSING, cutoff);

        if (stuckEntries.isEmpty()) {
            return;
        }

        log.warn("Reaper: recovering {} stuck PROCESSING entries", stuckEntries.size());

        for (BoundedContextOutboxEntry entry : stuckEntries) {
            entry.retry("Recovered by Reaper: stuck in PROCESSING for 5 minutes");
            outboxRepository.save(entry);
            log.warn("Reaper: recovered entry {}", entry.getId());
        }
    }

    /**
     * FAILED 엔트리 정리 스케줄러
     *
     * <p>매일 자정에 실행하여 7일 이상 된 FAILED 엔트리 삭제</p>
     *
     * <h4>정리 정책</h4>
     * <ul>
     *   <li>FAILED 엔트리는 DLQ(Dead Letter Queue) 역할</li>
     *   <li>7일 보관 후 자동 삭제</li>
     *   <li>필요 시 별도 아카이빙 테이블로 이동 가능</li>
     * </ul>
     */
    @Scheduled(cron = "0 0 0 * * *")  // 매일 자정
    @Transactional
    public void cleanupFailedEntries() {
        Instant cutoff = Instant.now().minus(7, ChronoUnit.DAYS);

        List<BoundedContextOutboxEntry> failedEntries = outboxRepository
            .findByStatusAndCreatedAtBefore(OutboxStatus.FAILED, cutoff);

        if (failedEntries.isEmpty()) {
            return;
        }

        log.warn("Cleanup: deleting {} FAILED entries older than 7 days", failedEntries.size());

        // 옵션 1: 삭제
        outboxRepository.deleteAll(failedEntries);

        // 옵션 2: 아카이빙 (별도 테이블로 이동)
        // archiveFailedEntries(failedEntries);
    }

    // ========================================
    // Private Methods
    // ========================================

    private void processOutboxEntry(BoundedContextOutboxEntry entry) {
        try {
            // 1. PROCESSING 상태로 전이
            entry.markProcessing();
            outboxRepository.save(entry);

            // 2. 실제 처리 (외부 API 호출 등)
            outboxProcessor.process(entry);

            // 3. COMPLETED 상태로 전이
            entry.markCompleted(Instant.now());
            outboxRepository.save(entry);

            log.info("Outbox entry processed successfully: id={}, eventType={}",
                entry.getId(), entry.getEventType());

        } catch (Exception e) {
            handleProcessingFailure(entry, e);
        }
    }

    private void handleProcessingFailure(BoundedContextOutboxEntry entry, Exception e) {
        boolean canRetry = entry.retry(e.getMessage());

        if (canRetry) {
            // 재시도 가능 → PENDING 복구
            outboxRepository.save(entry);
            log.warn("Outbox entry failed, will retry ({}/{}): id={}, eventType={}",
                entry.getRetryCount(), entry.getMaxRetries(), entry.getId(), entry.getEventType(), e);
        } else {
            // Max retries 초과 → FAILED
            outboxRepository.save(entry);
            log.error("Outbox entry failed after {} retries: id={}, eventType={}",
                entry.getMaxRetries(), entry.getId(), entry.getEventType(), e);
        }
    }
}
```

---

## 🎯 Processor 설계

### OutboxProcessor.java

```java
package com.company.template.application.outbox.processor;

import com.company.template.application.outbox.entity.BoundedContextOutboxEntry;
import com.company.template.application.outbox.handler.OutboxHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Outbox Processor: Event Type별 Handler 라우팅
 *
 * <h3>책임</h3>
 * <ul>
 *   <li>Event Type에 따라 적절한 Handler로 라우팅</li>
 *   <li>Handler 등록 및 관리</li>
 * </ul>
 *
 * @author Your Name
 * @since 2024-01-01
 */
@Component
public class OutboxProcessor {

    private final Map<String, OutboxHandler> handlers;

    public OutboxProcessor(List<OutboxHandler> handlerList) {
        this.handlers = handlerList.stream()
            .collect(Collectors.toMap(
                OutboxHandler::getEventType,
                Function.identity()
            ));
    }

    /**
     * Outbox 엔트리 처리
     *
     * @param entry Outbox 엔트리
     * @throws IllegalStateException Handler가 등록되지 않은 Event Type
     */
    public void process(BoundedContextOutboxEntry entry) {
        OutboxHandler handler = handlers.get(entry.getEventType());

        if (handler == null) {
            throw new IllegalStateException(
                "No handler found for event type: " + entry.getEventType()
            );
        }

        handler.handle(entry);
    }
}
```

### OutboxHandler.java (Interface)

```java
package com.company.template.application.outbox.handler;

import com.company.template.application.outbox.entity.BoundedContextOutboxEntry;

/**
 * Outbox Handler Interface
 *
 * <p>Event Type별로 구현하여 실제 처리 로직을 작성합니다.</p>
 *
 * @author Your Name
 * @since 2024-01-01
 */
public interface OutboxHandler {

    /**
     * 처리할 Event Type 반환
     *
     * @return Event Type (예: "ORDER_CREATED")
     */
    String getEventType();

    /**
     * Outbox 엔트리 처리
     *
     * @param entry Outbox 엔트리
     * @throws Exception 처리 실패 시
     */
    void handle(BoundedContextOutboxEntry entry) throws Exception;
}
```

### OrderCreatedOutboxHandler.java (Example)

```java
package com.company.template.application.outbox.handler.order;

import com.company.template.application.outbox.entity.BoundedContextOutboxEntry;
import com.company.template.application.outbox.handler.OutboxHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ORDER_CREATED Event Handler
 *
 * <h3>책임</h3>
 * <ul>
 *   <li>주문 생성 이벤트 처리</li>
 *   <li>재고 감소 (Inventory API)</li>
 *   <li>주문 확인 이메일 발송</li>
 * </ul>
 *
 * @author Your Name
 * @since 2024-01-01
 */
@Component
public class OrderCreatedOutboxHandler implements OutboxHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedOutboxHandler.class);

    private final ObjectMapper objectMapper;
    private final InventoryClient inventoryClient;
    private final EmailService emailService;

    public OrderCreatedOutboxHandler(
            ObjectMapper objectMapper,
            InventoryClient inventoryClient,
            EmailService emailService) {
        this.objectMapper = objectMapper;
        this.inventoryClient = inventoryClient;
        this.emailService = emailService;
    }

    @Override
    public String getEventType() {
        return "ORDER_CREATED";
    }

    @Override
    public void handle(BoundedContextOutboxEntry entry) throws Exception {
        // 1. Payload 파싱
        OrderCreatedPayload payload = objectMapper.readValue(
            entry.getPayload(),
            OrderCreatedPayload.class
        );

        log.info("Processing ORDER_CREATED: orderId={}, customerId={}",
            payload.getOrderId(), payload.getCustomerId());

        // 2. 재고 감소 (외부 API)
        inventoryClient.decreaseStock(payload.getItems());

        // 3. 이메일 발송
        emailService.sendOrderConfirmation(
            payload.getOrderId(),
            payload.getCustomerId()
        );

        log.info("ORDER_CREATED processed successfully: orderId={}", payload.getOrderId());
    }

    // ========================================
    // Payload DTO
    // ========================================

    public static class OrderCreatedPayload {
        private String orderId;
        private String customerId;
        private List<OrderLineItemDto> items;

        // Getters/Setters
        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public List<OrderLineItemDto> getItems() {
            return items;
        }

        public void setItems(List<OrderLineItemDto> items) {
            this.items = items;
        }
    }

    public static class OrderLineItemDto {
        private String productId;
        private int quantity;

        // Getters/Setters
        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
```

---

## 🚀 ECS 배포 전략

### 아키텍처: Main Application + Outbox Worker

```
┌─────────────────────────────────────────┐
│ ECS Service: main-app                   │
├─────────────────────────────────────────┤
│ - spring.profiles.active=production     │
│ - SCHEDULER_ENABLED=false               │
│ - Replicas: 3 (Auto Scaling)            │
│ - 역할: REST API 처리                   │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ ECS Service: outbox-worker              │
├─────────────────────────────────────────┤
│ - spring.profiles.active=production,    │
│   scheduler                             │
│ - SCHEDULER_ENABLED=true                │
│ - Replicas: 1 (고정, 중복 처리 방지)    │
│ - 역할: Outbox 폴링 및 처리             │
└─────────────────────────────────────────┘
```

### docker-compose.yml (로컬 개발 환경)

```yaml
version: '3.8'

services:
  # Main Application
  app:
    image: my-spring-app:latest
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: local
      SCHEDULER_ENABLED: "false"
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/mydb
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: password
    depends_on:
      - mysql

  # Outbox Scheduler Worker (별도 Task)
  outbox-worker:
    image: my-spring-app:latest
    environment:
      SPRING_PROFILES_ACTIVE: local,scheduler
      SCHEDULER_ENABLED: "true"
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/mydb
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: password
    depends_on:
      - mysql

  # MySQL
  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: mydb
    volumes:
      - mysql-data:/var/lib/mysql

volumes:
  mysql-data:
```

### application-scheduler.yml

```yaml
# Scheduler Worker 전용 프로파일

spring:
  # ========================================
  # Web Application 비활성화
  # ========================================
  main:
    web-application-type: none  # REST API 비활성화 (Scheduler만 실행)

  # ========================================
  # Scheduler 설정
  # ========================================
  task:
    scheduling:
      pool:
        size: 10  # Scheduler 스레드 풀 크기
      thread-name-prefix: outbox-scheduler-

  # ========================================
  # Logging (Scheduler 전용)
  # ========================================
logging:
  level:
    com.company.template.application.outbox.scheduler: INFO
    com.company.template.application.outbox.processor: DEBUG
    com.company.template.application.outbox.handler: DEBUG
```

### ECS Task Definition (Terraform 예시)

```hcl
# Main Application Task
resource "aws_ecs_task_definition" "main_app" {
  family                   = "main-app"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "1024"
  memory                   = "2048"

  container_definitions = jsonencode([{
    name  = "main-app"
    image = "my-ecr-repo/my-spring-app:latest"

    environment = [
      {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "production"
      },
      {
        name  = "SCHEDULER_ENABLED"
        value = "false"
      }
    ]

    portMappings = [{
      containerPort = 8080
      protocol      = "tcp"
    }]
  }])
}

# Outbox Worker Task
resource "aws_ecs_task_definition" "outbox_worker" {
  family                   = "outbox-worker"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "512"
  memory                   = "1024"

  container_definitions = jsonencode([{
    name  = "outbox-worker"
    image = "my-ecr-repo/my-spring-app:latest"

    environment = [
      {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "production,scheduler"
      },
      {
        name  = "SCHEDULER_ENABLED"
        value = "true"
      }
    ]
  }])
}

# Main Application Service (Auto Scaling)
resource "aws_ecs_service" "main_app" {
  name            = "main-app"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.main_app.arn
  desired_count   = 3  # Auto Scaling 가능

  # ... (생략)
}

# Outbox Worker Service (단일 인스턴스)
resource "aws_ecs_service" "outbox_worker" {
  name            = "outbox-worker"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.outbox_worker.arn
  desired_count   = 1  # 반드시 1개 (중복 처리 방지)

  # ... (생략)
}
```

---

## 📊 모니터링 및 운영

### 핵심 메트릭

```sql
-- 1. Outbox 큐 길이 (PENDING 엔트리 개수)
SELECT COUNT(*) AS pending_count
FROM bounded_context_outbox
WHERE status = 'PENDING';

-- 2. 평균 처리 시간
SELECT
    AVG(TIMESTAMPDIFF(SECOND, created_at, processed_at)) AS avg_processing_time_sec
FROM bounded_context_outbox
WHERE status = 'COMPLETED'
  AND processed_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR);

-- 3. 실패율 (최근 1시간)
SELECT
    event_type,
    COUNT(*) AS total_count,
    SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) AS failed_count,
    ROUND(SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) AS failure_rate
FROM bounded_context_outbox
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
GROUP BY event_type;

-- 4. Stuck 엔트리 (5분 이상 PROCESSING)
SELECT
    id,
    event_type,
    aggregate_id,
    created_at,
    TIMESTAMPDIFF(MINUTE, created_at, NOW()) AS stuck_minutes
FROM bounded_context_outbox
WHERE status = 'PROCESSING'
  AND created_at < DATE_SUB(NOW(), INTERVAL 5 MINUTE);
```

### CloudWatch Alarms (예시)

```hcl
# 1. Outbox 큐 길이 알람 (PENDING > 1000)
resource "aws_cloudwatch_metric_alarm" "outbox_queue_length" {
  alarm_name          = "outbox-queue-length-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "OutboxPendingCount"
  namespace           = "MyApp/Outbox"
  period              = "60"
  statistic           = "Average"
  threshold           = "1000"
  alarm_description   = "Outbox PENDING 엔트리가 1000개 초과"
  alarm_actions       = [aws_sns_topic.alerts.arn]
}

# 2. 처리 실패율 알람 (Failure Rate > 10%)
resource "aws_cloudwatch_metric_alarm" "outbox_failure_rate" {
  alarm_name          = "outbox-failure-rate-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "OutboxFailureRate"
  namespace           = "MyApp/Outbox"
  period              = "300"
  statistic           = "Average"
  threshold           = "10"
  alarm_description   = "Outbox 실패율이 10% 초과"
  alarm_actions       = [aws_sns_topic.alerts.arn]
}
```

### 로그 기반 모니터링 (Elasticsearch/Kibana)

```json
{
  "query": {
    "bool": {
      "must": [
        { "match": { "logger_name": "OutboxScheduler" } },
        { "match": { "level": "ERROR" } },
        { "range": { "@timestamp": { "gte": "now-1h" } } }
      ]
    }
  }
}
```

---

## ⚠️ 주의사항 및 Best Practices

### 1. 단일 Scheduler 인스턴스 유지

**중복 처리 방지**:
```yaml
# ECS Service
desired_count: 1  # 반드시 1개
```

**이유**: 여러 인스턴스가 동시에 폴링하면 동일한 엔트리를 중복 처리할 수 있습니다.

### 2. 멱등성 보장

**Handler는 반드시 멱등성을 보장해야 합니다**:
```java
@Override
public void handle(BoundedContextOutboxEntry entry) throws Exception {
    // ✅ 멱등성 보장: 동일한 orderId로 여러 번 호출해도 안전
    inventoryClient.decreaseStockIdempotent(payload.getOrderId(), payload.getItems());

    // ❌ 멱등성 미보장: 중복 호출 시 재고가 2배로 감소
    inventoryClient.decreaseStock(payload.getItems());
}
```

### 3. Payload 크기 제한

**MySQL JSON 타입 제한**:
- MySQL 8.0: 최대 1GB (실제로는 max_allowed_packet 설정에 의존)
- 권장: Payload는 10KB 이하로 유지

**대용량 Payload 처리**:
```java
// ❌ 나쁜 예: 전체 Order 데이터를 Payload에 저장
String payload = objectMapper.writeValueAsString(order);

// ✅ 좋은 예: Order ID만 저장, Handler에서 조회
String payload = objectMapper.writeValueAsString(Map.of(
    "orderId", order.getId().toString()
));
```

### 4. 트랜잭션 경계 주의

**Scheduler의 @Transactional 범위**:
```java
@Transactional
public void pollOutbox() {
    // ✅ DB 조회는 트랜잭션 내부
    List<BoundedContextOutboxEntry> entries = outboxRepository.findPendingEntries(...);

    for (BoundedContextOutboxEntry entry : entries) {
        // ✅ 각 엔트리 처리는 별도 try-catch
        processOutboxEntry(entry);
    }
}

private void processOutboxEntry(BoundedContextOutboxEntry entry) {
    try {
        // ⚠️ 외부 API 호출은 트랜잭션 밖에서 (상위 메서드가 @Transactional이지만, 여기서는 DB 업데이트만)
        outboxProcessor.process(entry);

        // ✅ DB 업데이트는 트랜잭션 내부
        entry.markCompleted(Instant.now());
        outboxRepository.save(entry);
    } catch (Exception e) {
        // ✅ 실패 시 재시도 또는 FAILED 처리
        handleProcessingFailure(entry, e);
    }
}
```

### 5. MySQL Replication Lag 고려

**5초 버퍼의 이유**:
```java
Instant cutoff = Instant.now().minus(5, ChronoUnit.SECONDS);
```

- **Primary-Replica 구조**: Write는 Primary, Read는 Replica
- **Replication Lag**: Primary → Replica 동기화 지연 (보통 1-3초)
- **5초 버퍼**: 트랜잭션 커밋 후 즉시 폴링하지 않고 5초 대기하여 Replica 동기화 보장

---

## 🧪 테스트 전략

### 1. Unit Test (Handler)

```java
@ExtendWith(MockitoExtension.class)
class OrderCreatedOutboxHandlerTest {

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private EmailService emailService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderCreatedOutboxHandler handler;

    @Test
    void handle_ShouldDecreaseStockAndSendEmail() throws Exception {
        // Given
        BoundedContextOutboxEntry entry = BoundedContextOutboxEntry.builder()
            .aggregateType("ORDER")
            .aggregateId("order-123")
            .eventType("ORDER_CREATED")
            .payload("{\"orderId\":\"order-123\",\"customerId\":\"customer-456\"}")
            .status(OutboxStatus.PENDING)
            .build();

        OrderCreatedPayload payload = new OrderCreatedPayload();
        payload.setOrderId("order-123");
        payload.setCustomerId("customer-456");

        when(objectMapper.readValue(anyString(), eq(OrderCreatedPayload.class)))
            .thenReturn(payload);

        // When
        handler.handle(entry);

        // Then
        verify(inventoryClient).decreaseStock(anyList());
        verify(emailService).sendOrderConfirmation("order-123", "customer-456");
    }
}
```

### 2. Integration Test (Scheduler + Repository)

```java
@SpringBootTest
@Testcontainers
class OutboxSchedulerIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private BoundedContextOutboxRepository outboxRepository;

    @Autowired
    private OutboxScheduler outboxScheduler;

    @Test
    void pollOutbox_ShouldProcessPendingEntries() {
        // Given
        BoundedContextOutboxEntry entry = BoundedContextOutboxEntry.builder()
            .aggregateType("ORDER")
            .aggregateId("order-123")
            .eventType("ORDER_CREATED")
            .payload("{\"orderId\":\"order-123\"}")
            .status(OutboxStatus.PENDING)
            .createdAt(Instant.now().minus(10, ChronoUnit.SECONDS))  // 10초 전 생성
            .build();

        outboxRepository.save(entry);

        // When
        outboxScheduler.pollOutbox();

        // Then
        BoundedContextOutboxEntry processed = outboxRepository.findById(entry.getId()).orElseThrow();
        assertThat(processed.getStatus()).isEqualTo(OutboxStatus.COMPLETED);
        assertThat(processed.getProcessedAt()).isNotNull();
    }
}
```

---

## 📖 참고 자료

- [Outbox Pattern (Martin Fowler)](https://microservices.io/patterns/data/transactional-outbox.html)
- [MySQL JSON Type](https://dev.mysql.com/doc/refman/8.0/en/json.html)
- [Spring @Scheduled](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/annotation/Scheduled.html)
- [AWS ECS Task Definition](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task_definitions.html)

---

## 🎯 마이그레이션 가이드: SQS → Outbox

### Step 1: Schema 생성

```sql
-- MySQL 8.0+
CREATE TABLE bounded_context_outbox (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload JSON NOT NULL,
    status VARCHAR(20) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 3,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    processed_at DATETIME(6) NULL,
    error_message TEXT NULL,

    INDEX idx_outbox_status_created (status, created_at),
    INDEX idx_outbox_aggregate (aggregate_type, aggregate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Step 2: Entity 및 Repository 추가

위의 `BoundedContextOutboxEntry.java` 및 `BoundedContextOutboxRepository.java` 추가

### Step 3: Scheduler 및 Processor 추가

위의 `OutboxScheduler.java` 및 `OutboxProcessor.java` 추가

### Step 4: Handler 구현

기존 SQS Consumer를 Outbox Handler로 변경:

```java
// ❌ 기존 (SQS Consumer)
@KafkaListener(topics = "order-created-topic")
public void handleOrderCreated(OrderCreatedMessage message) {
    inventoryClient.decreaseStock(message.getItems());
    emailService.sendOrderConfirmation(message.getOrderId(), message.getCustomerId());
}

// ✅ 신규 (Outbox Handler)
@Component
public class OrderCreatedOutboxHandler implements OutboxHandler {
    @Override
    public String getEventType() {
        return "ORDER_CREATED";
    }

    @Override
    public void handle(BoundedContextOutboxEntry entry) throws Exception {
        OrderCreatedPayload payload = parsePayload(entry.getPayload());
        inventoryClient.decreaseStock(payload.getItems());
        emailService.sendOrderConfirmation(payload.getOrderId(), payload.getCustomerId());
    }
}
```

### Step 5: 비즈니스 로직 수정

```java
// ❌ 기존 (SQS 발행)
@Transactional
public void createOrder(CreateOrderCommand cmd) {
    Order order = orderRepository.save(cmd.toEntity());

    // SQS 발행 (트랜잭션 밖)
    sqsClient.sendMessage(OrderCreatedMessage.of(order));
}

// ✅ 신규 (Outbox 저장)
@Transactional
public void createOrder(CreateOrderCommand cmd) {
    Order order = orderRepository.save(cmd.toEntity());

    // Outbox 저장 (동일 트랜잭션)
    BoundedContextOutboxEntry outbox = BoundedContextOutboxEntry.builder()
        .aggregateType("ORDER")
        .aggregateId(order.getId().toString())
        .eventType("ORDER_CREATED")
        .payload(toJson(order))
        .status(OutboxStatus.PENDING)
        .build();

    outboxRepository.save(outbox);
}
```

### Step 6: ECS Task Definition 수정

Main App과 Outbox Worker를 별도 Task로 분리 (위의 ECS 배포 전략 참고)

### Step 7: 모니터링 설정

CloudWatch Alarms 및 Dashboard 설정 (위의 모니터링 섹션 참고)

---

**✅ Outbox Pattern 문서 작성 완료!**

이제 다음 단계인 **커맨드 및 스킬 세팅**으로 넘어갈 준비가 되었습니다.
