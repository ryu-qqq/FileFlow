# PRD: FileAsset Processing & File Management System

**작성일**: 2025-12-02
**작성자**: sangwon-ryu
**상태**: Draft

---

## 📋 프로젝트 개요

### 비즈니스 목적
파일 업로드 후 자동 가공(이미지 리사이징, HTML 이미지 추출/교체) 및 n8n 파이프라인 연동을 위한 파일 관리 시스템 구축

**핵심 가치**:
- **파일 최적화**: 이미지 리사이징(용량 절감 + 다양한 크기 버전 생성)
- **포맷 변환**: WebP + JPEG 폴백으로 브라우저 호환성 확보
- **HTML 처리**: 상품 상세 페이지/크롤링 HTML 내 이미지 자동 최적화
- **n8n 연동**: 가공된 파일을 n8n 파이프라인에서 활용 가능한 API 제공
- **확장성**: SQS 기반 분산 워커로 대량 처리 지원

### 주요 사용자
- **ADMIN**: 시스템 관리자 (HTML 템플릿 관리, Excel 업로드)
- **SELLER**: 판매자 (상품 이미지, 상세 페이지 HTML, 라인시트/오더시트 Excel)
- **n8n**: 자동화 파이프라인 (API를 통한 파일 조회 및 다운로드)

### 성공 기준
1. 이미지 리사이징 처리 시간: < 5초 (P95, 5MB 이하 이미지)
2. HTML 이미지 추출/교체 처리 시간: < 30초 (P95, 이미지 20개 이하 HTML)
3. WebP 변환 용량 절감률: > 30%
4. 파일 가공 성공률: > 99%
5. n8n API 응답 시간: < 200ms (P95)

---

## 🏗️ Layer별 요구사항

### 1. Domain Layer

#### 1.1 ContentType 확장 (HTML, Excel 추가)

**기존 MIME 타입에 추가**:
```java
// ALLOWED_MIME_TYPES에 추가
"text/html",                    // HTML
"application/xhtml+xml",        // XHTML

// EXTENSION_TO_MIME에 추가
Map.entry("html", "text/html"),
Map.entry("htm", "text/html"),
Map.entry("xhtml", "application/xhtml+xml"),
```

**타입 체크 메서드 추가**:
```java
/**
 * HTML 타입인지 확인한다.
 * @return HTML 타입이면 true
 */
public boolean isHtml() {
    return type.equals("text/html") || type.equals("application/xhtml+xml");
}

/**
 * Excel 타입인지 확인한다.
 * @return Excel 타입이면 true
 */
public boolean isExcel() {
    return type.equals("application/vnd.ms-excel")
        || type.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
}
```

---

#### 1.2 UploadCategory 확장 (HTML 추가)

**기존 카테고리에 추가**:
```java
/** HTML 문서 (상품 상세 페이지, 이메일 템플릿 등). */
HTML("html", "HTML 문서"),
```

**타입 체크 메서드 추가**:
```java
/**
 * HTML 카테고리인지 확인한다.
 * @return HTML이면 true
 */
public boolean isHtml() {
    return this == HTML;
}

/**
 * 이미지 가공이 필요한 카테고리인지 확인한다.
 * @return 이미지 가공 필요시 true (BANNER, PRODUCT_IMAGE, HTML)
 */
public boolean requiresImageProcessing() {
    return this == BANNER || this == PRODUCT_IMAGE || this == HTML;
}
```

---

#### 1.3 FileAssetStatus 확장

**기존 상태에 추가**:
```java
public enum FileAssetStatus {
    PENDING,        // 생성됨, 가공 대기 중
    PROCESSING,     // 가공 처리 중
    COMPLETED,      // 가공 완료
    FAILED,         // 가공 실패
    DELETED,        // 삭제됨

    // 신규 추가
    RESIZED,        // 이미지 리사이징 완료 (n8n 대기)
    N8N_PROCESSING, // n8n에서 처리 중
    N8N_COMPLETED   // n8n 처리 완료
}
```

**상태 전환 다이어그램**:
```
PENDING → PROCESSING → RESIZED → N8N_PROCESSING → N8N_COMPLETED
                ↓           ↓            ↓
             FAILED      FAILED       FAILED
                              ↓
                          DELETED
```

---

#### 1.4 Value Object: ImageVariant (신규)

**정의**:
```java
/**
 * 이미지 리사이징 버전 정보.
 *
 * <p><strong>커머스 표준 사이즈</strong>:
 * <ul>
 *   <li>ORIGINAL: 원본 유지
 *   <li>LARGE: 긴 변 1200px (상세 페이지용)
 *   <li>MEDIUM: 긴 변 600px (목록 페이지용)
 *   <li>THUMBNAIL: 긴 변 200px (썸네일용)
 * </ul>
 *
 * <p><strong>컨벤션 준수</strong>: Java 21 Record + 정적 팩토리 메서드</p>
 *
 * @param type 버전 타입 (ORIGINAL, LARGE, MEDIUM, THUMBNAIL)
 * @param maxDimension 긴 변 최대 크기 (px)
 * @param suffix 파일명 suffix
 */
public record ImageVariant(
    ImageVariantType type,
    Integer maxDimension,
    String suffix
) {

    /**
     * Compact Constructor (검증 로직)
     */
    public ImageVariant {
        if (type == null) {
            throw new IllegalArgumentException("ImageVariant type은 null일 수 없습니다.");
        }
        if (suffix == null) {
            throw new IllegalArgumentException("suffix는 null일 수 없습니다.");
        }
    }

    // ===== 표준 사이즈 상수 =====
    public static final ImageVariant ORIGINAL = ImageVariant.of(ImageVariantType.ORIGINAL, null, "");
    public static final ImageVariant LARGE = ImageVariant.of(ImageVariantType.LARGE, 1200, "_large");
    public static final ImageVariant MEDIUM = ImageVariant.of(ImageVariantType.MEDIUM, 600, "_medium");
    public static final ImageVariant THUMBNAIL = ImageVariant.of(ImageVariantType.THUMBNAIL, 200, "_thumb");

    /**
     * 값 기반 생성
     */
    public static ImageVariant of(ImageVariantType type, Integer maxDimension, String suffix) {
        return new ImageVariant(type, maxDimension, suffix);
    }

    /**
     * 리사이징이 필요한 버전인지 확인.
     * @return ORIGINAL이 아니면 true
     */
    public boolean requiresResize() {
        return type != ImageVariantType.ORIGINAL;
    }
}

/**
 * ImageVariant 타입 Enum
 */
public enum ImageVariantType {
    ORIGINAL,
    LARGE,
    MEDIUM,
    THUMBNAIL
}
```

---

#### 1.5 Value Object: ImageFormat (신규)

**정의**:
```java
/**
 * 이미지 출력 포맷.
 *
 * <p>WebP + JPEG 폴백 전략 지원.</p>
 *
 * <p><strong>컨벤션 준수</strong>: Java 21 Record + 정적 팩토리 메서드</p>
 *
 * @param type 포맷 타입
 * @param extension 파일 확장자
 * @param mimeType MIME 타입
 */
public record ImageFormat(
    ImageFormatType type,
    String extension,
    String mimeType
) {

    /**
     * Compact Constructor (검증 로직)
     */
    public ImageFormat {
        if (type == null) {
            throw new IllegalArgumentException("ImageFormat type은 null일 수 없습니다.");
        }
        if (extension == null || extension.isBlank()) {
            throw new IllegalArgumentException("extension은 null이거나 빈 문자열일 수 없습니다.");
        }
        if (mimeType == null || mimeType.isBlank()) {
            throw new IllegalArgumentException("mimeType은 null이거나 빈 문자열일 수 없습니다.");
        }
    }

    // ===== 표준 포맷 상수 =====
    public static final ImageFormat WEBP = ImageFormat.of(ImageFormatType.WEBP, "webp", "image/webp");
    public static final ImageFormat JPEG = ImageFormat.of(ImageFormatType.JPEG, "jpg", "image/jpeg");
    public static final ImageFormat PNG = ImageFormat.of(ImageFormatType.PNG, "png", "image/png");

    /**
     * 값 기반 생성
     */
    public static ImageFormat of(ImageFormatType type, String extension, String mimeType) {
        return new ImageFormat(type, extension, mimeType);
    }

    /**
     * 원본 확장자로부터 최적 포맷 결정.
     * PNG → PNG 유지 (투명도 보존)
     * 그 외 → JPEG
     */
    public static ImageFormat fromOriginal(String originalExtension) {
        if ("png".equalsIgnoreCase(originalExtension)) {
            return PNG;
        }
        return JPEG;
    }
}

/**
 * ImageFormat 타입 Enum
 */
public enum ImageFormatType {
    WEBP,
    JPEG,
    PNG
}
```

---

#### 1.6 Aggregate: ProcessedFileAsset (신규)

**정의**:
```java
/**
 * 가공된 파일 에셋 Aggregate Root.
 *
 * <p><strong>컨벤션 준수</strong>:
 * <ul>
 *   <li>Lombok 금지 - Pure Java 사용
 *   <li>Law of Demeter - Getter 체이닝 금지
 *   <li>Tell Don't Ask - 행위 중심 메서드
 *   <li>정적 팩토리 메서드: forNew(), reconstitute()
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class ProcessedFileAsset {

    // ===== 식별 정보 =====
    private final ProcessedFileAssetId id;
    private final FileAssetId originalAssetId;  // 원본 FileAsset ID
    private final FileAssetId parentAssetId;    // HTML인 경우 부모 HTML의 FileAssetId (Nullable)

    // ===== 리사이징 정보 =====
    private final ImageVariant variant;         // ORIGINAL, LARGE, MEDIUM, THUMBNAIL
    private final ImageFormat format;           // WEBP, JPEG, PNG

    // ===== 파일 메타데이터 =====
    private final FileName fileName;            // image_large.webp
    private final FileSize fileSize;
    private final Integer width;                // px
    private final Integer height;               // px

    // ===== S3 위치 =====
    private final S3Bucket bucket;
    private final S3Key s3Key;

    // ===== 소유자 정보 (Long FK 전략) =====
    private final Long userId;
    private final Long organizationId;
    private final Long tenantId;

    // ===== 시간 =====
    private final LocalDateTime createdAt;

    // ===== Private Constructor =====
    private ProcessedFileAsset(
        ProcessedFileAssetId id,
        FileAssetId originalAssetId,
        FileAssetId parentAssetId,
        ImageVariant variant,
        ImageFormat format,
        FileName fileName,
        FileSize fileSize,
        Integer width,
        Integer height,
        S3Bucket bucket,
        S3Key s3Key,
        Long userId,
        Long organizationId,
        Long tenantId,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.originalAssetId = originalAssetId;
        this.parentAssetId = parentAssetId;
        this.variant = variant;
        this.format = format;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.width = width;
        this.height = height;
        this.bucket = bucket;
        this.s3Key = s3Key;
        this.userId = userId;
        this.organizationId = organizationId;
        this.tenantId = tenantId;
        this.createdAt = createdAt;
    }

    // ===== 정적 팩토리 메서드 =====

    /**
     * 새로운 ProcessedFileAsset 생성 (이미지 가공 결과).
     *
     * @param originalAssetId 원본 FileAsset ID
     * @param variant 이미지 버전
     * @param format 이미지 포맷
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param width 너비 (px)
     * @param height 높이 (px)
     * @param bucket S3 버킷
     * @param s3Key S3 키
     * @param userId 사용자 ID
     * @param organizationId 조직 ID
     * @param tenantId 테넌트 ID
     * @return 새로운 ProcessedFileAsset
     */
    public static ProcessedFileAsset forNew(
        FileAssetId originalAssetId,
        ImageVariant variant,
        ImageFormat format,
        FileName fileName,
        FileSize fileSize,
        Integer width,
        Integer height,
        S3Bucket bucket,
        S3Key s3Key,
        Long userId,
        Long organizationId,
        Long tenantId
    ) {
        return new ProcessedFileAsset(
            ProcessedFileAssetId.generate(),
            originalAssetId,
            null,  // parentAssetId는 별도 메서드로 설정
            variant,
            format,
            fileName,
            fileSize,
            width,
            height,
            bucket,
            s3Key,
            userId,
            organizationId,
            tenantId,
            LocalDateTime.now()
        );
    }

    /**
     * HTML에서 추출된 이미지용 ProcessedFileAsset 생성.
     *
     * @param parentAssetId HTML FileAsset ID
     * @param originalAssetId 추출된 이미지 FileAsset ID
     * @param variant 이미지 버전
     * @param format 이미지 포맷
     * @param fileName 파일명
     * @param fileSize 파일 크기
     * @param width 너비 (px)
     * @param height 높이 (px)
     * @param bucket S3 버킷
     * @param s3Key S3 키
     * @param userId 사용자 ID
     * @param organizationId 조직 ID
     * @param tenantId 테넌트 ID
     * @return HTML 추출 이미지용 ProcessedFileAsset
     */
    public static ProcessedFileAsset forHtmlExtractedImage(
        FileAssetId parentAssetId,
        FileAssetId originalAssetId,
        ImageVariant variant,
        ImageFormat format,
        FileName fileName,
        FileSize fileSize,
        Integer width,
        Integer height,
        S3Bucket bucket,
        S3Key s3Key,
        Long userId,
        Long organizationId,
        Long tenantId
    ) {
        return new ProcessedFileAsset(
            ProcessedFileAssetId.generate(),
            originalAssetId,
            parentAssetId,
            variant,
            format,
            fileName,
            fileSize,
            width,
            height,
            bucket,
            s3Key,
            userId,
            organizationId,
            tenantId,
            LocalDateTime.now()
        );
    }

    /**
     * DB에서 복원 (Persistence Layer용).
     *
     * @return 복원된 ProcessedFileAsset
     */
    public static ProcessedFileAsset reconstitute(
        ProcessedFileAssetId id,
        FileAssetId originalAssetId,
        FileAssetId parentAssetId,
        ImageVariant variant,
        ImageFormat format,
        FileName fileName,
        FileSize fileSize,
        Integer width,
        Integer height,
        S3Bucket bucket,
        S3Key s3Key,
        Long userId,
        Long organizationId,
        Long tenantId,
        LocalDateTime createdAt
    ) {
        return new ProcessedFileAsset(
            id,
            originalAssetId,
            parentAssetId,
            variant,
            format,
            fileName,
            fileSize,
            width,
            height,
            bucket,
            s3Key,
            userId,
            organizationId,
            tenantId,
            createdAt
        );
    }

    // ===== 비즈니스 메서드 (Tell Don't Ask) =====

    /**
     * HTML 부모 에셋이 있는지 확인.
     * @return HTML에서 추출된 이미지이면 true
     */
    public boolean hasParentAsset() {
        return parentAssetId != null;
    }

    /**
     * 원본 버전인지 확인.
     * @return ORIGINAL 버전이면 true
     */
    public boolean isOriginalVariant() {
        return variant.type() == ImageVariantType.ORIGINAL;
    }

    /**
     * WebP 포맷인지 확인.
     * @return WebP이면 true
     */
    public boolean isWebpFormat() {
        return format.type() == ImageFormatType.WEBP;
    }

    // ===== Getter (Law of Demeter 준수, 체이닝 방지) =====

    public ProcessedFileAssetId getId() {
        return id;
    }

    public FileAssetId getOriginalAssetId() {
        return originalAssetId;
    }

    public FileAssetId getParentAssetId() {
        return parentAssetId;
    }

    public ImageVariant getVariant() {
        return variant;
    }

    public ImageFormat getFormat() {
        return format;
    }

    public FileName getFileName() {
        return fileName;
    }

    public FileSize getFileSize() {
        return fileSize;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public S3Bucket getBucket() {
        return bucket;
    }

    public S3Key getS3Key() {
        return s3Key;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
```

