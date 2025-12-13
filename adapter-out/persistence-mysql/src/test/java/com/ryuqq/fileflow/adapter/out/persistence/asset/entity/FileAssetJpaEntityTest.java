package com.ryuqq.fileflow.adapter.out.persistence.asset.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UserId;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("FileAssetJpaEntity 단위 테스트")
class FileAssetJpaEntityTest {
    // 테스트용 UUIDv7 값 (실제 UUIDv7 형식)
    private static final String TEST_TENANT_ID = TenantId.generate().value();
    private static final String TEST_ORG_ID = OrganizationId.generate().value();
    private static final String TEST_USER_ID = UserId.generate().value();

    @Nested
    @DisplayName("of 팩토리 메서드 테스트")
    class OfFactoryMethodTest {

        @Test
        @DisplayName("모든 필드로 Entity를 생성할 수 있다")
        void of_WithAllFields_ShouldCreateEntity() {
            // given
            String userId = TEST_USER_ID;
            String organizationId = TEST_ORG_ID;
            String tenantId = TEST_TENANT_ID;
            Instant processedAt = LocalDateTime.of(2025, 11, 26, 12, 0).toInstant(ZoneOffset.UTC);
            Instant createdAt = LocalDateTime.of(2025, 11, 26, 10, 0).toInstant(ZoneOffset.UTC);
            Instant updatedAt = LocalDateTime.of(2025, 11, 26, 10, 5).toInstant(ZoneOffset.UTC);

            // when
            FileAssetJpaEntity entity =
                    FileAssetJpaEntity.of(
                            "file-asset-123",
                            "session-456",
                            "document.pdf",
                            1024 * 1024L,
                            "application/pdf",
                            FileCategory.DOCUMENT,
                            null, // imageWidth
                            null, // imageHeight
                            "test-bucket",
                            "uploads/document.pdf",
                            "\"etag-abc123\"",
                            userId,
                            organizationId,
                            tenantId,
                            FileAssetStatus.COMPLETED,
                            processedAt,
                            null,
                            createdAt,
                            updatedAt);

            // then
            assertThat(entity.getId()).isEqualTo("file-asset-123");
            assertThat(entity.getSessionId()).isEqualTo("session-456");
            assertThat(entity.getFileName()).isEqualTo("document.pdf");
            assertThat(entity.getFileSize()).isEqualTo(1024 * 1024L);
            assertThat(entity.getContentType()).isEqualTo("application/pdf");
            assertThat(entity.getCategory()).isEqualTo(FileCategory.DOCUMENT);
            assertThat(entity.getBucket()).isEqualTo("test-bucket");
            assertThat(entity.getS3Key()).isEqualTo("uploads/document.pdf");
            assertThat(entity.getEtag()).isEqualTo("\"etag-abc123\"");
            assertThat(entity.getUserId()).isEqualTo(userId);
            assertThat(entity.getOrganizationId()).isEqualTo(organizationId);
            assertThat(entity.getTenantId()).isEqualTo(tenantId);
            assertThat(entity.getStatus()).isEqualTo(FileAssetStatus.COMPLETED);
            assertThat(entity.getProcessedAt()).isEqualTo(processedAt);
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("userId가 null인 경우에도 생성할 수 있다")
        void of_WithNullUserId_ShouldCreateEntity() {
            // when
            FileAssetJpaEntity entity = createEntity(null);

            // then
            assertThat(entity.getUserId()).isNull();
        }

        @Test
        @DisplayName("processedAt이 null인 경우에도 생성할 수 있다")
        void of_WithNullProcessedAt_ShouldCreateEntity() {
            // when
            FileAssetJpaEntity entity = createEntityWithProcessedAt(null);

            // then
            assertThat(entity.getProcessedAt()).isNull();
        }

        @Test
        @DisplayName("PENDING 상태로 생성할 수 있다")
        void of_WithPendingStatus_ShouldCreateEntity() {
            // when
            FileAssetJpaEntity entity =
                    createEntityWithStatusAndProcessedAt(FileAssetStatus.PENDING, null);

            // then
            assertThat(entity.getStatus()).isEqualTo(FileAssetStatus.PENDING);
            assertThat(entity.getProcessedAt()).isNull();
        }

        @Test
        @DisplayName("COMPLETED 상태는 processedAt이 설정된다")
        void of_WithCompletedStatus_ShouldHaveProcessedAt() {
            // given
            Instant processedAt = LocalDateTime.of(2025, 11, 26, 12, 0).toInstant(ZoneOffset.UTC);

            // when
            FileAssetJpaEntity entity =
                    createEntityWithStatusAndProcessedAt(FileAssetStatus.COMPLETED, processedAt);

            // then
            assertThat(entity.getStatus()).isEqualTo(FileAssetStatus.COMPLETED);
            assertThat(entity.getProcessedAt()).isEqualTo(processedAt);
        }
    }

