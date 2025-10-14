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
 * SmallThumbnailStrategy 단위 테스트
 *
 * 테스트 시나리오:
 * 1. 정상적인 리사이징 (Aspect Ratio 유지)
 * 2. 정상적인 리사이징 (Aspect Ratio 무시)
 * 3. 업스케일링 방지 (원본이 더 작은 경우)
 * 4. 파일명 생성 검증
 * 5. 예외 처리 검증
 *
 * @author sangwon-ryu
 */
class SmallThumbnailStrategyTest {

    private SmallThumbnailStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new SmallThumbnailStrategy();
    }

    @Test
    @DisplayName("300x300보다 큰 이미지를 Aspect Ratio 유지하며 리사이징해야 한다")
    void shouldResizeImageWithAspectRatio() {
        // given: 1000x800 이미지
        BufferedImage sourceImage = new BufferedImage(1000, 800, BufferedImage.TYPE_INT_RGB);

        // when: Aspect Ratio 유지하며 썸네일 생성
        BufferedImage thumbnail = strategy.generateThumbnail(sourceImage, true);

        // then: 300x240으로 리사이징 (비율 유지)
        assertThat(thumbnail.getWidth()).isLessThanOrEqualTo(300);
        assertThat(thumbnail.getHeight()).isLessThanOrEqualTo(300);

        // Aspect Ratio 검증 (오차 허용: 1픽셀)
        double sourceRatio = (double) sourceImage.getWidth() / sourceImage.getHeight();
        double thumbnailRatio = (double) thumbnail.getWidth() / thumbnail.getHeight();
        assertThat(thumbnailRatio).isCloseTo(sourceRatio, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    @DisplayName("300x300보다 큰 이미지를 Aspect Ratio 무시하고 정확히 300x300으로 리사이징해야 한다")
    void shouldResizeImageWithoutAspectRatio() {
        // given: 1000x800 이미지
        BufferedImage sourceImage = new BufferedImage(1000, 800, BufferedImage.TYPE_INT_RGB);

        // when: Aspect Ratio 무시하고 썸네일 생성
        BufferedImage thumbnail = strategy.generateThumbnail(sourceImage, false);

        // then: 정확히 300x300으로 리사이징
        assertThat(thumbnail.getWidth()).isEqualTo(300);
        assertThat(thumbnail.getHeight()).isEqualTo(300);
    }

    @Test
    @DisplayName("원본이 300x300보다 작으면 업스케일링 하지 않고 원본을 반환해야 한다")
    void shouldNotUpscaleWhenSourceIsSmaller() {
        // given: 200x200 이미지 (타겟보다 작음)
        BufferedImage sourceImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);

        // when: 썸네일 생성
        BufferedImage thumbnail = strategy.generateThumbnail(sourceImage, true);

        // then: 원본 그대로 반환 (업스케일링 방지)
        assertThat(thumbnail).isSameAs(sourceImage);
        assertThat(thumbnail.getWidth()).isEqualTo(200);
        assertThat(thumbnail.getHeight()).isEqualTo(200);
    }

    @Test
    @DisplayName("정사각형 이미지를 처리해야 한다")
    void shouldHandleSquareImage() {
        // given: 1000x1000 정사각형 이미지
        BufferedImage sourceImage = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);

        // when: Aspect Ratio 유지하며 썸네일 생성
        BufferedImage thumbnail = strategy.generateThumbnail(sourceImage, true);

        // then: 300x300 정사각형으로 리사이징
        assertThat(thumbnail.getWidth()).isEqualTo(300);
        assertThat(thumbnail.getHeight()).isEqualTo(300);
    }

    @Test
    @DisplayName("세로가 긴 이미지를 Aspect Ratio 유지하며 리사이징해야 한다")
    void shouldResizePortraitImageWithAspectRatio() {
        // given: 800x1000 세로 이미지
        BufferedImage sourceImage = new BufferedImage(800, 1000, BufferedImage.TYPE_INT_RGB);

        // when: Aspect Ratio 유지하며 썸네일 생성
        BufferedImage thumbnail = strategy.generateThumbnail(sourceImage, true);

        // then: 240x300으로 리사이징 (비율 유지)
        assertThat(thumbnail.getWidth()).isLessThanOrEqualTo(300);
        assertThat(thumbnail.getHeight()).isLessThanOrEqualTo(300);

        double sourceRatio = (double) sourceImage.getWidth() / sourceImage.getHeight();
        double thumbnailRatio = (double) thumbnail.getWidth() / thumbnail.getHeight();
        assertThat(thumbnailRatio).isCloseTo(sourceRatio, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    @DisplayName("썸네일 파일명을 올바른 규칙으로 생성해야 한다")
    void shouldGenerateThumbnailFileName() {
        // given: 원본 파일명
        String originalFileName = "product-123.jpg";

        // when: 썸네일 파일명 생성
        String thumbnailFileName = strategy.generateThumbnailFileName(originalFileName);

        // then: {원본명}_small.webp 형식
        assertThat(thumbnailFileName).isEqualTo("product-123_small.webp");
    }

    @Test
    @DisplayName("확장자가 없는 파일명도 처리해야 한다")
    void shouldHandleFileNameWithoutExtension() {
        // given: 확장자가 없는 파일명
        String originalFileName = "product-123";

        // when: 썸네일 파일명 생성
        String thumbnailFileName = strategy.generateThumbnailFileName(originalFileName);

        // then: {원본명}_small.webp 형식
        assertThat(thumbnailFileName).isEqualTo("product-123_small.webp");
    }

    @Test
    @DisplayName("지원하는 썸네일 크기는 SMALL이어야 한다")
    void shouldSupportSmallSize() {
        // when: 지원 크기 조회
        ThumbnailSize supportedSize = strategy.getSupportedSize();

        // then: SMALL
        assertThat(supportedSize).isEqualTo(ThumbnailSize.SMALL);
    }

    @Test
    @DisplayName("타겟 크기는 300x300이어야 한다")
    void shouldHaveTargetDimension300x300() {
        // when: 타겟 크기 조회
        ImageDimension targetDimension = strategy.getTargetDimension();

        // then: 300x300
        assertThat(targetDimension.getWidth()).isEqualTo(300);
        assertThat(targetDimension.getHeight()).isEqualTo(300);
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
    @DisplayName("null 또는 빈 파일명을 전달하면 예외가 발생해야 한다")
    void shouldThrowExceptionWhenFileNameIsNullOrBlank() {
        // when & then: null 파일명
        assertThatThrownBy(() -> strategy.generateThumbnailFileName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Original file name cannot be null or blank");

        // when & then: 빈 파일명
        assertThatThrownBy(() -> strategy.generateThumbnailFileName("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Original file name cannot be null or blank");
    }

    @Test
    @DisplayName("업스케일링이 필요한지 정확히 판단해야 한다")
    void shouldCorrectlyDetectUpscalingNeed() {
        // given: 200x200 이미지 (타겟보다 작음)
        BufferedImage smallImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);

        // given: 1000x1000 이미지 (타겟보다 큼)
        BufferedImage largeImage = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);

        // when & then: 작은 이미지는 업스케일링 필요
        assertThat(strategy.needsUpscaling(smallImage)).isTrue();

        // when & then: 큰 이미지는 업스케일링 불필요
        assertThat(strategy.needsUpscaling(largeImage)).isFalse();
    }
}
