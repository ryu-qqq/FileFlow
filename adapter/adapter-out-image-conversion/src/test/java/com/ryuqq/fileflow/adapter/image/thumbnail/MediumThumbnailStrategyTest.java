package com.ryuqq.fileflow.adapter.image.thumbnail;

import com.ryuqq.fileflow.domain.image.command.GenerateThumbnailCommand.ThumbnailSize;
import com.ryuqq.fileflow.domain.image.vo.ImageDimension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MediumThumbnailStrategy 단위 테스트
 *
 * 테스트 시나리오:
 * 1. 정상적인 리사이징 (Aspect Ratio 유지)
 * 2. 정상적인 리사이징 (Aspect Ratio 무시)
 * 3. 업스케일링 방지 (원본이 더 작은 경우)
 * 4. 다양한 크기의 이미지 처리
 * 5. 예외 처리 검증
 *
 * @author sangwon-ryu
 */
class MediumThumbnailStrategyTest {

    private MediumThumbnailStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new MediumThumbnailStrategy();
    }

    @Test
    @DisplayName("800x800보다 큰 이미지를 Aspect Ratio 유지하며 리사이징해야 한다")
    void shouldResizeImageWithAspectRatio() {
        // given: 2000x1600 이미지
        BufferedImage sourceImage = new BufferedImage(2000, 1600, BufferedImage.TYPE_INT_RGB);

        // when: Aspect Ratio 유지하며 썸네일 생성
        BufferedImage thumbnail = strategy.generateThumbnail(sourceImage, true);

        // then: 800x640으로 리사이징 (비율 유지)
        assertThat(thumbnail.getWidth()).isLessThanOrEqualTo(800);
        assertThat(thumbnail.getHeight()).isLessThanOrEqualTo(800);

        // Aspect Ratio 검증 (오차 허용: 1픽셀)
        double sourceRatio = (double) sourceImage.getWidth() / sourceImage.getHeight();
        double thumbnailRatio = (double) thumbnail.getWidth() / thumbnail.getHeight();
        assertThat(thumbnailRatio).isCloseTo(sourceRatio, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    @DisplayName("800x800보다 큰 이미지를 Aspect Ratio 무시하고 정확히 800x800으로 리사이징해야 한다")
    void shouldResizeImageWithoutAspectRatio() {
        // given: 2000x1600 이미지
        BufferedImage sourceImage = new BufferedImage(2000, 1600, BufferedImage.TYPE_INT_RGB);

        // when: Aspect Ratio 무시하고 썸네일 생성
        BufferedImage thumbnail = strategy.generateThumbnail(sourceImage, false);

        // then: 정확히 800x800으로 리사이징
        assertThat(thumbnail.getWidth()).isEqualTo(800);
        assertThat(thumbnail.getHeight()).isEqualTo(800);
    }

    @Test
    @DisplayName("원본이 800x800보다 작으면 업스케일링 하지 않고 원본을 반환해야 한다")
    void shouldNotUpscaleWhenSourceIsSmaller() {
        // given: 600x600 이미지 (타겟보다 작음)
        BufferedImage sourceImage = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);

        // when: 썸네일 생성
        BufferedImage thumbnail = strategy.generateThumbnail(sourceImage, true);

        // then: 원본 그대로 반환 (업스케일링 방지)
        assertThat(thumbnail).isSameAs(sourceImage);
        assertThat(thumbnail.getWidth()).isEqualTo(600);
        assertThat(thumbnail.getHeight()).isEqualTo(600);
    }

    @Test
    @DisplayName("정사각형 이미지를 처리해야 한다")
    void shouldHandleSquareImage() {
        // given: 2000x2000 정사각형 이미지
        BufferedImage sourceImage = new BufferedImage(2000, 2000, BufferedImage.TYPE_INT_RGB);

        // when: Aspect Ratio 유지하며 썸네일 생성
        BufferedImage thumbnail = strategy.generateThumbnail(sourceImage, true);

        // then: 800x800 정사각형으로 리사이징
        assertThat(thumbnail.getWidth()).isEqualTo(800);
        assertThat(thumbnail.getHeight()).isEqualTo(800);
    }

    @Test
    @DisplayName("세로가 긴 이미지를 Aspect Ratio 유지하며 리사이징해야 한다")
    void shouldResizePortraitImageWithAspectRatio() {
        // given: 1600x2000 세로 이미지
        BufferedImage sourceImage = new BufferedImage(1600, 2000, BufferedImage.TYPE_INT_RGB);

        // when: Aspect Ratio 유지하며 썸네일 생성
        BufferedImage thumbnail = strategy.generateThumbnail(sourceImage, true);

        // then: 640x800으로 리사이징 (비율 유지)
        assertThat(thumbnail.getWidth()).isLessThanOrEqualTo(800);
        assertThat(thumbnail.getHeight()).isLessThanOrEqualTo(800);

        double sourceRatio = (double) sourceImage.getWidth() / sourceImage.getHeight();
        double thumbnailRatio = (double) thumbnail.getWidth() / thumbnail.getHeight();
        assertThat(thumbnailRatio).isCloseTo(sourceRatio, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    @DisplayName("지원하는 썸네일 크기는 MEDIUM이어야 한다")
    void shouldSupportMediumSize() {
        // when: 지원 크기 조회
        ThumbnailSize supportedSize = strategy.getSupportedSize();

        // then: MEDIUM
        assertThat(supportedSize).isEqualTo(ThumbnailSize.MEDIUM);
    }

    @Test
    @DisplayName("타겟 크기는 800x800이어야 한다")
    void shouldHaveTargetDimension800x800() {
        // when: 타겟 크기 조회
        ImageDimension targetDimension = strategy.getTargetDimension();

        // then: 800x800
        assertThat(targetDimension.getWidth()).isEqualTo(800);
        assertThat(targetDimension.getHeight()).isEqualTo(800);
    }

    @Test
    @DisplayName("null 이미지를 전달하면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenSourceImageIsNull() {
        // when & then: null 이미지 전달 시 예외 발생
        assertThatThrownBy(() -> strategy.generateThumbnail(null, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Source image cannot be null");
    }

    @Test
    @DisplayName("업스케일링이 필요한지 정확히 판단해야 한다")
    void shouldCorrectlyDetectUpscalingNeed() {
        // given: 600x600 이미지 (타겟보다 작음)
        BufferedImage smallImage = new BufferedImage(600, 600, BufferedImage.TYPE_INT_RGB);

        // given: 2000x2000 이미지 (타겟보다 큼)
        BufferedImage largeImage = new BufferedImage(2000, 2000, BufferedImage.TYPE_INT_RGB);

        // when & then: 작은 이미지는 업스케일링 필요
        assertThat(strategy.needsUpscaling(smallImage)).isTrue();

        // when & then: 큰 이미지는 업스케일링 불필요
        assertThat(strategy.needsUpscaling(largeImage)).isFalse();
    }

    @Test
    @DisplayName("가로가 매우 긴 이미지를 Aspect Ratio 유지하며 리사이징해야 한다")
    void shouldResizeLandscapeImageWithAspectRatio() {
        // given: 3000x1000 가로 이미지
        BufferedImage sourceImage = new BufferedImage(3000, 1000, BufferedImage.TYPE_INT_RGB);

        // when: Aspect Ratio 유지하며 썸네일 생성
        BufferedImage thumbnail = strategy.generateThumbnail(sourceImage, true);

        // then: 비율 유지 (세로가 800을 초과하지 않음)
        assertThat(thumbnail.getWidth()).isLessThanOrEqualTo(800);
        assertThat(thumbnail.getHeight()).isLessThanOrEqualTo(800);

        double sourceRatio = (double) sourceImage.getWidth() / sourceImage.getHeight();
        double thumbnailRatio = (double) thumbnail.getWidth() / thumbnail.getHeight();
        assertThat(thumbnailRatio).isCloseTo(sourceRatio, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    @DisplayName("Small 크기보다 크고 Medium 크기보다 작은 이미지는 원본을 반환해야 한다")
    void shouldReturnOriginalWhenSizeBetweenSmallAndMedium() {
        // given: 700x700 이미지 (Small: 300, Medium: 800)
        BufferedImage sourceImage = new BufferedImage(700, 700, BufferedImage.TYPE_INT_RGB);

        // when: 썸네일 생성
        BufferedImage thumbnail = strategy.generateThumbnail(sourceImage, true);

        // then: 원본 그대로 반환 (업스케일링 방지)
        assertThat(thumbnail).isSameAs(sourceImage);
        assertThat(thumbnail.getWidth()).isEqualTo(700);
        assertThat(thumbnail.getHeight()).isEqualTo(700);
    }
}