**비즈니스 규칙**:

1. **파일명 생성 규칙**:
   ```
   원본: image.jpg
   → image_large.webp, image_large.jpg
   → image_medium.webp, image_medium.jpg
   → image_thumb.webp, image_thumb.jpg
   ```

2. **S3 경로 규칙** (파일명 suffix 방식):
   ```
   원본: /tenant-1/org-1/product/2025/01/image.jpg
   리사이징: /tenant-1/org-1/product/2025/01/image_large.webp
   ```

3. **HTML 추출 이미지 연관관계**:
   - `parentAssetId`: HTML FileAsset의 ID
   - HTML과 추출된 이미지는 별도 FileAsset으로 관리
   - HTML 삭제 시 연관 이미지도 Cascade 삭제 (Soft Delete)

**Zero-Tolerance 규칙 준수**:
- ✅ Law of Demeter (Getter 체이닝 금지)
- ✅ Lombok 금지 (Pure Java 사용)
- ✅ Long FK 전략 (JPA 관계 어노테이션 금지)

---

#### 1.7 Domain Service: ImageProcessingPolicy (신규)

**정의**:
```java
/**
 * 이미지 가공 정책.
 *
 * <p>어떤 이미지를 어떻게 리사이징할지 결정.
 */
public class ImageProcessingPolicy {

    /**
     * 해당 ContentType이 이미지 가공 대상인지 확인.
     */
    public boolean shouldProcess(ContentType contentType) {
        return contentType.isImage();
    }

    /**
     * 해당 카테고리가 이미지 가공 대상인지 확인.
     */
    public boolean shouldProcess(UploadCategory category) {
        return category.requiresImageProcessing();
    }

    /**
     * 생성할 이미지 버전 목록 반환.
     * @return [LARGE, MEDIUM, THUMBNAIL] (ORIGINAL 제외)
     */
    public List<ImageVariant> getVariantsToGenerate() {
        return List.of(
            ImageVariant.LARGE,
            ImageVariant.MEDIUM,
            ImageVariant.THUMBNAIL
        );
    }

    /**
     * 생성할 이미지 포맷 목록 반환.
     * @return [WEBP, 원본포맷폴백]
     */
    public List<ImageFormat> getFormatsToGenerate(String originalExtension) {
        ImageFormat fallback = ImageFormat.fromOriginal(originalExtension);
        return List.of(ImageFormat.WEBP, fallback);
    }
}
```

---

#### 1.8 Aggregate: FileAssetStatusHistory (신규)

**목적**:
- FileAsset 상태 변경 이력 추적
- 가공 실패 원인 분석
- SLA 모니터링 (각 단계별 소요 시간)
- 감사(Audit) 로그

**정의**:
```java
/**
 * FileAsset 상태 변경 히스토리 Aggregate.
 *
 * <p>상태 전환 이력을 추적하여 디버깅, SLA 모니터링, 감사 로그를 지원합니다.</p>
 *
 * <p><strong>컨벤션 준수</strong>:
 * <ul>
 *   <li>Lombok 금지 - Pure Java 사용
 *   <li>Long FK 전략 - JPA 관계 어노테이션 금지
 *   <li>정적 팩토리 메서드: forNew(), reconstitute()
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class FileAssetStatusHistory {

    // ===== 식별 정보 =====
    private final FileAssetStatusHistoryId id;
    private final Long fileAssetId;              // Long FK 전략

    // ===== 상태 변경 정보 =====
    private final FileAssetStatus fromStatus;    // 이전 상태 (null 가능: 최초 생성 시)
    private final FileAssetStatus toStatus;      // 변경된 상태
    private final String message;                // 상태 메시지 (실패 사유, 처리 결과 등)

    // ===== 변경 주체 =====
    private final String actor;                  // SYSTEM, N8N, userId 등
    private final String actorType;              // SYSTEM, EXTERNAL_API, USER

    // ===== 시간 정보 =====
    private final LocalDateTime changedAt;
    private final Long durationMillis;           // 이전 상태에서 현재 상태까지 소요 시간

    // ===== Private Constructor =====
    private FileAssetStatusHistory(
        FileAssetStatusHistoryId id,
        Long fileAssetId,
        FileAssetStatus fromStatus,
        FileAssetStatus toStatus,
        String message,
        String actor,
        String actorType,
        LocalDateTime changedAt,
        Long durationMillis
    ) {
        this.id = id;
        this.fileAssetId = fileAssetId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.message = message;
        this.actor = actor;
        this.actorType = actorType;
        this.changedAt = changedAt;
        this.durationMillis = durationMillis;
    }

    // ===== 정적 팩토리 메서드 =====

    /**
     * 새로운 상태 변경 히스토리 생성.
     *
     * @param fileAssetId FileAsset ID
     * @param fromStatus 이전 상태 (최초 생성 시 null)
     * @param toStatus 변경된 상태
     * @param message 상태 메시지
     * @param actor 변경 주체
     * @param actorType 변경 주체 타입
     * @param durationMillis 이전 상태 소요 시간
     * @return 새로운 FileAssetStatusHistory
     */
    public static FileAssetStatusHistory forNew(
        Long fileAssetId,
        FileAssetStatus fromStatus,
        FileAssetStatus toStatus,
        String message,
        String actor,
        String actorType,
        Long durationMillis
    ) {
        return new FileAssetStatusHistory(
            FileAssetStatusHistoryId.generate(),
            fileAssetId,
            fromStatus,
            toStatus,
            message,
            actor,
            actorType,
            LocalDateTime.now(),
            durationMillis
        );
    }

    /**
     * 시스템에 의한 상태 변경 히스토리 생성 (편의 메서드).
     */
    public static FileAssetStatusHistory forSystemChange(
        Long fileAssetId,
        FileAssetStatus fromStatus,
        FileAssetStatus toStatus,
        String message,
        Long durationMillis
    ) {
        return forNew(
            fileAssetId,
            fromStatus,
            toStatus,
            message,
            "SYSTEM",
            "SYSTEM",
            durationMillis
        );
    }

    /**
     * n8n에 의한 상태 변경 히스토리 생성 (편의 메서드).
     */
    public static FileAssetStatusHistory forN8nChange(
        Long fileAssetId,
        FileAssetStatus fromStatus,
        FileAssetStatus toStatus,
        String message,
        Long durationMillis
    ) {
        return forNew(
            fileAssetId,
            fromStatus,
            toStatus,
            message,
            "N8N",
            "EXTERNAL_API",
            durationMillis
        );
    }

    /**
     * DB에서 복원 (Persistence Layer용).
     */
    public static FileAssetStatusHistory reconstitute(
        FileAssetStatusHistoryId id,
        Long fileAssetId,
        FileAssetStatus fromStatus,
        FileAssetStatus toStatus,
        String message,
        String actor,
        String actorType,
        LocalDateTime changedAt,
        Long durationMillis
    ) {
        return new FileAssetStatusHistory(
            id,
            fileAssetId,
            fromStatus,
            toStatus,
            message,
            actor,
            actorType,
            changedAt,
            durationMillis
        );
    }

    // ===== 비즈니스 메서드 =====

    /**
     * 실패 상태인지 확인.
     */
    public boolean isFailure() {
        return toStatus == FileAssetStatus.FAILED;
    }

    /**
     * 최초 생성 히스토리인지 확인.
     */
    public boolean isInitialCreation() {
        return fromStatus == null;
    }

    /**
     * SLA 위반 여부 확인 (5초 초과).
     */
    public boolean exceedsSla(long slaMillis) {
        return durationMillis != null && durationMillis > slaMillis;
    }

    // ===== Getter =====
    public FileAssetStatusHistoryId getId() { return id; }
    public Long getFileAssetId() { return fileAssetId; }
    public FileAssetStatus getFromStatus() { return fromStatus; }
    public FileAssetStatus getToStatus() { return toStatus; }
    public String getMessage() { return message; }
    public String getActor() { return actor; }
    public String getActorType() { return actorType; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public Long getDurationMillis() { return durationMillis; }
}
```

**상태 히스토리 활용 예시**:
```
FileAsset ID: fa-001
┌─────────────────────────────────────────────────────────────────────────┐
│ 순서 │ From → To              │ Duration │ Actor  │ Message            │
├─────────────────────────────────────────────────────────────────────────┤
│  1   │ null → PENDING         │    -     │ SYSTEM │ 파일 업로드 완료    │
│  2   │ PENDING → PROCESSING   │   50ms   │ SYSTEM │ 가공 시작           │
│  3   │ PROCESSING → FAILED    │ 3,200ms  │ SYSTEM │ 이미지 손상됨       │
│  4   │ FAILED → PENDING       │    -     │ ADMIN  │ 수동 재시도 요청    │
│  5   │ PENDING → PROCESSING   │   30ms   │ SYSTEM │ 가공 재시작         │
│  6   │ PROCESSING → RESIZED   │ 2,800ms  │ SYSTEM │ 리사이징 완료       │
│  7   │ RESIZED → N8N_PROCESSING│  500ms  │ N8N    │ n8n 처리 시작       │
│  8   │ N8N_PROCESSING → N8N_COMPLETED│ 15,000ms│ N8N │ n8n 처리 완료    │
└─────────────────────────────────────────────────────────────────────────┘
```

---

#### 1.9 Aggregate: FileProcessingOutbox (신규, Outbox 패턴)

**목적**:
- DB 저장과 SQS 메시지 발행의 원자성 보장
- 메시지 발행 실패 시 자동 재시도
- 데이터 일관성 확보 (DB 저장 성공 + 메시지 발행 실패 방지)

**Outbox 패턴 흐름**:
```
┌─────────────────────────────────────────────────────────────────────────┐
│                           기존 방식 (문제 있음)                          │
├─────────────────────────────────────────────────────────────────────────┤
│  UseCase                                                                │
│    │                                                                    │
│    ├─ 1. DB 저장 (트랜잭션) ✅                                          │
│    │                                                                    │
│    └─ 2. SQS 발행 (트랜잭션 밖) ❌ ← 여기서 실패하면 데이터 불일치!     │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                         Outbox 패턴 (권장)                               │
├─────────────────────────────────────────────────────────────────────────┤
│  UseCase                                                                │
│    │                                                                    │
│    └─ 1. DB 저장 + Outbox 저장 (같은 트랜잭션) ✅                       │
│                                                                         │
│  OutboxRelay (별도 스케줄러/프로세스)                                    │
│    │                                                                    │
│    ├─ 2. PENDING 상태 Outbox 조회                                       │
│    ├─ 3. SQS 발행                                                       │
│    └─ 4. 성공 시 Outbox 상태 → SENT (또는 삭제)                         │
│                                                                         │
│  ※ SQS 발행 실패해도 Outbox에 남아있으므로 재시도 가능                  │
└─────────────────────────────────────────────────────────────────────────┘
```

