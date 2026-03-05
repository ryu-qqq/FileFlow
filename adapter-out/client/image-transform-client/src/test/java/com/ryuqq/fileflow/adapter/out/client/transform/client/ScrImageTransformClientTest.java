package com.ryuqq.fileflow.adapter.out.client.transform.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.application.transform.dto.result.ImageProcessingResult;
import com.ryuqq.fileflow.application.transform.port.out.client.ImageTransformClient;
import com.ryuqq.fileflow.domain.transform.vo.TransformParams;
import com.ryuqq.fileflow.domain.transform.vo.TransformType;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.nio.PngWriter;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("ScrImageTransformClient 단위 테스트")
class ScrImageTransformClientTest {

    private ScrImageTransformClient sut;

    @BeforeEach
    void setUp() {
        sut = new ScrImageTransformClient();
    }

    @Nested
    @DisplayName("process 메서드")
    class Process {

        @Test
        @DisplayName("성공: RESIZE 변환 시 지정된 크기로 리사이즈한다")
        void shouldResizeImage() throws IOException {
            // given
            byte[] sourceImageBytes = createTestImageBytes(200, 160);
            TransformParams params = TransformParams.forResize(100, 80, false);

            // when
            ImageProcessingResult result =
                    sut.process(sourceImageBytes, TransformType.RESIZE, params);

            // then
            assertThat(result.width()).isEqualTo(100);
            assertThat(result.height()).isEqualTo(80);
            assertThat(result.contentType()).isEqualTo("image/png");
            assertThat(result.extension()).isEqualTo("png");
            assertThat(result.data()).isNotEmpty();
        }

        @Test
        @DisplayName("성공: THUMBNAIL 변환 시 커버 크롭으로 썸네일을 생성한다")
        void shouldCreateThumbnailWithCoverCrop() throws IOException {
            // given
            byte[] sourceImageBytes = createTestImageBytes(200, 160);
            TransformParams params = TransformParams.forThumbnail(50, 50);

            // when
            ImageProcessingResult result =
                    sut.process(sourceImageBytes, TransformType.THUMBNAIL, params);

            // then
            assertThat(result.width()).isEqualTo(50);
            assertThat(result.height()).isEqualTo(50);
        }

        @Test
        @DisplayName("성공: CONVERT 변환 시 지정 포맷으로 변환한다")
        void shouldConvertImageFormat() throws IOException {
            // given
            byte[] sourceImageBytes = createTestImageBytes(100, 100);
            TransformParams params = TransformParams.forConvert("jpeg");

            // when
            ImageProcessingResult result =
                    sut.process(sourceImageBytes, TransformType.CONVERT, params);

            // then
            assertThat(result.contentType()).isEqualTo("image/jpeg");
            assertThat(result.extension()).isEqualTo("jpeg");
            assertThat(result.width()).isEqualTo(100);
            assertThat(result.height()).isEqualTo(100);
        }

        @Test
        @DisplayName("성공: COMPRESS 변환 시 JPEG 압축 품질을 적용한다")
        void shouldCompressWithQuality() throws IOException {
            // given
            byte[] sourceImageBytes = createTestImageBytes(100, 100);
            TransformParams params = TransformParams.forCompress(50);

            // when
            ImageProcessingResult result =
                    sut.process(sourceImageBytes, TransformType.COMPRESS, params);

            // then
            assertThat(result.fileSize()).isGreaterThan(0);
        }

        @Test
        @DisplayName("성공: ImageTransformClient 인터페이스를 구현한다")
        void shouldImplementImageTransformClient() {
            assertThat(sut).isInstanceOf(ImageTransformClient.class);
        }

        @Test
        @DisplayName("실패: RESIZE 시 소스 이미지가 5x5 미만이면 예외를 던진다")
        void shouldThrowWhenResizeSourceTooSmall() throws IOException {
            // given
            byte[] tinyImageBytes = createTestImageBytes(1, 1);
            TransformParams params = TransformParams.forResize(100, 100, false);

            // when & then
            assertThatThrownBy(() -> sut.process(tinyImageBytes, TransformType.RESIZE, params))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("리샘플링 최소 요건 미달")
                    .hasMessageContaining("1x1");
        }

        @Test
        @DisplayName("실패: THUMBNAIL 시 소스 이미지가 5x5 미만이면 예외를 던진다")
        void shouldThrowWhenThumbnailSourceTooSmall() throws IOException {
            // given
            byte[] tinyImageBytes = createTestImageBytes(3, 3);
            TransformParams params = TransformParams.forThumbnail(50, 50);

            // when & then
            assertThatThrownBy(() -> sut.process(tinyImageBytes, TransformType.THUMBNAIL, params))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("리샘플링 최소 요건 미달")
                    .hasMessageContaining("3x3");
        }

        @Test
        @DisplayName("성공: CONVERT 시 소스 이미지가 1x1이어도 정상 처리한다")
        void shouldConvertEvenWithTinyImage() throws IOException {
            // given
            byte[] tinyImageBytes = createTestImageBytes(1, 1);
            TransformParams params = TransformParams.forConvert("jpeg");

            // when
            ImageProcessingResult result =
                    sut.process(tinyImageBytes, TransformType.CONVERT, params);

            // then
            assertThat(result.width()).isEqualTo(1);
            assertThat(result.height()).isEqualTo(1);
        }
    }

    private byte[] createTestImageBytes(int width, int height) throws IOException {
        ImmutableImage image = ImmutableImage.create(width, height);
        return image.bytes(PngWriter.NoCompression);
    }
}