    @Nested
    @DisplayName("상태별 테스트")
    class StatusTest {

        @ParameterizedTest
        @EnumSource(FileAssetStatus.class)
        @DisplayName("모든 FileAssetStatus로 Entity를 생성할 수 있다")
        void of_WithAllStatuses_ShouldCreateEntity(FileAssetStatus status) {
            // when
            FileAssetJpaEntity entity = createEntityWithStatus(status);

            // then
            assertThat(entity.getStatus()).isEqualTo(status);
        }
    }

    @Nested
    @DisplayName("카테고리별 테스트")
    class CategoryTest {

        @ParameterizedTest
        @EnumSource(FileCategory.class)
        @DisplayName("모든 FileCategory로 Entity를 생성할 수 있다")
        void of_WithAllCategories_ShouldCreateEntity(FileCategory category) {
            // when
            FileAssetJpaEntity entity = createEntityWithCategory(category);

            // then
            assertThat(entity.getCategory()).isEqualTo(category);
        }

        @Test
        @DisplayName("IMAGE 카테고리로 생성할 수 있다")
        void of_WithImageCategory_ShouldCreateEntity() {
            // when
            FileAssetJpaEntity entity =
                    createEntityWithCategoryAndContentType(FileCategory.IMAGE, "image/jpeg");

            // then
            assertThat(entity.getCategory()).isEqualTo(FileCategory.IMAGE);
            assertThat(entity.getContentType()).isEqualTo("image/jpeg");
        }

        @Test
        @DisplayName("VIDEO 카테고리로 생성할 수 있다")
        void of_WithVideoCategory_ShouldCreateEntity() {
            // when
            FileAssetJpaEntity entity =
                    createEntityWithCategoryAndContentType(FileCategory.VIDEO, "video/mp4");

            // then
            assertThat(entity.getCategory()).isEqualTo(FileCategory.VIDEO);
            assertThat(entity.getContentType()).isEqualTo("video/mp4");
        }

        @Test
        @DisplayName("AUDIO 카테고리로 생성할 수 있다")
        void of_WithAudioCategory_ShouldCreateEntity() {
            // when
            FileAssetJpaEntity entity =
                    createEntityWithCategoryAndContentType(FileCategory.AUDIO, "audio/mpeg");

            // then
            assertThat(entity.getCategory()).isEqualTo(FileCategory.AUDIO);
            assertThat(entity.getContentType()).isEqualTo("audio/mpeg");
        }

        @Test
        @DisplayName("DOCUMENT 카테고리로 생성할 수 있다")
        void of_WithDocumentCategory_ShouldCreateEntity() {
            // when
            FileAssetJpaEntity entity =
                    createEntityWithCategoryAndContentType(
                            FileCategory.DOCUMENT, "application/pdf");

            // then
            assertThat(entity.getCategory()).isEqualTo(FileCategory.DOCUMENT);
            assertThat(entity.getContentType()).isEqualTo("application/pdf");
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
            FileAssetJpaEntity entity = createEntityWithFileSize(smallSize);

            // then
            assertThat(entity.getFileSize()).isEqualTo(smallSize);
        }

        @Test
        @DisplayName("큰 파일 크기를 저장할 수 있다")
        void of_WithLargeFileSize_ShouldStoreCorrectly() {
            // given - 5GB
            long largeSize = 5L * 1024 * 1024 * 1024;

            // when
            FileAssetJpaEntity entity = createEntityWithFileSize(largeSize);

            // then
            assertThat(entity.getFileSize()).isEqualTo(largeSize);
        }

        @Test
        @DisplayName("한글 파일명을 저장할 수 있다")
        void of_WithKoreanFileName_ShouldStoreCorrectly() {
            // given
            String koreanFileName = "한글파일명_테스트.pdf";

            // when
            FileAssetJpaEntity entity = createEntityWithFileName(koreanFileName);

            // then
            assertThat(entity.getFileName()).isEqualTo(koreanFileName);
        }

