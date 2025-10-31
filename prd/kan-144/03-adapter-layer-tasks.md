# KAN-144: Adapter Layer 개발 태스크

## 📋 Adapter Layer 개요

**레이어 역할**: 외부 시스템(DB, API, Event) 연동
**패키지**:
- Inbound: `adapter-in/rest-api`
- Outbound: `adapter-out/{persistence-mysql, redis, event}`

**핵심 원칙**: Long FK Strategy, Anti-Corruption Layer, Port 구현

---

## 🎯 Adapter Layer 태스크 목록

### Adapter-Out (Persistence) - Phase 2A (2 Tasks)

#### KAN-313: MultipartUploadJpaAdapter 구현

**위치**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/fileflow/adapter/out/persistence/upload/`

**목표**: MultipartUpload 영속성 계층 구현

**파일 구조**:
```
persistence/upload/
├── entity/
│   ├── MultipartUploadEntity.java
│   └── UploadPartEntity.java
├── repository/
│   ├── MultipartUploadJpaRepository.java
│   └── UploadPartJpaRepository.java
├── mapper/
│   └── MultipartUploadMapper.java
└── MultipartUploadJpaAdapter.java
```

**구현 상세**:

```java
// Entity
/**
 * Multipart Upload JPA Entity
 * ⭐ Long FK Strategy 적용 (NO @ManyToOne)
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Entity
@Table(name = "upload_multipart")
public class MultipartUploadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ❌ 금지: @ManyToOne 사용 안함!
    // ✅ Long FK Strategy
    @Column(name = "upload_session_id", nullable = false)
    private Long uploadSessionId;

    @Column(name = "provider_upload_id", length = 500)
    private String providerUploadId;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MultipartStatus status;

    @Column(name = "total_parts")
    private Integer totalParts;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "aborted_at")
    private LocalDateTime abortedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // JPA Lifecycle
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getter/Setter (NO Lombok!)
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

    public String getProviderUploadId() {
        return providerUploadId;
    }

    public void setProviderUploadId(String providerUploadId) {
        this.providerUploadId = providerUploadId;
    }

    public MultipartStatus getStatus() {
        return status;
    }

    public void setStatus(MultipartStatus status) {
        this.status = status;
    }

    public Integer getTotalParts() {
        return totalParts;
    }

    public void setTotalParts(Integer totalParts) {
        this.totalParts = totalParts;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getAbortedAt() {
        return abortedAt;
    }

    public void setAbortedAt(LocalDateTime abortedAt) {
        this.abortedAt = abortedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

/**
 * Upload Part JPA Entity
 * ⭐ Long FK Strategy
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Entity
@Table(name = "upload_part")
public class UploadPartEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ❌ 금지: @ManyToOne
    @Column(name = "multipart_upload_id", nullable = false)
    private Long multipartUploadId;

    @Column(name = "part_number", nullable = false)
    private Integer partNumber;

    @Column(name = "etag", nullable = false, length = 255)
    private String etag;

    @Column(name = "size", nullable = false)
    private Long size;

    @Column(name = "checksum", length = 255)
    private String checksum;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getter/Setter (생략, 위와 동일)
}

// Repository
/**
 * Multipart Upload JPA Repository
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Repository
public interface MultipartUploadJpaRepository
    extends JpaRepository<MultipartUploadEntity, Long> {

    /**
     * 업로드 세션 ID로 조회
     */
    Optional<MultipartUploadEntity> findByUploadSessionId(Long uploadSessionId);

    /**
     * 상태별 조회
     */
    List<MultipartUploadEntity> findByStatus(MultipartStatus status);

    /**
     * 세션 ID 목록으로 조회
     */
    @Query("SELECT m FROM MultipartUploadEntity m " +
           "WHERE m.uploadSessionId IN :sessionIds")
    List<MultipartUploadEntity> findByUploadSessionIds(
        @Param("sessionIds") List<Long> sessionIds
    );
}

