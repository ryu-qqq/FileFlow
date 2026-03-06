package com.ryuqq.fileflow.adapter.out.client.http.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.ryuqq.fileflow.application.download.dto.response.RawDownloadedFile;
import com.ryuqq.fileflow.application.download.exception.PermanentDownloadFailureException;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
        @DisplayName("성공: HTTP Content-Type 헤더가 있으면 헤더 값을 우선 사용한다")
        void shouldUseHttpContentTypeHeaderWhenPresent() {
            // given
            String sourceUrl = "https://cdn.example.com/images/abc123";
            byte[] fileBytes = "fake-image-data".getBytes();

            setupExchangeMock(fileBytes, MediaType.IMAGE_JPEG);

            // when
            RawDownloadedFile result = sut.download(sourceUrl);

            // then
            assertThat(result.contentType()).isEqualTo("image/jpeg");
        }

        @Test
        @DisplayName("성공: CDN 리사이징 URL에서도 HTTP 헤더로 올바른 contentType을 감지한다")
        void shouldDetectContentTypeFromHeaderForCdnResizeUrl() {
            // given
            String sourceUrl =
                    "https://image.mustit.co.kr/lib/upload/product/fixedone/2023/11/abc.jpeg/_dims_/resize/300x300/extent/300x400";
            byte[] fileBytes = "jpeg-data".getBytes();

            setupExchangeMock(fileBytes, MediaType.IMAGE_JPEG);

            // when
            RawDownloadedFile result = sut.download(sourceUrl);

            // then
            assertThat(result.contentType()).isEqualTo("image/jpeg");
            assertThat(result.fileName()).isEqualTo("300x400");
        }

        @Test
        @DisplayName("성공: HTTP 헤더가 octet-stream이면 파일 확장자로 fallback한다")
        void shouldFallbackToExtensionWhenHeaderIsOctetStream() {
            // given
            String sourceUrl = "https://example.com/images/photo.jpg";
            byte[] fileBytes = "fake-image-data".getBytes();

            setupExchangeMock(fileBytes, MediaType.APPLICATION_OCTET_STREAM);

            // when
            RawDownloadedFile result = sut.download(sourceUrl);

            // then
            assertThat(result.contentType()).isEqualTo("image/jpeg");
            assertThat(result.fileName()).isEqualTo("photo.jpg");
        }

        @Test
        @DisplayName("성공: HTTP 헤더가 없으면 파일 확장자로 fallback한다")
        void shouldFallbackToExtensionWhenNoHeader() {
            // given
            String sourceUrl = "https://example.com/images/logo.png";
            byte[] fileBytes = "png-data".getBytes();

            setupExchangeMock(fileBytes, null);

            // when
            RawDownloadedFile result = sut.download(sourceUrl);

            // then
            assertThat(result.contentType()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("성공: HTTP 헤더도 없고 확장자도 없으면 octet-stream이 된다")
        void shouldDefaultToOctetStreamWhenNoHeaderAndNoExtension() {
            // given
            String sourceUrl = "https://example.com/files/data";
            byte[] fileBytes = "binary-data".getBytes();

            setupExchangeMock(fileBytes, null);

            // when
            RawDownloadedFile result = sut.download(sourceUrl);

            // then
            assertThat(result.contentType()).isEqualTo("application/octet-stream");
        }

        @Test
        @DisplayName("성공: 잘못된 Content-Type 헤더면 파일명 기반 감지로 대체한다")
        void shouldFallbackToFileNameWhenContentTypeHeaderIsInvalid() {
            // given
            String sourceUrl = "https://example.com/images/photo.jpg";
            byte[] fileBytes = "jpeg-data".getBytes();

            setupExchangeMockWithRawHeader(fileBytes, "binary");

            // when
            RawDownloadedFile result = sut.download(sourceUrl);

            // then
            assertThat(result.contentType()).isEqualTo("image/jpeg");
        }

        @Test
        @DisplayName("실패: 다운로드된 파일이 null이면 예외가 발생한다")
        void shouldThrowWhenDownloadedBytesAreNull() {
            // given
            String sourceUrl = "https://example.com/empty";

            setupExchangeMock(null, null);

            // when & then
            assertThatThrownBy(() -> sut.download(sourceUrl))
                    .isInstanceOf(PermanentDownloadFailureException.class)
                    .hasMessageContaining("비어있습니다");
        }

        @SuppressWarnings("unchecked")
        private void setupExchangeMock(byte[] fileBytes, MediaType contentType) {
            RestClient.RequestHeadersUriSpec<?> uriSpec =
                    mock(RestClient.RequestHeadersUriSpec.class);
            RestClient.RequestHeadersSpec<?> headersSpec =
                    mock(RestClient.RequestHeadersSpec.class);

            HttpHeaders headers = new HttpHeaders();
            if (contentType != null) {
                headers.setContentType(contentType);
            }

            ResponseEntity<byte[]> responseEntity =
                    new ResponseEntity<>(fileBytes, headers, HttpStatusCode.valueOf(200));

            given(restClient.get()).willReturn((RestClient.RequestHeadersUriSpec) uriSpec);
            given(uriSpec.uri(any(URI.class)))
                    .willReturn((RestClient.RequestHeadersSpec) headersSpec);
            given(headersSpec.exchange(any())).willReturn(responseEntity);
        }

        @SuppressWarnings("unchecked")
        private void setupExchangeMockWithRawHeader(byte[] fileBytes, String rawContentType) {
            RestClient.RequestHeadersUriSpec<?> uriSpec =
                    mock(RestClient.RequestHeadersUriSpec.class);
            RestClient.RequestHeadersSpec<?> headersSpec =
                    mock(RestClient.RequestHeadersSpec.class);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", rawContentType);

            ResponseEntity<byte[]> responseEntity =
                    new ResponseEntity<>(fileBytes, headers, HttpStatusCode.valueOf(200));

            given(restClient.get()).willReturn((RestClient.RequestHeadersUriSpec) uriSpec);
            given(uriSpec.uri(any(URI.class)))
                    .willReturn((RestClient.RequestHeadersSpec) headersSpec);
            given(headersSpec.exchange(any())).willReturn(responseEntity);
        }
    }
}
