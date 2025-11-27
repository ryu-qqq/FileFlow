package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.*;

import com.ryuqq.fileflow.domain.session.fixture.PresignedUrlFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PresignedUrl 단위 테스트")
class PresignedUrlTest {

    private static final String VALID_URL =
            "https://s3.amazonaws.com/bucket/key?X-Amz-Signature=abc123";

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("유효한 HTTPS URL로 PresignedUrl을 생성할 수 있다")
        void of_WithValidUrl_ShouldCreatePresignedUrl() {
            // given
            String url = VALID_URL;

            // when
            PresignedUrl presignedUrl = PresignedUrl.of(url);

            // then
            assertThat(presignedUrl.value()).isEqualTo(url);
        }

        @Test
        @DisplayName("null 또는 빈 문자열이면 예외가 발생한다")
        void of_WithNullOrBlank_ShouldThrowException() {
            // given
            String nullValue = null;
            String blankValue = "   ";

            // when & then
            assertThatThrownBy(() -> PresignedUrl.of(nullValue))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Presigned URL은 null 또는 빈 문자열일 수 없습니다.");
            assertThatThrownBy(() -> PresignedUrl.of(blankValue))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Presigned URL은 null 또는 빈 문자열일 수 없습니다.");
        }

        @Test
        @DisplayName("HTTPS가 아닌 URL이면 예외가 발생한다")
        void of_WithNonHttps_ShouldThrowException() {
            // given
            String httpUrl = "http://s3.amazonaws.com/bucket/key?X-Amz-Signature=abc123";

            // when & then
            assertThatThrownBy(() -> PresignedUrl.of(httpUrl))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Presigned URL은 HTTPS로 시작해야 합니다.");
        }
    }

    @Nested
    @DisplayName("유효성 검사 테스트")
    class ValidationTest {

        @Test
        @DisplayName("유효한 URL은 isValid가 true를 반환한다")
        void isValid_WithValidUrl_ShouldReturnTrue() {
            // given
            PresignedUrl presignedUrl = PresignedUrl.of(VALID_URL);

            // when & then
            assertThat(presignedUrl.isValid()).isTrue();
        }

        @Test
        @DisplayName("HTTPS가 아닌 URL은 isValid가 false를 반환한다")
        void isValid_WithInvalidUrl_ShouldReturnFalse() {
            // given
            PresignedUrl presignedUrl = new PresignedUrl("http://example.com/file");

            // when & then
            assertThat(presignedUrl.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가지면 동등하다")
        void equals_WithSameValue_ShouldBeEqual() {
            // given
            PresignedUrl presignedUrl1 = PresignedUrl.of(VALID_URL);
            PresignedUrl presignedUrl2 = PresignedUrl.of(VALID_URL);

            // when & then
            assertThat(presignedUrl1).isEqualTo(presignedUrl2);
            assertThat(presignedUrl1.hashCode()).isEqualTo(presignedUrl2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가지면 동등하지 않다")
        void equals_WithDifferentValue_ShouldNotBeEqual() {
            // given
            PresignedUrl presignedUrl1 = PresignedUrl.of(VALID_URL);
            PresignedUrl presignedUrl2 =
                    PresignedUrl.of("https://s3.amazonaws.com/bucket/key?X-Amz-Signature=zzz");

            // when & then
            assertThat(presignedUrl1).isNotEqualTo(presignedUrl2);
        }
    }

    @Nested
    @DisplayName("Fixture 테스트")
    class FixtureTest {

        @Test
        @DisplayName("Fixture로 생성된 PresignedUrl이 정상 동작한다")
        void fixture_ShouldWorkCorrectly() {
            // given & when
            PresignedUrl defaultPresignedUrl = PresignedUrlFixture.defaultPresignedUrl();
            PresignedUrl putPresignedUrl = PresignedUrlFixture.putPresignedUrl();
            PresignedUrl customPresignedUrl =
                    PresignedUrlFixture.customPresignedUrl(
                            "https://s3.amazonaws.com/bucket/custom?X-Amz-Signature=xyz789");

            // then
            assertThat(defaultPresignedUrl.isValid()).isTrue();
            assertThat(putPresignedUrl.value()).contains("X-Amz-Algorithm");
            assertThat(customPresignedUrl.value())
                    .isEqualTo("https://s3.amazonaws.com/bucket/custom?X-Amz-Signature=xyz789");
        }
    }
}
