package com.ryuqq.fileflow.adapter.in.rest.asset.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("FileAssetApiResponse 단위 테스트")
class FileAssetApiResponseTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드로 응답을 생성할 수 있다")
        void create_WithAllFields_ShouldSucceed() {
            // given
            Instant createdAt = Instant.parse("2025-11-26T10:00:00Z");
            Instant processedAt = Instant.parse("2025-11-26T10:05:00Z");

            // when
            FileAssetApiResponse response =
                    new FileAssetApiResponse(
                            "file-asset-123",
                            "session-456",
                            "document.pdf",
                            1024 * 1024L,
                            "application/pdf",
                            "DOCUMENT",
                            "test-bucket",
                            "uploads/document.pdf",
                            "etag-abc123",
                            "COMPLETED",
                            createdAt,
                            processedAt);

            // then
            assertThat(response.id()).isEqualTo("file-asset-123");
            assertThat(response.sessionId()).isEqualTo("session-456");
            assertThat(response.fileName()).isEqualTo("document.pdf");
            assertThat(response.fileSize()).isEqualTo(1024 * 1024L);
            assertThat(response.contentType()).isEqualTo("application/pdf");
            assertThat(response.category()).isEqualTo("DOCUMENT");
            assertThat(response.bucket()).isEqualTo("test-bucket");
            assertThat(response.s3Key()).isEqualTo("uploads/document.pdf");
            assertThat(response.etag()).isEqualTo("etag-abc123");
            assertThat(response.status()).isEqualTo("COMPLETED");
            assertThat(response.createdAt()).isEqualTo(createdAt);
            assertThat(response.processedAt()).isEqualTo(processedAt);
        }

        @Test
        @DisplayName("processedAt이 null인 경우에도 생성할 수 있다")
        void create_WithNullProcessedAt_ShouldSucceed() {
            // given
            Instant createdAt = Instant.now();

            // when
            FileAssetApiResponse response =
                    new FileAssetApiResponse(
                            "file-pending",
                            "session-789",
                            "image.jpg",
                            512 * 1024L,
                            "image/jpeg",
                            "IMAGE",
                            "bucket",
                            "key",
                            null,
                            "PENDING",
                            createdAt,
                            null);

            // then
            assertThat(response.status()).isEqualTo("PENDING");
            assertThat(response.etag()).isNull();
            assertThat(response.processedAt()).isNull();
        }

        @Test
        @DisplayName("etag가 null인 경우에도 생성할 수 있다")
        void create_WithNullEtag_ShouldSucceed() {
            // when
            FileAssetApiResponse response =
                    new FileAssetApiResponse(
                            "id",
                            "session",
                            "file.txt",
                            100L,
                            "text/plain",
                            "DOCUMENT",
                            "bucket",
                            "key",
                            null,
                            "PROCESSING",
                            Instant.now(),
                            null);

            // then
            assertThat(response.etag()).isNull();
        }
    }

    @Nested
    @DisplayName("카테고리별 테스트")
    class CategoryTest {

        @ParameterizedTest
        @ValueSource(strings = {"IMAGE", "VIDEO", "AUDIO", "DOCUMENT", "OTHER"})
        @DisplayName("모든 FileCategory로 응답을 생성할 수 있다")
        void create_WithAllCategories_ShouldSucceed(String category) {
            // when
            FileAssetApiResponse response =
                    new FileAssetApiResponse(
                            "id",
                            "session",
                            "file.ext",
                            100L,
                            "application/octet-stream",
                            category,
                            "bucket",
                            "key",
                            "etag",
                            "COMPLETED",
                            Instant.now(),
                            Instant.now());

            // then
            assertThat(response.category()).isEqualTo(category);
        }
    }

    @Nested
    @DisplayName("상태별 테스트")
    class StatusTest {

        @ParameterizedTest
        @ValueSource(strings = {"PENDING", "PROCESSING", "COMPLETED", "FAILED", "DELETED"})
        @DisplayName("모든 FileAssetStatus로 응답을 생성할 수 있다")
        void create_WithAllStatuses_ShouldSucceed(String status) {
            // when
            FileAssetApiResponse response =
                    new FileAssetApiResponse(
                            "id",
                            "session",
                            "file.ext",
                            100L,
                            "application/octet-stream",
                            "DOCUMENT",
                            "bucket",
                            "key",
                            "COMPLETED".equals(status) ? "etag" : null,
                            status,
                            Instant.now(),
                            "COMPLETED".equals(status) ? Instant.now() : null);

            // then
            assertThat(response.status()).isEqualTo(status);
        }
    }

    @Nested
    @DisplayName("컨텐츠 타입 테스트")
    class ContentTypeTest {

        @Test
        @DisplayName("이미지 컨텐츠 타입으로 응답을 생성할 수 있다")
        void create_WithImageContentType_ShouldSucceed() {
            // when
            FileAssetApiResponse response =
                    createResponse("image.png", "image/png", "IMAGE");

            // then
            assertThat(response.contentType()).isEqualTo("image/png");
            assertThat(response.category()).isEqualTo("IMAGE");
        }

        @Test
        @DisplayName("비디오 컨텐츠 타입으로 응답을 생성할 수 있다")
        void create_WithVideoContentType_ShouldSucceed() {
            // when
            FileAssetApiResponse response =
                    createResponse("video.mp4", "video/mp4", "VIDEO");

            // then
            assertThat(response.contentType()).isEqualTo("video/mp4");
            assertThat(response.category()).isEqualTo("VIDEO");
        }

        @Test
        @DisplayName("오디오 컨텐츠 타입으로 응답을 생성할 수 있다")
        void create_WithAudioContentType_ShouldSucceed() {
            // when
            FileAssetApiResponse response =
                    createResponse("audio.mp3", "audio/mpeg", "AUDIO");

            // then
            assertThat(response.contentType()).isEqualTo("audio/mpeg");
            assertThat(response.category()).isEqualTo("AUDIO");
        }

        @Test
        @DisplayName("문서 컨텐츠 타입으로 응답을 생성할 수 있다")
        void create_WithDocumentContentType_ShouldSucceed() {
            // when
            FileAssetApiResponse response =
                    createResponse("doc.pdf", "application/pdf", "DOCUMENT");

            // then
            assertThat(response.contentType()).isEqualTo("application/pdf");
            assertThat(response.category()).isEqualTo("DOCUMENT");
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
            FileAssetApiResponse response = createResponseWithSize(smallSize);

            // then
            assertThat(response.fileSize()).isEqualTo(smallSize);
        }

        @Test
        @DisplayName("큰 파일 크기로 응답을 생성할 수 있다")
        void create_WithLargeFileSize_ShouldSucceed() {
            // given - 5GB
            long largeSize = 5L * 1024 * 1024 * 1024;

            // when
            FileAssetApiResponse response = createResponseWithSize(largeSize);

            // then
            assertThat(response.fileSize()).isEqualTo(largeSize);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값을 가진 응답은 동등하다")
        void equals_WithSameValues_ShouldBeEqual() {
            // given
            Instant createdAt = Instant.parse("2025-11-26T10:00:00Z");
            Instant processedAt = Instant.parse("2025-11-26T10:05:00Z");

            FileAssetApiResponse response1 =
                    new FileAssetApiResponse(
                            "id-1",
                            "session",
                            "file.jpg",
                            100L,
                            "image/jpeg",
                            "IMAGE",
                            "bucket",
                            "key",
                            "etag",
                            "COMPLETED",
                            createdAt,
                            processedAt);

            FileAssetApiResponse response2 =
                    new FileAssetApiResponse(
                            "id-1",
                            "session",
                            "file.jpg",
                            100L,
                            "image/jpeg",
                            "IMAGE",
                            "bucket",
                            "key",
                            "etag",
                            "COMPLETED",
                            createdAt,
                            processedAt);

            // when & then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("다른 id를 가진 응답은 동등하지 않다")
        void equals_WithDifferentId_ShouldNotBeEqual() {
            // given
            Instant now = Instant.now();
            FileAssetApiResponse response1 = createFullResponse("id-1", now);
            FileAssetApiResponse response2 = createFullResponse("id-2", now);

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("다른 status를 가진 응답은 동등하지 않다")
        void equals_WithDifferentStatus_ShouldNotBeEqual() {
            // given
            Instant now = Instant.now();
            FileAssetApiResponse response1 =
                    new FileAssetApiResponse(
                            "id",
                            "session",
                            "file.jpg",
                            100L,
                            "image/jpeg",
                            "IMAGE",
                            "bucket",
                            "key",
                            "etag",
                            "COMPLETED",
                            now,
                            now);

            FileAssetApiResponse response2 =
                    new FileAssetApiResponse(
                            "id",
                            "session",
                            "file.jpg",
                            100L,
                            "image/jpeg",
                            "IMAGE",
                            "bucket",
                            "key",
                            "etag",
                            "PROCESSING",
                            now,
                            null);

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }
    }

    private FileAssetApiResponse createResponse(
            String fileName, String contentType, String category) {
        return new FileAssetApiResponse(
                "id",
                "session",
                fileName,
                1024L,
                contentType,
                category,
                "bucket",
                "key/" + fileName,
                "etag",
                "COMPLETED",
                Instant.now(),
                Instant.now());
    }

    private FileAssetApiResponse createResponseWithSize(long size) {
        return new FileAssetApiResponse(
                "id",
                "session",
                "file.bin",
                size,
                "application/octet-stream",
                "DOCUMENT",
                "bucket",
                "key",
                "etag",
                "COMPLETED",
                Instant.now(),
                Instant.now());
    }

    private FileAssetApiResponse createFullResponse(String id, Instant now) {
        return new FileAssetApiResponse(
                id,
                "session",
                "file.jpg",
                100L,
                "image/jpeg",
                "IMAGE",
                "bucket",
                "key",
                "etag",
                "COMPLETED",
                now,
                now);
    }
}
