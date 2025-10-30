# KAN-144: 파일 업로드 시스템 - 구현 가이드 (코딩 컨벤션 준수)

## 📋 개요
이 문서는 KAN-144 파일 업로드 시스템의 26개 태스크를 FileFlow 프로젝트의 코딩 컨벤션과 Zero-Tolerance 규칙에 맞춰 구현하기 위한 상세 가이드입니다.

---

## 🔥 Zero-Tolerance 규칙 (필수 준수)

### 전체 태스크 공통 적용 규칙
```java
// ❌ 절대 금지
@Data, @Getter, @Setter, @Builder  // Lombok 사용 금지
order.getCustomer().getAddress()    // Getter 체이닝 금지
@ManyToOne, @OneToMany              // JPA 관계 어노테이션 금지
@Transactional + RestTemplate       // 트랜잭션 내 외부 API 호출 금지

// ✅ 필수 준수
private Long userId;                 // Long FK Strategy
public String getCustomerZip()      // Tell, Don't Ask
@author: Sangwon Ryu                // Javadoc 필수
@since: 1.0.0                       // 버전 명시
```

---

## 📁 디렉토리 구조 및 패키지 규칙

```
fileflow/
├── domain/                                  # 순수 비즈니스 로직
│   └── src/main/java/com/ryuqq/fileflow/domain/
│       ├── upload/
│       │   ├── MultipartUpload.java       # Aggregate Root
│       │   ├── UploadPart.java           # Value Object
│       │   └── UploadSession.java        # Aggregate Root (확장)
│       ├── download/
│       │   └── ExternalDownload.java     # Aggregate Root
│       ├── policy/
│       │   └── UploadPolicy.java         # Aggregate Root
│       └── event/
│           └── upload/                   # Domain Events
│
├── application/                            # UseCase & Service
│   └── src/main/java/com/ryuqq/fileflow/application/
│       ├── upload/
│       │   ├── command/                  # Command DTOs
│       │   ├── query/                    # Query DTOs
│       │   └── usecase/                  # UseCase 구현
│       ├── download/
│       ├── policy/
│       └── batch/
│
└── adapter/
    ├── adapter-in/
    │   └── rest-api/                      # REST Controllers
    └── adapter-out/
        ├── persistence-mysql/             # JPA Adapters
        ├── redis/                         # Cache Adapters
        └── event/                         # Event Publishers
```

---

## 🗂️ Phase 2A: Multipart Upload 태스크 가이드

### KAN-310: MultipartUpload Aggregate 구현

#### 📋 구현 체크리스트
- [ ] **NO Lombok**: 모든 필드에 대한 getter 수동 작성
- [ ] **Immutability**: 가능한 모든 필드를 `final`로 선언
- [ ] **Static Factory Method**: 생성자 대신 `create()`, `of()` 메서드 사용
- [ ] **Tell, Don't Ask**: 상태 확인 메서드 제공 (`canComplete()`, `isInProgress()`)
- [ ] **Javadoc**: 클래스와 모든 public 메서드에 문서화

#### ⚠️ 주의사항
```java
/**
 * Multipart 업로드 Aggregate Root
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class MultipartUpload {
    private final MultipartUploadId id;
    private final UploadSessionId uploadSessionId;
    private String providerUploadId;  // S3 UploadId (변경 가능)
    private MultipartStatus status;
    private final List<UploadPart> uploadedParts;  // 불변 리스트 사용

    // 생성자는 private으로
    private MultipartUpload(MultipartUploadId id, UploadSessionId sessionId) {
        this.id = id;
        this.uploadSessionId = sessionId;
        this.status = MultipartStatus.INIT;
        this.uploadedParts = new ArrayList<>();
    }

    // Static Factory Method
    public static MultipartUpload create(UploadSessionId sessionId) {
        return new MultipartUpload(
            MultipartUploadId.generate(),
            sessionId
        );
    }

    // Tell, Don't Ask 패턴
    public boolean canComplete() {
        return status == MultipartStatus.IN_PROGRESS
            && uploadedParts.size() == totalParts
            && areAllPartsValid();
    }

    // 상태 변경은 의미있는 비즈니스 메서드로
    public void initiate(String providerUploadId) {
        validateInitiation();
        this.providerUploadId = providerUploadId;
        this.status = MultipartStatus.IN_PROGRESS;
    }

    // Getter는 필요한 것만 제공
    public MultipartUploadId getId() {
        return id;
    }

    // 내부 상태 직접 노출 금지
    public List<UploadPart> getUploadedParts() {
        return Collections.unmodifiableList(uploadedParts);
    }
}
```