/**
 * Upload Part JPA Repository
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Repository
public interface UploadPartJpaRepository
    extends JpaRepository<UploadPartEntity, Long> {

    /**
     * Multipart Upload ID로 조회
     */
    List<UploadPartEntity> findByMultipartUploadId(Long multipartUploadId);

    /**
     * Multipart Upload ID와 파트 번호로 조회
     */
    Optional<UploadPartEntity> findByMultipartUploadIdAndPartNumber(
        Long multipartUploadId,
        Integer partNumber
    );

    /**
     * Multipart Upload ID로 삭제
     */
    @Modifying
    @Query("DELETE FROM UploadPartEntity p " +
           "WHERE p.multipartUploadId = :multipartUploadId")
    void deleteByMultipartUploadId(@Param("multipartUploadId") Long multipartUploadId);
}

// Mapper
/**
 * Multipart Upload Mapper
 * Domain ↔ Entity 변환
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class MultipartUploadMapper {

    /**
     * Domain → Entity
     */
    public MultipartUploadEntity toEntity(MultipartUpload multipart) {
        MultipartUploadEntity entity = new MultipartUploadEntity();

        entity.setId(multipart.getId());
        entity.setUploadSessionId(multipart.getUploadSessionId());
        entity.setProviderUploadId(multipart.getProviderUploadId());
        entity.setStatus(multipart.getStatus());
        entity.setTotalParts(multipart.getTotalParts());
        entity.setStartedAt(multipart.getStartedAt());
        entity.setCompletedAt(multipart.getCompletedAt());

        return entity;
    }

    /**
     * Entity → Domain
     */
    public MultipartUpload toDomain(
        MultipartUploadEntity entity,
        List<UploadPart> parts
    ) {
        // Domain Aggregate 재구성
        // ⚠️ 주의: Reflection 사용 (불가피한 경우에만)
        // 또는 Domain에 reconstitute() 메서드 제공

        return MultipartUpload.reconstitute(
            entity.getId(),
            entity.getUploadSessionId(),
            entity.getProviderUploadId(),
            entity.getStatus(),
            entity.getTotalParts(),
            parts,
            entity.getStartedAt(),
            entity.getCompletedAt()
        );
    }

    /**
     * UploadPart Domain → Entity
     */
    public UploadPartEntity toEntity(UploadPart part, Long multipartUploadId) {
        UploadPartEntity entity = new UploadPartEntity();

        entity.setMultipartUploadId(multipartUploadId);
        entity.setPartNumber(part.getPartNumber());
        entity.setEtag(part.getEtag());
        entity.setSize(part.getSize());
        entity.setChecksum(part.getChecksum());
        entity.setUploadedAt(part.getUploadedAt());

        return entity;
    }

    /**
     * UploadPart Entity → Domain
     */
    public UploadPart toDomain(UploadPartEntity entity) {
        return UploadPart.of(
            entity.getPartNumber(),
            entity.getEtag(),
            entity.getSize(),
            entity.getChecksum()
        );
    }
}

