package com.ryuqq.fileflow.domain.file.asset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * S3UploadMetadata Value Object 테스트
 *
 * <p>S3 업로드 메타데이터 VO의 검증 로직을 테스트합니다.</p>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@DisplayName("S3UploadMetadata Value Object 테스트")
class S3UploadMetadataTest {

    @Nested
    @DisplayName("정상 생성 테스트")
    class ValidCreationTests {

        @Test
        @DisplayName("of_WithValidInputs_ShouldCreateMetadata - 정상 입력으로 생성")
        void of_WithValidInputs_ShouldCreateMetadata() {
            // When
            S3UploadMetadata metadata = S3UploadMetadata.of(
                1024L,
                "etag-123",
                "image/jpeg",
                "uploads/tenant-1/2024/11/06/test.jpg"
            );

            // Then
            assertThat(metadata.contentLength()).isEqualTo(1024L);
            assertThat(metadata.etag()).isEqualTo("etag-123");
            assertThat(metadata.contentType()).isEqualTo("image/jpeg");
            assertThat(metadata.storageKey()).isEqualTo("uploads/tenant-1/2024/11/06/test.jpg");
        }

        @Test
        @DisplayName("of_WithNullContentType_ShouldCreateMetadata - null ContentType 허용")
        void of_WithNullContentType_ShouldCreateMetadata() {
            // When
            S3UploadMetadata metadata = S3UploadMetadata.of(
                1024L,
                "etag-123",
                null,
                "uploads/test.jpg"
            );

            // Then
            assertThat(metadata.contentLength()).isEqualTo(1024L);
            assertThat(metadata.contentType()).isNull();
        }

        @Test
        @DisplayName("of_WithMultipartETag_ShouldCreateMetadata - 멀티파트 ETag 허용")
        void of_WithMultipartETag_ShouldCreateMetadata() {
            // When
            S3UploadMetadata metadata = S3UploadMetadata.of(
                100_000_000L,
                "abc123def456-5",
                "application/zip",
                "uploads/large-file.zip"
            );

            // Then
            assertThat(metadata.etag()).isEqualTo("abc123def456-5");
            assertThat(metadata.contentLength()).isEqualTo(100_000_000L);
        }
    }

    @Nested
    @DisplayName("검증 실패 테스트")
    class ValidationFailureTests {

        @Test
        @DisplayName("of_WithNullContentLength_ShouldThrowException - null contentLength")
        void of_WithNullContentLength_ShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> S3UploadMetadata.of(
                null,
                "etag",
                "image/jpeg",
                "uploads/test.jpg"
            )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("contentLength는 양수여야 합니다");
        }

        @Test
        @DisplayName("of_WithZeroContentLength_ShouldThrowException - 0 contentLength")
        void of_WithZeroContentLength_ShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> S3UploadMetadata.of(
                0L,
                "etag",
                "image/jpeg",
                "uploads/test.jpg"
            )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("contentLength는 양수여야 합니다");
        }

        @Test
        @DisplayName("of_WithNegativeContentLength_ShouldThrowException - 음수 contentLength")
        void of_WithNegativeContentLength_ShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> S3UploadMetadata.of(
                -1L,
                "etag",
                "image/jpeg",
                "uploads/test.jpg"
            )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("contentLength는 양수여야 합니다");
        }

        @Test
        @DisplayName("of_WithNullEtag_ShouldThrowException - null etag")
        void of_WithNullEtag_ShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> S3UploadMetadata.of(
                1024L,
                null,
                "image/jpeg",
                "uploads/test.jpg"
            )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("etag는 필수입니다");
        }

        @Test
        @DisplayName("of_WithBlankEtag_ShouldThrowException - 빈 문자열 etag")
        void of_WithBlankEtag_ShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> S3UploadMetadata.of(
                1024L,
                "   ",
                "image/jpeg",
                "uploads/test.jpg"
            )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("etag는 필수입니다");
        }

        @Test
        @DisplayName("of_WithNullStorageKey_ShouldThrowException - null storageKey")
        void of_WithNullStorageKey_ShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> S3UploadMetadata.of(
                1024L,
                "etag",
                "image/jpeg",
                null
            )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("storageKey는 필수입니다");
        }

        @Test
        @DisplayName("of_WithBlankStorageKey_ShouldThrowException - 빈 문자열 storageKey")
        void of_WithBlankStorageKey_ShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> S3UploadMetadata.of(
                1024L,
                "etag",
                "image/jpeg",
                ""
            )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("storageKey는 필수입니다");
        }
    }

    @Nested
    @DisplayName("Record 동등성 테스트")
    class RecordEqualityTests {

        @Test
        @DisplayName("equals_SameValues_ShouldReturnTrue - 동일한 값으로 생성된 객체는 같음")
        void equals_SameValues_ShouldReturnTrue() {
            // When
            S3UploadMetadata metadata1 = S3UploadMetadata.of(
                1024L,
                "etag",
                "image/jpeg",
                "uploads/test.jpg"
            );
            S3UploadMetadata metadata2 = S3UploadMetadata.of(
                1024L,
                "etag",
                "image/jpeg",
                "uploads/test.jpg"
            );

            // Then
            assertThat(metadata1).isEqualTo(metadata2);
            assertThat(metadata1.hashCode()).isEqualTo(metadata2.hashCode());
        }

        @Test
        @DisplayName("equals_DifferentValues_ShouldReturnFalse - 다른 값으로 생성된 객체는 다름")
        void equals_DifferentValues_ShouldReturnFalse() {
            // When
            S3UploadMetadata metadata1 = S3UploadMetadata.of(
                1024L,
                "etag1",
                "image/jpeg",
                "uploads/test1.jpg"
            );
            S3UploadMetadata metadata2 = S3UploadMetadata.of(
                2048L,
                "etag2",
                "image/png",
                "uploads/test2.jpg"
            );

            // Then
            assertThat(metadata1).isNotEqualTo(metadata2);
        }
    }
}

