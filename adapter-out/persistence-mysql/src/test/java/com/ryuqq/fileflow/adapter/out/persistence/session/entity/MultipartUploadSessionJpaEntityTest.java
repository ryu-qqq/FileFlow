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

@DisplayName("MultipartUploadSessionJpaEntity 단위 테스트")
class MultipartUploadSessionJpaEntityTest {

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
            MultipartUploadSessionJpaEntity entity =
                    MultipartUploadSessionJpaEntity.of(
                            "session-mp-123",
                            "01912345-6789-7abc-def0-123456789200",
                            "01912345-6789-7abc-def0-123456789100",
                            "Connectly Org",
                            "connectly",
                            "01912345-6789-7abc-def0-123456789001",
                            "Connectly",
                            "ADMIN",
                            "admin@example.com",
                            "large-video.mp4",
                            500 * 1024 * 1024L,
                            "video/mp4",
                            "test-bucket",
                            "uploads/large-video.mp4",
                            "s3-upload-id-xyz",
                            10,
                            50 * 1024 * 1024L,
                            expiresAt,
                            SessionStatus.PREPARING,
                            null,
                            null,
                            0L,
                            createdAt,
                            updatedAt);

            // then
            assertThat(entity.getId()).isEqualTo("session-mp-123");
            assertThat(entity.getUserId()).isEqualTo("01912345-6789-7abc-def0-123456789200");
            assertThat(entity.getOrganizationId()).isEqualTo("01912345-6789-7abc-def0-123456789100");
            assertThat(entity.getOrganizationName()).isEqualTo("Connectly Org");
            assertThat(entity.getOrganizationNamespace()).isEqualTo("connectly");
            assertThat(entity.getTenantId()).isEqualTo("01912345-6789-7abc-def0-123456789001");
            assertThat(entity.getTenantName()).isEqualTo("Connectly");
            assertThat(entity.getUserRole()).isEqualTo("ADMIN");
            assertThat(entity.getEmail()).isEqualTo("admin@example.com");
            assertThat(entity.getFileName()).isEqualTo("large-video.mp4");
            assertThat(entity.getFileSize()).isEqualTo(500 * 1024 * 1024L);
            assertThat(entity.getContentType()).isEqualTo("video/mp4");
            assertThat(entity.getBucket()).isEqualTo("test-bucket");
            assertThat(entity.getS3Key()).isEqualTo("uploads/large-video.mp4");
            assertThat(entity.getS3UploadId()).isEqualTo("s3-upload-id-xyz");
            assertThat(entity.getTotalParts()).isEqualTo(10);
            assertThat(entity.getPartSize()).isEqualTo(50 * 1024 * 1024L);
            assertThat(entity.getExpiresAt()).isEqualTo(expiresAt);
            assertThat(entity.getStatus()).isEqualTo(SessionStatus.PREPARING);
            assertThat(entity.getMergedEtag()).isNull();
            assertThat(entity.getCompletedAt()).isNull();
            assertThat(entity.getVersion()).isEqualTo(0L);
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("userId가 null인 경우에도 생성할 수 있다")
        void of_WithNullUserId_ShouldCreateEntity() {
            // when
            MultipartUploadSessionJpaEntity entity = createEntity(null);

            // then
            assertThat(entity.getUserId()).isNull();
        }

        @Test
        @DisplayName("email이 null인 경우에도 생성할 수 있다")
        void of_WithNullEmail_ShouldCreateEntity() {
            // when
            MultipartUploadSessionJpaEntity entity = createEntityWithEmail(null);

            // then
            assertThat(entity.getEmail()).isNull();
        }

