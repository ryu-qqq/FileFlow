# Phase 2B: External Download & Policy 태스크 상세 가이드

## 📋 Phase 2B 개요
- **목표**: 외부 URL 다운로드 기능과 테넌트별 업로드 정책 관리
- **태스크 수**: 6개 (KAN-320 ~ KAN-325)
- **예상 기간**: 1.5주
- **핵심 기술**: HTTP Client, 재시도 로직, 정책 평가 엔진

---

## KAN-320: ExternalDownload Aggregate 구현

### 📌 작업 내용
```java
// 위치: domain/src/main/java/com/ryuqq/fileflow/domain/download/ExternalDownload.java

/**
 * 외부 다운로드 Aggregate Root
 * URL로부터 파일 다운로드 관리
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class ExternalDownload {

    private final Long id;
    private final Long uploadSessionId;  // Long FK Strategy
    private final DownloadSource source;
    private DownloadProgress progress;
    private ExternalDownloadStatus status;
    private RetryContext retryContext;
    private FailureDetails failureDetails;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    // Private 생성자
    private ExternalDownload(Long uploadSessionId, String sourceUrl) {
        this.id = null;
        this.uploadSessionId = uploadSessionId;
        this.source = DownloadSource.fromUrl(sourceUrl);  // URL 검증
        this.progress = DownloadProgress.notStarted();
        this.status = ExternalDownloadStatus.INIT;
        this.retryContext = RetryContext.initial();
        this.startedAt = LocalDateTime.now();
    }

    // Static Factory Method
    public static ExternalDownload create(Long uploadSessionId, String sourceUrl) {
        return new ExternalDownload(uploadSessionId, sourceUrl);
    }

    // 다운로드 시작
    public void start() {
        if (status != ExternalDownloadStatus.INIT && status != ExternalDownloadStatus.RETRYING) {
            throw new IllegalStateException("Cannot start download in status: " + status);
        }
        this.status = ExternalDownloadStatus.DOWNLOADING;
    }

    // 진행률 업데이트
    public void updateProgress(long bytesTransferred, long totalBytes) {
        this.progress = progress.update(bytesTransferred, totalBytes);
    }

    // 실패 처리
    public void fail(String errorCode, String errorMessage) {
        this.failureDetails = new FailureDetails(errorCode, errorMessage);

        if (canRetry()) {
            this.status = ExternalDownloadStatus.RETRYING;
            this.retryContext = retryContext.incrementAttempt();
        } else {
            this.status = ExternalDownloadStatus.FAILED;
        }
    }

    // Tell, Don't Ask 패턴
    public boolean canRetry() {
        return retryContext.hasRetriesLeft() && isRetryableError();
    }

    private boolean isRetryableError() {
        if (failureDetails == null) return false;
        return failureDetails.isRetryable();
    }

    public Duration getNextRetryDelay() {
        return retryContext.getNextDelay();
    }

    // 완료 처리
    public void complete() {
        this.status = ExternalDownloadStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    // 중단 처리
    public void abort() {
        this.status = ExternalDownloadStatus.ABORTED;
    }

    // 진행률 계산 (Getter 체이닝 방지)
    public int getProgressPercentage() {
        return progress.getPercentage();
    }

    public String getProgressDescription() {
        return progress.getDescription();
    }

    // 필요한 Getter만 제공
    public Long getId() { return id; }
    public Long getUploadSessionId() { return uploadSessionId; }
    public String getSourceUrl() { return source.getUrl(); }
    public ExternalDownloadStatus getStatus() { return status; }
}

// Value Object: DownloadSource
final class DownloadSource {
    private final URL url;
    private final String protocol;
    private final String host;

    private DownloadSource(String urlString) {
        this.url = validateUrl(urlString);
        this.protocol = url.getProtocol();
        this.host = url.getHost();
    }

    public static DownloadSource fromUrl(String url) {
        return new DownloadSource(url);
    }

    private static URL validateUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            if (!url.getProtocol().matches("https?")) {
                throw new IllegalArgumentException("Only HTTP/HTTPS supported: " + urlString);
            }
            // 추가 검증: localhost, private IP 차단
            if (isPrivateUrl(url)) {
                throw new IllegalArgumentException("Private URLs not allowed: " + urlString);
            }
            return url;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + urlString, e);
        }
    }

    private static boolean isPrivateUrl(URL url) {
        String host = url.getHost();
        return host.equals("localhost") ||
               host.equals("127.0.0.1") ||
               host.startsWith("192.168.") ||
               host.startsWith("10.") ||
               host.startsWith("172.");
    }

    public String getUrl() { return url.toString(); }
}

// Value Object: RetryContext
final class RetryContext {
    private static final int MAX_RETRIES = 3;
    private final int attemptCount;
    private final LocalDateTime lastAttemptAt;

    private RetryContext(int attemptCount) {
        this.attemptCount = attemptCount;
        this.lastAttemptAt = LocalDateTime.now();
    }

    public static RetryContext initial() {
        return new RetryContext(0);
    }

    public RetryContext incrementAttempt() {
        return new RetryContext(attemptCount + 1);
    }

    public boolean hasRetriesLeft() {
        return attemptCount < MAX_RETRIES;
    }

    public Duration getNextDelay() {
        // 지수 백오프: 1s, 2s, 4s
        return Duration.ofSeconds((long) Math.pow(2, attemptCount));
    }

    public int getAttemptCount() { return attemptCount; }
}

// Value Object: DownloadProgress
final class DownloadProgress {
    private final long bytesTransferred;
    private final long totalBytes;

    private DownloadProgress(long bytesTransferred, long totalBytes) {
        this.bytesTransferred = bytesTransferred;
        this.totalBytes = totalBytes;
    }

    public static DownloadProgress notStarted() {
        return new DownloadProgress(0, 0);
    }

    public DownloadProgress update(long bytesTransferred, long totalBytes) {
        return new DownloadProgress(bytesTransferred, totalBytes);
    }

    public int getPercentage() {
        if (totalBytes == 0) return 0;
        return (int) ((bytesTransferred * 100) / totalBytes);
    }

    public String getDescription() {
        return String.format("%s / %s (%d%%)",
            formatBytes(bytesTransferred),
            formatBytes(totalBytes),
            getPercentage()
        );
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)) + " MB";
        return (bytes / (1024 * 1024 * 1024)) + " GB";
    }
}
```

