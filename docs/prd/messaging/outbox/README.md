# MessageOutbox Bounded Context

**Bounded Context**: `messaging/outbox`
**Dependencies**: `session/single` (File Aggregate)
**예상 기간**: 3일
**우선순위**: Level 2 (session/single 완료 후)

---

## 📋 개요

**목적**: Transactional Outbox Pattern을 구현하여 파일 업로드 완료 이벤트를 안전하게 외부 시스템(SQS, Webhook)으로 전달합니다.

**핵심 문제 해결**:
- **이중 쓰기 문제**: DB 커밋 성공 후 SQS 전송 실패 시 데이터 불일치 발생
- **At-Least-Once 보장**: 이벤트 최소 1회 전달 보장
- **트랜잭션 보장**: DB 커밋과 메시지 발송을 원자적으로 처리

---

## 🎯 주요 기능

### In Scope
1. **MessageOutbox Aggregate** - 발신 메시지 저장 및 상태 관리
2. **Transactional Outbox Pattern** - DB 트랜잭션 내 메시지 저장
3. **Message Relay** - 스케줄러 기반 메시지 전송 (SQS, Webhook)
4. **재시도 로직** - 전송 실패 시 exponential backoff
5. **중복 방지** - 멱등키 기반 메시지 중복 전송 방지

### Out of Scope (Future)
- Message Inbox (수신 메시지 처리)
- Event Sourcing
- Saga Pattern
- Dead Letter Queue 자동 처리

---

## 🏗️ Domain Layer

### Aggregates

#### 1. MessageOutbox
**책임**: 발신 메시지 생명주기 관리

**주요 메서드**:
```java
public class MessageOutbox {
    private OutboxId outboxId;              // UUID v7
    private EventType eventType;            // FILE_UPLOADED, FILE_PROCESSED
    private AggregateId aggregateId;        // FileId
    private String payload;                 // JSON
    private OutboxStatus status;            // PENDING, SENT, FAILED
    private int retryCount;                 // 재시도 횟수
    private LocalDateTime scheduledAt;      // 전송 예정 시각
    private LocalDateTime sentAt;           // 전송 완료 시각

    public static MessageOutbox create(EventType eventType, FileId fileId, String payload, Clock clock);
    public void markAsSent(Clock clock);
    public void markAsFailed(Clock clock);
    public void scheduleRetry(Clock clock);  // Exponential backoff
    public boolean isRetryable();            // 최대 3회 재시도
}
```

### Enums

#### OutboxStatus
- `PENDING`: 전송 대기
- `SENT`: 전송 완료
- `FAILED`: 전송 실패 (최종)

#### EventType
- `FILE_UPLOADED`: 파일 업로드 완료
- `FILE_PROCESSED`: 파일 가공 완료 (Level 3)

---

## 📦 Application Layer

### Use Cases

#### 1. PublishFileUploadedEvent (Command)
**책임**: File Aggregate 생성 시 이벤트 발행

**트랜잭션 전략**:
```java
@Transactional
public void publishFileUploadedEvent(FileId fileId) {
    // 1. File Aggregate 조회
    File file = fileQueryPort.findById(fileId);

    // 2. MessageOutbox 생성 및 저장 (같은 트랜잭션)
    MessageOutbox outbox = MessageOutbox.create(
        EventType.FILE_UPLOADED,
        fileId,
        buildPayload(file),
        clock
    );
    messageOutboxPersistencePort.save(outbox);

    // 3. 커밋 → 메시지는 스케줄러가 비동기 전송
}
```

#### 2. RelayPendingMessages (Scheduler)
**책임**: PENDING 상태 메시지를 외부 시스템으로 전송

**스케줄링**: 10초마다 실행
```java
@Scheduled(fixedDelay = 10000)
public void relayPendingMessages() {
    List<MessageOutbox> pendingMessages =
        messageOutboxQueryPort.findPendingMessages(LocalDateTime.now());

    for (MessageOutbox outbox : pendingMessages) {
        try {
            // SQS 또는 Webhook 전송
            sendToExternalSystem(outbox);
            outbox.markAsSent(clock);
        } catch (Exception e) {
            outbox.scheduleRetry(clock);  // Exponential backoff
        }
        messageOutboxPersistencePort.update(outbox);
    }
}
```

---

## 🗄️ Persistence Layer

### Flyway Migration

#### V3__create_message_outbox_table.sql
```sql
CREATE TABLE message_outbox (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    outbox_id VARCHAR(36) NOT NULL UNIQUE,
    event_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(36) NOT NULL,
    payload JSON NOT NULL,
    status VARCHAR(20) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    scheduled_at DATETIME(6) NOT NULL,
    sent_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    INDEX idx_status_scheduled (status, scheduled_at),
    INDEX idx_aggregate (aggregate_id),
    INDEX idx_event_type (event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 📊 Integration Points

### session/single 연동
```java
// CompleteUploadService.java (session/single)
@Transactional
public FileResponse execute(CompleteUploadCommand cmd) {
    // 1. File Aggregate 생성 및 저장
    File file = File.create(...);
    filePersistencePort.save(file);

    // 2. MessageOutbox 생성 (같은 트랜잭션)
    MessageOutbox outbox = MessageOutbox.create(
        EventType.FILE_UPLOADED,
        file.fileId(),
        buildFileUploadedPayload(file),
        clock
    );
    messageOutboxPersistencePort.save(outbox);

    // 3. 커밋 → File + MessageOutbox 원자적 저장
    return FileResponse.from(file);
}
```

### SQS Message Format
```json
{
  "eventType": "FILE_UPLOADED",
  "fileId": "01JD8001-1234-5678-9abc-def012345678",
  "fileName": "메인배너.jpg",
  "fileSize": 1048576,
  "mimeType": "image/jpeg",
  "s3Key": "uploads/1/admin/connectly/banner/01JD8001_메인배너.jpg",
  "s3Bucket": "fileflow-uploads-1",
  "uploaderId": 1,
  "uploaderType": "ADMIN",
  "tenantId": 1,
  "timestamp": "2025-11-18T10:30:00Z"
}
```

---

## ✅ Definition of Done

### 기능 요구사항
- [ ] MessageOutbox Aggregate 구현 (PENDING, SENT, FAILED 상태 전환)
- [ ] Transactional Outbox Pattern 구현 (File 저장과 같은 트랜잭션)
- [ ] 스케줄러 기반 메시지 전송 (10초마다)
- [ ] Exponential Backoff 재시도 (최대 3회)
- [ ] SQS 전송 성공률 > 99.9%

### 품질 요구사항
- [ ] Unit Test Coverage > 90%
- [ ] Integration Test (TestContainers + LocalStack SQS)
- [ ] ArchUnit Test 통과 (Long FK 전략, Lombok 금지)

### 성능 요구사항
- [ ] 메시지 전송 지연 < 30초 (P95)
- [ ] 스케줄러 실행 시간 < 5초 (배치 크기 100개)

---

## 🔗 의존성

### Upstream
- `session/single` - File Aggregate 생성 시 이벤트 발행

### Downstream
- SQS (외부 시스템)
- Webhook (외부 시스템)

---

**작성자**: Claude (Anthropic)
**검토자**: ryu-qqq
**변경 이력**:
- 2025-11-18: 초안 작성 (messaging/outbox Bounded Context)
