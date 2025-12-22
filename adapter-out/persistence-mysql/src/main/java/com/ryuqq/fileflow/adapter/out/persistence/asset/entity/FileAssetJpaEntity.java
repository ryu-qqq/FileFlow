package com.ryuqq.fileflow.adapter.out.persistence.asset.entity;

import com.ryuqq.fileflow.adapter.out.persistence.common.entity.BaseAuditEntity;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * FileAsset JPA Entity.
 *
 * <p>업로드 완료된 파일 자산 정보를 저장합니다.
 */
@Entity
@Table(name = "file_asset")
public class FileAssetJpaEntity extends BaseAuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "session_id", length = 36)
    private String sessionId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "category", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private FileCategory category;

    @Column(name = "image_width")
    private Integer imageWidth;

    @Column(name = "image_height")
    private Integer imageHeight;

    @Column(name = "bucket", nullable = false, length = 63)
    private String bucket;

    @Column(name = "s3_key", nullable = false, length = 1024)
    private String s3Key;

    @Column(name = "etag", nullable = false, length = 64)
    private String etag;

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(name = "organization_id", nullable = false, length = 36)
    private String organizationId;

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private FileAssetStatus status;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "last_error_message", length = 2000)
    private String lastErrorMessage;

    protected FileAssetJpaEntity() {
        super();
    }

    private FileAssetJpaEntity(
            String id,
            String sessionId,
            String fileName,
            Long fileSize,
            String contentType,
            FileCategory category,
            Integer imageWidth,
            Integer imageHeight,
            String bucket,
            String s3Key,
            String etag,
            String userId,
            String organizationId,
            String tenantId,
            FileAssetStatus status,
            Instant processedAt,
            Instant deletedAt,
            String lastErrorMessage,
            Instant createdAt,
            Instant updatedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.sessionId = sessionId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.category = category;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.bucket = bucket;
        this.s3Key = s3Key;
        this.etag = etag;
        this.userId = userId;
        this.organizationId = organizationId;
        this.tenantId = tenantId;
        this.status = status;
        this.processedAt = processedAt;
        this.deletedAt = deletedAt;
        this.lastErrorMessage = lastErrorMessage;
    }

    public static FileAssetJpaEntity of(
            String id,
            String sessionId,
            String fileName,
            Long fileSize,
            String contentType,
            FileCategory category,
            Integer imageWidth,
            Integer imageHeight,
            String bucket,
            String s3Key,
            String etag,
            String userId,
            String organizationId,
            String tenantId,
            FileAssetStatus status,
            Instant processedAt,
            Instant deletedAt,
            String lastErrorMessage,
            Instant createdAt,
            Instant updatedAt) {
        return new FileAssetJpaEntity(
                id,
                sessionId,
                fileName,
                fileSize,
                contentType,
                category,
                imageWidth,
                imageHeight,
                bucket,
                s3Key,
                etag,
                userId,
                organizationId,
                tenantId,
                status,
                processedAt,
                deletedAt,
                lastErrorMessage,
                createdAt,
                updatedAt);
    }

    public String getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
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

    public FileCategory getCategory() {
        return category;
    }

    public Integer getImageWidth() {
        return imageWidth;
    }

    public Integer getImageHeight() {
        return imageHeight;
    }

    public String getBucket() {
        return bucket;
    }

    public String getS3Key() {
        return s3Key;
    }

    public String getEtag() {
        return etag;
    }

    public String getUserId() {
        return userId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public FileAssetStatus getStatus() {
        return status;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    /**
     * Entity 상태 업데이트.
     *
     * <p>상태 변경(Soft Delete 포함)에 사용됩니다.
     *
     * @param status 새로운 상태
     * @param processedAt 처리 완료 시각 (nullable)
     * @param deletedAt 삭제 시각 (nullable)
     * @param lastErrorMessage 마지막 에러 메시지 (nullable)
     */
    public void update(
            FileAssetStatus status,
            Instant processedAt,
            Instant deletedAt,
            String lastErrorMessage) {
        this.status = status;
        this.processedAt = processedAt;
        this.deletedAt = deletedAt;
        this.lastErrorMessage = lastErrorMessage;
    }

    /**
     * 에러 메시지만 업데이트.
     *
     * <p>처리 실패 시 에러 메시지를 기록합니다.
     *
     * @param errorMessage 에러 메시지
     */
    public void updateErrorMessage(String errorMessage) {
        this.lastErrorMessage = errorMessage;
    }
}
