# PRD: External Download (외부 링크 다운로드)

**작성일**: 2025-11-26
**상태**: Draft

---

## 📋 프로젝트 개요

### 비즈니스 목적
서버-투-서버 통신 시 외부 이미지 URL을 받아서 FileFlow 서버에서 다운로드 후 S3에 업로드하는 기능.

**현재**: Presigned URL 발급 → 프론트엔드 직접 업로드 (클라이언트 업로드)
**필요**: 외부 URL → FileFlow 다운로드 → S3 업로드 (서버 업로드)

### 사용 케이스
- 상품 등록 API: 외부 쇼핑몰 상품 이미지 URL
- 리뷰 API: 외부 리뷰 이미지 URL
- 기타 Server-to-Server 연동

### 성공 기준
- 비동기 처리로 API 응답 시간 영향 없음
- 실패 시 2회 재시도 후 디폴트 이미지 적용
- 다중 Worker 환경에서 안정적인 분산 처리

---

## 🏗️ 아키텍처 개요

```
┌─────────────────────────────────────────────────────────────────────┐
│                         API Request Flow                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  Client API Request                                                  │
│       │                                                              │
│       ▼                                                              │
│  ┌─────────────────┐                                                │
│  │ REST Controller │                                                │
│  └────────┬────────┘                                                │
│           │                                                          │
│           ▼                                                          │
│  ┌─────────────────────────────────────────┐                        │
│  │         Application Layer               │                        │
│  │  ┌─────────────────────────────────┐   │                        │
│  │  │ RequestExternalDownloadUseCase  │   │                        │
│  │  │                                 │   │                        │
│  │  │ 1. ExternalDownload 생성        │   │                        │
│  │  │ 2. Outbox 생성                  │   │                        │
│  │  │ 3. ID 즉시 반환                 │   │                        │
│  │  └─────────────────────────────────┘   │                        │
│  └─────────────────────────────────────────┘                        │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                      Async Processing Flow                           │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌──────────────────┐      ┌─────────┐      ┌──────────────────┐   │
│  │ Outbox Scheduler │ ──▶  │   SQS   │ ──▶  │  Worker (N대)    │   │
│  │ (1분 주기 폴링)   │      │  Queue  │      │                  │   │
│  └──────────────────┘      └─────────┘      └────────┬─────────┘   │
│                                                       │              │
│                                              ┌────────▼─────────┐   │
│                                              │ ProcessExternal  │   │
│                                              │ DownloadUseCase  │   │
│                                              │                  │   │
│                                              │ 1. 분산락 획득    │   │
│                                              │ 2. 외부 URL 다운로드│  │
│                                              │ 3. S3 업로드      │   │
│                                              │ 4. FileAsset 생성 │   │
│                                              │ 5. 상태 업데이트   │   │
│                                              │ 6. [선택] Webhook │   │
│                                              └──────────────────┘   │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### 상태 흐름

```
PENDING ──────▶ PROCESSING ──────▶ COMPLETED
                    │                   │
                    │                   ▼
                    │              FileAsset 생성
                    │
                    ▼ (2회 재시도 실패)
                 FAILED
                    │
                    ▼
              디폴트 이미지 적용
```

---

## 🏗️ Layer별 요구사항

### 1. Domain Layer

#### 1.1 ExternalDownload Aggregate

**책임**: 외부 URL 다운로드 요청의 생명주기 관리

**속성**:
```java
ExternalDownload {
    // Identity (Value Object - 컨벤션 준수)
    id: ExternalDownloadId (VO, PK)

    // 다운로드 정보
    sourceUrl: SourceUrl (VO - 외부 이미지 URL)

    // 멀티테넌트 (Value Object)
    tenantId: TenantId (VO)
    organizationId: OrganizationId (VO)

    // 상태 관리
    status: ExternalDownloadStatus (PENDING/PROCESSING/COMPLETED/FAILED)
    retryCount: int (default: 0, max: 2)

    // 결과
    fileAssetId: FileAssetId (VO, nullable - 성공 시 생성된 FileAsset ID)
    errorMessage: String (nullable - 실패 시 에러 메시지)

    // Webhook (선택)
    webhookUrl: WebhookUrl (VO, nullable)

    // Audit
    createdAt: Instant
    updatedAt: Instant
}
```

**Factory Methods (컨벤션 준수)**:
```java
// 1. forNew(): 새 Aggregate 생성 (ID null, 검증 완료된 VO 전달)
public static ExternalDownload forNew(
    SourceUrl sourceUrl,
    TenantId tenantId,
    OrganizationId organizationId,
    WebhookUrl webhookUrl,  // nullable
    Clock clock
)

