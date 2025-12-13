package com.ryuqq.fileflow.adapter.out.persistence.session.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CompletedPartJpaEntity 단위 테스트")
class CompletedPartJpaEntityTest {

    @Nested
    @DisplayName("of 팩토리 메서드 테스트")
    class OfFactoryMethodTest {

        @Test
        @DisplayName("모든 필드로 신규 Entity를 생성할 수 있다")
        void of_WithAllFields_ShouldCreateEntity() {
            // given
            Instant uploadedAt = LocalDateTime.of(2025, 11, 26, 11, 0).toInstant(ZoneOffset.UTC);
            Instant createdAt = LocalDateTime.of(2025, 11, 26, 10, 0).toInstant(ZoneOffset.UTC);
            Instant updatedAt = LocalDateTime.of(2025, 11, 26, 10, 5).toInstant(ZoneOffset.UTC);

            // when
            CompletedPartJpaEntity entity =
                    CompletedPartJpaEntity.of(
                            "session-123",
                            1,
                            "https://presigned-url.s3.amazonaws.com/part1",
                            "\"etag-part-1\"",
                            5 * 1024 * 1024L,
                            uploadedAt,
                            createdAt,
                            updatedAt);

            // then
            assertThat(entity.getId()).isNull(); // 신규 생성 시 id는 null
            assertThat(entity.getSessionId()).isEqualTo("session-123");
            assertThat(entity.getPartNumber()).isEqualTo(1);
            assertThat(entity.getPresignedUrl())
                    .isEqualTo("https://presigned-url.s3.amazonaws.com/part1");
            assertThat(entity.getEtag()).isEqualTo("\"etag-part-1\"");
            assertThat(entity.getSize()).isEqualTo(5 * 1024 * 1024L);
            assertThat(entity.getUploadedAt()).isEqualTo(uploadedAt);
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("첫 번째 Part(1)로 생성할 수 있다")
        void of_WithFirstPartNumber_ShouldCreateEntity() {
            // when
            CompletedPartJpaEntity entity = createEntityWithPartNumber(1);

            // then
            assertThat(entity.getPartNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("마지막 Part(10000)로 생성할 수 있다")
        void of_WithMaxPartNumber_ShouldCreateEntity() {
            // given - S3 최대 10,000 parts 지원
            int maxPartNumber = 10000;

            // when
            CompletedPartJpaEntity entity = createEntityWithPartNumber(maxPartNumber);

            // then
            assertThat(entity.getPartNumber()).isEqualTo(maxPartNumber);
        }
    }

    @Nested
    @DisplayName("reconstitute 팩토리 메서드 테스트")
    class ReconstituteFactoryMethodTest {

        @Test
        @DisplayName("id 포함하여 Entity를 복원할 수 있다")
        void reconstitute_WithId_ShouldReconstituteEntity() {
            // given
            Long id = 123L;
            Instant uploadedAt = LocalDateTime.of(2025, 11, 26, 11, 0).toInstant(ZoneOffset.UTC);
            Instant createdAt = LocalDateTime.of(2025, 11, 26, 10, 0).toInstant(ZoneOffset.UTC);
            Instant updatedAt = LocalDateTime.of(2025, 11, 26, 10, 5).toInstant(ZoneOffset.UTC);

            // when
            CompletedPartJpaEntity entity =
                    CompletedPartJpaEntity.reconstitute(
                            id,
                            "session-456",
                            2,
                            "https://presigned-url.s3.amazonaws.com/part2",
                            "\"etag-part-2\"",
                            10 * 1024 * 1024L,
                            uploadedAt,
                            createdAt,
                            updatedAt);

            // then
            assertThat(entity.getId()).isEqualTo(123L);
            assertThat(entity.getSessionId()).isEqualTo("session-456");
            assertThat(entity.getPartNumber()).isEqualTo(2);
            assertThat(entity.getPresignedUrl())
                    .isEqualTo("https://presigned-url.s3.amazonaws.com/part2");
            assertThat(entity.getEtag()).isEqualTo("\"etag-part-2\"");
            assertThat(entity.getSize()).isEqualTo(10 * 1024 * 1024L);
            assertThat(entity.getUploadedAt()).isEqualTo(uploadedAt);
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("of와 reconstitute의 차이는 id 포함 여부이다")
        void reconstitute_DifferenceFromOf_ShouldBeIdPresence() {
            // given
            Instant now = Instant.now();

            // when
            CompletedPartJpaEntity newEntity =
                    CompletedPartJpaEntity.of("session", 1, "url", "etag", 100L, now, now, now);

            CompletedPartJpaEntity reconstitutedEntity =
                    CompletedPartJpaEntity.reconstitute(
                            999L, "session", 1, "url", "etag", 100L, now, now, now);

            // then
            assertThat(newEntity.getId()).isNull();
            assertThat(reconstitutedEntity.getId()).isEqualTo(999L);
        }
    }

    @Nested
    @DisplayName("Part 크기 테스트")
    class PartSizeTest {

        @Test
        @DisplayName("최소 Part 크기(5MB)를 저장할 수 있다")
        void of_WithMinimumPartSize_ShouldCreateEntity() {
            // given - S3 최소 5MB (마지막 Part 제외)
            long minSize = 5 * 1024 * 1024L;

            // when
            CompletedPartJpaEntity entity = createEntityWithSize(minSize);

            // then
            assertThat(entity.getSize()).isEqualTo(minSize);
        }

        @Test
        @DisplayName("최대 Part 크기(5GB)를 저장할 수 있다")
        void of_WithMaximumPartSize_ShouldCreateEntity() {
            // given - S3 최대 5GB per part
            long maxSize = 5L * 1024 * 1024 * 1024;

            // when
            CompletedPartJpaEntity entity = createEntityWithSize(maxSize);

            // then
            assertThat(entity.getSize()).isEqualTo(maxSize);
        }

        @Test
        @DisplayName("마지막 Part는 5MB 미만일 수 있다")
        void of_WithLastPartSmallSize_ShouldCreateEntity() {
            // given - 마지막 Part는 5MB 미만 허용
            long smallSize = 1024L; // 1KB

            // when
            CompletedPartJpaEntity entity = createEntityWithSize(smallSize);

            // then
            assertThat(entity.getSize()).isEqualTo(smallSize);
        }
    }

    @Nested
    @DisplayName("ETag 테스트")
    class EtagTest {

        @Test
        @DisplayName("따옴표가 포함된 ETag를 저장할 수 있다")
        void of_WithQuotedEtag_ShouldCreateEntity() {
            // given
            String quotedEtag = "\"abc123def456\"";

            // when
            CompletedPartJpaEntity entity = createEntityWithEtag(quotedEtag);

            // then
            assertThat(entity.getEtag()).isEqualTo(quotedEtag);
        }

        @Test
        @DisplayName("MD5 형식 ETag를 저장할 수 있다")
        void of_WithMd5Etag_ShouldCreateEntity() {
            // given
            String md5Etag = "d41d8cd98f00b204e9800998ecf8427e";

            // when
            CompletedPartJpaEntity entity = createEntityWithEtag(md5Etag);

            // then
            assertThat(entity.getEtag()).isEqualTo(md5Etag);
        }
    }

    @Nested
    @DisplayName("Presigned URL 테스트")
    class PresignedUrlTest {

        @Test
        @DisplayName("긴 Presigned URL을 저장할 수 있다")
        void of_WithLongPresignedUrl_ShouldCreateEntity() {
            // given
            String longUrl =
                    "https://bucket.s3.region.amazonaws.com/multipart/key/partNumber=1?"
                        + "X-Amz-Algorithm=AWS4-HMAC-SHA256"
                        + "&X-Amz-Credential=AKIAIOSFODNN7EXAMPLE/20251126/us-east-1/s3/aws4_request"
                        + "&X-Amz-Date=20251126T100000Z&X-Amz-Expires=3600&X-Amz-SignedHeaders=host"
                        + "&X-Amz-Signature=abcdef1234567890abcdef1234567890abcdef1234567890";

            // when
            CompletedPartJpaEntity entity = createEntityWithPresignedUrl(longUrl);

            // then
            assertThat(entity.getPresignedUrl()).isEqualTo(longUrl);
        }
    }

    @Nested
    @DisplayName("uploadedAt 테스트")
    class UploadedAtTest {

        @Test
        @DisplayName("업로드 시각을 저장할 수 있다")
        void of_WithUploadedAt_ShouldCreateEntity() {
            // given
            Instant uploadedAt =
                    LocalDateTime.of(2025, 11, 26, 15, 30, 45).toInstant(ZoneOffset.UTC);

            // when
            CompletedPartJpaEntity entity = createEntityWithUploadedAt(uploadedAt);

            // then
            assertThat(entity.getUploadedAt()).isEqualTo(uploadedAt);
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
            CompletedPartJpaEntity entity =
                    CompletedPartJpaEntity.of(
                            "session", 1, "url", "etag", 100L, Instant.now(), createdAt, updatedAt);

            // then
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    // ==================== Helper Methods ====================

    private CompletedPartJpaEntity createEntityWithPartNumber(int partNumber) {
        Instant now = Instant.now();
        return CompletedPartJpaEntity.of(
                "session-id",
                partNumber,
                "https://presigned-url.s3.amazonaws.com/part",
                "\"etag\"",
                5 * 1024 * 1024L,
                now,
                now,
                now);
    }

    private CompletedPartJpaEntity createEntityWithSize(long size) {
        Instant now = Instant.now();
        return CompletedPartJpaEntity.of(
                "session-id",
                1,
                "https://presigned-url.s3.amazonaws.com/part",
                "\"etag\"",
                size,
                now,
                now,
                now);
    }

    private CompletedPartJpaEntity createEntityWithEtag(String etag) {
        Instant now = Instant.now();
        return CompletedPartJpaEntity.of(
                "session-id",
                1,
                "https://presigned-url.s3.amazonaws.com/part",
                etag,
                5 * 1024 * 1024L,
                now,
                now,
                now);
    }

    private CompletedPartJpaEntity createEntityWithPresignedUrl(String presignedUrl) {
        Instant now = Instant.now();
        return CompletedPartJpaEntity.of(
                "session-id", 1, presignedUrl, "\"etag\"", 5 * 1024 * 1024L, now, now, now);
    }

    private CompletedPartJpaEntity createEntityWithUploadedAt(Instant uploadedAt) {
        Instant now = Instant.now();
        return CompletedPartJpaEntity.of(
                "session-id",
                1,
                "https://presigned-url.s3.amazonaws.com/part",
                "\"etag\"",
                5 * 1024 * 1024L,
                uploadedAt,
                now,
                now);
    }
}