### ⚠️ 코딩 컨벤션 체크포인트
- ✅ **URL 검증**: 생성 시점에 철저한 검증
- ✅ **Tell, Don't Ask**: `canRetry()` 메서드
- ✅ **Value Objects**: DownloadSource, RetryContext, DownloadProgress
- ✅ **NO Getter Chaining**: 진행률 정보 직접 제공

---

## KAN-321: UploadPolicy Aggregate 구현

### 📌 작업 내용
```java
// 위치: domain/src/main/java/com/ryuqq/fileflow/domain/policy/UploadPolicy.java

/**
 * 업로드 정책 Aggregate Root
 * 테넌트별 파일 업로드 규칙 관리
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class UploadPolicy implements Comparable<UploadPolicy> {

    private final Long id;
    private final Long tenantId;  // Long FK Strategy
    private final String policyName;
    private final PolicyRules rules;
    private PolicyStatus status;
    private final Integer priority;  // 낮을수록 우선
    private final LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    // Private 생성자
    private UploadPolicy(Long tenantId, String policyName, PolicyRules rules, Integer priority) {
        this.id = null;
        this.tenantId = tenantId;
        this.policyName = policyName;
        this.rules = rules;
        this.status = PolicyStatus.ACTIVE;
        this.priority = priority;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = this.createdAt;
    }

    // Static Factory Method
    public static UploadPolicy create(
        Long tenantId,
        String policyName,
        PolicyRules rules,
        Integer priority
    ) {
        return new UploadPolicy(tenantId, policyName, rules, priority);
    }

    // 기본 정책 생성
    public static UploadPolicy createDefault(Long tenantId) {
        PolicyRules defaultRules = PolicyRules.builder()
            .allowMimeTypes("image/jpeg", "image/png", "application/pdf")
            .maxFileSize(100 * 1024 * 1024L)  // 100MB
            .minFileSize(1L)
            .allowExtensions("jpg", "jpeg", "png", "pdf")
            .build();

        return new UploadPolicy(tenantId, "Default Policy", defaultRules, 999);
    }

    // 정책 평가 (Tell, Don't Ask)
    public PolicyEvaluationResult evaluate(FileMetadata file) {
        if (!isActive()) {
            return PolicyEvaluationResult.notApplicable(
                "Policy is not active: " + status
            );
        }

        List<String> violations = new ArrayList<>();

        // MIME 타입 검증
        if (!rules.isAllowedMimeType(file.getMimeType())) {
            violations.add("MIME type not allowed: " + file.getMimeType());
        }

        // 파일 크기 검증
        if (!rules.isWithinSizeRange(file.getSize())) {
            violations.add("File size out of range: " + file.getSize());
        }

        // 확장자 검증
        if (!rules.isAllowedExtension(file.getExtension())) {
            violations.add("Extension not allowed: " + file.getExtension());
        }

        if (violations.isEmpty()) {
            return PolicyEvaluationResult.passed(this.id, this.policyName);
        } else {
            return PolicyEvaluationResult.failed(this.id, this.policyName, violations);
        }
    }

    // 정책 활성화/비활성화
    public void activate() {
        if (status == PolicyStatus.DEPRECATED) {
            throw new IllegalStateException("Cannot activate deprecated policy");
        }
        this.status = PolicyStatus.ACTIVE;
        this.modifiedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.status = PolicyStatus.INACTIVE;
        this.modifiedAt = LocalDateTime.now();
    }

    public void deprecate() {
        this.status = PolicyStatus.DEPRECATED;
        this.modifiedAt = LocalDateTime.now();
    }

    // Tell, Don't Ask
    public boolean isActive() {
        return status == PolicyStatus.ACTIVE;
    }

    public boolean canBeModified() {
        return status != PolicyStatus.DEPRECATED;
    }

    // 우선순위 비교 (Comparable)
    @Override
    public int compareTo(UploadPolicy other) {
        return Integer.compare(this.priority, other.priority);
    }

    // 필요한 Getter만
    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getPolicyName() { return policyName; }
    public Integer getPriority() { return priority; }
    public PolicyStatus getStatus() { return status; }
}

// Value Object: PolicyRules (불변)
public final class PolicyRules {
    private final Set<String> allowedMimeTypes;
    private final Long maxFileSize;
    private final Long minFileSize;
    private final Set<String> allowedExtensions;
    private final ProcessingOptions processingOptions;

    private PolicyRules(Builder builder) {
        this.allowedMimeTypes = Set.copyOf(builder.allowedMimeTypes);
        this.maxFileSize = builder.maxFileSize;
        this.minFileSize = builder.minFileSize;
        this.allowedExtensions = Set.copyOf(builder.allowedExtensions);
        this.processingOptions = builder.processingOptions;
    }

    // 검증 메서드
    public boolean isAllowedMimeType(String mimeType) {
        return allowedMimeTypes.contains(mimeType);
    }

    public boolean isWithinSizeRange(Long size) {
        return size >= minFileSize && size <= maxFileSize;
    }

    public boolean isAllowedExtension(String extension) {
        return allowedExtensions.contains(extension.toLowerCase());
    }

    // Builder 패턴 (수동 구현)
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Set<String> allowedMimeTypes = new HashSet<>();
        private Long maxFileSize = 100 * 1024 * 1024L;  // 기본 100MB
        private Long minFileSize = 1L;
        private Set<String> allowedExtensions = new HashSet<>();
        private ProcessingOptions processingOptions = ProcessingOptions.defaults();

        public Builder allowMimeTypes(String... types) {
            this.allowedMimeTypes.addAll(Arrays.asList(types));
            return this;
        }

        public Builder maxFileSize(Long maxSize) {
            if (maxSize <= 0) {
                throw new IllegalArgumentException("Max size must be positive");
            }
            this.maxFileSize = maxSize;
            return this;
        }

        public Builder minFileSize(Long minSize) {
            if (minSize < 0) {
                throw new IllegalArgumentException("Min size cannot be negative");
            }
            this.minFileSize = minSize;
            return this;
        }

        public Builder allowExtensions(String... extensions) {
            for (String ext : extensions) {
                this.allowedExtensions.add(ext.toLowerCase());
            }
            return this;
        }

        public Builder processingOptions(ProcessingOptions options) {
            this.processingOptions = options;
            return this;
        }

        public PolicyRules build() {
            validate();
            return new PolicyRules(this);
        }

        private void validate() {
            if (minFileSize > maxFileSize) {
                throw new IllegalStateException("Min size cannot be greater than max size");
            }
            if (allowedMimeTypes.isEmpty()) {
                throw new IllegalStateException("At least one MIME type must be allowed");
            }
        }
    }
}

// Value Object: ProcessingOptions
public final class ProcessingOptions {
    private final boolean virusScanRequired;
    private final boolean ocrEnabled;
    private final boolean thumbnailGeneration;
    private final boolean autoTagging;

    private ProcessingOptions(
        boolean virusScanRequired,
        boolean ocrEnabled,
        boolean thumbnailGeneration,
        boolean autoTagging
    ) {
        this.virusScanRequired = virusScanRequired;
        this.ocrEnabled = ocrEnabled;
        this.thumbnailGeneration = thumbnailGeneration;
        this.autoTagging = autoTagging;
    }

    public static ProcessingOptions defaults() {
        return new ProcessingOptions(true, false, false, false);
    }

    // Getter
    public boolean isVirusScanRequired() { return virusScanRequired; }
    public boolean isOcrEnabled() { return ocrEnabled; }
    public boolean isThumbnailGeneration() { return thumbnailGeneration; }
    public boolean isAutoTagging() { return autoTagging; }
}
```

