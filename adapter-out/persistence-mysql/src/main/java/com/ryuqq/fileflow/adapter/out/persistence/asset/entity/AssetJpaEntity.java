package com.ryuqq.fileflow.adapter.out.persistence.asset.entity;

import com.ryuqq.fileflow.adapter.out.persistence.common.entity.SoftDeletableEntity;
import com.ryuqq.fileflow.domain.asset.vo.AssetOrigin;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "asset")
public class AssetJpaEntity extends SoftDeletableEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "bucket", length = 100, nullable = false)
    private String bucket;

    @Column(name = "s3_key", length = 512, nullable = false)
    private String s3Key;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", length = 20, nullable = false)
    private AccessType accessType;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "content_type", length = 100, nullable = false)
    private String contentType;

    @Column(name = "etag", length = 255, nullable = false)
    private String etag;

    @Column(name = "extension", length = 20, nullable = false)
    private String extension;

    @Enumerated(EnumType.STRING)
    @Column(name = "origin", length = 30, nullable = false)
    private AssetOrigin origin;

    @Column(name = "origin_id", length = 36, nullable = false)
    private String originId;

    @Column(name = "purpose", length = 100, nullable = false)
    private String purpose;

    @Column(name = "source", length = 100, nullable = false)
    private String source;

    protected AssetJpaEntity() {}

    private AssetJpaEntity(
            String id,
            String bucket,
            String s3Key,
            AccessType accessType,
            String fileName,
            long fileSize,
            String contentType,
            String etag,
            String extension,
            AssetOrigin origin,
            String originId,
            String purpose,
            String source,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt) {
        super(createdAt, updatedAt, deletedAt);
        this.id = id;
        this.bucket = bucket;
        this.s3Key = s3Key;
        this.accessType = accessType;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.etag = etag;
        this.extension = extension;
        this.origin = origin;
        this.originId = originId;
        this.purpose = purpose;
        this.source = source;
    }

    public static AssetJpaEntity create(
            String id,
            String bucket,
            String s3Key,
            AccessType accessType,
            String fileName,
            long fileSize,
            String contentType,
            String etag,
            String extension,
            AssetOrigin origin,
            String originId,
            String purpose,
            String source,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt) {
        return new AssetJpaEntity(
                id,
                bucket,
                s3Key,
                accessType,
                fileName,
                fileSize,
                contentType,
                etag,
                extension,
                origin,
                originId,
                purpose,
                source,
                createdAt,
                updatedAt,
                deletedAt);
    }

    public String getId() {
        return id;
    }

    public String getBucket() {
        return bucket;
    }

    public String getS3Key() {
        return s3Key;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public String getEtag() {
        return etag;
    }

    public String getExtension() {
        return extension;
    }

    public AssetOrigin getOrigin() {
        return origin;
    }

    public String getOriginId() {
        return originId;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getSource() {
        return source;
    }
}