**정의**:
```java
/**
 * 파일 가공 Outbox Aggregate.
 *
 * <p>Transactional Outbox 패턴을 구현하여 DB 저장과 메시지 발행의 원자성을 보장합니다.</p>
 *
 * <p><strong>컨벤션 준수</strong>:
 * <ul>
 *   <li>Lombok 금지 - Pure Java 사용
 *   <li>Long FK 전략 - JPA 관계 어노테이션 금지
 *   <li>정적 팩토리 메서드: forNew(), reconstitute()
 *   <li>Tell Don't Ask - 상태 변경은 비즈니스 메서드로
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public class FileProcessingOutbox {

    // ===== 식별 정보 =====
    private final FileProcessingOutboxId id;
    private final Long fileAssetId;              // Long FK 전략

    // ===== 이벤트 정보 =====
    private final String eventType;              // PROCESS_REQUESTED, STATUS_CHANGED, RETRY_REQUESTED
    private final String aggregateType;          // FILE_ASSET
    private final String payload;                // JSON 형태의 이벤트 데이터

    // ===== 발행 상태 =====
    private OutboxStatus status;                 // PENDING, SENT, FAILED
    private int retryCount;
    private String lastError;

    // ===== 시간 정보 =====
    private final LocalDateTime createdAt;
    private LocalDateTime processedAt;

    // ===== Private Constructor =====
    private FileProcessingOutbox(
        FileProcessingOutboxId id,
        Long fileAssetId,
        String eventType,
        String aggregateType,
        String payload,
        OutboxStatus status,
        int retryCount,
        String lastError,
        LocalDateTime createdAt,
        LocalDateTime processedAt
    ) {
        this.id = id;
        this.fileAssetId = fileAssetId;
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.payload = payload;
        this.status = status;
        this.retryCount = retryCount;
        this.lastError = lastError;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    // ===== 정적 팩토리 메서드 =====

    /**
     * 파일 가공 요청 Outbox 생성.
     *
     * @param fileAssetId FileAsset ID
     * @param payload 이벤트 페이로드 (JSON)
     * @return 새로운 FileProcessingOutbox
     */
    public static FileProcessingOutbox forProcessRequest(Long fileAssetId, String payload) {
        return new FileProcessingOutbox(
            FileProcessingOutboxId.generate(),
            fileAssetId,
            "PROCESS_REQUESTED",
            "FILE_ASSET",
            payload,
            OutboxStatus.PENDING,
            0,
            null,
            LocalDateTime.now(),
            null
        );
    }

    /**
     * 상태 변경 알림 Outbox 생성.
     *
     * @param fileAssetId FileAsset ID
     * @param payload 상태 변경 정보 (JSON)
     * @return 새로운 FileProcessingOutbox
     */
    public static FileProcessingOutbox forStatusChange(Long fileAssetId, String payload) {
        return new FileProcessingOutbox(
            FileProcessingOutboxId.generate(),
            fileAssetId,
            "STATUS_CHANGED",
            "FILE_ASSET",
            payload,
            OutboxStatus.PENDING,
            0,
            null,
            LocalDateTime.now(),
            null
        );
    }

    /**
     * 재처리 요청 Outbox 생성.
     *
     * @param fileAssetId FileAsset ID
     * @param payload 재처리 요청 정보 (JSON)
     * @return 새로운 FileProcessingOutbox
     */
    public static FileProcessingOutbox forRetryRequest(Long fileAssetId, String payload) {
        return new FileProcessingOutbox(
            FileProcessingOutboxId.generate(),
            fileAssetId,
            "RETRY_REQUESTED",
            "FILE_ASSET",
            payload,
            OutboxStatus.PENDING,
            0,
            null,
            LocalDateTime.now(),
            null
        );
    }

    /**
     * DB에서 복원 (Persistence Layer용).
     */
    public static FileProcessingOutbox reconstitute(
        FileProcessingOutboxId id,
        Long fileAssetId,
        String eventType,
        String aggregateType,
        String payload,
        OutboxStatus status,
        int retryCount,
        String lastError,
        LocalDateTime createdAt,
        LocalDateTime processedAt
    ) {
        return new FileProcessingOutbox(
            id,
            fileAssetId,
            eventType,
            aggregateType,
            payload,
            status,
            retryCount,
            lastError,
            createdAt,
            processedAt
        );
    }

    // ===== 비즈니스 메서드 (Tell Don't Ask) =====

    /**
     * 메시지 발행 성공 처리.
     */
    public void markAsSent() {
        this.status = OutboxStatus.SENT;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 메시지 발행 실패 처리.
     *
     * @param errorMessage 에러 메시지
     */
    public void markAsFailed(String errorMessage) {
        this.retryCount++;
        this.lastError = errorMessage;

        if (this.retryCount >= MAX_RETRY_COUNT) {
            this.status = OutboxStatus.FAILED;
        }
        // PENDING 상태 유지하여 재시도 대상 유지
    }

    /**
     * 재시도 가능한지 확인.
     */
    public boolean canRetry() {
        return status == OutboxStatus.PENDING && retryCount < MAX_RETRY_COUNT;
    }

    /**
     * 최대 재시도 횟수 초과 여부 확인.
     */
    public boolean isExhausted() {
        return retryCount >= MAX_RETRY_COUNT;
    }

    /**
     * 발행 완료 여부 확인.
     */
    public boolean isSent() {
        return status == OutboxStatus.SENT;
    }

    // ===== 상수 =====
    private static final int MAX_RETRY_COUNT = 3;

    // ===== Getter =====
    public FileProcessingOutboxId getId() { return id; }
    public Long getFileAssetId() { return fileAssetId; }
    public String getEventType() { return eventType; }
    public String getAggregateType() { return aggregateType; }
    public String getPayload() { return payload; }
    public OutboxStatus getStatus() { return status; }
    public int getRetryCount() { return retryCount; }
    public String getLastError() { return lastError; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }
}

/**
 * Outbox 상태
 */
public enum OutboxStatus {
    PENDING,    // 발행 대기
    SENT,       // 발행 완료
    FAILED      // 발행 실패 (재시도 횟수 초과)
}
```

**Outbox 페이로드 예시**:
```json
// PROCESS_REQUESTED
{
  "fileAssetId": 12345,
  "fileName": "product_image.jpg",
  "contentType": "image/jpeg",
  "category": "PRODUCT_IMAGE",
  "requestedAt": "2025-12-02T10:30:00Z"
}

// STATUS_CHANGED
{
  "fileAssetId": 12345,
  "fromStatus": "PROCESSING",
  "toStatus": "RESIZED",
  "changedAt": "2025-12-02T10:30:05Z"
}

// RETRY_REQUESTED
{
  "fileAssetId": 12345,
  "reason": "Previous processing failed",
  "requestedBy": "ADMIN",
  "requestedAt": "2025-12-02T11:00:00Z"
}
```

---

### 2. Application Layer

#### 2.1 Port: ImageProcessingPort (Out Port, 신규)

**정의**:
```java
/**
 * 이미지 가공 Out Port.
 *
 * <p>Infrastructure Layer에서 실제 이미지 처리 구현.
 */
public interface ImageProcessingPort {

    /**
     * 이미지 리사이징.
     *
     * @param sourceBytes 원본 이미지 바이트
     * @param variant 리사이징 버전
     * @param format 출력 포맷
     * @return 리사이징된 이미지 바이트 + 메타데이터
     */
    ImageProcessingResult resize(
        byte[] sourceBytes,
        ImageVariant variant,
        ImageFormat format
    );

    /**
     * 이미지 메타데이터 추출 (width, height).
     */
    ImageMetadata extractMetadata(byte[] imageBytes);
}

public record ImageProcessingResult(
    byte[] data,
    int width,
    int height,
    long size
) {}

public record ImageMetadata(
    int width,
    int height
) {}
```

---

#### 2.2 Port: HtmlProcessingPort (Out Port, 신규)

**정의**:
```java
/**
 * HTML 가공 Out Port.
 *
 * <p>HTML 내 이미지 추출 및 URL 교체.
 */
public interface HtmlProcessingPort {

    /**
     * HTML에서 이미지 URL 추출.
     *
     * @param htmlContent HTML 문자열
     * @return 이미지 URL 목록 (src 속성, style background 등)
     */
    List<ExtractedImage> extractImages(String htmlContent);

    /**
     * HTML 내 이미지 URL 교체.
     *
     * @param htmlContent 원본 HTML
     * @param urlMappings 기존 URL → 새 URL 매핑
     * @return URL 교체된 HTML
     */
    String replaceImageUrls(String htmlContent, Map<String, String> urlMappings);
}

public record ExtractedImage(
    String originalUrl,
    ImageSourceType sourceType  // IMG_SRC, CSS_BACKGROUND, INLINE_STYLE
) {}

public enum ImageSourceType {
    IMG_SRC,        // <img src="...">
    CSS_BACKGROUND, // background-image: url(...)
    INLINE_STYLE    // style="background: url(...)"
}
```

---

#### 2.3 Command UseCase: ProcessFileAssetUseCase (신규)

**Input**:
```java
public record ProcessFileAssetCommand(
    String fileAssetId
) {}
```

**Output**:
```java
public record ProcessFileAssetResponse(
    String fileAssetId,
    FileAssetStatus status,
    List<ProcessedFileInfo> processedFiles
) {}

public record ProcessedFileInfo(
    String processedAssetId,
    String s3Key,
    ImageVariant variant,
    ImageFormat format,
    long fileSize
) {}
```

**Transaction**: Yes (상태 업데이트)
- ⚠️ **S3 업로드, 이미지 가공은 트랜잭션 밖**

**비즈니스 로직**:
```
1. FileAsset 조회
2. 상태 검증 (PENDING만 처리 가능)
3. 상태 변경 (PENDING → PROCESSING) + 트랜잭션 커밋
4. ContentType 확인:
   - 이미지: processImage()
   - HTML: processHtml()
   - Excel: skip (가공 없이 RESIZED 처리)
5. 결과 저장 (ProcessedFileAsset)
6. 상태 변경 (PROCESSING → RESIZED) + 트랜잭션 커밋
```

**processImage() 흐름**:
```
1. S3에서 원본 이미지 다운로드
2. 메타데이터 추출 (width, height)
3. 각 Variant별 리사이징:
   - LARGE (1200px), MEDIUM (600px), THUMBNAIL (200px)
4. 각 Format별 변환:
   - WebP + 원본 폴백 (JPEG/PNG)
5. S3 업로드 (suffix 방식: image_large.webp)
6. ProcessedFileAsset 생성
```

**processHtml() 흐름**:
```
1. S3에서 HTML 다운로드
2. 이미지 URL 추출 (HtmlProcessingPort)
3. 각 이미지에 대해:
   a. 외부 URL이면 다운로드
   b. 리사이징 (processImage 재사용)
   c. S3 업로드
   d. ProcessedFileAsset 생성 (parentAssetId 설정)
4. HTML 내 URL 교체
5. 교체된 HTML S3 업로드
6. 원본 FileAsset 업데이트 (새 S3Key)
```

---

#### 2.4 Command UseCase: UpdateFileAssetStatusUseCase (신규)

**n8n에서 처리 상태 업데이트용**

**Input**:
```java
public record UpdateFileAssetStatusCommand(
    String fileAssetId,
    FileAssetStatus targetStatus,
    String statusMessage  // 선택적 메시지
) {}
```

**Output**:
```java
public record UpdateFileAssetStatusResponse(
    String fileAssetId,
    FileAssetStatus previousStatus,
    FileAssetStatus currentStatus
) {}
```

**비즈니스 로직**:
```
1. FileAsset 조회
2. 상태 전환 검증:
   - RESIZED → N8N_PROCESSING (허용)
   - N8N_PROCESSING → N8N_COMPLETED (허용)
   - N8N_PROCESSING → FAILED (허용)
   - 그 외 → 예외
3. 상태 변경 + 저장
```

---

#### 2.5 Query UseCase: ListFileAssetsForN8nUseCase (신규)

**n8n에서 가공 완료된 파일 목록 조회용**

**Input**:
```java
public record ListFileAssetsForN8nQuery(
    FileAssetStatus status,       // 필터: RESIZED, N8N_PROCESSING 등
    ContentType contentType,      // 필터: image/*, text/html 등 (선택)
    UploadCategory category,      // 필터: PRODUCT_IMAGE, HTML 등 (선택)
    LocalDateTime fromDate,       // 필터: 생성일 시작
    LocalDateTime toDate,         // 필터: 생성일 종료
    Long tenantId,               // 필터: 테넌트 (권한)
    Long organizationId,         // 필터: 조직 (권한)
    int page,
    int size
) {}
```

**Output**:
```java
public record FileAssetForN8nResponse(
    String fileAssetId,
    String fileName,
    String contentType,
    String category,
    FileAssetStatus status,
    String downloadUrl,           // Presigned URL
    List<ProcessedFileInfo> processedFiles,
    LocalDateTime createdAt,
    LocalDateTime processedAt
) {}
```

---

#### 2.6 SQS Message Handler: FileProcessingMessageHandler (신규)

**SQS 메시지 구조**:
```java
public record FileProcessingMessage(
    String fileAssetId,
    String messageType  // PROCESS, RETRY
) {}
```

**처리 흐름**:
```
1. SQS 메시지 수신 (fileAssetId)
2. ProcessFileAssetUseCase 호출
3. 성공 시: 메시지 삭제
4. 실패 시:
   - 재시도 가능 에러 → 메시지 유지 (Visibility Timeout 후 재처리)
   - 재시도 불가 에러 → DLQ 이동 + 상태 FAILED
```

**Zero-Tolerance 규칙 준수**:
- ✅ Command/Query 분리 (CQRS)
- ✅ Transaction 경계 엄격 관리 (S3/이미지 가공은 트랜잭션 밖)
- ✅ Orchestration Pattern (SQS 메시지 기반)
- ✅ TransactionManager 패턴 적용 (단일 Persistence Port당 Manager)
- ✅ DTO 패키지 분리 (dto/command/, dto/query/, dto/response/)
- ✅ Assembler 패턴 사용 (Command → Domain, Domain → Response 변환)

---

#### 2.7 Transaction 경계 설계 (TransactionManager 패턴)

**패턴 구조**:
```
UseCase Service
    └─ TransactionManager (단일 Persistence Port만 의존, @Transactional)
        └─ PersistencePort
```

**TransactionManager**:
```java
/**
 * ProcessedFileAsset Transaction Manager.
 * - ProcessedFileAssetPersistencePort만 의존
 * - 트랜잭션 짧게 유지
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@Transactional
public class ProcessedFileAssetTransactionManager {

    private final ProcessedFileAssetPersistencePort persistencePort;

    public ProcessedFileAssetTransactionManager(ProcessedFileAssetPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    /**
     * ProcessedFileAsset 저장 (트랜잭션)
     */
    public ProcessedFileAsset save(ProcessedFileAsset processedFileAsset) {
        return persistencePort.save(processedFileAsset);
    }

    /**
     * ProcessedFileAsset 배치 저장 (트랜잭션)
     */
    public List<ProcessedFileAsset> saveAll(List<ProcessedFileAsset> processedFileAssets) {
        return persistencePort.saveAll(processedFileAssets);
    }
}
```