// 2. of(): 조회용 (ID 필수)
public static ExternalDownload of(
    ExternalDownloadId id,
    SourceUrl sourceUrl,
    TenantId tenantId,
    OrganizationId organizationId,
    ExternalDownloadStatus status,
    int retryCount,
    FileAssetId fileAssetId,  // nullable
    String errorMessage,       // nullable
    WebhookUrl webhookUrl,     // nullable
    Instant createdAt,
    Instant updatedAt
)

// 3. reconstitute(): Mapper 전용 (private 생성자 직접 호출과 동일)
// → of()와 동일, 용도 구분을 위해 별도 정의 권장
```

**생성자 (private)**:
```java
private ExternalDownload(
    ExternalDownloadId id,
    SourceUrl sourceUrl,
    TenantId tenantId,
    OrganizationId organizationId,
    ExternalDownloadStatus status,
    int retryCount,
    FileAssetId fileAssetId,
    String errorMessage,
    WebhookUrl webhookUrl,
    Instant createdAt,
    Instant updatedAt
) {
    // 필수 필드 검증
    Objects.requireNonNull(sourceUrl, "sourceUrl must not be null");
    Objects.requireNonNull(tenantId, "tenantId must not be null");
    Objects.requireNonNull(organizationId, "organizationId must not be null");
    Objects.requireNonNull(status, "status must not be null");

    this.id = id;
    this.sourceUrl = sourceUrl;
    this.tenantId = tenantId;
    this.organizationId = organizationId;
    this.status = status;
    this.retryCount = retryCount;
    this.fileAssetId = fileAssetId;
    this.errorMessage = errorMessage;
    this.webhookUrl = webhookUrl;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
}
```

**비즈니스 규칙**:
1. **상태 전환 규칙**:
   - `PENDING` → `PROCESSING`: Worker가 처리 시작
   - `PROCESSING` → `COMPLETED`: 다운로드 + 업로드 성공
   - `PROCESSING` → `FAILED`: 2회 재시도 후 최종 실패
   - `PROCESSING` → `PENDING`: 재시도 (retryCount < 2)

2. **재시도 규칙**:
   - 최대 재시도 횟수: 2회
   - 재시도 시 `retryCount` 증가
   - 2회 초과 시 `FAILED` 상태로 전환

3. **FileAsset 연결**:
   - `COMPLETED` 상태에서만 `fileAssetId` 설정 가능
   - `FAILED` 상태에서는 디폴트 이미지의 `fileAssetId` 설정

**Value Objects (Record + Compact Constructor 컨벤션 준수)**:

```java
// ExternalDownloadId - ID Value Object
public record ExternalDownloadId(Long value) {
    // Compact Constructor (null 검증)
    public ExternalDownloadId {
        // forNew()로 생성 시 null 허용
    }

    // 새 생성용 (ID 미할당)
    public static ExternalDownloadId forNew() {
        return new ExternalDownloadId(null);
    }

    // 조회/재구성용 (ID 필수)
    public static ExternalDownloadId of(Long value) {
        Objects.requireNonNull(value, "ExternalDownloadId value must not be null");
        return new ExternalDownloadId(value);
    }

    // 신규 여부 확인
    public boolean isNew() {
        return value == null;
    }
}

// SourceUrl - 외부 이미지 URL (Record)
public record SourceUrl(String value) {
    // Compact Constructor (검증)
    public SourceUrl {
        Objects.requireNonNull(value, "SourceUrl must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("SourceUrl must not be blank");
        }
        // HTTP/HTTPS URL 형식 검증
        if (!value.matches("^https?://.*")) {
            throw new IllegalArgumentException("SourceUrl must start with http:// or https://");
        }
        // 이미지 확장자 검증 (jpg, jpeg, png, gif, webp, bmp, svg 등)
        // 확장자가 없는 URL도 허용 (Content-Type으로 검증)
    }

    public static SourceUrl of(String value) {
        return new SourceUrl(value);
    }
}

// WebhookUrl - 콜백 URL (Record, nullable)
public record WebhookUrl(String value) {
    // Compact Constructor (검증)
    public WebhookUrl {
        Objects.requireNonNull(value, "WebhookUrl must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("WebhookUrl must not be blank");
        }
        // HTTP/HTTPS URL 형식 검증
        if (!value.matches("^https?://.*")) {
            throw new IllegalArgumentException("WebhookUrl must start with http:// or https://");
        }
    }

    public static WebhookUrl of(String value) {
        return new WebhookUrl(value);
    }
}

// ExternalDownloadStatus - 상태 Enum
public enum ExternalDownloadStatus {
    PENDING,      // 요청됨, 처리 대기 중
    PROCESSING,   // Worker가 처리 중
    COMPLETED,    // 성공 완료
    FAILED        // 최종 실패 (2회 재시도 후)
}
```

**도메인 메서드**:
```java
// 처리 시작 (Clock 주입 - 컨벤션 준수)
startProcessing(Clock clock): void
    - PENDING → PROCESSING
    - updatedAt = Instant.now(clock)
    - 다른 상태에서 호출 시 예외