### KAN-311: UploadPart Value Object 구현

#### 📋 구현 체크리스트
- [ ] **완전한 불변성**: 모든 필드 `final`, setter 없음
- [ ] **equals/hashCode**: Value Object 필수 구현
- [ ] **Validation**: 생성 시점 검증
- [ ] **Static Factory Method**: `of()` 메서드 제공

#### ⚠️ 주의사항
```java
/**
 * 업로드 파트 Value Object
 * 불변 객체로 구현
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class UploadPart {
    private final Integer partNumber;
    private final String etag;
    private final Long size;
    private final LocalDateTime uploadedAt;

    // Private 생성자
    private UploadPart(Integer partNumber, String etag, Long size) {
        this.partNumber = validatePartNumber(partNumber);
        this.etag = validateEtag(etag);
        this.size = validateSize(size);
        this.uploadedAt = LocalDateTime.now();
    }

    // Static Factory Method
    public static UploadPart of(Integer partNumber, String etag, Long size) {
        return new UploadPart(partNumber, etag, size);
    }

    // 검증 로직
    private static Integer validatePartNumber(Integer partNumber) {
        if (partNumber == null || partNumber < 1 || partNumber > 10000) {
            throw new IllegalArgumentException(
                "Part number must be between 1 and 10000"
            );
        }
        return partNumber;
    }

    // Value Object 필수: equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UploadPart)) return false;
        UploadPart that = (UploadPart) o;
        return Objects.equals(partNumber, that.partNumber) &&
               Objects.equals(etag, that.etag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partNumber, etag);
    }

    // 필요한 getter만 제공
    public Integer getPartNumber() { return partNumber; }
    public String getEtag() { return etag; }
    public Long getSize() { return size; }
}
```

### KAN-313: MultipartUploadJpaAdapter 구현

#### 📋 구현 체크리스트
- [ ] **Long FK Strategy**: 관계 어노테이션 사용 금지
- [ ] **Entity ↔ Domain 매핑**: 명시적 Mapper 구현
- [ ] **트랜잭션 경계**: Repository 메서드에 @Transactional 금지
- [ ] **QueryDSL 활용**: 복잡한 쿼리는 QueryDSL 사용

#### ⚠️ 주의사항
```java
/**
 * JPA Entity - Long FK Strategy 적용
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Entity
@Table(name = "upload_multipart")
public class MultipartUploadEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ❌ 금지: @ManyToOne @JoinColumn(name = "upload_session_id")
    // ✅ 필수: Long FK Strategy
    @Column(name = "upload_session_id", nullable = false)
    private Long uploadSessionId;

    @Column(name = "provider_upload_id")
    private String providerUploadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MultipartStatus status;

    // Getter/Setter 수동 구현 (NO Lombok!)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUploadSessionId() {
        return uploadSessionId;
    }

    public void setUploadSessionId(Long uploadSessionId) {
        this.uploadSessionId = uploadSessionId;
    }
}

/**
 * Adapter 구현 - Port 구현
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class MultipartUploadJpaAdapter implements MultipartUploadPort {
    private final MultipartUploadJpaRepository repository;
    private final MultipartUploadMapper mapper;

    // 생성자 주입 (NO @Autowired)
    public MultipartUploadJpaAdapter(
        MultipartUploadJpaRepository repository,
        MultipartUploadMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public MultipartUpload save(MultipartUpload multipart) {
        MultipartUploadEntity entity = mapper.toEntity(multipart);
        MultipartUploadEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<MultipartUpload> findBySessionId(Long sessionId) {
        return repository.findByUploadSessionId(sessionId)
            .map(mapper::toDomain);
    }
}
```

