package com.ryuqq.fileflow.adapter.out.persistence.redis.common;

import com.ryuqq.fileflow.domain.iam.vo.UserContext;
import com.ryuqq.fileflow.domain.iam.vo.UserId;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ExpirationTime;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.session.vo.PartSize;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.S3UploadId;
import com.ryuqq.fileflow.domain.session.vo.TotalParts;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * 업로드 세션 테스트 픽스처.
 *
 * <p>테스트에서 사용할 SingleUploadSession 및 MultipartUploadSession 인스턴스를 생성합니다.
 */
public final class UploadSessionTestFixture {

    private static final Clock CLOCK = Clock.systemUTC();

    private UploadSessionTestFixture() {}

    /**
     * 테스트용 SingleUploadSession 생성.
     *
     * @param uniqueId 유니크 식별자 (키 충돌 방지용)
     * @return SingleUploadSession 인스턴스
     */
    public static SingleUploadSession createSingleUploadSession(String uniqueId) {
        return SingleUploadSession.forNew(
                IdempotencyKey.forNew(),
                createTestUserContext(),
                FileName.of("test-file-" + uniqueId + ".txt"),
                FileSize.of(1024L),
                ContentType.of("text/plain"),
                S3Bucket.of("test-bucket"),
                S3Key.of("uploads/test-file-" + uniqueId + ".txt"),
                ExpirationTime.of(Instant.now().plus(Duration.ofMinutes(15))),
                CLOCK);
    }

    /**
     * 테스트용 MultipartUploadSession 생성.
     *
     * @param uniqueId 유니크 식별자 (키 충돌 방지용)
     * @return MultipartUploadSession 인스턴스
     */
    public static MultipartUploadSession createMultipartUploadSession(String uniqueId) {
        return MultipartUploadSession.forNew(
                createTestUserContext(),
                FileName.of("test-large-file-" + uniqueId + ".zip"),
                FileSize.of(100 * 1024 * 1024L), // 100MB
                ContentType.of("application/zip"),
                S3Bucket.of("test-bucket"),
                S3Key.of("uploads/test-large-file-" + uniqueId + ".zip"),
                S3UploadId.of("test-upload-id-" + uniqueId),
                TotalParts.of(10),
                PartSize.of(10 * 1024 * 1024L), // 10MB
                ExpirationTime.of(Instant.now().plus(Duration.ofHours(24))),
                CLOCK);
    }

    private static UserContext createTestUserContext() {
        return UserContext.customer(UserId.generate());
    }
}
