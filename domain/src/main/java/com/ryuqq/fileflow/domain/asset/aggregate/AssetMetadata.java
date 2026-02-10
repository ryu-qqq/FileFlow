package com.ryuqq.fileflow.domain.asset.aggregate;

import com.ryuqq.fileflow.domain.asset.id.AssetId;
import com.ryuqq.fileflow.domain.asset.id.AssetMetadataId;
import java.time.Instant;
import java.util.Objects;

/**
 * AssetMetadata Aggregate.
 *
 * <p>이미지 Asset의 메타데이터를 관리합니다.
 *
 * <p>Asset 생성 후 비동기로 메타데이터가 추출되어 생성됩니다.
 *
 * <p>DB 테이블: asset_metadata (asset 테이블과 1:1 관계)
 */
public class AssetMetadata {

    private final AssetMetadataId id;
    private final AssetId assetId;
    private int width;
    private int height;
    private final String transformType;
    private final Instant createdAt;
    private Instant updatedAt;

    private AssetMetadata(
            AssetMetadataId id,
            AssetId assetId,
            int width,
            int height,
            String transformType,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.assetId = assetId;
        this.width = width;
        this.height = height;
        this.transformType = transformType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 새 이미지 메타데이터 생성.
     *
     * @param transformType 변환 유형 (null이면 원본 이미지, "RESIZE"/"CONVERT"/"COMPRESS"/"THUMBNAIL" 등)
     */
    public static AssetMetadata forNew(
            AssetMetadataId id,
            AssetId assetId,
            int width,
            int height,
            String transformType,
            Instant now) {
        return new AssetMetadata(id, assetId, width, height, transformType, now, now);
    }

    public static AssetMetadata reconstitute(
            AssetMetadataId id,
            AssetId assetId,
            int width,
            int height,
            String transformType,
            Instant createdAt,
            Instant updatedAt) {
        return new AssetMetadata(id, assetId, width, height, transformType, createdAt, updatedAt);
    }

    /** 이미지 해상도를 갱신합니다 (재추출 시). */
    public void updateDimensions(int width, int height, Instant now) {
        this.width = width;
        this.height = height;
        this.updatedAt = now;
    }

    public boolean isTransformed() {
        return transformType != null;
    }

    // -- query methods --

    public AssetMetadataId id() {
        return id;
    }

    public String idValue() {
        return id.value();
    }

    public AssetId assetId() {
        return assetId;
    }

    public String assetIdValue() {
        return assetId.value();
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public String transformType() {
        return transformType;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    // -- equals/hashCode ID 기반 --

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetMetadata that = (AssetMetadata) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
