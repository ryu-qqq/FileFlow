package com.ryuqq.fileflow.application.session.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.ryuqq.fileflow.application.session.manager.UploadSessionManager;
import com.ryuqq.fileflow.application.session.port.out.client.S3ClientPort;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.fixture.MultipartUploadSessionFixture;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("MultipartUploadExpireStrategy 단위 테스트")
@ExtendWith(MockitoExtension.class)
class MultipartUploadExpireStrategyTest {

    @Mock private UploadSessionManager uploadSessionManager;
    @Mock private S3ClientPort s3ClientPort;

    @InjectMocks private MultipartUploadExpireStrategy multipartUploadExpireStrategy;

    @Nested
    @DisplayName("expire")
    class Expire {

        @Test
        @DisplayName("첫 번째 ACTIVE 상태의 세션을 만료 처리하고 S3 Part를 정리한다")
        void expire_WhenFirstActiveSession_ShouldExpireAndCleanupS3Parts() {
            // given
            MultipartUploadSession session =
                    MultipartUploadSessionFixture.activeMultipartUploadSession();
            assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);

            S3Bucket bucket = session.getBucket();
            S3Key s3Key = session.getS3Key();
            String s3UploadId = session.getS3UploadIdValue();

            when(uploadSessionManager.save(session)).thenReturn(session);

            // when
            multipartUploadExpireStrategy.expire(session);

            // then
            assertThat(session.getStatus()).isEqualTo(SessionStatus.EXPIRED);
            verify(s3ClientPort).abortMultipartUpload(bucket, s3Key, s3UploadId);
            verify(uploadSessionManager).save(session);
        }

        @Test
        @DisplayName("ACTIVE 상태의 세션을 만료 처리하고 S3 Part를 정리한다")
        void expire_WhenActiveSession_ShouldExpireAndCleanupS3Parts() {
            // given
            MultipartUploadSession session =
                    MultipartUploadSessionFixture.activeMultipartUploadSession();
            assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);

            S3Bucket bucket = session.getBucket();
            S3Key s3Key = session.getS3Key();
            String s3UploadId = session.getS3UploadIdValue();

            when(uploadSessionManager.save(session)).thenReturn(session);

            // when
            multipartUploadExpireStrategy.expire(session);

            // then
            assertThat(session.getStatus()).isEqualTo(SessionStatus.EXPIRED);
            verify(s3ClientPort).abortMultipartUpload(bucket, s3Key, s3UploadId);
            verify(uploadSessionManager).save(session);
        }

        @Test
        @DisplayName("도메인 만료 처리 후 S3 정리, 저장 순서로 실행된다")
        void expire_ShouldExecuteInCorrectOrder() {
            // given
            MultipartUploadSession session =
                    spy(MultipartUploadSessionFixture.activeMultipartUploadSession());

            S3Bucket bucket = session.getBucket();
            S3Key s3Key = session.getS3Key();
            String s3UploadId = session.getS3UploadIdValue();

            when(uploadSessionManager.save(session)).thenReturn(session);

            // when
            multipartUploadExpireStrategy.expire(session);

            // then
            InOrder inOrder = inOrder(session, s3ClientPort, uploadSessionManager);
            inOrder.verify(session).expire();
            inOrder.verify(s3ClientPort).abortMultipartUpload(bucket, s3Key, s3UploadId);
            inOrder.verify(uploadSessionManager).save(session);
        }

        @Test
        @DisplayName("올바른 S3 버킷, 키, 업로드 ID로 AbortMultipartUpload를 호출한다")
        void expire_ShouldCallAbortMultipartUploadWithCorrectParameters() {
            // given
            MultipartUploadSession session =
                    MultipartUploadSessionFixture.activeMultipartUploadSession();

            S3Bucket expectedBucket = session.getBucket();
            S3Key expectedS3Key = session.getS3Key();
            String expectedS3UploadId = session.getS3UploadIdValue();

            when(uploadSessionManager.save(session)).thenReturn(session);

            // when
            multipartUploadExpireStrategy.expire(session);

            // then
            verify(s3ClientPort)
                    .abortMultipartUpload(expectedBucket, expectedS3Key, expectedS3UploadId);
        }
    }
}
