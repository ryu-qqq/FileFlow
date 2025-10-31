# KAN-144: Application Layer 개발 태스크

## 📋 Application Layer 개요

**레이어 역할**: Use Case 구현, 트랜잭션 관리, 도메인 조율
**패키지**: `com.ryuqq.fileflow.application`
**핵심 원칙**: Transaction 경계 엄격 관리, 외부 API 호출은 트랜잭션 밖에서

---

## 🎯 Application Layer 태스크 목록

### Phase 2A: Multipart Upload Use Cases (4 Tasks)

#### KAN-315: InitMultipartUploadUseCase 구현

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/upload/`

**목표**: Multipart 업로드 시작 Use Case

**파일 구조**:
```
application/upload/
├── command/
│   └── InitMultipartCommand.java
├── response/
│   └── InitMultipartResponse.java
└── usecase/
    └── InitMultipartUploadUseCase.java
```

**구현 상세**:

```java
// Command DTO
/**
 * Multipart 업로드 초기화 Command
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class InitMultipartCommand {

    private final Long tenantId;
    private final String fileName;
    private final Long fileSize;
    private final String contentType;
    private final String checksum;  // Optional

    // Private 생성자
    private InitMultipartCommand(
        Long tenantId,
        String fileName,
        Long fileSize,
        String contentType,
        String checksum
    ) {
        this.tenantId = tenantId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.checksum = checksum;
    }

    // Static Factory Method
    public static InitMultipartCommand of(
        Long tenantId,
        String fileName,
        Long fileSize,
        String contentType
    ) {
        return new InitMultipartCommand(
            tenantId,
            fileName,
            fileSize,
            contentType,
            null
        );
    }

    // Getter (NO Setter)
    public Long getTenantId() { return tenantId; }
    public String getFileName() { return fileName; }
    public Long getFileSize() { return fileSize; }
    public String getContentType() { return contentType; }
    public String getChecksum() { return checksum; }
}

// Response DTO
/**
 * Multipart 업로드 초기화 Response
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class InitMultipartResponse {

    private final String sessionKey;
    private final String uploadId;      // S3 UploadId
    private final Integer totalParts;
    private final String storageKey;    // S3 Object Key

    // Private 생성자
    private InitMultipartResponse(
        String sessionKey,
        String uploadId,
        Integer totalParts,
        String storageKey
    ) {
        this.sessionKey = sessionKey;
        this.uploadId = uploadId;
        this.totalParts = totalParts;
        this.storageKey = storageKey;
    }

    // Static Factory Method
    public static InitMultipartResponse of(
        String sessionKey,
        String uploadId,
        Integer totalParts,
        String storageKey
    ) {
        return new InitMultipartResponse(
            sessionKey,
            uploadId,
            totalParts,
            storageKey
        );
    }

    // Getter
    public String getSessionKey() { return sessionKey; }
    public String getUploadId() { return uploadId; }
    public Integer getTotalParts() { return totalParts; }
    public String getStorageKey() { return storageKey; }
}

// Use Case
/**
 * Multipart 업로드 초기화 UseCase
 *
 * 실행 흐름:
 * 1. 정책 검증 (트랜잭션 밖)
 * 2. S3 Multipart 초기화 (트랜잭션 밖) ⭐ 중요
 * 3. Domain 객체 생성 및 저장 (트랜잭션 내)
 * 4. 실패 시 S3 리소스 정리
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class InitMultipartUploadUseCase {

    private final UploadSessionPort uploadSessionPort;
    private final MultipartUploadPort multipartUploadPort;
    private final S3StoragePort s3StoragePort;
    private final PolicyResolverService policyResolver;

    /**
     * Multipart 업로드 초기화 실행
     *
     * @param command 초기화 Command
     * @return 초기화 Response
     */
    public InitMultipartResponse execute(InitMultipartCommand command) {
        // 1. 정책 검증 (트랜잭션 밖)
        validatePolicy(command);

        // 2. S3 Multipart 초기화 (트랜잭션 밖) ⭐
        S3InitResult s3Result = initializeS3Multipart(command);

        try {
            // 3. Domain 저장 (트랜잭션 내)
            UploadSession session = createAndSaveSession(command, s3Result);

            return buildResponse(session, s3Result);

        } catch (Exception e) {
            // 실패 시 S3 리소스 정리
            abortS3Multipart(s3Result);
            throw new UploadInitializationException(
                "Failed to initialize multipart upload", e
            );
        }
    }

    /**
     * 정책 검증
     * 트랜잭션 밖에서 실행
     */
    private void validatePolicy(InitMultipartCommand command) {
        FileMetadata metadata = FileMetadata.of(
            command.getFileName(),
            command.getFileSize(),
            command.getContentType()
        );

        UploadPolicy policy = policyResolver.resolvePolicy(
            command.getTenantId(),
            metadata
        );

        PolicyEvaluationResult evaluation = policy.evaluate(metadata);

        if (!evaluation.isPassed()) {
            throw new PolicyViolationException(
                "Policy violation: " + evaluation.getReasonMessage()
            );
        }
    }

    /**
     * S3 Multipart 초기화
     * ⭐ 트랜잭션 밖에서 실행 (외부 API 호출)
     */
    private S3InitResult initializeS3Multipart(InitMultipartCommand command) {
        try {
            String storageKey = generateStorageKey(command);
            String bucket = determineBucket(command.getTenantId());

            InitiateMultipartUploadRequest request =
                InitiateMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(storageKey)
                    .contentType(command.getContentType())
                    .build();

            InitiateMultipartUploadResponse response =
                s3StoragePort.initiateMultipartUpload(request);

            int partCount = calculatePartCount(command.getFileSize());

            return new S3InitResult(
                response.uploadId(),
                storageKey,
                bucket,
                partCount
            );

        } catch (S3Exception e) {
            throw new StorageException("Failed to initialize S3 multipart", e);
        }
    }

    /**
     * Domain 객체 생성 및 저장
     * ⭐ 트랜잭션 내에서 실행
     */
    @Transactional
    protected UploadSession createAndSaveSession(
        InitMultipartCommand command,
        S3InitResult s3Result
    ) {
        // 1. UploadSession 생성
        UploadSession session = UploadSession.createForMultipart(
            command.getTenantId(),
            command.getFileName(),
            command.getFileSize()
        );

        session.setStorageKey(s3Result.getStorageKey());

        // 2. 저장 (ID 생성)
        UploadSession savedSession = uploadSessionPort.save(session);

        // 3. MultipartUpload 생성
        MultipartUpload multipart = MultipartUpload.create(
            savedSession.getId()
        );

        multipart.initiate(
            s3Result.getUploadId(),
            s3Result.getPartCount()
        );

        // 4. MultipartUpload 저장
        multipartUploadPort.save(multipart);

        // 5. Session에 연결
        savedSession.attachMultipart(multipart);

        return savedSession;
    }

    /**
     * S3 Multipart 정리 (실패 시)
     */
    private void abortS3Multipart(S3InitResult s3Result) {
        try {
            AbortMultipartUploadRequest request =
                AbortMultipartUploadRequest.builder()
                    .bucket(s3Result.getBucket())
                    .key(s3Result.getStorageKey())
                    .uploadId(s3Result.getUploadId())
                    .build();

            s3StoragePort.abortMultipartUpload(request);

        } catch (Exception e) {
            log.error("Failed to abort S3 multipart: {}", s3Result, e);
            // 알림 시스템으로 전달 (수동 정리 필요)
        }
    }

    /**
     * 파트 개수 계산
     * 파트 크기: 100MB (AWS 권장)
     */
    private int calculatePartCount(Long fileSize) {
        long partSize = 100 * 1024 * 1024L;  // 100MB
        return (int) Math.ceil((double) fileSize / partSize);
    }

    /**
     * S3 Storage Key 생성
     * 패턴: uploads/{tenantId}/{date}/{uuid}_{fileName}
     */
    private String generateStorageKey(InitMultipartCommand command) {
        String date = LocalDate.now().format(
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
        );
        String uuid = UUID.randomUUID().toString();

        return String.format(
            "uploads/%d/%s/%s_%s",
            command.getTenantId(),
            date,
            uuid,
            command.getFileName()
        );
    }

    /**
     * 테넌트별 S3 Bucket 결정
     */
    private String determineBucket(Long tenantId) {
        // 실제로는 TenantConfig에서 조회
        return "fileflow-uploads-" + tenantId;
    }

    /**
     * Response 생성
     */
    private InitMultipartResponse buildResponse(
        UploadSession session,
        S3InitResult s3Result
    ) {
        return InitMultipartResponse.of(
            session.getSessionKey(),
            s3Result.getUploadId(),
            s3Result.getPartCount(),
            s3Result.getStorageKey()
        );
    }

    /**
     * S3 초기화 결과 (내부 사용)
     */
    private static class S3InitResult {
        private final String uploadId;
        private final String storageKey;
        private final String bucket;
        private final int partCount;

        public S3InitResult(
            String uploadId,
            String storageKey,
            String bucket,
            int partCount
        ) {
            this.uploadId = uploadId;
            this.storageKey = storageKey;
            this.bucket = bucket;
            this.partCount = partCount;
        }

        public String getUploadId() { return uploadId; }
        public String getStorageKey() { return storageKey; }
        public String getBucket() { return bucket; }
        public int getPartCount() { return partCount; }
    }
}
```

**Zero-Tolerance 체크리스트**:
- [ ] ⭐ S3 API 호출은 트랜잭션 밖에서 (`initializeS3Multipart()`)
- [ ] ⭐ Domain 저장은 트랜잭션 내에서 (`createAndSaveSession()`)
- [ ] 실패 시 S3 리소스 정리 (`abortS3Multipart()`)
- [ ] Command/Response DTO 사용
- [ ] NO Lombok (Command/Response는 불변)
- [ ] Javadoc 작성

---

#### KAN-316: GeneratePartPresignedUrlUseCase 구현

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/upload/`

