package com.ryuqq.fileflow.domain.download.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.domain.download.exception.DownloadException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("SourceUrl VO 단위 테스트")
class SourceUrlTest {

    @Nested
    @DisplayName("생성 검증")
    class ValidationTest {

        @Test
        @DisplayName("유효한 http URL로 생성할 수 있다")
        void validHttpUrl() {
            SourceUrl url = SourceUrl.of("http://example.com/image.jpg");
            assertThat(url.value()).isEqualTo("http://example.com/image.jpg");
        }

        @Test
        @DisplayName("유효한 https URL로 생성할 수 있다")
        void validHttpsUrl() {
            SourceUrl url = SourceUrl.of("https://example.com/image.jpg");
            assertThat(url.value()).isEqualTo("https://example.com/image.jpg");
        }

        @Test
        @DisplayName("null 값이면 NullPointerException이 발생한다")
        void nullValueThrows() {
            assertThatThrownBy(() -> SourceUrl.of(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("빈 문자열이면 DownloadException이 발생한다")
        void blankValueThrows() {
            assertThatThrownBy(() -> SourceUrl.of("")).isInstanceOf(DownloadException.class);
        }

        @Test
        @DisplayName("http/https로 시작하지 않으면 DownloadException이 발생한다")
        void invalidProtocolThrows() {
            assertThatThrownBy(() -> SourceUrl.of("ftp://example.com/file"))
                    .isInstanceOf(DownloadException.class);
        }

        @Test
        @DisplayName("localhost URL은 SSRF 방어로 DownloadException이 발생한다")
        void localhostUrlThrows() {
            assertThatThrownBy(() -> SourceUrl.of("http://localhost/secret"))
                    .isInstanceOf(DownloadException.class);
        }

        @Test
        @DisplayName("127.0.0.1 URL은 SSRF 방어로 DownloadException이 발생한다")
        void loopbackIpThrows() {
            assertThatThrownBy(() -> SourceUrl.of("http://127.0.0.1/secret"))
                    .isInstanceOf(DownloadException.class);
        }

        @Test
        @DisplayName("169.254.169.254 메타데이터 URL은 SSRF 방어로 DownloadException이 발생한다")
        void cloudMetadataIpThrows() {
            assertThatThrownBy(() -> SourceUrl.of("http://169.254.169.254/latest/meta-data"))
                    .isInstanceOf(DownloadException.class);
        }

        @Test
        @DisplayName("10.x.x.x 사설 IP URL은 SSRF 방어로 DownloadException이 발생한다")
        void privateIp10Throws() {
            assertThatThrownBy(() -> SourceUrl.of("http://10.0.0.1/internal"))
                    .isInstanceOf(DownloadException.class);
        }

        @Test
        @DisplayName("192.168.x.x 사설 IP URL은 SSRF 방어로 DownloadException이 발생한다")
        void privateIp192Throws() {
            assertThatThrownBy(() -> SourceUrl.of("http://192.168.1.1/internal"))
                    .isInstanceOf(DownloadException.class);
        }

        @Test
        @DisplayName("172.16-31.x.x 사설 IP URL은 SSRF 방어로 DownloadException이 발생한다")
        void privateIp172Throws() {
            assertThatThrownBy(() -> SourceUrl.of("http://172.16.0.1/internal"))
                    .isInstanceOf(DownloadException.class);
        }
    }

    @Nested
    @DisplayName("extractExtension - URL에서 확장자 추출")
    class ExtractExtensionTest {

        @Test
        @DisplayName("일반 URL에서 확장자를 추출한다")
        void extractsFromSimpleUrl() {
            SourceUrl url = SourceUrl.of("https://example.com/files/image.jpg");
            assertThat(url.extractExtension()).isEqualTo("jpg");
        }

        @Test
        @DisplayName("쿼리스트링이 있는 URL에서 확장자를 추출한다")
        void extractsFromUrlWithQueryString() {
            SourceUrl url =
                    SourceUrl.of("https://example.com/files/image.png?width=800&height=600");
            assertThat(url.extractExtension()).isEqualTo("png");
        }

        @Test
        @DisplayName("프래그먼트가 있는 URL에서 확장자를 추출한다")
        void extractsFromUrlWithFragment() {
            SourceUrl url = SourceUrl.of("https://example.com/files/doc.pdf#page=2");
            assertThat(url.extractExtension()).isEqualTo("pdf");
        }

        @Test
        @DisplayName("대문자 확장자를 소문자로 변환한다")
        void convertsToLowerCase() {
            SourceUrl url = SourceUrl.of("https://example.com/files/IMAGE.JPG");
            assertThat(url.extractExtension()).isEqualTo("jpg");
        }

        @Test
        @DisplayName("경로에 여러 점이 있으면 마지막 확장자를 추출한다")
        void extractsLastExtension() {
            SourceUrl url = SourceUrl.of("https://example.com/files/archive.tar.gz");
            assertThat(url.extractExtension()).isEmpty();
        }

        @Test
        @DisplayName("확장자가 없는 URL에서 빈 문자열을 반환한다")
        void returnsEmptyForNoExtension() {
            SourceUrl url = SourceUrl.of("https://example.com/files/noextension");
            assertThat(url.extractExtension()).isEmpty();
        }

        @Test
        @DisplayName("경로가 슬래시로 끝나면 빈 문자열을 반환한다")
        void returnsEmptyForTrailingSlash() {
            SourceUrl url = SourceUrl.of("https://example.com/files/");
            assertThat(url.extractExtension()).isEmpty();
        }

        @Test
        @DisplayName("점으로 끝나는 파일명에서 빈 문자열을 반환한다")
        void returnsEmptyForTrailingDot() {
            SourceUrl url = SourceUrl.of("https://example.com/files/image.");
            assertThat(url.extractExtension()).isEmpty();
        }

        @Test
        @DisplayName("CDN 리사이징 URL에서 중간 segment의 확장자를 추출한다")
        void extractsFromCdnResizeUrl() {
            SourceUrl url =
                    SourceUrl.of(
                            "https://image.mustit.co.kr/lib/upload/product/fixedone/2023/11/e31df1331e851101225a5f4f58dac1c9.jpeg/_dims_/resize/300x300/extent/300x400");
            assertThat(url.extractExtension()).isEqualTo("jpeg");
        }

        @Test
        @DisplayName("CDN 리사이징 URL (png)에서 중간 segment의 확장자를 추출한다")
        void extractsFromCdnResizeUrlPng() {
            SourceUrl url = SourceUrl.of("https://cdn.example.com/images/logo.png/_resize/200x200");
            assertThat(url.extractExtension()).isEqualTo("png");
        }

        @Test
        @DisplayName("알 수 없는 확장자는 빈 문자열을 반환한다")
        void returnsEmptyForUnknownExtension() {
            SourceUrl url = SourceUrl.of("https://example.com/files/data.xyz");
            assertThat(url.extractExtension()).isEmpty();
        }
    }
}