**UseCase 구현** (TransactionManager 사용):
```java
/**
 * 파일 에셋 가공 UseCase 구현.
 *
 * <p><strong>컨벤션 준수</strong>:
 * <ul>
 *   <li>TransactionManager 사용 - UseCase에 @Transactional 금지
 *   <li>외부 I/O는 트랜잭션 밖에서 실행
 *   <li>Assembler로 DTO ↔ Domain 변환
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class ProcessFileAssetService implements ProcessFileAssetUseCase {

    private final FileAssetTransactionManager fileAssetManager;
    private final ProcessedFileAssetTransactionManager processedFileAssetManager;
    private final FileAssetQueryPort fileAssetQueryPort;
    private final ImageProcessingPort imageProcessingPort;
    private final S3StoragePort s3StoragePort;
    private final ProcessFileAssetAssembler assembler;

    public ProcessFileAssetService(
        FileAssetTransactionManager fileAssetManager,
        ProcessedFileAssetTransactionManager processedFileAssetManager,
        FileAssetQueryPort fileAssetQueryPort,
        ImageProcessingPort imageProcessingPort,
        S3StoragePort s3StoragePort,
        ProcessFileAssetAssembler assembler
    ) {
        this.fileAssetManager = fileAssetManager;
        this.processedFileAssetManager = processedFileAssetManager;
        this.fileAssetQueryPort = fileAssetQueryPort;
        this.imageProcessingPort = imageProcessingPort;
        this.s3StoragePort = s3StoragePort;
        this.assembler = assembler;
    }

    @Override
    public ProcessFileAssetResponse execute(ProcessFileAssetCommand command) {
        // 1. 조회 (트랜잭션 밖)
        FileAsset fileAsset = fileAssetQueryPort.findById(command.fileAssetId())
            .orElseThrow(() -> new FileAssetNotFoundException(command.fileAssetId()));

        // 2. 상태 검증
        fileAsset.validateCanProcess();

        // 3. 상태 변경 → PROCESSING (짧은 트랜잭션)
        fileAsset.startProcessing();
        fileAssetManager.save(fileAsset);

        // 4. 이미지 가공 (트랜잭션 밖, 외부 I/O)
        List<ProcessedFileAsset> processedAssets = processImages(fileAsset);

        // 5. S3 업로드 (트랜잭션 밖, 외부 I/O)
        uploadToS3(processedAssets);

        // 6. 결과 저장 (짧은 트랜잭션)
        processedFileAssetManager.saveAll(processedAssets);

        // 7. 상태 변경 → RESIZED (짧은 트랜잭션)
        fileAsset.completeProcessing();
        fileAssetManager.save(fileAsset);

        // 8. Response 변환 (Assembler)
        return assembler.toResponse(fileAsset, processedAssets);
    }

    private List<ProcessedFileAsset> processImages(FileAsset fileAsset) {
        // 이미지 가공 로직 (트랜잭션 밖)
        // ...
    }

    private void uploadToS3(List<ProcessedFileAsset> processedAssets) {
        // S3 업로드 로직 (트랜잭션 밖)
        // ...
    }
}
```

**핵심 원칙**:
- ⚠️ UseCase 인터페이스/구현체에 `@Transactional` 금지
- ⚠️ TransactionManager만 `@Transactional` 가짐
- ⚠️ 외부 I/O (S3, 이미지 가공)는 반드시 트랜잭션 밖에서 실행
- ✅ 트랜잭션은 짧게 유지 (저장만)

---

#### 2.8 DTO 패키지 구조

**컨벤션 준수 패키지 구조**:
```
application/fileasset/
├─ dto/
│   ├─ command/
│   │   ├─ ProcessFileAssetCommand.java
│   │   └─ UpdateFileAssetStatusCommand.java
│   ├─ query/
│   │   └─ ListFileAssetsForN8nQuery.java
│   └─ response/
│       ├─ ProcessFileAssetResponse.java
│       ├─ FileAssetForN8nResponse.java
│       └─ ProcessedFileInfo.java
├─ port/
│   ├─ in/
│   │   ├─ command/
│   │   │   ├─ ProcessFileAssetUseCase.java
│   │   │   └─ UpdateFileAssetStatusUseCase.java
│   │   └─ query/
│   │       └─ ListFileAssetsForN8nUseCase.java
│   └─ out/
│       ├─ command/
│       │   ├─ FileAssetPersistencePort.java
│       │   └─ ProcessedFileAssetPersistencePort.java
│       └─ query/
│           ├─ FileAssetQueryPort.java
│           └─ ProcessedFileAssetQueryPort.java
├─ manager/
│   ├─ FileAssetTransactionManager.java
│   └─ ProcessedFileAssetTransactionManager.java
├─ assembler/
│   ├─ ProcessFileAssetAssembler.java
│   └─ FileAssetForN8nAssembler.java
└─ service/
    ├─ ProcessFileAssetService.java
    ├─ UpdateFileAssetStatusService.java
    └─ ListFileAssetsForN8nService.java
```

---

#### 2.9 Port: FileProcessingOutboxPersistencePort (Out Port, 신규)

**정의**:
```java
/**
 * FileProcessingOutbox Persistence Out Port.
 *
 * <p><strong>컨벤션 준수</strong>:
 * <ul>
 *   <li>단일 Aggregate 저장만 담당
 *   <li>TransactionManager에서 사용
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface FileProcessingOutboxPersistencePort {

    /**
     * Outbox 이벤트 저장.
     *
     * @param outbox 저장할 Outbox 이벤트
     * @return 저장된 Outbox 이벤트
     */
    FileProcessingOutbox save(FileProcessingOutbox outbox);

    /**
     * Outbox 이벤트 배치 저장.
     *
     * @param outboxList 저장할 Outbox 이벤트 목록
     * @return 저장된 Outbox 이벤트 목록
     */
    List<FileProcessingOutbox> saveAll(List<FileProcessingOutbox> outboxList);
}
```

**Query Port**:
```java
/**
 * FileProcessingOutbox Query Out Port.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface FileProcessingOutboxQueryPort {

    /**
     * PENDING 상태의 Outbox 이벤트 조회 (전송 대기).
     *
     * @param limit 조회 개수 제한
     * @return PENDING 상태 Outbox 목록
     */
    List<FileProcessingOutbox> findPendingEvents(int limit);

    /**
     * 재시도 가능한 FAILED 이벤트 조회.
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param limit 조회 개수 제한
     * @return 재시도 가능한 FAILED 이벤트 목록
     */
    List<FileProcessingOutbox> findRetryableFailedEvents(int maxRetryCount, int limit);
}
```

---

#### 2.10 Port: FileAssetStatusHistoryPersistencePort (Out Port, 신규)

**정의**:
```java
/**
 * FileAssetStatusHistory Persistence Out Port.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface FileAssetStatusHistoryPersistencePort {

    /**
     * 상태 변경 이력 저장.
     *
     * @param history 저장할 이력
     * @return 저장된 이력
     */
    FileAssetStatusHistory save(FileAssetStatusHistory history);
}
```

**Query Port**:
```java
/**
 * FileAssetStatusHistory Query Out Port.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface FileAssetStatusHistoryQueryPort {

    /**
     * 특정 FileAsset의 전체 상태 변경 이력 조회.
     *
     * @param fileAssetId FileAsset ID
     * @return 상태 변경 이력 목록 (시간순)
     */
    List<FileAssetStatusHistory> findByFileAssetId(Long fileAssetId);

    /**
     * 특정 FileAsset의 최근 상태 변경 이력 조회.
     *
     * @param fileAssetId FileAsset ID
     * @return 가장 최근 상태 변경 이력
     */
    Optional<FileAssetStatusHistory> findLatestByFileAssetId(Long fileAssetId);

    /**
     * SLA 초과 이력 조회 (모니터링용).
     *
     * @param slaMillis SLA 기준 밀리초
     * @param fromDate 조회 시작일
     * @param limit 조회 개수 제한
     * @return SLA 초과 이력 목록
     */
    List<FileAssetStatusHistory> findExceedingSla(long slaMillis, LocalDateTime fromDate, int limit);
}
```

---

#### 2.11 TransactionManager: Outbox 및 History (신규)

**FileProcessingOutboxTransactionManager**:
```java
/**
 * FileProcessingOutbox Transaction Manager.
 * - FileProcessingOutboxPersistencePort만 의존
 * - 트랜잭션 짧게 유지
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@Transactional
public class FileProcessingOutboxTransactionManager {

    private final FileProcessingOutboxPersistencePort persistencePort;

    public FileProcessingOutboxTransactionManager(FileProcessingOutboxPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    /**
     * Outbox 이벤트 저장 (트랜잭션)
     */
    public FileProcessingOutbox save(FileProcessingOutbox outbox) {
        return persistencePort.save(outbox);
    }

    /**
     * Outbox 이벤트 배치 저장 (트랜잭션)
     */
    public List<FileProcessingOutbox> saveAll(List<FileProcessingOutbox> outboxList) {
        return persistencePort.saveAll(outboxList);
    }
}
```

**FileAssetStatusHistoryTransactionManager**:
```java
/**
 * FileAssetStatusHistory Transaction Manager.
 * - FileAssetStatusHistoryPersistencePort만 의존
 * - 트랜잭션 짧게 유지
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@Transactional
public class FileAssetStatusHistoryTransactionManager {

    private final FileAssetStatusHistoryPersistencePort persistencePort;

    public FileAssetStatusHistoryTransactionManager(FileAssetStatusHistoryPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    /**
     * 상태 변경 이력 저장 (트랜잭션)
     */
    public FileAssetStatusHistory save(FileAssetStatusHistory history) {
        return persistencePort.save(history);
    }
}
```

---

#### 2.12 Facade: FileAssetProcessingFacade (신규)

**Outbox 패턴 적용 - 여러 TransactionManager 조합**:
```java
/**
 * FileAsset Processing Facade.
 * - 여러 TransactionManager 조합
 * - Outbox 패턴으로 DB + 메시지 원자성 보장
 *
 * <p><strong>컨벤션 준수</strong>:
 * <ul>
 *   <li>Facade는 여러 Manager 조합만 담당
 *   <li>비즈니스 로직은 Domain Layer에 위임
 *   <li>조합 트랜잭션 관리
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
@Transactional
public class FileAssetProcessingFacade {

    private final FileAssetTransactionManager fileAssetManager;
    private final FileProcessingOutboxTransactionManager outboxManager;
    private final FileAssetStatusHistoryTransactionManager historyManager;

    public FileAssetProcessingFacade(
        FileAssetTransactionManager fileAssetManager,
        FileProcessingOutboxTransactionManager outboxManager,
        FileAssetStatusHistoryTransactionManager historyManager
    ) {
        this.fileAssetManager = fileAssetManager;
        this.outboxManager = outboxManager;
        this.historyManager = historyManager;
    }

    /**
     * 파일 업로드 후 가공 요청 (Outbox 패턴).
     *
     * <p>하나의 트랜잭션으로:
     * <ol>
     *   <li>FileAsset 상태 변경
     *   <li>StatusHistory 저장
     *   <li>Outbox 이벤트 저장
     * </ol>
     *
     * @param fileAsset 상태 변경할 FileAsset
     * @param fromStatus 이전 상태
     * @param toStatus 새 상태
     * @param payload SQS 메시지 payload
     * @return 저장된 FileAsset
     */
    public FileAsset requestProcessingWithOutbox(
        FileAsset fileAsset,
        FileAssetStatus fromStatus,
        FileAssetStatus toStatus,
        String payload
    ) {
        // 1. FileAsset 상태 변경 + 저장
        FileAsset savedFileAsset = fileAssetManager.save(fileAsset);

        // 2. StatusHistory 저장
        FileAssetStatusHistory history = FileAssetStatusHistory.forSystemChange(
            savedFileAsset.getIdValue(),
            fromStatus,
            toStatus,
            "Processing requested"
        );
        historyManager.save(history);

        // 3. Outbox 이벤트 저장 (DB 트랜잭션 내)
        FileProcessingOutbox outbox = FileProcessingOutbox.forProcessRequest(
            savedFileAsset.getIdValue(),
            payload
        );
        outboxManager.save(outbox);

        return savedFileAsset;
    }

    /**
     * 상태 변경 (History + Outbox).
     *
     * @param fileAsset 상태 변경된 FileAsset
     * @param fromStatus 이전 상태
     * @param toStatus 새 상태
     * @param message 상태 변경 메시지
     * @param actor 변경 주체
     * @param actorType 변경 주체 타입
     * @param durationMillis 이전 상태 체류 시간
     */
    public FileAsset updateStatusWithHistory(
        FileAsset fileAsset,
        FileAssetStatus fromStatus,
        FileAssetStatus toStatus,
        String message,
        String actor,
        String actorType,
        Long durationMillis
    ) {
        // 1. FileAsset 저장
        FileAsset savedFileAsset = fileAssetManager.save(fileAsset);

        // 2. StatusHistory 저장
        FileAssetStatusHistory history = FileAssetStatusHistory.forNew(
            savedFileAsset.getIdValue(),
            fromStatus,
            toStatus,
            message,
            actor,
            actorType,
            durationMillis
        );
        historyManager.save(history);

        return savedFileAsset;
    }
}
```

---

#### 2.13 Scheduler: OutboxRelayScheduler (신규)

**Outbox Relay - SQS 전송 스케줄러**:
```java
/**
 * Outbox Relay Scheduler.
 * - PENDING 상태 Outbox 이벤트를 SQS로 전송
 * - 전송 성공 시 SENT, 실패 시 FAILED 상태로 변경
 *
 * <p><strong>Outbox 패턴 핵심</strong>:
 * <ul>
 *   <li>DB 트랜잭션과 메시지 발행 분리
 *   <li>At-least-once delivery 보장
 *   <li>주기적 폴링으로 미전송 이벤트 처리
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class OutboxRelayScheduler {

    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRY_COUNT = 3;

    private final FileProcessingOutboxQueryPort outboxQueryPort;
    private final FileProcessingOutboxTransactionManager outboxManager;
    private final SqsMessagePort sqsMessagePort;

    public OutboxRelayScheduler(
        FileProcessingOutboxQueryPort outboxQueryPort,
        FileProcessingOutboxTransactionManager outboxManager,
        SqsMessagePort sqsMessagePort
    ) {
        this.outboxQueryPort = outboxQueryPort;
        this.outboxManager = outboxManager;
        this.sqsMessagePort = sqsMessagePort;
    }

    /**
     * PENDING 이벤트 전송 (5초마다).
     */
    @Scheduled(fixedDelay = 5000)
    public void relayPendingEvents() {
        List<FileProcessingOutbox> pendingEvents =
            outboxQueryPort.findPendingEvents(BATCH_SIZE);

        for (FileProcessingOutbox outbox : pendingEvents) {
            try {
                // SQS 전송 (트랜잭션 밖)
                sqsMessagePort.sendMessage(outbox.getPayload());

                // 성공: SENT 상태로 변경 (트랜잭션)
                outbox.markAsSent();
                outboxManager.save(outbox);

            } catch (Exception e) {
                // 실패: FAILED 상태로 변경 (트랜잭션)
                outbox.markAsFailed(e.getMessage());
                outboxManager.save(outbox);
            }
        }
    }

    /**
     * 재시도 가능한 FAILED 이벤트 재전송 (1분마다).
     */
    @Scheduled(fixedDelay = 60000)
    public void retryFailedEvents() {
        List<FileProcessingOutbox> failedEvents =
            outboxQueryPort.findRetryableFailedEvents(MAX_RETRY_COUNT, BATCH_SIZE);

        for (FileProcessingOutbox outbox : failedEvents) {
            if (!outbox.canRetry()) {
                continue;  // MAX_RETRY 초과
            }

            try {
                sqsMessagePort.sendMessage(outbox.getPayload());
                outbox.markAsSent();
                outboxManager.save(outbox);

            } catch (Exception e) {
                outbox.markAsFailed(e.getMessage());
                outboxManager.save(outbox);
            }
        }
    }
}
```