**목표**: 각 파트 업로드를 위한 Presigned URL 생성

**구현 상세**:

```java
/**
 * 파트 업로드 URL 생성 UseCase
 * 클라이언트가 직접 S3에 파트를 업로드할 수 있도록 Presigned URL 제공
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class GeneratePartPresignedUrlUseCase {

    private final UploadSessionPort uploadSessionPort;
    private final MultipartUploadPort multipartUploadPort;
    private final S3StoragePort s3StoragePort;

    /**
     * Presigned URL 생성
     *
     * @param command URL 생성 Command
     * @return Presigned URL Response
     */
    public PartPresignedUrlResponse execute(GeneratePartUrlCommand command) {
        // 1. 업로드 세션 조회 (트랜잭션 내)
        UploadSession session = findUploadSession(command.getSessionKey());

        // 2. Multipart 정보 검증
        MultipartUpload multipart = session.getMultipartUpload();
        validateMultipartState(multipart, command.getPartNumber());

        // 3. Presigned URL 생성 (트랜잭션 밖)
        String presignedUrl = generatePresignedUrl(
            session.getStorageKey(),
            multipart.getProviderUploadId(),
            command.getPartNumber()
        );

        return buildResponse(command.getPartNumber(), presignedUrl);
    }

    @Transactional(readOnly = true)
    protected UploadSession findUploadSession(String sessionKey) {
        return uploadSessionPort.findBySessionKey(sessionKey)
            .orElseThrow(() -> new UploadSessionNotFoundException(sessionKey));
    }

    private void validateMultipartState(
        MultipartUpload multipart,
        Integer partNumber
    ) {
        if (multipart == null) {
            throw new IllegalStateException("Not a multipart upload");
        }

        if (!multipart.isInProgress()) {
            throw new IllegalStateException(
                "Multipart not in progress: " + multipart.getStatus()
            );
        }

        if (partNumber < 1 || partNumber > multipart.getTotalParts()) {
            throw new IllegalArgumentException(
                "Invalid part number: " + partNumber
            );
        }
    }

    /**
     * Presigned URL 생성 (외부 API 호출, 트랜잭션 밖)
     */
    private String generatePresignedUrl(
        String storageKey,
        String uploadId,
        Integer partNumber
    ) {
        try {
            UploadPartRequest request = UploadPartRequest.builder()
                .key(storageKey)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .build();

            PresignUrlRequest presignRequest = PresignUrlRequest.builder()
                .signatureDuration(Duration.ofHours(1))  // 1시간 유효
                .build();

            return s3StoragePort.presignUploadPart(request, presignRequest);

        } catch (S3Exception e) {
            throw new StorageException(
                "Failed to generate presigned URL", e
            );
        }
    }

    private PartPresignedUrlResponse buildResponse(
        Integer partNumber,
        String presignedUrl
    ) {
        return PartPresignedUrlResponse.of(
            partNumber,
            presignedUrl,
            Duration.ofHours(1)
        );
    }
}
```

