package com.ryuqq.fileflow.domain.transform.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TransformParamsTest {

    @Nested
    @DisplayName("forResize - 리사이즈 파라미터 생성")
    class ForResize {

        @Test
        @DisplayName("width, height, maintainAspectRatio가 설정된다")
        void creates_resize_params() {
            TransformParams params = TransformParams.forResize(800, 600, true);

            assertThat(params.width()).isEqualTo(800);
            assertThat(params.height()).isEqualTo(600);
            assertThat(params.maintainAspectRatio()).isTrue();
            assertThat(params.targetFormat()).isNull();
            assertThat(params.quality()).isNull();
        }
    }

    @Nested
    @DisplayName("forConvert - 포맷 변환 파라미터 생성")
    class ForConvert {

        @Test
        @DisplayName("targetFormat이 소문자로 설정된다")
        void creates_convert_params_with_lowercase() {
            TransformParams params = TransformParams.forConvert("WEBP");

            assertThat(params.targetFormat()).isEqualTo("webp");
            assertThat(params.width()).isNull();
            assertThat(params.height()).isNull();
            assertThat(params.quality()).isNull();
        }

        @Test
        @DisplayName("null targetFormat이면 IllegalArgumentException이 발생한다")
        void null_target_format_throws_exception() {
            assertThatThrownBy(() -> TransformParams.forConvert(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("targetFormat");
        }

        @Test
        @DisplayName("blank targetFormat이면 IllegalArgumentException이 발생한다")
        void blank_target_format_throws_exception() {
            assertThatThrownBy(() -> TransformParams.forConvert("  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("targetFormat");
        }
    }

    @Nested
    @DisplayName("forCompress - 압축 파라미터 생성")
    class ForCompress {

        @Test
        @DisplayName("quality가 설정된다")
        void creates_compress_params() {
            TransformParams params = TransformParams.forCompress(80);

            assertThat(params.quality()).isEqualTo(80);
            assertThat(params.width()).isNull();
            assertThat(params.height()).isNull();
            assertThat(params.targetFormat()).isNull();
        }
    }

    @Nested
    @DisplayName("forThumbnail - 썸네일 파라미터 생성")
    class ForThumbnail {

        @Test
        @DisplayName("width, height가 설정되고 maintainAspectRatio는 true이다")
        void creates_thumbnail_params() {
            TransformParams params = TransformParams.forThumbnail(150, 150);

            assertThat(params.width()).isEqualTo(150);
            assertThat(params.height()).isEqualTo(150);
            assertThat(params.maintainAspectRatio()).isTrue();
            assertThat(params.targetFormat()).isNull();
            assertThat(params.quality()).isNull();
        }
    }

    @Nested
    @DisplayName("compact constructor - 검증")
    class CompactConstructor {

        @Test
        @DisplayName("width가 0 이하이면 IllegalArgumentException이 발생한다")
        void negative_width_throws_exception() {
            assertThatThrownBy(() -> new TransformParams(0, null, false, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("width");
        }

        @Test
        @DisplayName("height가 0 이하이면 IllegalArgumentException이 발생한다")
        void negative_height_throws_exception() {
            assertThatThrownBy(() -> new TransformParams(null, -1, false, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("height");
        }

        @Test
        @DisplayName("quality가 0이면 IllegalArgumentException이 발생한다")
        void quality_zero_throws_exception() {
            assertThatThrownBy(() -> new TransformParams(null, null, false, null, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("quality");
        }

        @Test
        @DisplayName("quality가 101이면 IllegalArgumentException이 발생한다")
        void quality_over_100_throws_exception() {
            assertThatThrownBy(() -> new TransformParams(null, null, false, null, 101))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("quality");
        }

        @Test
        @DisplayName("모든 값이 null이면 정상 생성된다")
        void all_null_values_allowed() {
            TransformParams params = new TransformParams(null, null, false, null, null);

            assertThat(params.width()).isNull();
            assertThat(params.height()).isNull();
            assertThat(params.quality()).isNull();
        }
    }
}