// Adapter
/**
 * Multipart Upload JPA Adapter
 * Port 구현체
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class MultipartUploadJpaAdapter implements MultipartUploadPort {

    private final MultipartUploadJpaRepository multipartRepository;
    private final UploadPartJpaRepository partRepository;
    private final MultipartUploadMapper mapper;

    @Override
    @Transactional
    public MultipartUpload save(MultipartUpload multipart) {
        // 1. Multipart Entity 저장
        MultipartUploadEntity entity = mapper.toEntity(multipart);
        MultipartUploadEntity saved = multipartRepository.save(entity);

        // 2. Parts 저장
        if (multipart.getUploadedParts() != null &&
            !multipart.getUploadedParts().isEmpty()) {
            saveParts(saved.getId(), multipart.getUploadedParts());
        }

        // 3. Domain 재구성
        List<UploadPart> parts = loadParts(saved.getId());
        return mapper.toDomain(saved, parts);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MultipartUpload> findById(Long id) {
        return multipartRepository.findById(id)
            .map(entity -> mapper.toDomain(
                entity,
                loadParts(entity.getId())
            ));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MultipartUpload> findByUploadSessionId(Long sessionId) {
        return multipartRepository.findByUploadSessionId(sessionId)
            .map(entity -> mapper.toDomain(
                entity,
                loadParts(entity.getId())
            ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MultipartUpload> findByStatus(MultipartStatus status) {
        return multipartRepository.findByStatus(status)
            .stream()
            .map(entity -> mapper.toDomain(
                entity,
                loadParts(entity.getId())
            ))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // Parts 먼저 삭제
        partRepository.deleteByMultipartUploadId(id);
        // Multipart 삭제
        multipartRepository.deleteById(id);
    }

    // ===== Private 헬퍼 메서드 =====

    private void saveParts(Long multipartId, List<UploadPart> parts) {
        // 기존 파트 삭제 (교체)
        partRepository.deleteByMultipartUploadId(multipartId);

        // 새로운 파트 저장
        List<UploadPartEntity> entities = parts.stream()
            .map(part -> mapper.toEntity(part, multipartId))
            .collect(Collectors.toList());

        partRepository.saveAll(entities);
    }

    private List<UploadPart> loadParts(Long multipartId) {
        return partRepository.findByMultipartUploadId(multipartId)
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
}
```

**Zero-Tolerance 체크리스트**:
- [ ] ⭐ Long FK Strategy (NO @ManyToOne, @OneToMany)
- [ ] NO Lombok (Entity getter/setter 수동)
- [ ] 명시적 Mapper (Domain ↔ Entity)
- [ ] @Transactional 적절히 사용
- [ ] Javadoc 작성

---

#### KAN-314: UploadSessionJpaAdapter 확장

**위치**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/fileflow/adapter/out/persistence/upload/`

**목표**: 기존 UploadSessionJpaAdapter에 Multipart 관련 기능 추가

**구현 상세**:

```java
/**
 * Upload Session JPA Adapter (확장)
 * Multipart 지원 추가
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class UploadSessionJpaAdapter implements UploadSessionPort {

    private final UploadSessionJpaRepository sessionRepository;
    private final MultipartUploadJpaRepository multipartRepository;
    private final UploadSessionMapper mapper;
    private final MultipartUploadMapper multipartMapper;

    @Override
    @Transactional
    public UploadSession save(UploadSession session) {
        // 1. Session Entity 저장
        UploadSessionEntity entity = mapper.toEntity(session);
        UploadSessionEntity saved = sessionRepository.save(entity);

        // 2. Multipart 정보도 함께 저장
        if (session.isMultipart() && session.getMultipartUpload() != null) {
            MultipartUpload multipart = session.getMultipartUpload();
            MultipartUploadEntity multipartEntity =
                multipartMapper.toEntity(multipart);
            multipartEntity.setUploadSessionId(saved.getId());
            multipartRepository.save(multipartEntity);
        }

        // 3. Domain 재구성
        return loadSession(saved.getId()).orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UploadSession> findById(Long id) {
        return loadSession(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UploadSession> findBySessionKey(String sessionKey) {
        return sessionRepository.findBySessionKey(sessionKey)
            .flatMap(entity -> loadSession(entity.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UploadSession> findByStatusAndCreatedBefore(
        UploadStatus status,
        LocalDateTime createdBefore
    ) {
        return sessionRepository.findByStatusAndCreatedAtBefore(status, createdBefore)
            .stream()
            .map(entity -> loadSession(entity.getId()).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    // ===== Private 헬퍼 =====

    /**
     * Session + Multipart 함께 로드
     */
    private Optional<UploadSession> loadSession(Long id) {
        return sessionRepository.findById(id)
            .map(entity -> {
                UploadSession session = mapper.toDomain(entity);

                // Multipart 정보 로드 (있는 경우)
                if (session.isMultipart()) {
                    multipartRepository.findByUploadSessionId(id)
                        .ifPresent(multipartEntity -> {
                            MultipartUpload multipart =
                                multipartMapper.toDomain(
                                    multipartEntity,
                                    List.of()  // Parts는 별도 로드
                                );
                            session.attachMultipart(multipart);
                        });
                }

                return session;
            });
    }
}
```

**Zero-Tolerance 체크리스트**:
- [ ] Long FK 유지
- [ ] Multipart 정보 함께 저장/로드
- [ ] 트랜잭션 적절히 사용
- [ ] Javadoc 작성

---

### Adapter-Out (Event) - Phase 2C (2 Tasks)

#### KAN-328: UploadEventPublisher 구현

**위치**: `adapter-out/event/src/main/java/com/ryuqq/fileflow/adapter/out/event/`

**목표**: 도메인 이벤트를 SQS로 발행 (Anti-Corruption Layer)

**구현 상세**:

```java
/**
 * Upload Event Publisher
 * Domain Event를 외부 시스템(SQS)으로 전달
 * Anti-Corruption Layer 역할
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class UploadEventPublisher {

    private final SqsTemplate sqsTemplate;
    private final UploadEventMapper mapper;
    private final EventDeduplicationService deduplicationService;

    /**
     * 업로드 완료 이벤트 처리
     * ⭐ 트랜잭션 커밋 후 실행
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @EventListener
    public void handleUploadCompleted(UploadCompletedEvent event) {
        // 1. 중복 체크 (멱등성)
        if (deduplicationService.isDuplicate(event)) {
            log.warn("Duplicate event detected: {}", event.getSessionKey());
            return;
        }

        try {
            // 2. Domain Event → SQS Message 변환
            SqsUploadMessage message = mapper.toSqsMessage(event);

            // 3. SQS 발행
            sqsTemplate.send(to -> to
                .queue("upload-completed-queue")
                .payload(message)
            );

            // 4. 발행 성공 기록
            deduplicationService.markAsProcessed(event);

            log.info("Published upload completed: {}", event.getSessionKey());

        } catch (SqsException e) {
            log.error("Failed to publish event: {}", event, e);
            // 알림 시스템으로 전달
        }
    }

    /**
     * 업로드 실패 이벤트 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUploadFailed(UploadFailedEvent event) {
        try {
            SqsUploadFailedMessage message = mapper.toFailureMessage(event);

            sqsTemplate.send(to -> to
                .queue("upload-failed-queue")
                .payload(message)
            );

        } catch (Exception e) {
            log.error("Failed to publish failure event: {}", event, e);
        }
    }

    /**
     * 업로드 만료 이벤트 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUploadExpired(UploadExpiredEvent event) {
        try {
            SqsCleanupMessage message = new SqsCleanupMessage(
                event.getUploadSessionId(),
                "EXPIRED"
            );

            sqsTemplate.send(to -> to
                .queue("upload-cleanup-queue")
                .payload(message)
            );

        } catch (Exception e) {
            log.error("Failed to publish expired event: {}", event, e);
        }
    }

    /**
     * 업로드 중단 이벤트 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUploadAborted(UploadAbortedEvent event) {
        try {
            SqsCleanupMessage message = new SqsCleanupMessage(
                event.getUploadSessionId(),
                "ABORTED"
            );

            sqsTemplate.send(to -> to
                .queue("upload-cleanup-queue")
                .payload(message)
            );

        } catch (Exception e) {
            log.error("Failed to publish aborted event: {}", event, e);
        }
    }
}

/**
 * Event Deduplication Service
 * 중복 이벤트 방지 (Redis 활용)
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class EventDeduplicationService {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 중복 이벤트 체크
     */
    public boolean isDuplicate(UploadCompletedEvent event) {
        String key = buildEventKey(event);
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }

    /**
     * 처리 완료 마킹
     */
    public void markAsProcessed(UploadCompletedEvent event) {
        String key = buildEventKey(event);
        // 24시간 보관
        redisTemplate.opsForValue().set(
            key,
            event.getOccurredAt().toString(),
            Duration.ofHours(24)
        );
    }

    private String buildEventKey(UploadCompletedEvent event) {
        return String.format(
            "event:upload:completed:%d:%s",
            event.getUploadSessionId(),
            event.getOccurredAt().toEpochSecond(ZoneOffset.UTC)
        );
    }
}
```

**Zero-Tolerance 체크리스트**:
- [ ] ⭐ @TransactionalEventListener (커밋 후 실행)
- [ ] Anti-Corruption Layer (Domain → SQS)
- [ ] 멱등성 보장 (중복 체크)
- [ ] 실패 처리 (로깅 + 알림)
- [ ] Javadoc 작성

---

#### KAN-329: UploadEventMapper 구현

**위치**: `adapter-out/event/src/main/java/com/ryuqq/fileflow/adapter/out/event/`

**목표**: Domain Event와 외부 메시지 간 변환

**구현 상세**:

```java
/**
 * Upload Event Mapper
 * Domain Event → SQS Message 변환
 * Anti-Corruption Layer의 핵심
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class UploadEventMapper {

    /**
     * UploadCompletedEvent → SQS Message
     */
    public SqsUploadMessage toSqsMessage(UploadCompletedEvent event) {
        SqsUploadMessage message = new SqsUploadMessage();

        message.setMessageType("UPLOAD_COMPLETED");
        message.setSessionId(event.getUploadSessionId());
        message.setSessionKey(event.getSessionKey());
        message.setFileId(event.getFileId());
        message.setCompletedAt(event.getCompletedAt());
        message.setTimestamp(event.getOccurredAt());

        return message;
    }

    /**
     * UploadFailedEvent → SQS Message
     */
    public SqsUploadFailedMessage toFailureMessage(UploadFailedEvent event) {
        SqsUploadFailedMessage message = new SqsUploadFailedMessage();

        message.setMessageType("UPLOAD_FAILED");
        message.setSessionId(event.getUploadSessionId());
        message.setSessionKey(event.getSessionKey());
        message.setFailureReason(event.getFailureReason());
        message.setTimestamp(event.getOccurredAt());

        return message;
    }
}

