package com.ryuqq.fileflow.adapter.out.client.transform.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.fileflow.application.asset.dto.result.ImageMetadataResult;
import com.ryuqq.fileflow.application.asset.port.out.client.MetadataExtractionPort;
import com.sksamuel.scrimage.ImmutableImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("ImageMetadataExtractionClient 단위 테스트")
class ImageMetadataExtractionClientTest {

    private ImageMetadataExtractionClient sut;

    @BeforeEach
    void setUp() {
        sut = new ImageMetadataExtractionClient();
    }

    @Nested
    @DisplayName("extract 메서드")
    class Extract {

        @Test
        @DisplayName("성공: 이미지에서 width/height를 추출한다")
        void shouldExtractDimensions() throws Exception {
            // given
            byte[] imageBytes = createTestImageBytes(200, 150);

            // when
            ImageMetadataResult result = sut.extract(imageBytes);

            // then
            assertThat(result.width()).isEqualTo(200);
            assertThat(result.height()).isEqualTo(150);
        }

        @Test
        @DisplayName("성공: 정사각형 이미지의 메타데이터를 추출한다")
        void shouldExtractSquareImageMetadata() throws Exception {
            // given
            byte[] imageBytes = createTestImageBytes(500, 500);

            // when
            ImageMetadataResult result = sut.extract(imageBytes);

            // then
            assertThat(result.width()).isEqualTo(500);
            assertThat(result.height()).isEqualTo(500);
        }

        @Test
        @DisplayName("실패: 잘못된 바이트 데이터일 경우 예외를 던진다")
        void shouldThrowWhenInvalidImageBytes() {
            // given
            byte[] invalidBytes = new byte[] {0x00, 0x01, 0x02};

            // when & then
            assertThatThrownBy(() -> sut.extract(invalidBytes))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미지 로드 실패");
        }

        @Test
        @DisplayName("성공: MetadataExtractionPort 인터페이스를 구현한다")
        void shouldImplementMetadataExtractionPort() {
            assertThat(sut).isInstanceOf(MetadataExtractionPort.class);
        }
    }

    private byte[] createTestImageBytes(int width, int height) throws Exception {
        ImmutableImage image = ImmutableImage.create(width, height);
        return image.bytes(new com.sksamuel.scrimage.nio.PngWriter(0));
    }
}
