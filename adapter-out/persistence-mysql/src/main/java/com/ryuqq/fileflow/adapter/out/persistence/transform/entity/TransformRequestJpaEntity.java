package com.ryuqq.fileflow.adapter.out.persistence.transform.entity;

import com.ryuqq.fileflow.adapter.out.persistence.common.entity.BaseAuditEntity;
import com.ryuqq.fileflow.domain.transform.vo.TransformStatus;
import com.ryuqq.fileflow.domain.transform.vo.TransformType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "transform_request")
public class TransformRequestJpaEntity extends BaseAuditEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "source_asset_id", length = 36, nullable = false)
    private String sourceAssetId;

    @Column(name = "source_content_type", length = 100, nullable = false)
    private String sourceContentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private TransformType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private TransformStatus status;

    @Column(name = "result_asset_id", length = 36)
    private String resultAssetId;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "maintain_aspect_ratio", nullable = false)
    private boolean maintainAspectRatio;

    @Column(name = "target_format", length = 20)
    private String targetFormat;

    @Column(name = "quality")
    private Integer quality;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected TransformRequestJpaEntity() {}

    private TransformRequestJpaEntity(
            String id,
            String sourceAssetId,
            String sourceContentType,
            TransformType type,
            TransformStatus status,
            String resultAssetId,
            String lastError,
            Integer width,
            Integer height,
            boolean maintainAspectRatio,
            String targetFormat,
            Integer quality,
            Instant createdAt,
            Instant updatedAt,
            Instant completedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.sourceAssetId = sourceAssetId;
        this.sourceContentType = sourceContentType;
        this.type = type;
        this.status = status;
        this.resultAssetId = resultAssetId;
        this.lastError = lastError;
        this.width = width;
        this.height = height;
        this.maintainAspectRatio = maintainAspectRatio;
        this.targetFormat = targetFormat;
        this.quality = quality;
        this.completedAt = completedAt;
    }

    public static TransformRequestJpaEntity create(
            String id,
            String sourceAssetId,
            String sourceContentType,
            TransformType type,
            TransformStatus status,
            String resultAssetId,
            String lastError,
            Integer width,
            Integer height,
            boolean maintainAspectRatio,
            String targetFormat,
            Integer quality,
            Instant createdAt,
            Instant updatedAt,
            Instant completedAt) {
        return new TransformRequestJpaEntity(
                id,
                sourceAssetId,
                sourceContentType,
                type,
                status,
                resultAssetId,
                lastError,
                width,
                height,
                maintainAspectRatio,
                targetFormat,
                quality,
                createdAt,
                updatedAt,
                completedAt);
    }

    public String getId() {
        return id;
    }

    public String getSourceAssetId() {
        return sourceAssetId;
    }

    public String getSourceContentType() {
        return sourceContentType;
    }

    public TransformType getType() {
        return type;
    }

    public TransformStatus getStatus() {
        return status;
    }

    public String getResultAssetId() {
        return resultAssetId;
    }

    public String getLastError() {
        return lastError;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public boolean isMaintainAspectRatio() {
        return maintainAspectRatio;
    }

    public String getTargetFormat() {
        return targetFormat;
    }

    public Integer getQuality() {
        return quality;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