**Zero-Tolerance 체크리스트**:
- [ ] S3 API 호출은 트랜잭션 밖
- [ ] Read-only 트랜잭션 사용
- [ ] 상태 검증 로직
- [ ] Javadoc 작성

---

#### KAN-317: MarkPartUploadedUseCase 구현

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/upload/`

**목표**: 파트 업로드 완료 처리

**구현 상세**:

```java
/**
 * 파트 업로드 완료 처리 UseCase
 * 클라이언트가 파트 업로드 완료 후 호출
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class MarkPartUploadedUseCase {

    private final UploadSessionPort uploadSessionPort;
    private final MultipartUploadPort multipartUploadPort;

    /**
     * 파트 업로드 완료 마킹
     *
     * @param command 파트 업로드 완료 Command
     */
    @Transactional
    public void execute(MarkPartUploadedCommand command) {
        // 1. 업로드 세션 조회
        UploadSession session = uploadSessionPort
            .findBySessionKey(command.getSessionKey())
            .orElseThrow(() ->
                new UploadSessionNotFoundException(command.getSessionKey())
            );

        // 2. Multipart 정보 확인
        MultipartUpload multipart = session.getMultipartUpload();
        if (multipart == null) {
            throw new IllegalStateException("Not a multipart upload");
        }

        // 3. UploadPart Value Object 생성
        UploadPart part = UploadPart.of(
            command.getPartNumber(),
            command.getEtag(),
            command.getPartSize()
        );

        // 4. 파트 추가 (Domain 검증)
        multipart.addPart(part);

        // 5. 저장
        multipartUploadPort.save(multipart);
    }
}

