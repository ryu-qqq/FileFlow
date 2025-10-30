# KAN-144: 파일 업로드 시스템 - 개발 태스크 상세 가이드

## 📋 Epic 개요
- **Epic**: KAN-144 - 파일 업로드 시스템
- **목표**: FileFlow 플랫폼의 핵심 파일 업로드 기능 구현
- **범위**: Multipart Upload, External Download, Policy Evaluation, Event Publishing
- **총 태스크 수**: 26개 (Phase 2A: 10개, Phase 2B: 6개, Phase 2C: 10개)

---

## 🗂️ Phase 2A: Multipart Upload 기능 (10 Tasks)

### KAN-310: [Phase 2A-1] MultipartUpload Aggregate 구현

#### 📌 목표
대용량 파일 업로드를 위한 MultipartUpload Aggregate Root 구현 (상태 머신 패턴 적용)

#### 🛠️ 구현 상세

##### 1. MultipartUpload Aggregate Root 클래스 생성
```java
// 위치: domain/src/main/java/com/ryuqq/fileflow/domain/upload/MultipartUpload.java


public class MultipartUpload {
    private final MultipartUploadId id;
    private final UploadSessionId uploadSessionId;
    private S3UploadId providerUploadId;  // S3 UploadId
    private MultipartStatus status;
    private Integer totalParts;
    private final List<UploadPart> uploadedParts;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    // 상태: INIT → IN_PROGRESS → COMPLETED/ABORTED/FAILED
    public enum MultipartStatus {
        INIT, IN_PROGRESS, COMPLETED, ABORTED, FAILED
    }
}
```

##### 2. 상태 전환 메서드 구현
- `initiate(String providerUploadId)`: INIT → IN_PROGRESS
- `addPart(UploadPart part)`: 파트 추가 및 검증
- `complete()`: IN_PROGRESS → COMPLETED (모든 파트 업로드 확인)
- `abort()`: * → ABORTED
- `fail(String reason)`: * → FAILED

##### 3. Domain Event 발행
- `MultipartInitiatedEvent`: Multipart 업로드 시작
- `MultipartCompletedEvent`: 모든 파트 업로드 완료
- `MultipartFailedEvent`: 업로드 실패

##### 4. Invariant 검증
- 파트 번호 중복 방지
- 파트 번호 순서 검증 (1부터 시작, 연속된 번호)
- 상태 전환 규칙 검증

#### ✅ 체크리스트
- [ ] Zero-Tolerance 규칙 준수 (Lombok 금지)
- [ ] Law of Demeter 준수 (getter 체이닝 금지)
- [ ] Javadoc 작성 (@author: Sangwon Ryu, @since: 1.0.0)
- [ ] Unit Test 작성 (Coverage ≥ 80%)
- [ ] 상태 전환 시나리오 테스트

#### 📚 참고 자료
- `docs/guide/02/schema.sql`: upload_multipart 테이블 (lines 66-89)
- `docs/guide/02/seed.sql`: usn_demo_multi_001 샘플 데이터

---

### KAN-311: [Phase 2A-2] UploadPart Value Object 구현

#### 📌 목표
Multipart의 각 파트를 표현하는 불변 Value Object 구현

#### 🛠️ 구현 상세

##### UploadPart Value Object
```java
// 위치: domain/src/main/java/com/ryuqq/fileflow/domain/upload/UploadPart.java

public class UploadPart {
    private final PartNumber partNumber;  // 1-10000
    private final Etag etag;         // S3 ETag
    private final Size size;          // 파트 크기 (5MB-5GB)
    private final CheckSum checksum;    // MD5/SHA256
    private final LocalDateTime uploadedAt;

    // Static factory method
    public static UploadPart of(Integer partNumber, String etag, Long size) {
        validatePartNumber(partNumber);
        validateSize(size);
        return new UploadPart(partNumber, etag, size, null, LocalDateTime.now());
    }

    // 검증 메서드
    private static void validatePartNumber(Integer partNumber) {
        if (partNumber < 1 || partNumber > 10000) {
            throw new IllegalArgumentException("Part number must be between 1 and 10000");
        }
    }

    private static void validateSize(Long size) {
        // 마지막 파트를 제외하고 최소 5MB
        if (size < 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Part size must be at least 5MB");
        }
    }
}
```

#### ✅ 체크리스트
- [ ] 불변성 보장 (final 필드, setter 없음)
- [ ] 검증 로직 구현 (파트 번호, 크기)
- [ ] equals/hashCode 구현
- [ ] Javadoc 작성

---

### KAN-312: [Phase 2A-3] UploadSession Aggregate 확장

#### 📌 목표
기존 UploadSession에 Multipart 업로드 지원 추가

#### 🛠️ 구현 상세

##### UploadSession 확장
```java
// 기존 UploadSession 클래스에 추가

public class UploadSession {
    // 기존 필드들...

    private UploadType uploadType;  // SINGLE, MULTIPART
    private MultipartUpload multipartUpload;  // Multipart 정보

    public enum UploadType {
        SINGLE,     // 단일 파일 업로드
        MULTIPART   // 대용량 파일 분할 업로드
    }

    // Multipart 초기화
    public void initMultipart(Integer totalParts) {
        if (this.uploadType != UploadType.MULTIPART) {
            throw new IllegalStateException("Not a multipart upload session");
        }
        this.multipartUpload = MultipartUpload.create(this.id, totalParts);
    }

    // 파트 업로드 완료
    public void markPartUploaded(UploadPart part) {
        if (multipartUpload == null) {
            throw new IllegalStateException("Multipart not initialized");
        }
        multipartUpload.addPart(part);
    }
}
```