### KAN-315: InitMultipartUploadUseCase 구현

#### 📋 구현 체크리스트
- [ ] **트랜잭션 경계 설정**: UseCase 레벨에서 @Transactional
- [ ] **외부 API 호출 위치**: 트랜잭션 전/후로 분리
- [ ] **Command/Query 분리**: Command DTO 사용
- [ ] **이벤트 발행**: Domain Event 활용

#### ⚠️ 주의사항
```java
/**
 * Multipart 업로드 시작 UseCase
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class InitMultipartUploadUseCase {
    private final UploadSessionPort uploadSessionPort;
    private final S3StoragePort s3StoragePort;

    /**
     * Multipart 업로드 초기화
     * 주의: S3 API 호출은 트랜잭션 밖에서!
     */
    public InitMultipartResponse execute(InitMultipartCommand command) {
        // 1. S3 Multipart 초기화 (트랜잭션 밖)
        String uploadId = initializeS3Multipart(command);

        // 2. 도메인 로직 처리 (트랜잭션 내)
        UploadSession session = createAndSaveSession(command, uploadId);

        return new InitMultipartResponse(
            session.getSessionKey(),
            uploadId,
            session.calculateTotalParts()
        );
    }

    // S3 API 호출 - 트랜잭션 밖
    private String initializeS3Multipart(InitMultipartCommand command) {
        try {
            return s3StoragePort.initiateMultipartUpload(
                generateStorageKey(command)
            );
        } catch (S3Exception e) {
            throw new StorageException("Failed to initialize multipart", e);
        }
    }

    // 도메인 로직 - 트랜잭션 내
    @Transactional
    protected UploadSession createAndSaveSession(
        InitMultipartCommand command,
        String uploadId
    ) {
        // 세션 생성
        UploadSession session = UploadSession.createMultipart(
            command.getTenantId(),
            command.getFileName(),
            command.getFileSize()
        );

        // Multipart 정보 설정
        session.initMultipart(uploadId);

        // 저장 (이벤트는 자동 발행)
        return uploadSessionPort.save(session);
    }
}
```

---

## 🗂️ Phase 2B: External Download & Policy 태스크 가이드

### KAN-320: ExternalDownload Aggregate 구현

#### 📋 구현 체크리스트
- [ ] **URL 검증**: 생성 시점에 URL 유효성 검증
- [ ] **재시도 로직**: 지수 백오프 구현
- [ ] **상태 머신**: 명확한 상태 전환 규칙
- [ ] **Tell, Don't Ask**: `canRetry()`, `shouldRetry()` 메서드

#### ⚠️ 주의사항
```java
/**
 * 외부 다운로드 Aggregate
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class ExternalDownload {
    private final Long id;
    private final Long uploadSessionId;  // Long FK
    private final URL sourceUrl;         // 검증된 URL
    private DownloadProgress progress;
    private RetryPolicy retryPolicy;

    // URL 검증을 생성자에서
    private ExternalDownload(Long sessionId, String url) {
        this.uploadSessionId = sessionId;
        this.sourceUrl = validateAndParseUrl(url);
        this.progress = DownloadProgress.notStarted();
        this.retryPolicy = RetryPolicy.defaultPolicy();
    }

    // Tell, Don't Ask
    public boolean canRetry() {
        return retryPolicy.canRetry() &&
               isRetryableError();
    }

    public Duration getNextRetryDelay() {
        return retryPolicy.getNextDelay();
    }

    // 진행률 계산 (getter 체이닝 방지)
    public int getProgressPercentage() {
        return progress.calculatePercentage();
    }

    // 재시도 정책 (Value Object)
    private static class RetryPolicy {
        private final int maxRetries = 3;
        private final int currentAttempt;

        public Duration getNextDelay() {
            // 지수 백오프: 1s, 2s, 4s
            return Duration.ofSeconds(
                (long) Math.pow(2, currentAttempt)
            );
        }
    }
}
```

