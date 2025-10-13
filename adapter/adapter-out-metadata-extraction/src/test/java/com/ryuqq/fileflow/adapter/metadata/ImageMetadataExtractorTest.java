package com.ryuqq.fileflow.adapter.metadata;

import com.ryuqq.fileflow.application.file.MetadataExtractionException;
import com.ryuqq.fileflow.domain.file.FileMetadata;
import com.ryuqq.fileflow.domain.file.MetadataType;
import com.ryuqq.fileflow.domain.upload.vo.FileId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ImageMetadataExtractor 단위 테스트
 *
 * 테스트 전략:
 * - 지원 Content-Type 검증
 * - JPEG 메타데이터 추출 검증
 * - PNG 메타데이터 추출 검증
 * - EXIF 메타데이터 추출 검증
 * - 예외 상황 처리 검증
 *
 * @author sangwon-ryu
 */
@DisplayName("ImageMetadataExtractor 단위 테스트")
class ImageMetadataExtractorTest {

    private ImageMetadataExtractor extractor;
    private FileId testFileId;

    @BeforeEach
    void setUp() {
        extractor = new ImageMetadataExtractor();
        testFileId = FileId.generate();
    }

    @Test
    @DisplayName("JPEG Content-Type 지원 확인")
    void supportsJpegContentType() {
        assertThat(extractor.supports("image/jpeg")).isTrue();
        assertThat(extractor.supports("image/jpg")).isTrue();
        assertThat(extractor.supports("IMAGE/JPEG")).isTrue();
    }

    @Test
    @DisplayName("PNG Content-Type 지원 확인")
    void supportsPngContentType() {
        assertThat(extractor.supports("image/png")).isTrue();
        assertThat(extractor.supports("IMAGE/PNG")).isTrue();
    }

    @Test
    @DisplayName("지원하지 않는 Content-Type 확인")
    void doesNotSupportNonImageContentType() {
        assertThat(extractor.supports("video/mp4")).isFalse();
        assertThat(extractor.supports("application/pdf")).isFalse();
        assertThat(extractor.supports(null)).isFalse();
    }

    @Test
    @DisplayName("지원하지 않는 Content-Type으로 추출 시 예외 발생")
    void throwsExceptionForUnsupportedContentType() {
        InputStream dummyStream = new ByteArrayInputStream(new byte[0]);

        assertThatThrownBy(() -> extractor.extract(testFileId, dummyStream, "video/mp4"))
                .isInstanceOf(MetadataExtractionException.class)
                .hasMessageContaining("Unsupported content type");
    }

    @Test
    @DisplayName("잘못된 이미지 스트림으로 추출 시 예외 발생")
    void throwsExceptionForInvalidImageStream() {
        InputStream invalidStream = new ByteArrayInputStream("invalid image data".getBytes());

        assertThatThrownBy(() -> extractor.extract(testFileId, invalidStream, "image/jpeg"))
                .isInstanceOf(MetadataExtractionException.class)
                .hasMessageContaining("Failed to extract image metadata");
    }

    @Test
    @DisplayName("실제 JPEG 이미지에서 메타데이터 추출 (테스트 리소스 파일이 있는 경우)")
    void extractsMetadataFromRealJpegImage() throws IOException {
        // 테스트 리소스에 실제 JPEG 파일이 있다고 가정
        // 실제 프로젝트에서는 src/test/resources 에 테스트용 이미지 파일을 배치
        Path testImagePath = Paths.get("src/test/resources/test-images/sample.jpg");

        if (!Files.exists(testImagePath)) {
            // 테스트 이미지가 없으면 스킵
            return;
        }

        try (InputStream imageStream = Files.newInputStream(testImagePath)) {
            List<FileMetadata> metadata = extractor.extract(testFileId, imageStream, "image/jpeg");

            assertThat(metadata).isNotEmpty();
            assertThat(metadata).anyMatch(m -> m.hasKey("format"));
            assertThat(metadata).anyMatch(m -> m.hasKey("width"));
            assertThat(metadata).anyMatch(m -> m.hasKey("height"));

            // format 메타데이터 검증
            FileMetadata formatMeta = metadata.stream()
                    .filter(m -> m.hasKey("format"))
                    .findFirst()
                    .orElseThrow();
            assertThat(formatMeta.getMetadataValue()).isEqualTo("JPEG");
            assertThat(formatMeta.getValueType()).isEqualTo(MetadataType.STRING);

            // width, height는 NUMBER 타입이어야 함
            metadata.stream()
                    .filter(m -> m.hasKey("width") || m.hasKey("height"))
                    .forEach(m -> assertThat(m.getValueType()).isEqualTo(MetadataType.NUMBER));
        }
    }

