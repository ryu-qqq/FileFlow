package com.ryuqq.fileflow.domain.asset.fixture;

import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import com.ryuqq.fileflow.domain.asset.vo.ImageDimension;
import com.ryuqq.fileflow.domain.common.fixture.ClockFixture;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UserId;
import com.ryuqq.fileflow.domain.session.fixture.*;
import com.ryuqq.fileflow.domain.session.vo.*;
import java.time.Clock;
import java.time.Instant;

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
                null, // ImageDimension: 업로드 시점에는 알 수 없음
                S3BucketFixture.defaultS3Bucket(),
                S3KeyFixture.defaultS3Key(),
                ETagFixture.defaultETag(),
                UserId.generate(),
                OrganizationId.generate(),
                TenantId.generate(),
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
                        null, // ImageDimension: 업로드 시점에는 알 수 없음
                        S3BucketFixture.defaultS3Bucket(),
                        S3KeyFixture.defaultS3Key(),
                        ETagFixture.defaultETag(),
                        UserId.generate(),
                        OrganizationId.generate(),
                        TenantId.generate(),
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
                        null, // ImageDimension: 업로드 시점에는 알 수 없음
                        S3BucketFixture.defaultS3Bucket(),
                        S3KeyFixture.defaultS3Key(),
                        ETagFixture.defaultETag(),
                        UserId.generate(),
                        OrganizationId.generate(),
                        TenantId.generate(),
                        ClockFixture.defaultClock());
        asset.completeProcessing(ClockFixture.defaultClock());
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
                        null, // ImageDimension: 업로드 시점에는 알 수 없음
                        S3BucketFixture.defaultS3Bucket(),
                        S3KeyFixture.defaultS3Key(),
                        ETagFixture.defaultETag(),
                        UserId.generate(),
                        OrganizationId.generate(),
                        TenantId.generate(),
                        ClockFixture.defaultClock());
        asset.delete(ClockFixture.defaultClock());
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
                ImageDimension.of(1920, 1080), // 영속화된 데이터는 dimension 포함
                S3BucketFixture.defaultS3Bucket(),
                S3KeyFixture.defaultS3Key(),
                ETagFixture.defaultETag(),
                UserId.generate(),
                OrganizationId.generate(),
                TenantId.generate(),
                FileAssetStatus.COMPLETED,
                Instant.now().minusSeconds(3600),
                Instant.now(),
                null,
                null); // lastErrorMessage
    }

    /** Custom FileAsset Fixture */
    public static FileAsset customFileAsset(
            UploadSessionId sessionId,
            FileName fileName,
            FileSize fileSize,
            ContentType contentType,
            FileCategory category,
            ImageDimension dimension,
            S3Bucket bucket,
            S3Key s3Key,
            ETag etag,
            UserId userId,
            OrganizationId organizationId,
            TenantId tenantId,
            Clock clock) {
        return FileAsset.forNew(
                sessionId,
                fileName,
                fileSize,
                contentType,
                category,
                dimension,
                bucket,
                s3Key,
                etag,
                userId,
                organizationId,
                tenantId,
                clock);
    }
}