### KAN-321: UploadPolicy Aggregate 구현

#### 📋 구현 체크리스트
- [ ] **정책 평가 로직**: 단일 책임 원칙
- [ ] **우선순위 관리**: Comparable 구현
- [ ] **불변 규칙**: PolicyRules를 Value Object로

#### ⚠️ 주의사항
```java
/**
 * 업로드 정책 Aggregate
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class UploadPolicy {
    private final Long id;
    private final Long tenantId;  // Long FK
    private final PolicyRules rules;
    private PolicyStatus status;
    private final Integer priority;

    // 정책 평가 (Tell, Don't Ask)
    public PolicyEvaluationResult evaluate(FileMetadata file) {
        if (!isActive()) {
            return PolicyEvaluationResult.notApplicable();
        }

        ValidationResult validation = rules.validate(file);
        if (validation.isValid()) {
            return PolicyEvaluationResult.passed(this.id);
        }

        return PolicyEvaluationResult.failed(
            this.id,
            validation.getViolations()
        );
    }

    // 정책 규칙 (불변 Value Object)
    public static final class PolicyRules {
        private final Set<String> allowedMimeTypes;
        private final FileSizeRange sizeRange;
        private final Set<String> allowedExtensions;
        private final ProcessingOptions processingOptions;

        // 빌더 패턴 (수동 구현)
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Set<String> allowedMimeTypes = new HashSet<>();

            public Builder allowMimeTypes(String... types) {
                this.allowedMimeTypes.addAll(Arrays.asList(types));
                return this;
            }

            public PolicyRules build() {
                return new PolicyRules(this);
            }
        }
    }
}
```

### KAN-324: ExternalDownloadWorker 구현

#### 📋 구현 체크리스트
- [ ] **비동기 처리**: @Async 사용
- [ ] **재시도 메커니즘**: @Retryable 설정
- [ ] **스트리밍 처리**: 메모리 효율적 구현
- [ ] **진행률 추적**: 콜백 활용

#### ⚠️ 주의사항
```java
/**
 * 외부 다운로드 Worker
 * 백그라운드 실행
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class ExternalDownloadWorker {
    private final ExternalDownloadPort downloadPort;
    private final S3StoragePort s3StoragePort;
    private final RestTemplate restTemplate;

    /**
     * 비동기 다운로드 실행
     * 트랜잭션 주의: @Async와 @Transactional 분리
     */
    @Async("downloadExecutor")
    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2),
        value = {IOException.class, RestClientException.class}
    )
    public CompletableFuture<DownloadResult> executeDownload(Long downloadId) {
        // 1. 다운로드 정보 조회 (트랜잭션 짧게)
        ExternalDownload download = loadDownload(downloadId);

        // 2. 실제 다운로드 (트랜잭션 밖)
        return performDownload(download)
            .thenApply(result -> updateDownloadStatus(download, result));
    }

    @Transactional(readOnly = true)
    protected ExternalDownload loadDownload(Long downloadId) {
        return downloadPort.findById(downloadId)
            .orElseThrow(() -> new DownloadNotFoundException(downloadId));
    }

    // 스트리밍 다운로드 (트랜잭션 밖)
    private CompletableFuture<DownloadResult> performDownload(
        ExternalDownload download
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try (InputStream input = openUrlStream(download.getSourceUrl())) {
                // S3로 스트리밍 업로드
                return s3StoragePort.uploadStream(
                    generateS3Key(download),
                    input,
                    progress -> trackProgress(download, progress)
                );
            } catch (IOException e) {
                throw new DownloadException("Failed to download", e);
            }
        });
    }

    // 진행률 추적 (주기적 업데이트)
    private void trackProgress(ExternalDownload download, Progress progress) {
        // 1초마다 한 번씩만 업데이트 (DB 부하 방지)
        if (shouldUpdateProgress()) {
            updateProgressInDatabase(download, progress);
        }
    }
}
```