/**
 * SQS Upload Message (외부 시스템 형식)
 */
public class SqsUploadMessage {
    private String messageType;
    private Long sessionId;
    private String sessionKey;
    private Long fileId;
    private LocalDateTime completedAt;
    private LocalDateTime timestamp;

    // Getter/Setter (NO Lombok, JSON 직렬화용)
}

/**
 * SQS Upload Failed Message
 */
public class SqsUploadFailedMessage {
    private String messageType;
    private Long sessionId;
    private String sessionKey;
    private String failureReason;
    private LocalDateTime timestamp;

    // Getter/Setter
}

/**
 * SQS Cleanup Message
 */
public class SqsCleanupMessage {
    private Long sessionId;
    private String cleanupType;  // EXPIRED, ABORTED

    public SqsCleanupMessage(Long sessionId, String cleanupType) {
        this.sessionId = sessionId;
        this.cleanupType = cleanupType;
    }

    // Getter/Setter
}
```

**Zero-Tolerance 체크리스트**:
- [ ] Anti-Corruption Layer (명시적 변환)
- [ ] 외부 메시지 형식 캡슐화
- [ ] Javadoc 작성

---

### Adapter-In (REST API) - Phase 2A & 2B (3 Tasks)

#### KAN-319: UploadController 확장 (Multipart 엔드포인트 4개)

**위치**: `adapter-in/rest-api/src/main/java/com/ryuqq/fileflow/adapter/rest/upload/`

**목표**: Multipart Upload REST API 엔드포인트 구현

**파일 구조**:
```
rest/upload/
├── request/
│   ├── InitMultipartRequest.java
│   ├── MarkPartUploadedRequest.java
│   └── ...
├── response/
│   ├── InitMultipartApiResponse.java
│   ├── PartPresignedUrlApiResponse.java
│   └── ...
├── mapper/
│   └── UploadApiMapper.java
└── UploadController.java
```

**구현 상세**:

```java
/**
 * Upload Controller (확장)
 * Multipart Upload REST API 엔드포인트
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
public class UploadController {

    // 기존 UseCase들...
    private final InitMultipartUploadUseCase initMultipartUseCase;
    private final GeneratePartPresignedUrlUseCase generatePartUrlUseCase;
    private final MarkPartUploadedUseCase markPartUploadedUseCase;
    private final CompleteMultipartUploadUseCase completeMultipartUseCase;

    private final UploadApiMapper mapper;

    /**
     * Multipart 업로드 초기화
     *
     * POST /api/v1/uploads/multipart/init
     */
    @PostMapping("/multipart/init")
    @Idempotent  // 멱등성 보장
    public ResponseEntity<ApiResponse<InitMultipartApiResponse>> initMultipart(
        @Valid @RequestBody InitMultipartRequest request,
        @RequestHeader("X-Tenant-Id") Long tenantId,
        @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey
    ) {
        // 1. Command 생성
        InitMultipartCommand command = mapper.toCommand(request, tenantId);

        // 2. UseCase 실행
        InitMultipartResponse response = initMultipartUseCase.execute(command);

        // 3. API Response 변환
        InitMultipartApiResponse apiResponse = mapper.toApiResponse(response);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(apiResponse));
    }

    /**
     * 파트 업로드 URL 생성
     *
     * POST /api/v1/uploads/multipart/{sessionKey}/parts/{partNumber}/url
     */
    @PostMapping("/multipart/{sessionKey}/parts/{partNumber}/url")
    public ResponseEntity<ApiResponse<PartPresignedUrlApiResponse>> generatePartUrl(
        @PathVariable String sessionKey,
        @PathVariable @Min(1) @Max(10000) Integer partNumber
    ) {
        // Command 생성
        GeneratePartUrlCommand command = new GeneratePartUrlCommand(
            sessionKey,
            partNumber
        );

        // UseCase 실행
        PartPresignedUrlResponse response = generatePartUrlUseCase.execute(command);

        // API Response 변환
        PartPresignedUrlApiResponse apiResponse = mapper.toApiResponse(response);

        return ResponseEntity.ok(ApiResponse.success(apiResponse));
    }

    /**
     * 파트 업로드 완료 통보
     *
     * PUT /api/v1/uploads/multipart/{sessionKey}/parts/{partNumber}
     */
    @PutMapping("/multipart/{sessionKey}/parts/{partNumber}")
    public ResponseEntity<ApiResponse<Void>> markPartUploaded(
        @PathVariable String sessionKey,
        @PathVariable @Min(1) @Max(10000) Integer partNumber,
        @Valid @RequestBody MarkPartUploadedRequest request
    ) {
        // Command 생성
        MarkPartUploadedCommand command = new MarkPartUploadedCommand(
            sessionKey,
            partNumber,
            request.getEtag(),
            request.getPartSize()
        );

        // UseCase 실행
        markPartUploadedUseCase.execute(command);

        return ResponseEntity
            .noContent()
            .build();
    }

    /**
     * Multipart 업로드 완료
     *
     * POST /api/v1/uploads/multipart/{sessionKey}/complete
     */
    @PostMapping("/multipart/{sessionKey}/complete")
    @Idempotent
    public ResponseEntity<ApiResponse<CompleteMultipartApiResponse>> completeMultipart(
        @PathVariable String sessionKey,
        @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey
    ) {
        // Command 생성
        CompleteMultipartCommand command = new CompleteMultipartCommand(sessionKey);

        // UseCase 실행
        CompleteMultipartResponse response = completeMultipartUseCase.execute(command);

        // API Response 변환
        CompleteMultipartApiResponse apiResponse = mapper.toApiResponse(response);

        return ResponseEntity.ok(ApiResponse.success(apiResponse));
    }
}