### ⚠️ 코딩 컨벤션 체크포인트
- ✅ **불변 Value Object**: PolicyRules를 불변으로 설계
- ✅ **Builder 패턴**: Lombok 없이 수동 구현
- ✅ **Tell, Don't Ask**: `isActive()`, `canBeModified()`
- ✅ **Comparable**: 우선순위 기반 정렬

---

## KAN-322: PolicyResolverService 구현

### 📌 작업 내용
```java
// 위치: application/src/main/java/com/ryuqq/fileflow/application/policy/PolicyResolverService.java

/**
 * 정책 결정 서비스
 * 테넌트와 파일 정보를 기반으로 적용할 정책 결정
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class PolicyResolverService {

    private final UploadPolicyPort uploadPolicyPort;
    private final TenantPort tenantPort;
    private final PolicyCacheService cacheService;

    /**
     * 파일에 적용할 정책 결정
     *
     * @param tenantId 테넌트 ID
     * @param fileMetadata 파일 메타데이터
     * @return 적용할 정책
     */
    public UploadPolicy resolvePolicy(Long tenantId, FileMetadata fileMetadata) {
        // 1. 캐시 확인
        String cacheKey = buildCacheKey(tenantId, fileMetadata);
        Optional<UploadPolicy> cached = cacheService.getPolicy(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }

        // 2. 테넌트 확인
        Tenant tenant = tenantPort.findById(tenantId)
            .orElseThrow(() -> new TenantNotFoundException(tenantId));

        if (!tenant.isActive()) {
            throw new InactiveTenantException(tenantId);
        }

        // 3. 테넌트의 활성 정책 조회
        List<UploadPolicy> policies = uploadPolicyPort.findActiveByTenantId(tenantId);

        // 4. 적용 가능한 정책 필터링 및 우선순위 정렬
        UploadPolicy selectedPolicy = selectBestPolicy(policies, fileMetadata);

        // 5. 캐시 저장
        cacheService.putPolicy(cacheKey, selectedPolicy);

        return selectedPolicy;
    }

    private UploadPolicy selectBestPolicy(
        List<UploadPolicy> policies,
        FileMetadata fileMetadata
    ) {
        if (policies.isEmpty()) {
            return createSystemDefaultPolicy();
        }

        // 적용 가능한 정책 필터링
        List<PolicyMatch> matches = policies.stream()
            .map(policy -> evaluatePolicy(policy, fileMetadata))
            .filter(PolicyMatch::isApplicable)
            .sorted()  // PolicyMatch가 Comparable 구현
            .collect(Collectors.toList());

        if (matches.isEmpty()) {
            return createSystemDefaultPolicy();
        }

        // 가장 높은 우선순위 정책 반환
        return matches.get(0).getPolicy();
    }

    private PolicyMatch evaluatePolicy(UploadPolicy policy, FileMetadata fileMetadata) {
        PolicyEvaluationResult result = policy.evaluate(fileMetadata);
        return new PolicyMatch(policy, result);
    }

    private UploadPolicy createSystemDefaultPolicy() {
        // 시스템 기본 정책 (가장 관대함)
        return UploadPolicy.create(
            0L,  // System tenant
            "System Default",
            PolicyRules.builder()
                .allowMimeTypes("*/*")
                .maxFileSize(1024 * 1024 * 1024L)  // 1GB
                .minFileSize(1L)
                .allowExtensions("*")
                .build(),
            Integer.MAX_VALUE  // 가장 낮은 우선순위
        );
    }

    private String buildCacheKey(Long tenantId, FileMetadata fileMetadata) {
        return String.format("policy:%d:%s:%d",
            tenantId,
            fileMetadata.getMimeType(),
            fileMetadata.getSize()
        );
    }

    // 내부 클래스: PolicyMatch
    private static class PolicyMatch implements Comparable<PolicyMatch> {
        private final UploadPolicy policy;
        private final PolicyEvaluationResult result;

        PolicyMatch(UploadPolicy policy, PolicyEvaluationResult result) {
            this.policy = policy;
            this.result = result;
        }

        boolean isApplicable() {
            return result.isPassed();
        }

        UploadPolicy getPolicy() {
            return policy;
        }

        @Override
        public int compareTo(PolicyMatch other) {
            // 우선순위 기반 정렬
            return policy.compareTo(other.policy);
        }
    }
}
```