**SqsMessagePort (Out Port)**:
```java
/**
 * SQS Message Out Port.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SqsMessagePort {

    /**
     * SQS 메시지 전송.
     *
     * @param payload 메시지 payload (JSON)
     * @return 메시지 ID
     */
    String sendMessage(String payload);
}
```

---

#### 2.14 Updated UseCase Flow (Outbox 패턴 적용)

**기존 ProcessFileAssetService 수정**:
```java
/**
 * 파일 에셋 가공 UseCase 구현 (Outbox 패턴 적용).
 *
 * <p><strong>변경사항</strong>:
 * <ul>
 *   <li>Facade 사용으로 Outbox + History 원자성 보장
 *   <li>SQS 직접 전송 → Outbox 테이블 저장으로 변경
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class ProcessFileAssetService implements ProcessFileAssetUseCase {

    private final FileAssetProcessingFacade processingFacade;
    private final FileAssetQueryPort fileAssetQueryPort;
    private final ProcessedFileAssetTransactionManager processedFileAssetManager;
    private final ImageProcessingPort imageProcessingPort;
    private final S3StoragePort s3StoragePort;
    private final ProcessFileAssetAssembler assembler;
    private final ObjectMapper objectMapper;

    public ProcessFileAssetService(
        FileAssetProcessingFacade processingFacade,
        FileAssetQueryPort fileAssetQueryPort,
        ProcessedFileAssetTransactionManager processedFileAssetManager,
        ImageProcessingPort imageProcessingPort,
        S3StoragePort s3StoragePort,
        ProcessFileAssetAssembler assembler,
        ObjectMapper objectMapper
    ) {
        this.processingFacade = processingFacade;
        this.fileAssetQueryPort = fileAssetQueryPort;
        this.processedFileAssetManager = processedFileAssetManager;
        this.imageProcessingPort = imageProcessingPort;
        this.s3StoragePort = s3StoragePort;
        this.assembler = assembler;
        this.objectMapper = objectMapper;
    }

    @Override
    public ProcessFileAssetResponse execute(ProcessFileAssetCommand command) {
        // 1. 조회 (트랜잭션 밖)
        FileAsset fileAsset = fileAssetQueryPort.findById(command.fileAssetId())
            .orElseThrow(() -> new FileAssetNotFoundException(command.fileAssetId()));

        // 2. 상태 검증
        FileAssetStatus fromStatus = fileAsset.getStatus();
        fileAsset.validateCanProcess();

        // 3. 상태 변경 + History + Outbox (Facade, 원자적 트랜잭션)
        fileAsset.startProcessing();
        processingFacade.updateStatusWithHistory(
            fileAsset,
            fromStatus,
            FileAssetStatus.PROCESSING,
            "Processing started",
            "SYSTEM",
            "SYSTEM",
            null
        );

        // 4. 이미지 가공 (트랜잭션 밖, 외부 I/O)
        List<ProcessedFileAsset> processedAssets = processImages(fileAsset);

        // 5. S3 업로드 (트랜잭션 밖, 외부 I/O)
        uploadToS3(processedAssets);

        // 6. 결과 저장 (짧은 트랜잭션)
        processedFileAssetManager.saveAll(processedAssets);

        // 7. 상태 변경 → RESIZED + History (Facade)
        fileAsset.completeProcessing();
        processingFacade.updateStatusWithHistory(
            fileAsset,
            FileAssetStatus.PROCESSING,
            FileAssetStatus.RESIZED,
            "Processing completed",
            "SYSTEM",
            "SYSTEM",
            calculateDuration(fileAsset)
        );

        // 8. Response 변환 (Assembler)
        return assembler.toResponse(fileAsset, processedAssets);
    }

    private List<ProcessedFileAsset> processImages(FileAsset fileAsset) {
        // 이미지 가공 로직 (트랜잭션 밖)
        // ...
    }

    private void uploadToS3(List<ProcessedFileAsset> processedAssets) {
        // S3 업로드 로직 (트랜잭션 밖)
        // ...
    }

    private Long calculateDuration(FileAsset fileAsset) {
        // 처리 시간 계산
        // ...
    }
}
```

**Upload 시 Outbox 적용** (RequestFileProcessingUseCase):
```java
/**
 * 파일 업로드 후 가공 요청 UseCase.
 *
 * <p>Outbox 패턴 적용:
 * <ol>
 *   <li>파일 업로드 완료
 *   <li>FileAsset 상태: PENDING
 *   <li>Outbox 이벤트 저장 (DB 트랜잭션 내)
 *   <li>OutboxRelayScheduler가 비동기로 SQS 전송
 * </ol>
 */
@Service
public class RequestFileProcessingService implements RequestFileProcessingUseCase {

    private final FileAssetProcessingFacade processingFacade;
    private final FileAssetQueryPort queryPort;
    private final ObjectMapper objectMapper;

    @Override
    public void execute(RequestFileProcessingCommand command) {
        // 1. FileAsset 조회
        FileAsset fileAsset = queryPort.findById(command.fileAssetId())
            .orElseThrow(() -> new FileAssetNotFoundException(command.fileAssetId()));

        // 2. 상태 검증 (UPLOADED → PENDING)
        FileAssetStatus fromStatus = fileAsset.getStatus();
        fileAsset.requestProcessing();

        // 3. Facade로 원자적 저장 (FileAsset + History + Outbox)
        String payload = createPayload(fileAsset);
        processingFacade.requestProcessingWithOutbox(
            fileAsset,
            fromStatus,
            FileAssetStatus.PENDING,
            payload
        );

        // 4. SQS 전송은 OutboxRelayScheduler가 비동기로 처리
    }

    private String createPayload(FileAsset fileAsset) {
        try {
            FileProcessingMessage message = new FileProcessingMessage(
                fileAsset.getAssetIdValue(),
                "PROCESS"
            );
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize payload", e);
        }
    }
}
```

---

#### 2.15 Updated DTO 패키지 구조 (Outbox + History 추가)

**확장된 패키지 구조**:
```
application/fileasset/
├─ dto/
│   ├─ command/
│   │   ├─ ProcessFileAssetCommand.java
│   │   ├─ UpdateFileAssetStatusCommand.java
│   │   └─ RequestFileProcessingCommand.java   ← 신규
│   ├─ query/
│   │   └─ ListFileAssetsForN8nQuery.java
│   └─ response/
│       ├─ ProcessFileAssetResponse.java
│       ├─ FileAssetForN8nResponse.java
│       └─ ProcessedFileInfo.java
├─ port/
│   ├─ in/
│   │   ├─ command/
│   │   │   ├─ ProcessFileAssetUseCase.java
│   │   │   ├─ UpdateFileAssetStatusUseCase.java
│   │   │   └─ RequestFileProcessingUseCase.java   ← 신규
│   │   └─ query/
│   │       └─ ListFileAssetsForN8nUseCase.java
│   └─ out/
│       ├─ command/
│       │   ├─ FileAssetPersistencePort.java
│       │   ├─ ProcessedFileAssetPersistencePort.java
│       │   ├─ FileProcessingOutboxPersistencePort.java      ← 신규
│       │   └─ FileAssetStatusHistoryPersistencePort.java    ← 신규
│       └─ query/
│           ├─ FileAssetQueryPort.java
│           ├─ ProcessedFileAssetQueryPort.java
│           ├─ FileProcessingOutboxQueryPort.java            ← 신규
│           └─ FileAssetStatusHistoryQueryPort.java          ← 신규
├─ manager/
│   ├─ FileAssetTransactionManager.java
│   ├─ ProcessedFileAssetTransactionManager.java
│   ├─ FileProcessingOutboxTransactionManager.java           ← 신규
│   └─ FileAssetStatusHistoryTransactionManager.java         ← 신규
├─ facade/
│   └─ FileAssetProcessingFacade.java                        ← 신규
├─ scheduler/
│   └─ OutboxRelayScheduler.java                             ← 신규
├─ assembler/
│   ├─ ProcessFileAssetAssembler.java
│   └─ FileAssetForN8nAssembler.java
└─ service/
    ├─ ProcessFileAssetService.java
    ├─ UpdateFileAssetStatusService.java
    ├─ ListFileAssetsForN8nService.java
    └─ RequestFileProcessingService.java                     ← 신규
```

---

### 3. Persistence Layer

#### 3.1 JPA Entity: ProcessedFileAssetJpaEntity (신규)

**테이블**: `processed_file_assets`

**필드**:
| Column | Type | Constraint | Description |
|--------|------|------------|-------------|
| id | BIGINT | PK, Auto Increment | 내부 ID |
| processed_asset_id | VARCHAR(36) | Unique, Not Null, Index | UUID |
| original_asset_id | VARCHAR(36) | Not Null, Index, FK | 원본 FileAsset ID |
| parent_asset_id | VARCHAR(36) | Nullable, Index | HTML 부모 ID |
| variant | VARCHAR(20) | Not Null | ORIGINAL, LARGE, MEDIUM, THUMBNAIL |
| format | VARCHAR(10) | Not Null | WEBP, JPEG, PNG |
| file_name | VARCHAR(255) | Not Null | 파일명 |
| file_size | BIGINT | Not Null | 바이트 |
| width | INT | Nullable | px |
| height | INT | Nullable | px |
| bucket | VARCHAR(100) | Not Null | S3 버킷 |
| s3_key | VARCHAR(500) | Not Null, Index | S3 키 |
| user_id | BIGINT | Nullable | 사용자 ID |
| organization_id | BIGINT | Not Null | 조직 ID |
| tenant_id | BIGINT | Not Null, Index | 테넌트 ID |
| created_at | DATETIME | Not Null, Index | 생성 시각 |

**인덱스**:
- `idx_original_asset_id` (original_asset_id) - 원본 기준 조회
- `idx_parent_asset_id` (parent_asset_id) - HTML 기준 하위 이미지 조회
- `idx_tenant_created` (tenant_id, created_at DESC) - 테넌트별 목록

**Entity 정의** (컨벤션 준수):
```java
/**
 * ProcessedFileAsset JPA Entity.
 *
 * <p><strong>컨벤션 준수</strong>:
 * <ul>
 *   <li>Lombok 금지 - Pure Java 사용
 *   <li>Long FK 전략 - JPA 관계 어노테이션 금지
 *   <li>정적 팩토리 메서드: of()
 *   <li>protected 기본 생성자 (JPA용)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Entity
@Table(name = "processed_file_assets")
public class ProcessedFileAssetJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "processed_asset_id", nullable = false, unique = true)
    private String processedAssetId;

    @Column(name = "original_asset_id", nullable = false)
    private String originalAssetId;  // Long FK 전략

    @Column(name = "parent_asset_id")
    private String parentAssetId;    // Long FK 전략 (Nullable)

    @Column(name = "variant", nullable = false)
    @Enumerated(EnumType.STRING)
    private ImageVariantType variant;

    @Column(name = "format", nullable = false)
    @Enumerated(EnumType.STRING)
    private ImageFormatType format;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "bucket", nullable = false)
    private String bucket;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @Column(name = "user_id")
    private Long userId;             // Long FK 전략

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;     // Long FK 전략

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;           // Long FK 전략

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // ===== JPA용 기본 생성자 =====
    protected ProcessedFileAssetJpaEntity() {
    }

    // ===== Private Constructor =====
    private ProcessedFileAssetJpaEntity(
        String processedAssetId,
        String originalAssetId,
        String parentAssetId,
        ImageVariantType variant,
        ImageFormatType format,
        String fileName,
        Long fileSize,
        Integer width,
        Integer height,
        String bucket,
        String s3Key,
        Long userId,
        Long organizationId,
        Long tenantId,
        LocalDateTime createdAt
    ) {
        this.processedAssetId = processedAssetId;
        this.originalAssetId = originalAssetId;
        this.parentAssetId = parentAssetId;
        this.variant = variant;
        this.format = format;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.width = width;
        this.height = height;
        this.bucket = bucket;
        this.s3Key = s3Key;
        this.userId = userId;
        this.organizationId = organizationId;
        this.tenantId = tenantId;
        this.createdAt = createdAt;
    }

    // ===== 정적 팩토리 메서드 =====

    /**
     * Domain → Entity 변환.
     */
    public static ProcessedFileAssetJpaEntity of(ProcessedFileAsset domain) {
        return new ProcessedFileAssetJpaEntity(
            domain.getId().getValue(),
            domain.getOriginalAssetId().getValue(),
            domain.getParentAssetId() != null ? domain.getParentAssetId().getValue() : null,
            domain.getVariant().type(),
            domain.getFormat().type(),
            domain.getFileName().getValue(),
            domain.getFileSize().getValue(),
            domain.getWidth(),
            domain.getHeight(),
            domain.getBucket().getValue(),
            domain.getS3Key().getValue(),
            domain.getUserId(),
            domain.getOrganizationId(),
            domain.getTenantId(),
            domain.getCreatedAt()
        );
    }

    // ===== Getter (Lombok 금지) =====
    public Long getId() { return id; }
    public String getProcessedAssetId() { return processedAssetId; }
    public String getOriginalAssetId() { return originalAssetId; }
    public String getParentAssetId() { return parentAssetId; }
    public ImageVariantType getVariant() { return variant; }
    public ImageFormatType getFormat() { return format; }
    public String getFileName() { return fileName; }
    public Long getFileSize() { return fileSize; }
    public Integer getWidth() { return width; }
    public Integer getHeight() { return height; }
    public String getBucket() { return bucket; }
    public String getS3Key() { return s3Key; }
    public Long getUserId() { return userId; }
    public Long getOrganizationId() { return organizationId; }
    public Long getTenantId() { return tenantId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

**Zero-Tolerance 규칙 준수**:
- ❌ Lombok 금지 (@Data, @Getter, @Setter 등)
- ❌ JPA 관계 어노테이션 금지 (@ManyToOne, @OneToMany)
- ✅ Long FK 전략 (organizationId, tenantId, userId)
- ✅ 정적 팩토리 메서드 `of()` 사용
- ✅ protected 기본 생성자 (JPA 요구사항)

---

#### 3.2 FileAssetJpaEntity 수정

**추가 필드**:
| Column | Type | Constraint | Description |
|--------|------|------------|-------------|
| processed_at | DATETIME | Nullable | 가공 완료 시각 |
| status_message | VARCHAR(500) | Nullable | 상태 메시지 (에러 등) |

**인덱스 추가**:
- `idx_status_created` (status, created_at DESC) - 상태별 조회 (n8n용)
- `idx_category_status` (category, status) - 카테고리+상태별 조회

---

#### 3.3 Repository: ProcessedFileAssetQueryRepository (신규)

**QueryDSL Repository 정의** (컨벤션 준수):
```java
/**
 * ProcessedFileAsset Query Repository.
 *
 * <p><strong>컨벤션 준수</strong>:
 * <ul>
 *   <li>QueryDSL DTO Projection - Entity 직접 반환 금지
 *   <li>N+1 방지 - fetch join 또는 DTO Projection 사용
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ProcessedFileAssetQueryRepository {

    /**
     * 원본 FileAsset ID로 모든 가공 버전 조회.
     *
     * @param originalAssetId 원본 FileAsset ID
     * @return 가공된 파일 목록 (DTO Projection)
     */
    List<ProcessedFileAssetDto> findByOriginalAssetId(String originalAssetId);

    /**
     * HTML 부모 ID로 추출된 이미지 조회.
     *
     * @param parentAssetId HTML FileAsset ID
     * @return 추출된 이미지 목록 (DTO Projection)
     */
    List<ProcessedFileAssetDto> findByParentAssetId(String parentAssetId);
}
```

**QueryDSL 구현체**:
```java
/**
 * ProcessedFileAsset Query Repository 구현체.
 *
 * @author development-team
 * @since 1.0.0
 */
