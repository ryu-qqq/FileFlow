package com.ryuqq.fileflow.application.upload.port.out;

import com.ryuqq.fileflow.application.upload.port.out.VerifyS3ObjectPort.S3ObjectMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * S3ObjectMetadata 단위 테스트
 *
 * @author sangwon-ryu
 */
class S3ObjectMetadataTest {

    @Test
    @DisplayName("정상적인 S3ObjectMetadata 생성")
    void createMetadata_ValidInputs_Success() {
        // Given
        String etag = "d41d8cd98f00b204e9800998ecf8427e";
        long contentLength = 1024L;
        String contentType = "image/jpeg";
        String lastModified = "2024-01-01T10:00:00Z";

        // When
        S3ObjectMetadata metadata = new S3ObjectMetadata(
                etag,
                contentLength,
                contentType,
                lastModified
        );

        // Then
        assertThat(metadata).isNotNull();
        assertThat(metadata.etag()).isEqualTo(etag);
        assertThat(metadata.contentLength()).isEqualTo(contentLength);
        assertThat(metadata.contentType()).isEqualTo(contentType);
        assertThat(metadata.lastModified()).isEqualTo(lastModified);
    }

    @Test
    @DisplayName("null 값을 허용하는 S3ObjectMetadata 생성")
    void createMetadata_WithNulls_Success() {
        // Given & When
        S3ObjectMetadata metadata = new S3ObjectMetadata(
                null, // etag can be null
                0L,
                null, // contentType can be null
                null  // lastModified can be null
        );

        // Then
        assertThat(metadata).isNotNull();
        assertThat(metadata.etag()).isNull();
        assertThat(metadata.contentLength()).isEqualTo(0L);
        assertThat(metadata.contentType()).isNull();
        assertThat(metadata.lastModified()).isNull();
    }

    @Test
    @DisplayName("동일한 값을 가진 두 S3ObjectMetadata는 같음")
    void equals_SameValues_ReturnsTrue() {
        // Given
        S3ObjectMetadata metadata1 = new S3ObjectMetadata(
                "etag-123",
                1024L,
                "image/jpeg",
                "2024-01-01T10:00:00Z"
        );

        S3ObjectMetadata metadata2 = new S3ObjectMetadata(
                "etag-123",
                1024L,
                "image/jpeg",
                "2024-01-01T10:00:00Z"
        );

        // When & Then
        assertThat(metadata1).isEqualTo(metadata2);
        assertThat(metadata1.hashCode()).isEqualTo(metadata2.hashCode());
    }

    @Test
    @DisplayName("다른 값을 가진 두 S3ObjectMetadata는 다름")
    void equals_DifferentValues_ReturnsFalse() {
        // Given
        S3ObjectMetadata metadata1 = new S3ObjectMetadata(
                "etag-123",
                1024L,
                "image/jpeg",
                "2024-01-01T10:00:00Z"
        );

        S3ObjectMetadata metadata2 = new S3ObjectMetadata(
                "etag-456", // different etag
                1024L,
                "image/jpeg",
                "2024-01-01T10:00:00Z"
        );

        // When & Then
        assertThat(metadata1).isNotEqualTo(metadata2);
    }

    @Test
    @DisplayName("toString()이 모든 필드를 포함")
    void toString_ContainsAllFields() {
        // Given
        S3ObjectMetadata metadata = new S3ObjectMetadata(
                "etag-123",
                1024L,
                "image/jpeg",
                "2024-01-01T10:00:00Z"
        );

        // When
        String toString = metadata.toString();

        // Then
        assertThat(toString).contains("etag-123");
        assertThat(toString).contains("1024");
        assertThat(toString).contains("image/jpeg");
        assertThat(toString).contains("2024-01-01T10:00:00Z");
    }

    @Test
    @DisplayName("contentLength의 다양한 값 테스트")
    void contentLength_VariousValues_Success() {
        // Given & When
        S3ObjectMetadata smallFile = new S3ObjectMetadata("etag", 100L, "text/plain", null);
        S3ObjectMetadata largeFile = new S3ObjectMetadata("etag", 10_000_000_000L, "video/mp4", null);
        S3ObjectMetadata zeroFile = new S3ObjectMetadata("etag", 0L, "text/plain", null);

        // Then
        assertThat(smallFile.contentLength()).isEqualTo(100L);
        assertThat(largeFile.contentLength()).isEqualTo(10_000_000_000L);
        assertThat(zeroFile.contentLength()).isEqualTo(0L);
    }

    @Test
    @DisplayName("다양한 contentType 테스트")
    void contentType_VariousTypes_Success() {
        // Given & When
        S3ObjectMetadata imageMetadata = new S3ObjectMetadata("etag", 1024L, "image/jpeg", null);
        S3ObjectMetadata videoMetadata = new S3ObjectMetadata("etag", 1024L, "video/mp4", null);
        S3ObjectMetadata pdfMetadata = new S3ObjectMetadata("etag", 1024L, "application/pdf", null);
        S3ObjectMetadata textMetadata = new S3ObjectMetadata("etag", 1024L, "text/plain", null);

        // Then
        assertThat(imageMetadata.contentType()).isEqualTo("image/jpeg");
        assertThat(videoMetadata.contentType()).isEqualTo("video/mp4");
        assertThat(pdfMetadata.contentType()).isEqualTo("application/pdf");
        assertThat(textMetadata.contentType()).isEqualTo("text/plain");
    }

    @Test
    @DisplayName("Record의 불변성 검증")
    void immutability_FieldsCannotBeChanged() {
        // Given
        String originalEtag = "etag-123";
        S3ObjectMetadata metadata = new S3ObjectMetadata(
                originalEtag,
                1024L,
                "image/jpeg",
                "2024-01-01T10:00:00Z"
        );

        // When
        String retrievedEtag = metadata.etag();

        // Then - record는 불변이므로 getter로 얻은 값을 변경해도 원본은 변경되지 않음
        assertThat(retrievedEtag).isEqualTo(originalEtag);
        assertThat(metadata.etag()).isEqualTo(originalEtag);
    }

    @Test
    @DisplayName("빈 문자열 값을 가진 S3ObjectMetadata 생성")
    void createMetadata_WithEmptyStrings_Success() {
        // Given & When
        S3ObjectMetadata metadata = new S3ObjectMetadata(
                "", // empty etag
                1024L,
                "", // empty contentType
                ""  // empty lastModified
        );

        // Then
        assertThat(metadata.etag()).isEmpty();
        assertThat(metadata.contentType()).isEmpty();
        assertThat(metadata.lastModified()).isEmpty();
    }
}
