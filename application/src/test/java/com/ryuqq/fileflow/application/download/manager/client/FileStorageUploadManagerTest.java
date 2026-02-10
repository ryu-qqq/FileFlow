package com.ryuqq.fileflow.application.download.manager.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.common.port.out.client.FileStorageUploadClient;
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
@DisplayName("FileStorageUploadManager (Download) 단위 테스트")
class FileStorageUploadManagerTest {

    @InjectMocks private FileStorageUploadManager sut;
    @Mock private FileStorageUploadClient fileStorageUploadClient;

    @Nested
    @DisplayName("upload 메서드")
    class UploadTest {

        @Test
        @DisplayName("성공: 파일 스토리지에 업로드하고 etag를 반환한다")
        void upload_Success_ReturnsEtag() {
            // given
            String bucket = "test-bucket";
            String s3Key = "downloads/image.jpg";
            byte[] data = "fake-image-data".getBytes();
            String contentType = "image/jpeg";
            String expectedEtag = "\"abc123\"";

            given(fileStorageUploadClient.upload(bucket, s3Key, data, contentType))
                    .willReturn(expectedEtag);

            // when
            String result = sut.upload(bucket, s3Key, data, contentType);

            // then
            assertThat(result).isEqualTo(expectedEtag);
            then(fileStorageUploadClient).should().upload(bucket, s3Key, data, contentType);
        }

        @Test
        @DisplayName("실패: 클라이언트 예외 시 그대로 전파한다")
        void upload_ClientThrows_PropagatesException() {
            // given
            String bucket = "test-bucket";
            String s3Key = "downloads/image.jpg";
            byte[] data = "fake-image-data".getBytes();
            String contentType = "image/jpeg";

            given(fileStorageUploadClient.upload(bucket, s3Key, data, contentType))
                    .willThrow(new RuntimeException("S3 upload failed"));

            // when & then
            assertThatThrownBy(() -> sut.upload(bucket, s3Key, data, contentType))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("S3 upload failed");
        }
    }
}