/**
 * 파트 업로드 완료 Command
 */
public class MarkPartUploadedCommand {
    private final String sessionKey;
    private final Integer partNumber;
    private final String etag;
    private final Long partSize;

    // Static Factory + Getter
}
```

**Zero-Tolerance 체크리스트**:
- [ ] 트랜잭션 내에서만 실행 (외부 API 호출 없음)
- [ ] Domain 검증 활용 (`multipart.addPart()`)
- [ ] Javadoc 작성

---

#### KAN-318: CompleteMultipartUploadUseCase 구현

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/upload/`

**목표**: 모든 파트 업로드 완료 후 최종 파일 생성

**구현 상세**:

```java
/**
 * Multipart 업로드 완료 UseCase
 * 모든 파트 업로드 완료 후 S3에서 최종 파일 조립
 *
 * 실행 흐름:
 * 1. 완료 가능 검증 (트랜잭션 내)
 * 2. S3 Complete Multipart API 호출 (트랜잭션 밖) ⭐
 * 3. Domain 상태 업데이트 (트랜잭션 내)
 * 4. 이벤트 발행 (트랜잭션 커밋 시 자동)
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class CompleteMultipartUploadUseCase {

    private final UploadSessionPort uploadSessionPort;
    private final MultipartUploadPort multipartUploadPort;
    private final S3StoragePort s3StoragePort;

    /**
     * Multipart 업로드 완료
     *
     * @param command 완료 Command
     * @return 완료 Response
     */
    public CompleteMultipartResponse execute(CompleteMultipartCommand command) {
        // 1. 완료 가능 검증 (트랜잭션 내)
        UploadSession session = validateCanComplete(command.getSessionKey());
        MultipartUpload multipart = session.getMultipartUpload();

        // 2. S3 Complete (트랜잭션 밖) ⭐
        S3CompleteResult s3Result = completeS3Multipart(session, multipart);

        // 3. Domain 업데이트 (트랜잭션 내)
        completeUpload(session, multipart, s3Result);

        return buildResponse(session, s3Result);
    }

    /**
     * 완료 가능 검증
     */
    @Transactional(readOnly = true)
    protected UploadSession validateCanComplete(String sessionKey) {
        UploadSession session = uploadSessionPort
            .findBySessionKey(sessionKey)
            .orElseThrow(() -> new UploadSessionNotFoundException(sessionKey));

        if (!session.canCompleteMultipart()) {
            throw new IllegalStateException("Cannot complete multipart upload");
        }

        return session;
    }

    /**
     * S3 Multipart Complete API 호출
     * ⭐ 트랜잭션 밖에서 실행
     */
    private S3CompleteResult completeS3Multipart(
        UploadSession session,
        MultipartUpload multipart
    ) {
        try {
            // CompletedPart 리스트 생성
            List<CompletedPart> completedParts = multipart.getUploadedParts()
                .stream()
                .map(part -> CompletedPart.builder()
                    .partNumber(part.getPartNumber())
                    .eTag(part.getEtag())
                    .build())
                .collect(Collectors.toList());

            // Complete Multipart Upload Request
            CompleteMultipartUploadRequest request =
                CompleteMultipartUploadRequest.builder()
                    .bucket(session.getBucket())
                    .key(session.getStorageKey())
                    .uploadId(multipart.getProviderUploadId())
                    .multipartUpload(CompletedMultipartUpload.builder()
                        .parts(completedParts)
                        .build())
                    .build();

            CompleteMultipartUploadResponse response =
                s3StoragePort.completeMultipartUpload(request);

            return new S3CompleteResult(
                response.eTag(),
                response.location(),
                session.getFileSize()
            );

        } catch (S3Exception e) {
            throw new StorageException("Failed to complete S3 multipart", e);
        }
    }

    /**
     * Domain 상태 업데이트
     * ⭐ 트랜잭션 내에서 실행
     */
    @Transactional
    protected void completeUpload(
        UploadSession session,
        MultipartUpload multipart,
        S3CompleteResult s3Result
    ) {
        // 1. MultipartUpload 완료
        multipart.complete();
        multipartUploadPort.save(multipart);

        // 2. UploadSession 완료
        session.complete(s3Result.getEtag());

        // 3. 저장 (이벤트 자동 발행)
        uploadSessionPort.save(session);
    }

    private CompleteMultipartResponse buildResponse(
        UploadSession session,
        S3CompleteResult s3Result
    ) {
        return CompleteMultipartResponse.of(
            session.getFileId(),
            s3Result.getEtag(),
            s3Result.getLocation()
        );
    }

    /**
     * S3 완료 결과 (내부 사용)
     */
    private static class S3CompleteResult {
        private final String etag;
        private final String location;
        private final Long fileSize;

        public S3CompleteResult(String etag, String location, Long fileSize) {
            this.etag = etag;
            this.location = location;
            this.fileSize = fileSize;
        }

        public String getEtag() { return etag; }
        public String getLocation() { return location; }
        public Long getFileSize() { return fileSize; }
    }
}
```