@Repository
public class ProcessedFileAssetQueryRepositoryImpl implements ProcessedFileAssetQueryRepository {

    private final JPAQueryFactory queryFactory;

    public ProcessedFileAssetQueryRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<ProcessedFileAssetDto> findByOriginalAssetId(String originalAssetId) {
        QProcessedFileAssetJpaEntity entity = QProcessedFileAssetJpaEntity.processedFileAssetJpaEntity;

        return queryFactory
            .select(Projections.constructor(ProcessedFileAssetDto.class,
                entity.processedAssetId,
                entity.originalAssetId,
                entity.parentAssetId,
                entity.variant,
                entity.format,
                entity.fileName,
                entity.fileSize,
                entity.width,
                entity.height,
                entity.bucket,
                entity.s3Key,
                entity.createdAt
            ))
            .from(entity)
            .where(entity.originalAssetId.eq(originalAssetId))
            .fetch();
    }

    @Override
    public List<ProcessedFileAssetDto> findByParentAssetId(String parentAssetId) {
        QProcessedFileAssetJpaEntity entity = QProcessedFileAssetJpaEntity.processedFileAssetJpaEntity;

        return queryFactory
            .select(Projections.constructor(ProcessedFileAssetDto.class,
                entity.processedAssetId,
                entity.originalAssetId,
                entity.parentAssetId,
                entity.variant,
                entity.format,
                entity.fileName,
                entity.fileSize,
                entity.width,
                entity.height,
                entity.bucket,
                entity.s3Key,
                entity.createdAt
            ))
            .from(entity)
            .where(entity.parentAssetId.eq(parentAssetId))
            .fetch();
    }
}
```

**DTO Projection** (컨벤션 준수):
```java
/**
 * ProcessedFileAsset 조회 DTO.
 *
 * <p>QueryDSL DTO Projection용</p>
 */
public record ProcessedFileAssetDto(
    String processedAssetId,
    String originalAssetId,
    String parentAssetId,
    ImageVariantType variant,
    ImageFormatType format,
    String fileName,
    Long fileSize,
    Integer width,
    Integer height,
    String bucket,
    String s3Key,
    LocalDateTime createdAt
) {}
```

**Zero-Tolerance 규칙 준수**:
- ✅ QueryDSL DTO Projection 사용 (Entity 직접 반환 금지)
- ✅ N+1 방지
- ✅ Lombok 금지 (record 사용)

---

#### 3.4 Repository: FileAssetQueryRepository 확장

**추가 메서드**:
```java
/**
 * n8n용 파일 목록 조회 (다양한 필터).
 */
Page<FileAssetJpaEntity> findForN8n(
    FileAssetStatus status,
    String contentTypePrefix,    // "image/" 등
    UploadCategory category,
    LocalDateTime fromDate,
    LocalDateTime toDate,
    Long tenantId,
    Long organizationId,
    Pageable pageable
);
```

**Zero-Tolerance 규칙 준수**:
- ✅ Long FK 전략 (관계 어노테이션 금지)
- ✅ QueryDSL DTO Projection (N+1 방지)

---

#### 3.5 JPA Entity: FileAssetStatusHistoryJpaEntity (신규)

**테이블**: `file_asset_status_histories`

**필드**:
| Column | Type | Constraint | Description |
|--------|------|------------|-------------|
| id | BIGINT | PK, Auto Increment | 내부 ID |
| history_id | VARCHAR(36) | Unique, Not Null, Index | UUID |
| file_asset_id | BIGINT | Not Null, Index | FileAsset 내부 ID (Long FK) |
| from_status | VARCHAR(30) | Nullable | 이전 상태 (null = 최초 생성) |
| to_status | VARCHAR(30) | Not Null | 새 상태 |
| message | VARCHAR(500) | Nullable | 상태 변경 메시지 |
| actor | VARCHAR(100) | Not Null | 변경 주체 (SYSTEM, n8n-workflow-123 등) |
| actor_type | VARCHAR(20) | Not Null | SYSTEM, N8N, USER |
| changed_at | DATETIME(6) | Not Null, Index | 변경 시각 |
| duration_millis | BIGINT | Nullable | 이전 상태 체류 시간 (ms) |

**인덱스**:
- `idx_history_file_asset` (file_asset_id) - FileAsset별 이력 조회
- `idx_history_changed_at` (changed_at DESC) - 시간순 정렬
- `idx_history_to_status` (to_status) - 상태별 집계

**Entity 정의** (컨벤션 준수):
```java
/**
 * FileAssetStatusHistory JPA Entity.
 *
 * <p><strong>컨벤션 준수</strong>:
 * <ul>
 *   <li>Lombok 금지 - Pure Java 사용
 *   <li>Long FK 전략 - JPA 관계 어노테이션 금지
 *   <li>정적 팩토리 메서드: of()
 *   <li>protected 기본 생성자 (JPA용)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Entity
@Table(name = "file_asset_status_histories")
public class FileAssetStatusHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "history_id", nullable = false, unique = true)
    private String historyId;

    @Column(name = "file_asset_id", nullable = false)
    private Long fileAssetId;  // Long FK 전략

    @Column(name = "from_status", length = 30)
    @Enumerated(EnumType.STRING)
    private FileAssetStatusType fromStatus;

    @Column(name = "to_status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private FileAssetStatusType toStatus;

    @Column(name = "message", length = 500)
    private String message;

    @Column(name = "actor", nullable = false, length = 100)
    private String actor;

    @Column(name = "actor_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ActorType actorType;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "duration_millis")
    private Long durationMillis;

    /**
     * JPA 전용 기본 생성자.
     */
    protected FileAssetStatusHistoryJpaEntity() {
    }

    private FileAssetStatusHistoryJpaEntity(
        String historyId,
        Long fileAssetId,
        FileAssetStatusType fromStatus,
        FileAssetStatusType toStatus,
        String message,
        String actor,
        ActorType actorType,
        LocalDateTime changedAt,
        Long durationMillis
    ) {
        this.historyId = historyId;
        this.fileAssetId = fileAssetId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.message = message;
        this.actor = actor;
        this.actorType = actorType;
        this.changedAt = changedAt;
        this.durationMillis = durationMillis;
    }

    /**
     * 정적 팩토리 메서드.
     */
    public static FileAssetStatusHistoryJpaEntity of(
        String historyId,
        Long fileAssetId,
        FileAssetStatusType fromStatus,
        FileAssetStatusType toStatus,
        String message,
        String actor,
        ActorType actorType,
        LocalDateTime changedAt,
        Long durationMillis
    ) {
        return new FileAssetStatusHistoryJpaEntity(
            historyId, fileAssetId, fromStatus, toStatus,
            message, actor, actorType, changedAt, durationMillis
        );
    }

    // Getter 메서드들 (Lombok 금지)
    public Long getId() { return id; }
    public String getHistoryId() { return historyId; }
    public Long getFileAssetId() { return fileAssetId; }
    public FileAssetStatusType getFromStatus() { return fromStatus; }
    public FileAssetStatusType getToStatus() { return toStatus; }
    public String getMessage() { return message; }
    public String getActor() { return actor; }
    public ActorType getActorType() { return actorType; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public Long getDurationMillis() { return durationMillis; }
}

/**
 * 변경 주체 타입.
 */
public enum ActorType {
    SYSTEM,   // 내부 시스템
    N8N,      // n8n 워크플로우
    USER      // 사용자
}
```

---

#### 3.6 JPA Entity: FileProcessingOutboxJpaEntity (신규)

**테이블**: `file_processing_outbox`

**필드**:
| Column | Type | Constraint | Description |
|--------|------|------------|-------------|
| id | BIGINT | PK, Auto Increment | 내부 ID |
| outbox_id | VARCHAR(36) | Unique, Not Null, Index | UUID |
| file_asset_id | BIGINT | Not Null, Index | FileAsset 내부 ID (Long FK) |
| event_type | VARCHAR(30) | Not Null | PROCESS_REQUESTED, STATUS_CHANGED 등 |
| aggregate_type | VARCHAR(50) | Not Null | FileAsset |
| payload | TEXT | Not Null | JSON payload |
| status | VARCHAR(20) | Not Null, Index | PENDING, SENT, FAILED |
| retry_count | INT | Not Null, Default 0 | 재시도 횟수 |
| last_error | VARCHAR(1000) | Nullable | 마지막 에러 메시지 |
| created_at | DATETIME(6) | Not Null, Index | 생성 시각 |
| processed_at | DATETIME(6) | Nullable | 처리 완료 시각 |

**인덱스**:
- `idx_outbox_status` (status) - PENDING 이벤트 조회
- `idx_outbox_status_retry` (status, retry_count) - 재시도 가능 이벤트 조회
- `idx_outbox_created_at` (created_at) - 생성순 정렬
- `idx_outbox_file_asset` (file_asset_id) - FileAsset별 조회

**Entity 정의** (컨벤션 준수):
```java
/**
 * FileProcessingOutbox JPA Entity.
 *
 * <p><strong>컨벤션 준수</strong>:
 * <ul>
 *   <li>Lombok 금지 - Pure Java 사용
 *   <li>Long FK 전략 - JPA 관계 어노테이션 금지
 *   <li>정적 팩토리 메서드: of()
 *   <li>protected 기본 생성자 (JPA용)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Entity
@Table(name = "file_processing_outbox")
public class FileProcessingOutboxJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "outbox_id", nullable = false, unique = true)
    private String outboxId;

    @Column(name = "file_asset_id", nullable = false)
    private Long fileAssetId;  // Long FK 전략

    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OutboxStatusType status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * JPA 전용 기본 생성자.
     */
    protected FileProcessingOutboxJpaEntity() {
    }

    private FileProcessingOutboxJpaEntity(
        String outboxId,
        Long fileAssetId,
        String eventType,
        String aggregateType,
        String payload,
        OutboxStatusType status,
        int retryCount,
        String lastError,
        LocalDateTime createdAt,
        LocalDateTime processedAt
    ) {
        this.outboxId = outboxId;
        this.fileAssetId = fileAssetId;
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.payload = payload;
        this.status = status;
        this.retryCount = retryCount;
        this.lastError = lastError;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    /**
     * 정적 팩토리 메서드.
     */
    public static FileProcessingOutboxJpaEntity of(
        String outboxId,
        Long fileAssetId,
        String eventType,
        String aggregateType,
        String payload,
        OutboxStatusType status,
        int retryCount,
        String lastError,
        LocalDateTime createdAt,
        LocalDateTime processedAt
    ) {
        return new FileProcessingOutboxJpaEntity(
            outboxId, fileAssetId, eventType, aggregateType,
            payload, status, retryCount, lastError, createdAt, processedAt
        );
    }

    // Getter 메서드들 (Lombok 금지)
    public Long getId() { return id; }
    public String getOutboxId() { return outboxId; }
    public Long getFileAssetId() { return fileAssetId; }
    public String getEventType() { return eventType; }
    public String getAggregateType() { return aggregateType; }
    public String getPayload() { return payload; }
    public OutboxStatusType getStatus() { return status; }
    public int getRetryCount() { return retryCount; }
    public String getLastError() { return lastError; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }

    // 상태 변경 메서드 (Entity 내부 변경용)
    public void markAsSent(LocalDateTime processedAt) {
        this.status = OutboxStatusType.SENT;
        this.processedAt = processedAt;
    }

    public void markAsFailed(String errorMessage) {
        this.status = OutboxStatusType.FAILED;
        this.lastError = errorMessage;
        this.retryCount++;
    }
}

/**
 * Outbox 상태 타입.
 */
public enum OutboxStatusType {
    PENDING,  // 전송 대기
    SENT,     // 전송 완료
    FAILED    // 전송 실패
}
```

---

#### 3.7 Repository: FileAssetStatusHistoryRepository (신규)

**JPA Repository**:
```java
/**
 * FileAssetStatusHistory JPA Repository.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface FileAssetStatusHistoryJpaRepository
    extends JpaRepository<FileAssetStatusHistoryJpaEntity, Long> {

    /**
     * FileAsset별 이력 조회 (시간순).
     */
    List<FileAssetStatusHistoryJpaEntity> findByFileAssetIdOrderByChangedAtAsc(Long fileAssetId);

    /**
     * FileAsset별 최신 이력 조회.
     */
    Optional<FileAssetStatusHistoryJpaEntity> findTopByFileAssetIdOrderByChangedAtDesc(Long fileAssetId);
}
```

**QueryDSL Repository**:
```java
/**
 * FileAssetStatusHistory QueryDSL Repository.
 *
 * @author development-team
 * @since 1.0.0
 */
