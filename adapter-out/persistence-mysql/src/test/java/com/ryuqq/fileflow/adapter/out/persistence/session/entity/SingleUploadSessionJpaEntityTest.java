package com.ryuqq.fileflow.adapter.out.persistence.session.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.session.vo.SessionStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("SingleUploadSessionJpaEntity 단위 테스트")
class SingleUploadSessionJpaEntityTest {

    @Nested
    @DisplayName("of 팩토리 메서드 테스트")
    class OfFactoryMethodTest {

        @Test
        @DisplayName("모든 필드로 Entity를 생성할 수 있다")
        void of_WithAllFields_ShouldCreateEntity() {
            // given
            Instant expiresAt = LocalDateTime.of(2025, 11, 27, 10, 0).toInstant(ZoneOffset.UTC);
            Instant createdAt = LocalDateTime.of(2025, 11, 26, 10, 0).toInstant(ZoneOffset.UTC);
            Instant updatedAt = LocalDateTime.of(2025, 11, 26, 10, 5).toInstant(ZoneOffset.UTC);

            // when
            SingleUploadSessionJpaEntity entity =
                    SingleUploadSessionJpaEntity.of(
                            "session-123",
                            "idempotency-key-456",
                            "01912345-6789-7abc-def0-123456789200",
                            "01912345-6789-7abc-def0-123456789100",
                            "Connectly Org",
                            "connectly",
                            "01912345-6789-7abc-def0-123456789001",
                            "Connectly",
                            "ADMIN",
                            "admin@example.com",
                            "document.pdf",
                            1024 * 1024L,
                            "application/pdf",
                            "test-bucket",
                            "uploads/document.pdf",
                            expiresAt,
                            SessionStatus.PREPARING,
                            "https://presigned-url.s3.amazonaws.com/...",
                            null,
                            null,
                            0L,
                            createdAt,
                            updatedAt);

            // then
            assertThat(entity.getId()).isEqualTo("session-123");
            assertThat(entity.getIdempotencyKey()).isEqualTo("idempotency-key-456");
            assertThat(entity.getUserId()).isEqualTo("01912345-6789-7abc-def0-123456789200");
            assertThat(entity.getOrganizationId()).isEqualTo("01912345-6789-7abc-def0-123456789100");
            assertThat(entity.getOrganizationName()).isEqualTo("Connectly Org");
            assertThat(entity.getOrganizationNamespace()).isEqualTo("connectly");
            assertThat(entity.getTenantId()).isEqualTo("01912345-6789-7abc-def0-123456789001");
            assertThat(entity.getTenantName()).isEqualTo("Connectly");
            assertThat(entity.getUserRole()).isEqualTo("ADMIN");
            assertThat(entity.getEmail()).isEqualTo("admin@example.com");
            assertThat(entity.getFileName()).isEqualTo("document.pdf");
            assertThat(entity.getFileSize()).isEqualTo(1024 * 1024L);
            assertThat(entity.getContentType()).isEqualTo("application/pdf");
            assertThat(entity.getBucket()).isEqualTo("test-bucket");
            assertThat(entity.getS3Key()).isEqualTo("uploads/document.pdf");
            assertThat(entity.getExpiresAt()).isEqualTo(expiresAt);
            assertThat(entity.getStatus()).isEqualTo(SessionStatus.PREPARING);
            assertThat(entity.getPresignedUrl()).startsWith("https://presigned-url");
            assertThat(entity.getEtag()).isNull();
            assertThat(entity.getCompletedAt()).isNull();
            assertThat(entity.getVersion()).isEqualTo(0L);
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("userId가 null인 경우에도 생성할 수 있다")
        void of_WithNullUserId_ShouldCreateEntity() {
            // when
            SingleUploadSessionJpaEntity entity = createEntity(null);

            // then
            assertThat(entity.getUserId()).isNull();
        }

        @Test
        @DisplayName("email이 null인 경우에도 생성할 수 있다")
        void of_WithNullEmail_ShouldCreateEntity() {
            // when
            SingleUploadSessionJpaEntity entity = createEntityWithEmail(null);

            // then
            assertThat(entity.getEmail()).isNull();
        }

        @Test
        @DisplayName("완료된 세션은 etag와 completedAt이 설정된다")
        void of_WithCompletedSession_ShouldHaveEtagAndCompletedAt() {
            // given
            Instant completedAt = LocalDateTime.of(2025, 11, 26, 11, 0).toInstant(ZoneOffset.UTC);
            String etag = "\"abc123def456\"";

            // when
            SingleUploadSessionJpaEntity entity =
                    createCompletedEntity(etag, completedAt, SessionStatus.COMPLETED);

            // then
            assertThat(entity.getStatus()).isEqualTo(SessionStatus.COMPLETED);
            assertThat(entity.getEtag()).isEqualTo(etag);
            assertThat(entity.getCompletedAt()).isEqualTo(completedAt);
        }
    }

