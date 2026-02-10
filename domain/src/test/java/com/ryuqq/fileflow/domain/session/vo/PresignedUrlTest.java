package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("PresignedUrl Value Object 단위 테스트")
class PresignedUrlTest {

    @Nested
    @DisplayName("of - 생성")
    class Of {

        @Test
        @DisplayName("유효한 URL 문자열로 PresignedUrl을 생성할 수 있다")
        void createsWithValidValue() {
            PresignedUrl url = PresignedUrl.of("https://s3.amazonaws.com/bucket/key?signature=abc");

            assertThat(url.value()).isEqualTo("https://s3.amazonaws.com/bucket/key?signature=abc");
        }
    }

    @Nested
    @DisplayName("유효성 검증")
    class Validation {

        @Test
        @DisplayName("null 값으로 생성하면 NullPointerException이 발생한다")
        void throwsWhenValueIsNull() {
            assertThatThrownBy(() -> PresignedUrl.of(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("presignedUrl must not be null");
        }

        @Test
        @DisplayName("빈 문자열로 생성하면 IllegalArgumentException이 발생한다")
        void throwsWhenValueIsBlank() {
            assertThatThrownBy(() -> PresignedUrl.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("presignedUrl must not be blank");
        }

        @Test
        @DisplayName("공백만 있는 문자열로 생성하면 IllegalArgumentException이 발생한다")
        void throwsWhenValueIsWhitespaceOnly() {
            assertThatThrownBy(() -> PresignedUrl.of("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("presignedUrl must not be blank");
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class Equality {

        @Test
        @DisplayName("같은 값의 PresignedUrl은 동등하다")
        void sameValueAreEqual() {
            PresignedUrl url1 = PresignedUrl.of("https://s3.presigned-url.com/test");
            PresignedUrl url2 = PresignedUrl.of("https://s3.presigned-url.com/test");

            assertThat(url1).isEqualTo(url2);
            assertThat(url1.hashCode()).isEqualTo(url2.hashCode());
        }

        @Test
        @DisplayName("다른 값의 PresignedUrl은 동등하지 않다")
        void differentValueAreNotEqual() {
            PresignedUrl url1 = PresignedUrl.of("https://s3.presigned-url.com/test1");
            PresignedUrl url2 = PresignedUrl.of("https://s3.presigned-url.com/test2");

            assertThat(url1).isNotEqualTo(url2);
        }
    }
}
