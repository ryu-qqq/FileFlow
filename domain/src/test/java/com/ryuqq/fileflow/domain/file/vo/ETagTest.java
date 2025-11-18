package com.ryuqq.fileflow.domain.file.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ETag Value Object 테스트
 */
class ETagTest {

    @Test
    @DisplayName("유효한 ETag 값으로 ETag를 생성해야 한다")
    void shouldCreateValidETag() {
        // given
        String validETag = "d41d8cd98f00b204e9800998ecf8427e";

        // when
        ETag etag = ETag.of(validETag);

        // then
        assertThat(etag).isNotNull();
        assertThat(etag.value()).isEqualTo(validETag);
    }

    @Test
    @DisplayName("null 또는 빈 문자열로 생성 시 예외가 발생해야 한다")
    void shouldThrowExceptionWhenValueIsNullOrEmpty() {
        // when & then
        assertThatThrownBy(() -> ETag.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ETag는 null이거나 빈 값일 수 없습니다");

        assertThatThrownBy(() -> ETag.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ETag는 null이거나 빈 값일 수 없습니다");
    }

    @Test
    @DisplayName("같은 값을 가진 ETag는 동등해야 한다")
    void shouldBeEqualWhenValueIsSame() {
        // given
        String value = "abc123def456";
        ETag etag1 = ETag.of(value);
        ETag etag2 = ETag.of(value);

        // when & then
        assertThat(etag1).isEqualTo(etag2);
    }

    @Test
    @DisplayName("getValue()는 생성 시 전달한 값을 반환해야 한다")
    void shouldReturnSameValueFromValue() {
        // given
        String expectedValue = "etag12345";
        ETag etag = ETag.of(expectedValue);

        // when
        String actualValue = etag.value();

        // then
        assertThat(actualValue).isEqualTo(expectedValue);
    }
}