### ⚠️ 코딩 컨벤션 체크포인트
- ✅ **캐싱 전략**: 정책 캐싱으로 성능 최적화
- ✅ **명확한 책임**: 정책 결정만 담당
- ✅ **예외 처리**: 테넌트 없음, 비활성 처리

---

## KAN-323: StartExternalDownloadUseCase 구현

### 📌 작업 내용
```java
// 위치: application/src/main/java/com/ryuqq/fileflow/application/download/StartExternalDownloadUseCase.java

/**
 * 외부 다운로드 시작 UseCase
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class StartExternalDownloadUseCase {

    private final UploadSessionPort uploadSessionPort;
    private final ExternalDownloadPort externalDownloadPort;
    private final DownloadTaskScheduler taskScheduler;
    private final PolicyResolverService policyResolver;

    /**
     * 외부 URL 다운로드 시작
     *
     * @param command 다운로드 명령
     * @return 다운로드 시작 응답
     */
    @Transactional
    public StartDownloadResponse execute(StartDownloadCommand command) {
        // 1. URL 검증 (Domain에서 처리)
        // 2. 정책 확인 (External Download 허용 여부)
        validateDownloadPolicy(command);

        // 3. 업로드 세션 생성
        UploadSession session = createUploadSession(command);

        // 4. External Download 생성
        ExternalDownload download = ExternalDownload.create(
            session.getId(),
            command.getSourceUrl()
        );

        // 5. 저장
        UploadSession savedSession = uploadSessionPort.save(session);
        ExternalDownload savedDownload = externalDownloadPort.save(download);

        // 6. 비동기 다운로드 작업 스케줄링
        scheduleDownloadTask(savedDownload.getId());

        return new StartDownloadResponse(
            savedSession.getSessionKey(),
            savedDownload.getId(),
            savedDownload.getStatus().name()
        );
    }

    private void validateDownloadPolicy(StartDownloadCommand command) {
        // 예상 파일 메타데이터 (URL에서 추론)
        FileMetadata estimatedMetadata = estimateFileMetadata(command.getSourceUrl());

        UploadPolicy policy = policyResolver.resolvePolicy(
            command.getTenantId(),
            estimatedMetadata
        );

        // External Download가 허용되는지 확인
        if (!policy.getRules().isExternalDownloadAllowed()) {
            throw new PolicyViolationException(
                "External download not allowed for tenant: " + command.getTenantId()
            );
        }
    }

    private FileMetadata estimateFileMetadata(String url) {
        // URL에서 파일 정보 추론
        String fileName = extractFileName(url);
        String extension = extractExtension(fileName);
        String mimeType = guessMimeType(extension);

        return FileMetadata.of(
            fileName,
            null,  // 크기는 다운로드 중 확인
            mimeType
        );
    }

    private UploadSession createUploadSession(StartDownloadCommand command) {
        String fileName = extractFileName(command.getSourceUrl());

        return UploadSession.createForExternalDownload(
            command.getTenantId(),
            fileName,
            command.getSourceUrl()
        );
    }

    private void scheduleDownloadTask(Long downloadId) {
        // 비동기 작업 스케줄링
        taskScheduler.scheduleDownload(downloadId);
    }

    private String extractFileName(String url) {
        try {
            URL parsedUrl = new URL(url);
            String path = parsedUrl.getPath();
            if (path.isEmpty() || path.equals("/")) {
                return "download_" + System.currentTimeMillis();
            }
            return path.substring(path.lastIndexOf('/') + 1);
        } catch (MalformedURLException e) {
            return "download_" + System.currentTimeMillis();
        }
    }

    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    private String guessMimeType(String extension) {
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "pdf" -> "application/pdf";
            case "doc", "docx" -> "application/msword";
            case "xls", "xlsx" -> "application/vnd.ms-excel";
            case "zip" -> "application/zip";
            default -> "application/octet-stream";
        };
    }
}
```

