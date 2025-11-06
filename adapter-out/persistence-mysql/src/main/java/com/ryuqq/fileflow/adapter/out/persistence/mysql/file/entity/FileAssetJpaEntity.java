package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.common.entity.BaseAuditEntity;
import com.ryuqq.fileflow.domain.file.asset.FileStatus;
import com.ryuqq.fileflow.domain.file.asset.Visibility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;

import java.time.LocalDateTime;

/**
 * FileAsset JPA Entity (Persistence Layer)
 *
 * <p><strong>역할</strong>: DB 매핑만 담당 (비즈니스 로직 없음)</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/file/entity/</p>
 * <p><strong>변환</strong>: {@code FileAssetMapper}를 통해 Domain {@code FileAsset}와 상호 변환</p>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>✅ JPA 어노테이션만 사용 (비즈니스 로직 없음)</li>
 *   <li>✅ Long FK 전략 (JPA 관계 어노테이션 금지)</li>
 *   <li>✅ Setter 제공 (JPA 전용, 외부 노출 금지)</li>
 *   <li>✅ Static Factory Methods: {@code create()}, {@code reconstitute()}</li>
 *   <li>❌ Lombok 금지</li>
 *   <li>❌ JPA 관계 어노테이션 금지 (ManyToOne, OneToMany 등)</li>
 * </ul>
 *
 * <h3>테이블 스키마</h3>
 * <pre>
 * CREATE TABLE file_assets (
 *   id BIGINT PRIMARY KEY AUTO_INCREMENT,
 *   tenant_id VARCHAR(50) NOT NULL,
 *   organization_id BIGINT NULL,
 *   owner_user_id BIGINT NOT NULL,
 *   file_name VARCHAR(255) NOT NULL,
 *   file_size BIGINT NOT NULL,
 *   mime_type VARCHAR(150) NOT NULL,
 *   storage_key VARCHAR(512) NOT NULL,
 *   checksum_sha256 CHAR(64) NULL,
 *   upload_session_id BIGINT NOT NULL,
 *   status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
 *   visibility VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
 *   uploaded_at DATETIME NOT NULL,
 *   processed_at DATETIME NULL,
 *   expires_at DATETIME NULL,
 *   retention_days INT NULL,
 *   deleted_at DATETIME NULL,
 *   created_at DATETIME NOT NULL,
 *   updated_at DATETIME NOT NULL,
 *   INDEX idx_tenant_org_uploaded (tenant_id, organization_id, uploaded_at),
 *   INDEX idx_owner (owner_user_id),
 *   INDEX idx_status (status),
 *   INDEX idx_deleted (deleted_at)
 * );
 * </pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see com.ryuqq.fileflow.domain.file.asset.FileAsset Domain Model
 */
