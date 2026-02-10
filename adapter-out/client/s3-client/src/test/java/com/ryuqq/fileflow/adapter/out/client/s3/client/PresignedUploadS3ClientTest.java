package com.ryuqq.fileflow.adapter.out.client.s3.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.ryuqq.fileflow.adapter.out.client.s3.config.S3ClientProperties;
import com.ryuqq.fileflow.application.session.port.out.client.PresignedUploadClient;
import java.net.URL;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Tag("unit")
@DisplayName("PresignedUploadS3Client 단위 테스트")
class PresignedUploadS3ClientTest {

    private static final String BUCKET = "test-bucket";

    private S3Presigner s3Presigner;
    private S3ClientProperties properties;
    private PresignedUploadS3Client sut;

    @BeforeEach
    void setUp() {
        s3Presigner = mock(S3Presigner.class);
        properties = new S3ClientProperties(BUCKET, "ap-northeast-2", "");
        sut = new PresignedUploadS3Client(s3Presigner, properties);
    }

    @Nested
    @DisplayName("getBucket 메서드")
    class GetBucket {

        @Test
        @DisplayName("성공: Properties에서 설정된 버킷명을 반환한다")
        void shouldReturnConfiguredBucket() {
            assertThat(sut.getBucket()).isEqualTo(BUCKET);
        }
    }

    @Nested
    @DisplayName("generatePresignedUploadUrl 메서드")
    class GeneratePresignedUploadUrl {

        @Test
        @DisplayName("성공: S3 Presigned PUT URL을 생성한다")
        void shouldGeneratePresignedPutUrl() throws Exception {
            // given
            String s3Key = "uploads/photo.jpg";
            String contentType = "image/jpeg";
            Duration ttl = Duration.ofMinutes(15);
            String expectedUrl =
                    "https://test-bucket.s3.amazonaws.com/uploads/photo.jpg?X-Amz-Signature=abc";

            PresignedPutObjectRequest presignedRequest = mock(PresignedPutObjectRequest.class);
            given(presignedRequest.url()).willReturn(new URL(expectedUrl));
            given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                    .willReturn(presignedRequest);

            // when
            String result = sut.generatePresignedUploadUrl(s3Key, contentType, ttl);

            // then
            assertThat(result).isEqualTo(expectedUrl);

            ArgumentCaptor<PutObjectPresignRequest> captor =
                    ArgumentCaptor.forClass(PutObjectPresignRequest.class);
            verify(s3Presigner).presignPutObject(captor.capture());
            assertThat(captor.getValue().signatureDuration()).isEqualTo(ttl);
        }

        @Test
        @DisplayName("성공: PresignedUploadClient 인터페이스를 구현한다")
        void shouldImplementPresignedUploadClient() {
            assertThat(sut).isInstanceOf(PresignedUploadClient.class);
        }
    }
}
