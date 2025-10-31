# KAN-144: Domain Layer 개발 태스크

## 📋 Domain Layer 개요

**레이어 역할**: 순수 비즈니스 로직, 외부 의존성 없음
**패키지**: `com.ryuqq.fileflow.domain`
**핵심 원칙**: Lombok 금지, Law of Demeter, Tell Don't Ask

---

## 🎯 Domain Layer 태스크 목록

### Phase 2A: Multipart Upload Domain (3 Tasks)

#### KAN-310: MultipartUpload Aggregate Root 구현

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/upload/MultipartUpload.java`

**목표**: 대용량 파일 업로드를 위한 MultipartUpload Aggregate Root 구현 (상태 머신 패턴)

**구현 상세**:

```java
/**
 * Multipart Upload Aggregate Root
 * 대용량 파일의 분할 업로드 상태를 관리하는 Aggregate
 *
 * 비즈니스 규칙:
 * 1. 파트 번호는 1부터 시작하며 연속되어야 함
 * 2. 모든 파트가 업로드된 후에만 완료 가능
 * 3. 상태 전환은 정의된 규칙에 따라서만 가능 (INIT → IN_PROGRESS → COMPLETED/ABORTED/FAILED)
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class MultipartUpload {

    // 식별자
    private final Long id;
    private final Long uploadSessionId;  // Long FK Strategy (NO @ManyToOne)

    // S3 관련 정보
    private String providerUploadId;  // S3 UploadId (변경 가능)

    // 상태 정보
    private MultipartStatus status;
    private Integer totalParts;
    private final List<UploadPart> uploadedParts;

    // 시간 정보
    private final LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime abortedAt;

    /**
     * 상태 Enum
     */
    public enum MultipartStatus {
        INIT,           // 초기화 전
        IN_PROGRESS,    // 업로드 진행 중
        COMPLETED,      // 완료
        ABORTED,        // 사용자에 의한 중단
        FAILED          // 시스템 오류로 실패
    }

    // Private 생성자 (외부에서 직접 생성 불가)
    private MultipartUpload(Long uploadSessionId) {
        this.id = null;  // DB에서 자동 생성
        this.uploadSessionId = uploadSessionId;
        this.status = MultipartStatus.INIT;
        this.uploadedParts = new ArrayList<>();
        this.startedAt = LocalDateTime.now();
    }

    /**
     * Static Factory Method - Aggregate 생성
     */
    public static MultipartUpload create(Long uploadSessionId) {
        if (uploadSessionId == null) {
            throw new IllegalArgumentException("Upload session ID cannot be null");
        }
        return new MultipartUpload(uploadSessionId);
    }

    /**
     * Multipart 업로드 시작
     * 상태: INIT → IN_PROGRESS
     *
     * @param providerUploadId S3 UploadId
     * @param totalParts 총 파트 수
     */
    public void initiate(String providerUploadId, Integer totalParts) {
        validateInitiation();
        validateTotalParts(totalParts);

        this.providerUploadId = providerUploadId;
        this.totalParts = totalParts;
        this.status = MultipartStatus.IN_PROGRESS;
    }

    /**
     * 파트 추가
     * 파트 번호 중복 및 순서 검증
     *
     * @param part 업로드된 파트
     */
    public void addPart(UploadPart part) {
        validatePartAddition(part);
        this.uploadedParts.add(part);
    }

    /**
     * Multipart 업로드 완료
     * 상태: IN_PROGRESS → COMPLETED
     */
    public void complete() {
        if (!canComplete()) {
            throw new IllegalStateException(
                "Cannot complete: not all parts uploaded or invalid state"
            );
        }
        this.status = MultipartStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Multipart 업로드 중단
     * 상태: * → ABORTED
     */
    public void abort() {
        if (this.status == MultipartStatus.COMPLETED) {
            throw new IllegalStateException("Cannot abort completed upload");
        }
        this.status = MultipartStatus.ABORTED;
        this.abortedAt = LocalDateTime.now();
    }

    /**
     * Multipart 업로드 실패
     * 상태: * → FAILED
     *
     * @param reason 실패 사유
     */
    public void fail(String reason) {
        this.status = MultipartStatus.FAILED;
        // reason은 UploadSession에서 관리 (SRP)
    }

    // ===== Tell, Don't Ask 패턴 =====

    /**
     * 완료 가능 여부 확인
     *
     * @return 완료 가능하면 true
     */
    public boolean canComplete() {
        return status == MultipartStatus.IN_PROGRESS
            && uploadedParts.size() == totalParts
            && hasAllPartsInSequence();
    }

    /**
     * 진행 중인지 확인
     */
    public boolean isInProgress() {
        return status == MultipartStatus.IN_PROGRESS;
    }

    /**
     * 완료되었는지 확인
     */
    public boolean isCompleted() {
        return status == MultipartStatus.COMPLETED;
    }

    // ===== 검증 메서드 (Private) =====

    private void validateInitiation() {
        if (this.status != MultipartStatus.INIT) {
            throw new IllegalStateException(
                "Multipart already initiated: " + status
            );
        }
    }

    private void validateTotalParts(Integer totalParts) {
        if (totalParts == null || totalParts < 1 || totalParts > 10000) {
            throw new IllegalArgumentException(
                "Total parts must be between 1 and 10000: " + totalParts
            );
        }
    }

    private void validatePartAddition(UploadPart part) {
        if (part == null) {
            throw new IllegalArgumentException("Part cannot be null");
        }

        if (this.status != MultipartStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                "Cannot add part in status: " + status
            );
        }

        // 중복 파트 번호 검증
        boolean duplicate = uploadedParts.stream()
            .anyMatch(p -> p.getPartNumber().equals(part.getPartNumber()));

        if (duplicate) {
            throw new IllegalArgumentException(
                "Duplicate part number: " + part.getPartNumber()
            );
        }
    }

    /**
     * 모든 파트가 순서대로 존재하는지 검증
     */
    private boolean hasAllPartsInSequence() {
        Set<Integer> partNumbers = uploadedParts.stream()
            .map(UploadPart::getPartNumber)
            .collect(Collectors.toSet());

        for (int i = 1; i <= totalParts; i++) {
            if (!partNumbers.contains(i)) {
                return false;
            }
        }
        return true;
    }

    // ===== Getter (필요한 것만 제공, NO Setter) =====

    public Long getId() {
        return id;
    }

    public Long getUploadSessionId() {
        return uploadSessionId;
    }

    public String getProviderUploadId() {
        return providerUploadId;
    }

    public MultipartStatus getStatus() {
        return status;
    }

    public Integer getTotalParts() {
        return totalParts;
    }

    /**
     * 방어적 복사 - 외부 변경 방지
     */
    public List<UploadPart> getUploadedParts() {
        return Collections.unmodifiableList(uploadedParts);
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
```

**Zero-Tolerance 체크리스트**:
- [ ] NO Lombok (모든 getter 수동 작성)
- [ ] Law of Demeter 준수 (getter 체이닝 없음)
- [ ] Tell, Don't Ask (`canComplete()`, `isInProgress()` 등)
- [ ] Static Factory Method (`create()`)
- [ ] Immutable Collection (`unmodifiableList`)
- [ ] Javadoc 작성 (@author, @since)

**테스트 시나리오**:
```java
@Test
void multipart_상태_전환_테스트() {
    // given
    MultipartUpload upload = MultipartUpload.create(1L);

    // when
    upload.initiate("s3-upload-id", 3);

    // then
    assertThat(upload.getStatus()).isEqualTo(MultipartStatus.IN_PROGRESS);
    assertThat(upload.canComplete()).isFalse();
}

@Test
void 모든_파트_업로드_후_완료() {
    // given
    MultipartUpload upload = MultipartUpload.create(1L);
    upload.initiate("s3-upload-id", 2);

    upload.addPart(UploadPart.of(1, "etag1", 5242880L));
    upload.addPart(UploadPart.of(2, "etag2", 3000000L));

    // when
    upload.complete();

    // then
    assertThat(upload.isCompleted()).isTrue();
}

@Test
void 중복_파트_추가_거부() {
    // given
    MultipartUpload upload = MultipartUpload.create(1L);
    upload.initiate("s3-upload-id", 2);
    UploadPart part1 = UploadPart.of(1, "etag1", 5242880L);

    upload.addPart(part1);

    // when & then
    assertThatThrownBy(() -> upload.addPart(part1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Duplicate part number");
}
```

---

#### KAN-311: UploadPart Value Object 구현

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/upload/UploadPart.java`

**목표**: Multipart의 각 파트를 표현하는 불변 Value Object

**구현 상세**:

```java
/**
 * Upload Part Value Object
 * Multipart Upload의 개별 파트를 표현하는 불변 객체
 *
 * 비즈니스 규칙:
 * 1. 파트 번호는 1-10000 범위
 * 2. 파트 크기는 최소 5MB (마지막 파트 제외)
 * 3. ETag는 필수 (S3에서 반환)
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class UploadPart {

    private final Integer partNumber;      // 파트 번호 (1-10000)
    private final String etag;              // S3 ETag (MD5 체크섬)
    private final Long size;                // 파트 크기 (bytes)
    private final String checksum;          // Optional: SHA256 체크섬
    private final LocalDateTime uploadedAt; // 업로드 완료 시간

    // Private 생성자 (직접 생성 불가)
    private UploadPart(
        Integer partNumber,
        String etag,
        Long size,
        String checksum
    ) {
        this.partNumber = validatePartNumber(partNumber);
        this.etag = validateEtag(etag);
        this.size = validateSize(size);
        this.checksum = checksum;  // Optional
        this.uploadedAt = LocalDateTime.now();
    }

    /**
     * Static Factory Method
     *
     * @param partNumber 파트 번호
     * @param etag S3 ETag
     * @param size 파트 크기
     * @return UploadPart 인스턴스
     */
    public static UploadPart of(Integer partNumber, String etag, Long size) {
        return new UploadPart(partNumber, etag, size, null);
    }

    /**
     * Static Factory Method (with checksum)
     */
    public static UploadPart of(
        Integer partNumber,
        String etag,
        Long size,
        String checksum
    ) {
        return new UploadPart(partNumber, etag, size, checksum);
    }

    // ===== 검증 메서드 =====

    private static Integer validatePartNumber(Integer partNumber) {
        if (partNumber == null || partNumber < 1 || partNumber > 10000) {
            throw new IllegalArgumentException(
                "Part number must be between 1 and 10000: " + partNumber
            );
        }
        return partNumber;
    }

    private static String validateEtag(String etag) {
        if (etag == null || etag.isBlank()) {
            throw new IllegalArgumentException("ETag cannot be empty");
        }
        return etag.trim();
    }

    private static Long validateSize(Long size) {
        if (size == null || size < 0) {
            throw new IllegalArgumentException(
                "Size must be non-negative: " + size
            );
        }
        // 최소 크기는 5MB (마지막 파트 제외)
        // 하지만 여기서는 마지막 파트 여부를 알 수 없으므로 검증 생략
        return size;
    }

    // ===== Value Object 필수: equals & hashCode =====

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

    @Override
    public String toString() {
        return String.format(
            "UploadPart{partNumber=%d, etag='%s', size=%d, uploadedAt=%s}",
            partNumber, etag, size, uploadedAt
        );
    }

    // ===== Getter (NO Setter, 불변 객체) =====

    public Integer getPartNumber() {
        return partNumber;
    }

    public String getEtag() {
        return etag;
    }

    public Long getSize() {
        return size;
    }

    public String getChecksum() {
        return checksum;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}
```

**Zero-Tolerance 체크리스트**:
- [ ] 완전 불변 (모든 필드 final, setter 없음)
- [ ] 검증 로직 (생성 시점)
- [ ] Value Object 패턴 (equals/hashCode)
- [ ] Static Factory Method (`of()`)
- [ ] Javadoc 작성

**테스트 시나리오**:
```java
@Test
void 유효한_파트_생성() {
    // when
    UploadPart part = UploadPart.of(1, "etag-abc", 5242880L);

    // then
    assertThat(part.getPartNumber()).isEqualTo(1);
    assertThat(part.getEtag()).isEqualTo("etag-abc");
    assertThat(part.getSize()).isEqualTo(5242880L);
}

@Test
void 파트_번호_범위_검증() {
    // when & then
    assertThatThrownBy(() -> UploadPart.of(0, "etag", 5242880L))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> UploadPart.of(10001, "etag", 5242880L))
        .isInstanceOf(IllegalArgumentException.class);
}

@Test
void Value_Object_동등성() {
    // given
    UploadPart part1 = UploadPart.of(1, "etag", 5242880L);
    UploadPart part2 = UploadPart.of(1, "etag", 5242880L);

    // then
    assertThat(part1).isEqualTo(part2);
    assertThat(part1.hashCode()).isEqualTo(part2.hashCode());
}
```

---

#### KAN-312: UploadSession Aggregate 확장

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/upload/UploadSession.java`

**목표**: 기존 UploadSession에 Multipart 업로드 지원 추가

**구현 상세**:

```java
/**
 * Upload Session Aggregate Root (확장)
 * Multipart 업로드 지원 추가
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class UploadSession {

    // 기존 필드들...
    private final Long id;
    private final String sessionKey;
    private final Long tenantId;
    // ...

    // 새로 추가: Multipart 지원
    private UploadType uploadType;             // SINGLE or MULTIPART
    private MultipartUpload multipartUpload;   // Multipart 정보 (Optional)

    /**
     * 업로드 타입 Enum
     */
    public enum UploadType {
        SINGLE,     // 단일 파일 업로드 (기존)
        MULTIPART   // 대용량 파일 분할 업로드 (신규)
    }

    /**
     * Multipart 업로드용 세션 생성
     */
    public static UploadSession createForMultipart(
        Long tenantId,
        String fileName,
        Long fileSize
    ) {
        UploadSession session = new UploadSession(tenantId, fileName, fileSize);
        session.uploadType = UploadType.MULTIPART;
        return session;
    }

    /**
     * Multipart 정보 연결
     *
     * @param multipart MultipartUpload Aggregate
     */
    public void attachMultipart(MultipartUpload multipart) {
        if (this.uploadType != UploadType.MULTIPART) {
            throw new IllegalStateException(
                "Upload type is not MULTIPART: " + uploadType
            );
        }

        if (!multipart.getUploadSessionId().equals(this.id)) {
            throw new IllegalArgumentException(
                "Multipart session ID mismatch"
            );
        }

        this.multipartUpload = multipart;
    }

    /**
     * Multipart 초기화
     */
    public void initMultipart(Integer totalParts) {
        if (this.uploadType != UploadType.MULTIPART) {
            throw new IllegalStateException("Not a multipart upload session");
        }

        if (this.multipartUpload == null) {
            throw new IllegalStateException("Multipart not attached");
        }

        // Delegate to MultipartUpload
        // (실제 초기화는 UseCase에서 수행)
    }

    /**
     * 파트 업로드 완료 마킹
     *
     * @param part 업로드된 파트
     */
    public void markPartUploaded(UploadPart part) {
        if (multipartUpload == null) {
            throw new IllegalStateException("Multipart not initialized");
        }

        multipartUpload.addPart(part);
    }

    /**
     * Multipart 업로드 완료 가능 여부
     */
    public boolean canCompleteMultipart() {
        if (this.uploadType != UploadType.MULTIPART || multipartUpload == null) {
            return false;
        }
        return multipartUpload.canComplete();
    }

    // ===== Getter =====

    public UploadType getUploadType() {
        return uploadType;
    }

    public MultipartUpload getMultipartUpload() {
        return multipartUpload;
    }

    public boolean isMultipart() {
        return uploadType == UploadType.MULTIPART;
    }
}
```

**Zero-Tolerance 체크리스트**:
- [ ] NO Lombok
- [ ] Law of Demeter (상태 확인 메서드)
- [ ] 기존 기능과의 호환성 유지
- [ ] Javadoc 작성

---

### Phase 2B: External Download & Policy Domain (2 Tasks)

#### KAN-320: ExternalDownload Aggregate Root 구현

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/download/ExternalDownload.java`

**목표**: 외부 URL로부터 파일 다운로드 기능 (재시도 로직 포함)

**구현 상세**:

```java
/**
 * External Download Aggregate Root
 * 외부 URL로부터 파일을 다운로드하여 S3에 저장
 *
 * 비즈니스 규칙:
 * 1. HTTP/HTTPS만 지원
 * 2. 최대 3회 재시도 (지수 백오프)
 * 3. 5xx, Timeout 오류만 재시도
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class ExternalDownload {

    private final Long id;
    private final Long uploadSessionId;  // Long FK
    private final URL sourceUrl;          // 검증된 URL

    // 다운로드 진행 상태
    private Long bytesTransferred;
    private Long totalBytes;
    private ExternalDownloadStatus status;

    // 재시도 정책
    private Integer retryCount;
    private final Integer maxRetry = 3;
    private LocalDateTime lastRetryAt;

    // 오류 정보
    private String errorCode;
    private String errorMessage;

    /**
     * 다운로드 상태 Enum
     */
    public enum ExternalDownloadStatus {
        INIT,          // 초기화
        DOWNLOADING,   // 다운로드 진행 중
        COMPLETED,     // 완료
        FAILED,        // 실패 (재시도 불가)
        ABORTED        // 사용자에 의한 중단
    }

    // Private 생성자
    private ExternalDownload(Long uploadSessionId, String url) {
        this.id = null;
        this.uploadSessionId = uploadSessionId;
        this.sourceUrl = validateAndParseUrl(url);
        this.status = ExternalDownloadStatus.INIT;
        this.bytesTransferred = 0L;
        this.retryCount = 0;
    }

    /**
     * Static Factory Method
     */
    public static ExternalDownload create(String sourceUrl, Long uploadSessionId) {
        return new ExternalDownload(uploadSessionId, sourceUrl);
    }

    /**
     * 다운로드 시작
     * 상태: INIT → DOWNLOADING
     */
    public void start() {
        if (this.status != ExternalDownloadStatus.INIT) {
            throw new IllegalStateException(
                "Can only start from INIT state: " + status
            );
        }
        this.status = ExternalDownloadStatus.DOWNLOADING;
    }

    /**
     * 진행률 업데이트
     */
    public void updateProgress(Long transferred, Long total) {
        if (this.status != ExternalDownloadStatus.DOWNLOADING) {
            throw new IllegalStateException("Not downloading: " + status);
        }
        this.bytesTransferred = transferred;
        this.totalBytes = total;
    }

    /**
     * 다운로드 완료
     * 상태: DOWNLOADING → COMPLETED
     */
    public void complete() {
        if (this.status != ExternalDownloadStatus.DOWNLOADING) {
            throw new IllegalStateException("Not downloading: " + status);
        }
        this.status = ExternalDownloadStatus.COMPLETED;
    }

    /**
     * 다운로드 실패 처리
     * 재시도 가능한 경우 상태 유지, 불가능한 경우 FAILED
     */
    public void fail(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;

        if (canRetry(errorCode)) {
            this.retryCount++;
            this.lastRetryAt = LocalDateTime.now();
            // 상태는 DOWNLOADING 유지
        } else {
            this.status = ExternalDownloadStatus.FAILED;
        }
    }

    /**
     * 다운로드 중단
     */
    public void abort() {
        if (this.status == ExternalDownloadStatus.COMPLETED) {
            throw new IllegalStateException("Cannot abort completed download");
        }
        this.status = ExternalDownloadStatus.ABORTED;
    }

    // ===== Tell, Don't Ask =====

    /**
     * 재시도 가능 여부
     */
    public boolean canRetry(String errorCode) {
        return isRetryableError(errorCode) && retryCount < maxRetry;
    }

    /**
     * 진행률 계산 (%)
     */
    public int getProgressPercentage() {
        if (totalBytes == null || totalBytes == 0) {
            return 0;
        }
        return (int) ((bytesTransferred * 100) / totalBytes);
    }

    /**
     * 다음 재시도까지의 대기 시간 계산 (지수 백오프)
     */
    public Duration getNextRetryDelay() {
        if (retryCount >= maxRetry) {
            return Duration.ZERO;
        }
        // 1초, 2초, 4초
        return Duration.ofSeconds((long) Math.pow(2, retryCount));
    }

    // ===== 검증 및 유틸리티 =====

    private static URL validateAndParseUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL cannot be empty");
        }

        try {
            URL parsedUrl = new URL(url);
            String protocol = parsedUrl.getProtocol();

            if (!protocol.matches("https?")) {
                throw new IllegalArgumentException(
                    "Only HTTP/HTTPS protocols are supported: " + protocol
                );
            }

            return parsedUrl;

        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }

    /**
     * 재시도 가능한 오류인지 판단
     * 5xx 서버 오류, Timeout만 재시도
     */
    private boolean isRetryableError(String errorCode) {
        if (errorCode == null) {
            return false;
        }

        // 5xx 서버 오류
        if (errorCode.startsWith("5")) {
            return true;
        }

        // Timeout
        if ("TIMEOUT".equals(errorCode) || "READ_TIMEOUT".equals(errorCode)) {
            return true;
        }

        return false;
    }

    // ===== Getter =====

    public Long getId() { return id; }
    public Long getUploadSessionId() { return uploadSessionId; }
    public URL getSourceUrl() { return sourceUrl; }
    public ExternalDownloadStatus getStatus() { return status; }
    public Long getBytesTransferred() { return bytesTransferred; }
    public Long getTotalBytes() { return totalBytes; }
    public Integer getRetryCount() { return retryCount; }
    public String getErrorCode() { return errorCode; }
    public String getErrorMessage() { return errorMessage; }
}
```

**Zero-Tolerance 체크리스트**:
- [ ] NO Lombok
- [ ] URL 검증 (생성 시점)
- [ ] Tell, Don't Ask (`canRetry()`, `getProgressPercentage()`)
- [ ] 재시도 로직 (지수 백오프)
- [ ] Javadoc 작성

---

#### KAN-321: UploadPolicy Aggregate Root 구현

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/policy/UploadPolicy.java`

**목표**: 테넌트별 업로드 정책 관리

**구현 상세**:

```java
/**
 * Upload Policy Aggregate Root
 * 테넌트별 파일 업로드 정책 관리
 *
 * 비즈니스 규칙:
 * 1. 우선순위가 낮을수록 먼저 적용
 * 2. 활성 상태의 정책만 평가
 * 3. 정책 규칙은 불변 (Value Object)
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public class UploadPolicy {

    private final Long id;
    private final Long tenantId;  // Long FK
    private final String policyName;
    private final PolicyRules rules;
    private PolicyStatus status;
    private final Integer priority;  // 낮을수록 우선

    /**
     * 정책 상태 Enum
     */
    public enum PolicyStatus {
        ACTIVE,      // 활성
        INACTIVE,    // 비활성
        DEPRECATED   // 폐기
    }

    // Private 생성자
    private UploadPolicy(
        Long tenantId,
        String policyName,
        PolicyRules rules,
        Integer priority
    ) {
        this.id = null;
        this.tenantId = tenantId;
        this.policyName = policyName;
        this.rules = rules;
        this.status = PolicyStatus.ACTIVE;
        this.priority = priority;
    }

    /**
     * Static Factory Method
     */
    public static UploadPolicy create(
        Long tenantId,
        String policyName,
        PolicyRules rules,
        Integer priority
    ) {
        return new UploadPolicy(tenantId, policyName, rules, priority);
    }

    /**
     * 기본 정책 생성
     */
    public static UploadPolicy createDefault() {
        PolicyRules defaultRules = PolicyRules.builder()
            .allowAllMimeTypes()
            .maxFileSize(5 * 1024 * 1024 * 1024L)  // 5GB
            .minFileSize(1L)
            .build();

        return new UploadPolicy(0L, "DEFAULT_POLICY", defaultRules, 999);
    }

    /**
     * 정책 평가
     *
     * @param file 파일 메타데이터
     * @return 평가 결과
     */
    public PolicyEvaluationResult evaluate(FileMetadata file) {
        if (!isActive()) {
            return PolicyEvaluationResult.notApplicable(
                "Policy is not active: " + status
            );
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

    /**
     * 정책 활성화
     */
    public void activate() {
        this.status = PolicyStatus.ACTIVE;
    }

    /**
     * 정책 비활성화
     */
    public void deactivate() {
        this.status = PolicyStatus.INACTIVE;
    }

    /**
     * 정책 폐기
     */
    public void deprecate() {
        this.status = PolicyStatus.DEPRECATED;
    }

    // ===== Tell, Don't Ask =====

    public boolean isActive() {
        return status == PolicyStatus.ACTIVE;
    }

    // ===== Getter =====

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getPolicyName() { return policyName; }
    public PolicyRules getRules() { return rules; }
    public PolicyStatus getStatus() { return status; }
    public Integer getPriority() { return priority; }

    /**
     * 정책 규칙 Value Object
     */
    public static final class PolicyRules {

        private final Set<String> allowedMimeTypes;
        private final Long maxFileSize;
        private final Long minFileSize;
        private final Set<String> allowedExtensions;
        private final Boolean scanRequired;   // 바이러스 스캔 필수
        private final Boolean ocrEnabled;     // OCR 처리 활성화

        // Private 생성자
        private PolicyRules(Builder builder) {
            this.allowedMimeTypes = Set.copyOf(builder.allowedMimeTypes);
            this.maxFileSize = builder.maxFileSize;
            this.minFileSize = builder.minFileSize;
            this.allowedExtensions = Set.copyOf(builder.allowedExtensions);
            this.scanRequired = builder.scanRequired;
            this.ocrEnabled = builder.ocrEnabled;
        }

        /**
         * 파일 검증
         */
        public ValidationResult validate(FileMetadata file) {
            List<String> violations = new ArrayList<>();

            // MIME Type 검증
            if (!allowedMimeTypes.isEmpty() &&
                !allowedMimeTypes.contains(file.getMimeType())) {
                violations.add("MIME type not allowed: " + file.getMimeType());
            }

            // 파일 크기 검증
            if (file.getSize() > maxFileSize) {
                violations.add("File too large: " + file.getSize());
            }

            if (file.getSize() < minFileSize) {
                violations.add("File too small: " + file.getSize());
            }

            // 확장자 검증
            String extension = extractExtension(file.getName());
            if (!allowedExtensions.isEmpty() &&
                !allowedExtensions.contains(extension)) {
                violations.add("Extension not allowed: " + extension);
            }

            return violations.isEmpty()
                ? ValidationResult.valid()
                : ValidationResult.invalid(violations);
        }

        private String extractExtension(String fileName) {
            int lastDot = fileName.lastIndexOf('.');
            return (lastDot == -1) ? "" : fileName.substring(lastDot + 1).toLowerCase();
        }

        // Builder 패턴 (수동 구현, NO Lombok)
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Set<String> allowedMimeTypes = new HashSet<>();
            private Long maxFileSize = Long.MAX_VALUE;
            private Long minFileSize = 1L;
            private Set<String> allowedExtensions = new HashSet<>();
            private Boolean scanRequired = false;
            private Boolean ocrEnabled = false;

            public Builder allowMimeTypes(String... types) {
                this.allowedMimeTypes.addAll(Arrays.asList(types));
                return this;
            }

            public Builder allowAllMimeTypes() {
                this.allowedMimeTypes = new HashSet<>();
                return this;
            }

            public Builder maxFileSize(Long size) {
                this.maxFileSize = size;
                return this;
            }

            public Builder minFileSize(Long size) {
                this.minFileSize = size;
                return this;
            }

            public Builder allowExtensions(String... extensions) {
                this.allowedExtensions.addAll(Arrays.asList(extensions));
                return this;
            }

            public Builder requireScan() {
                this.scanRequired = true;
                return this;
            }

            public Builder enableOcr() {
                this.ocrEnabled = true;
                return this;
            }

            public PolicyRules build() {
                return new PolicyRules(this);
            }
        }

        // Getter
        public Set<String> getAllowedMimeTypes() { return allowedMimeTypes; }
        public Long getMaxFileSize() { return maxFileSize; }
        public Long getMinFileSize() { return minFileSize; }
        public Set<String> getAllowedExtensions() { return allowedExtensions; }
        public Boolean getScanRequired() { return scanRequired; }
        public Boolean getOcrEnabled() { return ocrEnabled; }
    }
}
```

**Zero-Tolerance 체크리스트**:
- [ ] NO Lombok (Builder 수동 구현)
- [ ] PolicyRules를 불변 Value Object로
- [ ] Tell, Don't Ask (`isActive()`, `evaluate()`)
- [ ] Javadoc 작성

---

### Phase 2C: Domain Events (1 Task)

#### KAN-327: Domain Events 정의 (4개)

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/event/upload/`

**목표**: 업로드 관련 도메인 이벤트 정의

**구현 상세**:

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

    /**
     * Static Factory Method
     */
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

    // Getter (NO Setter)
    public Long getUploadSessionId() { return uploadSessionId; }
    public String getSessionKey() { return sessionKey; }
    public Long getFileId() { return fileId; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public LocalDateTime getOccurredAt() { return occurredAt; }

    // equals/hashCode (이벤트 중복 방지)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UploadCompletedEvent)) return false;
        UploadCompletedEvent that = (UploadCompletedEvent) o;
        return Objects.equals(uploadSessionId, that.uploadSessionId) &&
               Objects.equals(occurredAt, that.occurredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uploadSessionId, occurredAt);
    }
}

/**
 * 업로드 실패 도메인 이벤트
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class UploadFailedEvent {
    private final Long uploadSessionId;
    private final String sessionKey;
    private final String failureReason;
    private final LocalDateTime occurredAt;

    // (구현 생략 - UploadCompletedEvent와 유사)
}

/**
 * 업로드 만료 도메인 이벤트
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class UploadExpiredEvent {
    private final Long uploadSessionId;
    private final String sessionKey;
    private final LocalDateTime occurredAt;

    // (구현 생략)
}

/**
 * 업로드 중단 도메인 이벤트
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class UploadAbortedEvent {
    private final Long uploadSessionId;
    private final String sessionKey;
    private final LocalDateTime occurredAt;

    // (구현 생략)
}
```

**Zero-Tolerance 체크리스트**:
- [ ] 완전 불변 (모든 필드 final)
- [ ] Static Factory Method (`of()`)
- [ ] equals/hashCode 구현
- [ ] occurredAt 타임스탬프 필수
- [ ] Javadoc 작성

---

## 📊 Domain Layer 완료 체크리스트

### Phase 2A
- [ ] KAN-310: MultipartUpload Aggregate
- [ ] KAN-311: UploadPart Value Object
- [ ] KAN-312: UploadSession 확장

### Phase 2B
- [ ] KAN-320: ExternalDownload Aggregate
- [ ] KAN-321: UploadPolicy Aggregate

### Phase 2C
- [ ] KAN-327: Domain Events (4개)

---

## 🎯 다음 단계

Domain Layer 완료 후 **Application Layer** 개발로 진행합니다.