---

## 🗂️ Phase 2C: Event & Integration 태스크 가이드

### KAN-326: AbstractAggregateRoot 확장

#### 📋 구현 체크리스트
- [ ] **Spring Data 통합**: AbstractAggregateRoot 상속
- [ ] **이벤트 등록**: registerEvent() 사용
- [ ] **트랜잭션 커밋 시 발행**: 자동 처리 확인
- [ ] **이벤트 불변성**: 이벤트 객체 불변 설계

#### ⚠️ 주의사항
```java
/**
 * UploadSession Aggregate Root
 * Spring Data의 도메인 이벤트 기능 활용
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class UploadSession extends AbstractAggregateRoot<UploadSession> {
    // 기존 필드들...

    /**
     * 업로드 완료
     * 이벤트는 트랜잭션 커밋 시 자동 발행
     */
    public void complete(String etag) {
        validateCanComplete();

        this.status = UploadStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.etag = etag;

        // 이벤트 등록 (즉시 발행 X, 커밋 시 발행)
        registerEvent(UploadCompletedEvent.of(
            this.id,
            this.sessionKey,
            this.fileId,
            this.completedAt
        ));
    }

    /**
     * 업로드 실패
     */
    public void fail(FailureReason reason) {
        this.status = UploadStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = reason;

        // 실패 이벤트 등록
        registerEvent(UploadFailedEvent.of(
            this.id,
            this.sessionKey,
            reason
        ));
    }

    // 검증 메서드 (Tell, Don't Ask)
    private void validateCanComplete() {
        if (status != UploadStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                "Cannot complete upload in status: " + status
            );
        }
    }
}
```

### KAN-327: Domain Events 정의

#### 📋 구현 체크리스트
- [ ] **불변 이벤트**: 모든 필드 final
- [ ] **타임스탬프**: occurredAt 필수
- [ ] **Static Factory**: of() 메서드 제공
- [ ] **최소 정보**: 필요한 정보만 포함

#### ⚠️ 주의사항
```java
/**
 * 업로드 완료 도메인 이벤트
 * 불변 객체로 설계
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class UploadCompletedEvent {
    private final Long uploadSessionId;
    private final String sessionKey;
    private final Long fileId;
    private final LocalDateTime completedAt;
    private final LocalDateTime occurredAt;

    // Private 생성자
    private UploadCompletedEvent(
        Long uploadSessionId,
        String sessionKey,
        Long fileId,
        LocalDateTime completedAt
    ) {
        this.uploadSessionId = uploadSessionId;
        this.sessionKey = sessionKey;
        this.fileId = fileId;
        this.completedAt = completedAt;
        this.occurredAt = LocalDateTime.now();
    }

    // Static Factory Method
    public static UploadCompletedEvent of(
        Long uploadSessionId,
        String sessionKey,
        Long fileId,
        LocalDateTime completedAt
    ) {
        return new UploadCompletedEvent(
            uploadSessionId,
            sessionKey,
            fileId,
            completedAt
        );
    }

    // Getter만 제공 (Setter 없음)
    public Long getUploadSessionId() {
        return uploadSessionId;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    // equals/hashCode 구현 (이벤트 중복 방지)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UploadCompletedEvent)) return false;
        UploadCompletedEvent that = (UploadCompletedEvent) o;
        return Objects.equals(uploadSessionId, that.uploadSessionId) &&
               Objects.equals(occurredAt, that.occurredAt);
    }
}
```

### KAN-328: UploadEventPublisher 구현