**Zero-Tolerance 체크리스트**:
- [ ] ⭐ S3 API 호출은 트랜잭션 밖
- [ ] Domain 업데이트는 트랜잭션 내
- [ ] 이벤트는 Domain에서 자동 발행
- [ ] Javadoc 작성

---

### Phase 2B: External Download & Policy Use Cases (3 Tasks)

#### KAN-322: PolicyResolverService 구현

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/policy/`

**목표**: 테넌트에 적용 가능한 정책 결정 서비스

**구현 상세**:

```java
/**
 * Upload Policy Resolver Service
 * 테넌트와 파일 메타데이터를 기반으로 적용할 정책 결정
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class PolicyResolverService {

    private final UploadPolicyPort uploadPolicyPort;

    /**
     * 정책 결정
     *
     * @param tenantId 테넌트 ID
     * @param file 파일 메타데이터
     * @return 적용할 정책
     */
    public UploadPolicy resolvePolicy(Long tenantId, FileMetadata file) {
        // 1. 테넌트의 활성 정책 조회
        List<UploadPolicy> policies =
            uploadPolicyPort.findActiveByTenantId(tenantId);

        if (policies.isEmpty()) {
            return UploadPolicy.createDefault();
        }

        // 2. 파일에 적용 가능한 정책 필터링
        List<UploadPolicy> applicablePolicies = policies.stream()
            .filter(policy -> policy.getRules().validate(file).isValid())
            .sorted(Comparator.comparing(UploadPolicy::getPriority))
            .collect(Collectors.toList());

        // 3. 우선순위가 가장 높은 정책 반환
        return applicablePolicies.isEmpty()
            ? UploadPolicy.createDefault()
            : applicablePolicies.get(0);
    }
}
```

**Zero-Tolerance 체크리스트**:
- [ ] Domain 로직 활용 (`policy.getRules().validate()`)
- [ ] 기본 정책 제공
- [ ] Javadoc 작성

---

#### KAN-323: StartExternalDownloadUseCase 구현

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/download/`