@Entity
@Table(
    name = "file_assets",
    indexes = {
        @Index(name = "idx_tenant_org_uploaded", columnList = "tenant_id,organization_id,uploaded_at"),
        @Index(name = "idx_owner", columnList = "owner_user_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_deleted", columnList = "deleted_at")
    }
)
public class FileAssetJpaEntity extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Tenant ID (보안 스코프 필수)
     */
    @Column(name = "tenant_id", nullable = false, length = 50)
    private Long tenantId;

    /**
     * Organization ID (선택, Long FK Strategy)
     */
    @Column(name = "organization_id")
    private Long organizationId;

    /**
     * Owner User ID (Long FK Strategy)
     * ❌ ManyToOne 관계 어노테이션 사용 안함!
     * 
     * <p>익명 업로드 및 외부 다운로드 시 null 허용</p>
     */
    @Column(name = "owner_user_id", nullable = true)
    private Long ownerUserId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", nullable = false, length = 150)
    private String mimeType;

    @Column(name = "storage_key", nullable = false, length = 512)
    private String storageKey;

    @Column(name = "checksum_sha256", length = 64)
    private String checksumSha256;

    /**
     * Upload Session ID (Long FK Strategy)
     */
    @Column(name = "upload_session_id", nullable = false)
    private Long uploadSessionId;

    /**
     * File Status (PROCESSING, AVAILABLE, DELETED, ERROR)
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private FileStatus status;

    /**
     * Visibility (PRIVATE, INTERNAL, PUBLIC)
     */
    @Column(name = "visibility", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    /**
     * 업로드 완료 시간
     */
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    /**
     * 후처리 완료 시간 (PROCESSING → AVAILABLE 전이 시)
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * 만료 시간 (선택)
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * 보존 기간 (일 단위, 선택)
     */
    @Column(name = "retention_days")
    private Integer retentionDays;

    /**
     * Soft Delete 타임스탬프
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 기본 생성자 (JPA 스펙 요구사항)
     */
    protected FileAssetJpaEntity() {
        super();
    }

    /**
     * Private 전체 생성자 (reconstitute 전용)
     *
     * @param id File Asset ID
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @param ownerUserId Owner User ID
     * @param fileName File Name
     * @param fileSize File Size
     * @param mimeType MIME Type
     * @param storageKey Storage Key
     * @param checksumSha256 Checksum SHA-256
     * @param uploadSessionId Upload Session ID
     * @param status File Status
     * @param visibility Visibility
     * @param uploadedAt Uploaded At
     * @param processedAt Processed At
     * @param expiresAt Expires At
     * @param retentionDays Retention Days
     * @param deletedAt Deleted At
     * @param createdAt Created At
     * @param updatedAt Updated At
     */
    private FileAssetJpaEntity(
        Long id,
        Long tenantId,
        Long organizationId,
        Long ownerUserId,
        String fileName,
        Long fileSize,
        String mimeType,
        String storageKey,
        String checksumSha256,
        Long uploadSessionId,
        FileStatus status,
        Visibility visibility,
        LocalDateTime uploadedAt,
        LocalDateTime processedAt,
        LocalDateTime expiresAt,
        Integer retentionDays,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        super(createdAt, updatedAt);
        this.id = id;
        this.tenantId = tenantId;
        this.organizationId = organizationId;
        this.ownerUserId = ownerUserId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.storageKey = storageKey;
        this.checksumSha256 = checksumSha256;
        this.uploadSessionId = uploadSessionId;
        this.status = status;
        this.visibility = visibility;
        this.uploadedAt = uploadedAt;
        this.processedAt = processedAt;
        this.expiresAt = expiresAt;
        this.retentionDays = retentionDays;
        this.deletedAt = deletedAt;
    }

    /**
     * Static Factory Method - 신규 엔티티 생성
     *
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @param ownerUserId Owner User ID
     * @param fileName File Name
     * @param fileSize File Size
     * @param mimeType MIME Type
     * @param storageKey Storage Key
     * @param checksumSha256 Checksum SHA-256
     * @param uploadSessionId Upload Session ID
     * @param uploadedAt Uploaded At
     * @return 생성된 FileAssetJpaEntity
     */
    public static FileAssetJpaEntity create(
        Long tenantId,
        Long organizationId,
        Long ownerUserId,
        String fileName,
        Long fileSize,
        String mimeType,
        String storageKey,
        String checksumSha256,
        Long uploadSessionId,
        LocalDateTime uploadedAt
    ) {
        FileAssetJpaEntity entity = new FileAssetJpaEntity();
        entity.tenantId = tenantId;
        entity.organizationId = organizationId;
        entity.ownerUserId = ownerUserId;
        entity.fileName = fileName;
        entity.fileSize = fileSize;
        entity.mimeType = mimeType;
        entity.storageKey = storageKey;
        entity.checksumSha256 = checksumSha256;
        entity.uploadSessionId = uploadSessionId;
        entity.status = FileStatus.PROCESSING; // 초기 상태
        entity.visibility = Visibility.PRIVATE; // 기본 가시성
        entity.uploadedAt = uploadedAt;
        entity.initializeAuditFields();
        return entity;
    }

    /**
     * Static Factory Method - DB 조회 데이터로 재구성
     *
     * @param id File Asset ID
     * @param tenantId Tenant ID
     * @param organizationId Organization ID
     * @param ownerUserId Owner User ID
     * @param fileName File Name
     * @param fileSize File Size
     * @param mimeType MIME Type
     * @param storageKey Storage Key
     * @param checksumSha256 Checksum SHA-256
     * @param uploadSessionId Upload Session ID
     * @param status File Status
     * @param visibility Visibility
     * @param uploadedAt Uploaded At
     * @param processedAt Processed At
     * @param expiresAt Expires At
     * @param retentionDays Retention Days
     * @param deletedAt Deleted At
     * @param createdAt Created At
     * @param updatedAt Updated At
     * @return 재구성된 FileAssetJpaEntity
     */
    public static FileAssetJpaEntity reconstitute(
        Long id,
        Long tenantId,
        Long organizationId,
        Long ownerUserId,
        String fileName,
        Long fileSize,
        String mimeType,
        String storageKey,
        String checksumSha256,
        Long uploadSessionId,
        FileStatus status,
        Visibility visibility,
        LocalDateTime uploadedAt,
        LocalDateTime processedAt,
        LocalDateTime expiresAt,
        Integer retentionDays,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new FileAssetJpaEntity(
            id,
            tenantId,
            organizationId,
            ownerUserId,
            fileName,
            fileSize,
            mimeType,
            storageKey,
            checksumSha256,
            uploadSessionId,
            status,
            visibility,
            uploadedAt,
            processedAt,
            expiresAt,
            retentionDays,
            deletedAt,
            createdAt,
            updatedAt
        );
    }

    // ===== Getters =====

    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getChecksumSha256() {
        return checksumSha256;
    }

    public Long getUploadSessionId() {
        return uploadSessionId;
    }

    public FileStatus getStatus() {
        return status;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public Integer getRetentionDays() {
        return retentionDays;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
