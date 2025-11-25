package com.ryuqq.fileflow.adapter.out.persistence.session.entity;

import com.ryuqq.fileflow.adapter.out.persistence.common.entity.BaseAuditEntity;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

/**
 * SingleUploadSession JPA Entity.
 *
 * <p>단일 파일 업로드 세션 정보를 저장합니다.
 */
@Entity
@Table(name = "single_upload_session")
public class SingleUploadSessionJpaEntity extends BaseAuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "idempotency_key", nullable = false, length = 36)
    private String idempotencyKey;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "organization_name", nullable = false, length = 100)
    private String organizationName;

    @Column(name = "organization_namespace", nullable = false, length = 50)
    private String organizationNamespace;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "tenant_name", nullable = false, length = 50)
    private String tenantName;

    @Column(name = "user_role", nullable = false, length = 20)
    private String userRole;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "bucket", nullable = false, length = 63)
    private String bucket;

    @Column(name = "s3_key", nullable = false, length = 1024)
    private String s3Key;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @Column(name = "presigned_url", length = 2048)
    private String presignedUrl;

    @Column(name = "etag", length = 64)
    private String etag;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected SingleUploadSessionJpaEntity() {
        super();
    }

    private SingleUploadSessionJpaEntity(
            String id,
            String idempotencyKey,
            Long userId,
            Long organizationId,
            String organizationName,
            String organizationNamespace,
            Long tenantId,
            String tenantName,
            String userRole,
            String email,
            String fileName,
            Long fileSize,
            String contentType,
            String bucket,
            String s3Key,
            LocalDateTime expiresAt,
            SessionStatus status,
            String presignedUrl,
            String etag,
            LocalDateTime completedAt,
            Long version,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.userId = userId;
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.organizationNamespace = organizationNamespace;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.userRole = userRole;
        this.email = email;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.bucket = bucket;
        this.s3Key = s3Key;
        this.expiresAt = expiresAt;
        this.status = status;
        this.presignedUrl = presignedUrl;
        this.etag = etag;
        this.completedAt = completedAt;
        this.version = version;
    }

    public static SingleUploadSessionJpaEntity of(
            String id,
            String idempotencyKey,
            Long userId,
            Long organizationId,
            String organizationName,
            String organizationNamespace,
            Long tenantId,
            String tenantName,
            String userRole,
            String email,
            String fileName,
            Long fileSize,
            String contentType,
            String bucket,
            String s3Key,
            LocalDateTime expiresAt,
            SessionStatus status,
            String presignedUrl,
            String etag,
            LocalDateTime completedAt,
            Long version,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new SingleUploadSessionJpaEntity(
                id,
                idempotencyKey,
                userId,
                organizationId,
                organizationName,
                organizationNamespace,
                tenantId,
                tenantName,
                userRole,
                email,
                fileName,
                fileSize,
                contentType,
                bucket,
                s3Key,
                expiresAt,
                status,
                presignedUrl,
                etag,
                completedAt,
                version,
                createdAt,
                updatedAt);
    }

    public String getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public String getOrganizationNamespace() {
        return organizationNamespace;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public String getUserRole() {
        return userRole;
    }

    public String getEmail() {
        return email;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public String getBucket() {
        return bucket;
    }

    public String getS3Key() {
        return s3Key;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public String getPresignedUrl() {
        return presignedUrl;
    }

    public String getEtag() {
        return etag;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public Long getVersion() {
        return version;
    }
}
