package com.ryuqq.fileflow.domain.asset.fixture;

import com.ryuqq.fileflow.domain.asset.aggregate.ProcessedFileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.ImageFormat;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariant;
import com.ryuqq.fileflow.domain.asset.vo.ProcessedFileAssetId;
import com.ryuqq.fileflow.domain.common.fixture.ClockFixture;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UserId;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.util.UUID;

/**
 * ProcessedFileAsset Aggregate Test Fixture.
 *
 * @author development-team
 * @since 1.0.0
 */
public class ProcessedFileAssetFixture {

    private ProcessedFileAssetFixture() {
        throw new AssertionError("Utility class");
    }

    /**
     * 기본 ProcessedFileAsset Fixture (ORIGINAL + WEBP).
     *
     * @return ProcessedFileAsset
     */
    public static ProcessedFileAsset defaultProcessedFileAsset() {
        return ProcessedFileAsset.reconstitute(
                new ProcessedFileAssetId(UUID.randomUUID()),
                new FileAssetId(UUID.randomUUID()),
                null,
                ImageVariant.ORIGINAL,
                ImageFormat.WEBP,
                new FileName("processed-image.webp"),
                new FileSize(1024L),
                new S3Bucket("test-bucket"),
                new S3Key("uploads/test/processed-image.webp"),
                UserId.generate(),
                OrganizationId.generate(),
                TenantId.generate(),
                ClockFixture.defaultClock().instant());
    }

    /**
     * LARGE variant ProcessedFileAsset Fixture.
     *
     * @return ProcessedFileAsset
     */
    public static ProcessedFileAsset largeVariantProcessedFileAsset() {
        return ProcessedFileAsset.reconstitute(
                new ProcessedFileAssetId(UUID.randomUUID()),
                new FileAssetId(UUID.randomUUID()),
                null,
                ImageVariant.LARGE,
                ImageFormat.WEBP,
                new FileName("processed-image-large.webp"),
                new FileSize(2048L),
                new S3Bucket("test-bucket"),
                new S3Key("uploads/test/processed-image-large.webp"),
                UserId.generate(),
                OrganizationId.generate(),
                TenantId.generate(),
                ClockFixture.defaultClock().instant());
    }

    /**
     * THUMBNAIL variant ProcessedFileAsset Fixture.
     *
     * @return ProcessedFileAsset
     */
    public static ProcessedFileAsset thumbnailProcessedFileAsset() {
        return ProcessedFileAsset.reconstitute(
                new ProcessedFileAssetId(UUID.randomUUID()),
                new FileAssetId(UUID.randomUUID()),
                null,
                ImageVariant.THUMBNAIL,
                ImageFormat.WEBP,
                new FileName("processed-image-thumb.webp"),
                new FileSize(256L),
                new S3Bucket("test-bucket"),
                new S3Key("uploads/test/processed-image-thumb.webp"),
                UserId.generate(),
                OrganizationId.generate(),
                TenantId.generate(),
                ClockFixture.defaultClock().instant());
    }

    /**
     * HTML에서 추출된 이미지 Fixture (parentAssetId 포함).
     *
     * @return ProcessedFileAsset
     */
    public static ProcessedFileAsset htmlExtractedProcessedFileAsset() {
        return ProcessedFileAsset.reconstitute(
                new ProcessedFileAssetId(UUID.randomUUID()),
                new FileAssetId(UUID.randomUUID()),
                new FileAssetId(UUID.randomUUID()),
                ImageVariant.MEDIUM,
                ImageFormat.WEBP,
                new FileName("extracted-image.webp"),
                new FileSize(512L),
                new S3Bucket("test-bucket"),
                new S3Key("uploads/test/extracted-image.webp"),
                UserId.generate(),
                OrganizationId.generate(),
                TenantId.generate(),
                ClockFixture.defaultClock().instant());
    }

    /**
     * 특정 원본 에셋 ID를 가진 ProcessedFileAsset.
     *
     * @param originalAssetId 원본 에셋 ID
     * @return ProcessedFileAsset
     */
    public static ProcessedFileAsset processedFileAssetWithOriginalId(FileAssetId originalAssetId) {
        return ProcessedFileAsset.reconstitute(
                new ProcessedFileAssetId(UUID.randomUUID()),
                originalAssetId,
                null,
                ImageVariant.ORIGINAL,
                ImageFormat.WEBP,
                new FileName("processed-image.webp"),
                new FileSize(1024L),
                new S3Bucket("test-bucket"),
                new S3Key("uploads/test/processed-image.webp"),
                UserId.generate(),
                OrganizationId.generate(),
                TenantId.generate(),
                ClockFixture.defaultClock().instant());
    }

    /**
     * 특정 variant와 format을 가진 ProcessedFileAsset.
     *
     * @param variant 이미지 변형
     * @param format 이미지 포맷
     * @return ProcessedFileAsset
     */
    public static ProcessedFileAsset processedFileAssetWith(ImageVariant variant, ImageFormat format) {
        return ProcessedFileAsset.reconstitute(
                new ProcessedFileAssetId(UUID.randomUUID()),
                new FileAssetId(UUID.randomUUID()),
                null,
                variant,
                format,
                new FileName("processed-image." + format.extension()),
                new FileSize(1024L),
                new S3Bucket("test-bucket"),
                new S3Key("uploads/test/processed-image." + format.extension()),
                UserId.generate(),
                OrganizationId.generate(),
                TenantId.generate(),
                ClockFixture.defaultClock().instant());
    }
}
