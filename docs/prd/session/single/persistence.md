# Persistence Layer - Single Presigned URL Upload

**Bounded Context**: `session/single`
**Layer**: Persistence
**작성일**: 2025-11-18

---

## 📋 목차

1. [JPA Entities](#jpa-entities)
2. [Flyway Migrations](#flyway-migrations)
3. [Adapters](#adapters)
4. [Repositories](#repositories)

---

## JPA Entities

### 1. FileJpaEntity

**위치**: `persistence/mysql/src/main/java/com/ryuqq/fileflow/persistence/mysql/entity/FileJpaEntity.java`

```java
/**
 * File JPA Entity
 * <p>
 * - Zero-Tolerance: Lombok 금지, Long FK 전략
 * - BaseAuditEntity 상속 (createdAt, updatedAt)
 * </p>
 */
@Entity
@Table(name = "files")
public class FileJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id", nullable = false, unique = true, length = 36)
    private String fileId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @Column(name = "s3_bucket", nullable = false, length = 100)
    private String s3Bucket;

    @Column(name = "uploader_id", nullable = false)
    private Long uploaderId;

    @Column(name = "uploader_type", nullable = false, length = 20)
    private String uploaderType;

    @Column(name = "uploader_slug", nullable = false, length = 100)
    private String uploaderSlug;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    // Getter, Setter (Plain Java)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public String getS3Key() { return s3Key; }
    public void setS3Key(String s3Key) { this.s3Key = s3Key; }
    public String getS3Bucket() { return s3Bucket; }
    public void setS3Bucket(String s3Bucket) { this.s3Bucket = s3Bucket; }
    public Long getUploaderId() { return uploaderId; }
    public void setUploaderId(Long uploaderId) { this.uploaderId = uploaderId; }
    public String getUploaderType() { return uploaderType; }
    public void setUploaderType(String uploaderType) { this.uploaderType = uploaderType; }
    public String getUploaderSlug() { return uploaderSlug; }
    public void setUploaderSlug(String uploaderSlug) { this.uploaderSlug = uploaderSlug; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
```

---

### 2. UploadSessionJpaEntity

**위치**: `persistence/mysql/src/main/java/com/ryuqq/fileflow/persistence/mysql/entity/UploadSessionJpaEntity.java`

```java
/**
 * UploadSession JPA Entity
 * <p>
 * - Zero-Tolerance: Lombok 금지
 * - BaseAuditEntity 상속
 * </p>
 */
@Entity
@Table(name = "upload_sessions")
public class UploadSessionJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true, length = 36)
    private String sessionId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "upload_type", nullable = false, length = 20)
    private String uploadType;

    @Column(name = "presigned_url", columnDefinition = "TEXT")
    private String presignedUrl;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    // Getter, Setter (Plain Java)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public String getUploadType() { return uploadType; }
    public void setUploadType(String uploadType) { this.uploadType = uploadType; }
    public String getPresignedUrl() { return presignedUrl; }
    public void setPresignedUrl(String presignedUrl) { this.presignedUrl = presignedUrl; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
```

---

## Flyway Migrations

### V1__create_files_table.sql

**위치**: `persistence/mysql/src/main/resources/db/migration/V1__create_files_table.sql`

```sql
CREATE TABLE files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id VARCHAR(36) NOT NULL UNIQUE,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL CHECK (file_size > 0),
    mime_type VARCHAR(100) NOT NULL,
    s3_key VARCHAR(500) NOT NULL,
    s3_bucket VARCHAR(100) NOT NULL,
    uploader_id BIGINT NOT NULL,
    uploader_type VARCHAR(20) NOT NULL,
    uploader_slug VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    tenant_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    INDEX idx_file_id (file_id),
    INDEX idx_uploader (uploader_id, uploader_type),
    INDEX idx_tenant_created (tenant_id, created_at DESC),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

### V2__create_upload_sessions_table.sql

**위치**: `persistence/mysql/src/main/resources/db/migration/V2__create_upload_sessions_table.sql`

```sql
CREATE TABLE upload_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL UNIQUE,
    tenant_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    upload_type VARCHAR(20) NOT NULL,
    presigned_url TEXT,
    expires_at DATETIME(6) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    INDEX idx_session_id (session_id),
    INDEX idx_status_expires (status, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## Adapters

### 1. S3ClientAdapter

**위치**: `persistence/s3/src/main/java/com/ryuqq/fileflow/persistence/s3/adapter/S3ClientAdapter.java`

```java
/**
 * S3 Client Adapter
 * <p>
 * - AWS S3 SDK 래핑
 * - Presigned URL 생성
 * </p>
 */
@Component
public class S3ClientAdapter implements S3ClientPort {

    private final S3Presigner presigner;

    public S3ClientAdapter(S3Presigner presigner) {
        this.presigner = presigner;
    }

    @Override
    public PresignedUrl generatePresignedPutUrl(
        S3Bucket bucket,
        S3Key key,
        MimeType mimeType,
        Duration expiration
    ) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(bucket.value())
            .key(key.value())
            .contentType(mimeType.value())
            .build();

        PresignedPutObjectRequest presignedRequest =
            presigner.presignPutObject(r -> r
                .putObjectRequest(putRequest)
                .signatureDuration(expiration)
            );

        return PresignedUrl.of(presignedRequest.url().toString());
    }
}
```

---

## Repositories

### 1. FileJpaRepository

**위치**: `persistence/mysql/src/main/java/com/ryuqq/fileflow/persistence/mysql/repository/FileJpaRepository.java`

```java
/**
 * File JPA Repository
 * <p>
 * - Spring Data JPA
 * - QueryDSL 사용하지 않음 (MVP 단순화)
 * </p>
 */
public interface FileJpaRepository extends JpaRepository<FileJpaEntity, Long> {
    Optional<FileJpaEntity> findByFileId(String fileId);
}
```

---

### 2. UploadSessionJpaRepository

**위치**: `persistence/mysql/src/main/java/com/ryuqq/fileflow/persistence/mysql/repository/UploadSessionJpaRepository.java`

```java
/**
 * UploadSession JPA Repository
 * <p>
 * - Spring Data JPA
 * - sessionId 조회 (멱등성 체크)
 * </p>
 */
public interface UploadSessionJpaRepository extends JpaRepository<UploadSessionJpaEntity, Long> {
    Optional<UploadSessionJpaEntity> findBySessionId(String sessionId);
}
```

---

**작성자**: Claude (Anthropic)
**검토자**: ryu-qqq
**변경 이력**:
- 2025-11-18: 초안 작성 (session/single Persistence Layer)
