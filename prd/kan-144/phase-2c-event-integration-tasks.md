# Phase 2C: Event & Integration 태스크 상세 가이드

## 📋 Phase 2C 개요
- **목표**: 도메인 이벤트 발행, 멱등성 보장, 배치 작업 구현
- **태스크 수**: 10개 (KAN-326 ~ KAN-335)
- **예상 기간**: 2주
- **핵심 기술**: Spring Data Domain Events, Anti-Corruption Layer, Redis 멱등성

---

## KAN-326: UploadSession AbstractAggregateRoot 확장

### 📌 작업 내용
```java
// 위치: domain/src/main/java/com/ryuqq/fileflow/domain/upload/UploadSession.java

import org.springframework.data.domain.AbstractAggregateRoot;

/**
 * Upload Session Aggregate Root
 * Spring Data의 도메인 이벤트 기능 활용
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class UploadSession extends AbstractAggregateRoot<UploadSession> {

    private final Long id;
    private final Long tenantId;  // Long FK Strategy
    private final String sessionKey;
    private String fileName;
    private Long fileSize;
    private UploadType uploadType;
    private UploadStatus status;
    private String storageKey;
    private String etag;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime expiredAt;
    private FailureDetails failureDetails;

    // Associations (Long FK)
    private Long multipartUploadId;
    private Long externalDownloadId;
    private Long fileId;

    // Private 생성자
    private UploadSession(
        Long tenantId,
        String fileName,
        Long fileSize,
        UploadType uploadType
    ) {
        this.id = null;
        this.tenantId = tenantId;
        this.sessionKey = generateSessionKey();
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.uploadType = uploadType;
        this.status = UploadStatus.INIT;
        this.createdAt = LocalDateTime.now();
        this.expiredAt = createdAt.plusHours(24);
    }

    // Static Factory Methods
    public static UploadSession createForSingleUpload(
        Long tenantId,
        String fileName,
        Long fileSize
    ) {
        UploadSession session = new UploadSession(
            tenantId, fileName, fileSize, UploadType.SINGLE
        );

        // 세션 생성 이벤트 등록
        session.registerEvent(UploadSessionCreatedEvent.of(
            session.sessionKey,
            session.uploadType,
            session.createdAt
        ));

        return session;
    }

    public static UploadSession createForMultipart(
        Long tenantId,
        String fileName,
        Long fileSize
    ) {
        return new UploadSession(
            tenantId, fileName, fileSize, UploadType.MULTIPART
        );
    }

    public static UploadSession createForExternalDownload(
        Long tenantId,
        String fileName,
        String sourceUrl
    ) {
        UploadSession session = new UploadSession(
            tenantId, fileName, null, UploadType.EXTERNAL
        );

        // External 다운로드 시작 이벤트
        session.registerEvent(ExternalDownloadRequestedEvent.of(
            session.sessionKey,
            sourceUrl,
            session.createdAt
        ));

        return session;
    }

    /**
     * 업로드 완료 처리
     * 이벤트는 registerEvent()로 등록되고,
     * Repository.save() 시 트랜잭션 커밋 직전에 자동 발행됨
     */
    public void complete(String etag, Long fileId) {
        validateCanComplete();

        this.status = UploadStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.etag = etag;
        this.fileId = fileId;

        // 이벤트 등록 (트랜잭션 커밋 시 발행)
        registerEvent(UploadCompletedEvent.of(
            this.id,
            this.sessionKey,
            this.fileId,
            this.completedAt
        ));

        // 처리 시간 메트릭 이벤트
        Duration processingTime = Duration.between(createdAt, completedAt);
        registerEvent(UploadMetricEvent.of(
            this.tenantId,
            this.uploadType,
            processingTime,
            this.fileSize
        ));
    }

    /**
     * 업로드 실패 처리
     */
    public void fail(String errorCode, String errorMessage) {
        this.status = UploadStatus.FAILED;
        this.failureDetails = new FailureDetails(errorCode, errorMessage);

        // 실패 이벤트 등록
        registerEvent(UploadFailedEvent.of(
            this.id,
            this.sessionKey,
            errorCode,
            errorMessage,
            LocalDateTime.now()
        ));
    }

    /**
     * 세션 만료 처리
     */
    public void expire() {
        if (this.status == UploadStatus.COMPLETED) {
            throw new IllegalStateException("Cannot expire completed session");
        }

        this.status = UploadStatus.EXPIRED;
        this.expiredAt = LocalDateTime.now();

        // 만료 이벤트 등록
        registerEvent(UploadExpiredEvent.of(
            this.id,
            this.sessionKey,
            this.expiredAt
        ));
    }

    /**
     * 업로드 중단
     */
    public void abort(String reason) {
        this.status = UploadStatus.ABORTED;

        // 중단 이벤트 등록
        registerEvent(UploadAbortedEvent.of(
            this.id,
            this.sessionKey,
            reason,
            LocalDateTime.now()
        ));
    }

    // Multipart 연결
    public void attachMultipart(Long multipartUploadId) {
        if (this.uploadType != UploadType.MULTIPART) {
            throw new IllegalStateException("Not a multipart upload session");
        }
        this.multipartUploadId = multipartUploadId;
    }

    // External Download 연결
    public void attachExternalDownload(Long externalDownloadId) {
        if (this.uploadType != UploadType.EXTERNAL) {
            throw new IllegalStateException("Not an external download session");
        }
        this.externalDownloadId = externalDownloadId;
    }

    // 검증 메서드 (Tell, Don't Ask)
    private void validateCanComplete() {
        if (status != UploadStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                "Cannot complete upload in status: " + status
            );
        }
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    public boolean canBeCompleted() {
        return status == UploadStatus.IN_PROGRESS && !isExpired();
    }

    // Session Key 생성
    private static String generateSessionKey() {
        return "USN_" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    // Getter (필요한 것만)
    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getSessionKey() { return sessionKey; }
    public UploadStatus getStatus() { return status; }
    public UploadType getUploadType() { return uploadType; }
    public Long getFileId() { return fileId; }
}

// Enums
public enum UploadType {
    SINGLE,     // 단일 파일 업로드
    MULTIPART,  // 대용량 분할 업로드
    EXTERNAL    // 외부 URL 다운로드
}

public enum UploadStatus {
    INIT,        // 초기화
    IN_PROGRESS, // 진행 중
    COMPLETED,   // 완료
    FAILED,      // 실패
    EXPIRED,     // 만료
    ABORTED      // 중단
}
```

