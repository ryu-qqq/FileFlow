# Domain Layer - Single Presigned URL Upload

**Bounded Context**: `session/single`
**Layer**: Domain
**작성일**: 2025-11-18

---

## 📋 목차

1. [Value Objects (11개)](#value-objects)
2. [Enums (3개)](#enums)
3. [Aggregates (2개)](#aggregates)
4. [Domain Exceptions (5개)](#domain-exceptions)
5. [Design Principles](#design-principles)

---

## Value Objects

### 1. FileId (UUID v7)

**책임**: 파일 고유 식별자, 시간 순서 정렬 가능

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/shared/vo/FileId.java`

```java
/**
 * 파일 고유 식별자 (UUID v7)
 * <p>
 * - UUID v7: 시간 기반 정렬 가능 (Timestamp 포함)
 * - S3 Key 생성 시 사용
 * - Zero-Tolerance: Plain Java (Lombok 금지)
 * </p>
 */
public record FileId(String value) {

    /**
     * UUID v7 생성
     *
     * @return 새로운 FileId
     */
    public static FileId generate() {
        return new FileId(UuidCreator.getTimeOrderedEpoch().toString());
    }

    /**
     * UUID 문자열 추출
     *
     * @return UUID 문자열 (36자)
     */
    public String uuid() {
        return value;
    }
}
```

**테스트 케이스**:
- UUID v7 형식 검증 (36자, `-` 포함)
- 시간 순서 정렬 가능 여부 (생성 시각 순서대로 정렬)

---

### 2. SessionId (UUID v7)

**책임**: 멱등키, 세션 고유 식별자

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/shared/vo/SessionId.java`

```java
/**
 * 업로드 세션 고유 식별자 (UUID v7)
 * <p>
 * - 멱등성 보장: 동일 sessionId로 중복 발급 방지
 * - UUID v7: 시간 기반 정렬 가능
 * </p>
 */
public record SessionId(String value) {

    /**
     * UUID v7 생성
     *
     * @return 새로운 SessionId
     */
    public static SessionId generate() {
        return new SessionId(UuidCreator.getTimeOrderedEpoch().toString());
    }

    /**
     * 기존 UUID 문자열로 생성
     *
     * @param value UUID 문자열
     * @return SessionId
     * @throws IllegalArgumentException value가 null이거나 빈 문자열인 경우
     */
    public static SessionId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SessionId는 필수입니다");
        }
        return new SessionId(value);
    }
}
```

**테스트 케이스**:
- UUID v7 생성 성공
- of() null 검증
- of() 빈 문자열 검증

---

### 3. FileName (파일명)

**책임**: 파일명 검증 (길이, 금지 문자)

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/shared/vo/FileName.java`

```java
/**
 * 파일명 VO
 * <p>
 * - 길이: 1-255자
 * - 금지 문자: /, \, <, >, :, ", |, ?, * (없음)
 * - Law of Demeter: value() 메서드로 직접 접근
 * </p>
 */
public record FileName(String value) {

    private static final int MAX_LENGTH = 255;

    /**
     * 파일명 검증 및 생성
     *
     * @param value 파일명
     * @return FileName
     * @throws IllegalArgumentException 파일명이 null, 빈 문자열, 또는 255자 초과인 경우
     */
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

**테스트 케이스**:
- 정상 파일명 생성 (예: "example.jpg")
- null 검증 실패
- 빈 문자열 검증 실패
- 255자 초과 검증 실패

---

### 4. FileSize (파일 크기)

**책임**: 파일 크기 검증 (1 byte ~ 1GB)

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/shared/vo/FileSize.java`

```java
/**
 * 파일 크기 VO
 * <p>
 * - 범위: 1 byte ~ 1GB (1,073,741,824 bytes)
 * - 0 이하 불가
 * - MVP: 100MB 미만만 지원 (단일 업로드)
 * </p>
 */
public record FileSize(Long bytes) {

    private static final long MAX_SIZE = 1073741824L; // 1GB

    /**
     * 파일 크기 검증 및 생성
     *
     * @param bytes 파일 크기 (bytes)
     * @return FileSize
     * @throws IllegalArgumentException bytes가 null이거나 0 이하인 경우
     * @throws FileSizeExceededException bytes가 1GB 초과인 경우
     */
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

**테스트 케이스**:
- 정상 파일 크기 생성 (예: 1048576L = 1MB)
- null 검증 실패
- 0 이하 검증 실패
- 1GB 초과 검증 실패

---

### 5. MimeType (MIME 타입)

**책임**: MIME 타입 화이트리스트 검증

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/shared/vo/MimeType.java`

```java
/**
 * MIME 타입 VO
 * <p>
 * - 허용 목록: 이미지 (JPEG, PNG, GIF, WEBP), 문서 (PDF), 엑셀 (XLS, XLSX)
 * - 소문자 정규화
 * </p>
 */
public record MimeType(String value) {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp",
        "application/pdf",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    /**
     * MIME 타입 검증 및 생성
     *
     * @param value MIME 타입
     * @return MimeType
     * @throws IllegalArgumentException value가 null이거나 빈 문자열인 경우
     * @throws UnsupportedMimeTypeException MIME 타입이 허용 목록에 없는 경우
     */
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

**테스트 케이스**:
- 허용된 MIME 타입 생성 (예: "image/jpeg")
- 대소문자 정규화 (예: "Image/JPEG" → "image/jpeg")
- null 검증 실패
- 허용되지 않은 MIME 타입 검증 실패 (예: "video/mp4")

---

### 6. S3Key (스토리지 경로)

**책임**: UploaderType별 S3 Object Key 생성

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/shared/vo/S3Key.java`

```java
/**
 * S3 Object Key VO
 * <p>
 * - UploaderType별 경로 생성:
 *   - Admin: uploads/{tenantId}/admin/{uploaderSlug}/{category}/{fileId}_{fileName}
 *   - Seller: uploads/{tenantId}/seller/{uploaderSlug}/{category}/{fileId}_{fileName}
 *   - Customer: uploads/{tenantId}/customer/default/{fileId}_{fileName}
 * </p>
 */
public record S3Key(String value) {

    /**
     * S3 Key 생성
     *
     * @param tenantId 테넌트 ID
     * @param uploaderType 업로더 타입
     * @param uploaderSlug 업로더 슬러그 (예: "connectly", "samsung-electronics")
     * @param category 파일 카테고리
     * @param fileId 파일 ID
     * @param fileName 파일명
     * @return S3Key
     */
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
            // Admin, Seller: 서브카테고리 포함
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
            // Customer: 서브카테고리 없음
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

**테스트 케이스**:
- Admin 경로 생성 (예: "uploads/1/admin/connectly/banner/01JD8001_메인배너.jpg")
- Seller 경로 생성 (예: "uploads/1/seller/samsung-electronics/product/01JD8010_갤럭시.jpg")
- Customer 경로 생성 (예: "uploads/1/customer/default/01JD8100_리뷰.jpg")

---

### 7. S3Bucket (S3 버킷)

**책임**: 테넌트별 S3 버킷 네이밍

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/shared/vo/S3Bucket.java`

```java
/**
 * S3 버킷 VO
 * <p>
 * - 네이밍: fileflow-uploads-{tenantId}
 * - 테넌트별 버킷 분리 (향후 확장)
 * </p>
 */
public record S3Bucket(String value) {

    /**
     * 테넌트별 S3 버킷 생성
     *
     * @param tenantId 테넌트 ID
     * @return S3Bucket
     */
    public static S3Bucket forTenant(TenantId tenantId) {
        return new S3Bucket("fileflow-uploads-" + tenantId.value());
    }
}
```

**테스트 케이스**:
- 버킷 네이밍 검증 (예: TenantId(1) → "fileflow-uploads-1")

---

### 8. TenantId (테넌트 식별자)

**책임**: 테넌트 식별자 (Long FK 전략)

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/shared/vo/TenantId.java`

```java
/**
 * 테넌트 식별자 VO
 * <p>
 * - Long FK 전략 준수 (JPA 관계 어노테이션 금지)
 * - 1 이상 양수 검증
 * </p>
 */
public record TenantId(Long value) {

    /**
     * 테넌트 ID 생성
     *
     * @param value 테넌트 ID
     * @return TenantId
     * @throws IllegalArgumentException value가 null이거나 0 이하인 경우
     */
    public static TenantId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("TenantId는 1 이상이어야 합니다");
        }
        return new TenantId(value);
    }
}
```

**테스트 케이스**:
- 정상 생성 (예: TenantId.of(1L))
- null 검증 실패
- 0 이하 검증 실패

---

### 9. UploaderId (업로더 식별자)

**책임**: 업로더 식별자 (Long FK 전략)

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/shared/vo/UploaderId.java`

```java
/**
 * 업로더 식별자 VO
 * <p>
 * - Long FK 전략 준수 (JPA 관계 어노테이션 금지)
 * - 1 이상 양수 검증
 * </p>
 */
public record UploaderId(Long value) {

    /**
     * 업로더 ID 생성
     *
     * @param value 업로더 ID
     * @return UploaderId
     * @throws IllegalArgumentException value가 null이거나 0 이하인 경우
     */
    public static UploaderId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("UploaderId는 1 이상이어야 합니다");
        }
        return new UploaderId(value);
    }
}
```

**테스트 케이스**:
- 정상 생성 (예: UploaderId.of(100L))
- null 검증 실패
- 0 이하 검증 실패

---

### 10. FileCategory (파일 카테고리)

**책임**: UploaderType별 카테고리 검증

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/shared/vo/FileCategory.java`

```java
/**
 * 파일 카테고리 VO
 * <p>
 * - UploaderType별 허용 카테고리:
 *   - Admin: banner, event, excel, notice, default
 *   - Seller: product, review, promotion, default
 *   - Customer: default만 허용
 * </p>
 */
public record FileCategory(String value) {

    // Admin 카테고리
    private static final Set<String> ADMIN_CATEGORIES = Set.of(
        "banner", "event", "excel", "notice", "default"
    );

    // Seller 카테고리
    private static final Set<String> SELLER_CATEGORIES = Set.of(
        "product", "review", "promotion", "default"
    );

    /**
     * 파일 카테고리 생성
     *
     * @param value 카테고리 값
     * @param uploaderType 업로더 타입
     * @return FileCategory
     * @throws IllegalArgumentException UploaderType에서 허용하지 않는 카테고리인 경우
     */
    public static FileCategory of(String value, UploaderType uploaderType) {
        String normalized = (value != null && !value.isBlank())
            ? value.toLowerCase()
            : "default";

        Set<String> allowedCategories = switch (uploaderType) {
            case ADMIN -> ADMIN_CATEGORIES;
            case SELLER -> SELLER_CATEGORIES;
            case CUSTOMER -> Set.of("default");
        };

        if (!allowedCategories.contains(normalized)) {
            throw new IllegalArgumentException(
                uploaderType + "에서 지원하지 않는 카테고리입니다: " + value
            );
        }

        return new FileCategory(normalized);
    }

    /**
     * 기본 카테고리 생성
     *
     * @return FileCategory ("default")
     */
    public static FileCategory defaultCategory() {
        return new FileCategory("default");
    }
}
```

**테스트 케이스**:
- Admin 허용 카테고리 생성 (예: "banner")
- Seller 허용 카테고리 생성 (예: "product")
- Customer 기본 카테고리 생성 (예: "default")
- 허용되지 않은 카테고리 검증 실패 (예: Admin에서 "product")

---

### 11. PresignedUrl (Presigned URL)

**책임**: Presigned URL 검증

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/shared/vo/PresignedUrl.java`

```java
/**
 * Presigned URL VO
 * <p>
 * - S3 Presigned URL (5분 유효)
 * - Null/Empty 검증
 * </p>
 */
public record PresignedUrl(String value) {

    /**
     * Presigned URL 생성
     *
     * @param value Presigned URL
     * @return PresignedUrl
     * @throws IllegalArgumentException value가 null이거나 빈 문자열인 경우
     */
    public static PresignedUrl of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Presigned URL은 필수입니다");
        }
        return new PresignedUrl(value);
    }
}
```

**테스트 케이스**:
- 정상 생성
- null 검증 실패
- 빈 문자열 검증 실패

---

## Enums

### 1. UploaderType

**책임**: 업로더 타입 정의

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/shared/enums/UploaderType.java`

```java
/**
 * 업로더 타입 Enum
 * <p>
 * - ADMIN: 커넥틀리 관리자 (자사 상품)
 * - SELLER: 입점 셀러 (회사별)
 * - CUSTOMER: 일반 고객 (리뷰)
 * </p>
 */
public enum UploaderType {
    ADMIN,      // 관리자
    SELLER,     // 셀러
    CUSTOMER    // 고객
}
```

---

### 2. FileStatus

**책임**: 파일 상태 정의

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/file/enums/FileStatus.java`

```java
/**
 * 파일 상태 Enum
 * <p>
 * - PENDING: 업로드 대기 중 (UploadSession 생성 시)
 * - COMPLETED: 업로드 완료
 * </p>
 */
public enum FileStatus {
    PENDING,    // 업로드 대기
    COMPLETED   // 업로드 완료
}
```

---

### 3. SessionStatus

**책임**: 세션 상태 정의

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/session/enums/SessionStatus.java`

```java
/**
 * 세션 상태 Enum
 * <p>
 * - INITIATED: 세션 생성됨 (Presigned URL 발급 전)
 * - IN_PROGRESS: Presigned URL 발급 완료 (업로드 진행 중)
 * - COMPLETED: 업로드 완료
 * - EXPIRED: 세션 만료 (5분 초과)
 * </p>
 */
public enum SessionStatus {
    INITIATED,      // 세션 생성
    IN_PROGRESS,    // 업로드 진행 중
    COMPLETED,      // 업로드 완료
    EXPIRED         // 세션 만료
}
```

---

## Aggregates

### 1. UploadSession Aggregate Root

**책임**: 세션 기반 멱등성 관리, Presigned URL 발급 추적

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/session/UploadSession.java`

```java
/**
 * 업로드 세션 Aggregate Root
 * <p>
 * - 멱등성: sessionId로 중복 발급 방지
 * - 만료 관리: 5분 유효 (expiresAt)
 * - Zero-Tolerance: Lombok 금지, Law of Demeter 준수
 * </p>
 */
public class UploadSession {

    // 식별자
    private SessionId sessionId;

    // 파일 정보
    private TenantId tenantId;
    private FileName fileName;
    private FileSize fileSize;
    private MimeType mimeType;

    // 업로드 정보
    private UploadType uploadType;  // MVP: SINGLE만 지원
    private PresignedUrl presignedUrl;  // Nullable (발급 후 저장)
    private LocalDateTime expiresAt;

    // 상태
    private SessionStatus status;

    // 감사
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 생성자 (Private)
    private UploadSession() {}

    /**
     * 세션 초기화 (INITIATED 상태)
     *
     * @param sessionId 세션 ID (멱등키)
     * @param tenantId 테넌트 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param mimeType MIME 타입
     * @param uploadType 업로드 타입 (MVP: SINGLE)
     * @param presignedUrl Presigned URL
     * @param clock Clock
     * @return UploadSession
     */
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

    /**
     * 세션 만료 확인
     *
     * @param clock Clock
     * @throws SessionExpiredException 세션이 만료된 경우
     */
    public void ensureNotExpired(Clock clock) {
        if (LocalDateTime.now(clock).isAfter(expiresAt)) {
            throw new SessionExpiredException(sessionId);
        }
    }

    /**
     * 세션 완료 여부 확인
     *
     * @throws SessionAlreadyCompletedException 이미 완료된 세션인 경우
     */
    public void ensureNotCompleted() {
        if (status == SessionStatus.COMPLETED) {
            throw new SessionAlreadyCompletedException(sessionId);
        }
    }

    /**
     * 세션 완료 처리
     *
     * @param clock Clock
     * @throws InvalidSessionStatusException 상태 전환 불가능한 경우
     */
    public void markAsCompleted(Clock clock) {
        if (status != SessionStatus.INITIATED && status != SessionStatus.IN_PROGRESS) {
            throw new InvalidSessionStatusException(sessionId, status, SessionStatus.COMPLETED);
        }
        this.status = SessionStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * 세션 진행 중 상태로 변경
     *
     * @param clock Clock
     */
    public void markAsInProgress(Clock clock) {
        this.status = SessionStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now(clock);
    }

    // Getter (Plain Java, Law of Demeter 준수)
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

**도메인 규칙**:
1. **멱등성**: 동일 `sessionId`로 중복 발급 방지
2. **만료 관리**: 5분 유효 (`expiresAt`)
3. **상태 전환**: INITIATED → IN_PROGRESS → COMPLETED
4. **세션 만료**: `ensureNotExpired()` 호출 시 만료 체크

**테스트 케이스**:
- 세션 초기화 성공 (INITIATED 상태)
- 만료 체크 성공 (5분 이내)
- 만료 체크 실패 (5분 초과) → SessionExpiredException
- 완료된 세션 체크 실패 → SessionAlreadyCompletedException
- 상태 전환 성공 (INITIATED → COMPLETED)
- 상태 전환 실패 (잘못된 상태) → InvalidSessionStatusException

---

### 2. File Aggregate Root

**책임**: 파일 메타데이터 관리

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/file/File.java`

```java
/**
 * 파일 Aggregate Root
 * <p>
 * - 파일 메타데이터 관리
 * - UploadSession 완료 후 생성
 * - Zero-Tolerance: Lombok 금지, Law of Demeter 준수
 * </p>
 */
public class File {

    // 식별자
    private FileId fileId;

    // 파일 정보
    private FileName fileName;
    private FileSize fileSize;
    private MimeType mimeType;

    // 스토리지 정보
    private S3Key s3Key;
    private S3Bucket s3Bucket;

    // 업로더 정보
    private UploaderId uploaderId;
    private UploaderType uploaderType;
    private String uploaderSlug;  // "connectly", "samsung-electronics", "default"
    private FileCategory category;

    // 테넌트
    private TenantId tenantId;

    // 상태
    private FileStatus status;

    // 감사
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 생성자 (Private)
    private File() {}

    /**
     * UploadSession 완료 후 File 생성
     *
     * @param fileId 파일 ID
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param mimeType MIME 타입
     * @param s3Key S3 Key
     * @param s3Bucket S3 Bucket
     * @param uploaderId 업로더 ID
     * @param uploaderType 업로더 타입
     * @param uploaderSlug 업로더 슬러그
     * @param category 파일 카테고리
     * @param tenantId 테넌트 ID
     * @param clock Clock
     * @return File
     */
    public static File createFromSession(
        FileId fileId,
        FileName fileName,
        FileSize fileSize,
        MimeType mimeType,
        S3Key s3Key,
        S3Bucket s3Bucket,
        UploaderId uploaderId,
        UploaderType uploaderType,
        String uploaderSlug,
        FileCategory category,
        TenantId tenantId,
        Clock clock
    ) {
        File file = new File();
        file.fileId = fileId;
        file.fileName = fileName;
        file.fileSize = fileSize;
        file.mimeType = mimeType;
        file.s3Key = s3Key;
        file.s3Bucket = s3Bucket;
        file.uploaderId = uploaderId;
        file.uploaderType = uploaderType;
        file.uploaderSlug = uploaderSlug;
        file.category = category;
        file.tenantId = tenantId;
        file.status = FileStatus.COMPLETED;
        file.createdAt = LocalDateTime.now(clock);
        file.updatedAt = LocalDateTime.now(clock);
        return file;
    }

    // Getter (Plain Java, Law of Demeter 준수)
    public FileId fileId() { return fileId; }
    public FileName fileName() { return fileName; }
    public FileSize fileSize() { return fileSize; }
    public MimeType mimeType() { return mimeType; }
    public S3Key s3Key() { return s3Key; }
    public S3Bucket s3Bucket() { return s3Bucket; }
    public UploaderId uploaderId() { return uploaderId; }
    public UploaderType uploaderType() { return uploaderType; }
    public String uploaderSlug() { return uploaderSlug; }
    public FileCategory category() { return category; }
    public TenantId tenantId() { return tenantId; }
    public FileStatus status() { return status; }
    public LocalDateTime createdAt() { return createdAt; }
    public LocalDateTime updatedAt() { return updatedAt; }
}
```

**도메인 규칙**:
1. **생성 조건**: UploadSession 완료 후에만 생성 가능
2. **초기 상태**: COMPLETED (업로드 완료 상태로 시작)
3. **S3 경로**: S3Key VO에서 UploaderType별 경로 생성

**테스트 케이스**:
- File 생성 성공 (COMPLETED 상태)
- S3Key 경로 검증 (UploaderType별)

---

## Domain Exceptions

### 1. SessionExpiredException

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/session/exception/SessionExpiredException.java`

```java
/**
 * 세션 만료 예외
 * <p>
 * - HTTP Status: 410 GONE
 * - 발생 조건: expiresAt < now()
 * </p>
 */
public class SessionExpiredException extends DomainException {
    public SessionExpiredException(SessionId sessionId) {
        super("세션이 만료되었습니다: " + sessionId.value());
    }
}
```

---

### 2. SessionAlreadyCompletedException

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/session/exception/SessionAlreadyCompletedException.java`

```java
/**
 * 세션 이미 완료 예외
 * <p>
 * - HTTP Status: 409 CONFLICT
 * - 발생 조건: status == COMPLETED
 * </p>
 */
public class SessionAlreadyCompletedException extends DomainException {
    public SessionAlreadyCompletedException(SessionId sessionId) {
        super("이미 완료된 세션입니다: " + sessionId.value());
    }
}
```

---

### 3. InvalidSessionStatusException

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/session/exception/InvalidSessionStatusException.java`

```java
/**
 * 잘못된 세션 상태 예외
 * <p>
 * - HTTP Status: 400 BAD REQUEST
 * - 발생 조건: 상태 전환 불가능
 * </p>
 */
public class InvalidSessionStatusException extends DomainException {
    public InvalidSessionStatusException(
        SessionId sessionId,
        SessionStatus current,
        SessionStatus expected
    ) {
        super(String.format(
            "세션 상태 전환 오류: %s (현재: %s, 예상: %s)",
            sessionId.value(), current, expected
        ));
    }
}
```

---

### 4. FileSizeExceededException

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/file/exception/FileSizeExceededException.java`

```java
/**
 * 파일 크기 초과 예외
 * <p>
 * - HTTP Status: 400 BAD REQUEST
 * - 발생 조건: fileSize > 1GB
 * </p>
 */
public class FileSizeExceededException extends DomainException {
    public FileSizeExceededException(Long actual, Long max) {
        super(String.format(
            "파일 크기 초과: %d bytes (최대: %d bytes)",
            actual, max
        ));
    }
}
```

---

### 5. UnsupportedMimeTypeException

**위치**: `domain/src/main/java/com/ryuqq/fileflow/domain/file/exception/UnsupportedMimeTypeException.java`

```java
/**
 * 지원하지 않는 MIME 타입 예외
 * <p>
 * - HTTP Status: 400 BAD REQUEST
 * - 발생 조건: MIME 타입이 허용 목록에 없음
 * </p>
 */
public class UnsupportedMimeTypeException extends DomainException {
    public UnsupportedMimeTypeException(String mimeType) {
        super("지원하지 않는 MIME Type입니다: " + mimeType);
    }
}
```

---

## Design Principles

### Zero-Tolerance 규칙 준수

1. **Lombok 금지**:
   - ✅ Plain Java 또는 Record 사용
   - ✅ 명시적 Getter 메서드 (Law of Demeter)

2. **Law of Demeter**:
   - ✅ `file.fileName()` (O)
   - ❌ `file.fileName().value()` (X)
   - 해결: VO에 `value()` 메서드 제공

3. **Long FK 전략**:
   - ✅ JPA 관계 어노테이션 금지
   - ✅ `TenantId`, `UploaderId` VO 사용

4. **Tell Don't Ask**:
   - ✅ `session.ensureNotExpired(clock)` (Tell)
   - ❌ `if (session.isExpired())` (Ask)

### VO 설계 원칙

1. **불변성**: Record 또는 final 필드
2. **검증**: 생성 시점에 모든 검증 수행
3. **명확성**: 책임이 명확한 작은 VO
4. **재사용**: Shared VOs (`domain/shared/vo/`)

### Aggregate 설계 원칙

1. **트랜잭션 경계**: Aggregate = Transaction
2. **불변성**: private 생성자 + 정적 팩토리 메서드
3. **도메인 로직**: 비즈니스 규칙을 도메인 메서드로 표현

---

**작성자**: Claude (Anthropic)
**검토자**: ryu-qqq
**변경 이력**:
- 2025-11-18: 초안 작성 (session/single Domain Layer)
