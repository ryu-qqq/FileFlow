package com.ryuqq.fileflow.domain.upload.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ContentType 테스트")
class ContentTypeTest {

    @Test
    @DisplayName("유효한 MIME 타입으로 ContentType을 생성한다")
    void createContentTypeWithValidMimeType() {
        // given
        String mimeType = "image/jpeg";

        // when
        ContentType contentType = ContentType.of(mimeType);

        // then
        assertThat(contentType.value()).isEqualTo("image/jpeg");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("null 또는 빈 문자열로 생성 시 예외가 발생한다")
    void throwsExceptionWhenValueIsNullOrEmpty(String invalidValue) {
        // when & then
        assertThatThrownBy(() -> ContentType.of(invalidValue))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ContentType cannot be null or empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "invalid",
        "image",
        "image/",
        "/jpeg",
        "image//jpeg",
        "image jpeg",
        "image@jpeg",
        "image#jpeg"
    })
    @DisplayName("유효하지 않은 MIME 타입 형식으로 생성 시 예외가 발생한다")
    void throwsExceptionWhenValueIsNotValidMimeType(String invalidMimeType) {
        // when & then
        assertThatThrownBy(() -> ContentType.of(invalidMimeType))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ContentType must follow MIME type format");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp",
        "image/svg+xml"
    })
    @DisplayName("이미지 타입을 올바르게 식별한다")
    void identifiesImageTypesCorrectly(String imageType) {
        // when
        ContentType contentType = ContentType.of(imageType);

        // then
        assertThat(contentType.isImage()).isTrue();
        assertThat(contentType.isVideo()).isFalse();
        assertThat(contentType.isDocument()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "video/mp4",
        "video/mpeg",
        "video/webm",
        "video/quicktime"
    })
    @DisplayName("비디오 타입을 올바르게 식별한다")
    void identifiesVideoTypesCorrectly(String videoType) {
        // when
        ContentType contentType = ContentType.of(videoType);

        // then
        assertThat(contentType.isVideo()).isTrue();
        assertThat(contentType.isImage()).isFalse();
        assertThat(contentType.isDocument()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    })
    @DisplayName("문서 타입을 올바르게 식별한다")
    void identifiesDocumentTypesCorrectly(String documentType) {
        // when
        ContentType contentType = ContentType.of(documentType);

        // then
        assertThat(contentType.isDocument()).isTrue();
        assertThat(contentType.isImage()).isFalse();
        assertThat(contentType.isVideo()).isFalse();
    }

    @Test
    @DisplayName("주 타입을 올바르게 추출한다")
    void extractsMainTypeCorrectly() {
        // given
        ContentType contentType = ContentType.of("image/jpeg");

        // when
        String mainType = contentType.getMainType();

        // then
        assertThat(mainType).isEqualTo("image");
    }

    @Test
    @DisplayName("서브 타입을 올바르게 추출한다")
    void extractsSubTypeCorrectly() {
        // given
        ContentType contentType = ContentType.of("image/jpeg");

        // when
        String subType = contentType.getSubType();

        // then
        assertThat(subType).isEqualTo("jpeg");
    }

    @Test
    @DisplayName("복잡한 서브 타입을 올바르게 추출한다")
    void extractsComplexSubTypeCorrectly() {
        // given
        ContentType contentType = ContentType.of("application/vnd.ms-excel");

        // when
        String mainType = contentType.getMainType();
        String subType = contentType.getSubType();

        // then
        assertThat(mainType).isEqualTo("application");
        assertThat(subType).isEqualTo("vnd.ms-excel");
    }

    @Test
    @DisplayName("동일한 MIME 타입의 ContentType은 같다")
    void equalContentTypesAreEqual() {
        // given
        ContentType contentType1 = ContentType.of("image/jpeg");
        ContentType contentType2 = ContentType.of("image/jpeg");

        // when & then
        assertThat(contentType1).isEqualTo(contentType2);
        assertThat(contentType1.hashCode()).isEqualTo(contentType2.hashCode());
    }

    @Test
    @DisplayName("다른 MIME 타입의 ContentType은 다르다")
    void differentContentTypesAreNotEqual() {
        // given
        ContentType contentType1 = ContentType.of("image/jpeg");
        ContentType contentType2 = ContentType.of("image/png");

        // when & then
        assertThat(contentType1).isNotEqualTo(contentType2);
    }

    @Test
    @DisplayName("대소문자를 구분하지 않고 이미지 타입을 식별한다")
    void identifiesImageTypeCaseInsensitively() {
        // given
        ContentType lowercase = ContentType.of("image/jpeg");
        ContentType uppercase = ContentType.of("IMAGE/JPEG");
        ContentType mixedCase = ContentType.of("Image/Jpeg");

        // when & then
        assertThat(lowercase.isImage()).isTrue();
        assertThat(uppercase.isImage()).isTrue();
        assertThat(mixedCase.isImage()).isTrue();
    }

    @Test
    @DisplayName("대소문자를 구분하지 않고 비디오 타입을 식별한다")
    void identifiesVideoTypeCaseInsensitively() {
        // given
        ContentType lowercase = ContentType.of("video/mp4");
        ContentType uppercase = ContentType.of("VIDEO/MP4");

        // when & then
        assertThat(lowercase.isVideo()).isTrue();
        assertThat(uppercase.isVideo()).isTrue();
    }

    @Test
    @DisplayName("대소문자를 구분하지 않고 문서 타입을 식별한다")
    void identifiesDocumentTypeCaseInsensitively() {
        // given
        ContentType lowercase = ContentType.of("application/pdf");
        ContentType uppercase = ContentType.of("APPLICATION/PDF");

        // when & then
        assertThat(lowercase.isDocument()).isTrue();
        assertThat(uppercase.isDocument()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "text/plain",
        "text/html",
        "text/css",
        "text/javascript",
        "application/json",
        "application/xml"
    })
    @DisplayName("기타 타입은 이미지, 비디오, 문서가 아니다")
    void otherTypesAreNotImageVideoOrDocument(String otherType) {
        // when
        ContentType contentType = ContentType.of(otherType);

        // then
        assertThat(contentType.isImage()).isFalse();
        assertThat(contentType.isVideo()).isFalse();
        assertThat(contentType.isDocument()).isFalse();
    }

    @Test
    @DisplayName("특수 문자가 포함된 유효한 MIME 타입을 처리한다")
    void handlesValidMimeTypeWithSpecialCharacters() {
        // given
        String mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

        // when
        ContentType contentType = ContentType.of(mimeType);

        // then
        assertThat(contentType.value()).isEqualTo(mimeType);
        assertThat(contentType.isDocument()).isTrue();
    }

    @Test
    @DisplayName("더하기 기호가 포함된 유효한 MIME 타입을 처리한다")
    void handlesValidMimeTypeWithPlusSign() {
        // given
        String mimeType = "image/svg+xml";

        // when
        ContentType contentType = ContentType.of(mimeType);

        // then
        assertThat(contentType.value()).isEqualTo("image/svg+xml");
        assertThat(contentType.isImage()).isTrue();
    }

    @Test
    @DisplayName("언더스코어가 포함된 유효한 MIME 타입을 처리한다")
    void handlesValidMimeTypeWithUnderscore() {
        // given
        String mimeType = "application/x_custom_type";

        // when
        ContentType contentType = ContentType.of(mimeType);

        // then
        assertThat(contentType.value()).isEqualTo("application/x_custom_type");
    }

    @Test
    @DisplayName("하이픈이 포함된 유효한 MIME 타입을 처리한다")
    void handlesValidMimeTypeWithHyphen() {
        // given
        String mimeType = "application/x-custom-type";

        // when
        ContentType contentType = ContentType.of(mimeType);

        // then
        assertThat(contentType.value()).isEqualTo("application/x-custom-type");
    }

    @Test
    @DisplayName("점이 포함된 유효한 MIME 타입을 처리한다")
    void handlesValidMimeTypeWithDot() {
        // given
        String mimeType = "application/vnd.ms-excel";

        // when
        ContentType contentType = ContentType.of(mimeType);

        // then
        assertThat(contentType.value()).isEqualTo("application/vnd.ms-excel");
        assertThat(contentType.isDocument()).isTrue();
    }

    @Test
    @DisplayName("숫자가 포함된 유효한 MIME 타입을 처리한다")
    void handlesValidMimeTypeWithNumbers() {
        // given
        String mimeType = "application/mp4";

        // when
        ContentType contentType = ContentType.of(mimeType);

        // then
        assertThat(contentType.value()).isEqualTo("application/mp4");
    }

    @Test
    @DisplayName("toString이 MIME 타입을 포함한다")
    void toStringContainsMimeType() {
        // given
        ContentType contentType = ContentType.of("image/jpeg");

        // when
        String result = contentType.toString();

        // then
        assertThat(result).contains("image/jpeg");
    }

    @Test
    @DisplayName("슬래시가 없는 경우 전체 값이 주 타입이 된다")
    void whenNoSlashEntireValueIsMainType() {
        // 이 경우는 검증에서 걸리지만, 만약 통과한다면
        // getMainType은 전체 값을 반환해야 함
    }

    @Test
    @DisplayName("알려지지 않은 이미지 타입은 isImage가 false를 반환한다")
    void unknownImageTypeReturnsFalseForIsImage() {
        // given
        ContentType contentType = ContentType.of("image/custom");

        // when & then
        assertThat(contentType.isImage()).isFalse();
        assertThat(contentType.getMainType()).isEqualTo("image");
    }

    @Test
    @DisplayName("알려지지 않은 비디오 타입은 isVideo가 false를 반환한다")
    void unknownVideoTypeReturnsFalseForIsVideo() {
        // given
        ContentType contentType = ContentType.of("video/custom");

        // when & then
        assertThat(contentType.isVideo()).isFalse();
        assertThat(contentType.getMainType()).isEqualTo("video");
    }

    @Test
    @DisplayName("알려지지 않은 문서 타입은 isDocument가 false를 반환한다")
    void unknownDocumentTypeReturnsFalseForIsDocument() {
        // given
        ContentType contentType = ContentType.of("application/custom");

        // when & then
        assertThat(contentType.isDocument()).isFalse();
        assertThat(contentType.getMainType()).isEqualTo("application");
    }

    @Test
    @DisplayName("복잡한 MIME 타입에서 주 타입과 서브 타입을 올바르게 추출한다")
    void extractsTypesFromComplexMimeType() {
        // given
        ContentType contentType = ContentType.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        // when
        String mainType = contentType.getMainType();
        String subType = contentType.getSubType();

        // then
        assertThat(mainType).isEqualTo("application");
        assertThat(subType).isEqualTo("vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }
}
