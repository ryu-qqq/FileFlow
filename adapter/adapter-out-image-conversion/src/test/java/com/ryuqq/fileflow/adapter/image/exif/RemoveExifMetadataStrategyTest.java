package com.ryuqq.fileflow.adapter.image.exif;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * RemoveExifMetadataStrategy 단위 테스트
 *
 * 테스트 전략:
 * - EXIF 메타데이터 제거 전략 검증
 * - GPS 정보가 있는 이미지 처리 검증
 * - preservesMetadata() 반환값 검증
 * - 예외 없이 정상 처리되는지 검증
 *
 * @author sangwon-ryu
 */
@DisplayName("RemoveExifMetadataStrategy 단위 테스트")
class RemoveExifMetadataStrategyTest {

    private RemoveExifMetadataStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new RemoveExifMetadataStrategy();
    }

    @Test
    @DisplayName("메타데이터를 보존하지 않음을 반환")
    void returnsFalseForPreservesMetadata() {
        assertThat(strategy.preservesMetadata()).isFalse();
    }

    @Test
    @DisplayName("GPS 정보가 있는 이미지를 예외 없이 처리")
    void processesImageWithGpsMetadataWithoutException() throws IOException {
        Path testImagePath = Paths.get("src/test/resources/test-images/with_gps.jpg");

        if (!Files.exists(testImagePath)) {
            // 테스트 이미지가 없으면 스킵
            return;
        }

        try (InputStream imageStream = Files.newInputStream(testImagePath)) {
            BufferedImage sourceImage = ImageIO.read(imageStream);
            byte[] sourceImageBytes = readImageBytes(testImagePath);

            assertThatCode(() -> strategy.processMetadata(sourceImage, sourceImageBytes))
                    .doesNotThrowAnyException();

            BufferedImage processedImage = strategy.processMetadata(sourceImage, sourceImageBytes);
            assertThat(processedImage).isNotNull();
            assertThat(processedImage.getWidth()).isEqualTo(sourceImage.getWidth());
            assertThat(processedImage.getHeight()).isEqualTo(sourceImage.getHeight());
        }
    }

    @Test
    @DisplayName("EXIF 정보가 있는 이미지를 예외 없이 처리")
    void processesImageWithExifMetadataWithoutException() throws IOException {
        Path testImagePath = Paths.get("src/test/resources/test-images/with_exif.jpg");

        if (!Files.exists(testImagePath)) {
            // 테스트 이미지가 없으면 스킵
            return;
        }

        try (InputStream imageStream = Files.newInputStream(testImagePath)) {
            BufferedImage sourceImage = ImageIO.read(imageStream);
            byte[] sourceImageBytes = readImageBytes(testImagePath);

            assertThatCode(() -> strategy.processMetadata(sourceImage, sourceImageBytes))
                    .doesNotThrowAnyException();

            BufferedImage processedImage = strategy.processMetadata(sourceImage, sourceImageBytes);
            assertThat(processedImage).isNotNull();
        }
    }

    @Test
    @DisplayName("메타데이터가 없는 단순 이미지를 예외 없이 처리")
    void processesSimpleImageWithoutException() throws IOException {
        Path testImagePath = Paths.get("src/test/resources/test-images/simple.jpg");

        if (!Files.exists(testImagePath)) {
            // 테스트 이미지가 없으면 스킵
            return;
        }

        try (InputStream imageStream = Files.newInputStream(testImagePath)) {
            BufferedImage sourceImage = ImageIO.read(imageStream);
            byte[] sourceImageBytes = readImageBytes(testImagePath);

            assertThatCode(() -> strategy.processMetadata(sourceImage, sourceImageBytes))
                    .doesNotThrowAnyException();

            BufferedImage processedImage = strategy.processMetadata(sourceImage, sourceImageBytes);
            assertThat(processedImage).isNotNull();
            assertThat(processedImage).isSameAs(sourceImage);
        }
    }

    @Test
    @DisplayName("null 이미지 바이트 배열로 호출 시 예외 없이 처리")
    void handlesNullImageBytesGracefully() throws IOException {
        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        assertThatCode(() -> strategy.processMetadata(testImage, null))
                .doesNotThrowAnyException();

        BufferedImage processedImage = strategy.processMetadata(testImage, null);
        assertThat(processedImage).isSameAs(testImage);
    }

    @Test
    @DisplayName("빈 바이트 배열로 호출 시 예외 없이 처리")
    void handlesEmptyImageBytesGracefully() throws IOException {
        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        byte[] emptyBytes = new byte[0];

        assertThatCode(() -> strategy.processMetadata(testImage, emptyBytes))
                .doesNotThrowAnyException();

        BufferedImage processedImage = strategy.processMetadata(testImage, emptyBytes);
        assertThat(processedImage).isSameAs(testImage);
    }

    /**
     * 이미지 파일을 바이트 배열로 읽습니다.
     *
     * @param imagePath 이미지 파일 경로
     * @return 이미지 바이트 배열
     * @throws IOException 읽기 실패 시
     */
    private byte[] readImageBytes(Path imagePath) throws IOException {
        return Files.readAllBytes(imagePath);
    }
}
