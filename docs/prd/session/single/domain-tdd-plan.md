# Domain Layer TDD Plan - Single Presigned URL Upload

**Bounded Context**: `session/single`
**Layer**: Domain
**작성일**: 2025-11-18
**TDD Methodology**: Kent Beck Red-Green-Refactor

---

## 📋 목차

1. [Cycle 1-11: Value Objects (11개)](#value-objects)
2. [Cycle 12-14: Enums (3개)](#enums)
3. [Cycle 15-16: Aggregates (2개)](#aggregates)
4. [Cycle 17-21: Domain Exceptions (5개)](#domain-exceptions)

**전체 21 Cycles**

---

## Value Objects

### Cycle 1: FileId (UUID v7)

**책임**: 파일 고유 식별자, 시간 순서 정렬 가능

#### Red (test:)

```java
// FileIdTest.java
class FileIdTest {
    @Test
    @DisplayName("UUID v7로 FileId를 생성해야 한다")
    void shouldGenerateFileIdWithUuidV7() {
        FileId fileId = FileId.generate();

        assertThat(fileId.value()).hasSize(36);
        assertThat(fileId.value()).contains("-");
    }

    @Test
    @DisplayName("UUID 문자열로 FileId를 생성해야 한다")
    void shouldCreateFileIdFromUuid() {
        String uuid = "01JD8001-1234-5678-9abc-def012345678";
        FileId fileId = FileId.of(uuid);

        assertThat(fileId.uuid()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("생성 시각 순서대로 정렬되어야 한다 (UUID v7)")
    void shouldBeSortableByCreationTime() throws InterruptedException {
        FileId first = FileId.generate();
        Thread.sleep(10);
        FileId second = FileId.generate();

        assertThat(first.value()).isLessThan(second.value());
    }
}
```

#### Green (feat:)

```java
// FileId.java
/**
 * 파일 고유 식별자 (UUID v7)
 * <p>
 * - UUID v7: 시간 기반 정렬 가능 (Timestamp 포함)
 * - S3 Key 생성 시 사용
 * - Zero-Tolerance: Plain Java (Lombok 금지)
 * </p>
 */
public record FileId(String value) {

    public static FileId generate() {
        return new FileId(UuidCreator.getTimeOrderedEpoch().toString());
    }

    public static FileId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("FileId는 필수입니다");
        }
        return new FileId(value);
    }

    public String uuid() {
        return value;
    }
}
```

#### 커밋

```bash
test: FileId VO 테스트 추가 (UUID v7)
feat: FileId VO 구현 (UUID v7 시간 정렬 지원)
```

---

### Cycle 2: SessionId (UUID v7)

**책임**: 멱등키, 세션 고유 식별자

#### Red (test:)

```java
// SessionIdTest.java
class SessionIdTest {
    @Test
    @DisplayName("UUID v7로 SessionId를 생성해야 한다")
    void shouldGenerateSessionIdWithUuidV7() {
        SessionId sessionId = SessionId.generate();

        assertThat(sessionId.value()).hasSize(36);
    }

    @Test
    @DisplayName("UUID 문자열로 SessionId를 생성해야 한다")
    void shouldCreateSessionIdFromUuid() {
        String uuid = "01JD8000-1234-5678-9abc-def012345678";
        SessionId sessionId = SessionId.of(uuid);

        assertThat(sessionId.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("null 또는 빈 문자열로 생성 시 예외를 던져야 한다")
    void shouldThrowExceptionWhenValueIsNullOrBlank() {
        assertThatThrownBy(() -> SessionId.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("SessionId는 필수입니다");

        assertThatThrownBy(() -> SessionId.of(""))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
```

#### Green (feat:)

```java
// SessionId.java
public record SessionId(String value) {

    public static SessionId generate() {
        return new SessionId(UuidCreator.getTimeOrderedEpoch().toString());
    }

    public static SessionId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SessionId는 필수입니다");
        }
        return new SessionId(value);
    }
}
```

#### 커밋

```bash
test: SessionId VO 테스트 추가 (멱등키)
feat: SessionId VO 구현 (UUID v7)
```

---

### Cycle 3: FileName (파일명)

**책임**: 파일명 검증 (길이 1-255자)

#### Red (test:)

```java
// FileNameTest.java
class FileNameTest {
    @Test
    @DisplayName("정상 파일명으로 FileName을 생성해야 한다")
    void shouldCreateFileName() {
        FileName fileName = FileName.of("example.jpg");

        assertThat(fileName.value()).isEqualTo("example.jpg");
    }

    @Test
    @DisplayName("null 또는 빈 문자열로 생성 시 예외를 던져야 한다")
    void shouldThrowExceptionWhenValueIsNullOrBlank() {
        assertThatThrownBy(() -> FileName.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("파일명은 필수입니다");
    }

    @Test
    @DisplayName("255자 초과 파일명으로 생성 시 예외를 던져야 한다")
    void shouldThrowExceptionWhenValueExceeds255Chars() {
        String longName = "a".repeat(256) + ".jpg";

        assertThatThrownBy(() -> FileName.of(longName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("파일명은 최대 255자입니다");
    }
}
```

#### Green (feat:)

```java
// FileName.java
public record FileName(String value) {

    private static final int MAX_LENGTH = 255;

    public static FileName of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("파일명은 필수입니다");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("파일명은 최대 255자입니다");
        }
        return new FileName(value);
    }
}
```

#### 커밋

```bash
test: FileName VO 테스트 추가 (1-255자 검증)
feat: FileName VO 구현 (길이 검증)
```

---

### Cycle 4: FileSize (파일 크기)

**책임**: 파일 크기 검증 (1 byte ~ 1GB)

#### Red (test:)

```java
// FileSizeTest.java
class FileSizeTest {
    @Test
    @DisplayName("정상 파일 크기로 FileSize를 생성해야 한다")
    void shouldCreateFileSize() {
        FileSize fileSize = FileSize.of(1048576L); // 1MB

        assertThat(fileSize.bytes()).isEqualTo(1048576L);
    }

    @Test
    @DisplayName("null 또는 0 이하 값으로 생성 시 예외를 던져야 한다")
    void shouldThrowExceptionWhenValueIsNullOrNonPositive() {
        assertThatThrownBy(() -> FileSize.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("파일 크기는 1 byte 이상이어야 합니다");

        assertThatThrownBy(() -> FileSize.of(0L))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("1GB 초과 시 FileSizeExceededException을 던져야 한다")
    void shouldThrowFileSizeExceededExceptionWhenExceeds1GB() {
        Long overSize = 1073741825L; // 1GB + 1 byte

        assertThatThrownBy(() -> FileSize.of(overSize))
            .isInstanceOf(FileSizeExceededException.class)
            .hasMessageContaining("파일 크기 초과");
    }
}
```

#### Green (feat:)

```java
// FileSize.java
public record FileSize(Long bytes) {

    private static final long MAX_SIZE = 1073741824L; // 1GB

    public static FileSize of(Long bytes) {
        if (bytes == null || bytes <= 0) {
            throw new IllegalArgumentException("파일 크기는 1 byte 이상이어야 합니다");
        }
        if (bytes > MAX_SIZE) {
            throw new FileSizeExceededException(bytes, MAX_SIZE);
        }
        return new FileSize(bytes);
    }
}
```

#### 커밋

```bash
test: FileSize VO 테스트 추가 (1 byte ~ 1GB 검증)
feat: FileSize VO 구현 (범위 검증)
```

---

### Cycle 5: MimeType (MIME 타입)

**책임**: MIME 타입 화이트리스트 검증

#### Red (test:)

```java
// MimeTypeTest.java
class MimeTypeTest {
    @Test
    @DisplayName("허용된 MIME 타입으로 MimeType을 생성해야 한다")
    void shouldCreateMimeTypeWithAllowedType() {
        MimeType mimeType = MimeType.of("image/jpeg");

        assertThat(mimeType.value()).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("대소문자 정규화를 수행해야 한다")
    void shouldNormalizeMimeTypeToLowerCase() {
        MimeType mimeType = MimeType.of("Image/JPEG");

        assertThat(mimeType.value()).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("허용되지 않은 MIME 타입으로 생성 시 예외를 던져야 한다")
    void shouldThrowExceptionWhenMimeTypeNotAllowed() {
        assertThatThrownBy(() -> MimeType.of("video/mp4"))
            .isInstanceOf(UnsupportedMimeTypeException.class)
            .hasMessageContaining("지원하지 않는 MIME Type입니다");
    }
}
```

#### Green (feat:)

```java
// MimeType.java
public record MimeType(String value) {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp",
        "application/pdf",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    public static MimeType of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("MIME Type은 필수입니다");
        }
        String normalized = value.toLowerCase();
        if (!ALLOWED_MIME_TYPES.contains(normalized)) {
            throw new UnsupportedMimeTypeException(value);
        }
        return new MimeType(normalized);
    }
}
```

#### 커밋

```bash
test: MimeType VO 테스트 추가 (화이트리스트 검증)
feat: MimeType VO 구현 (허용 목록 검증)
```

---

### Cycle 6: S3Key (스토리지 경로)

**책임**: UploaderType별 S3 Object Key 생성

#### Red (test:)

```java
// S3KeyTest.java
class S3KeyTest {
    @Test
    @DisplayName("Admin 경로로 S3Key를 생성해야 한다")
    void shouldGenerateS3KeyForAdmin() {
        S3Key s3Key = S3Key.generate(
            TenantId.of(1L),
            UploaderType.ADMIN,
            "connectly",
            FileCategory.of("banner", UploaderType.ADMIN),
            FileId.of("01JD8001-1234-5678-9abc-def012345678"),
            FileName.of("메인배너.jpg")
        );

        assertThat(s3Key.value()).contains("uploads/1/admin/connectly/banner/");
        assertThat(s3Key.value()).contains("01JD8001-1234-5678-9abc-def012345678_메인배너.jpg");
    }

    @Test
    @DisplayName("Seller 경로로 S3Key를 생성해야 한다")
    void shouldGenerateS3KeyForSeller() {
        S3Key s3Key = S3Key.generate(
            TenantId.of(1L),
            UploaderType.SELLER,
            "samsung-electronics",
            FileCategory.of("product", UploaderType.SELLER),
            FileId.of("01JD8010-1234-5678-9abc-def012345678"),
            FileName.of("갤럭시.jpg")
        );

        assertThat(s3Key.value()).contains("uploads/1/seller/samsung-electronics/product/");
    }

    @Test
    @DisplayName("Customer 경로로 S3Key를 생성해야 한다")
    void shouldGenerateS3KeyForCustomer() {
        S3Key s3Key = S3Key.generate(
            TenantId.of(1L),
            UploaderType.CUSTOMER,
            null, // Customer는 slug 없음
            FileCategory.defaultCategory(),
            FileId.of("01JD8100-1234-5678-9abc-def012345678"),
            FileName.of("리뷰.jpg")
        );

        assertThat(s3Key.value()).isEqualTo("uploads/1/customer/default/01JD8100-1234-5678-9abc-def012345678_리뷰.jpg");
    }
}
```

#### Green (feat:)

```java
// S3Key.java
public record S3Key(String value) {

    public static S3Key generate(
        TenantId tenantId,
        UploaderType uploaderType,
        String uploaderSlug,
        FileCategory category,
        FileId fileId,
        FileName fileName
    ) {
        String key;

        if (uploaderType == UploaderType.ADMIN || uploaderType == UploaderType.SELLER) {
            key = String.format(
                "uploads/%d/%s/%s/%s/%s_%s",
                tenantId.value(),
                uploaderType.name().toLowerCase(),
                uploaderSlug,
                category.value(),
                fileId.uuid(),
                fileName.value()
            );
        } else {
            key = String.format(
                "uploads/%d/customer/default/%s_%s",
                tenantId.value(),
                fileId.uuid(),
                fileName.value()
            );
        }

        return new S3Key(key);
    }
}
```

#### 커밋

```bash
test: S3Key VO 테스트 추가 (UploaderType별 경로 생성)
feat: S3Key VO 구현 (Admin/Seller/Customer 경로 분기)
```

---

### Cycle 7-11: 나머지 VOs

**나머지 6개 VO (S3Bucket, TenantId, UploaderId, FileCategory, PresignedUrl, UploadType) 동일 패턴 적용**

- Cycle 7: S3Bucket
- Cycle 8: TenantId
- Cycle 9: UploaderId
- Cycle 10: FileCategory
- Cycle 11: PresignedUrl

---

## Enums

### Cycle 12-14: Enums (3개)

**동일 Red-Green-Refactor 패턴**:
- Cycle 12: UploaderType
- Cycle 13: FileStatus
- Cycle 14: SessionStatus

---

## Aggregates

### Cycle 15: UploadSession Aggregate Root

**책임**: 세션 기반 멱등성 관리, Presigned URL 발급 추적

#### Red (test:)

```java
// UploadSessionTest.java
class UploadSessionTest {

    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2025-01-18T12:00:00Z"), ZoneId.systemDefault());
    }

    @Test
    @DisplayName("세션을 초기화해야 한다 (INITIATED 상태)")
    void shouldInitiateSession() {
        UploadSession session = UploadSession.initiate(
            SessionId.generate(),
            TenantId.of(1L),
            FileName.of("example.jpg"),
            FileSize.of(1048576L),
            MimeType.of("image/jpeg"),
            UploadType.SINGLE,
            PresignedUrl.of("https://example.com/presigned"),
            clock
        );

        assertThat(session.status()).isEqualTo(SessionStatus.INITIATED);
        assertThat(session.expiresAt()).isEqualTo(LocalDateTime.now(clock).plusMinutes(5));
    }

    @Test
    @DisplayName("5분 이내 세션은 만료되지 않아야 한다")
    void shouldNotExpireWithin5Minutes() {
        UploadSession session = createSession(clock);

        assertThatCode(() -> session.ensureNotExpired(clock))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("5분 경과 후 세션은 만료되어야 한다")
    void shouldExpireAfter5Minutes() {
        UploadSession session = createSession(clock);
        Clock expiredClock = Clock.offset(clock, Duration.ofMinutes(6));

        assertThatThrownBy(() -> session.ensureNotExpired(expiredClock))
            .isInstanceOf(SessionExpiredException.class)
            .hasMessageContaining("세션이 만료되었습니다");
    }

    @Test
    @DisplayName("INITIATED → COMPLETED 상태 전환이 가능해야 한다")
    void shouldTransitionFromInitiatedToCompleted() {
        UploadSession session = createSession(clock);

        session.markAsCompleted(clock);

        assertThat(session.status()).isEqualTo(SessionStatus.COMPLETED);
    }

    @Test
    @DisplayName("COMPLETED 세션은 완료 체크 시 예외를 던져야 한다")
    void shouldThrowExceptionWhenSessionAlreadyCompleted() {
        UploadSession session = createSession(clock);
        session.markAsCompleted(clock);

        assertThatThrownBy(() -> session.ensureNotCompleted())
            .isInstanceOf(SessionAlreadyCompletedException.class)
            .hasMessageContaining("이미 완료된 세션입니다");
    }

    private UploadSession createSession(Clock clock) {
        return UploadSession.initiate(
            SessionId.generate(),
            TenantId.of(1L),
            FileName.of("example.jpg"),
            FileSize.of(1048576L),
            MimeType.of("image/jpeg"),
            UploadType.SINGLE,
            PresignedUrl.of("https://example.com/presigned"),
            clock
        );
    }
}
```

#### Green (feat:)

```java
// UploadSession.java
public class UploadSession {

    private SessionId sessionId;
    private TenantId tenantId;
    private FileName fileName;
    private FileSize fileSize;
    private MimeType mimeType;
    private UploadType uploadType;
    private PresignedUrl presignedUrl;
    private LocalDateTime expiresAt;
    private SessionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UploadSession() {}

    public static UploadSession initiate(
        SessionId sessionId,
        TenantId tenantId,
        FileName fileName,
        FileSize fileSize,
        MimeType mimeType,
        UploadType uploadType,
        PresignedUrl presignedUrl,
        Clock clock
    ) {
        UploadSession session = new UploadSession();
        session.sessionId = sessionId;
        session.tenantId = tenantId;
        session.fileName = fileName;
        session.fileSize = fileSize;
        session.mimeType = mimeType;
        session.uploadType = uploadType;
        session.presignedUrl = presignedUrl;
        session.status = SessionStatus.INITIATED;
        session.createdAt = LocalDateTime.now(clock);
        session.updatedAt = LocalDateTime.now(clock);
        session.expiresAt = LocalDateTime.now(clock).plusMinutes(5);
        return session;
    }

    public void ensureNotExpired(Clock clock) {
        if (LocalDateTime.now(clock).isAfter(expiresAt)) {
            throw new SessionExpiredException(sessionId);
        }
    }

    public void ensureNotCompleted() {
        if (status == SessionStatus.COMPLETED) {
            throw new SessionAlreadyCompletedException(sessionId);
        }
    }

    public void markAsCompleted(Clock clock) {
        if (status != SessionStatus.INITIATED && status != SessionStatus.IN_PROGRESS) {
            throw new InvalidSessionStatusException(sessionId, status, SessionStatus.COMPLETED);
        }
        this.status = SessionStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now(clock);
    }

    public void markAsInProgress(Clock clock) {
        this.status = SessionStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now(clock);
    }

    // Getters (Plain Java, Law of Demeter)
    public SessionId sessionId() { return sessionId; }
    public TenantId tenantId() { return tenantId; }
    public FileName fileName() { return fileName; }
    public FileSize fileSize() { return fileSize; }
    public MimeType mimeType() { return mimeType; }
    public UploadType uploadType() { return uploadType; }
    public PresignedUrl presignedUrl() { return presignedUrl; }
    public LocalDateTime expiresAt() { return expiresAt; }
    public SessionStatus status() { return status; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime updatedAt() { return updatedAt; }
}
```

#### 커밋

```bash
test: UploadSession Aggregate 테스트 추가 (멱등성, 만료, 상태 전환)
feat: UploadSession Aggregate 구현 (Plain Java, Tell Don't Ask)
```

---

### Cycle 16: File Aggregate Root

**동일 패턴 적용**

---

## Domain Exceptions

### Cycle 17-21: Domain Exceptions (5개)

**Red-Green 패턴**:
- Cycle 17: SessionExpiredException
- Cycle 18: SessionAlreadyCompletedException
- Cycle 19: InvalidSessionStatusException
- Cycle 20: FileSizeExceededException
- Cycle 21: UnsupportedMimeTypeException

---

## 완료 조건

- [x] 11개 Value Objects (Record 기반)
- [x] 3개 Enums
- [x] 2개 Aggregates (Plain Java, Law of Demeter)
- [x] 5개 Domain Exceptions
- [x] Lombok 금지 (Zero-Tolerance)
- [x] Tell Don't Ask 패턴 적용

---

**작성자**: Claude (Anthropic)
**검토자**: ryu-qqq
**변경 이력**:
- 2025-11-18: TDD Plan 변환 완료 (Kent Beck Red-Green-Refactor)