    @Nested
    @DisplayName("상태별 테스트")
    class StatusTest {

        @ParameterizedTest
        @EnumSource(SessionStatus.class)
        @DisplayName("모든 SessionStatus로 Entity를 생성할 수 있다")
        void of_WithAllStatuses_ShouldCreateEntity(SessionStatus status) {
            // when
            SingleUploadSessionJpaEntity entity = createEntityWithStatus(status);

            // then
            assertThat(entity.getStatus()).isEqualTo(status);
        }
    }

    @Nested
    @DisplayName("파일 정보 테스트")
    class FileInfoTest {

        @Test
        @DisplayName("작은 파일 크기를 저장할 수 있다")
        void of_WithSmallFileSize_ShouldStoreCorrectly() {
            // given - 1KB
            long smallSize = 1024L;

            // when
            SingleUploadSessionJpaEntity entity = createEntityWithFileSize(smallSize);

            // then
            assertThat(entity.getFileSize()).isEqualTo(smallSize);
        }

        @Test
        @DisplayName("큰 파일 크기를 저장할 수 있다")
        void of_WithLargeFileSize_ShouldStoreCorrectly() {
            // given - 5GB
            long largeSize = 5L * 1024 * 1024 * 1024;

            // when
            SingleUploadSessionJpaEntity entity = createEntityWithFileSize(largeSize);

            // then
            assertThat(entity.getFileSize()).isEqualTo(largeSize);
        }

        @Test
        @DisplayName("다양한 Content-Type을 저장할 수 있다")
        void of_WithVariousContentTypes_ShouldStoreCorrectly() {
            // given
            String imageContentType = "image/jpeg";

            // when
            SingleUploadSessionJpaEntity entity = createEntityWithContentType(imageContentType);

            // then
            assertThat(entity.getContentType()).isEqualTo(imageContentType);
        }
    }

    @Nested
    @DisplayName("Presigned URL 테스트")
    class PresignedUrlTest {

        @Test
        @DisplayName("긴 Presigned URL을 저장할 수 있다")
        void of_WithLongPresignedUrl_ShouldStoreCorrectly() {
            // given
            String longUrl =
                    "https://bucket.s3.region.amazonaws.com/key?"
                            + "X-Amz-Algorithm=AWS4-HMAC-SHA256"
                            + "&X-Amz-Credential=..."
                            + "&X-Amz-Date=20251126T100000Z"
                            + "&X-Amz-Expires=3600"
                            + "&X-Amz-Signature=abcdef1234567890";

            // when
            SingleUploadSessionJpaEntity entity = createEntityWithPresignedUrl(longUrl);

            // then
            assertThat(entity.getPresignedUrl()).isEqualTo(longUrl);
        }
    }

    @Nested
    @DisplayName("BaseAuditEntity 상속 테스트")
    class InheritanceTest {

