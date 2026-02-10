package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("UploadSource Value Object 단위 테스트")
class UploadSourceTest {

    @Nested
    @DisplayName("of - 생성")
    class Of {

        @Test
        @DisplayName("유효한 값으로 UploadSource를 생성할 수 있다")
        void createsWithValidValue() {
            UploadSource source = UploadSource.of("commerce-service");

            assertThat(source.value()).isEqualTo("commerce-service");
        }
    }

    @Nested
    @DisplayName("유효성 검증")
    class Validation {

        @Test
        @DisplayName("null 값으로 생성하면 NullPointerException이 발생한다")
        void throwsWhenValueIsNull() {
            assertThatThrownBy(() -> UploadSource.of(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("source must not be null");
        }

        @Test
        @DisplayName("빈 문자열로 생성하면 IllegalArgumentException이 발생한다")
        void throwsWhenValueIsBlank() {
            assertThatThrownBy(() -> UploadSource.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("source must not be blank");
        }

        @Test
        @DisplayName("공백만 있는 문자열로 생성하면 IllegalArgumentException이 발생한다")
        void throwsWhenValueIsWhitespaceOnly() {
            assertThatThrownBy(() -> UploadSource.of("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("source must not be blank");
        }

        @Test
        @DisplayName("100자 초과하면 IllegalArgumentException이 발생한다")
        void throwsWhenValueExceedsMaxLength() {
            String longValue = "a".repeat(101);

            assertThatThrownBy(() -> UploadSource.of(longValue))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("source must not exceed 100 characters");
        }

        @Test
        @DisplayName("100자인 값은 정상 생성된다")
        void createsWithExactMaxLength() {
            String maxLengthValue = "a".repeat(100);

            UploadSource source = UploadSource.of(maxLengthValue);

            assertThat(source.value()).hasSize(100);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class Equality {

        @Test
        @DisplayName("같은 값의 UploadSource는 동등하다")
        void sameValueAreEqual() {
            UploadSource s1 = UploadSource.of("commerce-service");
            UploadSource s2 = UploadSource.of("commerce-service");

            assertThat(s1).isEqualTo(s2);
            assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
        }

        @Test
        @DisplayName("다른 값의 UploadSource는 동등하지 않다")
        void differentValueAreNotEqual() {
            UploadSource s1 = UploadSource.of("commerce-service");
            UploadSource s2 = UploadSource.of("admin-service");

            assertThat(s1).isNotEqualTo(s2);
        }
    }
}