### ⚠️ 코딩 컨벤션 체크포인트
- ✅ **AbstractAggregateRoot 상속**: Spring Data 이벤트 기능
- ✅ **이벤트 등록**: `registerEvent()` 사용
- ✅ **트랜잭션 경계**: 커밋 시 자동 발행
- ✅ **Tell, Don't Ask**: `canBeCompleted()`, `isExpired()`

---

## KAN-327: Domain Events 정의

### 📌 작업 내용
```java
// 위치: domain/src/main/java/com/ryuqq/fileflow/domain/event/upload/

/**
 * 업로드 완료 도메인 이벤트
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
    private final String eventId;

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
        this.eventId = UUID.randomUUID().toString();
    }

    // Static Factory Method
    public static UploadCompletedEvent of(
        Long uploadSessionId,
        String sessionKey,
        Long fileId,
        LocalDateTime completedAt
    ) {
        return new UploadCompletedEvent(
            uploadSessionId, sessionKey, fileId, completedAt
        );
    }

    // 이벤트 식별용
    public String getEventKey() {
        return String.format("upload.completed.%s", sessionKey);
    }

    // 멱등성 키
    public String getIdempotencyKey() {
        return String.format("%s:%d:%d", eventId, uploadSessionId, fileId);
    }

    // Getter
    public Long getUploadSessionId() { return uploadSessionId; }
    public String getSessionKey() { return sessionKey; }
    public Long getFileId() { return fileId; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public String getEventId() { return eventId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UploadCompletedEvent)) return false;
        UploadCompletedEvent that = (UploadCompletedEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }
}

/**
 * 업로드 실패 이벤트
 */
public final class UploadFailedEvent {

    private final Long uploadSessionId;
    private final String sessionKey;
    private final String errorCode;
    private final String errorMessage;
    private final LocalDateTime failedAt;
    private final LocalDateTime occurredAt;
    private final String eventId;

    private UploadFailedEvent(
        Long uploadSessionId,
        String sessionKey,
        String errorCode,
        String errorMessage,
        LocalDateTime failedAt
    ) {
        this.uploadSessionId = uploadSessionId;
        this.sessionKey = sessionKey;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.failedAt = failedAt;
        this.occurredAt = LocalDateTime.now();
        this.eventId = UUID.randomUUID().toString();
    }

    public static UploadFailedEvent of(
        Long uploadSessionId,
        String sessionKey,
        String errorCode,
        String errorMessage,
        LocalDateTime failedAt
    ) {
        return new UploadFailedEvent(
            uploadSessionId, sessionKey, errorCode, errorMessage, failedAt
        );
    }

    public boolean isRetryable() {
        // 재시도 가능한 에러 판별
        return errorCode.startsWith("5") || "TIMEOUT".equals(errorCode);
    }

    // Getter
    public Long getUploadSessionId() { return uploadSessionId; }
    public String getSessionKey() { return sessionKey; }
    public String getErrorCode() { return errorCode; }
    public String getErrorMessage() { return errorMessage; }
}

/**
 * 업로드 만료 이벤트
 */
public final class UploadExpiredEvent {

    private final Long uploadSessionId;
    private final String sessionKey;
    private final LocalDateTime expiredAt;
    private final LocalDateTime occurredAt;

    private UploadExpiredEvent(
        Long uploadSessionId,
        String sessionKey,
        LocalDateTime expiredAt
    ) {
        this.uploadSessionId = uploadSessionId;
        this.sessionKey = sessionKey;
        this.expiredAt = expiredAt;
        this.occurredAt = LocalDateTime.now();
    }

    public static UploadExpiredEvent of(
        Long uploadSessionId,
        String sessionKey,
        LocalDateTime expiredAt
    ) {
        return new UploadExpiredEvent(uploadSessionId, sessionKey, expiredAt);
    }

    // Getter
    public Long getUploadSessionId() { return uploadSessionId; }
    public String getSessionKey() { return sessionKey; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
}

/**
 * 업로드 메트릭 이벤트
 */
public final class UploadMetricEvent {

    private final Long tenantId;
    private final UploadType uploadType;
    private final Duration processingTime;
    private final Long fileSize;
    private final LocalDateTime occurredAt;

    private UploadMetricEvent(
        Long tenantId,
        UploadType uploadType,
        Duration processingTime,
        Long fileSize
    ) {
        this.tenantId = tenantId;
        this.uploadType = uploadType;
        this.processingTime = processingTime;
        this.fileSize = fileSize;
        this.occurredAt = LocalDateTime.now();
    }

    public static UploadMetricEvent of(
        Long tenantId,
        UploadType uploadType,
        Duration processingTime,
        Long fileSize
    ) {
        return new UploadMetricEvent(tenantId, uploadType, processingTime, fileSize);
    }

    public long getProcessingTimeMillis() {
        return processingTime.toMillis();
    }

    public double getThroughputMBps() {
        if (fileSize == null || fileSize == 0) return 0;
        double sizeMB = fileSize / (1024.0 * 1024.0);
        double timeSeconds = processingTime.toMillis() / 1000.0;
        return sizeMB / timeSeconds;
    }

    // Getter
    public Long getTenantId() { return tenantId; }
    public UploadType getUploadType() { return uploadType; }
    public Duration getProcessingTime() { return processingTime; }
    public Long getFileSize() { return fileSize; }
}
```