**목표**: 외부 URL 다운로드 시작

**구현 상세**:

```java
/**
 * 외부 다운로드 시작 UseCase
 * 외부 URL로부터 파일을 다운로드하여 S3에 저장
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class StartExternalDownloadUseCase {

    private final UploadSessionPort uploadSessionPort;
    private final ExternalDownloadPort externalDownloadPort;
    private final HttpDownloadService httpDownloadService;

    /**
     * 외부 다운로드 시작
     * 비동기로 다운로드를 시작하고 즉시 반환
     *
     * @param command 다운로드 시작 Command
     * @return 다운로드 시작 Response
     */
    @Transactional
    public StartDownloadResponse execute(StartDownloadCommand command) {
        // 1. UploadSession 생성
        UploadSession session = UploadSession.createForExternal(
            command.getTenantId(),
            extractFileName(command.getSourceUrl())
        );

        UploadSession savedSession = uploadSessionPort.save(session);

        // 2. ExternalDownload 생성
        ExternalDownload download = ExternalDownload.create(
            command.getSourceUrl(),
            savedSession.getId()
        );

        ExternalDownload savedDownload = externalDownloadPort.save(download);

        // 3. 비동기 다운로드 시작 (트랜잭션 밖)
        httpDownloadService.startDownloadAsync(savedDownload.getId());

        return buildResponse(savedSession, savedDownload);
    }

    private String extractFileName(String url) {
        try {
            Path path = Paths.get(new URI(url).getPath());
            return path.getFileName().toString();
        } catch (Exception e) {
            return "downloaded-file";
        }
    }

    private StartDownloadResponse buildResponse(
        UploadSession session,
        ExternalDownload download
    ) {
        return StartDownloadResponse.of(
            session.getSessionKey(),
            download.getId()
        );
    }
}
```

**Zero-Tolerance 체크리스트**:
- [ ] 비동기 호출은 트랜잭션 커밋 후
- [ ] Domain 생성 로직 활용
- [ ] Javadoc 작성

---

