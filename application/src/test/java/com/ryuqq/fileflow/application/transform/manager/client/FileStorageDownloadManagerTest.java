package com.ryuqq.fileflow.application.transform.manager.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.common.port.out.client.FileStorageDownloadClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("FileStorageDownloadManager (Transform) 단위 테스트")
class FileStorageDownloadManagerTest {

    @InjectMocks private FileStorageDownloadManager sut;
    @Mock private FileStorageDownloadClient fileStorageDownloadClient;

    @Nested
    @DisplayName("download 메서드")
    class DownloadTest {

        @Test
        @DisplayName("성공: 파일 스토리지에서 다운로드하고 바이트 배열을 반환한다")
        void download_Success_ReturnsBytes() {
            // given
            String bucket = "test-bucket";
            String s3Key = "uploads/image.jpg";
            byte[] expectedData = "fake-image-data".getBytes();

            given(fileStorageDownloadClient.download(bucket, s3Key)).willReturn(expectedData);

            // when
            byte[] result = sut.download(bucket, s3Key);

            // then
            assertThat(result).isEqualTo(expectedData);
            then(fileStorageDownloadClient).should().download(bucket, s3Key);
        }

        @Test
        @DisplayName("실패: 클라이언트 예외 시 그대로 전파한다")
        void download_ClientThrows_PropagatesException() {
            // given
            String bucket = "test-bucket";
            String s3Key = "uploads/image.jpg";

            given(fileStorageDownloadClient.download(bucket, s3Key))
                    .willThrow(new RuntimeException("S3 download failed"));

            // when & then
            assertThatThrownBy(() -> sut.download(bucket, s3Key))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("S3 download failed");
        }
    }
}
