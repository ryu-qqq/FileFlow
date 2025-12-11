package com.ryuqq.fileflow.domain.session.fixture;

import com.ryuqq.fileflow.domain.common.fixture.ClockFixture;
import com.ryuqq.fileflow.domain.iam.fixture.UserContextFixture;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.vo.*;
import java.time.Clock;
import java.time.Instant;

/**
 * MultipartUploadSession Aggregate Test Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public class MultipartUploadSessionFixture {

    private MultipartUploadSessionFixture() {
        throw new AssertionError("Utility class");
    }

    /** 기본 MultipartUploadSession Fixture (PREPARING 상태, 신규 생성) */
    public static MultipartUploadSession defaultMultipartUploadSession() {
        return MultipartUploadSession.forNew(
                UserContextFixture.defaultAdminUserContext(),
                FileNameFixture.defaultFileName(),
                FileSizeFixture.largeFileSize(),
                ContentTypeFixture.defaultContentType(),
                S3BucketFixture.defaultS3Bucket(),
                S3KeyFixture.defaultS3Key(),
                S3UploadIdFixture.defaultS3UploadId(),
                TotalPartsFixture.defaultTotalParts(),
                PartSizeFixture.defaultPartSize(),
                ExpirationTimeFixture.multipartExpirationTime(),
                ClockFixture.defaultClock());
    }

    /** ACTIVE 상태의 MultipartUploadSession Fixture */
    public static MultipartUploadSession activeMultipartUploadSession() {
        MultipartUploadSession session =
                MultipartUploadSession.forNew(
                        UserContextFixture.defaultAdminUserContext(),
                        FileNameFixture.defaultFileName(),
                        FileSizeFixture.largeFileSize(),
                        ContentTypeFixture.defaultContentType(),
                        S3BucketFixture.defaultS3Bucket(),
                        S3KeyFixture.defaultS3Key(),
                        S3UploadIdFixture.defaultS3UploadId(),
                        TotalPartsFixture.defaultTotalParts(),
                        PartSizeFixture.defaultPartSize(),
                        ExpirationTimeFixture.multipartExpirationTime(),
                        ClockFixture.defaultClock());
        session.activate();
        return session;
    }

    /** 영속화된 MultipartUploadSession Fixture (reconstitute) */
    public static MultipartUploadSession existingMultipartUploadSession() {
        return MultipartUploadSession.reconstitute(
                UploadSessionIdFixture.fixedUploadSessionId(),
                UserContextFixture.defaultAdminUserContext(),
                FileNameFixture.defaultFileName(),
                FileSizeFixture.largeFileSize(),
                ContentTypeFixture.defaultContentType(),
                S3BucketFixture.defaultS3Bucket(),
                S3KeyFixture.defaultS3Key(),
                S3UploadIdFixture.defaultS3UploadId(),
                TotalPartsFixture.defaultTotalParts(),
                PartSizeFixture.defaultPartSize(),
                ExpirationTimeFixture.multipartExpirationTime(),
                Instant.now().minusSeconds(3600),
                SessionStatus.ACTIVE,
                null,
                1L);
    }

    /** Custom MultipartUploadSession Fixture */
    public static MultipartUploadSession customMultipartUploadSession(
            com.ryuqq.fileflow.domain.iam.vo.UserContext userContext,
            FileName fileName,
            FileSize fileSize,
            ContentType contentType,
            S3Bucket bucket,
            S3Key s3Key,
            S3UploadId s3UploadId,
            TotalParts totalParts,
            PartSize partSize,
            ExpirationTime expirationTime,
            Clock clock) {
        return MultipartUploadSession.forNew(
                userContext,
                fileName,
                fileSize,
                contentType,
                bucket,
                s3Key,
                s3UploadId,
                totalParts,
                partSize,
                expirationTime,
                clock);
    }
}