#### KAN-324: ExternalDownloadWorker 구현

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/download/`

**목표**: 백그라운드에서 실제 다운로드 수행

**구현 상세**:

```java
/**
 * 외부 다운로드 Worker
 * 백그라운드에서 실제 HTTP 다운로드를 수행하고 S3에 업로드
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class ExternalDownloadWorker {

    private final ExternalDownloadPort downloadPort;
    private final S3StoragePort s3StoragePort;
    private final RestTemplate restTemplate;

    /**
     * 비동기 다운로드 실행
     * ⭐ @Async와 @Transactional 분리
     */
    @Async("downloadExecutor")
    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2),
        value = {IOException.class, RestClientException.class}
    )
    public CompletableFuture<DownloadResult> executeDownload(Long downloadId) {
        // 1. 다운로드 정보 조회 (짧은 트랜잭션)
        ExternalDownload download = loadDownload(downloadId);

        try {
            // 2. 다운로드 시작 상태 업데이트
            updateDownloadStatus(download, ExternalDownloadStatus.DOWNLOADING);

            // 3. 실제 다운로드 수행 (트랜잭션 밖)
            DownloadResult result = performDownload(download);

            // 4. 완료 상태 업데이트
            updateDownloadStatus(download, ExternalDownloadStatus.COMPLETED);

            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            handleDownloadError(download, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    protected ExternalDownload loadDownload(Long downloadId) {
        return downloadPort.findById(downloadId)
            .orElseThrow(() -> new DownloadNotFoundException(downloadId));
    }

    /**
     * 실제 다운로드 수행
     * ⭐ 트랜잭션 밖에서 실행
     */
    private DownloadResult performDownload(ExternalDownload download) {
        try {
            // HTTP 스트림 열기
            ResponseEntity<Resource> response = restTemplate.exchange(
                download.getSourceUrl().toString(),
                HttpMethod.GET,
                null,
                Resource.class
            );

            // S3로 스트리밍 업로드
            try (InputStream inputStream = response.getBody().getInputStream()) {
                String s3Key = generateS3Key(download);

                S3UploadResult result = s3StoragePort.uploadStream(
                    s3Key,
                    inputStream,
                    progress -> trackProgress(download, progress)
                );

                return new DownloadResult(result.getEtag(), result.getSize());
            }

        } catch (IOException e) {
            throw new DownloadException("Failed to download file", e);
        }
    }

    /**
     * 진행률 추적
     * 1초마다 한 번씩만 DB 업데이트 (부하 방지)
     */
    private void trackProgress(ExternalDownload download, Progress progress) {
        if (shouldUpdateProgress()) {
            updateProgressInDatabase(download, progress);
        }
    }

    @Transactional
    protected void updateDownloadStatus(
        ExternalDownload download,
        ExternalDownloadStatus status
    ) {
        if (status == ExternalDownloadStatus.DOWNLOADING) {
            download.start();
        } else if (status == ExternalDownloadStatus.COMPLETED) {
            download.complete();
        }
        downloadPort.save(download);
    }

    @Transactional
    protected void updateProgressInDatabase(
        ExternalDownload download,
        Progress progress
    ) {
        download.updateProgress(
            progress.getBytesTransferred(),
            progress.getTotalBytes()
        );
        downloadPort.save(download);
    }

    private void handleDownloadError(ExternalDownload download, Exception e) {
        String errorCode = determineErrorCode(e);
        download.fail(errorCode, e.getMessage());
        downloadPort.save(download);
    }

    private String determineErrorCode(Exception e) {
        if (e instanceof SocketTimeoutException) {
            return "TIMEOUT";
        } else if (e instanceof HttpServerErrorException) {
            HttpServerErrorException serverError = (HttpServerErrorException) e;
            return String.valueOf(serverError.getRawStatusCode());
        }
        return "UNKNOWN";
    }
}
```

**Zero-Tolerance 체크리스트**:
- [ ] ⭐ @Async와 @Transactional 분리
- [ ] 실제 다운로드는 트랜잭션 밖
- [ ] 진행률 업데이트 최적화
- [ ] 재시도 로직 (@Retryable)
- [ ] Javadoc 작성

---

### Phase 2C: Batch & Event (2 Tasks)

#### KAN-331: UploadSessionExpirationBatchJob 구현

**위치**: `application/src/main/java/com/ryuqq/fileflow/application/batch/`

**목표**: 만료된 업로드 세션 정리

**구현 상세**:

```java
/**
 * 업로드 세션 만료 배치 작업
 * 24시간 경과한 IN_PROGRESS 세션을 EXPIRED로 변경하고 S3 리소스 정리
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class UploadSessionExpirationBatchJob {

    private final UploadSessionPort uploadSessionPort;
    private final MultipartUploadPort multipartUploadPort;
    private final S3StoragePort s3StoragePort;

    /**
     * 매일 새벽 2시 실행
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void expireOldSessions() {
        LocalDateTime expirationTime = LocalDateTime.now().minusHours(24);

        // 1. 만료 대상 조회
        List<UploadSession> expiredSessions =
            uploadSessionPort.findByStatusAndCreatedBefore(
                UploadStatus.IN_PROGRESS,
                expirationTime
            );

        log.info("Found {} expired sessions", expiredSessions.size());

        // 2. 각 세션 만료 처리
        expiredSessions.forEach(this::expireSessionSafely);
    }

    /**
     * 세션 만료 처리 (예외 격리)
     */
    private void expireSessionSafely(UploadSession session) {
        try {
            expireSession(session);
        } catch (Exception e) {
            log.error("Failed to expire session: {}", session.getSessionKey(), e);
        }
    }

    /**
     * 세션 만료 처리
     * ⭐ 트랜잭션 분리
     */
    @Transactional
    protected void expireSession(UploadSession session) {
        // 1. 상태 변경 (이벤트 자동 발행)
        session.expire();
        uploadSessionPort.save(session);

        // 2. S3 정리 (트랜잭션 밖)
        cleanupS3Resources(session);
    }

    /**
     * S3 리소스 정리
     * ⭐ 트랜잭션 밖에서 실행
     */
    private void cleanupS3Resources(UploadSession session) {
        try {
            // 단일 파일 삭제
            if (session.getStorageKey() != null) {
                s3StoragePort.deleteObject(session.getStorageKey());
            }

            // Multipart 정리
            if (session.isMultipart()) {
                cleanupMultipart(session);
            }

        } catch (S3Exception e) {
            log.error("Failed to cleanup S3 resources: {}",
                session.getSessionKey(), e);
        }
    }

    private void cleanupMultipart(UploadSession session) {
        MultipartUpload multipart = session.getMultipartUpload();
        if (multipart != null && multipart.getProviderUploadId() != null) {
            s3StoragePort.abortMultipartUpload(
                session.getStorageKey(),
                multipart.getProviderUploadId()
            );
        }
    }
}
```

**Zero-Tolerance 체크리스트**:
- [ ] 예외 격리 (한 세션 실패가 전체에 영향 없음)
- [ ] S3 정리는 트랜잭션 밖
- [ ] 이벤트 자동 발행 (`session.expire()`)
- [ ] Javadoc 작성

---

## 📊 Application Layer 완료 체크리스트

### Phase 2A: Multipart Upload
- [ ] KAN-315: InitMultipartUploadUseCase
- [ ] KAN-316: GeneratePartPresignedUrlUseCase
- [ ] KAN-317: MarkPartUploadedUseCase
- [ ] KAN-318: CompleteMultipartUploadUseCase

### Phase 2B: External Download & Policy
- [ ] KAN-322: PolicyResolverService
- [ ] KAN-323: StartExternalDownloadUseCase
- [ ] KAN-324: ExternalDownloadWorker

### Phase 2C: Batch & Event
- [ ] KAN-331: UploadSessionExpirationBatchJob

---

## 🎯 다음 단계

Application Layer 완료 후 **Adapter Layer** 개발로 진행합니다.