// Request DTOs
/**
 * Multipart 초기화 Request
 */
public class InitMultipartRequest {

    @NotBlank(message = "File name is required")
    @Size(max = 255)
    private String fileName;

    @NotNull(message = "File size is required")
    @Min(value = 1, message = "File size must be positive")
    private Long fileSize;

    @NotBlank(message = "Content type is required")
    private String contentType;

    private String checksum;  // Optional

    // Getter/Setter (NO Lombok)
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }
}

/**
 * 파트 업로드 완료 Request
 */
public class MarkPartUploadedRequest {

    @NotBlank(message = "ETag is required")
    private String etag;

    @NotNull(message = "Part size is required")
    @Min(value = 1)
    private Long partSize;

    // Getter/Setter
    public String getEtag() { return etag; }
    public void setEtag(String etag) { this.etag = etag; }

    public Long getPartSize() { return partSize; }
    public void setPartSize(Long partSize) { this.partSize = partSize; }
}

// Response DTOs
/**
 * Multipart 초기화 API Response
 */
public class InitMultipartApiResponse {
    private String sessionKey;
    private String uploadId;
    private Integer totalParts;
    private String storageKey;

    // Static Factory + Getter
}

/**
 * Part Presigned URL API Response
 */
public class PartPresignedUrlApiResponse {
    private Integer partNumber;
    private String presignedUrl;
    private Long expiresInSeconds;

