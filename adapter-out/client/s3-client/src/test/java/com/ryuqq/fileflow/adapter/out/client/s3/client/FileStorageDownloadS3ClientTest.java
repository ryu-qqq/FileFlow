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
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Tag("unit")
@DisplayName("FileStorageDownloadS3Client 단위 테스트")
class FileStorageDownloadS3ClientTest {

    private S3Client s3Client;
    private FileStorageDownloadS3Client sut;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        sut = new FileStorageDownloadS3Client(s3Client);
    }

    @Nested
    @DisplayName("download 메서드")
    class Download {

        @Test
        @DisplayName("성공: S3에서 파일을 다운로드하고 바이트 배열을 반환한다")
        void shouldDownloadFromS3AndReturnBytes() {
            // given
            String bucket = "test-bucket";
            String s3Key = "uploads/image.jpg";
            byte[] expectedData = "fake-image-data".getBytes();

            @SuppressWarnings("unchecked")
            ResponseBytes<GetObjectResponse> responseBytes = mock(ResponseBytes.class);
            given(responseBytes.asByteArray()).willReturn(expectedData);
            given(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).willReturn(responseBytes);

            // when
            byte[] result = sut.download(bucket, s3Key);

            // then
            assertThat(result).isEqualTo(expectedData);

            ArgumentCaptor<GetObjectRequest> requestCaptor =
                    ArgumentCaptor.forClass(GetObjectRequest.class);
            verify(s3Client).getObjectAsBytes(requestCaptor.capture());

            GetObjectRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.bucket()).isEqualTo(bucket);
            assertThat(capturedRequest.key()).isEqualTo(s3Key);
        }
    }
}