### ⚠️ 코딩 컨벤션 체크포인트
- ✅ **완전 불변**: 모든 필드 final, setter 없음
- ✅ **Static Factory**: `of()` 메서드 제공
- ✅ **이벤트 ID**: 멱등성 보장용 고유 ID
- ✅ **비즈니스 메서드**: `isRetryable()`, `getThroughputMBps()`

---

## KAN-328: UploadEventPublisher 구현 (Anti-Corruption Layer)

### 📌 작업 내용
```java
// 위치: adapter-out/event/src/main/java/com/ryuqq/fileflow/adapter/out/event/UploadEventPublisher.java

/**
 * 업로드 이벤트 Publisher
 * Anti-Corruption Layer 패턴 적용
 * 도메인 이벤트 → 외부 메시지 변환
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UploadEventPublisher {

    private final SqsClient sqsClient;
    private final UploadEventMapper mapper;
    private final EventDeduplicationService deduplicationService;
    private final EventMetricsCollector metricsCollector;

    @Value("${aws.sqs.queues.upload-events}")
    private String uploadEventQueueUrl;

    /**
     * 업로드 완료 이벤트 처리
     * TransactionPhase.AFTER_COMMIT: 트랜잭션 커밋 후 실행
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Order(1)  // 실행 순서
    public void handleUploadCompleted(UploadCompletedEvent event) {
        String idempotencyKey = event.getIdempotencyKey();

        // 1. 중복 체크
        if (deduplicationService.isDuplicate(idempotencyKey)) {
            log.warn("Duplicate event detected: {}", idempotencyKey);
            metricsCollector.recordDuplicateEvent(event.getClass().getSimpleName());
            return;
        }

        try {
            // 2. 도메인 이벤트 → SQS 메시지 변환 (Anti-Corruption)
            SqsUploadMessage message = mapper.toSqsMessage(event);

            // 3. SQS 발행
            publishToSqs(message, event.getEventKey());

            // 4. 중복 방지 기록
            deduplicationService.markAsProcessed(idempotencyKey);

            // 5. 메트릭 수집
            metricsCollector.recordEventPublished(
                event.getClass().getSimpleName(),
                message.getMessageSize()
            );

            log.info("Published upload completed event: {} -> SQS",
                event.getSessionKey()
            );

        } catch (SdkException e) {
            handlePublishFailure(event, e);
        }
    }

    /**
     * 업로드 실패 이벤트 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Order(2)
    public void handleUploadFailed(UploadFailedEvent event) {
        try {
            // 실패 이벤트는 Dead Letter Queue로
            SqsFailureMessage message = mapper.toFailureMessage(event);

            if (event.isRetryable()) {
                // 재시도 가능한 경우 일반 큐로
                publishToSqs(message, event.getSessionKey());
            } else {
                // 재시도 불가능한 경우 DLQ로
                publishToDeadLetterQueue(message);
            }

            log.info("Published upload failed event: {}", event.getSessionKey());

        } catch (Exception e) {
            // 실패 이벤트 발행 실패는 로깅만
            log.error("Failed to publish failure event: {}", event, e);
        }
    }

    /**
     * 업로드 만료 이벤트 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Order(3)
    public void handleUploadExpired(UploadExpiredEvent event) {
        try {
            // 정리 작업 큐로 발행
            SqsCleanupMessage message = mapper.toCleanupMessage(event);
            publishToCleanupQueue(message);

            log.info("Published cleanup request for expired session: {}",
                event.getSessionKey()
            );

        } catch (Exception e) {
            log.error("Failed to publish cleanup event: {}", event, e);
        }
    }

    /**
     * 메트릭 이벤트 처리 (비동기)
     */
    @Async("metricsExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUploadMetric(UploadMetricEvent event) {
        metricsCollector.recordUploadMetrics(
            event.getTenantId(),
            event.getUploadType(),
            event.getProcessingTimeMillis(),
            event.getThroughputMBps()
        );
    }

    private void publishToSqs(SqsMessage message, String messageGroupId) {
        SendMessageRequest request = SendMessageRequest.builder()
            .queueUrl(uploadEventQueueUrl)
            .messageBody(message.toJson())
            .messageGroupId(messageGroupId)  // FIFO 큐용
            .messageDeduplicationId(message.getDeduplicationId())
            .build();

        SendMessageResponse response = sqsClient.sendMessage(request);
        log.debug("Message published to SQS: {}", response.messageId());
    }

    private void publishToDeadLetterQueue(SqsMessage message) {
        // DLQ로 직접 발행
        String dlqUrl = uploadEventQueueUrl + "-dlq";
        SendMessageRequest request = SendMessageRequest.builder()
            .queueUrl(dlqUrl)
            .messageBody(message.toJson())
            .build();

        sqsClient.sendMessage(request);
    }

    private void publishToCleanupQueue(SqsCleanupMessage message) {
        // 정리 작업 큐로 발행
        String cleanupQueueUrl = uploadEventQueueUrl.replace("events", "cleanup");
        SendMessageRequest request = SendMessageRequest.builder()
            .queueUrl(cleanupQueueUrl)
            .messageBody(message.toJson())
            .delaySeconds(300)  // 5분 지연
            .build();

        sqsClient.sendMessage(request);
    }

    private void handlePublishFailure(Object event, Exception e) {
        log.error("Failed to publish event: {}", event, e);

        // 알림 시스템으로 전달
        alertingService.notifyEventPublishFailure(
            event.getClass().getSimpleName(),
            e.getMessage()
        );

        // 재시도 큐에 추가 (별도 처리)
        retryQueueService.enqueue(event);

        // 메트릭 기록
        metricsCollector.recordEventPublishFailure(
            event.getClass().getSimpleName()
        );
    }
}
```

