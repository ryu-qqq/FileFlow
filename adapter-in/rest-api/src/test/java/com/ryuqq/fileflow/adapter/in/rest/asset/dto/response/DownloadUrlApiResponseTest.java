package com.ryuqq.fileflow.adapter.in.rest.asset.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * DownloadUrlApiResponse 단위 테스트.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("DownloadUrlApiResponse 단위 테스트")
class DownloadUrlApiResponseTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드로 응답을 생성할 수 있다")
        void create_WithAllFields_ShouldSucceed() {
            // given
            String fileAssetId = "asset-123";
            String downloadUrl = "https://s3.amazonaws.com/bucket/key?signature=xxx";
            String fileName = "image.jpg";
            String contentType = "image/jpeg";
            long fileSize = 1024 * 1024L;
            Instant expiresAt = Instant.parse("2025-12-10T11:00:00Z");

            // when
            DownloadUrlApiResponse response =
                    new DownloadUrlApiResponse(
                            fileAssetId, downloadUrl, fileName, contentType, fileSize, expiresAt);

            // then
            assertThat(response.fileAssetId()).isEqualTo(fileAssetId);
            assertThat(response.downloadUrl()).isEqualTo(downloadUrl);
            assertThat(response.fileName()).isEqualTo(fileName);
            assertThat(response.contentType()).isEqualTo(contentType);
            assertThat(response.fileSize()).isEqualTo(fileSize);
            assertThat(response.expiresAt()).isEqualTo(expiresAt);
        }

        @Test
        @DisplayName("of 팩토리 메서드로 응답을 생성할 수 있다")
        void create_WithOf_ShouldSucceed() {
            // given
            Instant expiresAt = Instant.now().plusSeconds(3600);

            // when
            DownloadUrlApiResponse response =
                    DownloadUrlApiResponse.of(
                            "asset-456",
                            "https://s3.example.com/file",
                            "document.pdf",
                            "application/pdf",
                            2048L,
                            expiresAt);

            // then
            assertThat(response.fileAssetId()).isEqualTo("asset-456");
            assertThat(response.downloadUrl()).isEqualTo("https://s3.example.com/file");
            assertThat(response.fileName()).isEqualTo("document.pdf");
            assertThat(response.contentType()).isEqualTo("application/pdf");
            assertThat(response.fileSize()).isEqualTo(2048L);
            assertThat(response.expiresAt()).isEqualTo(expiresAt);
        }
    }

    @Nested
    @DisplayName("파일 크기 테스트")
    class FileSizeTest {

        @Test
        @DisplayName("작은 파일 크기로 응답을 생성할 수 있다")
        void create_WithSmallFileSize_ShouldSucceed() {
            // given - 1KB
            long smallSize = 1024L;

            // when
            DownloadUrlApiResponse response = createResponseWithSize(smallSize);

            // then
            assertThat(response.fileSize()).isEqualTo(smallSize);
        }

        @Test
        @DisplayName("큰 파일 크기로 응답을 생성할 수 있다")
        void create_WithLargeFileSize_ShouldSucceed() {
            // given - 5GB
            long largeSize = 5L * 1024 * 1024 * 1024;

            // when
            DownloadUrlApiResponse response = createResponseWithSize(largeSize);

            // then
            assertThat(response.fileSize()).isEqualTo(largeSize);
        }

        private DownloadUrlApiResponse createResponseWithSize(long size) {
            return new DownloadUrlApiResponse(
                    "asset-id",
                    "https://s3.example.com/file",
                    "file.bin",
                    "application/octet-stream",
                    size,
                    Instant.now().plusSeconds(3600));
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 응답은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            Instant expiresAt = Instant.parse("2025-12-10T11:00:00Z");
            DownloadUrlApiResponse response1 =
                    new DownloadUrlApiResponse(
                            "asset-123",
                            "https://s3.example.com/file",
                            "image.jpg",
                            "image/jpeg",
                            1024L,
                            expiresAt);
            DownloadUrlApiResponse response2 =
                    new DownloadUrlApiResponse(
                            "asset-123",
                            "https://s3.example.com/file",
                            "image.jpg",
                            "image/jpeg",
                            1024L,
                            expiresAt);

            // when & then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("다른 fileAssetId를 가진 응답은 동등하지 않다")
        void equals_WithDifferentFileAssetId_ShouldNotBeEqual() {
            // given
            Instant expiresAt = Instant.now();
            DownloadUrlApiResponse response1 =
                    new DownloadUrlApiResponse(
                            "asset-1", "url", "file.jpg", "image/jpeg", 1024L, expiresAt);
            DownloadUrlApiResponse response2 =
                    new DownloadUrlApiResponse(
                            "asset-2", "url", "file.jpg", "image/jpeg", 1024L, expiresAt);

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("다른 downloadUrl을 가진 응답은 동등하지 않다")
        void equals_WithDifferentDownloadUrl_ShouldNotBeEqual() {
            // given
            Instant expiresAt = Instant.now();
            DownloadUrlApiResponse response1 =
                    new DownloadUrlApiResponse(
                            "asset-123", "url-1", "file.jpg", "image/jpeg", 1024L, expiresAt);
            DownloadUrlApiResponse response2 =
                    new DownloadUrlApiResponse(
                            "asset-123", "url-2", "file.jpg", "image/jpeg", 1024L, expiresAt);

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }
    }
}