        @Test
        @DisplayName("완료된 세션은 mergedEtag와 completedAt이 설정된다")
        void of_WithCompletedSession_ShouldHaveMergedEtagAndCompletedAt() {
            // given
            Instant completedAt = LocalDateTime.of(2025, 11, 26, 12, 0).toInstant(ZoneOffset.UTC);
            String mergedEtag = "\"abc123-10\"";

            // when
            MultipartUploadSessionJpaEntity entity =
                    createCompletedEntity(mergedEtag, completedAt, SessionStatus.COMPLETED);

            // then
            assertThat(entity.getStatus()).isEqualTo(SessionStatus.COMPLETED);
            assertThat(entity.getMergedEtag()).isEqualTo(mergedEtag);
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
            MultipartUploadSessionJpaEntity entity = createEntityWithStatus(status);

            // then
            assertThat(entity.getStatus()).isEqualTo(status);
        }
    }

    @Nested
    @DisplayName("멀티파트 정보 테스트")
    class MultipartInfoTest {

        @Test
        @DisplayName("최소 Part 수(1개)로 생성할 수 있다")
        void of_WithMinimumParts_ShouldCreateEntity() {
            // when
            MultipartUploadSessionJpaEntity entity = createEntityWithTotalParts(1);

            // then
            assertThat(entity.getTotalParts()).isEqualTo(1);
        }

        @Test
        @DisplayName("최대 Part 수(10000개)로 생성할 수 있다")
        void of_WithMaximumParts_ShouldCreateEntity() {
            // given - S3 최대 10,000 parts 지원
            int maxParts = 10000;

            // when
            MultipartUploadSessionJpaEntity entity = createEntityWithTotalParts(maxParts);

            // then
            assertThat(entity.getTotalParts()).isEqualTo(maxParts);
        }

        @Test
        @DisplayName("최소 Part 크기(5MB)를 저장할 수 있다")
        void of_WithMinimumPartSize_ShouldCreateEntity() {
            // given - S3 최소 5MB (마지막 Part 제외)
            long minPartSize = 5 * 1024 * 1024L;

            // when
            MultipartUploadSessionJpaEntity entity = createEntityWithPartSize(minPartSize);

            // then
            assertThat(entity.getPartSize()).isEqualTo(minPartSize);
        }

        @Test
        @DisplayName("최대 Part 크기(5GB)를 저장할 수 있다")
        void of_WithMaximumPartSize_ShouldCreateEntity() {
            // given - S3 최대 5GB per part
            long maxPartSize = 5L * 1024 * 1024 * 1024;

            // when
            MultipartUploadSessionJpaEntity entity = createEntityWithPartSize(maxPartSize);

            // then
            assertThat(entity.getPartSize()).isEqualTo(maxPartSize);
        }

        @Test
        @DisplayName("대용량 파일(5TB) 크기를 저장할 수 있다")
        void of_WithMaxFileSize_ShouldCreateEntity() {
            // given - S3 최대 5TB
            long maxFileSize = 5L * 1024 * 1024 * 1024 * 1024;

            // when
            MultipartUploadSessionJpaEntity entity = createEntityWithFileSize(maxFileSize);

            // then
            assertThat(entity.getFileSize()).isEqualTo(maxFileSize);
        }
    }

    @Nested
    @DisplayName("S3 Upload ID 테스트")
    class S3UploadIdTest {

        @Test
        @DisplayName("긴 S3 Upload ID를 저장할 수 있다")
        void of_WithLongS3UploadId_ShouldStoreCorrectly() {
            // given
            String longUploadId =
                    "aBcDeFgHiJkLmNoPqRsTuVwXyZ.abcdefghijklmnopqrstuvwxyz.1234567890";

            // when
            MultipartUploadSessionJpaEntity entity = createEntityWithS3UploadId(longUploadId);

            // then
            assertThat(entity.getS3UploadId()).isEqualTo(longUploadId);
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
            MultipartUploadSessionJpaEntity entity =
                    MultipartUploadSessionJpaEntity.of(
                            "id",
                            "01912345-6789-7abc-def0-123456789200",
                            "01912345-6789-7abc-def0-123456789100",
                            "org",
                            "ns",
                            "01912345-6789-7abc-def0-123456789001",
                            "tenant",
                            "USER",
                            null,
                            "file.txt",
                            100 * 1024 * 1024L,
                            "text/plain",
                            "bucket",
                            "key",
                            "upload-id",
                            5,
                            20 * 1024 * 1024L,
                            Instant.now().plus(java.time.Duration.ofDays(1)),
                            SessionStatus.PREPARING,
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

    private MultipartUploadSessionJpaEntity createEntity(String userId) {
        return MultipartUploadSessionJpaEntity.of(
                "id",
                userId,
                "01912345-6789-7abc-def0-123456789100",
                "org",
                "ns",
                "01912345-6789-7abc-def0-123456789001",
                "tenant",
                "USER",
                "email@test.com",
                "file.txt",
                100 * 1024 * 1024L,
                "text/plain",
                "bucket",
                "key",
                "upload-id",
                5,
                20 * 1024 * 1024L,
                Instant.now().plus(java.time.Duration.ofDays(1)),
                SessionStatus.PREPARING,
                null,
                null,
                0L,
                Instant.now(),
                Instant.now());
    }

    private MultipartUploadSessionJpaEntity createEntityWithEmail(String email) {
        return MultipartUploadSessionJpaEntity.of(
                "id",
                "01912345-6789-7abc-def0-123456789200",
                "01912345-6789-7abc-def0-123456789100",
                "org",
                "ns",
                "01912345-6789-7abc-def0-123456789001",
                "tenant",
                "USER",
                email,
                "file.txt",
                100 * 1024 * 1024L,
                "text/plain",
                "bucket",
                "key",
                "upload-id",
                5,
                20 * 1024 * 1024L,
                Instant.now().plus(java.time.Duration.ofDays(1)),
                SessionStatus.PREPARING,
                null,
                null,
                0L,
                Instant.now(),
                Instant.now());
    }

    private MultipartUploadSessionJpaEntity createCompletedEntity(
            String mergedEtag, Instant completedAt, SessionStatus status) {
        return MultipartUploadSessionJpaEntity.of(
                "id",
                "01912345-6789-7abc-def0-123456789200",
                "01912345-6789-7abc-def0-123456789100",
                "org",
                "ns",
                "01912345-6789-7abc-def0-123456789001",
                "tenant",
                "USER",
                "email@test.com",
                "file.txt",
                100 * 1024 * 1024L,
                "text/plain",
                "bucket",
                "key",
                "upload-id",
                5,
                20 * 1024 * 1024L,
                Instant.now().plus(java.time.Duration.ofDays(1)),
                status,
                mergedEtag,
                completedAt,
                0L,
                Instant.now(),
                Instant.now());
    }

    private MultipartUploadSessionJpaEntity createEntityWithStatus(SessionStatus status) {
        return MultipartUploadSessionJpaEntity.of(
                "id",
                "01912345-6789-7abc-def0-123456789200",
                "01912345-6789-7abc-def0-123456789100",
                "org",
                "ns",
                "01912345-6789-7abc-def0-123456789001",
                "tenant",
                "USER",
                "email@test.com",
                "file.txt",
                100 * 1024 * 1024L,
                "text/plain",
                "bucket",
                "key",
                "upload-id",
                5,
                20 * 1024 * 1024L,
                Instant.now().plus(java.time.Duration.ofDays(1)),
                status,
                null,
                null,
                0L,
                Instant.now(),
                Instant.now());
    }

    private MultipartUploadSessionJpaEntity createEntityWithTotalParts(int totalParts) {
        return MultipartUploadSessionJpaEntity.of(
                "id",
                "01912345-6789-7abc-def0-123456789200",
                "01912345-6789-7abc-def0-123456789100",
                "org",
                "ns",
                "01912345-6789-7abc-def0-123456789001",
                "tenant",
                "USER",
                "email@test.com",
                "file.txt",
                100 * 1024 * 1024L,
                "text/plain",
                "bucket",
                "key",
                "upload-id",
                totalParts,
                20 * 1024 * 1024L,
                Instant.now().plus(java.time.Duration.ofDays(1)),
                SessionStatus.PREPARING,
                null,
                null,
                0L,
                Instant.now(),
                Instant.now());
    }

    private MultipartUploadSessionJpaEntity createEntityWithPartSize(long partSize) {
        return MultipartUploadSessionJpaEntity.of(
                "id",
                "01912345-6789-7abc-def0-123456789200",
                "01912345-6789-7abc-def0-123456789100",
                "org",
                "ns",
                "01912345-6789-7abc-def0-123456789001",
                "tenant",
                "USER",
                "email@test.com",
                "file.txt",
                100 * 1024 * 1024L,
                "text/plain",
                "bucket",
                "key",
                "upload-id",
                5,
                partSize,
                Instant.now().plus(java.time.Duration.ofDays(1)),
                SessionStatus.PREPARING,
                null,
                null,
                0L,
                Instant.now(),
                Instant.now());
    }

    private MultipartUploadSessionJpaEntity createEntityWithFileSize(long fileSize) {
        return MultipartUploadSessionJpaEntity.of(
                "id",
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
                "upload-id",
                5,
                20 * 1024 * 1024L,
                Instant.now().plus(java.time.Duration.ofDays(1)),
                SessionStatus.PREPARING,
                null,
                null,
                0L,
                Instant.now(),
                Instant.now());
    }

    private MultipartUploadSessionJpaEntity createEntityWithS3UploadId(String s3UploadId) {
        return MultipartUploadSessionJpaEntity.of(
                "id",
                "01912345-6789-7abc-def0-123456789200",
                "01912345-6789-7abc-def0-123456789100",
                "org",
                "ns",
                "01912345-6789-7abc-def0-123456789001",
                "tenant",
                "USER",
                "email@test.com",
                "file.txt",
                100 * 1024 * 1024L,
                "text/plain",
                "bucket",
                "key",
                s3UploadId,
                5,
                20 * 1024 * 1024L,
                Instant.now().plus(java.time.Duration.ofDays(1)),
                SessionStatus.PREPARING,
                null,
                null,
                0L,
                Instant.now(),
                Instant.now());
    }
}