#### ✅ 체크리스트
- [ ] UploadType enum 추가
- [ ] MultipartUpload 연관 관계
- [ ] 상태 검증 로직
- [ ] 기존 기능과의 호환성

---

### KAN-313: [Phase 2A-4] MultipartUploadJpaAdapter 구현

#### 📌 목표
MultipartUpload 영속성 계층 구현 (JPA Adapter)

#### 🛠️ 구현 상세

##### 1. JPA Entity
```java
// 위치: adapter-out/persistence-mysql/src/main/java/com/ryuqq/fileflow/adapter/out/persistence/upload/MultipartUploadEntity.java

@Entity
@Table(name = "upload_multipart")
public class MultipartUploadEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "upload_session_id")
    private Long uploadSessionId;  // Long FK Strategy (NO @ManyToOne)

    @Column(name = "provider_upload_id")
    private String providerUploadId;

    @Enumerated(EnumType.STRING)
    private MultipartStatus status;

    @Column(name = "total_parts")
    private Integer totalParts;

    // Getters/Setters (NO Lombok!)
}
```

##### 2. Repository
```java
@Repository
public interface MultipartUploadJpaRepository extends JpaRepository<MultipartUploadEntity, Long> {
    Optional<MultipartUploadEntity> findByUploadSessionId(Long uploadSessionId);
    List<MultipartUploadEntity> findByStatus(MultipartStatus status);
}
```

##### 3. Adapter 구현
```java
@Component
public class MultipartUploadJpaAdapter implements MultipartUploadPort {
    private final MultipartUploadJpaRepository repository;
    private final MultipartUploadMapper mapper;

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

#### ✅ 체크리스트
- [ ] Long FK Strategy 적용 (JPA 관계 금지)
- [ ] Mapper 구현 (Domain ↔ Entity)
- [ ] QueryDSL 설정 (복잡한 쿼리용)
- [ ] 트랜잭션 경계 확인

---

### KAN-314: [Phase 2A-5] UploadSessionJpaAdapter 확장

#### 📌 목표
기존 UploadSessionJpaAdapter에 Multipart 관련 기능 추가

#### 🛠️ 구현 상세
```java
// 기존 Adapter 확장

@Component
public class UploadSessionJpaAdapter implements UploadSessionPort {
    // 기존 의존성...
    private final MultipartUploadJpaRepository multipartRepository;

    @Override
    @Transactional
    public UploadSession save(UploadSession session) {
        // 기존 저장 로직...

        // Multipart 정보도 함께 저장
        if (session.getUploadType() == UploadType.MULTIPART) {
            MultipartUpload multipart = session.getMultipartUpload();
            if (multipart != null) {
                multipartRepository.save(mapper.toEntity(multipart));
            }
        }

        return session;
    }
}
```

---

### KAN-315: [Phase 2A-6] InitMultipartUploadUseCase 구현

#### 📌 목표
Multipart 업로드 시작 Use Case 구현

#### 🛠️ 구현 상세

##### Use Case 구현
```java
// 위치: application/src/main/java/com/ryuqq/fileflow/application/upload/InitMultipartUploadUseCase.java

@Component
public class InitMultipartUploadUseCase {
    private final UploadSessionPort uploadSessionPort;
    private final S3StoragePort s3StoragePort;
    private final EventPublisher eventPublisher;

    @Transactional
    public InitMultipartResponse execute(InitMultipartCommand command) {
        // 1. 업로드 세션 생성
        UploadSession session = UploadSession.create(
            command.getTenantId(),
            command.getFileName(),
            command.getFileSize(),
            UploadType.MULTIPART
        );

        // 2. S3 Multipart 초기화
        String uploadId = s3StoragePort.initiateMultipartUpload(
            session.getStorageKey()
        );

        // 3. Multipart 정보 저장
        session.initMultipart(calculateTotalParts(command.getFileSize()));
        session.getMultipartUpload().initiate(uploadId);

        // 4. 저장
        UploadSession saved = uploadSessionPort.save(session);

        // 5. 이벤트 발행
        eventPublisher.publish(new MultipartInitiatedEvent(saved.getId()));

        return new InitMultipartResponse(
            saved.getSessionKey(),
            uploadId,
            saved.getMultipartUpload().getTotalParts()
        );
    }

    private Integer calculateTotalParts(Long fileSize) {
        // 파트 크기: 5MB (최소), 100MB (권장)
        long partSize = 100 * 1024 * 1024; // 100MB
        return (int) Math.ceil((double) fileSize / partSize);
    }
}
```

#### ✅ 체크리스트
- [ ] 트랜잭션 경계 설정
- [ ] S3 초기화 실패 시 롤백
- [ ] 파트 크기 계산 로직
- [ ] 이벤트 발행 확인

---

### KAN-316: [Phase 2A-7] GeneratePartPresignedUrlUseCase 구현

#### 📌 목표
각 파트 업로드를 위한 Presigned URL 생성

#### 🛠️ 구현 상세
```java
@Component
public class GeneratePartPresignedUrlUseCase {
    private final UploadSessionPort uploadSessionPort;
    private final S3StoragePort s3StoragePort;