### ⚠️ 코딩 컨벤션 체크포인트
- ✅ **트랜잭션 경계**: 도메인 객체 생성/저장만
- ✅ **비동기 처리**: 실제 다운로드는 별도 Worker
- ✅ **정책 검증**: External Download 허용 여부

---

## KAN-324: ExternalDownloadWorker 구현

### 📌 작업 내용
```java
// 위치: application/src/main/java/com/ryuqq/fileflow/application/download/ExternalDownloadWorker.java

/**
 * 외부 다운로드 실행 Worker
 * 백그라운드에서 실제 다운로드 수행
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ExternalDownloadWorker {

    private final ExternalDownloadPort downloadPort;
    private final S3StoragePort s3StoragePort;
    private final RestTemplate restTemplate;
    private final DownloadProgressTracker progressTracker;

    /**
     * 다운로드 실행 (비동기)
     *
     * @param downloadId 다운로드 ID
     * @return 다운로드 결과
     */
    @Async("downloadExecutor")
    @Retryable(
        value = {IOException.class, RestClientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<DownloadResult> executeDownload(Long downloadId) {
        log.info("Starting download task: {}", downloadId);

        try {
            // 1. 다운로드 정보 조회
            ExternalDownload download = loadDownload(downloadId);

            // 2. 다운로드 시작 상태 변경
            markDownloadStarted(download);

            // 3. 실제 다운로드 수행
            DownloadResult result = performDownload(download);

            // 4. 완료 처리
            markDownloadCompleted(download, result);

            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            log.error("Download failed: {}", downloadId, e);
            handleDownloadFailure(downloadId, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    protected ExternalDownload loadDownload(Long downloadId) {
        return downloadPort.findById(downloadId)
            .orElseThrow(() -> new DownloadNotFoundException(downloadId));
    }

    @Transactional
    protected void markDownloadStarted(ExternalDownload download) {
        download.start();
        downloadPort.save(download);
    }

    private DownloadResult performDownload(ExternalDownload download) throws IOException {
        String sourceUrl = download.getSourceUrl();
        String s3Key = generateS3Key(download);

        // HTTP 요청 설정
        RequestCallback requestCallback = request -> {
            request.getHeaders().setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
            request.getHeaders().set("User-Agent", "FileFlow/1.0");
        };

        // 스트리밍 다운로드 및 S3 업로드
        ResponseExtractor<DownloadResult> responseExtractor = response -> {
            long contentLength = response.getHeaders().getContentLength();

            try (InputStream inputStream = response.getBody()) {
                // Progress Tracking Wrapper
                ProgressTrackingInputStream trackingStream = new ProgressTrackingInputStream(
                    inputStream,
                    contentLength,
                    progress -> updateProgress(download, progress)
                );

                // S3로 스트리밍 업로드
                S3UploadResult s3Result = s3StoragePort.uploadStream(
                    s3Key,
                    trackingStream,
                    contentLength
                );

                return new DownloadResult(
                    s3Result.getEtag(),
                    s3Result.getSize(),
                    s3Key
                );
            }
        };

        return restTemplate.execute(sourceUrl, HttpMethod.GET, requestCallback, responseExtractor);
    }

    private void updateProgress(ExternalDownload download, ProgressUpdate progress) {
        // 진행률 업데이트 (1초 단위 제한)
        if (progressTracker.shouldUpdate(download.getId())) {
            download.updateProgress(
                progress.getBytesTransferred(),
                progress.getTotalBytes()
            );

            // 비동기로 DB 업데이트
            CompletableFuture.runAsync(() -> {
                try {
                    saveProgressUpdate(download);
                } catch (Exception e) {
                    log.warn("Failed to update progress: {}", download.getId(), e);
                }
            });
        }
    }

    @Transactional
    protected void saveProgressUpdate(ExternalDownload download) {
        downloadPort.save(download);
    }

    @Transactional
    protected void markDownloadCompleted(ExternalDownload download, DownloadResult result) {
        download.complete();
        downloadPort.save(download);
        log.info("Download completed: {} -> {}", download.getId(), result.getS3Key());
    }

    @Transactional
    protected void handleDownloadFailure(Long downloadId, Exception e) {
        try {
            ExternalDownload download = downloadPort.findById(downloadId).orElse(null);
            if (download != null) {
                String errorCode = determineErrorCode(e);
                download.fail(errorCode, e.getMessage());
                downloadPort.save(download);

                // 재시도 가능한 경우 재스케줄링
                if (download.canRetry()) {
                    scheduleRetry(download);
                }
            }
        } catch (Exception ex) {
            log.error("Failed to handle download failure: {}", downloadId, ex);
        }
    }

    private String determineErrorCode(Exception e) {
        if (e instanceof SocketTimeoutException) {
            return "TIMEOUT";
        } else if (e instanceof HttpServerErrorException) {
            HttpServerErrorException httpError = (HttpServerErrorException) e;
            return String.valueOf(httpError.getStatusCode().value());
        } else if (e instanceof IOException) {
            return "IO_ERROR";
        }
        return "UNKNOWN";
    }

    private void scheduleRetry(ExternalDownload download) {
        Duration delay = download.getNextRetryDelay();
        log.info("Scheduling retry for download {} after {}", download.getId(), delay);

        // TaskScheduler를 통한 재시도 스케줄링
        CompletableFuture.delayedExecutor(
            delay.toMillis(),
            TimeUnit.MILLISECONDS
        ).execute(() -> executeDownload(download.getId()));
    }

    private String generateS3Key(ExternalDownload download) {
        return String.format("external-downloads/%d/%s",
            download.getUploadSessionId(),
            UUID.randomUUID().toString()
        );
    }
}

// Progress Tracking InputStream
class ProgressTrackingInputStream extends FilterInputStream {
    private final long totalBytes;
    private long bytesRead = 0;
    private final Consumer<ProgressUpdate> progressCallback;
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 100; // 100ms

    public ProgressTrackingInputStream(
        InputStream in,
        long totalBytes,
        Consumer<ProgressUpdate> progressCallback
    ) {
        super(in);
        this.totalBytes = totalBytes;
        this.progressCallback = progressCallback;
    }

    @Override
    public int read() throws IOException {
        int result = super.read();
        if (result != -1) {
            bytesRead++;
            updateProgress();
        }
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = super.read(b, off, len);
        if (result != -1) {
            bytesRead += result;
            updateProgress();
        }
        return result;
    }

    private void updateProgress() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime >= UPDATE_INTERVAL) {
            progressCallback.accept(new ProgressUpdate(bytesRead, totalBytes));
            lastUpdateTime = currentTime;
        }
    }
}
```

