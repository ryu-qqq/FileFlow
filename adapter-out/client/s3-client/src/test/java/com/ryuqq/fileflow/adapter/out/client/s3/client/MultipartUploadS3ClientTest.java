package com.ryuqq.fileflow.adapter.out.client.s3.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.ryuqq.fileflow.adapter.out.client.s3.config.S3ClientProperties;
import com.ryuqq.fileflow.adapter.out.client.s3.mapper.MultipartUploadS3Mapper;
import com.ryuqq.fileflow.application.session.port.out.client.MultipartUploadClient;
import com.ryuqq.fileflow.domain.session.vo.CompletedPart;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

@Tag("unit")
@DisplayName("MultipartUploadS3Client 단위 테스트")
class MultipartUploadS3ClientTest {

    private static final String BUCKET = "test-bucket";

    private S3Client s3Client;
    private S3Presigner s3Presigner;
    private S3ClientProperties properties;
    private MultipartUploadS3Mapper mapper;
    private MultipartUploadS3Client sut;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        s3Presigner = mock(S3Presigner.class);
        properties = new S3ClientProperties(BUCKET, "ap-northeast-2", "");
        mapper = mock(MultipartUploadS3Mapper.class);
        sut = new MultipartUploadS3Client(s3Client, s3Presigner, properties, mapper);
    }

    @Nested
    @DisplayName("createMultipartUpload 메서드")
    class CreateMultipartUpload {

        @Test
        @DisplayName("성공: S3 멀티파트 업로드를 시작하고 uploadId를 반환한다")
        void shouldCreateMultipartUploadAndReturnUploadId() {
            // given
            String s3Key = "uploads/large-file.zip";
            String contentType = "application/zip";
            String expectedUploadId = "upload-id-123";

            CreateMultipartUploadResponse response =
                    CreateMultipartUploadResponse.builder().uploadId(expectedUploadId).build();
            given(s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
                    .willReturn(response);

            // when
            String result = sut.createMultipartUpload(s3Key, contentType);

            // then
            assertThat(result).isEqualTo(expectedUploadId);

            ArgumentCaptor<CreateMultipartUploadRequest> captor =
                    ArgumentCaptor.forClass(CreateMultipartUploadRequest.class);
            verify(s3Client).createMultipartUpload(captor.capture());
            assertThat(captor.getValue().bucket()).isEqualTo(BUCKET);
            assertThat(captor.getValue().key()).isEqualTo(s3Key);
            assertThat(captor.getValue().contentType()).isEqualTo(contentType);
        }
    }

    @Nested
    @DisplayName("generatePresignedPartUrl 메서드")
    class GeneratePresignedPartUrl {

        @Test
        @DisplayName("성공: 파트 업로드용 Presigned URL을 생성한다")
        void shouldGeneratePresignedPartUrl() throws Exception {
            // given
            String s3Key = "uploads/large-file.zip";
            String uploadId = "upload-id-123";
            int partNumber = 1;
            Duration ttl = Duration.ofMinutes(30);
            String expectedUrl =
                    "https://test-bucket.s3.amazonaws.com/uploads/large-file.zip?partNumber=1";

            PresignedUploadPartRequest presignedRequest = mock(PresignedUploadPartRequest.class);
            given(presignedRequest.url()).willReturn(new URL(expectedUrl));
            given(s3Presigner.presignUploadPart(any(UploadPartPresignRequest.class)))
                    .willReturn(presignedRequest);

            // when
            String result = sut.generatePresignedPartUrl(s3Key, uploadId, partNumber, ttl);

            // then
            assertThat(result).isEqualTo(expectedUrl);
            verify(s3Presigner).presignUploadPart(any(UploadPartPresignRequest.class));
        }
    }

    @Nested
    @DisplayName("completeMultipartUpload 메서드")
    class CompleteMultipartUploadTest {

        @Test
        @DisplayName("성공: 매퍼로 변환한 파트로 멀티파트 업로드를 완료한다")
        void shouldCompleteMultipartUploadWithMappedParts() {
            // given
            String s3Key = "uploads/large-file.zip";
            String uploadId = "upload-id-123";
            String expectedEtag = "\"combined-etag\"";
            Instant now = Instant.now();

            List<CompletedPart> parts =
                    List.of(
                            CompletedPart.of(1, "\"etag-1\"", 5_000_000, now),
                            CompletedPart.of(2, "\"etag-2\"", 3_000_000, now));

            List<software.amazon.awssdk.services.s3.model.CompletedPart> s3Parts =
                    List.of(
                            software.amazon.awssdk.services.s3.model.CompletedPart.builder()
                                    .partNumber(1)
                                    .eTag("\"etag-1\"")
                                    .build(),
                            software.amazon.awssdk.services.s3.model.CompletedPart.builder()
                                    .partNumber(2)
                                    .eTag("\"etag-2\"")
                                    .build());
            given(mapper.toS3Parts(parts)).willReturn(s3Parts);

            CompleteMultipartUploadResponse response =
                    CompleteMultipartUploadResponse.builder().eTag(expectedEtag).build();
            given(s3Client.completeMultipartUpload(any(CompleteMultipartUploadRequest.class)))
                    .willReturn(response);

            // when
            String result = sut.completeMultipartUpload(s3Key, uploadId, parts);

            // then
            assertThat(result).isEqualTo(expectedEtag);
            verify(mapper).toS3Parts(parts);

            ArgumentCaptor<CompleteMultipartUploadRequest> captor =
                    ArgumentCaptor.forClass(CompleteMultipartUploadRequest.class);
            verify(s3Client).completeMultipartUpload(captor.capture());

            CompleteMultipartUploadRequest captured = captor.getValue();
            assertThat(captured.bucket()).isEqualTo(BUCKET);
            assertThat(captured.key()).isEqualTo(s3Key);
            assertThat(captured.uploadId()).isEqualTo(uploadId);
            assertThat(captured.multipartUpload().parts()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("abortMultipartUpload 메서드")
    class AbortMultipartUploadTest {

        @Test
        @DisplayName("성공: 멀티파트 업로드를 중단한다")
        void shouldAbortMultipartUpload() {
            // given
            String s3Key = "uploads/large-file.zip";
            String uploadId = "upload-id-123";

            // when
            sut.abortMultipartUpload(s3Key, uploadId);

            // then
            ArgumentCaptor<AbortMultipartUploadRequest> captor =
                    ArgumentCaptor.forClass(AbortMultipartUploadRequest.class);
            verify(s3Client).abortMultipartUpload(captor.capture());

            assertThat(captor.getValue().bucket()).isEqualTo(BUCKET);
            assertThat(captor.getValue().key()).isEqualTo(s3Key);
            assertThat(captor.getValue().uploadId()).isEqualTo(uploadId);
        }

        @Test
        @DisplayName("성공: MultipartUploadClient 인터페이스를 구현한다")
        void shouldImplementMultipartUploadClient() {
            assertThat(sut).isInstanceOf(MultipartUploadClient.class);
        }
    }
}
