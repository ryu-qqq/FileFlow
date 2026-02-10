package com.ryuqq.fileflow.domain.transform.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.domain.transform.exception.TransformException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("ImageDimension")
class ImageDimensionTest {

    @Nested
    @DisplayName("생성")
    class CreateTest {

        @Test
        @DisplayName("유효한 width, height로 생성할 수 있다")
        void shouldCreateWithValidDimensions() {
            var dimension = ImageDimension.of(1920, 1080);

            assertThat(dimension.width()).isEqualTo(1920);
            assertThat(dimension.height()).isEqualTo(1080);
        }

        @Test
        @DisplayName("width가 0이면 예외가 발생한다")
        void shouldThrowWhenWidthIsZero() {
            assertThatThrownBy(() -> ImageDimension.of(0, 100))
                    .isInstanceOf(TransformException.class);
        }

        @Test
        @DisplayName("width가 음수이면 예외가 발생한다")
        void shouldThrowWhenWidthIsNegative() {
            assertThatThrownBy(() -> ImageDimension.of(-1, 100))
                    .isInstanceOf(TransformException.class);
        }

        @Test
        @DisplayName("height가 0이면 예외가 발생한다")
        void shouldThrowWhenHeightIsZero() {
            assertThatThrownBy(() -> ImageDimension.of(100, 0))
                    .isInstanceOf(TransformException.class);
        }

        @Test
        @DisplayName("height가 음수이면 예외가 발생한다")
        void shouldThrowWhenHeightIsNegative() {
            assertThatThrownBy(() -> ImageDimension.of(100, -1))
                    .isInstanceOf(TransformException.class);
        }
    }
}