// 처리 완료 (성공)
complete(FileAssetId fileAssetId, Clock clock): void
    - PROCESSING → COMPLETED
    - fileAssetId 설정
    - updatedAt = Instant.now(clock)

// 재시도
retry(Clock clock): void
    - PROCESSING → PENDING
    - retryCount 증가
    - updatedAt = Instant.now(clock)
    - retryCount >= 2 이면 예외 (fail() 호출해야 함)

// 최종 실패
fail(String errorMessage, FileAssetId defaultFileAssetId, Clock clock): void
    - PROCESSING → FAILED
    - errorMessage 설정
    - 디폴트 이미지 fileAssetId 설정
    - updatedAt = Instant.now(clock)

// 재시도 가능 여부
canRetry(): boolean
    - retryCount < 2

// Webhook URL 존재 여부
hasWebhook(): boolean
    - webhookUrl != null

// ID 조회 (컨벤션: getIdValue() 제공)
getIdValue(): Long
    - return id.value()
```

#### 1.2 ExternalDownloadOutbox Aggregate

**책임**: SQS 발행 실패 대비 Outbox 패턴

**속성**:
```java
ExternalDownloadOutbox {
    // Identity (Value Object - 컨벤션 준수)
    id: ExternalDownloadOutboxId (VO, PK)
    externalDownloadId: ExternalDownloadId (VO, FK)

    // 발행 상태
    published: boolean (default: false)
    publishedAt: Instant (nullable)

    // Audit
    createdAt: Instant
}
```

**Factory Methods (컨벤션 준수)**:
```java
// forNew(): 새 Outbox 생성
public static ExternalDownloadOutbox forNew(
    ExternalDownloadId externalDownloadId,
    Clock clock
)

// of(): 조회/재구성용
public static ExternalDownloadOutbox of(
    ExternalDownloadOutboxId id,
    ExternalDownloadId externalDownloadId,
    boolean published,
    Instant publishedAt,
    Instant createdAt
)
```

**도메인 메서드**:
```java
// 발행 완료 표시
markAsPublished(Clock clock): void
    - published = true
    - publishedAt = Instant.now(clock)
```

**비즈니스 규칙**:
1. ExternalDownload 생성 시 Outbox도 함께 생성 (같은 트랜잭션)
2. SQS 발행 성공 시 `markAsPublished()` 호출
3. Outbox Scheduler가 `published = false`인 레코드 주기적 처리

---

### 2. Application Layer

#### 2.1 Command UseCase

**RequestExternalDownloadUseCase** (API 요청 처리):
```java
Input: RequestExternalDownloadCommand {
    sourceUrl: String
    tenantId: Long
    organizationId: Long
    webhookUrl: String (nullable)
}

Output: ExternalDownloadResponse {
    id: Long
    status: String
    createdAt: Instant
}

Flow:
1. SourceUrl VO 생성 (URL 검증)
2. WebhookUrl VO 생성 (있으면)
3. ExternalDownload Aggregate 생성 (PENDING)
4. ExternalDownloadOutbox 생성
5. [트랜잭션 커밋]
6. SQS 메시지 발행 시도 (트랜잭션 밖)
7. 성공 시 Outbox.published = true
8. ExternalDownload ID 반환

Transaction:
- 1-5: @Transactional
- 6-7: 트랜잭션 밖 (SQS 발행 실패해도 롤백 안됨)
```

**ProcessExternalDownloadUseCase** (Worker 처리):
```java
Input: ProcessExternalDownloadCommand {
    externalDownloadId: Long
}

Output: void (또는 ProcessingResult)

Flow:
1. Redis 분산락 획득 (key: external-download:{id})
2. ExternalDownload 조회
3. 상태 확인 (PENDING만 처리)
4. startProcessing() 호출 → PROCESSING
5. [트랜잭션 커밋 - 상태 변경]
6. 외부 URL 다운로드 (HTTP Client) - 트랜잭션 밖
7. S3 업로드 - 트랜잭션 밖
8. FileAsset 생성 - 새 트랜잭션
9. ExternalDownload.complete(fileAssetId) - 새 트랜잭션
10. [선택] Webhook 호출 - 트랜잭션 밖
11. 분산락 해제

Exception Handling:
- 다운로드/업로드 실패 시:
  - canRetry() == true → retry() + SQS 재발행
  - canRetry() == false → fail(errorMessage, defaultFileAssetId)

