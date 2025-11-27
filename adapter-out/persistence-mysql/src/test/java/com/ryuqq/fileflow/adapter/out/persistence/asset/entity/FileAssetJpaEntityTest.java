package com.ryuqq.fileflow.adapter.out.persistence.asset.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("FileAssetJpaEntity 단위 테스트")
class FileAssetJpaEntityTest {

    @Nested
    @DisplayName("of 팩토리 메서드 테스트")
    class OfFactoryMethodTest {

        @Test
        @DisplayName("모든 필드로 Entity를 생성할 수 있다")
        void of_WithAllFields_ShouldCreateEntity() {
            // given
            LocalDateTime processedAt = LocalDateTime.of(2025, 11, 26, 12, 0);
            LocalDateTime createdAt = LocalDateTime.of(2025, 11, 26, 10, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2025, 11, 26, 10, 5);

            // when
            FileAssetJpaEntity entity =
                    FileAssetJpaEntity.of(
                            "file-asset-123",
                            "session-456",
                            "document.pdf",
                            1024 * 1024L,
                            "application/pdf",
                            FileCategory.DOCUMENT,
                            "test-bucket",
                            "uploads/document.pdf",
                            "\"etag-abc123\"",
                            1L,
                            100L,
                            1L,
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
            assertThat(entity.getUserId()).isEqualTo(1L);
            assertThat(entity.getOrganizationId()).isEqualTo(100L);
            assertThat(entity.getTenantId()).isEqualTo(1L);
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
            LocalDateTime processedAt = LocalDateTime.of(2025, 11, 26, 12, 0);

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
            LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 0, 0);
            LocalDateTime updatedAt = LocalDateTime.of(2025, 6, 1, 12, 0);

            // when
            FileAssetJpaEntity entity =
                    FileAssetJpaEntity.of(
                            "id",
                            "session",
                            "file.txt",
                            100L,
                            "text/plain",
                            FileCategory.DOCUMENT,
                            "bucket",
                            "key",
                            "etag",
                            1L,
                            1L,
                            1L,
                            FileAssetStatus.COMPLETED,
                            LocalDateTime.now(),
                            null,
                            createdAt,
                            updatedAt);

            // then
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    // ==================== Helper Methods ====================

    private FileAssetJpaEntity createEntity(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return FileAssetJpaEntity.of(
                "id",
                "session",
                "file.txt",
                100L,
                "text/plain",
                FileCategory.DOCUMENT,
                "bucket",
                "key",
                "etag",
                userId,
                1L,
                1L,
                FileAssetStatus.COMPLETED,
                now,
                null,
                now,
                now);
    }

    private FileAssetJpaEntity createEntityWithProcessedAt(LocalDateTime processedAt) {
        LocalDateTime now = LocalDateTime.now();
        return FileAssetJpaEntity.of(
                "id",
                "session",
                "file.txt",
                100L,
                "text/plain",
                FileCategory.DOCUMENT,
                "bucket",
                "key",
                "etag",
                1L,
                1L,
                1L,
                FileAssetStatus.COMPLETED,
                processedAt,
                null,
                now,
                now);
    }

    private FileAssetJpaEntity createEntityWithStatus(FileAssetStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return FileAssetJpaEntity.of(
                "id",
                "session",
                "file.txt",
                100L,
                "text/plain",
                FileCategory.DOCUMENT,
                "bucket",
                "key",
                "etag",
                1L,
                1L,
                1L,
                status,
                status == FileAssetStatus.COMPLETED ? now : null,
                null,
                now,
                now);
    }

    private FileAssetJpaEntity createEntityWithStatusAndProcessedAt(
            FileAssetStatus status, LocalDateTime processedAt) {
        LocalDateTime now = LocalDateTime.now();
        return FileAssetJpaEntity.of(
                "id",
                "session",
                "file.txt",
                100L,
                "text/plain",
                FileCategory.DOCUMENT,
                "bucket",
                "key",
                "etag",
                1L,
                1L,
                1L,
                status,
                processedAt,
                null,
                now,
                now);
    }

    private FileAssetJpaEntity createEntityWithCategory(FileCategory category) {
        LocalDateTime now = LocalDateTime.now();
        return FileAssetJpaEntity.of(
                "id",
                "session",
                "file.ext",
                100L,
                "application/octet-stream",
                category,
                "bucket",
                "key",
                "etag",
                1L,
                1L,
                1L,
                FileAssetStatus.COMPLETED,
                now,
                null,
                now,
                now);
    }

    private FileAssetJpaEntity createEntityWithCategoryAndContentType(
            FileCategory category, String contentType) {
        LocalDateTime now = LocalDateTime.now();
        return FileAssetJpaEntity.of(
                "id",
                "session",
                "file.ext",
                100L,
                contentType,
                category,
                "bucket",
                "key",
                "etag",
                1L,
                1L,
                1L,
                FileAssetStatus.COMPLETED,
                now,
                null,
                now,
                now);
    }

    private FileAssetJpaEntity createEntityWithFileSize(long fileSize) {
        LocalDateTime now = LocalDateTime.now();
        return FileAssetJpaEntity.of(
                "id",
                "session",
                "file.txt",
                fileSize,
                "text/plain",
                FileCategory.DOCUMENT,
                "bucket",
                "key",
                "etag",
                1L,
                1L,
                1L,
                FileAssetStatus.COMPLETED,
                now,
                null,
                now,
                now);
    }

    private FileAssetJpaEntity createEntityWithFileName(String fileName) {
        LocalDateTime now = LocalDateTime.now();
        return FileAssetJpaEntity.of(
                "id",
                "session",
                fileName,
                100L,
                "application/octet-stream",
                FileCategory.DOCUMENT,
                "bucket",
                "key",
                "etag",
                1L,
                1L,
                1L,
                FileAssetStatus.COMPLETED,
                now,
                null,
                now,
                now);
    }

    private FileAssetJpaEntity createEntityWithS3Key(String s3Key) {
        LocalDateTime now = LocalDateTime.now();
        return FileAssetJpaEntity.of(
                "id",
                "session",
                "file.txt",
                100L,
                "text/plain",
                FileCategory.DOCUMENT,
                "bucket",
                s3Key,
                "etag",
                1L,
                1L,
                1L,
                FileAssetStatus.COMPLETED,
                now,
                null,
                now,
                now);
    }

    private FileAssetJpaEntity createEntityWithBucket(String bucket) {
        LocalDateTime now = LocalDateTime.now();
        return FileAssetJpaEntity.of(
                "id",
                "session",
                "file.txt",
                100L,
                "text/plain",
                FileCategory.DOCUMENT,
                bucket,
                "key",
                "etag",
                1L,
                1L,
                1L,
                FileAssetStatus.COMPLETED,
                now,
                null,
                now,
                now);
    }

    private FileAssetJpaEntity createEntityWithEtag(String etag) {
        LocalDateTime now = LocalDateTime.now();
        return FileAssetJpaEntity.of(
                "id",
                "session",
                "file.txt",
                100L,
                "text/plain",
                FileCategory.DOCUMENT,
                "bucket",
                "key",
                etag,
                1L,
                1L,
                1L,
                FileAssetStatus.COMPLETED,
                now,
                null,
                now,
                now);
    }
}
