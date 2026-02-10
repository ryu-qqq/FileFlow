package com.ryuqq.fileflow.adapter.out.client.s3.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Tag("unit")
@DisplayName("FileStorageUploadS3Client 단위 테스트")
class FileStorageUploadS3ClientTest {

    private S3Client s3Client;
    private FileStorageUploadS3Client sut;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        sut = new FileStorageUploadS3Client(s3Client);
    }

    @Nested
    @DisplayName("upload 메서드")
    class Upload {

        @Test
        @DisplayName("성공: S3에 파일을 업로드하고 etag를 반환한다")
        void shouldUploadToS3AndReturnEtag() {
            // given
            String bucket = "test-bucket";
            String s3Key = "downloads/image.jpg";
            byte[] data = "fake-image-data".getBytes();
            String contentType = "image/jpeg";
            String expectedEtag = "\"abc123\"";

            PutObjectResponse putResponse = PutObjectResponse.builder().eTag(expectedEtag).build();
            given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .willReturn(putResponse);

            // when
            String result = sut.upload(bucket, s3Key, data, contentType);

            // then
            assertThat(result).isEqualTo(expectedEtag);

            ArgumentCaptor<PutObjectRequest> requestCaptor =
                    ArgumentCaptor.forClass(PutObjectRequest.class);
            verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

            PutObjectRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.bucket()).isEqualTo(bucket);
            assertThat(capturedRequest.key()).isEqualTo(s3Key);
            assertThat(capturedRequest.contentType()).isEqualTo(contentType);
            assertThat(capturedRequest.contentLength()).isEqualTo(data.length);
        }
    }
}