Transaction:
- 4-5: @Transactional (상태 변경)
- 6-7: 트랜잭션 밖 (외부 I/O)
- 8-9: @Transactional (결과 저장)
- 10: 트랜잭션 밖 (Webhook)
```

#### 2.2 Query UseCase

**GetExternalDownloadUseCase**:
```java
Input: GetExternalDownloadQuery {
    id: Long
    tenantId: Long (권한 체크)
}

Output: ExternalDownloadDetailResponse {
    id: Long
    sourceUrl: String
    status: String
    fileAssetId: Long (nullable)
    errorMessage: String (nullable)
    retryCount: int
    createdAt: Instant
    updatedAt: Instant
}
```

#### 2.3 Scheduler

**OutboxPublishScheduler**:
```java
@Scheduled(fixedRate = 60000) // 1분 주기
publishPendingOutbox():
    1. published = false인 Outbox 조회 (limit 100)
    2. 각 Outbox에 대해:
       - SQS 메시지 발행
       - 성공 시 published = true
       - 실패 시 로그 (다음 주기에 재시도)
```

#### 2.4 Port 정의 (컨벤션 준수)

**Outbound Ports - Persistence (컨벤션: persist() 단일 메서드, VO 파라미터/반환)**:
```java
// Command Port - persist() 단일 메서드만 제공
ExternalDownloadPersistencePort {
    ExternalDownloadId persist(ExternalDownload externalDownload);
}

// Query Port - VO 파라미터, Domain 반환, Optional 사용
ExternalDownloadQueryPort {
    Optional<ExternalDownload> findById(ExternalDownloadId id);
    Optional<ExternalDownload> findByIdAndTenantId(ExternalDownloadId id, TenantId tenantId);
    boolean existsById(ExternalDownloadId id);
}

// Outbox Command Port
ExternalDownloadOutboxPersistencePort {
    ExternalDownloadOutboxId persist(ExternalDownloadOutbox outbox);
}

// Outbox Query Port
ExternalDownloadOutboxQueryPort {
    List<ExternalDownloadOutbox> findUnpublished(int limit);
}
```

**Outbound Ports - External Services**:
```java
// HTTP 다운로드
HttpDownloadPort {
    DownloadResult download(SourceUrl sourceUrl);
}

// DownloadResult (Application DTO)
public record DownloadResult(
    byte[] content,
    String contentType,
    long contentLength
) {}

// SQS 발행
SqsPublishPort {
    boolean publish(ExternalDownloadMessage message);
}

// Webhook 호출
WebhookPort {
    void call(WebhookUrl webhookUrl, WebhookPayload payload);
}

// 분산락
DistributedLockPort {
    boolean tryLock(String key, Duration timeout);
    void unlock(String key);
}
```

---

### 3. Persistence Layer (컨벤션 준수)

#### 3.1 패키지 구조 (CQRS 분리)

```
persistence-mysql/
└─ download/
   ├─ adapter/
   │  ├─ ExternalDownloadCommandAdapter.java    # Command - JPA 저장
   │  └─ ExternalDownloadQueryAdapter.java      # Query - QueryDSL 조회
   ├─ entity/
   │  ├─ ExternalDownloadJpaEntity.java
   │  └─ ExternalDownloadOutboxJpaEntity.java
   ├─ mapper/
   │  └─ ExternalDownloadJpaEntityMapper.java   # @Component, 순수 Java
   └─ repository/
      ├─ ExternalDownloadJpaRepository.java     # Command용 JPA
      ├─ ExternalDownloadQueryDslRepository.java # Query용 QueryDSL
      ├─ ExternalDownloadOutboxJpaRepository.java
      └─ ExternalDownloadOutboxQueryDslRepository.java
```

#### 3.2 JPA Entity (Lombok 금지)

**ExternalDownloadJpaEntity**:
```java
@Entity
@Table(name = "external_download")
public class ExternalDownloadJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_url", nullable = false, length = 2048)
    private String sourceUrl;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ExternalDownloadStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "file_asset_id")
    private Long fileAssetId;  // nullable

    @Column(name = "webhook_url", length = 2048)
    private String webhookUrl;  // nullable

    @Column(name = "error_message", length = 1000)
    private String errorMessage;  // nullable

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Protected 기본 생성자 (JPA용)
    protected ExternalDownloadJpaEntity() {}

    // 정적 팩토리 메서드 (Mapper에서 호출)
    public static ExternalDownloadJpaEntity of(...) { ... }

    // Getter만 제공 (Setter 금지)
}
```

**DDL**:
```sql
CREATE TABLE external_download (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_url VARCHAR(2048) NOT NULL,
    tenant_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    file_asset_id BIGINT NULL,
    webhook_url VARCHAR(2048) NULL,
    error_message VARCHAR(1000) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_tenant_status (tenant_id, status),
    INDEX idx_status_created (status, created_at),
    INDEX idx_file_asset_id (file_asset_id)
);
```

**ExternalDownloadOutboxJpaEntity**:
```sql
CREATE TABLE external_download_outbox (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_download_id BIGINT NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_published_created (published, created_at),
    FOREIGN KEY (external_download_id) REFERENCES external_download(id)
);
```

#### 3.3 Adapter (CQRS 분리)

**ExternalDownloadCommandAdapter** (Command - JPA):
```java
@Component
public class ExternalDownloadCommandAdapter implements ExternalDownloadPersistencePort {