    // Static Factory + Getter
}

/**
 * Multipart 완료 API Response
 */
public class CompleteMultipartApiResponse {
    private Long fileId;
    private String etag;
    private String location;

    // Static Factory + Getter
}

// Mapper
/**
 * Upload API Mapper
 * Request → Command, Response → API Response 변환
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class UploadApiMapper {

    /**
     * Request → Command
     */
    public InitMultipartCommand toCommand(
        InitMultipartRequest request,
        Long tenantId
    ) {
        return InitMultipartCommand.of(
            tenantId,
            request.getFileName(),
            request.getFileSize(),
            request.getContentType()
        );
    }

    /**
     * UseCase Response → API Response
     */
    public InitMultipartApiResponse toApiResponse(InitMultipartResponse response) {
        return InitMultipartApiResponse.of(
            response.getSessionKey(),
            response.getUploadId(),
            response.getTotalParts(),
            response.getStorageKey()
        );
    }

    public PartPresignedUrlApiResponse toApiResponse(PartPresignedUrlResponse response) {
        return PartPresignedUrlApiResponse.of(
            response.getPartNumber(),
            response.getPresignedUrl(),
            response.getExpiresIn().getSeconds()
        );
    }

    public CompleteMultipartApiResponse toApiResponse(CompleteMultipartResponse response) {
        return CompleteMultipartApiResponse.of(
            response.getFileId(),
            response.getEtag(),
            response.getLocation()
        );
    }
}