### ⚠️ 코딩 컨벤션 체크포인트
- ✅ **@TransactionalEventListener**: AFTER_COMMIT 페이즈
- ✅ **Anti-Corruption Layer**: 도메인 → 외부 변환
- ✅ **멱등성 처리**: 중복 이벤트 방지
- ✅ **실패 처리**: DLQ, 알림, 재시도

---

## KAN-330: IdempotencyMiddleware 구현

### 📌 작업 내용
```java
// 위치: adapter-in/rest-api/src/main/java/com/ryuqq/fileflow/adapter/rest/middleware/IdempotencyMiddleware.java

/**
 * 멱등성 보장 미들웨어
 * 중복 요청 방지 및 응답 캐싱
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)  // 가장 먼저 실행
@Slf4j
@RequiredArgsConstructor
public class IdempotencyMiddleware {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final IdempotencyConfiguration config;

    /**
     * @Idempotent 어노테이션 처리
     */
    @Around("@annotation(idempotent)")
    public Object handleIdempotency(
        ProceedingJoinPoint joinPoint,
        Idempotent idempotent
    ) throws Throwable {

        // 1. HTTP 요청에서 멱등성 키 추출
        HttpServletRequest request = getCurrentHttpRequest();
        String idempotencyKey = extractIdempotencyKey(request);

        if (idempotencyKey == null) {
            // 멱등성 키가 없으면 일반 처리
            return joinPoint.proceed();
        }

        // 2. 캐시 키 생성
        String cacheKey = buildCacheKey(idempotencyKey, request);
        String lockKey = cacheKey + ":lock";
        String statusKey = cacheKey + ":status";

        // 3. 이미 처리된 요청인지 확인
        CachedResponse cached = getCachedResponse(cacheKey);
        if (cached != null) {
            log.info("Idempotent cache hit: key={}, status={}",
                idempotencyKey, cached.getStatus()
            );
            return buildResponseFromCache(cached);
        }

        // 4. 진행 중인 요청인지 확인
        String status = redisTemplate.opsForValue().get(statusKey);
        if ("PROCESSING".equals(status)) {
            // 동일한 요청이 처리 중
            return waitForProcessingOrTimeout(cacheKey, idempotent.waitTimeout());
        }

        // 5. 분산 락 획득
        boolean lockAcquired = acquireDistributedLock(
            lockKey, idempotent.lockTimeout()
        );

        if (!lockAcquired) {
            throw new ConcurrentRequestException(
                "Failed to acquire lock for idempotency key: " + idempotencyKey
            );
        }

        try {
            // 6. 처리 중 상태 설정
            setProcessingStatus(statusKey);

            // 7. 실제 처리
            Object result = joinPoint.proceed();

            // 8. 결과 캐싱
            cacheSuccessfulResponse(cacheKey, result, idempotent.ttl());

            return result;

        } catch (Exception e) {
            // 실패도 캐싱 (재시도 방지)
            if (idempotent.cacheFailures()) {
                cacheFailedResponse(cacheKey, e, idempotent.failureTtl());
            }
            throw e;

        } finally {
            // 9. 락 해제 및 상태 정리
            releaseDistributedLock(lockKey);
            clearProcessingStatus(statusKey);
        }
    }

    private String extractIdempotencyKey(HttpServletRequest request) {
        // Header에서 추출
        String key = request.getHeader("X-Idempotency-Key");
        if (key != null) {
            return key;
        }

        // Query Parameter에서 추출
        key = request.getParameter("idempotencyKey");
        if (key != null) {
            return key;
        }

        return null;
    }

    private String buildCacheKey(String idempotencyKey, HttpServletRequest request) {
        // 멱등성 키 + 메서드 + 경로로 캐시 키 생성
        return String.format("idempotency:%s:%s:%s",
            idempotencyKey,
            request.getMethod(),
            request.getRequestURI()
        );
    }

    private boolean acquireDistributedLock(String lockKey, long timeoutMillis) {
        String lockValue = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue()
            .setIfAbsent(
                lockKey,
                lockValue,
                Duration.ofMillis(timeoutMillis)
            );

        if (Boolean.TRUE.equals(acquired)) {
            // ThreadLocal에 락 값 저장 (해제 시 검증용)
            LockContext.setLockValue(lockKey, lockValue);
            return true;
        }

        return false;
    }

    private void releaseDistributedLock(String lockKey) {
        String expectedValue = LockContext.getLockValue(lockKey);
        if (expectedValue == null) {
            return;
        }

        // Lua 스크립트로 원자적 해제 (본인 락만 해제)
        String script = """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            else
                return 0
            end
            """;

        redisTemplate.execute(
            new DefaultRedisScript<>(script, Long.class),
            Collections.singletonList(lockKey),
            expectedValue
        );

        LockContext.removeLockValue(lockKey);
    }

    private void setProcessingStatus(String statusKey) {
        redisTemplate.opsForValue().set(
            statusKey,
            "PROCESSING",
            Duration.ofSeconds(30)
        );
    }

    private void clearProcessingStatus(String statusKey) {
        redisTemplate.delete(statusKey);
    }

    private CachedResponse getCachedResponse(String cacheKey) {
        String json = redisTemplate.opsForValue().get(cacheKey);
        if (json == null) {
            return null;
        }

        try {
            return objectMapper.readValue(json, CachedResponse.class);
        } catch (Exception e) {
            log.error("Failed to deserialize cached response", e);
            return null;
        }
    }

    private void cacheSuccessfulResponse(String cacheKey, Object result, long ttlMillis) {
        CachedResponse cached = new CachedResponse(
            200,
            "SUCCESS",
            serialize(result),
            LocalDateTime.now()
        );

        String json = serialize(cached);
        redisTemplate.opsForValue().set(
            cacheKey,
            json,
            Duration.ofMillis(ttlMillis)
        );
    }

    private void cacheFailedResponse(String cacheKey, Exception e, long ttlMillis) {
        CachedResponse cached = new CachedResponse(
            determineErrorCode(e),
            "FAILED",
            e.getMessage(),
            LocalDateTime.now()
        );

        String json = serialize(cached);
        redisTemplate.opsForValue().set(
            cacheKey,
            json,
            Duration.ofMillis(ttlMillis)
        );
    }

    private Object waitForProcessingOrTimeout(String cacheKey, long timeoutMillis) {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            // 100ms 간격으로 체크
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting", e);
            }

            CachedResponse cached = getCachedResponse(cacheKey);
            if (cached != null) {
                return buildResponseFromCache(cached);
            }
        }

        throw new TimeoutException(
            "Timeout waiting for idempotent request to complete"
        );
    }

    // ThreadLocal for Lock Context
    private static class LockContext {
        private static final ThreadLocal<Map<String, String>> lockValues =
            ThreadLocal.withInitial(HashMap::new);

        static void setLockValue(String key, String value) {
            lockValues.get().put(key, value);
        }

        static String getLockValue(String key) {
            return lockValues.get().get(key);
        }

        static void removeLockValue(String key) {
            lockValues.get().remove(key);
        }
    }
}

/**
 * 멱등성 어노테이션
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {
    /**
     * 캐시 TTL (밀리초)
     */
    long ttl() default 86400000L;  // 24시간

    /**
     * 실패 응답 캐시 TTL (밀리초)
     */
    long failureTtl() default 60000L;  // 1분

    /**
     * 락 타임아웃 (밀리초)
     */
    long lockTimeout() default 10000L;  // 10초

    /**
     * 대기 타임아웃 (밀리초)
     */
    long waitTimeout() default 5000L;  // 5초

    /**
     * 실패 응답도 캐싱할지 여부
     */
    boolean cacheFailures() default true;
}

/**
 * 캐시된 응답
 */
@Data
class CachedResponse {
    private final int statusCode;
    private final String status;
    private final String body;
    private final LocalDateTime cachedAt;
}
```