    private final ExternalDownloadJpaRepository jpaRepository;
    private final ExternalDownloadJpaEntityMapper mapper;

    public ExternalDownloadCommandAdapter(
        ExternalDownloadJpaRepository jpaRepository,
        ExternalDownloadJpaEntityMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public ExternalDownloadId persist(ExternalDownload externalDownload) {
        ExternalDownloadJpaEntity entity = mapper.toEntity(externalDownload);
        ExternalDownloadJpaEntity saved = jpaRepository.save(entity);
        return ExternalDownloadId.of(saved.getId());
    }
}
```

**ExternalDownloadQueryAdapter** (Query - QueryDSL):
```java
@Component
public class ExternalDownloadQueryAdapter implements ExternalDownloadQueryPort {

    private final ExternalDownloadQueryDslRepository queryDslRepository;
    private final ExternalDownloadJpaEntityMapper mapper;

    public ExternalDownloadQueryAdapter(
        ExternalDownloadQueryDslRepository queryDslRepository,
        ExternalDownloadJpaEntityMapper mapper
    ) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<ExternalDownload> findById(ExternalDownloadId id) {
        return queryDslRepository.findById(id.value())
            .map(mapper::toDomain);
    }

    @Override
    public Optional<ExternalDownload> findByIdAndTenantId(ExternalDownloadId id, TenantId tenantId) {
        return queryDslRepository.findByIdAndTenantId(id.value(), tenantId.value())
            .map(mapper::toDomain);
    }

    @Override
    public boolean existsById(ExternalDownloadId id) {
        return findById(id).isPresent();
    }
}
```

#### 3.4 Repository (CQRS 분리)

**ExternalDownloadJpaRepository** (Command용):
```java
public interface ExternalDownloadJpaRepository extends JpaRepository<ExternalDownloadJpaEntity, Long> {
    // JPA 기본 메서드만 사용 (save)
}
```

**ExternalDownloadQueryDslRepository** (Query용):
```java
@Repository
public class ExternalDownloadQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public ExternalDownloadQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public Optional<ExternalDownloadJpaEntity> findById(Long id) {
        QExternalDownloadJpaEntity e = QExternalDownloadJpaEntity.externalDownloadJpaEntity;
        return Optional.ofNullable(
            queryFactory.selectFrom(e)
                .where(e.id.eq(id))
                .fetchOne()
        );
    }

    public Optional<ExternalDownloadJpaEntity> findByIdAndTenantId(Long id, Long tenantId) {
        QExternalDownloadJpaEntity e = QExternalDownloadJpaEntity.externalDownloadJpaEntity;
        return Optional.ofNullable(
            queryFactory.selectFrom(e)
                .where(e.id.eq(id), e.tenantId.eq(tenantId))
                .fetchOne()
        );
    }
}
```

---

### 4. REST API Layer (컨벤션 준수)

#### 4.1 패키지 구조 (CQRS 분리)

```
adapter-in/rest-api/
└─ download/
   ├─ controller/
   │  ├─ ExternalDownloadCommandController.java  # POST (Command)
   │  └─ ExternalDownloadQueryController.java    # GET (Query)
   ├─ dto/
   │  ├─ command/
   │  │  └─ RequestExternalDownloadApiRequest.java
   │  ├─ query/
   │  │  └─ (필요시 추가)
   │  └─ response/
   │     ├─ ExternalDownloadApiResponse.java
   │     └─ ExternalDownloadDetailApiResponse.java
   ├─ mapper/
   │  └─ ExternalDownloadApiMapper.java  # @Component
   └─ error/
      └─ ExternalDownloadApiErrorMapper.java
```

#### 4.2 API 엔드포인트

| Method | Path | Description | Request | Response | Status |
|--------|------|-------------|---------|----------|--------|
| POST | /api/v1/external-downloads | 외부 다운로드 요청 | RequestExternalDownloadApiRequest | ResponseEntity<ApiResponse<ExternalDownloadApiResponse>> | 202 Accepted |
| GET | /api/v1/external-downloads/{id} | 다운로드 상태 조회 | - | ResponseEntity<ApiResponse<ExternalDownloadDetailApiResponse>> | 200 OK |

#### 4.3 Controller (Thin Controller 패턴)

**ExternalDownloadCommandController**:
```java
@RestController
@RequestMapping("${api.endpoints.base-v1}/external-downloads")
@Validated
public class ExternalDownloadCommandController {

