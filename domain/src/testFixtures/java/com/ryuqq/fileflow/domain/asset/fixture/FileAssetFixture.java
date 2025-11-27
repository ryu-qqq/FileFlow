package com.ryuqq.fileflow.domain.asset.fixture;

import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import com.ryuqq.fileflow.domain.common.fixture.ClockFixture;
import com.ryuqq.fileflow.domain.session.fixture.*;
import com.ryuqq.fileflow.domain.session.vo.*;
import java.time.Clock;
import java.time.LocalDateTime;

/**
 * FileAsset Aggregate Test Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public class FileAssetFixture {

    private FileAssetFixture() {
        throw new AssertionError("Utility class");
    }

    /** 기본 FileAsset Fixture (PENDING 상태, 신규 생성) */
    public static FileAsset defaultFileAsset() {
        return FileAsset.forNew(
                UploadSessionIdFixture.defaultUploadSessionId(),
                FileNameFixture.defaultFileName(),
                FileSizeFixture.defaultFileSize(),
                ContentTypeFixture.defaultContentType(),
                FileCategory.IMAGE,
                S3BucketFixture.defaultS3Bucket(),
                S3KeyFixture.defaultS3Key(),
                ETagFixture.defaultETag(),
                1000L,
                1L,
                1L,
                ClockFixture.defaultClock());
    }

    /** PROCESSING 상태의 FileAsset Fixture */
    public static FileAsset processingFileAsset() {
        FileAsset asset =
                FileAsset.forNew(
                        UploadSessionIdFixture.defaultUploadSessionId(),
                        FileNameFixture.defaultFileName(),
                        FileSizeFixture.defaultFileSize(),
                        ContentTypeFixture.defaultContentType(),
                        FileCategory.IMAGE,
                        S3BucketFixture.defaultS3Bucket(),
                        S3KeyFixture.defaultS3Key(),
                        ETagFixture.defaultETag(),
                        1000L,
                        1L,
                        1L,
                        ClockFixture.defaultClock());
        asset.startProcessing();
        return asset;
    }

    /** COMPLETED 상태의 FileAsset Fixture */
    public static FileAsset completedFileAsset() {
        FileAsset asset =
                FileAsset.forNew(
                        UploadSessionIdFixture.defaultUploadSessionId(),
                        FileNameFixture.defaultFileName(),
                        FileSizeFixture.defaultFileSize(),
                        ContentTypeFixture.defaultContentType(),
                        FileCategory.IMAGE,
                        S3BucketFixture.defaultS3Bucket(),
                        S3KeyFixture.defaultS3Key(),
                        ETagFixture.defaultETag(),
                        1000L,
                        1L,
                        1L,
                        ClockFixture.defaultClock());
        asset.completeProcessing();
        return asset;
    }

    /** DELETED 상태의 FileAsset Fixture */
    public static FileAsset deletedFileAsset() {
        FileAsset asset =
                FileAsset.forNew(
                        UploadSessionIdFixture.defaultUploadSessionId(),
                        FileNameFixture.defaultFileName(),
                        FileSizeFixture.defaultFileSize(),
                        ContentTypeFixture.defaultContentType(),
                        FileCategory.IMAGE,
                        S3BucketFixture.defaultS3Bucket(),
                        S3KeyFixture.defaultS3Key(),
                        ETagFixture.defaultETag(),
                        1000L,
                        1L,
                        1L,
                        ClockFixture.defaultClock());
        asset.delete();
        return asset;
    }

    /** 영속화된 FileAsset Fixture (reconstitute) */
    public static FileAsset existingFileAsset() {
        return FileAsset.reconstitute(
                FileAssetIdFixture.fixedFileAssetId(),
                UploadSessionIdFixture.fixedUploadSessionId(),
                FileNameFixture.defaultFileName(),
                FileSizeFixture.defaultFileSize(),
                ContentTypeFixture.defaultContentType(),
                FileCategory.IMAGE,
                S3BucketFixture.defaultS3Bucket(),
                S3KeyFixture.defaultS3Key(),
                ETagFixture.defaultETag(),
                1000L,
                1L,
                1L,
                FileAssetStatus.COMPLETED,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now(),
                null,
                ClockFixture.defaultClock());
    }

    /** Custom FileAsset Fixture */
    public static FileAsset customFileAsset(
            UploadSessionId sessionId,
            FileName fileName,
            FileSize fileSize,
            ContentType contentType,
            FileCategory category,
            S3Bucket bucket,
            S3Key s3Key,
            ETag etag,
            Long userId,
            Long organizationId,
            Long tenantId,
            Clock clock) {
        return FileAsset.forNew(
                sessionId,
                fileName,
                fileSize,
                contentType,
                category,
                bucket,
                s3Key,
                etag,
                userId,
                organizationId,
                tenantId,
                clock);
    }
}