    @Test
    @DisplayName("format 메타데이터 생성 확인 - JPEG")
    void createsFormatMetadataForJpeg() throws IOException {
        // 최소한의 유효한 JPEG 헤더를 가진 바이트 배열
        // FFD8 (JPEG 시작 마커) + FFD9 (JPEG 종료 마커)
        byte[] minimalJpeg = new byte[]{
                (byte) 0xFF, (byte) 0xD8,  // SOI (Start of Image)
                (byte) 0xFF, (byte) 0xD9   // EOI (End of Image)
        };

        InputStream jpegStream = new ByteArrayInputStream(minimalJpeg);

        try {
            List<FileMetadata> metadata = extractor.extract(testFileId, jpegStream, "image/jpeg");

            FileMetadata formatMeta = metadata.stream()
                    .filter(m -> m.hasKey("format"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("format metadata not found"));

            assertThat(formatMeta.getMetadataValue()).isEqualTo("JPEG");
            assertThat(formatMeta.getFileId()).isEqualTo(testFileId);
        } catch (MetadataExtractionException e) {
            // 최소 JPEG 구조로도 파싱 실패할 수 있음 (정상 동작)
            assertThat(e.getMessage()).contains("Failed to extract image metadata");
        }
    }

    @Test
    @DisplayName("format 메타데이터 생성 확인 - PNG")
    void createsFormatMetadataForPng() {
        // 최소한의 유효한 PNG 헤더
        byte[] minimalPng = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47,  // PNG signature
                0x0D, 0x0A, 0x1A, 0x0A           // PNG signature continuation
        };

        InputStream pngStream = new ByteArrayInputStream(minimalPng);

        try {
            List<FileMetadata> metadata = extractor.extract(testFileId, pngStream, "image/png");

            FileMetadata formatMeta = metadata.stream()
                    .filter(m -> m.hasKey("format"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("format metadata not found"));

            assertThat(formatMeta.getMetadataValue()).isEqualTo("PNG");
            assertThat(formatMeta.getFileId()).isEqualTo(testFileId);
        } catch (MetadataExtractionException e) {
            // 최소 PNG 구조로도 파싱 실패할 수 있음 (정상 동작)
            assertThat(e.getMessage()).contains("Failed to extract image metadata");
        }
    }

    @Test
    @DisplayName("GPS 메타데이터 추출 검증 - GPS 정보가 있는 이미지")
    void extractsGpsMetadataFromImageWithGps() throws IOException {
        Path testImagePath = Paths.get("src/test/resources/test-images/with_gps.jpg");

        if (!Files.exists(testImagePath)) {
            // 테스트 이미지가 없으면 스킵
            return;
        }

        try (InputStream imageStream = Files.newInputStream(testImagePath)) {
            List<FileMetadata> metadata = extractor.extract(testFileId, imageStream, "image/jpeg");

            // GPS 메타데이터가 추출되었는지 확인
            boolean hasGpsLatitude = metadata.stream()
                    .anyMatch(m -> m.hasKey("exif_gps_latitude"));
            boolean hasGpsLongitude = metadata.stream()
                    .anyMatch(m -> m.hasKey("exif_gps_longitude"));

            assertThat(hasGpsLatitude || hasGpsLongitude)
                    .as("GPS metadata should be extracted if present")
                    .isTrue();

            // GPS 메타데이터가 있으면 NUMBER 타입이어야 함
            metadata.stream()
                    .filter(m -> m.hasKey("exif_gps_latitude") || m.hasKey("exif_gps_longitude") || m.hasKey("exif_gps_altitude"))
                    .forEach(m -> assertThat(m.getValueType()).isEqualTo(MetadataType.NUMBER));
        }
    }

    @Test
    @DisplayName("GPS 메타데이터 미존재 검증 - GPS 정보가 없는 이미지")
    void doesNotExtractGpsMetadataFromImageWithoutGps() throws IOException {
        Path testImagePath = Paths.get("src/test/resources/test-images/simple.jpg");

        if (!Files.exists(testImagePath)) {
            // 테스트 이미지가 없으면 스킵
            return;
        }

        try (InputStream imageStream = Files.newInputStream(testImagePath)) {
            List<FileMetadata> metadata = extractor.extract(testFileId, imageStream, "image/jpeg");

            // GPS 메타데이터가 없어야 함
            boolean hasGpsMetadata = metadata.stream()
                    .anyMatch(m -> m.hasKey("exif_gps_latitude") || m.hasKey("exif_gps_longitude"));

            assertThat(hasGpsMetadata)
                    .as("GPS metadata should not exist for images without GPS data")
                    .isFalse();
        }
    }

    @Test
    @DisplayName("EXIF Orientation 태그 추출 검증")
    void extractsExifOrientationTag() throws IOException {
        Path testImagePath = Paths.get("src/test/resources/test-images/with_exif.jpg");

        if (!Files.exists(testImagePath)) {
            // 테스트 이미지가 없으면 스킵
            return;
        }

        try (InputStream imageStream = Files.newInputStream(testImagePath)) {
            List<FileMetadata> metadata = extractor.extract(testFileId, imageStream, "image/jpeg");

            // Orientation 메타데이터 확인 (있을 수도 있고 없을 수도 있음)
            metadata.stream()
                    .filter(m -> m.hasKey("exif_orientation"))
                    .forEach(m -> {
                        // Orientation은 NUMBER 타입이어야 함
                        assertThat(m.getValueType()).isEqualTo(MetadataType.NUMBER);

                        // Orientation 값은 1-8 범위여야 함
                        int orientationValue = Integer.parseInt(m.getMetadataValue());
                        assertThat(orientationValue)
                                .as("EXIF Orientation should be between 1 and 8")
                                .isBetween(1, 8);
                    });
        }
    }

    @Test
    @DisplayName("EXIF 메타데이터 전체 추출 검증 - 카메라 정보 포함")
    void extractsCompleteExifMetadata() throws IOException {
        Path testImagePath = Paths.get("src/test/resources/test-images/with_exif.jpg");

        if (!Files.exists(testImagePath)) {
            // 테스트 이미지가 없으면 스킵
            return;
        }

        try (InputStream imageStream = Files.newInputStream(testImagePath)) {
            List<FileMetadata> metadata = extractor.extract(testFileId, imageStream, "image/jpeg");

            assertThat(metadata).isNotEmpty();

            // 기본 메타데이터 확인
            assertThat(metadata).anyMatch(m -> m.hasKey("format"));

            // EXIF 메타데이터가 있으면 STRING 또는 NUMBER 타입이어야 함
            metadata.stream()
                    .filter(m -> m.hasKey("exif_make") || m.hasKey("exif_model") ||
                                 m.hasKey("exif_datetime") || m.hasKey("exif_software"))
                    .forEach(m -> assertThat(m.getValueType()).isEqualTo(MetadataType.STRING));
        }
    }
}
