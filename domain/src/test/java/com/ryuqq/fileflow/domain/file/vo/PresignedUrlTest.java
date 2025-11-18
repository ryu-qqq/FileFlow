package com.ryuqq.fileflow.domain.file.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PresignedUrl Value Object 테스트
 * <p>
 * S3 Presigned URL 검증 (5분 유효)
 * </p>
 */
@DisplayName("PresignedUrl Value Object 테스트")
class PresignedUrlTest {

    @Test
    @DisplayName("유효한 Presigned URL로 PresignedUrl을 생성해야 한다")
    void shouldCreateValidPresignedUrl() {
        // given
        String validUrl = "https://fileflow-uploads-1.s3.ap-northeast-2.amazonaws.com/uploads/1/admin/connectly/banner/01JD8001_test.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=...";

        // when
        PresignedUrl presignedUrl = PresignedUrl.of(validUrl);

        // then
        assertThat(presignedUrl).isNotNull();
        assertThat(presignedUrl.getValue()).isEqualTo(validUrl);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("null 또는 빈 문자열로 생성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenValueIsNullOrEmpty(String invalidUrl) {
        // when & then
        assertThatThrownBy(() -> PresignedUrl.of(invalidUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Presigned URL은 필수입니다");
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("공백 문자열로 생성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenValueIsBlank(String blankUrl) {
        // when & then
        assertThatThrownBy(() -> PresignedUrl.of(blankUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Presigned URL은 필수입니다");
    }

    @Test
    @DisplayName("getValue()는 생성 시 전달한 URL을 반환해야 한다")
    void shouldReturnSameValueFromGetValue() {
        // given
        String expectedUrl = "https://example.com/presigned-url?signature=abc123";
        PresignedUrl presignedUrl = PresignedUrl.of(expectedUrl);

        // when
        String actualUrl = presignedUrl.getValue();

        // then
        assertThat(actualUrl).isEqualTo(expectedUrl);
    }

    @Test
    @DisplayName("같은 URL로 생성된 PresignedUrl은 동등해야 한다")
    void shouldBeEqualWhenValueIsSame() {
        // given
        String url = "https://s3.amazonaws.com/bucket/key?signature=xyz";
        PresignedUrl presignedUrl1 = PresignedUrl.of(url);
        PresignedUrl presignedUrl2 = PresignedUrl.of(url);

        // when & then
        assertThat(presignedUrl1).isEqualTo(presignedUrl2);
    }

    @Test
    @DisplayName("같은 URL로 생성된 PresignedUrl은 같은 해시코드를 가져야 한다")
    void shouldHaveSameHashCodeWhenValueIsSame() {
        // given
        String url = "https://fileflow.s3.amazonaws.com/test.jpg?token=abc";
        PresignedUrl presignedUrl1 = PresignedUrl.of(url);
        PresignedUrl presignedUrl2 = PresignedUrl.of(url);

        // when & then
        assertThat(presignedUrl1.hashCode()).isEqualTo(presignedUrl2.hashCode());
    }

    @Test
    @DisplayName("다른 URL로 생성된 PresignedUrl은 동등하지 않아야 한다")
    void shouldNotBeEqualWhenValueIsDifferent() {
        // given
        String url1 = "https://s3.amazonaws.com/bucket/file1.jpg?signature=aaa";
        String url2 = "https://s3.amazonaws.com/bucket/file2.jpg?signature=bbb";
        PresignedUrl presignedUrl1 = PresignedUrl.of(url1);
        PresignedUrl presignedUrl2 = PresignedUrl.of(url2);

        // when & then
        assertThat(presignedUrl1).isNotEqualTo(presignedUrl2);
    }

    @Test
    @DisplayName("S3 Presigned URL 형식의 URL을 생성해야 한다")
    void shouldCreateS3PresignedUrlFormat() {
        // given
        String s3PresignedUrl = "https://fileflow-uploads-1.s3.ap-northeast-2.amazonaws.com/uploads/1/admin/connectly/banner/test.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256";

        // when
        PresignedUrl presignedUrl = PresignedUrl.of(s3PresignedUrl);

        // then
        assertThat(presignedUrl.getValue()).startsWith("https://");
        assertThat(presignedUrl.getValue()).contains("s3");
        assertThat(presignedUrl.getValue()).contains("amazonaws.com");
    }

    @Test
    @DisplayName("긴 URL도 정상적으로 생성되어야 한다")
    void shouldCreateLongUrl() {
        // given
        String longUrl = "https://fileflow-uploads-1.s3.ap-northeast-2.amazonaws.com/uploads/1/admin/connectly/banner/01JD8001-1234-5678-9abc-def012345678_메인배너.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAIOSFODNN7EXAMPLE/20250118/ap-northeast-2/s3/aws4_request&X-Amz-Date=20250118T030000Z&X-Amz-Expires=300&X-Amz-SignedHeaders=host&X-Amz-Signature=abcdef1234567890";

        // when
        PresignedUrl presignedUrl = PresignedUrl.of(longUrl);

        // then
        assertThat(presignedUrl.getValue()).hasSize(longUrl.length());
        assertThat(presignedUrl.getValue()).isEqualTo(longUrl);
    }
}
