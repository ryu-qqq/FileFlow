# FileFlow 파일 업로드 → 이미지 리사이징 데이터 흐름 분석

## 📋 목차
1. [개요](#개요)
2. [데이터 흐름도](#데이터-흐름도)
3. [각 단계별 상세 분석](#각-단계별-상세-분석)
4. [테이블 구조](#테이블-구조)
5. [핵심 컴포넌트 역할](#핵심-컴포넌트-역할)

---

## 개요

FileFlow는 **Transactional Outbox 패턴**을 활용하여 파일 업로드 완료 후 이미지 리사이징까지의 데이터 흐름을 관리합니다.

### 핵심 원칙
- **이벤트 기반**: FileUploadCompletedEvent → FileAsset 생성
- **DDD 원칙**: Domain Service에서 Aggregate와 도메인 이벤트 생성
- **이미지만 처리**: 이미지 파일인 경우에만 가공 이벤트 발행
- **트랜잭션 안전성**: Outbox 저장 + 트랜잭션 커밋 + 이벤트 발행

---

## 데이터 흐름도

```
┌─────────────────────────────────────────────────────────────────┐
│ 1️⃣  FileUploadCompletedEvent 발행                              │
│    (파일 업로드 완료 - Session Bounded Context)                 │
└────────────────────┬────────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────────┐
│ 2️⃣  FileAssetCreationFacade.createWithOutbox(event)             │
│    (Application Layer - Transaction 시작)                       │
└────────────────────┬────────────────────────────────────────────┘
                     ↓
        ┌────────────┴────────────┬──────────────┐
        ↓                         ↓              ↓
   ┌─────────────┐        ┌──────────────┐  ┌─────────────┐
   │   Domain    │        │  Domain      │  │   Domain    │
   │   Service   │        │  Service     │  │   Service   │
   │             │        │              │  │             │
   │  생성:      │        │  생성:       │  │  생성:      │
   │ FileAsset   │        │StatusHistory │  │  Outbox +   │
   │             │        │  (초기 상태) │  │DomainEvent  │
   └──────┬──────┘        └──────┬───────┘  └──────┬──────┘
          ↓                      ↓                 ↓
       ┌──────────────────────────────────────────┐
       │ FileAssetCreationResult                  │
       │ - FileAsset (PENDING 상태)              │
       │ - FileAssetStatusHistory (null→PENDING) │
       │ - FileProcessingOutbox (PENDING)       │
       │ - FileProcessingRequestedEvent          │
       └─────────────┬──────────────────────────┘
                     ↓
      ┌──────────────────────────────────────┐
      │ 3️⃣  persistAll() - 3개 Manager 호출  │
      │                                      │
      │ ✅ FileAsset 저장                    │
      │ ✅ StatusHistory 저장                │
      │ ✅ FileProcessingOutbox 저장         │
      │                                      │
      │ (모두 같은 Transaction 내)           │
      └────────────┬───────────────────────┘
                   ↓
      ┌──────────────────────────────────────┐
      │ 4️⃣  이미지 파일 체크                 │
      │                                      │
      │ if (contentType.isImage()) {        │
      │   publishDomainEvent(event)        │
      │ }                                    │
      └────────────┬───────────────────────┘
                   ↓
      ┌──────────────────────────────────────┐
      │ 5️⃣  eventPublisher.publish(event)    │
      │    (Spring ApplicationEventPublisher)│
      │                                      │
      │ FileProcessingRequestedEvent 발행    │
      └────────────┬───────────────────────┘
                   ↓
      ┌──────────────────────────────────────┐
      │ 6️⃣  Transaction COMMIT                │
      │                                      │
      │ ✅ 모든 DB 변경 커밋됨               │
      │ ✅ file_asset, file_asset_status_*  │
      │ ✅ file_processing_outbox 저장됨    │
      └────────────┬───────────────────────┘
                   ↓
      ┌──────────────────────────────────────┐
      │ 7️⃣  FileProcessingOutboxEventListener│
      │    @TransactionalEventListener       │
      │    (AFTER_COMMIT)                   │
      │                                      │
      │ - SQS 메시지 생성                    │
      │ - SQS 발행                          │
      │ - Outbox 상태 업데이트 (SENT/FAILED)│
      └────────────┬───────────────────────┘
                   ↓
      ┌──────────────────────────────────────┐
      │ 8️⃣  SQS 메시지 수신                  │
      │    (이미지 처리 Worker)              │
      │                                      │
      │ - 이미지 리사이징 처리               │
      │ - ProcessedFileAsset 생성 및 저장    │
      │ - FileAsset 상태 업데이트            │
      │   (PENDING → COMPLETED)              │
      └────────────┬───────────────────────┘
                   ↓
         ┌────────────────────┐
         │ ✅ 완료            │
         │ FileAsset (상태:   │
         │ COMPLETED)        │
         │ +                 │
         │ ProcessedFileAsset│
         │ (리사이징 결과)    │
         └────────────────────┘
```

---

## 각 단계별 상세 분석

### Stage 1️⃣: FileAsetCreationFacade.createWithOutbox()

**메서드 시그니처**
```java
@Transactional
public FileAssetId createWithOutbox(FileUploadCompletedEvent event)
```

**실행 단계:**

#### 1-1) Domain Service 호출
```java
FileAssetCreationResult result = fileAssetCreationService.createFromUploadEvent(event);
```

**Domain Service 내부:**
```
a) Event → FileAsset 변환
   - FileAssetId 생성 (UUID)
   - FileCategory 결정 (MIME type 기반)
   - 상태: PENDING

b) FileAssetStatusHistory 생성
   - fromStatus: null (최초 생성)
   - toStatus: PENDING
   - message: "FileAsset 생성됨"
   - actor: "system"

c) FileProcessingOutbox 생성
   - eventType: "PROCESS_REQUEST"
   - payload: "{\"fileAssetId\":\"<id>\"}"
   - status: PENDING

d) FileProcessingRequestedEvent 생성
   - 도메인 이벤트 (DomainEvent 인터페이스 구현)
   - Domain Service에서 생성 (Facade가 아님)
```

#### 1-2) 영속화 (persistAll)
```java
FileAssetId savedId = persistAll(result);
```

**persistAll() 구현:**
```java
private FileAssetId persistAll(FileAssetCreationResult result) {
    // 1. FileAsset 저장
    FileAssetId savedId = fileAssetManager.save(result.fileAsset());
    
    // 2. StatusHistory 저장
    statusHistoryManager.save(result.statusHistory());
    
    // 3. Outbox 저장
    FileProcessingOutboxId outboxId = outboxManager.save(result.outbox());
    
    return savedId;
}
```

**각 Manager의 역할:**
- FileAssetManager: FileAsset 저장
- FileAssetStatusHistoryManager: StatusHistory 저장
- FileProcessingOutboxManager: Outbox 저장

#### 1-3) 도메인 이벤트 발행 (이미지만)
```java
private void publishDomainEvent(FileAssetCreationResult result) {
    if (!result.fileAsset().getContentType().isImage()) {
        // 이미지가 아니면 발행하지 않음
        return;
    }
    eventPublisher.publish(result.domainEvent());
}
```

**이미지 체크:**
- contentType.isImage() 메서드로 MIME type 확인
- image/* 타입만 처리
- 엑셀, 문서 등 다른 파일은 발행하지 않음

---

### Stage 2️⃣: Transaction Commit 및 Event Listener 실행

**Flow:**
```
Domain Service 완료
  ↓
3개 Manager를 통해 DB 저장
  ↓
eventPublisher.publish(event) - Spring Event 발행
  ↓
Transaction 커밋 (모든 DB 변경 사항 커밋)
  ↓
@TransactionalEventListener(phase = AFTER_COMMIT) 트리거
  ↓
FileProcessingOutboxEventListener.handle(event)
```

### FileProcessingOutboxEventListener

**핵심 로직:**
```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void handle(FileProcessingRequestedEvent event) {
    try {
        // 1. SQS 메시지 생성
        FileProcessingMessage message = FileProcessingMessage.of(
            event.fileAssetId().getValue(),
            event.outboxId().getValue(),
            event.eventType()
        );
        
        // 2. SQS 발행
        boolean published = sqsPublishPort.publish(message);
        
        if (published) {
            handleSuccess(event);    // Outbox → SENT
        } else {
            handleFailure(event);    // Outbox → FAILED
        }
    } catch (Exception e) {
        handleFailure(event, e.getMessage());
    }
}
```

**성공 시 처리:**
```java
private void handleSuccess(FileProcessingRequestedEvent event) {
    FileProcessingOutbox outbox = outboxQueryPort
        .findById(event.outboxId())
        .orElseThrow(...);
    
    outbox.markAsSent();           // status: PENDING → SENT
    outboxManager.save(outbox);    // DB 업데이트
}
```

**실패 시 처리:**
```java
private void handleFailure(FileProcessingRequestedEvent event, String errorMessage) {
    FileProcessingOutbox outbox = outboxQueryPort
        .findById(event.outboxId())
        .orElseThrow(...);
    
    outbox.markAsFailed(errorMessage);  // status: FAILED, retryCount++
    outboxManager.save(outbox);         // DB 업데이트
}
```

---

## 테이블 구조

### 1️⃣ file_asset 테이블

```sql
CREATE TABLE file_asset (
    id VARCHAR(36) NOT NULL PRIMARY KEY,              -- FileAsset ID (UUID)
    session_id VARCHAR(36) NOT NULL,                  -- Upload Session ID
    file_name VARCHAR(255) NOT NULL,                  -- 원본 파일명
    file_size BIGINT NOT NULL,                        -- 파일 크기 (바이트)
    content_type VARCHAR(100) NOT NULL,               -- MIME type (image/jpeg 등)
    category VARCHAR(50) NOT NULL,                    -- 파일 카테고리 (IMAGE, DOCUMENT 등)
    bucket VARCHAR(63) NOT NULL,                      -- S3 버킷명
    s3_key VARCHAR(1024) NOT NULL,                    -- S3 객체 키
    etag VARCHAR(64) NOT NULL,                        -- S3 ETag
    user_id BIGINT NULL,                              -- 업로드한 사용자 ID
    organization_id BIGINT NOT NULL,                  -- 조직 ID
    tenant_id BIGINT NOT NULL,                        -- 테넌트 ID
    status VARCHAR(20) NOT NULL,                      -- 상태 (PENDING, PROCESSING, COMPLETED, etc)
    processed_at TIMESTAMP NULL,                      -- 처리 완료 시각
    deleted_at TIMESTAMP NULL,                        -- 삭제 시각 (Soft Delete)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes
    INDEX idx_file_asset_session_id (session_id),
    INDEX idx_file_asset_organization_id (organization_id),
    INDEX idx_file_asset_tenant_id (tenant_id),
    INDEX idx_file_asset_status (status),
    INDEX idx_file_asset_created_at (created_at),
    INDEX idx_file_asset_org_tenant_created (organization_id, tenant_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**상태 변화:**
```
생성 시점: PENDING (Outbox에서 즉시 처리됨)
   ↓
SQS 메시지 수신: PROCESSING (이미지 리사이징 중)
   ↓
리사이징 완료: COMPLETED 또는 RESIZED
   ↓
실패 시: FAILED
```

---

### 2️⃣ file_asset_status_history 테이블

```sql
CREATE TABLE file_asset_status_history (
    id VARCHAR(36) NOT NULL PRIMARY KEY,              -- History ID (UUID)
    file_asset_id VARCHAR(36) NOT NULL,               -- FileAsset ID (외래키)
    from_status VARCHAR(20) NULL,                     -- 이전 상태 (최초 생성 시 NULL)
    to_status VARCHAR(20) NOT NULL,                   -- 새 상태
    message VARCHAR(500) NULL,                        -- 상태 메시지
    actor VARCHAR(100) NULL,                          -- 변경한 주체 (system, n8n, user 등)
    actor_type VARCHAR(50) NULL,                      -- 주체 타입 (SYSTEM, N8N, USER 등)
    duration_millis BIGINT NULL,                      -- 이전 상태의 지속 시간 (ms)
    changed_at TIMESTAMP NOT NULL,                    -- 상태 변경 시각
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes
    INDEX idx_file_asset_status_history_file_asset_id (file_asset_id),
    INDEX idx_file_asset_status_history_to_status (to_status),
    INDEX idx_file_asset_status_history_changed_at (changed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**최초 생성 시 레코드:**
```
file_asset_id: "abc123..."
from_status: NULL           -- 최초 생성
to_status: PENDING
message: "FileAsset 생성됨"
actor: "system"
actor_type: "SYSTEM"
duration_millis: NULL
```

---

### 3️⃣ file_processing_outbox 테이블

```sql
CREATE TABLE file_processing_outbox (
    id BINARY(16) NOT NULL PRIMARY KEY,               -- Outbox ID (UUID v7)
    file_asset_id VARCHAR(36) NOT NULL,               -- FileAsset ID (외래키)
    event_type VARCHAR(50) NOT NULL,                  -- 이벤트 타입 (PROCESS_REQUEST 등)
    payload TEXT NOT NULL,                            -- 이벤트 페이로드 (JSON)
    status VARCHAR(20) NOT NULL,                      -- Outbox 상태 (PENDING, SENT, FAILED)
    retry_count INT NOT NULL DEFAULT 0,               -- 재시도 횟수
    error_message VARCHAR(500) NULL,                  -- SQS 발행 실패 이유
    processed_at TIMESTAMP NULL,                      -- 처리 완료 시각 (SENT/FAILED 전환 시)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes
    INDEX idx_file_processing_outbox_file_asset_id (file_asset_id),
    INDEX idx_file_processing_outbox_status (status),
    INDEX idx_file_processing_outbox_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Transactional Outbox 패턴:**
```
상태 변화:
PENDING → SENT      (SQS 발행 성공)
PENDING → FAILED    (SQS 발행 실패 → 재시도)
FAILED → SENT       (재시도 성공)

재시도 정책:
- MAX_RETRY_COUNT = 3회
- canRetry() 조건: status != SENT && retryCount < 3
```

**최초 저장 레코드 예시:**
```
id: "8e6c3c8a-..."     (UUID v7)
file_asset_id: "abc123..."
event_type: "PROCESS_REQUEST"
payload: "{\"fileAssetId\":\"abc123...\"}"
status: PENDING
retry_count: 0
error_message: NULL
processed_at: NULL
created_at: 2024-12-03 10:30:45.123456
updated_at: 2024-12-03 10:30:45.123456
```

---

### 4️⃣ processed_file_asset 테이블 (리사이징 결과)

```sql
CREATE TABLE processed_file_asset (
    id VARCHAR(36) NOT NULL PRIMARY KEY,              -- ProcessedFileAsset ID (UUID)
    original_asset_id VARCHAR(36) NOT NULL,           -- 원본 FileAsset ID (외래키)
    parent_asset_id VARCHAR(36) NULL,                 -- 부모 FileAsset ID (HTML 추출 이미지용)
    image_variant VARCHAR(50) NOT NULL,               -- 이미지 크기 (ORIGINAL, THUMB_500 등)
    image_format VARCHAR(50) NOT NULL,                -- 이미지 포맷 (JPEG, WEBP 등)
    file_name VARCHAR(255) NOT NULL,                  -- 리사이징된 파일명
    file_size BIGINT NOT NULL,                        -- 리사이징된 파일 크기
    width INT NOT NULL,                               -- 이미지 너비 (픽셀)
    height INT NOT NULL,                              -- 이미지 높이 (픽셀)
    bucket VARCHAR(63) NOT NULL,                      -- S3 버킷명
    s3_key VARCHAR(1024) NOT NULL,                    -- S3 객체 키
    user_id BIGINT NULL,                              -- 사용자 ID
    organization_id BIGINT NOT NULL,                  -- 조직 ID
    tenant_id BIGINT NOT NULL,                        -- 테넌트 ID
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Indexes
    INDEX idx_processed_file_asset_original_asset_id (original_asset_id),
    INDEX idx_processed_file_asset_parent_asset_id (parent_asset_id),
    INDEX idx_processed_file_asset_organization_id (organization_id),
    INDEX idx_processed_file_asset_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**리사이징 완료 후 저장 예시:**
```
id: "xyz789..."
original_asset_id: "abc123..."           -- 원본 FileAsset 참조
parent_asset_id: NULL                    -- 직접 업로드된 이미지 (HTML 추출 아님)
image_variant: "ORIGINAL"
image_format: "WEBP"
file_name: "image-original.webp"
file_size: 512000
width: 2000
height: 1500
bucket: "fileflow-bucket"
s3_key: "processed/original/image-original.webp"
```

**여러 variant 저장 예시:**
```
-- ORIGINAL (WebP)
id: "xyz789-original-webp"
image_variant: "ORIGINAL"
image_format: "WEBP"
file_size: 512000
width: 2000, height: 1500

-- THUMBNAIL_500 (WebP)
id: "xyz789-thumb-webp"
image_variant: "THUMB_500"
image_format: "WEBP"
file_size: 45000
width: 500, height: 375

-- THUMBNAIL_500 (JPEG 폴백)
id: "xyz789-thumb-jpeg"
image_variant: "THUMB_500"
image_format: "JPEG"
file_size: 52000
width: 500, height: 375
```

---

## 핵심 컴포넌트 역할

### 1️⃣ Domain Layer

#### FileAsset (Aggregate Root)
```java
public class FileAsset {
    private FileAssetId id;
    private FileName fileName;
    private FileSize fileSize;
    private ContentType contentType;
    private FileCategory category;
    private S3Bucket bucket;
    private S3Key s3Key;
    private FileAssetStatus status;        // 상태 관리
    private LocalDateTime processedAt;
}
```

**상태:**
- PENDING: 생성되어 가공 대기 중
- PROCESSING: 이미지 리사이징 중
- COMPLETED: 리사이징 완료
- FAILED: 실패
- RESIZED: 리사이징 완료 (특정 상태)
- DELETED: 삭제 (Soft Delete)

#### FileProcessingOutbox (Aggregate)
```java
public class FileProcessingOutbox {
    private FileProcessingOutboxId id;
    private FileAssetId fileAssetId;
    private String eventType;              // PROCESS_REQUEST, STATUS_CHANGE, RETRY_REQUEST
    private String payload;                // JSON 페이로드
    private OutboxStatus status;           // PENDING, SENT, FAILED
    private int retryCount;                // 재시도 횟수
}
```

**역할:** Transactional Outbox 패턴 구현
- 메시지 저장과 발행의 일관성 보장
- SQS 발행 실패 시 재시도 매커니즘 제공

#### FileAssetStatusHistory (Aggregate)
```java
public class FileAssetStatusHistory {
    private FileAssetStatusHistoryId id;
    private FileAssetId fileAssetId;
    private FileAssetStatus fromStatus;    // 이전 상태
    private FileAssetStatus toStatus;      // 새 상태
    private String message;
    private String actor;                  // system, n8n, user
    private LocalDateTime changedAt;
    private Long durationMillis;           // SLA 모니터링
}
```

**역할:** 상태 변경 추적
- 누가, 언제, 왜 상태를 변경했는지 기록
- SLA 모니터링 (각 단계별 소요 시간)

#### ProcessedFileAsset (Aggregate)
```java
public class ProcessedFileAsset {
    private ProcessedFileAssetId id;
    private FileAssetId originalAssetId;   // 원본 이미지 참조
    private FileAssetId parentAssetId;     // HTML 추출 이미지용 부모 참조
    private ImageVariant variant;          // ORIGINAL, THUMB_500 등
    private ImageFormat format;            // JPEG, WEBP 등
    private Integer width;
    private Integer height;
}
```

**역할:** 리사이징된 이미지 메타데이터 저장

---

### 2️⃣ Application Layer

#### FileAssetCreationFacade
```java
@Transactional
public FileAssetId createWithOutbox(FileUploadCompletedEvent event) {
    // 1. Domain Service에서 Aggregate 생성
    FileAssetCreationResult result = fileAssetCreationService.createFromUploadEvent(event);
    
    // 2. 3개 Manager를 통해 영속화
    FileAssetId savedId = persistAll(result);
    
    // 3. 도메인 이벤트 발행 (이미지만)
    publishDomainEvent(result);
    
    return savedId;
}
```

**책임:**
- Domain Service 호출
- 3개 Manager를 통한 영속화 조율
- 도메인 이벤트 발행

#### FileProcessingOutboxEventListener
```java
@TransactionalEventListener(phase = AFTER_COMMIT)
@Transactional(propagation = REQUIRES_NEW)
public void handle(FileProcessingRequestedEvent event) {
    // 1. SQS 발행
    boolean published = sqsPublishPort.publish(message);
    
    // 2. Outbox 상태 업데이트 (SENT 또는 FAILED)
    if (published) {
        outbox.markAsSent();
    } else {
        outbox.markAsFailed(errorMessage);
    }
}
```

**책임:**
- Transaction AFTER_COMMIT에서 SQS 발행
- Outbox 상태 추적

---

### 3️⃣ Persistence Layer

#### Mapper들
- `FileAssetJpaEntityMapper`: Domain ↔ JPA Entity 변환
- `FileProcessingOutboxJpaMapper`: Domain ↔ JPA Entity 변환
- `FileAssetStatusHistoryMapper`: Domain ↔ JPA Entity 변환

#### Manager들 (Application Layer)
- `FileAssetManager`: fileAssetPersistencePort 호출
- `FileProcessingOutboxManager`: outboxPersistencePort 호출
- `FileAssetStatusHistoryManager`: statusHistoryPersistencePort 호출

---

## 데이터 저장 순서 (동일 Transaction 내)

```
1️⃣ FileAsset 저장
   file_asset 테이블 INSERT
   
   예시:
   id: "abc123..."
   session_id: "sess-abc..."
   file_name: "image.jpg"
   status: PENDING
   ...

2️⃣ FileAssetStatusHistory 저장
   file_asset_status_history 테이블 INSERT
   
   예시:
   id: "hist-123..."
   file_asset_id: "abc123..."
   from_status: NULL
   to_status: PENDING
   message: "FileAsset 생성됨"
   actor: "system"

3️⃣ FileProcessingOutbox 저장
   file_processing_outbox 테이블 INSERT
   
   예시:
   id: "outbox-123..."
   file_asset_id: "abc123..."
   event_type: "PROCESS_REQUEST"
   payload: "{\"fileAssetId\":\"abc123...\"}"
   status: PENDING
   retry_count: 0

✅ Transaction COMMIT

4️⃣ Spring Event 발행 (Listener 트리거)
   FileProcessingRequestedEvent

5️⃣ @TransactionalEventListener 실행
   SQS 발행 + Outbox 상태 업데이트
```

---

## 이미지 필터링 로직

```java
private void publishDomainEvent(FileAssetCreationResult result) {
    if (!result.fileAsset().getContentType().isImage()) {
        log.info("이미지가 아니므로 가공 이벤트 발행 생략");
        return;
    }
    eventPublisher.publish(result.domainEvent());
}
```

### ContentType.isImage() 메서드

```java
public class ContentType {
    private final String type;  // "image/jpeg", "image/png" 등
    
    public boolean isImage() {
        return type.startsWith("image/");
    }
}
```

**지원되는 이미지 타입:**
- image/jpeg
- image/png
- image/webp
- image/gif
- image/bmp
- 기타 image/* 타입

---

## 요약 표

| 컴포넌트 | 계층 | 책임 | 생성 시점 |
|--------|------|------|----------|
| FileAsset | Domain | 파일 메타데이터 + 상태 관리 | Domain Service |
| FileAssetStatusHistory | Domain | 상태 변경 추적 | Domain Service |
| FileProcessingOutbox | Domain | Outbox 패턴 구현 | Domain Service |
| FileProcessingRequestedEvent | Domain | 도메인 이벤트 | Domain Service |
| FileAssetCreationFacade | Application | 3개 Manager 조율 + 이벤트 발행 | Application Layer |
| FileProcessingOutboxEventListener | Application | SQS 발행 + Outbox 상태 업데이트 | Event Listener |
| ProcessedFileAsset | Domain | 리사이징된 이미지 메타데이터 | SQS Worker |

---

## 주요 설계 원칙

### 1️⃣ DDD 원칙
- Domain Service에서 Aggregate와 도메인 이벤트 생성
- Application Layer(Facade)는 이벤트를 발행만 할 뿐, 생성하지 않음

### 2️⃣ Transactional Outbox 패턴
- Outbox 저장과 도메인 이벤트 발행을 동일 Transaction 내에서 처리
- Transaction AFTER_COMMIT에서 SQS 메시지 발행
- SQS 발행 실패 시 재시도 매커니즘 제공

### 3️⃣ 이미지만 처리
- ContentType.isImage() 체크로 이미지만 필터링
- 다른 파일 타입은 Outbox에 저장되지 않음

### 4️⃣ 트랜잭션 전파
- FileProcessingOutboxEventListener: REQUIRES_NEW
- SQS 발행 실패해도 Outbox 상태는 FAILED로 기록
- 재시도 스케줄러에서 처리