        @Test
        @DisplayName("특수문자가 포함된 파일명을 저장할 수 있다")
        void of_WithSpecialCharsFileName_ShouldStoreCorrectly() {
            // given
            String specialFileName = "file (1) [copy] #2.pdf";

            // when
            FileAssetJpaEntity entity = createEntityWithFileName(specialFileName);

            // then
            assertThat(entity.getFileName()).isEqualTo(specialFileName);
        }
    }

    @Nested
    @DisplayName("S3 정보 테스트")
    class S3InfoTest {

        @Test
        @DisplayName("긴 S3 Key를 저장할 수 있다")
        void of_WithLongS3Key_ShouldStoreCorrectly() {
            // given
            String longS3Key =
                    "tenant/1/org/100/uploads/2025/11/26/category/subcategory/uuid-123-456.pdf";

            // when
            FileAssetJpaEntity entity = createEntityWithS3Key(longS3Key);

            // then
            assertThat(entity.getS3Key()).isEqualTo(longS3Key);
        }

        @Test
        @DisplayName("S3 Bucket 이름을 저장할 수 있다")
        void of_WithBucketName_ShouldStoreCorrectly() {
            // given
            String bucketName = "my-fileflow-bucket-production";

            // when
            FileAssetJpaEntity entity = createEntityWithBucket(bucketName);

            // then
            assertThat(entity.getBucket()).isEqualTo(bucketName);
        }
    }

    @Nested
    @DisplayName("ETag 테스트")
    class EtagTest {

        @Test
        @DisplayName("따옴표가 포함된 ETag를 저장할 수 있다")
        void of_WithQuotedEtag_ShouldStoreCorrectly() {
            // given
            String quotedEtag = "\"abc123def456\"";

            // when
            FileAssetJpaEntity entity = createEntityWithEtag(quotedEtag);

            // then
            assertThat(entity.getEtag()).isEqualTo(quotedEtag);
        }