    public PartPresignedUrlResponse execute(GeneratePartUrlCommand command) {
        // 1. 세션 조회
        UploadSession session = uploadSessionPort.findBySessionKey(command.getSessionKey())
            .orElseThrow(() -> new UploadSessionNotFoundException());

        // 2. Multipart 정보 확인
        MultipartUpload multipart = session.getMultipartUpload();
        if (multipart == null) {
            throw new IllegalStateException("Not a multipart upload");
        }

        // 3. Presigned URL 생성
        String presignedUrl = s3StoragePort.generatePartUploadUrl(
            session.getStorageKey(),
            multipart.getProviderUploadId(),
            command.getPartNumber()
        );

        return new PartPresignedUrlResponse(
            command.getPartNumber(),
            presignedUrl,
            Duration.ofMinutes(60) // 유효시간
        );
    }
}
```

---

### KAN-317: [Phase 2A-8] MarkPartUploadedUseCase 구현

#### 📌 목표
파트 업로드 완료 처리

#### 🛠️ 구현 상세
```java
@Component
public class MarkPartUploadedUseCase {
    private final UploadSessionPort uploadSessionPort;

    @Transactional
    public void execute(MarkPartUploadedCommand command) {
        // 1. 세션 조회
        UploadSession session = uploadSessionPort.findBySessionKey(command.getSessionKey())
            .orElseThrow();

        // 2. 파트 정보 생성
        UploadPart part = UploadPart.of(
            command.getPartNumber(),
            command.getEtag(),
            command.getPartSize()
        );

        // 3. 파트 추가
        session.markPartUploaded(part);

        // 4. 저장
        uploadSessionPort.save(session);
    }
}
```

---

### KAN-318: [Phase 2A-9] CompleteMultipartUploadUseCase 구현

#### 📌 목표
모든 파트 업로드 완료 후 최종 파일 생성

#### 🛠️ 구현 상세
```java
@Component
public class CompleteMultipartUploadUseCase {
    private final UploadSessionPort uploadSessionPort;
    private final S3StoragePort s3StoragePort;
    private final EventPublisher eventPublisher;

    @Transactional
    public CompleteMultipartResponse execute(CompleteMultipartCommand command) {
        // 1. 세션 조회
        UploadSession session = uploadSessionPort.findBySessionKey(command.getSessionKey())
            .orElseThrow();

        // 2. Multipart 완료 검증
        MultipartUpload multipart = session.getMultipartUpload();
        if (!multipart.canComplete()) {
            throw new IllegalStateException("Not all parts uploaded");
        }

        // 3. S3 Multipart 완료
        String finalEtag = s3StoragePort.completeMultipartUpload(
            session.getStorageKey(),
            multipart.getProviderUploadId(),
            multipart.getUploadedParts()
        );

        // 4. 상태 업데이트
        multipart.complete();
        session.complete(finalEtag);

        // 5. 저장
        UploadSession completed = uploadSessionPort.save(session);

        // 6. 이벤트 발행
        eventPublisher.publish(new UploadCompletedEvent(completed.getId()));

        return new CompleteMultipartResponse(
            completed.getFileId(),
            finalEtag
        );
    }
}
```

---

### KAN-319: [Phase 2A-10] UploadController 확장 (Multipart 엔드포인트 4개)

#### 📌 목표
Multipart 업로드 REST API 엔드포인트 구현

#### 🛠️ 구현 상세
```java
// 위치: adapter-in/rest-api/src/main/java/com/ryuqq/fileflow/adapter/rest/upload/UploadController.java

@RestController
@RequestMapping("/api/v1/uploads")
public class UploadController {
    // 기존 의존성...
    private final InitMultipartUploadUseCase initMultipartUseCase;
    private final GeneratePartPresignedUrlUseCase generatePartUrlUseCase;
    private final MarkPartUploadedUseCase markPartUploadedUseCase;
    private final CompleteMultipartUploadUseCase completeMultipartUseCase;

    /**
     * Multipart 업로드 시작
     */
    @PostMapping("/multipart/init")
    public ResponseEntity<InitMultipartResponse> initMultipart(
        @Valid @RequestBody InitMultipartRequest request,
        @RequestHeader("X-Tenant-Id") Long tenantId
    ) {
        InitMultipartCommand command = UploadApiMapper.toCommand(request, tenantId);
        InitMultipartResponse response = initMultipartUseCase.execute(command);
        return ResponseEntity.ok(response);
    }

    /**
     * 파트 업로드 URL 생성
     */
    @PostMapping("/multipart/{sessionKey}/parts/{partNumber}/url")
    public ResponseEntity<PartPresignedUrlResponse> generatePartUrl(
        @PathVariable String sessionKey,
        @PathVariable Integer partNumber
    ) {
        GeneratePartUrlCommand command = new GeneratePartUrlCommand(sessionKey, partNumber);
        PartPresignedUrlResponse response = generatePartUrlUseCase.execute(command);
        return ResponseEntity.ok(response);
    }

