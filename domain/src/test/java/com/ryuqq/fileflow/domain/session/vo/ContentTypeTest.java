package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.session.exception.UnsupportedFileTypeException;
import com.ryuqq.fileflow.domain.session.fixture.ContentTypeFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ContentType 단위 테스트")
class ContentTypeTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("허용된 MIME 타입으로 생성할 수 있다")
        void of_WithAllowedMimeType_ShouldCreateContentType() {
            // given
            String[] allowedTypes = {"image/jpeg", "image/png", "video/mp4", "application/pdf"};

            // when & then
            for (String allowedType : allowedTypes) {
                ContentType contentType = ContentType.of(allowedType);
                assertThat(contentType.type()).isEqualTo(allowedType);
            }
        }

        @Test
        @DisplayName("null 타입으로 생성 시 예외가 발생한다")
        void of_WithNull_ShouldThrowException() {
            // given
            String nullType = null;

            // when & then
            assertThatThrownBy(() -> ContentType.of(nullType))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("빈 문자열 타입으로 생성 시 예외가 발생한다")
        void of_WithEmptyString_ShouldThrowException() {
            // given
            String emptyType = "";

            // when & then
            assertThatThrownBy(() -> ContentType.of(emptyType))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("허용되지 않는 타입으로 생성 시 예외가 발생한다")
        void of_WithUnsupportedType_ShouldThrowException() {
            // given
            String unsupportedType = "application/x-malware";

            // when & then
            assertThatThrownBy(() -> ContentType.of(unsupportedType))
                    .isInstanceOf(UnsupportedFileTypeException.class);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 타입을 가진 ContentType은 동등하다")
        void equals_WithSameType_ShouldBeEqual() {
            // given
            String type = "image/jpeg";
            ContentType contentType1 = ContentType.of(type);
            ContentType contentType2 = ContentType.of(type);

            // when & then
            assertThat(contentType1).isEqualTo(contentType2);
            assertThat(contentType1.hashCode()).isEqualTo(contentType2.hashCode());
        }

        @Test
        @DisplayName("다른 타입을 가진 ContentType은 동등하지 않다")
        void equals_WithDifferentType_ShouldNotBeEqual() {
            // given
            ContentType contentType1 = ContentType.of("image/jpeg");
            ContentType contentType2 = ContentType.of("image/png");

            // when & then
            assertThat(contentType1).isNotEqualTo(contentType2);
        }
    }

    @Nested
    @DisplayName("fromExtension 테스트")
    class FromExtensionTest {

        @Test
        @DisplayName("유효한 확장자로 ContentType을 생성할 수 있다")
        void fromExtension_WithValidExtension_ShouldCreate() {
            // given & when & then
            assertThat(ContentType.fromExtension("jpg").type()).isEqualTo("image/jpeg");
            assertThat(ContentType.fromExtension("jpeg").type()).isEqualTo("image/jpeg");
            assertThat(ContentType.fromExtension("png").type()).isEqualTo("image/png");
            assertThat(ContentType.fromExtension("pdf").type()).isEqualTo("application/pdf");
            assertThat(ContentType.fromExtension("mp4").type()).isEqualTo("video/mp4");
        }

        @Test
        @DisplayName("대문자 확장자도 처리할 수 있다")
        void fromExtension_WithUpperCase_ShouldCreate() {
            // given & when & then
            assertThat(ContentType.fromExtension("JPG").type()).isEqualTo("image/jpeg");
            assertThat(ContentType.fromExtension("PNG").type()).isEqualTo("image/png");
        }

        @Test
        @DisplayName("지원하지 않는 확장자는 예외가 발생한다")
        void fromExtension_WithUnsupported_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> ContentType.fromExtension("xyz"))
                    .isInstanceOf(UnsupportedFileTypeException.class);
        }

        @Test
        @DisplayName("null 확장자는 예외가 발생한다")
        void fromExtension_WithNull_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> ContentType.fromExtension(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("빈 문자열 확장자는 예외가 발생한다")
        void fromExtension_WithBlank_ShouldThrowException() {
            // given & when & then
            assertThatThrownBy(() -> ContentType.fromExtension(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("matchesExtension 테스트")
    class MatchesExtensionTest {

        @Test
        @DisplayName("확장자와 Content-Type이 일치하면 true를 반환한다")
        void matchesExtension_WithMatching_ShouldReturnTrue() {
            // given
            ContentType jpeg = ContentType.of("image/jpeg");
            ContentType png = ContentType.of("image/png");

            // when & then
            assertThat(jpeg.matchesExtension("jpg")).isTrue();
            assertThat(jpeg.matchesExtension("jpeg")).isTrue();
            assertThat(png.matchesExtension("png")).isTrue();
        }

        @Test
        @DisplayName("확장자와 Content-Type이 불일치하면 false를 반환한다")
        void matchesExtension_WithNonMatching_ShouldReturnFalse() {
            // given
            ContentType jpeg = ContentType.of("image/jpeg");

            // when & then
            assertThat(jpeg.matchesExtension("png")).isFalse();
            assertThat(jpeg.matchesExtension("gif")).isFalse();
        }

        @Test
        @DisplayName("null이나 빈 확장자는 false를 반환한다")
        void matchesExtension_WithNullOrBlank_ShouldReturnFalse() {
            // given
            ContentType jpeg = ContentType.of("image/jpeg");

            // when & then
            assertThat(jpeg.matchesExtension(null)).isFalse();
            assertThat(jpeg.matchesExtension("")).isFalse();
            assertThat(jpeg.matchesExtension("   ")).isFalse();
        }
    }

    @Nested
    @DisplayName("타입 체크 테스트")
    class TypeCheckTest {

        @Test
        @DisplayName("이미지 타입 체크가 정상 동작한다")
        void isImage_ShouldWork() {
            // given & when & then
            assertThat(ContentType.of("image/jpeg").isImage()).isTrue();
            assertThat(ContentType.of("image/png").isImage()).isTrue();
            assertThat(ContentType.of("video/mp4").isImage()).isFalse();
        }

        @Test
        @DisplayName("비디오 타입 체크가 정상 동작한다")
        void isVideo_ShouldWork() {
            // given & when & then
            assertThat(ContentType.of("video/mp4").isVideo()).isTrue();
            assertThat(ContentType.of("video/mpeg").isVideo()).isTrue();
            assertThat(ContentType.of("image/jpeg").isVideo()).isFalse();
        }

        @Test
        @DisplayName("오디오 타입 체크가 정상 동작한다")
        void isAudio_ShouldWork() {
            // given & when & then
            assertThat(ContentType.of("audio/mpeg").isAudio()).isTrue();
            assertThat(ContentType.of("audio/wav").isAudio()).isTrue();
            assertThat(ContentType.of("image/jpeg").isAudio()).isFalse();
        }

        @Test
        @DisplayName("문서 타입 체크가 정상 동작한다")
        void isDocument_ShouldWork() {
            // given & when & then
            assertThat(ContentType.of("application/pdf").isDocument()).isTrue();
            assertThat(ContentType.of("text/plain").isDocument()).isTrue();
            assertThat(ContentType.of("image/jpeg").isDocument()).isFalse();
        }

        @Test
        @DisplayName("압축파일 타입 체크가 정상 동작한다")
        void isArchive_ShouldWork() {
            // given & when & then
            assertThat(ContentType.of("application/zip").isArchive()).isTrue();
            assertThat(ContentType.of("image/jpeg").isArchive()).isFalse();
        }

        @Test
        @DisplayName("카테고리를 올바르게 반환한다")
        void getCategory_ShouldWork() {
            // given & when & then
            assertThat(ContentType.of("image/jpeg").getCategory()).isEqualTo("image");
            assertThat(ContentType.of("video/mp4").getCategory()).isEqualTo("video");
            assertThat(ContentType.of("audio/mpeg").getCategory()).isEqualTo("audio");
            assertThat(ContentType.of("application/pdf").getCategory()).isEqualTo("document");
            assertThat(ContentType.of("application/zip").getCategory()).isEqualTo("archive");
            assertThat(ContentType.of("application/octet-stream").getCategory()).isEqualTo("other");
        }
    }

    @Nested
    @DisplayName("HTML/XHTML 타입 테스트")
    class HtmlTypeTest {

        @Test
        @DisplayName("text/html MIME 타입을 인식할 수 있다")
        void shouldRecognizeHtmlMimeType() {
            // given
            String htmlMimeType = "text/html";

            // when
            ContentType contentType = ContentType.of(htmlMimeType);

            // then
            assertThat(contentType.type()).isEqualTo(htmlMimeType);
        }

        @Test
        @DisplayName("application/xhtml+xml MIME 타입을 인식할 수 있다")
        void shouldRecognizeXhtmlMimeType() {
            // given
            String xhtmlMimeType = "application/xhtml+xml";

            // when
            ContentType contentType = ContentType.of(xhtmlMimeType);

            // then
            assertThat(contentType.type()).isEqualTo(xhtmlMimeType);
        }

        @Test
        @DisplayName("html, htm, xhtml 확장자를 MIME 타입으로 매핑할 수 있다")
        void shouldMapHtmlExtensionToMimeType() {
            // when & then
            assertThat(ContentType.fromExtension("html").type()).isEqualTo("text/html");
            assertThat(ContentType.fromExtension("htm").type()).isEqualTo("text/html");
            assertThat(ContentType.fromExtension("xhtml").type())
                    .isEqualTo("application/xhtml+xml");
        }

        @Test
        @DisplayName("text/html 타입은 isHtml()이 true를 반환한다")
        void shouldReturnTrueForHtmlContentType() {
            // given
            ContentType htmlType = ContentType.of("text/html");

            // when & then
            assertThat(htmlType.isHtml()).isTrue();
        }

        @Test
        @DisplayName("application/xhtml+xml 타입은 isHtml()이 true를 반환한다")
        void shouldReturnTrueForXhtmlContentType() {
            // given
            ContentType xhtmlType = ContentType.of("application/xhtml+xml");

            // when & then
            assertThat(xhtmlType.isHtml()).isTrue();
        }

        @Test
        @DisplayName("HTML이 아닌 타입은 isHtml()이 false를 반환한다")
        void shouldReturnFalseForNonHtmlContentType() {
            // given & when & then
            assertThat(ContentType.of("image/jpeg").isHtml()).isFalse();
            assertThat(ContentType.of("application/pdf").isHtml()).isFalse();
            assertThat(ContentType.of("video/mp4").isHtml()).isFalse();
        }
    }

    @Nested
    @DisplayName("Excel 타입 테스트")
    class ExcelTypeTest {

        @Test
        @DisplayName("application/vnd.ms-excel(xls) 타입은 isExcel()이 true를 반환한다")
        void shouldReturnTrueForXlsContentType() {
            // given
            ContentType xlsType = ContentType.of("application/vnd.ms-excel");

            // when & then
            assertThat(xlsType.isExcel()).isTrue();
        }

        @Test
        @DisplayName(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet(xlsx) 타입은"
                        + " isExcel()이 true를 반환한다")
        void shouldReturnTrueForXlsxContentType() {
            // given
            ContentType xlsxType =
                    ContentType.of(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            // when & then
            assertThat(xlsxType.isExcel()).isTrue();
        }

        @Test
        @DisplayName("Excel이 아닌 타입은 isExcel()이 false를 반환한다")
        void shouldReturnFalseForNonExcelContentType() {
            // given & when & then
            assertThat(ContentType.of("image/jpeg").isExcel()).isFalse();
            assertThat(ContentType.of("application/pdf").isExcel()).isFalse();
            assertThat(ContentType.of("text/html").isExcel()).isFalse();
        }
    }

    @Nested
    @DisplayName("Fixture 테스트")
    class FixtureTest {

        @Test
        @DisplayName("Fixture로 생성된 ContentType이 정상 동작한다")
        void fixture_ShouldWorkCorrectly() {
            // given & when
            ContentType defaultType = ContentTypeFixture.defaultContentType();
            ContentType pngType = ContentTypeFixture.pngContentType();
            ContentType pdfType = ContentTypeFixture.pdfContentType();
            ContentType videoType = ContentTypeFixture.videoContentType();

            // then
            assertThat(defaultType.type()).isEqualTo("image/jpeg");
            assertThat(pngType.type()).isEqualTo("image/png");
            assertThat(pdfType.type()).isEqualTo("application/pdf");
            assertThat(videoType.type()).isEqualTo("video/mp4");
        }
    }
}
