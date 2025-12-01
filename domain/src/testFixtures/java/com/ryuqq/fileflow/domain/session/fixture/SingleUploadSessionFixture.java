package com.ryuqq.fileflow.domain.session.fixture;

import com.ryuqq.fileflow.domain.common.fixture.ClockFixture;
import com.ryuqq.fileflow.domain.iam.fixture.UserContextFixture;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.vo.*;
import java.time.Clock;
import java.time.LocalDateTime;

/**
 * SingleUploadSession Aggregate Test Fixture
 *
 * @author development-team
 * @since 1.0.0
 */
public class SingleUploadSessionFixture {

    private SingleUploadSessionFixture() {
        throw new AssertionError("Utility class");
    }

    /** 기본 SingleUploadSession Fixture (PREPARING 상태, 신규 생성) */
    public static SingleUploadSession defaultSingleUploadSession() {
        return SingleUploadSession.forNew(
                IdempotencyKeyFixture.defaultIdempotencyKey(),
                UserContextFixture.defaultAdminUserContext(),
                FileNameFixture.defaultFileName(),
                FileSizeFixture.defaultFileSize(),
                ContentTypeFixture.defaultContentType(),
                S3BucketFixture.defaultS3Bucket(),
                S3KeyFixture.defaultS3Key(),
                ExpirationTimeFixture.defaultExpirationTime(),
                ClockFixture.defaultClock());
    }

    /** ACTIVE 상태의 SingleUploadSession Fixture */
    public static SingleUploadSession activeSingleUploadSession() {
        SingleUploadSession session =
                SingleUploadSession.forNew(
                        IdempotencyKeyFixture.defaultIdempotencyKey(),
                        UserContextFixture.defaultAdminUserContext(),
                        FileNameFixture.defaultFileName(),
                        FileSizeFixture.defaultFileSize(),
                        ContentTypeFixture.defaultContentType(),
                        S3BucketFixture.defaultS3Bucket(),
                        S3KeyFixture.defaultS3Key(),
                        ExpirationTimeFixture.defaultExpirationTime(),
                        ClockFixture.defaultClock());
        session.activate(PresignedUrlFixture.defaultPresignedUrl());
        return session;
    }

    /** COMPLETED 상태의 SingleUploadSession Fixture */
    public static SingleUploadSession completedSingleUploadSession() {
        SingleUploadSession session =
                SingleUploadSession.forNew(
                        IdempotencyKeyFixture.defaultIdempotencyKey(),
                        UserContextFixture.defaultAdminUserContext(),
                        FileNameFixture.defaultFileName(),
                        FileSizeFixture.defaultFileSize(),
                        ContentTypeFixture.defaultContentType(),
                        S3BucketFixture.defaultS3Bucket(),
                        S3KeyFixture.defaultS3Key(),
                        ExpirationTimeFixture.defaultExpirationTime(),
                        ClockFixture.defaultClock());
        session.activate(PresignedUrlFixture.defaultPresignedUrl());
        ETag etag = ETagFixture.defaultETag();
        session.complete(etag, etag);
        return session;
    }

    /** 영속화된 SingleUploadSession Fixture (reconstitute) */
    public static SingleUploadSession existingSingleUploadSession() {
        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(5);
        return SingleUploadSession.reconstitute(
                UploadSessionIdFixture.fixedUploadSessionId(),
                IdempotencyKeyFixture.fixedIdempotencyKey(),
                UserContextFixture.defaultAdminUserContext(),
                FileNameFixture.defaultFileName(),
                FileSizeFixture.defaultFileSize(),
                ContentTypeFixture.defaultContentType(),
                S3BucketFixture.defaultS3Bucket(),
                S3KeyFixture.defaultS3Key(),
                ExpirationTimeFixture.defaultExpirationTime(),
                createdAt,
                SessionStatus.ACTIVE,
                PresignedUrlFixture.defaultPresignedUrl(),
                null,
                null,
                createdAt, // updatedAt
                1L,
                ClockFixture.defaultClock());
    }

    /** Custom SingleUploadSession Fixture */
    public static SingleUploadSession customSingleUploadSession(
            IdempotencyKey idempotencyKey,
            com.ryuqq.fileflow.domain.iam.vo.UserContext userContext,
            FileName fileName,
            FileSize fileSize,
            ContentType contentType,
            S3Bucket bucket,
            S3Key s3Key,
            ExpirationTime expirationTime,
            Clock clock) {
        return SingleUploadSession.forNew(
                idempotencyKey,
                userContext,
                fileName,
                fileSize,
                contentType,
                bucket,
                s3Key,
                expirationTime,
                clock);
    }
}