    /**
     * 파트 업로드 완료 통보
     */
    @PutMapping("/multipart/{sessionKey}/parts/{partNumber}")
    public ResponseEntity<Void> markPartUploaded(
        @PathVariable String sessionKey,
        @PathVariable Integer partNumber,
        @Valid @RequestBody MarkPartUploadedRequest request
    ) {
        MarkPartUploadedCommand command = new MarkPartUploadedCommand(
            sessionKey,
            partNumber,
            request.getEtag(),
            request.getPartSize()
        );
        markPartUploadedUseCase.execute(command);
        return ResponseEntity.noContent().build();
    }

    /**
     * Multipart 업로드 완료
     */
    @PostMapping("/multipart/{sessionKey}/complete")
    public ResponseEntity<CompleteMultipartResponse> completeMultipart(
        @PathVariable String sessionKey
    ) {
        CompleteMultipartCommand command = new CompleteMultipartCommand(sessionKey);
        CompleteMultipartResponse response = completeMultipartUseCase.execute(command);
        return ResponseEntity.ok(response);
    }
}
```

#### ✅ 체크리스트
- [ ] Request/Response DTO 구현
- [ ] Validation 어노테이션
- [ ] ApiMapper 구현
- [ ] OpenAPI 문서화
- [ ] 에러 응답 처리

---

## 🗂️ Phase 2B: External Download & Policy (6 Tasks)

### KAN-320: [Phase 2B-1] ExternalDownload Aggregate 구현

#### 📌 목표
외부 URL로부터 파일 다운로드 기능 구현 (재시도 로직 포함)

#### 🛠️ 구현 상세

##### ExternalDownload Aggregate
```java
// 위치: domain/src/main/java/com/ryuqq/fileflow/domain/download/ExternalDownload.java

public class ExternalDownload {
    private final Long id;
    private final Long uploadSessionId;
    private final String sourceUrl;
    private Long bytesTransferred;
    private Long totalBytes;
    private ExternalDownloadStatus status;
    private Integer retryCount;
    private final Integer maxRetry = 3;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime lastRetryAt;

    public enum ExternalDownloadStatus {
        INIT, DOWNLOADING, COMPLETED, FAILED, ABORTED
    }

    // URL 검증
    public static ExternalDownload create(String sourceUrl, Long sessionId) {
        validateUrl(sourceUrl);
        return new ExternalDownload(null, sessionId, sourceUrl);
    }

    private static void validateUrl(String url) {
        try {
            URL validUrl = new URL(url);
            if (!validUrl.getProtocol().matches("https?")) {
                throw new IllegalArgumentException("Only HTTP/HTTPS supported");
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL", e);
        }
    }

    // 상태 전환 메서드
    public void start() {
        if (this.status != ExternalDownloadStatus.INIT) {
            throw new IllegalStateException("Can only start from INIT");
        }
        this.status = ExternalDownloadStatus.DOWNLOADING;
    }

    public void updateProgress(long transferred, long total) {
        this.bytesTransferred = transferred;
        this.totalBytes = total;
    }

    public int getProgressPercentage() {
        if (totalBytes == null || totalBytes == 0) return 0;
        return (int) ((bytesTransferred * 100) / totalBytes);
    }

    public void fail(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;

        // 재시도 가능한 오류인지 확인
        if (isRetryableError(errorCode) && retryCount < maxRetry) {
            this.retryCount++;
            this.lastRetryAt = LocalDateTime.now();
            // 상태는 DOWNLOADING 유지
        } else {
            this.status = ExternalDownloadStatus.FAILED;
        }
    }

    private boolean isRetryableError(String errorCode) {
        // Timeout, 5xx 에러는 재시도
        return errorCode.startsWith("5") || "TIMEOUT".equals(errorCode);
    }

    public Duration getNextRetryDelay() {
        // 지수 백오프: 1초, 2초, 4초
        return Duration.ofSeconds((long) Math.pow(2, retryCount));
    }
}
```

#### ✅ 체크리스트
- [ ] URL 검증 로직 (스킴, 도메인)
- [ ] 진행률 계산
- [ ] 재시도 로직 (지수 백오프)
- [ ] 재시도 가능/불가능 오류 구분

---

### KAN-321: [Phase 2B-2] UploadPolicy Aggregate 구현

#### 📌 목표
테넌트별 업로드 정책 관리 Aggregate 구현

#### 🛠️ 구현 상세
```java
// 위치: domain/src/main/java/com/ryuqq/fileflow/domain/policy/UploadPolicy.java

public class UploadPolicy {
    private final Long id;
    private final Long tenantId;
    private final String policyName;
    private PolicyRules rules;
    private PolicyStatus status;
    private Integer priority;  // 우선순위 (낮을수록 우선)

    public class PolicyRules {
        private final Set<String> allowedMimeTypes;
        private final Long maxFileSize;
        private final Long minFileSize;
        private final Set<String> allowedExtensions;
        private final Boolean scanRequired;  // 바이러스 스캔 필수 여부
        private final Boolean ocrEnabled;    // OCR 처리 여부

        public boolean evaluate(FileMetadata file) {
            // 파일이 정책을 만족하는지 평가
            if (!allowedMimeTypes.contains(file.getMimeType())) {
                return false;
            }
            if (file.getSize() > maxFileSize || file.getSize() < minFileSize) {
                return false;
            }
            String extension = extractExtension(file.getName());
            if (!allowedExtensions.contains(extension)) {
                return false;
            }
            return true;
        }
    }

