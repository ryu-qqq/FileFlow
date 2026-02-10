package com.ryuqq.fileflow.adapter.out.persistence.session.entity;

import com.ryuqq.fileflow.adapter.out.persistence.common.entity.BaseAuditEntity;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.session.vo.MultipartSessionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "multipart_upload_session")
public class MultipartUploadSessionJpaEntity extends BaseAuditEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "s3_key", length = 512, nullable = false)
    private String s3Key;

    @Column(name = "bucket", length = 100, nullable = false)
    private String bucket;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", length = 20, nullable = false)
    private AccessType accessType;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "content_type", length = 100, nullable = false)
    private String contentType;

    @Column(name = "upload_id", length = 255, nullable = false)
    private String uploadId;

    @Column(name = "part_size", nullable = false)
    private long partSize;

    @Column(name = "purpose", length = 100, nullable = false)
    private String purpose;

    @Column(name = "source", length = 100, nullable = false)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private MultipartSessionStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected MultipartUploadSessionJpaEntity() {}

    private MultipartUploadSessionJpaEntity(
            String id,
            String s3Key,
            String bucket,
            AccessType accessType,
            String fileName,
            String contentType,
            String uploadId,
            long partSize,
            String purpose,
            String source,
            MultipartSessionStatus status,
            Instant expiresAt,
            Instant createdAt,
            Instant updatedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.s3Key = s3Key;
        this.bucket = bucket;
        this.accessType = accessType;
        this.fileName = fileName;
        this.contentType = contentType;
        this.uploadId = uploadId;
        this.partSize = partSize;
        this.purpose = purpose;
        this.source = source;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    public static MultipartUploadSessionJpaEntity create(
            String id,
            String s3Key,
            String bucket,
            AccessType accessType,
            String fileName,
            String contentType,
            String uploadId,
            long partSize,
            String purpose,
            String source,
            MultipartSessionStatus status,
            Instant expiresAt,
            Instant createdAt,
            Instant updatedAt) {
        return new MultipartUploadSessionJpaEntity(
                id,
                s3Key,
                bucket,
                accessType,
                fileName,
                contentType,
                uploadId,
                partSize,
                purpose,
                source,
                status,
                expiresAt,
                createdAt,
                updatedAt);
    }

    public String getId() {
        return id;
    }

    public String getS3Key() {
        return s3Key;
    }

    public String getBucket() {
        return bucket;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public String getUploadId() {
        return uploadId;
    }

    public long getPartSize() {
        return partSize;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getSource() {
        return source;
    }

    public MultipartSessionStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
