package com.ryuqq.fileflow.domain.policy.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Dimension Value Object 테스트
 */
@DisplayName("Dimension Value Object 테스트")
class DimensionTest {

    @Test
    @DisplayName("정상적인 너비와 높이로 Dimension 생성 성공")
    void createDimension_Success() {
        // when
        Dimension dimension = Dimension.of(1920, 1080);

        // then
        assertThat(dimension.getWidth()).isEqualTo(1920);
        assertThat(dimension.getHeight()).isEqualTo(1080);
    }

    @Test
    @DisplayName("최소값(1)으로 Dimension 생성 성공")
    void createDimension_MinValue_Success() {
        // when
        Dimension dimension = Dimension.of(1, 1);

        // then
        assertThat(dimension.getWidth()).isEqualTo(1);
        assertThat(dimension.getHeight()).isEqualTo(1);
    }

    @Test
    @DisplayName("최대값(50000)으로 Dimension 생성 성공")
    void createDimension_MaxValue_Success() {
        // when
        Dimension dimension = Dimension.of(50000, 50000);

        // then
        assertThat(dimension.getWidth()).isEqualTo(50000);
        assertThat(dimension.getHeight()).isEqualTo(50000);
    }

    @Test
    @DisplayName("너비가 0 이하일 때 예외 발생")
    void createDimension_WidthZeroOrNegative_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> Dimension.of(0, 1080))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("width must be at least 1");

        assertThatThrownBy(() -> Dimension.of(-1, 1080))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("width must be at least 1");
    }

    @Test
    @DisplayName("높이가 0 이하일 때 예외 발생")
    void createDimension_HeightZeroOrNegative_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> Dimension.of(1920, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("height must be at least 1");

        assertThatThrownBy(() -> Dimension.of(1920, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("height must be at least 1");
    }

    @Test
    @DisplayName("너비가 최대값(50000)을 초과할 때 예외 발생")
    void createDimension_WidthExceedsMax_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> Dimension.of(50001, 1080))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("width must not exceed 50000");
    }

    @Test
    @DisplayName("높이가 최대값(50000)을 초과할 때 예외 발생")
    void createDimension_HeightExceedsMax_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> Dimension.of(1920, 50001))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("height must not exceed 50000");
    }

    @Test
    @DisplayName("isWithin: 최대 차원 내에 있을 때 true 반환")
    void isWithin_WithinMaxDimension_ReturnsTrue() {
        // given
        Dimension dimension = Dimension.of(1920, 1080);
        Dimension maxDimension = Dimension.of(2048, 2048);

        // when & then
        assertThat(dimension.isWithin(maxDimension)).isTrue();
    }

    @Test
    @DisplayName("isWithin: 최대 차원과 정확히 일치할 때 true 반환")
    void isWithin_ExactlyMaxDimension_ReturnsTrue() {
        // given
        Dimension dimension = Dimension.of(2048, 2048);
        Dimension maxDimension = Dimension.of(2048, 2048);

        // when & then
        assertThat(dimension.isWithin(maxDimension)).isTrue();
    }

    @Test
    @DisplayName("isWithin: 너비가 최대 차원을 초과할 때 false 반환")
    void isWithin_WidthExceedsMax_ReturnsFalse() {
        // given
        Dimension dimension = Dimension.of(2049, 1080);
        Dimension maxDimension = Dimension.of(2048, 2048);

        // when & then
        assertThat(dimension.isWithin(maxDimension)).isFalse();
    }

    @Test
    @DisplayName("isWithin: 높이가 최대 차원을 초과할 때 false 반환")
    void isWithin_HeightExceedsMax_ReturnsFalse() {
        // given
        Dimension dimension = Dimension.of(1920, 2049);
        Dimension maxDimension = Dimension.of(2048, 2048);

        // when & then
        assertThat(dimension.isWithin(maxDimension)).isFalse();
    }

    @Test
    @DisplayName("isWithin: maxDimension이 null일 때 예외 발생")
    void isWithin_NullMaxDimension_ThrowsException() {
        // given
        Dimension dimension = Dimension.of(1920, 1080);

        // when & then
        assertThatThrownBy(() -> dimension.isWithin(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxDimension cannot be null");
    }

    @Test
    @DisplayName("equals: 동일한 값을 가진 Dimension은 같음")
    void equals_SameValues_ReturnsTrue() {
        // given
        Dimension dimension1 = Dimension.of(1920, 1080);
        Dimension dimension2 = Dimension.of(1920, 1080);

        // when & then
        assertThat(dimension1).isEqualTo(dimension2);
    }

    @Test
    @DisplayName("equals: 다른 값을 가진 Dimension은 다름")
    void equals_DifferentValues_ReturnsFalse() {
        // given
        Dimension dimension1 = Dimension.of(1920, 1080);
        Dimension dimension2 = Dimension.of(2048, 2048);

        // when & then
        assertThat(dimension1).isNotEqualTo(dimension2);
    }

    @Test
    @DisplayName("hashCode: 동일한 값을 가진 Dimension은 같은 hashCode")
    void hashCode_SameValues_ReturnsSameHashCode() {
        // given
        Dimension dimension1 = Dimension.of(1920, 1080);
        Dimension dimension2 = Dimension.of(1920, 1080);

        // when & then
        assertThat(dimension1.hashCode()).isEqualTo(dimension2.hashCode());
    }

    @Test
    @DisplayName("toString: 적절한 문자열 표현 반환")
    void toString_ReturnsCorrectFormat() {
        // given
        Dimension dimension = Dimension.of(1920, 1080);

        // when
        String result = dimension.toString();

        // then
        assertThat(result).contains("1920");
        assertThat(result).contains("1080");
    }
}