    public enum PolicyStatus {
        ACTIVE, INACTIVE, DEPRECATED
    }

    // 정책 평가
    public PolicyEvaluationResult evaluate(FileMetadata file) {
        if (status != PolicyStatus.ACTIVE) {
            return PolicyEvaluationResult.notApplicable("Policy inactive");
        }

        boolean passed = rules.evaluate(file);
        if (passed) {
            return PolicyEvaluationResult.passed(this.id);
        } else {
            return PolicyEvaluationResult.failed(this.id, "File does not meet policy requirements");
        }
    }
}
```

---

### KAN-322: [Phase 2B-3] PolicyResolverService 구현

#### 📌 목표
테넌트에 적용 가능한 정책 결정 서비스

#### 🛠️ 구현 상세
```java
// 위치: application/src/main/java/com/ryuqq/fileflow/application/policy/PolicyResolverService.java

@Service
public class PolicyResolverService {
    private final UploadPolicyPort uploadPolicyPort;

    /**
     * 테넌트와 파일 정보를 기반으로 적용할 정책 결정
     */
    public UploadPolicy resolvePolicy(Long tenantId, FileMetadata file) {
        // 1. 테넌트의 활성 정책 조회
        List<UploadPolicy> policies = uploadPolicyPort.findActiveByTenantId(tenantId);

        if (policies.isEmpty()) {
            // 기본 정책 반환
            return getDefaultPolicy();
        }

        // 2. 파일에 적용 가능한 정책 필터링
        List<UploadPolicy> applicablePolicies = policies.stream()
            .filter(policy -> policy.getRules().evaluate(file))
            .sorted(Comparator.comparing(UploadPolicy::getPriority))
            .collect(Collectors.toList());

        // 3. 우선순위가 가장 높은 정책 반환
        return applicablePolicies.isEmpty()
            ? getDefaultPolicy()
            : applicablePolicies.get(0);
    }

    private UploadPolicy getDefaultPolicy() {
        // 시스템 기본 정책
        return UploadPolicy.createDefault();
    }
}
```

---

### KAN-323: [Phase 2B-4] StartExternalDownloadUseCase 구현

#### 📌 목표
외부 URL 다운로드 시작 Use Case

#### 🛠️ 구현 상세
```java
@Component
public class StartExternalDownloadUseCase {
    private final UploadSessionPort uploadSessionPort;
    private final ExternalDownloadPort externalDownloadPort;
    private final HttpDownloadService httpDownloadService;
    private final EventPublisher eventPublisher;

    @Transactional
    public StartDownloadResponse execute(StartDownloadCommand command) {
        // 1. 업로드 세션 생성
        UploadSession session = UploadSession.create(
            command.getTenantId(),
            extractFileName(command.getSourceUrl()),
            null, // 크기는 다운로드 중 확인
            UploadType.EXTERNAL
        );

        // 2. External Download 생성
        ExternalDownload download = ExternalDownload.create(
            command.getSourceUrl(),
            session.getId()
        );

        // 3. 저장
        UploadSession savedSession = uploadSessionPort.save(session);
        ExternalDownload savedDownload = externalDownloadPort.save(download);

        // 4. 비동기 다운로드 시작
        httpDownloadService.startDownload(savedDownload.getId());

        // 5. 이벤트 발행
        eventPublisher.publish(new ExternalDownloadStartedEvent(savedDownload.getId()));

        return new StartDownloadResponse(
            savedSession.getSessionKey(),
            savedDownload.getId()
        );
    }
}
```

---

### KAN-324: [Phase 2B-5] ExternalDownloadWorker 구현

#### 📌 목표
백그라운드에서 실제 다운로드를 수행하는 Worker

#### 🛠️ 구현 상세
```java
// 위치: application/src/main/java/com/ryuqq/fileflow/application/download/ExternalDownloadWorker.java

@Component
public class ExternalDownloadWorker {
    private final ExternalDownloadPort downloadPort;
    private final S3StoragePort s3StoragePort;
    private final RestTemplate restTemplate;

    @Async
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void executeDownload(Long downloadId) {
        ExternalDownload download = downloadPort.findById(downloadId)
            .orElseThrow();

        try {
            // 1. 다운로드 시작
            download.start();
            downloadPort.save(download);

            // 2. HTTP 스트림 열기
            ResponseEntity<Resource> response = restTemplate.exchange(
                download.getSourceUrl(),
                HttpMethod.GET,
                null,
                Resource.class
            );

            // 3. S3로 스트리밍 업로드
            try (InputStream inputStream = response.getBody().getInputStream()) {
                String s3Key = generateS3Key(download);

                // 진행률 추적하면서 업로드
                S3UploadResult result = s3StoragePort.uploadStream(
                    s3Key,
                    inputStream,
                    progress -> {
                        download.updateProgress(
                            progress.getBytesTransferred(),
                            progress.getTotalBytes()
                        );
                        downloadPort.save(download);
                    }
                );

                // 4. 완료 처리
                download.complete();
                downloadPort.save(download);
            }

        } catch (IOException | RestClientException e) {
            handleDownloadError(download, e);
        }
    }

