package com.ryuqq.fileflow.domain.image.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ImageDimension 테스트")
class ImageDimensionTest {

    @Test
    @DisplayName("ImageDimension을 생성할 수 있다")
    void of_Success() {
        // given & when
        ImageDimension dimension = ImageDimension.of(1920, 1080);

        // then
        assertThat(dimension.getWidth()).isEqualTo(1920);
        assertThat(dimension.getHeight()).isEqualTo(1080);
    }

    @Test
    @DisplayName("정사각형 ImageDimension을 생성할 수 있다")
    void square_Success() {
        // given & when
        ImageDimension dimension = ImageDimension.square(800);

        // then
        assertThat(dimension.getWidth()).isEqualTo(800);
        assertThat(dimension.getHeight()).isEqualTo(800);
        assertThat(dimension.isSquare()).isTrue();
    }

    @Test
    @DisplayName("너비가 0 이하이면 예외가 발생한다")
    void of_InvalidWidth_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> ImageDimension.of(0, 1080))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Width must be at least");
    }

    @Test
    @DisplayName("높이가 0 이하이면 예외가 발생한다")
    void of_InvalidHeight_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> ImageDimension.of(1920, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Height must be at least");
    }

    @Test
    @DisplayName("최대 크기를 초과하면 예외가 발생한다")
    void of_ExceedsMaxDimension_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> ImageDimension.of(10001, 1080))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot exceed");
    }

    @Test
    @DisplayName("가로 세로 비율을 계산할 수 있다")
    void getAspectRatio() {
        // given
        ImageDimension dimension = ImageDimension.of(1920, 1080);

        // when
        double aspectRatio = dimension.getAspectRatio();

        // then
        assertThat(aspectRatio).isEqualTo(1920.0 / 1080.0);
    }

    @Test
    @DisplayName("전체 픽셀 수를 계산할 수 있다")
    void getTotalPixels() {
        // given
        ImageDimension dimension = ImageDimension.of(1920, 1080);

        // when
        long totalPixels = dimension.getTotalPixels();

        // then
        assertThat(totalPixels).isEqualTo(1920L * 1080L);
    }

    @Test
    @DisplayName("가로 이미지인지 확인할 수 있다")
    void isLandscape() {
        // given
        ImageDimension landscape = ImageDimension.of(1920, 1080);
        ImageDimension portrait = ImageDimension.of(1080, 1920);

        // when & then
        assertThat(landscape.isLandscape()).isTrue();
        assertThat(portrait.isLandscape()).isFalse();
    }

    @Test
    @DisplayName("세로 이미지인지 확인할 수 있다")
    void isPortrait() {
        // given
        ImageDimension landscape = ImageDimension.of(1920, 1080);
        ImageDimension portrait = ImageDimension.of(1080, 1920);

        // when & then
        assertThat(landscape.isPortrait()).isFalse();
        assertThat(portrait.isPortrait()).isTrue();
    }

    @Test
    @DisplayName("정사각형 이미지인지 확인할 수 있다")
    void isSquare() {
        // given
        ImageDimension square = ImageDimension.square(800);
        ImageDimension rectangle = ImageDimension.of(1920, 1080);

        // when & then
        assertThat(square.isSquare()).isTrue();
        assertThat(rectangle.isSquare()).isFalse();
    }

    @Test
    @DisplayName("비율을 유지하면서 리사이징할 수 있다")
    void resize_MaintainsAspectRatio() {
        // given
        ImageDimension original = ImageDimension.of(1920, 1080);

        // when
        ImageDimension resized = original.resize(960, 540);

        // then
        assertThat(resized.getWidth()).isEqualTo(960);
        assertThat(resized.getHeight()).isEqualTo(540);
    }

    @Test
    @DisplayName("이미 작은 이미지는 리사이징하지 않는다")
    void resize_AlreadySmall_NoResize() {
        // given
        ImageDimension small = ImageDimension.of(800, 600);

        // when
        ImageDimension resized = small.resize(1920, 1080);

        // then
        assertThat(resized).isEqualTo(small);
    }

    @Test
    @DisplayName("다른 크기보다 큰지 확인할 수 있다")
    void isLargerThan() {
        // given
        ImageDimension large = ImageDimension.of(1920, 1080);
        ImageDimension small = ImageDimension.of(800, 600);

        // when & then
        assertThat(large.isLargerThan(small)).isTrue();
        assertThat(small.isLargerThan(large)).isFalse();
    }

    @Test
    @DisplayName("썸네일 크기인지 확인할 수 있다")
    void isThumbnailSize() {
        // given
        ImageDimension thumbnail = ImageDimension.of(800, 600);
        ImageDimension large = ImageDimension.of(1920, 1080);

        // when & then
        assertThat(thumbnail.isThumbnailSize()).isTrue();
        assertThat(large.isThumbnailSize()).isFalse();
    }

    @Test
    @DisplayName("toString은 '너비x높이' 형식으로 반환한다")
    void toStringFormat() {
        // given
        ImageDimension dimension = ImageDimension.of(1920, 1080);

        // when
        String result = dimension.toString();

        // then
        assertThat(result).isEqualTo("1920x1080");
    }
}
