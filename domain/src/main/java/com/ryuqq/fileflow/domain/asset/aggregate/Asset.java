package com.ryuqq.fileflow.domain.asset.aggregate;

import com.ryuqq.fileflow.domain.asset.exception.AssetErrorCode;
import com.ryuqq.fileflow.domain.asset.exception.AssetException;
import com.ryuqq.fileflow.domain.asset.id.AssetId;
import com.ryuqq.fileflow.domain.asset.vo.AssetOrigin;
import com.ryuqq.fileflow.domain.asset.vo.FileInfo;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.common.vo.StorageInfo;
import java.time.Instant;
import java.util.Objects;

/**
 * Asset Aggregate Root.
 *
 * <p>S3에 저장된 파일을 표현합니다.
 *
 * <p>업로드/다운로드가 완료된 후에만 생성됩니다 (존재 = 파일이 S3에 있다는 보장).
 */
public class Asset {

    private final AssetId id;
    private final StorageInfo storageInfo;
    private final FileInfo fileInfo;
    private final AssetOrigin origin;
    private final String originId;
    private final String purpose;
    private final String source;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    private Asset(
            AssetId id,
            StorageInfo storageInfo,
            FileInfo fileInfo,
            AssetOrigin origin,
            String originId,
            String purpose,
            String source,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt) {
        this.id = id;
        this.storageInfo = storageInfo;
        this.fileInfo = fileInfo;
        this.origin = origin;
        this.originId = originId;
        this.purpose = purpose;
        this.source = source;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static Asset forNew(
            AssetId id,
            StorageInfo storageInfo,
            FileInfo fileInfo,
            AssetOrigin origin,
            String originId,
            String purpose,
            String source,
            Instant now) {
        return new Asset(
                id, storageInfo, fileInfo, origin, originId, purpose, source, now, now, null);
    }

    public static Asset reconstitute(
            AssetId id,
            StorageInfo storageInfo,
            FileInfo fileInfo,
            AssetOrigin origin,
            String originId,
            String purpose,
            String source,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt) {
        return new Asset(
                id,
                storageInfo,
                fileInfo,
                origin,
                originId,
                purpose,
                source,
                createdAt,
                updatedAt,
                deletedAt);
    }

    /** 소프트 삭제 처리. */
    public void delete(Instant now) {
        validateNotDeleted();
        this.deletedAt = now;
        this.updatedAt = now;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isImage() {
        return fileInfo.contentType() != null && fileInfo.contentType().startsWith("image/");
    }

    // -- query methods --

    public AssetId id() {
        return id;
    }

    public String idValue() {
        return id.value();
    }

    public StorageInfo storageInfo() {
        return storageInfo;
    }

    public String s3Key() {
        return storageInfo.s3Key();
    }

    public String bucket() {
        return storageInfo.bucket();
    }

    public AccessType accessType() {
        return storageInfo.accessType();
    }

    public FileInfo fileInfo() {
        return fileInfo;
    }

    public String fileName() {
        return fileInfo.fileName();
    }

    public long fileSize() {
        return fileInfo.fileSize();
    }

    public String contentType() {
        return fileInfo.contentType();
    }

    public String etag() {
        return fileInfo.etag();
    }

    public String extension() {
        return fileInfo.extension();
    }

    public AssetOrigin origin() {
        return origin;
    }

    public String originId() {
        return originId;
    }

    public String purpose() {
        return purpose;
    }

    public String source() {
        return source;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public Instant deletedAt() {
        return deletedAt;
    }

    // -- invariant validation --

    private void validateNotDeleted() {
        if (isDeleted()) {
            throw new AssetException(AssetErrorCode.ASSET_ALREADY_DELETED);
        }
    }

    // -- equals/hashCode ID 기반 --

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Asset asset = (Asset) o;
        return Objects.equals(id, asset.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