        @Test
        @DisplayName("BaseAuditEntity의 감사 필드를 상속받는다")
        void inheritance_ShouldProvideAuditFields() {
            // given
            Instant createdAt = LocalDateTime.of(2025, 1, 1, 0, 0).toInstant(ZoneOffset.UTC);
            Instant updatedAt = LocalDateTime.of(2025, 6, 1, 12, 0).toInstant(ZoneOffset.UTC);

            // when
            SingleUploadSessionJpaEntity entity =
                    SingleUploadSessionJpaEntity.of(
                            "id",
                            "key",
                            "01912345-6789-7abc-def0-123456789200",
                            "01912345-6789-7abc-def0-123456789100",
                            "org",
                            "ns",
                            "01912345-6789-7abc-def0-123456789001",
                            "tenant",
                            "USER",
                            null,
                            "file.txt",
                            100L,
                            "text/plain",
                            "bucket",
                            "key",
                            Instant.now().plus(java.time.Duration.ofDays(1)),
                            SessionStatus.PREPARING,
                            "url",
                            null,
                            null,
                            0L,
                            createdAt,
                            updatedAt);

            // then
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    // ==================== Helper Methods ====================

    private SingleUploadSessionJpaEntity createEntity(String userId) {
        return SingleUploadSessionJpaEntity.of(
                "id",
                "key",
                userId,
                "01912345-6789-7abc-def0-123456789100",
                "org",
                "ns",
                "01912345-6789-7abc-def0-123456789001",
                "tenant",
                "USER",
                "email@test.com",
                "file.txt",
                100L,
                "text/plain",
                "bucket",
                "key",
                Instant.now().plus(java.time.Duration.ofDays(1)),
                SessionStatus.PREPARING,
                "url",
                null,
                null,
                0L,
                Instant.now(),
                Instant.now());
    }

    private SingleUploadSessionJpaEntity createEntityWithEmail(String email) {
        return SingleUploadSessionJpaEntity.of(
                "id",
                "key",
                "01912345-6789-7abc-def0-123456789200",
                "01912345-6789-7abc-def0-123456789100",
                "org",
                "ns",
                "01912345-6789-7abc-def0-123456789001",
                "tenant",
                "USER",
                email,
                "file.txt",
                100L,
                "text/plain",
                "bucket",
                "key",
                Instant.now().plus(java.time.Duration.ofDays(1)),
                SessionStatus.PREPARING,
                "url",
                null,
                null,
                0L,
                Instant.now(),
                Instant.now());
    }

    private SingleUploadSessionJpaEntity createCompletedEntity(
            String etag, Instant completedAt, SessionStatus status) {
        return SingleUploadSessionJpaEntity.of(
                "id",
                "key",
                "01912345-6789-7abc-def0-123456789200",
                "01912345-6789-7abc-def0-123456789100",
                "org",
                "ns",
                "01912345-6789-7abc-def0-123456789001",
                "tenant",
                "USER",
                "email@test.com",
                "file.txt",
                100L,
                "text/plain",
                "bucket",
                "key",
                Instant.now().plus(java.time.Duration.ofDays(1)),
                status,
                "url",
                etag,
                completedAt,
                0L,
                Instant.now(),
                Instant.now());
    }

    private SingleUploadSessionJpaEntity createEntityWithStatus(SessionStatus status) {
        return SingleUploadSessionJpaEntity.of(
                "id",
                "key",
                "01912345-6789-7abc-def0-123456789200",
                "01912345-6789-7abc-def0-123456789100",
                "org",
                "ns",
                "01912345-6789-7abc-def0-123456789001",
                "tenant",
                "USER",
                "email@test.com",
                "file.txt",
                100L,
                "text/plain",
                "bucket",
                "key",
                Instant.now().plus(java.time.Duration.ofDays(1)),
                status,
                "url",
                null,
                null,
                0L,
                Instant.now(),
                Instant.now());
    }

    private SingleUploadSessionJpaEntity createEntityWithFileSize(long fileSize) {
        return SingleUploadSessionJpaEntity.of(
                "id",
                "key",
                "01912345-6789-7abc-def0-123456789200",
                "01912345-6789-7abc-def0-123456789100",
                "org",
                "ns",
                "01912345-6789-7abc-def0-123456789001",
                "tenant",
                "USER",
                "email@test.com",
                "file.txt",
                fileSize,
                "text/plain",
                "bucket",
                "key",
                Instant.now().plus(java.time.Duration.ofDays(1)),
                SessionStatus.PREPARING,
                "url",
                null,
                null,
                0L,
                Instant.now(),
                Instant.now());
    }

    private SingleUploadSessionJpaEntity createEntityWithContentType(String contentType) {
        return SingleUploadSessionJpaEntity.of(
                "id",
                "key",
                "01912345-6789-7abc-def0-123456789200",
                "01912345-6789-7abc-def0-123456789100",
                "org",
                "ns",
                "01912345-6789-7abc-def0-123456789001",
                "tenant",
                "USER",
                "email@test.com",
                "file.ext",
                100L,
                contentType,
                "bucket",
                "key",
                Instant.now().plus(java.time.Duration.ofDays(1)),
                SessionStatus.PREPARING,
                "url",
                null,
                null,
                0L,
                Instant.now(),
                Instant.now());
    }

    private SingleUploadSessionJpaEntity createEntityWithPresignedUrl(String presignedUrl) {
        return SingleUploadSessionJpaEntity.of(
                "id",
                "key",
                "01912345-6789-7abc-def0-123456789200",
                "01912345-6789-7abc-def0-123456789100",
                "org",
                "ns",
                "01912345-6789-7abc-def0-123456789001",
                "tenant",
                "USER",
                "email@test.com",
                "file.txt",
                100L,
                "text/plain",
                "bucket",
                "key",
                Instant.now().plus(java.time.Duration.ofDays(1)),
                SessionStatus.PREPARING,
                presignedUrl,
                null,
                null,
                0L,
                Instant.now(),
                Instant.now());
    }
}
