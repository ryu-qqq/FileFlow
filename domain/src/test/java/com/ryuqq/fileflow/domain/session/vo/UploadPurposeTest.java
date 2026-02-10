package com.ryuqq.fileflow.domain.session.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("UploadPurpose Value Object 단위 테스트")
class UploadPurposeTest {

    @Nested
    @DisplayName("of - 생성")
    class Of {

        @Test
        @DisplayName("유효한 값으로 UploadPurpose를 생성할 수 있다")
        void createsWithValidValue() {
            UploadPurpose purpose = UploadPurpose.of("product-image");

            assertThat(purpose.value()).isEqualTo("product-image");
        }
    }

    @Nested
    @DisplayName("유효성 검증")
    class Validation {

        @Test
        @DisplayName("null 값으로 생성하면 NullPointerException이 발생한다")
        void throwsWhenValueIsNull() {
            assertThatThrownBy(() -> UploadPurpose.of(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("purpose must not be null");
        }

        @Test
        @DisplayName("빈 문자열로 생성하면 IllegalArgumentException이 발생한다")
        void throwsWhenValueIsBlank() {
            assertThatThrownBy(() -> UploadPurpose.of(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("purpose must not be blank");
        }

        @Test
        @DisplayName("공백만 있는 문자열로 생성하면 IllegalArgumentException이 발생한다")
        void throwsWhenValueIsWhitespaceOnly() {
            assertThatThrownBy(() -> UploadPurpose.of("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("purpose must not be blank");
        }

        @Test
        @DisplayName("100자 초과하면 IllegalArgumentException이 발생한다")
        void throwsWhenValueExceedsMaxLength() {
            String longValue = "a".repeat(101);

            assertThatThrownBy(() -> UploadPurpose.of(longValue))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("purpose must not exceed 100 characters");
        }

        @Test
        @DisplayName("100자인 값은 정상 생성된다")
        void createsWithExactMaxLength() {
            String maxLengthValue = "a".repeat(100);

            UploadPurpose purpose = UploadPurpose.of(maxLengthValue);

            assertThat(purpose.value()).hasSize(100);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class Equality {

        @Test
        @DisplayName("같은 값의 UploadPurpose는 동등하다")
        void sameValueAreEqual() {
            UploadPurpose p1 = UploadPurpose.of("product-image");
            UploadPurpose p2 = UploadPurpose.of("product-image");

            assertThat(p1).isEqualTo(p2);
            assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
        }

        @Test
        @DisplayName("다른 값의 UploadPurpose는 동등하지 않다")
        void differentValueAreNotEqual() {
            UploadPurpose p1 = UploadPurpose.of("product-image");
            UploadPurpose p2 = UploadPurpose.of("user-avatar");

            assertThat(p1).isNotEqualTo(p2);
        }
    }
}