#### 📋 구현 체크리스트
- [ ] **@TransactionalEventListener**: 트랜잭션 후 처리
- [ ] **Anti-Corruption Layer**: 도메인 → 외부 변환
- [ ] **실패 처리**: 이벤트 발행 실패 시 로깅
- [ ] **멱등성**: 중복 이벤트 처리 방지

#### ⚠️ 주의사항
```java
/**
 * 도메인 이벤트 Publisher
 * Anti-Corruption Layer 역할
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class UploadEventPublisher {
    private final SqsTemplate sqsTemplate;
    private final UploadEventMapper mapper;
    private final EventDeduplicationService deduplicationService;

    /**
     * 업로드 완료 이벤트 처리
     * 트랜잭션 커밋 후 실행
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @EventListener(condition = "#event.shouldPublish()")
    public void handleUploadCompleted(UploadCompletedEvent event) {
        // 1. 중복 체크 (멱등성)
        if (deduplicationService.isDuplicate(event)) {
            log.warn("Duplicate event detected: {}", event);
            return;
        }

        try {
            // 2. 도메인 이벤트 → SQS 메시지 변환
            SqsMessage message = mapper.toSqsMessage(event);

            // 3. SQS 발행
            sqsTemplate.send(message);

            // 4. 발행 성공 기록
            deduplicationService.markAsProcessed(event);

            log.info("Published upload completed: {}", event.getSessionKey());

        } catch (SqsException e) {
            // 실패 시 로깅 (재시도는 SQS가 처리)
            log.error("Failed to publish event: {}", event, e);
            // 알림 또는 모니터링 시스템으로 전달
            alertingService.notifyEventPublishFailure(event, e);
        }
    }

    /**
     * 업로드 실패 이벤트 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUploadFailed(UploadFailedEvent event) {
        // 실패 이벤트는 항상 발행 (중복 체크 불필요)
        try {
            SqsMessage message = mapper.toFailureMessage(event);
            sqsTemplate.send(message);
        } catch (Exception e) {
            log.error("Failed to publish failure event: {}", event, e);
        }
    }
}
```

### KAN-330: IdempotencyMiddleware 구현

#### 📋 구현 체크리스트
- [ ] **Redis 활용**: 멱등성 키 저장
- [ ] **분산 락**: 동시 요청 방지
- [ ] **TTL 설정**: 자동 만료
- [ ] **AOP 적용**: @Idempotent 어노테이션

#### ⚠️ 주의사항
```java
/**
 * 멱등성 미들웨어
 * 중복 요청 방지
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@Aspect
@Order(1)  // 다른 AOP보다 먼저 실행
public class IdempotencyMiddleware {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * @Idempotent 어노테이션 처리
     */
    @Around("@annotation(idempotent)")
    public Object handleIdempotency(
        ProceedingJoinPoint joinPoint,
        Idempotent idempotent
    ) throws Throwable {

        // 1. 멱등성 키 추출
        String idempotencyKey = extractIdempotencyKey();
        if (idempotencyKey == null) {
            // 키가 없으면 일반 처리
            return joinPoint.proceed();
        }

        String cacheKey = buildCacheKey(idempotencyKey);
        String lockKey = cacheKey + ":lock";

        // 2. 캐시 확인
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("Idempotent cache hit: {}", idempotencyKey);
            return deserializeResponse(cached);
        }

        // 3. 분산 락 획득
        Boolean locked = acquireLock(lockKey, idempotent.lockTimeout());
        if (!locked) {
            throw new ConcurrentRequestException(
                "Request already in progress: " + idempotencyKey
            );
        }

        try {
            // 4. 실제 처리
            Object result = joinPoint.proceed();

            // 5. 결과 캐싱
            cacheResult(cacheKey, result, idempotent.ttl());

            return result;

        } finally {
            // 6. 락 해제
            releaseLock(lockKey);
        }
    }

    private Boolean acquireLock(String lockKey, long timeout) {
        return redisTemplate.opsForValue()
            .setIfAbsent(
                lockKey,
                Thread.currentThread().getId().toString(),
                Duration.ofMillis(timeout)
            );
    }

    private void releaseLock(String lockKey) {
        // 본인이 획득한 락만 해제
        String lockHolder = redisTemplate.opsForValue().get(lockKey);
        if (Thread.currentThread().getId().toString().equals(lockHolder)) {
            redisTemplate.delete(lockKey);
        }
    }
}

/**
 * 멱등성 어노테이션
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    long ttl() default 86400000L;  // 24시간
    long lockTimeout() default 10000L;  // 10초
}
```