### ⚠️ 코딩 컨벤션 체크포인트
- ✅ **분산 락**: Redis 기반 원자적 락
- ✅ **Lua 스크립트**: 원자적 연산 보장
- ✅ **ThreadLocal**: 락 컨텍스트 관리
- ✅ **타임아웃 처리**: 대기 시간 제한

---

## 통합 테스트 가이드

### 이벤트 발행 테스트
```java
@SpringBootTest
@AutoConfigureMockMvc
@TestContainers
class EventPublishingIntegrationTest {

    @Container
    static LocalStackContainer localStack = new LocalStackContainer()
        .withServices(S3, SQS);

    @Autowired
    private UploadSessionPort uploadSessionPort;

    @SpyBean
    private UploadEventPublisher eventPublisher;

    @Test
    @DisplayName("업로드 완료 시 이벤트 발행")
    void should_publish_event_on_upload_completion() {
        // given
        UploadSession session = UploadSession.createForSingleUpload(1L, "test.pdf", 1024L);
        session = uploadSessionPort.save(session);

        // when
        session.complete("etag123", 100L);
        uploadSessionPort.save(session);  // 이벤트 발행

        // then
        verify(eventPublisher, timeout(1000))
            .handleUploadCompleted(any(UploadCompletedEvent.class));
    }

    @Test
    @DisplayName("멱등성 보장 테스트")
    void should_handle_duplicate_requests() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();

        // 첫 번째 요청
        MvcResult first = mockMvc.perform(
            post("/api/v1/uploads")
                .header("X-Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(uploadRequestJson)
        ).andReturn();

        // 두 번째 요청 (동일 키)
        MvcResult second = mockMvc.perform(
            post("/api/v1/uploads")
                .header("X-Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(uploadRequestJson)
        ).andReturn();

        // 동일한 응답
        assertThat(first.getResponse().getContentAsString())
            .isEqualTo(second.getResponse().getContentAsString());
    }
}
```