// Global API Response Wrapper
/**
 * API Response Wrapper
 * 모든 API 응답의 표준 형식
 */
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ApiError error;
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.timestamp = LocalDateTime.now();
        return response;
    }

    public static <T> ApiResponse<T> error(ApiError error) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.error = error;
        response.timestamp = LocalDateTime.now();
        return response;
    }

    // Getter/Setter
}
```

**Zero-Tolerance 체크리스트**:
- [ ] Request/Response DTO (NO Lombok)
- [ ] Validation 어노테이션 (@NotNull, @Size 등)
- [ ] ApiMapper 명시적 구현
- [ ] 멱등성 보장 (@Idempotent)
- [ ] 에러 응답 처리 (GlobalExceptionHandler)
- [ ] OpenAPI 문서화 (@Operation)
- [ ] Javadoc 작성

---

#### KAN-325: ExternalDownloadController 구현

**위치**: `adapter-in/rest-api/src/main/java/com/ryuqq/fileflow/adapter/rest/download/`

**목표**: External Download REST API 엔드포인트

**구현 상세**:

```java
/**
 * External Download Controller
 * 외부 URL 다운로드 REST API
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/downloads")
@RequiredArgsConstructor
public class ExternalDownloadController {

    private final StartExternalDownloadUseCase startDownloadUseCase;
    private final GetDownloadStatusUseCase getStatusUseCase;

    /**
     * 외부 URL 다운로드 시작
     *
     * POST /api/v1/downloads/external
     */
    @PostMapping("/external")
    public ResponseEntity<ApiResponse<StartDownloadApiResponse>> startDownload(
        @Valid @RequestBody StartDownloadRequest request,
        @RequestHeader("X-Tenant-Id") Long tenantId
    ) {
        StartDownloadCommand command = new StartDownloadCommand(
            tenantId,
            request.getSourceUrl()
        );

        StartDownloadResponse response = startDownloadUseCase.execute(command);

        StartDownloadApiResponse apiResponse = StartDownloadApiResponse.of(
            response.getSessionKey(),
            response.getDownloadId()
        );

        return ResponseEntity
            .accepted()
            .body(ApiResponse.success(apiResponse));
    }

    /**
     * 다운로드 진행 상태 조회
     *
     * GET /api/v1/downloads/external/{downloadId}/status
     */
    @GetMapping("/external/{downloadId}/status")
    public ResponseEntity<ApiResponse<DownloadStatusApiResponse>> getStatus(
        @PathVariable Long downloadId
    ) {
        DownloadStatusResponse response = getStatusUseCase.execute(downloadId);

        DownloadStatusApiResponse apiResponse = DownloadStatusApiResponse.of(
            response.getStatus(),
            response.getProgressPercentage(),
            response.getBytesTransferred(),
            response.getTotalBytes()
        );

        return ResponseEntity.ok(ApiResponse.success(apiResponse));
    }
}

// Request/Response DTOs
public class StartDownloadRequest {
    @NotBlank(message = "Source URL is required")
    @Pattern(regexp = "^https?://.*", message = "Must be HTTP/HTTPS URL")
    private String sourceUrl;

    // Getter/Setter
}