@Repository
public class FileAssetStatusHistoryQueryRepository {

    private final JPAQueryFactory queryFactory;
    private static final QFileAssetStatusHistoryJpaEntity entity =
        QFileAssetStatusHistoryJpaEntity.fileAssetStatusHistoryJpaEntity;

    public FileAssetStatusHistoryQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * SLA 초과 이력 조회 (모니터링용).
     *
     * @param slaMillis SLA 기준 밀리초
     * @param fromDate 조회 시작일
     * @param limit 조회 개수 제한
     * @return SLA 초과 이력 DTO 목록
     */
    public List<StatusHistoryDto> findExceedingSla(
        long slaMillis,
        LocalDateTime fromDate,
        int limit
    ) {
        return queryFactory
            .select(Projections.constructor(StatusHistoryDto.class,
                entity.historyId,
                entity.fileAssetId,
                entity.fromStatus,
                entity.toStatus,
                entity.message,
                entity.actor,
                entity.actorType,
                entity.changedAt,
                entity.durationMillis
            ))
            .from(entity)
            .where(
                entity.durationMillis.gt(slaMillis),
                entity.changedAt.goe(fromDate)
            )
            .orderBy(entity.durationMillis.desc())
            .limit(limit)
            .fetch();
    }
}

/**
 * StatusHistory 조회 DTO.
 */
public record StatusHistoryDto(
    String historyId,
    Long fileAssetId,
    FileAssetStatusType fromStatus,
    FileAssetStatusType toStatus,
    String message,
    String actor,
    ActorType actorType,
    LocalDateTime changedAt,
    Long durationMillis
) {}
```

---

#### 3.8 Repository: FileProcessingOutboxRepository (신규)

**JPA Repository**:
```java
/**
 * FileProcessingOutbox JPA Repository.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface FileProcessingOutboxJpaRepository
    extends JpaRepository<FileProcessingOutboxJpaEntity, Long> {

    /**
     * PENDING 상태 이벤트 조회.
     */
    List<FileProcessingOutboxJpaEntity> findByStatusOrderByCreatedAtAsc(
        OutboxStatusType status,
        Pageable pageable
    );
}
```

**QueryDSL Repository**:
```java
/**
 * FileProcessingOutbox QueryDSL Repository.
 *
 * @author development-team
 * @since 1.0.0
 */
@Repository
public class FileProcessingOutboxQueryRepository {

    private final JPAQueryFactory queryFactory;
    private static final QFileProcessingOutboxJpaEntity entity =
        QFileProcessingOutboxJpaEntity.fileProcessingOutboxJpaEntity;

    public FileProcessingOutboxQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * PENDING 상태 Outbox 이벤트 조회.
     *
     * @param limit 조회 개수 제한
     * @return PENDING 상태 이벤트 목록
     */
    public List<FileProcessingOutboxJpaEntity> findPendingEvents(int limit) {
        return queryFactory
            .selectFrom(entity)
            .where(entity.status.eq(OutboxStatusType.PENDING))
            .orderBy(entity.createdAt.asc())
            .limit(limit)
            .fetch();
    }

    /**
     * 재시도 가능한 FAILED 이벤트 조회.
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param limit 조회 개수 제한
     * @return 재시도 가능한 FAILED 이벤트 목록
     */
    public List<FileProcessingOutboxJpaEntity> findRetryableFailedEvents(
        int maxRetryCount,
        int limit
    ) {
        return queryFactory
            .selectFrom(entity)
            .where(
                entity.status.eq(OutboxStatusType.FAILED),
                entity.retryCount.lt(maxRetryCount)
            )
            .orderBy(entity.createdAt.asc())
            .limit(limit)
            .fetch();
    }
}
```

---

#### 3.9 Mapper: StatusHistory & Outbox Mapper (신규)

**FileAssetStatusHistoryMapper**:
```java
/**
 * FileAssetStatusHistory Entity ↔ Domain Mapper.
 *
 * <p><strong>컨벤션 준수</strong>:
 * <ul>
 *   <li>Lombok 금지
 *   <li>정적 메서드만 사용
 *   <li>DTO가 아닌 Domain 변환용
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public final class FileAssetStatusHistoryMapper {

    private FileAssetStatusHistoryMapper() {
        // 유틸리티 클래스
    }

    /**
     * Domain → Entity 변환.
     */
    public static FileAssetStatusHistoryJpaEntity toEntity(FileAssetStatusHistory domain) {
        return FileAssetStatusHistoryJpaEntity.of(
            domain.getIdValue(),
            domain.getFileAssetId(),
            domain.getFromStatus() != null
                ? FileAssetStatusType.valueOf(domain.getFromStatus().name())
                : null,
            FileAssetStatusType.valueOf(domain.getToStatus().name()),
            domain.getMessage(),
            domain.getActor(),
            ActorType.valueOf(domain.getActorType()),
            domain.getChangedAt(),
            domain.getDurationMillis()
        );
    }

    /**
     * Entity → Domain 변환.
     */
    public static FileAssetStatusHistory toDomain(FileAssetStatusHistoryJpaEntity entity) {
        return FileAssetStatusHistory.reconstitute(
            FileAssetStatusHistoryId.of(entity.getHistoryId()),
            entity.getFileAssetId(),
            entity.getFromStatus() != null
                ? FileAssetStatus.valueOf(entity.getFromStatus().name())
                : null,
            FileAssetStatus.valueOf(entity.getToStatus().name()),
            entity.getMessage(),
            entity.getActor(),
            entity.getActorType().name(),
            entity.getChangedAt(),
            entity.getDurationMillis()
        );
    }
}
```

**FileProcessingOutboxMapper**:
```java
/**
 * FileProcessingOutbox Entity ↔ Domain Mapper.
 *
 * @author development-team
 * @since 1.0.0
 */
public final class FileProcessingOutboxMapper {

    private FileProcessingOutboxMapper() {
        // 유틸리티 클래스
    }

    /**
     * Domain → Entity 변환.
     */
    public static FileProcessingOutboxJpaEntity toEntity(FileProcessingOutbox domain) {
        return FileProcessingOutboxJpaEntity.of(
            domain.getIdValue(),
            domain.getFileAssetId(),
            domain.getEventType(),
            domain.getAggregateType(),
            domain.getPayload(),
            OutboxStatusType.valueOf(domain.getStatus().name()),
            domain.getRetryCount(),
            domain.getLastError(),
            domain.getCreatedAt(),
            domain.getProcessedAt()
        );
    }

    /**
     * Entity → Domain 변환.
     */
    public static FileProcessingOutbox toDomain(FileProcessingOutboxJpaEntity entity) {
        return FileProcessingOutbox.reconstitute(
            FileProcessingOutboxId.of(entity.getOutboxId()),
            entity.getFileAssetId(),
            entity.getEventType(),
            entity.getAggregateType(),
            entity.getPayload(),
            OutboxStatus.valueOf(entity.getStatus().name()),
            entity.getRetryCount(),
            entity.getLastError(),
            entity.getCreatedAt(),
            entity.getProcessedAt()
        );
    }
}
```

---

#### 3.10 Adapter: StatusHistory & Outbox Adapter (신규)

**FileAssetStatusHistoryPersistenceAdapter**:
```java
/**
 * FileAssetStatusHistory Persistence Adapter.
 *
 * @author development-team
 * @since 1.0.0
 */
@Repository
public class FileAssetStatusHistoryPersistenceAdapter
    implements FileAssetStatusHistoryPersistencePort {

    private final FileAssetStatusHistoryJpaRepository jpaRepository;

    public FileAssetStatusHistoryPersistenceAdapter(
        FileAssetStatusHistoryJpaRepository jpaRepository
    ) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public FileAssetStatusHistory save(FileAssetStatusHistory history) {
        FileAssetStatusHistoryJpaEntity entity =
            FileAssetStatusHistoryMapper.toEntity(history);
        FileAssetStatusHistoryJpaEntity saved = jpaRepository.save(entity);
        return FileAssetStatusHistoryMapper.toDomain(saved);
    }
}
```

**FileAssetStatusHistoryQueryAdapter**:
```java
/**
 * FileAssetStatusHistory Query Adapter.
 *
 * @author development-team
 * @since 1.0.0
 */
@Repository
public class FileAssetStatusHistoryQueryAdapter
    implements FileAssetStatusHistoryQueryPort {

    private final FileAssetStatusHistoryJpaRepository jpaRepository;
    private final FileAssetStatusHistoryQueryRepository queryRepository;

    public FileAssetStatusHistoryQueryAdapter(
        FileAssetStatusHistoryJpaRepository jpaRepository,
        FileAssetStatusHistoryQueryRepository queryRepository
    ) {
        this.jpaRepository = jpaRepository;
        this.queryRepository = queryRepository;
    }

    @Override
    public List<FileAssetStatusHistory> findByFileAssetId(Long fileAssetId) {
        return jpaRepository.findByFileAssetIdOrderByChangedAtAsc(fileAssetId)
            .stream()
            .map(FileAssetStatusHistoryMapper::toDomain)
            .toList();
    }

    @Override
    public Optional<FileAssetStatusHistory> findLatestByFileAssetId(Long fileAssetId) {
        return jpaRepository.findTopByFileAssetIdOrderByChangedAtDesc(fileAssetId)
            .map(FileAssetStatusHistoryMapper::toDomain);
    }

    @Override
    public List<FileAssetStatusHistory> findExceedingSla(
        long slaMillis,
        LocalDateTime fromDate,
        int limit
    ) {
        // QueryDSL DTO Projection 사용
        return queryRepository.findExceedingSla(slaMillis, fromDate, limit)
            .stream()
            .map(dto -> FileAssetStatusHistory.reconstitute(
                FileAssetStatusHistoryId.of(dto.historyId()),
                dto.fileAssetId(),
                dto.fromStatus() != null
                    ? FileAssetStatus.valueOf(dto.fromStatus().name())
                    : null,
                FileAssetStatus.valueOf(dto.toStatus().name()),
                dto.message(),
                dto.actor(),
                dto.actorType().name(),
                dto.changedAt(),
                dto.durationMillis()
            ))
            .toList();
    }
}
```

**FileProcessingOutboxPersistenceAdapter**:
```java
/**
 * FileProcessingOutbox Persistence Adapter.
 *
 * @author development-team
 * @since 1.0.0
 */
@Repository
public class FileProcessingOutboxPersistenceAdapter
    implements FileProcessingOutboxPersistencePort {

    private final FileProcessingOutboxJpaRepository jpaRepository;

    public FileProcessingOutboxPersistenceAdapter(
        FileProcessingOutboxJpaRepository jpaRepository
    ) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public FileProcessingOutbox save(FileProcessingOutbox outbox) {
        FileProcessingOutboxJpaEntity entity =
            FileProcessingOutboxMapper.toEntity(outbox);
        FileProcessingOutboxJpaEntity saved = jpaRepository.save(entity);
        return FileProcessingOutboxMapper.toDomain(saved);
    }

    @Override
    public List<FileProcessingOutbox> saveAll(List<FileProcessingOutbox> outboxList) {
        List<FileProcessingOutboxJpaEntity> entities = outboxList.stream()
            .map(FileProcessingOutboxMapper::toEntity)
            .toList();
        return jpaRepository.saveAll(entities).stream()
            .map(FileProcessingOutboxMapper::toDomain)
            .toList();
    }
}
```

**FileProcessingOutboxQueryAdapter**:
```java
/**
 * FileProcessingOutbox Query Adapter.
 *
 * @author development-team
 * @since 1.0.0
 */
@Repository
public class FileProcessingOutboxQueryAdapter
    implements FileProcessingOutboxQueryPort {

    private final FileProcessingOutboxQueryRepository queryRepository;

    public FileProcessingOutboxQueryAdapter(
        FileProcessingOutboxQueryRepository queryRepository
    ) {
        this.queryRepository = queryRepository;
    }

    @Override
    public List<FileProcessingOutbox> findPendingEvents(int limit) {
        return queryRepository.findPendingEvents(limit).stream()
            .map(FileProcessingOutboxMapper::toDomain)
            .toList();
    }

    @Override
    public List<FileProcessingOutbox> findRetryableFailedEvents(
        int maxRetryCount,
        int limit
    ) {
        return queryRepository.findRetryableFailedEvents(maxRetryCount, limit).stream()
            .map(FileProcessingOutboxMapper::toDomain)
            .toList();
    }
}
```

**Zero-Tolerance 규칙 준수**:
- ✅ Lombok 금지 - Pure Java 사용
- ✅ Long FK 전략 - JPA 관계 어노테이션 금지
- ✅ 정적 팩토리 메서드: of()
- ✅ QueryDSL DTO Projection 사용
- ✅ Mapper 패턴으로 Entity ↔ Domain 분리

---

### 4. Infrastructure Layer

#### 4.1 ImageProcessor (ImageProcessingPort 구현)

**라이브러리**: Java ImageIO + Thumbnailator (또는 imgscalr)

```java
@Component
public class ThumbnailatorImageProcessor implements ImageProcessingPort {

    @Override
    public ImageProcessingResult resize(byte[] sourceBytes, ImageVariant variant, ImageFormat format) {
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(sourceBytes));

        int targetWidth, targetHeight;
        if (original.getWidth() > original.getHeight()) {
            // 가로가 긴 이미지
            targetWidth = variant.getMaxDimension();
            targetHeight = (int) (original.getHeight() * ((double) targetWidth / original.getWidth()));
        } else {
            // 세로가 긴 이미지
            targetHeight = variant.getMaxDimension();
            targetWidth = (int) (original.getWidth() * ((double) targetHeight / original.getHeight()));
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(original)
            .size(targetWidth, targetHeight)
            .outputFormat(format.getExtension())
            .toOutputStream(out);

        return new ImageProcessingResult(out.toByteArray(), targetWidth, targetHeight, out.size());
    }
}
```

**WebP 지원**:
- `webp-imageio` 라이브러리 추가 필요
- 또는 `libwebp` native 바인딩

---

#### 4.2 HtmlImageExtractor (HtmlProcessingPort 구현)

**라이브러리**: Jsoup

```java
@Component
public class JsoupHtmlImageExtractor implements HtmlProcessingPort {