---

## 📊 전체 구현 체크리스트

### Zero-Tolerance 규칙
- [ ] **NO Lombok**: 모든 코드에서 Lombok 미사용
- [ ] **Law of Demeter**: Getter 체이닝 없음
- [ ] **Long FK Strategy**: JPA 관계 어노테이션 미사용
- [ ] **트랜잭션 경계**: 외부 API 호출 분리
- [ ] **Javadoc**: 모든 public 요소 문서화

### Phase별 완료 기준
- [ ] **Phase 2A**: Multipart Upload 10개 태스크 완료
- [ ] **Phase 2B**: External Download & Policy 6개 태스크 완료
- [ ] **Phase 2C**: Event & Integration 10개 태스크 완료

### 테스트 커버리지
- [ ] **Domain Layer**: 90% 이상
- [ ] **Application Layer**: 80% 이상
- [ ] **Adapter Layer**: 70% 이상

### 문서화
- [ ] 각 태스크별 구현 가이드 작성
- [ ] API 문서 (OpenAPI) 작성
- [ ] 통합 테스트 시나리오 문서화

---

## 🎯 다음 단계

1. 각 Phase별 구현 진행
2. 코드 리뷰 및 피드백 반영
3. 통합 테스트 실행
4. 성능 테스트 및 최적화
5. 배포 준비

모든 구현은 FileFlow 프로젝트의 코딩 컨벤션을 엄격히 준수하며, 특히 Zero-Tolerance 규칙은 예외 없이 적용되어야 합니다.