    private final RequestExternalDownloadUseCase requestExternalDownloadUseCase;
    private final ExternalDownloadApiMapper mapper;

    public ExternalDownloadCommandController(
        RequestExternalDownloadUseCase requestExternalDownloadUseCase,
        ExternalDownloadApiMapper mapper
    ) {
        this.requestExternalDownloadUseCase = requestExternalDownloadUseCase;
        this.mapper = mapper;
    }

    /**
     * 외부 다운로드 요청
     *
     * @param request 외부 다운로드 요청 DTO
     * @return 생성된 다운로드 요청 정보 (202 Accepted)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ExternalDownloadApiResponse>> requestExternalDownload(
            @RequestBody @Valid RequestExternalDownloadApiRequest request) {

        // 1. API Request → UseCase Command 변환
        var command = mapper.toRequestCommand(request);

        // 2. UseCase 실행
        var useCaseResponse = requestExternalDownloadUseCase.execute(command);

        // 3. UseCase Response → API Response 변환
        var apiResponse = mapper.toApiResponse(useCaseResponse);

        // 4. ResponseEntity<ApiResponse<T>> 래핑 (202 Accepted)
        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(ApiResponse.ofSuccess(apiResponse));
    }
}
```

**ExternalDownloadQueryController**:
```java
@RestController
@RequestMapping("${api.endpoints.base-v1}/external-downloads")
@Validated
public class ExternalDownloadQueryController {

    private final GetExternalDownloadUseCase getExternalDownloadUseCase;
    private final ExternalDownloadApiMapper mapper;

    public ExternalDownloadQueryController(
        GetExternalDownloadUseCase getExternalDownloadUseCase,
        ExternalDownloadApiMapper mapper
    ) {
        this.getExternalDownloadUseCase = getExternalDownloadUseCase;
        this.mapper = mapper;
    }

    /**
     * 다운로드 상태 조회
     *
     * @param id 다운로드 요청 ID
     * @return 다운로드 상태 상세 정보 (200 OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExternalDownloadDetailApiResponse>> getExternalDownload(
            @PathVariable @Positive Long id) {

        // 1. API Request → UseCase Query 변환
        var query = mapper.toGetQuery(id);

        // 2. UseCase 실행
        var useCaseResponse = getExternalDownloadUseCase.execute(query);

        // 3. UseCase Response → API Response 변환
        var apiResponse = mapper.toDetailApiResponse(useCaseResponse);

        // 4. ResponseEntity<ApiResponse<T>> 래핑
        return ResponseEntity.ok(ApiResponse.ofSuccess(apiResponse));
    }
}
```

#### 4.4 Request/Response DTO (Record, *ApiRequest/*ApiResponse 네이밍)

**RequestExternalDownloadApiRequest** (Command DTO):
```java
public record RequestExternalDownloadApiRequest(
    @NotBlank(message = "sourceUrl은 필수입니다")
    @URL(message = "올바른 URL 형식이어야 합니다")
    String sourceUrl,

    @URL(message = "올바른 URL 형식이어야 합니다")
    String webhookUrl  // nullable
) {}
```

**ExternalDownloadApiResponse** (Response DTO):
```java
public record ExternalDownloadApiResponse(
    Long id,
    String status,
    Instant createdAt
) {}
```

**ExternalDownloadDetailApiResponse** (Response DTO):
```java
public record ExternalDownloadDetailApiResponse(
    Long id,
    String sourceUrl,
    String status,
    Long fileAssetId,      // nullable
    String errorMessage,   // nullable
    int retryCount,
    String webhookUrl,     // nullable
    Instant createdAt,
    Instant updatedAt
) {}
```

#### 4.5 Mapper (@Component DI)

**ExternalDownloadApiMapper**:
```java
@Component
public class ExternalDownloadApiMapper {

    public RequestExternalDownloadCommand toRequestCommand(RequestExternalDownloadApiRequest request) {
        return new RequestExternalDownloadCommand(
            request.sourceUrl(),
            request.webhookUrl()
        );
    }

    public GetExternalDownloadQuery toGetQuery(Long id) {
        return new GetExternalDownloadQuery(id);
    }

    public ExternalDownloadApiResponse toApiResponse(ExternalDownloadResponse useCaseResponse) {
        return new ExternalDownloadApiResponse(
            useCaseResponse.id(),
            useCaseResponse.status(),
            useCaseResponse.createdAt()
        );
    }

    public ExternalDownloadDetailApiResponse toDetailApiResponse(ExternalDownloadDetailResponse useCaseResponse) {
        return new ExternalDownloadDetailApiResponse(
            useCaseResponse.id(),
            useCaseResponse.sourceUrl(),
            useCaseResponse.status(),
            useCaseResponse.fileAssetId(),
            useCaseResponse.errorMessage(),
            useCaseResponse.retryCount(),
            useCaseResponse.webhookUrl(),
            useCaseResponse.createdAt(),
            useCaseResponse.updatedAt()
        );
    }
}
```

#### 4.6 Error Response (RFC 7807 준수)

```json
// 404 - 다운로드 요청 없음
{
    "errorCode": "EXTERNAL_DOWNLOAD_NOT_FOUND",
    "message": "외부 다운로드 요청을 찾을 수 없습니다.",
    "timestamp": "2025-11-26T12:34:56Z",
    "path": "/api/v1/external-downloads/123"
}

// 400 - 잘못된 URL 형식
{
    "errorCode": "INVALID_SOURCE_URL",
    "message": "유효하지 않은 이미지 URL입니다.",
    "timestamp": "2025-11-26T12:34:56Z",
    "path": "/api/v1/external-downloads"
}
```

---

### 5. Infrastructure Layer

#### 5.1 HTTP Client (외부 다운로드)

**HttpDownloadAdapter**:
```java
@Component
public class HttpDownloadAdapter implements HttpDownloadPort {

    private final RestTemplate restTemplate;

    // Timeout 설정
    // - Connection Timeout: 5초
    // - Read Timeout: 30초

    @Override
    public DownloadResult download(SourceUrl sourceUrl) {
        // 1. HEAD 요청으로 Content-Type 확인 (선택)
        // 2. GET 요청으로 다운로드
        // 3. 이미지 타입 검증
        // 4. DownloadResult 반환
    }
}
```

#### 5.2 SQS Publisher

**SqsPublishAdapter**:
```java
@Component
public class SqsPublishAdapter implements SqsPublishPort {

    private final AmazonSQS amazonSQS;

    @Override
    public boolean publish(ExternalDownloadMessage message) {
        // 1. 메시지 직렬화 (JSON)
        // 2. SQS SendMessage
        // 3. 성공/실패 반환
    }
}
```

**ExternalDownloadMessage**:
```java
public record ExternalDownloadMessage(
    Long externalDownloadId,
    String sourceUrl,
    Long tenantId,
    Long organizationId
) {}
```

#### 5.3 SQS Listener (Worker)

**ExternalDownloadSqsListener**:
```java
@Component
public class ExternalDownloadSqsListener {

    private final ProcessExternalDownloadUseCase useCase;

    @SqsListener("external-download-queue")
    public void onMessage(ExternalDownloadMessage message) {
        useCase.execute(new ProcessExternalDownloadCommand(
            message.externalDownloadId()
        ));
    }
}
```

#### 5.4 분산락 (Redis)

**RedisDistributedLockAdapter**:
```java
@Component
public class RedisDistributedLockAdapter implements DistributedLockPort {

    private final RedissonClient redissonClient;

    @Override
    public boolean tryLock(String key, Duration timeout) {
        RLock lock = redissonClient.getLock(key);
        return lock.tryLock(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void unlock(String key) {
        RLock lock = redissonClient.getLock(key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
```

---

## ⚠️ 제약사항

### 비기능 요구사항

**성능**:
- API 응답 시간: < 100ms (비동기이므로 빠름)
- 다운로드 처리 시간: 외부 서버 의존 (Timeout 30초)
- Worker TPS: 서버당 10 requests/sec (외부 I/O 병목)

**확장성**:
- Worker 수평 확장 가능 (SQS + 분산락)
- SQS Visibility Timeout: 5분 (처리 시간 고려)

**안정성**:
- Outbox 패턴으로 SQS 발행 보장
- 2회 재시도 + 디폴트 이미지 Fallback
- 분산락으로 중복 처리 방지

---

## 🧪 테스트 전략

### Unit Test

**Domain**:
- ExternalDownload 상태 전환 테스트
- SourceUrl, WebhookUrl VO 검증 테스트
- 재시도 로직 테스트 (canRetry, retry, fail)

**Application**:
- RequestExternalDownloadUseCase (Mock Port)
- ProcessExternalDownloadUseCase (Mock Port)
- OutboxPublishScheduler (Mock Port)

### Integration Test

**Persistence**:
- ExternalDownloadJpaRepository CRUD (TestContainers MySQL)
- ExternalDownloadOutboxJpaRepository 쿼리 테스트

**Infrastructure**:
- HttpDownloadAdapter (MockServer)
- SqsPublishAdapter (LocalStack)
- RedisDistributedLockAdapter (Embedded Redis)

### E2E Test

1. 외부 다운로드 요청 → 상태 조회 → 완료 확인
2. 다운로드 실패 → 재시도 → 디폴트 이미지 적용

---

## 🚀 개발 계획

### Phase 1: Domain Layer (예상: 2일)
- [ ] ExternalDownload Aggregate 구현
- [ ] ExternalDownloadOutbox Aggregate 구현
- [ ] SourceUrl, WebhookUrl VO 구현
- [ ] ExternalDownloadStatus Enum 구현
- [ ] Domain Unit Test

### Phase 2: Application Layer (예상: 3일)
- [ ] RequestExternalDownloadUseCase 구현
- [ ] ProcessExternalDownloadUseCase 구현
- [ ] GetExternalDownloadUseCase 구현
- [ ] OutboxPublishScheduler 구현
- [ ] Port 인터페이스 정의
- [ ] Application Unit Test

### Phase 3: Persistence Layer (예상: 2일)
- [ ] ExternalDownloadJpaEntity 구현
- [ ] ExternalDownloadOutboxJpaEntity 구현
- [ ] Flyway Migration 스크립트
- [ ] Repository 구현
- [ ] Persistence Integration Test

### Phase 4: Infrastructure Layer (예상: 3일)
- [ ] HttpDownloadAdapter 구현
- [ ] SqsPublishAdapter 구현
- [ ] ExternalDownloadSqsListener 구현
- [ ] RedisDistributedLockAdapter 구현
- [ ] Infrastructure Integration Test

### Phase 5: REST API Layer (예상: 2일)
- [ ] ExternalDownloadController 구현
- [ ] Request/Response DTO 구현
- [ ] Exception Handler 구현
- [ ] REST API Test

### Phase 6: Integration & E2E Test (예상: 2일)
- [ ] End-to-End Test
- [ ] 성능 테스트
- [ ] 장애 시나리오 테스트

---

## 📚 참고 문서

- [Domain Layer 규칙](../coding_convention/02-domain-layer/)
- [Application Layer 규칙](../coding_convention/03-application-layer/)
- [Persistence Layer 규칙](../coding_convention/04-persistence-layer/)
- [REST API Layer 규칙](../coding_convention/01-adapter-in-layer/rest-api/)

---

## 📝 결정 사항 (Decision Log)

| 항목 | 결정 | 이유 |
|------|------|------|
| 처리 방식 | 비동기 (SQS + Worker) | 대량 처리, 외부 I/O 병목 분리 |
| 상태 관리 | ExternalDownload 별도 Aggregate | FileAsset과 책임 분리 |
| 재시도 | 2회 | 과도한 재시도 방지 |
| 실패 처리 | 디폴트 이미지 (S3 고정 URL) | 서비스 연속성 보장 |
| 분산락 | Redis (Redisson) | 성능 + 신뢰성 |
| Outbox | 별도 테이블 | SQS 발행 보장 |
| Webhook | 선택적 파라미터 | 유연한 결과 통지 |

---

---

## ✅ 컨벤션 준수 체크리스트

### Domain Layer
- [x] ID는 Value Object로 정의 (`ExternalDownloadId`, `ExternalDownloadOutboxId`)
- [x] Factory Methods: `forNew()`, `of()`, `reconstitute()` 패턴
- [x] Private 생성자 + 정적 팩토리
- [x] Clock 주입으로 Instant 생성 (테스트 용이)
- [x] Value Object는 Record + Compact Constructor
- [x] ID VO는 `forNew()` + `of()` + `isNew()` 메서드
- [x] Lombok 금지

### Application Layer
- [x] PersistencePort는 `persist()` 단일 메서드
- [x] Port 네이밍: `{Bc}PersistencePort`, `{Bc}QueryPort`
- [x] QueryPort: VO 파라미터, Domain 반환, Optional 사용
- [x] Transaction 경계: 외부 I/O는 트랜잭션 밖에서
- [x] CQRS: Command/Query UseCase 분리

### Persistence Layer
- [x] CQRS 분리: Command=JPA, Query=QueryDSL
- [x] Adapter 네이밍: `{Bc}CommandAdapter`, `{Bc}QueryAdapter`
- [x] Entity: Lombok 금지, protected 기본 생성자, Getter만 제공
- [x] 연관관계 어노테이션 금지 (Long FK 전략)
- [x] Mapper: @Component, 순수 Java, Setter 금지

### REST API Layer
- [x] Controller 분리: Command/Query Controller
- [x] DTO 네이밍: `*ApiRequest`, `*ApiResponse`
- [x] Response 래핑: `ResponseEntity<ApiResponse<T>>`
- [x] Mapper: @Component DI
- [x] @Valid 검증 필수
- [x] Lombok 금지 (Record 사용)

---

**다음 단계**:
1. PRD 검토 및 피드백
2. `/jira-task` 또는 Plan 파일 생성
3. Kent Beck TDD 사이클 시작