    private void handleDownloadError(ExternalDownload download, Exception e) {
        String errorCode = determineErrorCode(e);
        download.fail(errorCode, e.getMessage());
        downloadPort.save(download);

        if (download.getStatus() != ExternalDownloadStatus.FAILED) {
            // 재시도 가능한 경우 재스케줄
            scheduleRetry(download);
        }
    }
}
```

---

### KAN-325: [Phase 2B-6] ExternalDownloadController 구현

#### 📌 목표
External Download REST API 엔드포인트

#### 🛠️ 구현 상세
```java
@RestController
@RequestMapping("/api/v1/downloads")
public class ExternalDownloadController {
    private final StartExternalDownloadUseCase startDownloadUseCase;
    private final GetDownloadStatusUseCase getStatusUseCase;

    /**
     * 외부 URL 다운로드 시작
     */
    @PostMapping("/external")
    public ResponseEntity<StartDownloadResponse> startDownload(
        @Valid @RequestBody StartDownloadRequest request,
        @RequestHeader("X-Tenant-Id") Long tenantId
    ) {
        StartDownloadCommand command = new StartDownloadCommand(
            tenantId,
            request.getSourceUrl()
        );
        StartDownloadResponse response = startDownloadUseCase.execute(command);
        return ResponseEntity.accepted().body(response);
    }

    /**
     * 다운로드 진행 상태 조회
     */
    @GetMapping("/external/{downloadId}/status")
    public ResponseEntity<DownloadStatusResponse> getStatus(
        @PathVariable Long downloadId
    ) {
        DownloadStatusResponse response = getStatusUseCase.execute(downloadId);
        return ResponseEntity.ok(response);
    }
}
```

---

## 🗂️ Phase 2C: Event & Integration (10 Tasks)

### KAN-326: [Phase 2C-1] UploadSession AbstractAggregateRoot 확장

#### 📌 목표
Spring Data의 AbstractAggregateRoot를 활용한 도메인 이벤트 발행

#### 🛠️ 구현 상세
```java
// 위치: domain/src/main/java/com/ryuqq/fileflow/domain/upload/UploadSession.java

import org.springframework.data.domain.AbstractAggregateRoot;

public class UploadSession extends AbstractAggregateRoot<UploadSession> {
    // 기존 필드들...

    /**
     * 업로드 완료 처리
     * 이벤트는 registerEvent()로 등록하고,
     * Repository.save() 시 트랜잭션 커밋 직전에 자동 발행됨
     */
    public void complete(String finalEtag) {
        if (this.status != UploadStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot complete from " + status);
        }

        this.status = UploadStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.etag = finalEtag;

        // 이벤트 등록 (트랜잭션 커밋 시 발행)
        registerEvent(new UploadCompletedEvent(
            this.id,
            this.sessionKey,
            this.fileId,
            this.completedAt
        ));
    }

    public void fail(String reason) {
        this.status = UploadStatus.FAILED;
        this.failureReason = reason;

        // 실패 이벤트 등록
        registerEvent(new UploadFailedEvent(
            this.id,
            this.sessionKey,
            reason
        ));
    }

    public void expire() {
        this.status = UploadStatus.EXPIRED;

        // 만료 이벤트 등록
        registerEvent(new UploadExpiredEvent(
            this.id,
            this.sessionKey
        ));
    }

    public void abort() {
        this.status = UploadStatus.ABORTED;

        // 중단 이벤트 등록
        registerEvent(new UploadAbortedEvent(
            this.id,
            this.sessionKey
        ));
    }
}
```

#### ✅ 체크리스트
- [ ] AbstractAggregateRoot 상속
- [ ] 각 상태 전환 메서드에서 이벤트 등록
- [ ] 트랜잭션 커밋 시 이벤트 발행 확인
- [ ] 롤백 시 이벤트 미발행 테스트

---

### KAN-327: [Phase 2C-2] Domain Events 정의 (4개)

#### 📌 목표
업로드 관련 도메인 이벤트 정의

#### 🛠️ 구현 상세
```java
// 위치: domain/src/main/java/com/ryuqq/fileflow/domain/event/

/**
 * 업로드 완료 이벤트
 */
public class UploadCompletedEvent {
    private final Long uploadSessionId;
    private final String sessionKey;
    private final Long fileId;
    private final LocalDateTime completedAt;
    private final LocalDateTime occurredAt;

    public UploadCompletedEvent(Long uploadSessionId, String sessionKey,
                                Long fileId, LocalDateTime completedAt) {
        this.uploadSessionId = uploadSessionId;
        this.sessionKey = sessionKey;
        this.fileId = fileId;
        this.completedAt = completedAt;
        this.occurredAt = LocalDateTime.now();
    }
}

/**
 * 업로드 실패 이벤트
 */
public class UploadFailedEvent {
    private final Long uploadSessionId;
    private final String sessionKey;
    private final String failureReason;
    private final LocalDateTime occurredAt;
}

/**
 * 업로드 만료 이벤트
 */
public class UploadExpiredEvent {
    private final Long uploadSessionId;
    private final String sessionKey;
    private final LocalDateTime occurredAt;
}

/**
 * 업로드 중단 이벤트
 */
public class UploadAbortedEvent {
    private final Long uploadSessionId;
    private final String sessionKey;
    private final LocalDateTime occurredAt;
}
```

---

### KAN-328: [Phase 2C-3] UploadEventPublisher 구현 (Anti-Corruption Layer)

#### 📌 목표
도메인 이벤트를 외부 시스템(SQS)으로 전달하는 Anti-Corruption Layer

#### 🛠️ 구현 상세
```java
// 위치: adapter-out/event/src/main/java/com/ryuqq/fileflow/adapter/out/event/UploadEventPublisher.java

@Component
public class UploadEventPublisher {
    private final SqsTemplate sqsTemplate;
    private final UploadEventMapper mapper;

