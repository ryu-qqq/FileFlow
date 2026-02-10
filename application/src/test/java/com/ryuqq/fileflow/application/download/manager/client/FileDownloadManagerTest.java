package com.ryuqq.fileflow.application.download.manager.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.download.dto.response.RawDownloadedFile;
import com.ryuqq.fileflow.application.download.port.out.client.FileDownloadClient;
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
@DisplayName("FileDownloadManager 단위 테스트")
class FileDownloadManagerTest {

    @InjectMocks private FileDownloadManager sut;
    @Mock private FileDownloadClient fileDownloadClient;

    @Nested
    @DisplayName("download 메서드")
    class DownloadTest {

        @Test
        @DisplayName("성공: 외부 URL에서 파일을 다운로드하여 RawDownloadedFile을 반환한다")
        void download_Success_ReturnsRawDownloadedFile() {
            // given
            String sourceUrl = "https://example.com/images/image.jpg";
            byte[] data = "fake-image-data".getBytes();
            RawDownloadedFile expected = RawDownloadedFile.of("image.jpg", "image/jpeg", data);

            given(fileDownloadClient.download(sourceUrl)).willReturn(expected);

            // when
            RawDownloadedFile result = sut.download(sourceUrl);

            // then
            assertThat(result.fileName()).isEqualTo("image.jpg");
            assertThat(result.contentType()).isEqualTo("image/jpeg");
            assertThat(result.fileSize()).isEqualTo(data.length);
            assertThat(result.data()).isEqualTo(data);
            then(fileDownloadClient).should().download(sourceUrl);
        }

        @Test
        @DisplayName("실패: 클라이언트 예외 시 그대로 전파한다")
        void download_ClientThrows_PropagatesException() {
            // given
            String sourceUrl = "https://example.com/images/image.jpg";

            given(fileDownloadClient.download(sourceUrl))
                    .willThrow(new RuntimeException("Connection timeout"));

            // when & then
            assertThatThrownBy(() -> sut.download(sourceUrl))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Connection timeout");
        }
    }
}