### ⚠️ 코딩 컨벤션 체크포인트
- ✅ **비동기 처리**: `@Async` 사용
- ✅ **재시도 메커니즘**: `@Retryable` 설정
- ✅ **스트리밍 처리**: 메모리 효율적 구현
- ✅ **진행률 추적**: 주기적 업데이트

---

## 테스트 가이드

### 단위 테스트
```java
@ExtendWith(MockitoExtension.class)
class ExternalDownloadTest {

    @Test
    @DisplayName("재시도 가능한 에러 판별")
    void should_identify_retryable_errors() {
        // given
        ExternalDownload download = ExternalDownload.create(1L, "http://example.com/file.pdf");

        // when
        download.fail("500", "Server Error");

        // then
        assertThat(download.canRetry()).isTrue();
        assertThat(download.getStatus()).isEqualTo(ExternalDownloadStatus.RETRYING);
    }

    @Test
    @DisplayName("지수 백오프 딜레이 계산")
    void should_calculate_exponential_backoff() {
        // given
        ExternalDownload download = ExternalDownload.create(1L, "http://example.com/file.pdf");

        // when & then
        download.fail("TIMEOUT", "Timeout");
        assertThat(download.getNextRetryDelay()).isEqualTo(Duration.ofSeconds(1));

        download.fail("TIMEOUT", "Timeout");
        assertThat(download.getNextRetryDelay()).isEqualTo(Duration.ofSeconds(2));

        download.fail("TIMEOUT", "Timeout");
        assertThat(download.getNextRetryDelay()).isEqualTo(Duration.ofSeconds(4));
    }
}
```

---

## 다음 Phase: Phase 2C

Phase 2C에서는 다음을 구현합니다:
- Domain Event 발행 메커니즘
- Anti-Corruption Layer
- 멱등성 보장
- 배치 작업

각 태스크는 동일한 코딩 컨벤션을 준수하며, 특히 이벤트 처리 시 트랜잭션 경계에 주의해야 합니다.