package com.ryuqq.fileflow.adapter.out.client.http.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.ryuqq.fileflow.application.download.dto.response.RawDownloadedFile;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@Tag("unit")
@DisplayName("FileDownloadHttpClient 단위 테스트")
class FileDownloadHttpClientTest {

    private RestClient restClient;
    private FileDownloadHttpClient sut;

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class);
        sut = new FileDownloadHttpClient(restClient);
    }

    @Nested
    @DisplayName("download 메서드")
    class Download {

        @Test
        @DisplayName("성공: 외부 URL에서 파일을 다운로드하여 RawDownloadedFile을 반환한다")
        void shouldDownloadAndReturnRawFile() {
            // given
            String sourceUrl = "https://example.com/images/photo.jpg";
            byte[] fileBytes = "fake-image-data".getBytes();

            setupRestClientMock(sourceUrl, fileBytes);

            // when
            RawDownloadedFile result = sut.download(sourceUrl);

            // then
            assertThat(result.fileName()).isEqualTo("photo.jpg");
            assertThat(result.contentType()).isEqualTo("image/jpeg");
            assertThat(result.fileSize()).isEqualTo(fileBytes.length);
            assertThat(result.data()).isEqualTo(fileBytes);
        }

        @Test
        @DisplayName("성공: PNG 파일의 Content-Type이 올바르게 감지된다")
        void shouldDetectPngContentType() {
            // given
            String sourceUrl = "https://example.com/images/logo.png";
            byte[] fileBytes = "png-data".getBytes();

            setupRestClientMock(sourceUrl, fileBytes);

            // when
            RawDownloadedFile result = sut.download(sourceUrl);

            // then
            assertThat(result.contentType()).isEqualTo("image/png");
            assertThat(result.fileName()).isEqualTo("logo.png");
        }

        @Test
        @DisplayName("성공: 확장자 없는 파일은 application/octet-stream으로 처리된다")
        void shouldDefaultToOctetStreamForUnknownExtension() {
            // given
            String sourceUrl = "https://example.com/files/data";
            byte[] fileBytes = "binary-data".getBytes();

            setupRestClientMock(sourceUrl, fileBytes);

            // when
            RawDownloadedFile result = sut.download(sourceUrl);

            // then
            assertThat(result.contentType()).isEqualTo("application/octet-stream");
        }

        @Test
        @DisplayName("실패: 다운로드된 파일이 null이면 예외가 발생한다")
        void shouldThrowWhenDownloadedBytesAreNull() {
            // given
            String sourceUrl = "https://example.com/empty";

            setupRestClientMock(sourceUrl, null);

            // when & then
            assertThatThrownBy(() -> sut.download(sourceUrl))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("비어있습니다");
        }

        private void setupRestClientMock(String sourceUrl, byte[] fileBytes) {
            RestClient.RequestHeadersUriSpec<?> uriSpec =
                    mock(RestClient.RequestHeadersUriSpec.class);
            RestClient.RequestHeadersSpec<?> headersSpec =
                    mock(RestClient.RequestHeadersSpec.class);
            RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

            given(restClient.get()).willReturn((RestClient.RequestHeadersUriSpec) uriSpec);
            given(uriSpec.uri(any(URI.class)))
                    .willReturn((RestClient.RequestHeadersSpec) headersSpec);
            given(headersSpec.retrieve()).willReturn(responseSpec);
            given(responseSpec.body(byte[].class)).willReturn(fileBytes);
        }
    }
}