    /**
     * Spring의 @TransactionalEventListener 사용
     * 트랜잭션 커밋 후 이벤트 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUploadCompleted(UploadCompletedEvent event) {
        // Domain Event → SQS Message 변환 (Anti-Corruption)
        SqsUploadMessage message = mapper.toSqsMessage(event);

        // SQS 발행
        sqsTemplate.send(message);

        log.info("Published upload completed event: {}", event.getSessionKey());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUploadFailed(UploadFailedEvent event) {
        SqsUploadFailedMessage message = mapper.toSqsMessage(event);
        sqsTemplate.send(message);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUploadExpired(UploadExpiredEvent event) {
        // 만료된 파일 정리 요청
        SqsCleanupMessage message = new SqsCleanupMessage(
            event.getUploadSessionId(),
            "EXPIRED"
        );
        sqsTemplate.send(message);
    }
}
```

#### ✅ 체크리스트
- [ ] @TransactionalEventListener 설정
- [ ] TransactionPhase.AFTER_COMMIT 확인
- [ ] Anti-Corruption Layer 패턴 적용
- [ ] 실패 시 재시도 로직

---

### KAN-329: [Phase 2C-4] UploadEventMapper 구현

#### 📌 목표
도메인 이벤트와 외부 메시지 간 변환

#### 🛠️ 구현 상세
```java
@Component
public class UploadEventMapper {

    /**
     * Domain Event → SQS Message
     * Anti-Corruption Layer의 핵심
     */
    public SqsUploadMessage toSqsMessage(UploadCompletedEvent event) {
        return SqsUploadMessage.builder()
            .messageType("UPLOAD_COMPLETED")
            .sessionId(event.getUploadSessionId())
            .sessionKey(event.getSessionKey())
            .fileId(event.getFileId())
            .timestamp(event.getOccurredAt())
            .build();
    }

    public SqsUploadFailedMessage toSqsMessage(UploadFailedEvent event) {
        return SqsUploadFailedMessage.builder()
            .messageType("UPLOAD_FAILED")
            .sessionId(event.getUploadSessionId())
            .reason(event.getFailureReason())
            .timestamp(event.getOccurredAt())
            .build();
    }
}
```

---

### KAN-330: [Phase 2C-5] IdempotencyMiddleware 구현

#### 📌 목표
중복 요청 방지를 위한 멱등성 미들웨어

#### 🛠️ 구현 상세
```java
// 위치: adapter-in/rest-api/src/main/java/com/ryuqq/fileflow/adapter/rest/middleware/IdempotencyMiddleware.java

@Component
@Aspect
public class IdempotencyMiddleware {
    private final RedisTemplate<String, String> redisTemplate;