        @Test
        @DisplayName("Multipart ETag 형식을 저장할 수 있다")
        void of_WithMultipartEtag_ShouldStoreCorrectly() {
            // given - Multipart 업로드 시 ETag 형식
            String multipartEtag = "\"abc123def456-10\"";

            // when
            FileAssetJpaEntity entity = createEntityWithEtag(multipartEtag);

            // then
            assertThat(entity.getEtag()).isEqualTo(multipartEtag);
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
            FileAssetJpaEntity entity =
                    FileAssetJpaEntity.of(
                            "id",
                            "session",
                            "file.txt",
                            100L,
                            "text/plain",
                            FileCategory.DOCUMENT,
                            null, // imageWidth
                            null, // imageHeight
                            "bucket",
                            "key",
                            "etag",
                            TEST_USER_ID,
                            TEST_ORG_ID,
                            TEST_TENANT_ID,
                            FileAssetStatus.COMPLETED,
                            Instant.now(),
                            null,
                            createdAt,
                            updatedAt);

            // then
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    // ==================== Helper Methods ====================

    private FileAssetJpaEntity createEntity(String userId) {
        Instant now = Instant.now();
        return FileAssetJpaEntity.of(
                "id",
                "session",
                "file.txt",
                100L,
                "text/plain",
                FileCategory.DOCUMENT,
                null, // imageWidth
                null, // imageHeight
                "bucket",
                "key",
                "etag",
                userId,
                TEST_ORG_ID,
                TEST_TENANT_ID,
                FileAssetStatus.COMPLETED,
                now,
                null,
                now,
                now);
    }

    private FileAssetJpaEntity createEntityWithProcessedAt(Instant processedAt) {
        Instant now = Instant.now();
        return FileAssetJpaEntity.of(
                "id",
                "session",
                "file.txt",
                100L,
                "text/plain",
                FileCategory.DOCUMENT,
                null, // imageWidth
                null, // imageHeight
                "bucket",
                "key",
                "etag",
                TEST_USER_ID,
                TEST_ORG_ID,
                TEST_TENANT_ID,
                FileAssetStatus.COMPLETED,
                processedAt,
                null,
                now,
                now);
    }

    private FileAssetJpaEntity createEntityWithStatus(FileAssetStatus status) {
        Instant now = Instant.now();
        return FileAssetJpaEntity.of(
                "id",
                "session",
                "file.txt",
                100L,
                "text/plain",
                FileCategory.DOCUMENT,
                null, // imageWidth
                null, // imageHeight
                "bucket",
                "key",
                "etag",
                TEST_USER_ID,
                TEST_ORG_ID,
                TEST_TENANT_ID,
                status,
                status == FileAssetStatus.COMPLETED ? now : null,
                null,
                now,
                now);
    }

    private FileAssetJpaEntity createEntityWithStatusAndProcessedAt(
            FileAssetStatus status, Instant processedAt) {
        Instant now = Instant.now();
        return FileAssetJpaEntity.of(
                "id",
                "session",
                "file.txt",
                100L,
                "text/plain",
                FileCategory.DOCUMENT,
                null, // imageWidth
                null, // imageHeight
                "bucket",
                "key",
                "etag",
                TEST_USER_ID,
                TEST_ORG_ID,
                TEST_TENANT_ID,
                status,
                processedAt,
                null,
                now,
                now);
    }

    private FileAssetJpaEntity createEntityWithCategory(FileCategory category) {
        Instant now = Instant.now();
        return FileAssetJpaEntity.of(
                "id",
                "session",
                "file.ext",
                100L,
                "application/octet-stream",
                category,
                null, // imageWidth
                null, // imageHeight
                "bucket",
                "key",
                "etag",
                TEST_USER_ID,
                TEST_ORG_ID,
                TEST_TENANT_ID,
                FileAssetStatus.COMPLETED,
                now,
                null,
                now,
                now);
    }

    private FileAssetJpaEntity createEntityWithCategoryAndContentType(
            FileCategory category, String contentType) {
        Instant now = Instant.now();
        return FileAssetJpaEntity.of(
                "id",
                "session",
                "file.ext",
                100L,
                contentType,
                category,
                null, // imageWidth
                null, // imageHeight
                "bucket",
                "key",
                "etag",
                TEST_USER_ID,
                TEST_ORG_ID,
                TEST_TENANT_ID,
                FileAssetStatus.COMPLETED,
                now,
                null,
                now,
                now);
    }

    private FileAssetJpaEntity createEntityWithFileSize(long fileSize) {
        Instant now = Instant.now();
        return FileAssetJpaEntity.of(
                "id",
                "session",
                "file.txt",
                fileSize,
                "text/plain",
                FileCategory.DOCUMENT,
                null, // imageWidth
                null, // imageHeight
                "bucket",
                "key",
                "etag",
                TEST_USER_ID,
                TEST_ORG_ID,
                TEST_TENANT_ID,
                FileAssetStatus.COMPLETED,
                now,
                null,
                now,
                now);
    }

    private FileAssetJpaEntity createEntityWithFileName(String fileName) {
        Instant now = Instant.now();
        return FileAssetJpaEntity.of(
                "id",
                "session",
                fileName,
                100L,
                "application/octet-stream",
                FileCategory.DOCUMENT,
                null, // imageWidth
                null, // imageHeight
                "bucket",
                "key",
                "etag",
                TEST_USER_ID,
                TEST_ORG_ID,
                TEST_TENANT_ID,
                FileAssetStatus.COMPLETED,
                now,
                null,
                now,
                now);
    }

    private FileAssetJpaEntity createEntityWithS3Key(String s3Key) {
        Instant now = Instant.now();
        return FileAssetJpaEntity.of(
                "id",
                "session",
                "file.txt",
                100L,
                "text/plain",
                FileCategory.DOCUMENT,
                null, // imageWidth
                null, // imageHeight
                "bucket",
                s3Key,
                "etag",
                TEST_USER_ID,
                TEST_ORG_ID,
                TEST_TENANT_ID,
                FileAssetStatus.COMPLETED,
                now,
                null,
                now,
                now);
    }

    private FileAssetJpaEntity createEntityWithBucket(String bucket) {
        Instant now = Instant.now();
        return FileAssetJpaEntity.of(
                "id",
                "session",
                "file.txt",
                100L,
                "text/plain",
                FileCategory.DOCUMENT,
                null, // imageWidth
                null, // imageHeight
                bucket,
                "key",
                "etag",
                TEST_USER_ID,
                TEST_ORG_ID,
                TEST_TENANT_ID,
                FileAssetStatus.COMPLETED,
                now,
                null,
                now,
                now);
    }

    private FileAssetJpaEntity createEntityWithEtag(String etag) {
        Instant now = Instant.now();
        return FileAssetJpaEntity.of(
                "id",
                "session",
                "file.txt",
                100L,
                "text/plain",
                FileCategory.DOCUMENT,
                null, // imageWidth
                null, // imageHeight
                "bucket",
                "key",
                etag,
                TEST_USER_ID,
                TEST_ORG_ID,
                TEST_TENANT_ID,
                FileAssetStatus.COMPLETED,
                now,
                null,
                now,
                now);
    }
}
