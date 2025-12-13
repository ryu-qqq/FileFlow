package com.ryuqq.fileflow.adapter.in.rest.session.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * UploadSessionApiResponse 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("UploadSessionApiResponse 단위 테스트")
class UploadSessionApiResponseTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("모든 필드로 응답을 생성할 수 있다")
        void create_WithAllFields_ShouldSucceed() {
            // given
            String sessionId = "session-123";
            String fileName = "image.jpg";
            long fileSize = 1024000L;
            String contentType = "image/jpeg";
            String uploadType = "SINGLE";
            String status = "PENDING";
            String bucket = "fileflow-bucket";
            String key = "uploads/image.jpg";
            Instant createdAt = Instant.now();
            Instant expiresAt = createdAt.plusSeconds(900);

            // when
            UploadSessionApiResponse response =
                    new UploadSessionApiResponse(
                            sessionId,
                            fileName,
                            fileSize,
                            contentType,
                            uploadType,
                            status,
                            bucket,
                            key,
                            createdAt,
                            expiresAt);

            // then
            assertThat(response.sessionId()).isEqualTo(sessionId);
            assertThat(response.fileName()).isEqualTo(fileName);
            assertThat(response.fileSize()).isEqualTo(fileSize);
            assertThat(response.contentType()).isEqualTo(contentType);
            assertThat(response.uploadType()).isEqualTo(uploadType);
            assertThat(response.status()).isEqualTo(status);
            assertThat(response.bucket()).isEqualTo(bucket);
            assertThat(response.key()).isEqualTo(key);
            assertThat(response.createdAt()).isEqualTo(createdAt);
            assertThat(response.expiresAt()).isEqualTo(expiresAt);
        }

        @Test
        @DisplayName("of 팩토리 메서드로 응답을 생성할 수 있다")
        void create_WithOfMethod_ShouldSucceed() {
            // given
            Instant now = Instant.now();

            // when
            UploadSessionApiResponse response =
                    UploadSessionApiResponse.of(
                            "session-456",
                            "document.pdf",
                            2048000L,
                            "application/pdf",
                            "SINGLE",
                            "COMPLETED",
                            "my-bucket",
                            "docs/document.pdf",
                            now,
                            now.plusSeconds(900));

            // then
            assertThat(response.sessionId()).isEqualTo("session-456");
            assertThat(response.fileName()).isEqualTo("document.pdf");
            assertThat(response.fileSize()).isEqualTo(2048000L);
            assertThat(response.contentType()).isEqualTo("application/pdf");
        }
    }

    @Nested
    @DisplayName("업로드 타입 테스트")
    class UploadTypeTest {

        @ParameterizedTest
        @ValueSource(strings = {"SINGLE", "MULTIPART"})
        @DisplayName("유효한 업로드 타입으로 응답을 생성할 수 있다")
        void create_WithValidUploadType_ShouldSucceed(String uploadType) {
            // when
            UploadSessionApiResponse response = createResponseWithUploadType(uploadType);

            // then
            assertThat(response.uploadType()).isEqualTo(uploadType);
        }

        @Test
        @DisplayName("SINGLE 업로드 타입으로 응답을 생성할 수 있다")
        void create_WithSingleUploadType_ShouldSucceed() {
            // when
            UploadSessionApiResponse response = createResponseWithUploadType("SINGLE");

            // then
            assertThat(response.uploadType()).isEqualTo("SINGLE");
        }

        @Test
        @DisplayName("MULTIPART 업로드 타입으로 응답을 생성할 수 있다")
        void create_WithMultipartUploadType_ShouldSucceed() {
            // when
            UploadSessionApiResponse response = createResponseWithUploadType("MULTIPART");

            // then
            assertThat(response.uploadType()).isEqualTo("MULTIPART");
        }
    }

    @Nested
    @DisplayName("상태 테스트")
    class StatusTest {

        @ParameterizedTest
        @ValueSource(strings = {"PENDING", "IN_PROGRESS", "COMPLETED", "EXPIRED", "CANCELLED"})
        @DisplayName("유효한 상태로 응답을 생성할 수 있다")
        void create_WithValidStatus_ShouldSucceed(String status) {
            // when
            UploadSessionApiResponse response = createResponseWithStatus(status);

            // then
            assertThat(response.status()).isEqualTo(status);
        }
    }

    @Nested
    @DisplayName("파일 크기 테스트")
    class FileSizeTest {

        @Test
        @DisplayName("작은 파일 크기로 응답을 생성할 수 있다")
        void create_WithSmallFileSize_ShouldSucceed() {
            // given
            long smallSize = 1024L; // 1KB

            // when
            UploadSessionApiResponse response = createResponseWithFileSize(smallSize);

            // then
            assertThat(response.fileSize()).isEqualTo(1024L);
        }

        @Test
        @DisplayName("큰 파일 크기로 응답을 생성할 수 있다")
        void create_WithLargeFileSize_ShouldSucceed() {
            // given
            long largeSize = 5L * 1024 * 1024 * 1024; // 5GB

            // when
            UploadSessionApiResponse response = createResponseWithFileSize(largeSize);

            // then
            assertThat(response.fileSize()).isEqualTo(5L * 1024 * 1024 * 1024);
        }
    }

    @Nested
    @DisplayName("만료 시각 테스트")
    class ExpiresAtTest {

        @Test
        @DisplayName("15분 후 만료 시각으로 응답을 생성할 수 있다")
        void create_With15MinutesExpiry_ShouldSucceed() {
            // given
            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(900);

            // when
            UploadSessionApiResponse response =
                    UploadSessionApiResponse.of(
                            "session",
                            "file.txt",
                            1024L,
                            "text/plain",
                            "SINGLE",
                            "PENDING",
                            "bucket",
                            "key",
                            now,
                            expiresAt);

            // then
            assertThat(response.expiresAt()).isAfter(response.createdAt());
            assertThat(response.expiresAt()).isBefore(now.plusSeconds(901));
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
            Instant expiresAt = Instant.parse("2025-11-26T10:15:00Z");

            UploadSessionApiResponse response1 =
                    new UploadSessionApiResponse(
                            "session-1",
                            "file.jpg",
                            1024L,
                            "image/jpeg",
                            "SINGLE",
                            "PENDING",
                            "bucket",
                            "key",
                            createdAt,
                            expiresAt);
            UploadSessionApiResponse response2 =
                    new UploadSessionApiResponse(
                            "session-1",
                            "file.jpg",
                            1024L,
                            "image/jpeg",
                            "SINGLE",
                            "PENDING",
                            "bucket",
                            "key",
                            createdAt,
                            expiresAt);

            // when & then
            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("다른 sessionId를 가진 응답은 동등하지 않다")
        void equals_WithDifferentSessionId_ShouldNotBeEqual() {
            // given
            UploadSessionApiResponse response1 = createResponseWithSessionId("session-1");
            UploadSessionApiResponse response2 = createResponseWithSessionId("session-2");

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("다른 status를 가진 응답은 동등하지 않다")
        void equals_WithDifferentStatus_ShouldNotBeEqual() {
            // given
            UploadSessionApiResponse response1 = createResponseWithStatus("PENDING");
            UploadSessionApiResponse response2 = createResponseWithStatus("COMPLETED");

            // when & then
            assertThat(response1).isNotEqualTo(response2);
        }
    }

    private UploadSessionApiResponse createResponseWithUploadType(String uploadType) {
        Instant now = Instant.now();
        return UploadSessionApiResponse.of(
                "session",
                "file.txt",
                1024L,
                "text/plain",
                uploadType,
                "PENDING",
                "bucket",
                "key",
                now,
                now.plusSeconds(900));
    }

    private UploadSessionApiResponse createResponseWithStatus(String status) {
        Instant now = Instant.now();
        return UploadSessionApiResponse.of(
                "session",
                "file.txt",
                1024L,
                "text/plain",
                "SINGLE",
                status,
                "bucket",
                "key",
                now,
                now.plusSeconds(900));
    }

    private UploadSessionApiResponse createResponseWithFileSize(long fileSize) {
        Instant now = Instant.now();
        return UploadSessionApiResponse.of(
                "session",
                "file.txt",
                fileSize,
                "text/plain",
                "SINGLE",
                "PENDING",
                "bucket",
                "key",
                now,
                now.plusSeconds(900));
    }

    private UploadSessionApiResponse createResponseWithSessionId(String sessionId) {
        Instant now = Instant.now();
        return UploadSessionApiResponse.of(
                sessionId,
                "file.txt",
                1024L,
                "text/plain",
                "SINGLE",
                "PENDING",
                "bucket",
                "key",
                now,
                now.plusSeconds(900));
    }
}
