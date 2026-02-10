package com.ryuqq.fileflow.adapter.out.persistence.download.entity;

import com.ryuqq.fileflow.adapter.out.persistence.common.entity.BaseAuditEntity;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.download.vo.DownloadTaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "download_task")
public class DownloadTaskJpaEntity extends BaseAuditEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "source_url", columnDefinition = "TEXT", nullable = false)
    private String sourceUrl;

    @Column(name = "bucket", length = 100, nullable = false)
    private String bucket;

    @Column(name = "s3_key", length = 512, nullable = false)
    private String s3Key;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", length = 20, nullable = false)
    private AccessType accessType;

    @Column(name = "purpose", length = 100, nullable = false)
    private String purpose;

    @Column(name = "source", length = 100, nullable = false)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private DownloadTaskStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "max_retries", nullable = false)
    private int maxRetries;

    @Column(name = "callback_url", columnDefinition = "TEXT")
    private String callbackUrl;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected DownloadTaskJpaEntity() {}

    private DownloadTaskJpaEntity(
            String id,
            String sourceUrl,
            String bucket,
            String s3Key,
            AccessType accessType,
            String purpose,
            String source,
            DownloadTaskStatus status,
            int retryCount,
            int maxRetries,
            String callbackUrl,
            String lastError,
            Instant createdAt,
            Instant updatedAt,
            Instant startedAt,
            Instant completedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.sourceUrl = sourceUrl;
        this.bucket = bucket;
        this.s3Key = s3Key;
        this.accessType = accessType;
        this.purpose = purpose;
        this.source = source;
        this.status = status;
        this.retryCount = retryCount;
        this.maxRetries = maxRetries;
        this.callbackUrl = callbackUrl;
        this.lastError = lastError;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public static DownloadTaskJpaEntity create(
            String id,
            String sourceUrl,
            String bucket,
            String s3Key,
            AccessType accessType,
            String purpose,
            String source,
            DownloadTaskStatus status,
            int retryCount,
            int maxRetries,
            String callbackUrl,
            String lastError,
            Instant createdAt,
            Instant updatedAt,
            Instant startedAt,
            Instant completedAt) {
        return new DownloadTaskJpaEntity(
                id,
                sourceUrl,
                bucket,
                s3Key,
                accessType,
                purpose,
                source,
                status,
                retryCount,
                maxRetries,
                callbackUrl,
                lastError,
                createdAt,
                updatedAt,
                startedAt,
                completedAt);
    }

    public String getId() {
        return id;
    }

    public String getSourceUrl() {
        return sourceUrl;
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

    public String getPurpose() {
        return purpose;
    }

    public String getSource() {
        return source;
    }

    public DownloadTaskStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