---

## 📊 통합 테스트 가이드

### 테스트 전략
```java
/**
 * Multipart Upload 통합 테스트
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestContainers
@Tag("integration")
public class MultipartUploadIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.0");

    @Container
    static LocalStackContainer localStack = new LocalStackContainer()
        .withServices(S3);

    @Test
    @DisplayName("Multipart 업로드 전체 플로우 테스트")
    void shouldCompleteMultipartUploadFlow() {
        // Given: 테넌트와 파일 정보
        Long tenantId = 1L;
        String fileName = "large-file.zip";
        Long fileSize = 200 * 1024 * 1024L; // 200MB

        // When: Multipart 초기화
        InitMultipartResponse initResponse = initMultipart(
            tenantId, fileName, fileSize
        );

        // Then: 초기화 검증
        assertThat(initResponse.getUploadId()).isNotNull();
        assertThat(initResponse.getTotalParts()).isEqualTo(2);

        // When: 각 파트 업로드
        uploadParts(initResponse);

        // When: 완료 처리
        CompleteResponse complete = completeMultipart(
            initResponse.getSessionKey()
        );

        // Then: 완료 검증
        assertThat(complete.getFileId()).isNotNull();

        // And: 이벤트 발행 확인
        verifyEventPublished(UploadCompletedEvent.class);
    }

    @Test
    @DisplayName("멱등성 테스트 - 중복 요청 처리")
    void shouldHandleDuplicateRequests() {
        // Given: 멱등성 키
        String idempotencyKey = UUID.randomUUID().toString();

        // When: 첫 번째 요청
        ResponseEntity<InitResponse> first = callWithIdempotencyKey(
            idempotencyKey
        );

        // When: 동일한 키로 재요청
        ResponseEntity<InitResponse> second = callWithIdempotencyKey(
            idempotencyKey
        );

        // Then: 동일한 응답
        assertThat(first.getBody()).isEqualTo(second.getBody());
    }
}
```

---

## ✅ 최종 체크리스트

### 각 태스크 완료 시 확인사항
- [ ] **NO Lombok**: 코드에 Lombok 어노테이션 없음
- [ ] **Law of Demeter**: Getter 체이닝 없음
- [ ] **Long FK Strategy**: JPA 관계 어노테이션 없음
- [ ] **트랜잭션 경계**: 외부 API 호출 분리
- [ ] **Javadoc**: 모든 public 요소 문서화
- [ ] **테스트 커버리지**: Domain 90%, Application 80%, Adapter 70%
- [ ] **Static Factory Method**: 생성자 대신 사용
- [ ] **Tell, Don't Ask**: 상태 확인 메서드 제공
- [ ] **불변성**: Value Object는 완전 불변
- [ ] **이벤트**: AbstractAggregateRoot 활용

### Git Commit 메시지 규칙
```bash
# 기능 구현
feat(KAN-310): implement MultipartUpload aggregate with state machine

# 테스트 추가
test(KAN-310): add unit tests for MultipartUpload state transitions

# 리팩토링
refactor(KAN-310): extract retry policy to value object

# 버그 수정
fix(KAN-310): resolve part number validation issue
```

---

이 가이드를 따라 각 태스크를 구현하시면, FileFlow 프로젝트의 코딩 컨벤션을 완벽하게 준수하면서 고품질의 코드를 작성할 수 있습니다.