public class StartDownloadApiResponse {
    private String sessionKey;
    private Long downloadId;

    // Static Factory + Getter
}

public class DownloadStatusApiResponse {
    private String status;
    private Integer progressPercentage;
    private Long bytesTransferred;
    private Long totalBytes;

    // Static Factory + Getter
}
```

**Zero-Tolerance 체크리스트**:
- [ ] Request/Response DTO
- [ ] Validation
- [ ] ApiMapper
- [ ] Javadoc 작성

---

#### KAN-330: IdempotencyMiddleware 구현

**위치**: `adapter-in/rest-api/src/main/java/com/ryuqq/fileflow/adapter/rest/middleware/`

**목표**: 중복 요청 방지를 위한 멱등성 미들웨어

**구현 상세**:

```java
/**
 * Idempotency Middleware
 * 중복 요청 방지 (AOP + Redis)
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@Aspect
@Order(1)  // 다른 AOP보다 먼저 실행
@RequiredArgsConstructor
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
        String cachedResponse = redisTemplate.opsForValue().get(cacheKey);
        if (cachedResponse != null) {
            log.info("Idempotent cache hit: {}", idempotencyKey);
            return deserializeResponse(cachedResponse, joinPoint);
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

    /**
     * HTTP Header에서 멱등성 키 추출
     */
    private String extractIdempotencyKey() {
        HttpServletRequest request = getCurrentRequest();
        return request.getHeader("X-Idempotency-Key");
    }

    private HttpServletRequest getCurrentRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) attrs).getRequest();
        }
        return null;
    }

    /**
     * 분산 락 획득
     */
    private Boolean acquireLock(String lockKey, long timeout) {
        String lockValue = UUID.randomUUID().toString();

        return redisTemplate.opsForValue().setIfAbsent(
            lockKey,
            lockValue,
            Duration.ofMillis(timeout)
        );
    }

    /**
     * 분산 락 해제
     */
    private void releaseLock(String lockKey) {
        redisTemplate.delete(lockKey);
    }

    /**
     * 결과 캐싱
     */
    private void cacheResult(String cacheKey, Object result, long ttl) {
        try {
            String serialized = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(
                cacheKey,
                serialized,
                Duration.ofMillis(ttl)
            );
        } catch (Exception e) {
            log.error("Failed to cache result", e);
        }
    }

    /**
     * 캐시된 응답 역직렬화
     */
    private Object deserializeResponse(String cached, ProceedingJoinPoint joinPoint) {
        try {
            Class<?> returnType = ((MethodSignature) joinPoint.getSignature())
                .getReturnType();
            return objectMapper.readValue(cached, returnType);
        } catch (Exception e) {
            log.error("Failed to deserialize cached response", e);
            return null;
        }
    }

    private String buildCacheKey(String idempotencyKey) {
        return "idempotency:" + idempotencyKey;
    }
}

/**
 * Idempotent 어노테이션
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    /**
     * 캐시 TTL (밀리초)
     */
    long ttl() default 86400000L;  // 24시간

    /**
     * 락 타임아웃 (밀리초)
     */
    long lockTimeout() default 10000L;  // 10초
}

/**
 * ConcurrentRequestException
 * 동시 요청 예외
 */
public class ConcurrentRequestException extends RuntimeException {
    public ConcurrentRequestException(String message) {
        super(message);
    }
}
```

**Zero-Tolerance 체크리스트**:
- [ ] Redis 분산 락
- [ ] 캐시 TTL 설정
- [ ] 예외 처리
- [ ] Javadoc 작성

---

## 📊 Adapter Layer 완료 체크리스트

### Adapter-Out (Persistence)
- [ ] KAN-313: MultipartUploadJpaAdapter
- [ ] KAN-314: UploadSessionJpaAdapter 확장

### Adapter-Out (Event)
- [ ] KAN-328: UploadEventPublisher
- [ ] KAN-329: UploadEventMapper

### Adapter-In (REST API)
- [ ] KAN-319: UploadController 확장 (Multipart 4개 엔드포인트)
- [ ] KAN-325: ExternalDownloadController
- [ ] KAN-330: IdempotencyMiddleware

---

## 🎯 다음 단계

모든 레이어 완료 후 **통합 테스트** 작성 및 **문서화** 작업을 진행합니다.