    @Override
    public List<ExtractedImage> extractImages(String htmlContent) {
        Document doc = Jsoup.parse(htmlContent);
        List<ExtractedImage> images = new ArrayList<>();

        // <img src="...">
        doc.select("img[src]").forEach(img -> {
            images.add(new ExtractedImage(img.attr("src"), ImageSourceType.IMG_SRC));
        });

        // style="background: url(...)"
        doc.select("[style*=background]").forEach(el -> {
            String style = el.attr("style");
            extractUrlsFromStyle(style).forEach(url -> {
                images.add(new ExtractedImage(url, ImageSourceType.INLINE_STYLE));
            });
        });

        return images;
    }

    @Override
    public String replaceImageUrls(String htmlContent, Map<String, String> urlMappings) {
        Document doc = Jsoup.parse(htmlContent);

        doc.select("img[src]").forEach(img -> {
            String oldUrl = img.attr("src");
            if (urlMappings.containsKey(oldUrl)) {
                img.attr("src", urlMappings.get(oldUrl));
            }
        });

        // style 속성 내 URL도 교체
        // ...

        return doc.html();
    }
}
```

---

#### 4.3 ExternalImageDownloader (신규)

**외부 URL 이미지 다운로드**:
```java
@Component
public class ExternalImageDownloader {

    private final RestTemplate restTemplate;

    /**
     * 외부 URL에서 이미지 다운로드.
     *
     * @param imageUrl 이미지 URL
     * @return 이미지 바이트
     * @throws ImageDownloadException 다운로드 실패 시
     */
    public byte[] download(String imageUrl) {
        ResponseEntity<byte[]> response = restTemplate.getForEntity(imageUrl, byte[].class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        }
        throw new ImageDownloadException("이미지 다운로드 실패: " + imageUrl);
    }
}
```

**Timeout 설정**:
- Connection Timeout: 5초
- Read Timeout: 30초

---

#### 4.4 SQS Listener (ECS Worker)

**메시지 수신**:
```java
@Component
@SqsListener(queues = "${sqs.file-processing-queue}")
public class FileProcessingQueueListener {

    private final ProcessFileAssetUseCase processFileAssetUseCase;

    @SqsHandler
    public void handleMessage(FileProcessingMessage message) {
        processFileAssetUseCase.execute(
            new ProcessFileAssetCommand(message.fileAssetId())
        );
    }
}
```

**Dead Letter Queue 설정**:
- MaxReceiveCount: 3 (3회 실패 시 DLQ)
- DLQ 모니터링: CloudWatch Alarm

---

### 5. REST API Layer

#### 5.1 API 엔드포인트

| Method | Path | Description | Request | Response | Status |
|--------|------|-------------|---------|----------|--------|
| POST | /api/v1/file-assets/{id}/process | 수동 가공 트리거 | - | ProcessFileAssetResponse | 202 Accepted |
| PATCH | /api/v1/file-assets/{id}/status | 상태 업데이트 (n8n용) | UpdateStatusRequest | UpdateStatusResponse | 200 OK |
| GET | /api/v1/file-assets | 파일 목록 조회 (n8n용) | Query Params | PageResponse | 200 OK |
| GET | /api/v1/file-assets/{id} | 파일 상세 조회 | - | FileAssetDetailResponse | 200 OK |
| GET | /api/v1/file-assets/{id}/download | 다운로드 URL 조회 | Query: variant, format | DownloadUrlResponse | 200 OK |
| GET | /api/v1/file-assets/{id}/processed | 가공된 파일 목록 | - | List<ProcessedFileResponse> | 200 OK |

---

#### 5.2 Request/Response DTO

**UpdateStatusRequest**:
```java
public record UpdateStatusRequest(
    @NotNull FileAssetStatus status,
    String message
) {}
```

**FileAssetDetailResponse**:
```java
public record FileAssetDetailResponse(
    String id,
    String sessionId,
    String fileName,
    long fileSize,
    String contentType,
    String category,
    FileAssetStatus status,
    String statusMessage,
    String bucket,
    String s3Key,
    String downloadUrl,
    Long userId,
    Long organizationId,
    Long tenantId,
    LocalDateTime createdAt,
    LocalDateTime processedAt,
    List<ProcessedFileResponse> processedFiles
) {}
```

**ProcessedFileResponse**:
```java
public record ProcessedFileResponse(
    String id,
    String variant,       // LARGE, MEDIUM, THUMBNAIL
    String format,        // WEBP, JPEG
    String fileName,
    long fileSize,
    Integer width,
    Integer height,
    String downloadUrl
) {}
```

**DownloadUrlResponse**:
```java
public record DownloadUrlResponse(
    String fileAssetId,
    String variant,
    String format,
    String downloadUrl,
    LocalDateTime expiresAt
) {}
```

---

#### 5.3 Query Parameters (목록 조회)

```
GET /api/v1/file-assets?status=RESIZED&category=PRODUCT_IMAGE&from=2025-01-01&to=2025-12-31&page=0&size=20
```

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| status | String | N | 상태 필터 (RESIZED, N8N_PROCESSING 등) |
| contentType | String | N | MIME 타입 prefix (image/, text/html) |
| category | String | N | 카테고리 (PRODUCT_IMAGE, HTML, EXCEL) |
| from | DateTime | N | 생성일 시작 (ISO 8601) |
| to | DateTime | N | 생성일 종료 (ISO 8601) |
| page | Integer | N | 페이지 번호 (기본: 0) |
| size | Integer | N | 페이지 크기 (기본: 20, 최대: 100) |

---

#### 5.4 Error Handling

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| FILE_ASSET_NOT_FOUND | 404 | 파일 없음 |
| INVALID_STATUS_TRANSITION | 400 | 잘못된 상태 전환 |
| PROCESSING_IN_PROGRESS | 409 | 이미 가공 중 |
| IMAGE_PROCESSING_FAILED | 500 | 이미지 가공 실패 |
| HTML_PARSING_FAILED | 500 | HTML 파싱 실패 |
| EXTERNAL_IMAGE_DOWNLOAD_FAILED | 502 | 외부 이미지 다운로드 실패 |

---

## ⚠️ 제약사항

### 비기능 요구사항

**성능**:
- 이미지 리사이징 (5MB 이하): < 5초 (P95)
- HTML 처리 (이미지 20개 이하): < 30초 (P95)
- API 응답 시간: < 200ms (P95)
- 동시 처리: ECS Worker Auto Scaling (1~5대)

**보안**:
- JWT 인증 필수
- 테넌트/조직 기반 권한 검증
- Presigned URL 15분 만료
- 외부 이미지 다운로드 시 Whitelist 도메인 검증 (선택)

**확장성**:
- SQS 기반 분산 처리
- ECS Auto Scaling (SQS 메시지 수 기반)
- S3 무제한 저장

**가용성**:
- Worker 장애 시: SQS 재시도 (3회)
- 3회 실패 시: DLQ + FAILED 상태

---

## 🧪 테스트 전략

> **⚠️ 컨벤션 준수**: MockMvc 사용 금지, TestRestTemplate 필수

### Unit Test

**Domain**:
- ContentType 확장 (isHtml, isExcel)
- UploadCategory 확장 (isHtml, requiresImageProcessing)
- ImageVariant 파일명 생성
- ImageFormat 폴백 로직
- ProcessedFileAsset 생성 (forNew, forHtmlExtractedImage, reconstitute)

**Application**:
- ProcessFileAssetUseCase (Mock ImageProcessingPort, HtmlProcessingPort)
- UpdateFileAssetStatusUseCase (상태 전환 검증)
- TransactionManager 단위 테스트

### Integration Test

**Persistence**:
- ProcessedFileAssetJpaEntity CRUD (TestContainers MySQL)
- FileAssetQueryRepository n8n 조회 쿼리

**Infrastructure**:
- ThumbnailatorImageProcessor 리사이징 (실제 이미지)
- JsoupHtmlImageExtractor 파싱 (테스트 HTML)

**REST API** (TestRestTemplate 사용):
```java
/**
 * FileAsset API 통합 테스트.
 *
 * <p><strong>컨벤션 준수</strong>: MockMvc 금지, TestRestTemplate 필수</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FileAssetControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void 파일_가공_트리거_성공() {
        // given
        String fileAssetId = "test-file-asset-id";

        // when
        ResponseEntity<ProcessFileAssetResponse> response = restTemplate.postForEntity(
            "/api/v1/file-assets/{id}/process",
            null,
            ProcessFileAssetResponse.class,
            fileAssetId
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(FileAssetStatus.PROCESSING);
    }

    @Test
    void n8n_파일_목록_조회_성공() {
        // given
        String url = "/api/v1/file-assets?status=RESIZED&category=PRODUCT_IMAGE&page=0&size=20";

        // when
        ResponseEntity<PageResponse<FileAssetForN8nResponse>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<PageResponse<FileAssetForN8nResponse>>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}
```

**테스트 컨벤션**:
- ❌ MockMvc 금지 - 실제 서블릿 컨테이너 동작 검증 불가
- ✅ TestRestTemplate 사용 - 실제 HTTP 요청/응답 검증
- ✅ @SpringBootTest(webEnvironment = RANDOM_PORT) 필수
- ✅ TestContainers로 실제 MySQL 사용

### E2E Test

- 이미지 업로드 → SQS → 리사이징 → 상태 확인
- HTML 업로드 → 이미지 추출 → 리사이징 → URL 교체 확인
- n8n API 호출 시뮬레이션

**E2E 테스트 구조** (TestRestTemplate 사용):
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class FileProcessingE2ETest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(...)
        .withServices(LocalStackContainer.Service.S3, LocalStackContainer.Service.SQS);

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void 이미지_업로드_가공_전체_흐름() {
        // 1. 이미지 업로드
        // 2. SQS 메시지 발행 확인
        // 3. 리사이징 완료 대기
        // 4. 상태 RESIZED 확인
        // 5. 가공된 파일 목록 확인
    }
}

---

## 🚀 개발 계획

### Phase 1: Domain Layer (예상: 2일)
- [ ] ContentType 확장 (HTML 타입 추가)
- [ ] UploadCategory 확장 (HTML 카테고리 추가)
- [ ] FileAssetStatus 확장 (RESIZED, N8N_* 상태 추가)
- [ ] ImageVariant, ImageFormat Value Object
- [ ] ProcessedFileAsset Aggregate
- [ ] ImageProcessingPolicy Domain Service
- [ ] Domain Unit Test

### Phase 2: Application Layer (예상: 4일)
- [ ] ImageProcessingPort, HtmlProcessingPort 정의
- [ ] ProcessFileAssetUseCase 구현
- [ ] UpdateFileAssetStatusUseCase 구현
- [ ] ListFileAssetsForN8nUseCase 구현
- [ ] FileProcessingMessageHandler (SQS)
- [ ] Application Unit Test

### Phase 3: Persistence Layer (예상: 2일)
- [ ] ProcessedFileAssetJpaEntity 구현
- [ ] FileAssetJpaEntity 필드 추가
- [ ] ProcessedFileAssetQueryRepository 구현
- [ ] FileAssetQueryRepository 확장
- [ ] Flyway Migration 작성
- [ ] Integration Test

### Phase 4: Infrastructure Layer (예상: 3일)
- [ ] ThumbnailatorImageProcessor 구현
- [ ] JsoupHtmlImageExtractor 구현
- [ ] ExternalImageDownloader 구현
- [ ] SQS Listener 구현
- [ ] WebP 라이브러리 통합
- [ ] Infrastructure Test

### Phase 5: REST API Layer (예상: 2일)
- [ ] FileAssetController 확장
- [ ] Request/Response DTO
- [ ] Error Handling
- [ ] API Documentation (REST Docs)
- [ ] REST API Test

### Phase 6: Worker 배포 (예상: 1일)
- [ ] ECS Task Definition 작성
- [ ] SQS Queue 생성
- [ ] DLQ 설정
- [ ] Auto Scaling 설정
- [ ] CloudWatch Alarm 설정

---

## 📚 참고 문서

- [Domain Layer 규칙](../../docs/coding_convention/02-domain-layer/)
- [Application Layer 규칙](../../docs/coding_convention/03-application-layer/)
- [Persistence Layer 규칙](../../docs/coding_convention/04-persistence-layer/)
- [REST API Layer 규칙](../../docs/coding_convention/01-adapter-in-layer/rest-api/)
- [Thumbnailator GitHub](https://github.com/coobird/thumbnailator)
- [webp-imageio GitHub](https://github.com/nicoulaj/webp-imageio)
- [Jsoup Documentation](https://jsoup.org/)

---

## 🔍 추가 고려사항

### 1. 이미지 리사이징 품질 설정 (미결정)
- **JPEG 품질**: 85% (기본) vs 90%
- **WebP 품질**: 80% (기본) vs 85%
- 용량 vs 품질 트레이드오프

### 2. 외부 이미지 다운로드 Whitelist (미결정)
- **옵션1**: 모든 도메인 허용 (보안 위험)
- **옵션2**: Whitelist 도메인만 허용
- **옵션3**: 특정 도메인 Blacklist

### 3. 대용량 이미지 처리 (미결정)
- **현재**: 5MB 이상 이미지 → 타임아웃 가능
- **대안1**: 이미지 크기 제한 (10MB 등)
- **대안2**: Lambda로 대용량 이미지 별도 처리

### 4. HTML 이미지 Base64 처리 (미결정)
- **현재**: Base64 인라인 이미지 미지원
- **추후**: Base64 디코딩 → 리사이징 → URL 교체

### 5. 가공 진행률 추적 (미결정)
- HTML 내 이미지 20개 → 진행률 5%, 10%, ...
- WebSocket or Polling 기반 진행률 API

---

**다음 단계**:
1. PRD 검토 및 수정
2. `/jira-task docs/prd/file-asset-processing.md` - Jira 티켓 생성
3. Layer별 TDD 사이클 시작 (`/kb/domain/go`, `/kb/application/go` 등)
