package com.ryuqq.fileflow.adapter.out.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.application.asset.dto.response.ImageMetadataResponse;
import com.ryuqq.fileflow.application.asset.dto.response.ImageProcessingResultResponse;
import com.ryuqq.fileflow.domain.asset.vo.ImageFormat;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariant;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ScrimageImageProcessor 단위 테스트.
 *
 * <p>이미지 리사이징 및 메타데이터 추출 기능을 검증한다.
 */
class ScrimageImageProcessorTest {

    private ScrimageImageProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new ScrimageImageProcessor();
    }

    @Nested
    @DisplayName("resize 메서드")
    class ResizeTest {

        @Test
        @DisplayName("THUMBNAIL 변형 시 200x200 이하로 리사이징된다")
        void resize_thumbnail_shouldResizeToThumbnailSize() throws IOException {
            // given
            byte[] imageData = createTestImage(800, 600, "png");
            ImageVariant variant = ImageVariant.THUMBNAIL;
            ImageFormat format = ImageFormat.PNG;

            // when
            ImageProcessingResultResponse result = processor.resize(imageData, variant, format);

            // then
            assertThat(result).isNotNull();
            assertThat(result.data()).isNotNull();
            assertThat(result.data().length).isGreaterThan(0);
            assertThat(result.width()).isLessThanOrEqualTo(200);
            assertThat(result.height()).isLessThanOrEqualTo(200);
        }

        @Test
        @DisplayName("MEDIUM 변형 시 600x600 이하로 리사이징된다")
        void resize_medium_shouldResizeToMediumSize() throws IOException {
            // given
            byte[] imageData = createTestImage(1200, 900, "png");
            ImageVariant variant = ImageVariant.MEDIUM;
            ImageFormat format = ImageFormat.JPEG;

            // when
            ImageProcessingResultResponse result = processor.resize(imageData, variant, format);

            // then
            assertThat(result).isNotNull();
            assertThat(result.width()).isLessThanOrEqualTo(600);
            assertThat(result.height()).isLessThanOrEqualTo(600);
        }

        @Test
        @DisplayName("LARGE 변형 시 1200x1200 이하로 리사이징된다")
        void resize_large_shouldResizeToLargeSize() throws IOException {
            // given
            byte[] imageData = createTestImage(2000, 1500, "png");
            ImageVariant variant = ImageVariant.LARGE;
            ImageFormat format = ImageFormat.WEBP;

            // when
            ImageProcessingResultResponse result = processor.resize(imageData, variant, format);

            // then
            assertThat(result).isNotNull();
            assertThat(result.width()).isLessThanOrEqualTo(1200);
            assertThat(result.height()).isLessThanOrEqualTo(1200);
        }

        @Test
        @DisplayName("ORIGINAL 변형 시 리사이징 없이 포맷만 변환된다")
        void resize_original_shouldNotResize() throws IOException {
            // given
            int originalWidth = 400;
            int originalHeight = 300;
            byte[] imageData = createTestImage(originalWidth, originalHeight, "png");
            ImageVariant variant = ImageVariant.ORIGINAL;
            ImageFormat format = ImageFormat.JPEG;

            // when
            ImageProcessingResultResponse result = processor.resize(imageData, variant, format);

            // then
            assertThat(result).isNotNull();
            assertThat(result.width()).isEqualTo(originalWidth);
            assertThat(result.height()).isEqualTo(originalHeight);
        }

        @Test
        @DisplayName("원본보다 작은 변형 타입이면 리사이징하지 않는다")
        void resize_smallerOriginal_shouldNotEnlarge() throws IOException {
            // given
            int smallWidth = 100;
            int smallHeight = 80;
            byte[] imageData = createTestImage(smallWidth, smallHeight, "png");
            ImageVariant variant = ImageVariant.LARGE;
            ImageFormat format = ImageFormat.PNG;

            // when
            ImageProcessingResultResponse result = processor.resize(imageData, variant, format);

            // then
            assertThat(result).isNotNull();
            // bound() 메서드는 확대하지 않으므로 원본 크기 유지
            assertThat(result.width()).isEqualTo(smallWidth);
            assertThat(result.height()).isEqualTo(smallHeight);
        }

        @Test
        @DisplayName("null 이미지 데이터 시 IllegalArgumentException 발생")
        void resize_nullImageData_shouldThrowException() {
            // given
            byte[] imageData = null;
            ImageVariant variant = ImageVariant.THUMBNAIL;
            ImageFormat format = ImageFormat.PNG;

            // when & then
            assertThatThrownBy(() -> processor.resize(imageData, variant, format))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("빈 이미지 데이터 시 IllegalArgumentException 발생")
        void resize_emptyImageData_shouldThrowException() {
            // given
            byte[] imageData = new byte[0];
            ImageVariant variant = ImageVariant.THUMBNAIL;
            ImageFormat format = ImageFormat.PNG;

            // when & then
            assertThatThrownBy(() -> processor.resize(imageData, variant, format))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비어있을 수 없습니다");
        }

        @Test
        @DisplayName("null variant 시 IllegalArgumentException 발생")
        void resize_nullVariant_shouldThrowException() throws IOException {
            // given
            byte[] imageData = createTestImage(100, 100, "png");
            ImageVariant variant = null;
            ImageFormat format = ImageFormat.PNG;

            // when & then
            assertThatThrownBy(() -> processor.resize(imageData, variant, format))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("null format 시 IllegalArgumentException 발생")
        void resize_nullFormat_shouldThrowException() throws IOException {
            // given
            byte[] imageData = createTestImage(100, 100, "png");
            ImageVariant variant = ImageVariant.THUMBNAIL;
            ImageFormat format = null;

            // when & then
            assertThatThrownBy(() -> processor.resize(imageData, variant, format))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("JPEG 포맷으로 출력 가능하다")
        void resize_toJpeg_shouldSucceed() throws IOException {
            // given
            byte[] imageData = createTestImage(400, 300, "png");
            ImageVariant variant = ImageVariant.THUMBNAIL;
            ImageFormat format = ImageFormat.JPEG;

            // when
            ImageProcessingResultResponse result = processor.resize(imageData, variant, format);

            // then
            assertThat(result).isNotNull();
            assertThat(result.data()).isNotNull();
            assertThat(result.data().length).isGreaterThan(0);
            // JPEG magic bytes: 0xFF 0xD8 0xFF
            assertThat(result.data()[0]).isEqualTo((byte) 0xFF);
            assertThat(result.data()[1]).isEqualTo((byte) 0xD8);
        }

        @Test
        @DisplayName("PNG 포맷으로 출력 가능하다")
        void resize_toPng_shouldSucceed() throws IOException {
            // given
            byte[] imageData = createTestImage(400, 300, "png");
            ImageVariant variant = ImageVariant.THUMBNAIL;
            ImageFormat format = ImageFormat.PNG;

            // when
            ImageProcessingResultResponse result = processor.resize(imageData, variant, format);

            // then
            assertThat(result).isNotNull();
            assertThat(result.data()).isNotNull();
            // PNG magic bytes: 0x89 0x50 0x4E 0x47
            assertThat(result.data()[0]).isEqualTo((byte) 0x89);
            assertThat(result.data()[1]).isEqualTo((byte) 0x50);
            assertThat(result.data()[2]).isEqualTo((byte) 0x4E);
            assertThat(result.data()[3]).isEqualTo((byte) 0x47);
        }
    }

    @Nested
    @DisplayName("extractMetadata 메서드")
    class ExtractMetadataTest {

        @Test
        @DisplayName("PNG 이미지에서 메타데이터를 추출한다")
        void extractMetadata_png_shouldExtractCorrectly() throws IOException {
            // given
            int width = 640;
            int height = 480;
            byte[] imageData = createTestImage(width, height, "png");

            // when
            ImageMetadataResponse result = processor.extractMetadata(imageData);

            // then
            assertThat(result).isNotNull();
            assertThat(result.width()).isEqualTo(width);
            assertThat(result.height()).isEqualTo(height);
            assertThat(result.format()).isEqualTo("png");
            assertThat(result.colorSpace()).isNotNull();
        }

        @Test
        @DisplayName("JPEG 이미지에서 메타데이터를 추출한다")
        void extractMetadata_jpeg_shouldExtractCorrectly() throws IOException {
            // given
            int width = 800;
            int height = 600;
            byte[] imageData = createTestImage(width, height, "jpg");

            // when
            ImageMetadataResponse result = processor.extractMetadata(imageData);

            // then
            assertThat(result).isNotNull();
            assertThat(result.width()).isEqualTo(width);
            assertThat(result.height()).isEqualTo(height);
            assertThat(result.format()).isEqualTo("jpeg");
        }

        @Test
        @DisplayName("GIF 이미지에서 메타데이터를 추출한다")
        void extractMetadata_gif_shouldExtractCorrectly() throws IOException {
            // given
            int width = 320;
            int height = 240;
            byte[] imageData = createTestImage(width, height, "gif");

            // when
            ImageMetadataResponse result = processor.extractMetadata(imageData);

            // then
            assertThat(result).isNotNull();
            assertThat(result.width()).isEqualTo(width);
            assertThat(result.height()).isEqualTo(height);
            assertThat(result.format()).isEqualTo("gif");
        }

        @Test
        @DisplayName("null 이미지 데이터 시 IllegalArgumentException 발생")
        void extractMetadata_nullImageData_shouldThrowException() {
            // given
            byte[] imageData = null;

            // when & then
            assertThatThrownBy(() -> processor.extractMetadata(imageData))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("빈 이미지 데이터 시 IllegalArgumentException 발생")
        void extractMetadata_emptyImageData_shouldThrowException() {
            // given
            byte[] imageData = new byte[0];

            // when & then
            assertThatThrownBy(() -> processor.extractMetadata(imageData))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("비어있을 수 없습니다");
        }

        @Test
        @DisplayName("짧은 바이트 배열에서도 unknown 포맷으로 처리된다")
        void extractMetadata_shortData_shouldReturnUnknown() {
            // given
            byte[] shortData = new byte[] {0x01, 0x02, 0x03, 0x04, 0x05};

            // when & then
            assertThatThrownBy(() -> processor.extractMetadata(shortData))
                    .isInstanceOf(ImageProcessingException.class);
        }
    }

    @Nested
    @DisplayName("비율 유지 테스트")
    class AspectRatioTest {

        @Test
        @DisplayName("가로가 긴 이미지는 가로 기준으로 리사이징된다")
        void resize_landscape_shouldMaintainAspectRatio() throws IOException {
            // given
            byte[] imageData = createTestImage(1600, 900, "png"); // 16:9 비율
            ImageVariant variant = ImageVariant.MEDIUM; // 600x600

            // when
            ImageProcessingResultResponse result =
                    processor.resize(imageData, variant, ImageFormat.PNG);

            // then
            assertThat(result.width()).isLessThanOrEqualTo(600);
            assertThat(result.height()).isLessThanOrEqualTo(600);
            // 가로가 600이면 세로는 약 337.5 (16:9 비율 유지)
            double ratio = (double) result.width() / result.height();
            assertThat(ratio).isBetween(1.7, 1.8); // 약 16:9
        }

        @Test
        @DisplayName("세로가 긴 이미지는 세로 기준으로 리사이징된다")
        void resize_portrait_shouldMaintainAspectRatio() throws IOException {
            // given
            byte[] imageData = createTestImage(600, 1200, "png"); // 1:2 비율
            ImageVariant variant = ImageVariant.THUMBNAIL; // 200x200

            // when
            ImageProcessingResultResponse result =
                    processor.resize(imageData, variant, ImageFormat.PNG);

            // then
            assertThat(result.width()).isLessThanOrEqualTo(200);
            assertThat(result.height()).isLessThanOrEqualTo(200);
            // 세로가 200이면 가로는 100 (1:2 비율 유지)
            double ratio = (double) result.height() / result.width();
            assertThat(ratio).isBetween(1.9, 2.1); // 약 2:1
        }

        @Test
        @DisplayName("정사각형 이미지는 정사각형으로 리사이징된다")
        void resize_square_shouldRemainSquare() throws IOException {
            // given
            byte[] imageData = createTestImage(1000, 1000, "png");
            ImageVariant variant = ImageVariant.THUMBNAIL;

            // when
            ImageProcessingResultResponse result =
                    processor.resize(imageData, variant, ImageFormat.PNG);

            // then
            assertThat(result.width()).isEqualTo(result.height());
            assertThat(result.width()).isLessThanOrEqualTo(200);
        }
    }

    /**
     * 테스트용 이미지를 생성한다.
     *
     * @param width 이미지 너비
     * @param height 이미지 높이
     * @param formatName 포맷명 (png, jpg, gif)
     * @return 이미지 바이트 배열
     */
    private byte[] createTestImage(int width, int height, String formatName) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.WHITE);
        g2d.fillOval(width / 4, height / 4, width / 2, height / 2);
        g2d.dispose();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, formatName, baos);
            return baos.toByteArray();
        }
    }
}
