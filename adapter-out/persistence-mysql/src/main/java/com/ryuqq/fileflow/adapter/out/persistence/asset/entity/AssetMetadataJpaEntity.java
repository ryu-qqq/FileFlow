package com.ryuqq.fileflow.adapter.out.persistence.asset.entity;

import com.ryuqq.fileflow.adapter.out.persistence.common.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "asset_metadata")
public class AssetMetadataJpaEntity extends BaseAuditEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "asset_id", length = 36, nullable = false)
    private String assetId;

    @Column(name = "width", nullable = false)
    private int width;

    @Column(name = "height", nullable = false)
    private int height;

    @Column(name = "transform_type", length = 30)
    private String transformType;

    protected AssetMetadataJpaEntity() {}

    private AssetMetadataJpaEntity(
            String id,
            String assetId,
            int width,
            int height,
            String transformType,
            Instant createdAt,
            Instant updatedAt) {
        super(createdAt, updatedAt);
        this.id = id;
        this.assetId = assetId;
        this.width = width;
        this.height = height;
        this.transformType = transformType;
    }

    public static AssetMetadataJpaEntity create(
            String id,
            String assetId,
            int width,
            int height,
            String transformType,
            Instant createdAt,
            Instant updatedAt) {
        return new AssetMetadataJpaEntity(
                id, assetId, width, height, transformType, createdAt, updatedAt);
    }

    public String getId() {
        return id;
    }

    public String getAssetId() {
        return assetId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getTransformType() {
        return transformType;
    }
}
