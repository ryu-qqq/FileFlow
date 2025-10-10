package com.ryuqq.fileflow.adapter.metadata;

import com.ryuqq.fileflow.domain.file.FileMetadata;
import com.ryuqq.fileflow.domain.upload.vo.FileId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MetadataExtractionAdapter 통합 테스트
 *
 * 테스트 전략:
 * - ImageMetadataExtractor와의 통합 검증
 * - Content-Type 기반 Extractor 라우팅 검증
 * - 지원하지 않는 타입 처리 검증
 *
 * @author sangwon-ryu
 */
@DisplayName("MetadataExtractionAdapter 통합 테스트")
class MetadataExtractionAdapterTest {

    private MetadataExtractionAdapter adapter;
    private FileId testFileId;

    @BeforeEach
    void setUp() {
        ImageMetadataExtractor imageExtractor = new ImageMetadataExtractor();
        adapter = new MetadataExtractionAdapter(imageExtractor);
        testFileId = FileId.generate();
    }

    @Test
    @DisplayName("이미지 Content-Type 지원 확인")
    void supportsImageContentTypes() {
        assertThat(adapter.supports("image/jpeg")).isTrue();
        assertThat(adapter.supports("image/png")).isTrue();
        assertThat(adapter.supports("image/gif")).isTrue();
    }

    @Test
    @DisplayName("비디오 Content-Type은 아직 지원하지 않음")
    void doesNotSupportVideoContentTypes() {
        assertThat(adapter.supports("video/mp4")).isFalse();
        assertThat(adapter.supports("video/avi")).isFalse();
    }

    @Test
    @DisplayName("문서 Content-Type은 아직 지원하지 않음")
    void doesNotSupportDocumentContentTypes() {
        assertThat(adapter.supports("application/pdf")).isFalse();
        assertThat(adapter.supports("application/msword")).isFalse();
    }

    @Test
    @DisplayName("지원하지 않는 Content-Type에 대해 빈 리스트 반환")
    void returnsEmptyListForUnsupportedContentType() {
        InputStream dummyStream = new ByteArrayInputStream(new byte[0]);

        List<FileMetadata> result = adapter.extractMetadata(testFileId, dummyStream, "video/mp4");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("이미지 Content-Type에 대해 ImageMetadataExtractor로 위임")
    void delegatesToImageExtractorForImageContentType() {
        // 최소한의 유효한 JPEG 헤더
        byte[] minimalJpeg = new byte[]{
                (byte) 0xFF, (byte) 0xD8,  // SOI
                (byte) 0xFF, (byte) 0xD9   // EOI
        };

        InputStream jpegStream = new ByteArrayInputStream(minimalJpeg);

        try {
            List<FileMetadata> result = adapter.extractMetadata(testFileId, jpegStream, "image/jpeg");

            // ImageMetadataExtractor가 호출되어 최소한 format 메타데이터는 생성되어야 함
            assertThat(result).isNotEmpty();
            assertThat(result).anyMatch(m -> m.hasKey("format"));
        } catch (Exception e) {
            // 최소 JPEG 구조로 파싱 실패할 수 있지만, 예외는 MetadataExtractionException이어야 함
            assertThat(e.getClass().getSimpleName()).contains("MetadataExtractionException");
        }
    }

    @Test
    @DisplayName("null Content-Type에 대해 빈 리스트 반환")
    void returnsEmptyListForNullContentType() {
        InputStream dummyStream = new ByteArrayInputStream(new byte[0]);

        List<FileMetadata> result = adapter.extractMetadata(testFileId, dummyStream, null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("대소문자 무관하게 Content-Type 처리")
    void handlesCaseInsensitiveContentType() {
        assertThat(adapter.supports("IMAGE/JPEG")).isTrue();
        assertThat(adapter.supports("Image/Png")).isTrue();
        assertThat(adapter.supports("image/GIF")).isTrue();
    }
}