    @Around("@annotation(Idempotent)")
    public Object checkIdempotency(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getCurrentRequest();
        String idempotencyKey = request.getHeader("X-Idempotency-Key");

        if (idempotencyKey == null) {
            // 멱등성 키 없으면 그냥 진행
            return joinPoint.proceed();
        }

        String cacheKey = "idempotency:" + idempotencyKey;

        // 1. 캐시 확인
        String cachedResponse = redisTemplate.opsForValue().get(cacheKey);
        if (cachedResponse != null) {
            // 이미 처리된 요청
            return deserialize(cachedResponse);
        }

        // 2. 락 획득 (동시 요청 방지)
        Boolean lockAcquired = redisTemplate.opsForValue()
            .setIfAbsent(cacheKey + ":lock", "1", Duration.ofSeconds(10));

        if (!lockAcquired) {
            throw new ConcurrentRequestException("Request already in progress");
        }

        try {
            // 3. 실제 처리
            Object result = joinPoint.proceed();

            // 4. 결과 캐싱 (24시간)
            redisTemplate.opsForValue().set(
                cacheKey,
                serialize(result),
                Duration.ofHours(24)
            );

            return result;
        } finally {
            // 5. 락 해제
            redisTemplate.delete(cacheKey + ":lock");
        }
    }
}

/**
 * 멱등성 보장이 필요한 엔드포인트에 적용
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
}
```

---

### KAN-331: [Phase 2C-6] UploadSessionExpirationBatchJob 구현

#### 📌 목표
만료된 업로드 세션 정리 배치 작업

#### 🛠️ 구현 상세
```java
// 위치: application/src/main/java/com/ryuqq/fileflow/application/batch/UploadSessionExpirationBatchJob.java

@Component
public class UploadSessionExpirationBatchJob {
    private final UploadSessionPort uploadSessionPort;
    private final S3StoragePort s3StoragePort;

    /**
     * 매일 새벽 2시 실행
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void expireOldSessions() {
        LocalDateTime expirationTime = LocalDateTime.now().minusHours(24);

        // 1. 만료 대상 조회
        List<UploadSession> expiredSessions = uploadSessionPort
            .findByStatusAndCreatedBefore(
                UploadStatus.IN_PROGRESS,
                expirationTime
            );

        log.info("Found {} expired sessions", expiredSessions.size());

        // 2. 각 세션 만료 처리
        for (UploadSession session : expiredSessions) {
            try {
                expireSession(session);
            } catch (Exception e) {
                log.error("Failed to expire session: {}", session.getSessionKey(), e);
            }
        }
    }

    @Transactional
    protected void expireSession(UploadSession session) {
        // 1. 상태 변경 (이벤트 자동 발행)
        session.expire();
        uploadSessionPort.save(session);

        // 2. S3 임시 파일 삭제
        if (session.getStorageKey() != null) {
            s3StoragePort.deleteObject(session.getStorageKey());
        }

        // 3. Multipart 정리
        if (session.getUploadType() == UploadType.MULTIPART) {
            MultipartUpload multipart = session.getMultipartUpload();
            if (multipart != null && multipart.getProviderUploadId() != null) {
                s3StoragePort.abortMultipartUpload(
                    session.getStorageKey(),
                    multipart.getProviderUploadId()
                );
            }
        }

        log.info("Expired session: {}", session.getSessionKey());
    }
}
```

---

### KAN-332~335: 통합 테스트 태스크들

이 태스크들은 각 기능별 통합 테스트 구현입니다.

#### KAN-332: [Phase 2C-7] Multipart Upload 통합 테스트
```java
@SpringBootTest
@AutoConfigureMockMvc
public class MultipartUploadIntegrationTest {

    @Test
    void shouldCompleteMultipartUpload() {
        // 1. Multipart 초기화
        // 2. 각 파트 업로드
        // 3. 완료 처리
        // 4. 이벤트 발행 확인
    }
}
```

#### KAN-333: [Phase 2C-8] External Download 통합 테스트
#### KAN-334: [Phase 2C-9] Policy Evaluation 통합 테스트
#### KAN-335: [Phase 2C-10] Event Publishing 통합 테스트

---

## 📝 개발 시 주의사항

### 1. Zero-Tolerance 규칙 (반드시 준수)
- **NO Lombok**: 모든 getter/setter 수동 작성
- **Law of Demeter**: getter 체이닝 금지
- **Long FK Strategy**: JPA 관계 어노테이션 사용 금지
- **Transaction Boundary**: @Transactional 내 외부 API 호출 금지

### 2. 코딩 컨벤션
- 모든 public 클래스/메서드에 Javadoc 필수
- @author: Sangwon Ryu
- @since: 1.0.0
- 패키지 구조 준수 (헥사고날 아키텍처)

### 3. 테스트 요구사항
- Domain Layer: 90% 커버리지
- Application Layer: 80% 커버리지
- Adapter Layer: 70% 커버리지

### 4. Git 브랜치 전략
```bash
# 각 태스크별 브랜치 생성
git checkout -b feature/KAN-310-multipart-aggregate
git checkout -b feature/KAN-320-external-download-aggregate
git checkout -b feature/KAN-326-aggregate-root-extension
```

---

## 🚀 실행 순서 권장사항

### Phase별 순차 진행
1. **Phase 2A** (Multipart Upload): KAN-310 ~ KAN-319
2. **Phase 2B** (External Download & Policy): KAN-320 ~ KAN-325
3. **Phase 2C** (Event & Integration): KAN-326 ~ KAN-335

### 각 Phase 내에서는 번호 순서대로 진행
- Domain 계층 먼저 구현
- Application 계층 구현
- Adapter 계층 구현
- 통합 테스트로 마무리

---

## 📊 진행 상황 체크리스트

### Phase 2A: Multipart Upload
- [ ] KAN-310: MultipartUpload Aggregate
- [ ] KAN-311: UploadPart Value Object
- [ ] KAN-312: UploadSession 확장
- [ ] KAN-313: MultipartUploadJpaAdapter
- [ ] KAN-314: UploadSessionJpaAdapter 확장
- [ ] KAN-315: InitMultipartUploadUseCase
- [ ] KAN-316: GeneratePartPresignedUrlUseCase
- [ ] KAN-317: MarkPartUploadedUseCase
- [ ] KAN-318: CompleteMultipartUploadUseCase
- [ ] KAN-319: UploadController 확장

### Phase 2B: External Download & Policy
- [ ] KAN-320: ExternalDownload Aggregate
- [ ] KAN-321: UploadPolicy Aggregate
- [ ] KAN-322: PolicyResolverService
- [ ] KAN-323: StartExternalDownloadUseCase
- [ ] KAN-324: ExternalDownloadWorker
- [ ] KAN-325: ExternalDownloadController

### Phase 2C: Event & Integration
- [ ] KAN-326: AbstractAggregateRoot 확장
- [ ] KAN-327: Domain Events 정의
- [ ] KAN-328: UploadEventPublisher
- [ ] KAN-329: UploadEventMapper
- [ ] KAN-330: IdempotencyMiddleware
- [ ] KAN-331: ExpirationBatchJob
- [ ] KAN-332: Multipart 통합 테스트
- [ ] KAN-333: External Download 통합 테스트
- [ ] KAN-334: Policy 통합 테스트
- [ ] KAN-335: Event 통합 테스트

---

이 문서는 주니어 개발자도 쉽게 이해하고 구현할 수 있도록 상세한 코드 예시와 체크리스트를 포함하고 있습니다. 각 태스크를 진행하면서 체크리스트를 확인하고, 코딩 컨벤션을 준수하여 구현해 주세요.