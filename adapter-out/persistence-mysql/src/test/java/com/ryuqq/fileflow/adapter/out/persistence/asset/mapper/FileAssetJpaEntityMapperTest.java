package com.ryuqq.fileflow.adapter.out.persistence.asset.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.FileAssetJpaEntity;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UserId;
import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("FileAssetJpaEntityMapper 단위 테스트")
class FileAssetJpaEntityMapperTest {

    private FileAssetJpaEntityMapper mapper;

    // 테스트용 UUIDv7 값 (실제 UUIDv7 형식)
    private static final String TEST_USER_ID = UserId.generate().value();
    private static final String TEST_ORG_ID = OrganizationId.generate().value();
    private static final String TEST_TENANT_ID = TenantId.generate().value();

    @BeforeEach
    void setUp() {
        mapper = new FileAssetJpaEntityMapper();
    }

    @Nested
    @DisplayName("toEntity 테스트")
    class ToEntityTest {

        @Test
        @DisplayName("Domain을 Entity로 변환할 수 있다")
        void toEntity_WithValidDomain_ShouldConvertToEntity() {
            // given
            FileAsset domain = createDomain(FileAssetStatus.PENDING);

            // when
            FileAssetJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getId()).isEqualTo(domain.getIdValue());
            assertThat(entity.getSessionId()).isEqualTo(domain.getSessionIdValue());
            assertThat(entity.getFileName()).isEqualTo(domain.getFileNameValue());
            assertThat(entity.getFileSize()).isEqualTo(domain.getFileSizeValue());
            assertThat(entity.getContentType()).isEqualTo(domain.getContentTypeValue());
            assertThat(entity.getCategory()).isEqualTo(domain.getCategory());
            assertThat(entity.getBucket()).isEqualTo(domain.getBucketValue());
            assertThat(entity.getS3Key()).isEqualTo(domain.getS3KeyValue());
            assertThat(entity.getEtag()).isEqualTo(domain.getEtagValue());
            assertThat(entity.getUserId())
                    .isEqualTo(domain.getUserId() != null ? domain.getUserId().value() : null);
            assertThat(entity.getOrganizationId()).isEqualTo(domain.getOrganizationId().value());
            assertThat(entity.getTenantId()).isEqualTo(domain.getTenantId().value());
            assertThat(entity.getStatus()).isEqualTo(domain.getStatus());
        }

        @ParameterizedTest
        @EnumSource(FileAssetStatus.class)
        @DisplayName("모든 FileAssetStatus를 변환할 수 있다")
        void toEntity_WithAllStatuses_ShouldConvert(FileAssetStatus status) {
            // given
            FileAsset domain = createDomain(status);

            // when
            FileAssetJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getStatus()).isEqualTo(status);
        }

        @ParameterizedTest
        @EnumSource(FileCategory.class)
        @DisplayName("모든 FileCategory를 변환할 수 있다")
        void toEntity_WithAllCategories_ShouldConvert(FileCategory category) {
            // given
            FileAsset domain = createDomainWithCategory(category);

            // when
            FileAssetJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getCategory()).isEqualTo(category);
        }

        @Test
        @DisplayName("userId가 null인 경우도 변환할 수 있다")
        void toEntity_WithNullUserId_ShouldConvert() {
            // given
            FileAsset domain = createDomainWithNullUserId();

            // when
            FileAssetJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getUserId()).isNull();
        }

        @Test
        @DisplayName("processedAt이 설정된 경우 변환할 수 있다")
        void toEntity_WithProcessedAt_ShouldConvert() {
            // given
            FileAsset domain = createCompletedDomain();

            // when
            FileAssetJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getStatus()).isEqualTo(FileAssetStatus.COMPLETED);
            assertThat(entity.getProcessedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("toDomain 테스트")
    class ToDomainTest {

        @Test
        @DisplayName("Entity를 Domain으로 변환할 수 있다")
        void toDomain_WithValidEntity_ShouldConvertToDomain() {
            // given
            FileAssetJpaEntity entity = createEntity(FileAssetStatus.PENDING);

            // when
            FileAsset domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getIdValue()).isEqualTo(entity.getId());
            assertThat(domain.getSessionIdValue()).isEqualTo(entity.getSessionId());
            assertThat(domain.getFileNameValue()).isEqualTo(entity.getFileName());
            assertThat(domain.getFileSizeValue()).isEqualTo(entity.getFileSize());
            assertThat(domain.getContentTypeValue()).isEqualTo(entity.getContentType());
            assertThat(domain.getCategory()).isEqualTo(entity.getCategory());
            assertThat(domain.getBucketValue()).isEqualTo(entity.getBucket());
            assertThat(domain.getS3KeyValue()).isEqualTo(entity.getS3Key());
            assertThat(domain.getEtagValue()).isEqualTo(entity.getEtag());
            assertThat(domain.getUserId() != null ? domain.getUserId().value() : null)
                    .isEqualTo(entity.getUserId());
            assertThat(domain.getOrganizationId().value()).isEqualTo(entity.getOrganizationId());
            assertThat(domain.getTenantId().value()).isEqualTo(entity.getTenantId());
            assertThat(domain.getStatus()).isEqualTo(entity.getStatus());
        }

        @ParameterizedTest
        @EnumSource(FileAssetStatus.class)
        @DisplayName("모든 FileAssetStatus를 복원할 수 있다")
        void toDomain_WithAllStatuses_ShouldRestore(FileAssetStatus status) {
            // given
            FileAssetJpaEntity entity = createEntity(status);

            // when
            FileAsset domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getStatus()).isEqualTo(status);
        }

        @ParameterizedTest
        @EnumSource(FileCategory.class)
        @DisplayName("모든 FileCategory를 복원할 수 있다")
        void toDomain_WithAllCategories_ShouldRestore(FileCategory category) {
            // given
            FileAssetJpaEntity entity = createEntityWithCategory(category);

            // when
            FileAsset domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getCategory()).isEqualTo(category);
        }

        @Test
        @DisplayName("userId가 null인 Entity도 변환할 수 있다")
        void toDomain_WithNullUserId_ShouldRestore() {
            // given
            FileAssetJpaEntity entity = createEntityWithNullUserId();

            // when
            FileAsset domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getUserId()).isNull();
        }

        @Test
        @DisplayName("processedAt이 설정된 Entity도 변환할 수 있다")
        void toDomain_WithProcessedAt_ShouldRestore() {
            // given
            FileAssetJpaEntity entity = createCompletedEntity();

            // when
            FileAsset domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getStatus()).isEqualTo(FileAssetStatus.COMPLETED);
            assertThat(domain.getProcessedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("양방향 변환 테스트")
    class RoundTripTest {

        @Test
        @DisplayName("Domain → Entity → Domain 변환 시 데이터가 보존된다")
        void roundTrip_ShouldPreserveData() {
            // given
            FileAsset original = createDomain(FileAssetStatus.PENDING);

            // when
            FileAssetJpaEntity entity = mapper.toEntity(original);
            FileAsset restored = mapper.toDomain(entity);

            // then
            assertThat(restored.getIdValue()).isEqualTo(original.getIdValue());
            assertThat(restored.getSessionIdValue()).isEqualTo(original.getSessionIdValue());
            assertThat(restored.getFileNameValue()).isEqualTo(original.getFileNameValue());
            assertThat(restored.getFileSizeValue()).isEqualTo(original.getFileSizeValue());
            assertThat(restored.getContentTypeValue()).isEqualTo(original.getContentTypeValue());
            assertThat(restored.getCategory()).isEqualTo(original.getCategory());
            assertThat(restored.getBucketValue()).isEqualTo(original.getBucketValue());
            assertThat(restored.getS3KeyValue()).isEqualTo(original.getS3KeyValue());
            assertThat(restored.getEtagValue()).isEqualTo(original.getEtagValue());
            assertThat(restored.getUserId()).isEqualTo(original.getUserId());
            assertThat(restored.getOrganizationId().value())
                    .isEqualTo(original.getOrganizationId().value());
            assertThat(restored.getTenantId().value()).isEqualTo(original.getTenantId().value());
            assertThat(restored.getStatus()).isEqualTo(original.getStatus());
        }

        @Test
        @DisplayName("Entity → Domain → Entity 변환 시 데이터가 보존된다")
        void reverseRoundTrip_ShouldPreserveData() {
            // given
            FileAssetJpaEntity original = createEntity(FileAssetStatus.PENDING);

            // when
            FileAsset domain = mapper.toDomain(original);
            FileAssetJpaEntity restored = mapper.toEntity(domain);

            // then
            assertThat(restored.getId()).isEqualTo(original.getId());
            assertThat(restored.getSessionId()).isEqualTo(original.getSessionId());
            assertThat(restored.getFileName()).isEqualTo(original.getFileName());
            assertThat(restored.getFileSize()).isEqualTo(original.getFileSize());
            assertThat(restored.getContentType()).isEqualTo(original.getContentType());
            assertThat(restored.getCategory()).isEqualTo(original.getCategory());
            assertThat(restored.getBucket()).isEqualTo(original.getBucket());
            assertThat(restored.getS3Key()).isEqualTo(original.getS3Key());
            assertThat(restored.getEtag()).isEqualTo(original.getEtag());
            assertThat(restored.getUserId()).isEqualTo(original.getUserId());
            assertThat(restored.getOrganizationId()).isEqualTo(original.getOrganizationId());
            assertThat(restored.getTenantId()).isEqualTo(original.getTenantId());
            assertThat(restored.getStatus()).isEqualTo(original.getStatus());
        }
    }

    @Nested
    @DisplayName("다양한 파일 타입 테스트")
    class FileTypeTest {

        @Test
        @DisplayName("이미지 파일을 변환할 수 있다")
        void toEntity_WithImageFile_ShouldConvert() {
            // given
            FileAsset domain =
                    createDomainWithFileInfo("photo.jpg", "image/jpeg", FileCategory.IMAGE);

            // when
            FileAssetJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getFileName()).isEqualTo("photo.jpg");
            assertThat(entity.getContentType()).isEqualTo("image/jpeg");
            assertThat(entity.getCategory()).isEqualTo(FileCategory.IMAGE);
        }

        @Test
        @DisplayName("동영상 파일을 변환할 수 있다")
        void toEntity_WithVideoFile_ShouldConvert() {
            // given
            FileAsset domain =
                    createDomainWithFileInfo("video.mp4", "video/mp4", FileCategory.VIDEO);

            // when
            FileAssetJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getFileName()).isEqualTo("video.mp4");
            assertThat(entity.getContentType()).isEqualTo("video/mp4");
            assertThat(entity.getCategory()).isEqualTo(FileCategory.VIDEO);
        }

        @Test
        @DisplayName("문서 파일을 변환할 수 있다")
        void toEntity_WithDocumentFile_ShouldConvert() {
            // given
            FileAsset domain =
                    createDomainWithFileInfo(
                            "report.pdf", "application/pdf", FileCategory.DOCUMENT);

            // when
            FileAssetJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getFileName()).isEqualTo("report.pdf");
            assertThat(entity.getContentType()).isEqualTo("application/pdf");
            assertThat(entity.getCategory()).isEqualTo(FileCategory.DOCUMENT);
        }
    }

    // ==================== Helper Methods ====================

    private FileAsset createDomain(FileAssetStatus status) {
        Instant now = Instant.now();
        Instant processedAt =
                (status == FileAssetStatus.COMPLETED || status == FileAssetStatus.FAILED)
                        ? now
                        : null;
        Instant deletedAt = (status == FileAssetStatus.DELETED) ? now : null;

        return FileAsset.reconstitute(
                FileAssetId.of(UUID.randomUUID().toString()),
                UploadSessionId.of(UUID.randomUUID()),
                FileName.of("document.pdf"),
                FileSize.of(1024 * 1024L),
                ContentType.of("application/pdf"),
                FileCategory.DOCUMENT,
                null, // ImageDimension
                S3Bucket.of("test-bucket"),
                S3Key.of("uploads/document.pdf"),
                ETag.of("\"abc123\""),
                UserId.of(TEST_USER_ID),
                OrganizationId.of(TEST_ORG_ID),
                TenantId.of(TEST_TENANT_ID),
                status,
                now,
                processedAt,
                deletedAt);
    }

    private FileAsset createDomainWithCategory(FileCategory category) {
        Instant now = Instant.now();

        return FileAsset.reconstitute(
                FileAssetId.of(UUID.randomUUID().toString()),
                UploadSessionId.of(UUID.randomUUID()),
                FileName.of("file.txt"),
                FileSize.of(1024L),
                ContentType.of("text/plain"),
                category,
                null, // ImageDimension
                S3Bucket.of("test-bucket"),
                S3Key.of("uploads/file.txt"),
                ETag.of("\"abc123\""),
                UserId.of(TEST_USER_ID),
                OrganizationId.of(TEST_ORG_ID),
                TenantId.of(TEST_TENANT_ID),
                FileAssetStatus.PENDING,
                now,
                null,
                null);
    }

    private FileAsset createDomainWithNullUserId() {
        Instant now = Instant.now();

        return FileAsset.reconstitute(
                FileAssetId.of(UUID.randomUUID().toString()),
                UploadSessionId.of(UUID.randomUUID()),
                FileName.of("document.pdf"),
                FileSize.of(1024 * 1024L),
                ContentType.of("application/pdf"),
                FileCategory.DOCUMENT,
                null, // ImageDimension
                S3Bucket.of("test-bucket"),
                S3Key.of("uploads/document.pdf"),
                ETag.of("\"abc123\""),
                null,
                OrganizationId.of(TEST_ORG_ID),
                TenantId.of(TEST_TENANT_ID),
                FileAssetStatus.PENDING,
                now,
                null,
                null);
    }

    private FileAsset createCompletedDomain() {
        Instant now = Instant.now();
        Instant createdAt = now.minus(java.time.Duration.ofMinutes(10));

        return FileAsset.reconstitute(
                FileAssetId.of(UUID.randomUUID().toString()),
                UploadSessionId.of(UUID.randomUUID()),
                FileName.of("document.pdf"),
                FileSize.of(1024 * 1024L),
                ContentType.of("application/pdf"),
                FileCategory.DOCUMENT,
                null, // ImageDimension
                S3Bucket.of("test-bucket"),
                S3Key.of("uploads/document.pdf"),
                ETag.of("\"abc123\""),
                UserId.of(TEST_USER_ID),
                OrganizationId.of(TEST_ORG_ID),
                TenantId.of(TEST_TENANT_ID),
                FileAssetStatus.COMPLETED,
                createdAt,
                now,
                null);
    }

    private FileAsset createDomainWithFileInfo(
            String fileName, String contentType, FileCategory category) {
        Instant now = Instant.now();

        return FileAsset.reconstitute(
                FileAssetId.of(UUID.randomUUID().toString()),
                UploadSessionId.of(UUID.randomUUID()),
                FileName.of(fileName),
                FileSize.of(1024 * 1024L),
                ContentType.of(contentType),
                category,
                null, // ImageDimension
                S3Bucket.of("test-bucket"),
                S3Key.of("uploads/" + fileName),
                ETag.of("\"abc123\""),
                UserId.of(TEST_USER_ID),
                OrganizationId.of(TEST_ORG_ID),
                TenantId.of(TEST_TENANT_ID),
                FileAssetStatus.PENDING,
                now,
                null,
                null);
    }

    private FileAssetJpaEntity createEntity(FileAssetStatus status) {
        Instant now = Instant.now();
        Instant processedAt =
                (status == FileAssetStatus.COMPLETED || status == FileAssetStatus.FAILED)
                        ? now
                        : null;
        Instant deletedAt = (status == FileAssetStatus.DELETED) ? now : null;

        return FileAssetJpaEntity.of(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "document.pdf",
                1024 * 1024L,
                "application/pdf",
                FileCategory.DOCUMENT,
                null, // imageWidth
                null, // imageHeight
                "test-bucket",
                "uploads/document.pdf",
                "\"abc123\"",
                TEST_USER_ID,
                TEST_ORG_ID,
                TEST_TENANT_ID,
                status,
                processedAt,
                deletedAt,
                now,
                now);
    }

    private FileAssetJpaEntity createEntityWithCategory(FileCategory category) {
        Instant now = Instant.now();

        return FileAssetJpaEntity.of(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "file.txt",
                1024L,
                "text/plain",
                category,
                null, // imageWidth
                null, // imageHeight
                "test-bucket",
                "uploads/file.txt",
                "\"abc123\"",
                TEST_USER_ID,
                TEST_ORG_ID,
                TEST_TENANT_ID,
                FileAssetStatus.PENDING,
                null,
                null,
                now,
                now);
    }

    private FileAssetJpaEntity createEntityWithNullUserId() {
        Instant now = Instant.now();

        return FileAssetJpaEntity.of(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "document.pdf",
                1024 * 1024L,
                "application/pdf",
                FileCategory.DOCUMENT,
                null, // imageWidth
                null, // imageHeight
                "test-bucket",
                "uploads/document.pdf",
                "\"abc123\"",
                null,
                TEST_ORG_ID,
                TEST_TENANT_ID,
                FileAssetStatus.PENDING,
                null,
                null,
                now,
                now);
    }

    private FileAssetJpaEntity createCompletedEntity() {
        Instant now = Instant.now();
        Instant createdAt = now.minus(java.time.Duration.ofMinutes(10));

        return FileAssetJpaEntity.of(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "document.pdf",
                1024 * 1024L,
                "application/pdf",
                FileCategory.DOCUMENT,
                null, // imageWidth
                null, // imageHeight
                "test-bucket",
                "uploads/document.pdf",
                "\"abc123\"",
                TEST_USER_ID,
                TEST_ORG_ID,
                TEST_TENANT_ID,
                FileAssetStatus.COMPLETED,
                now,
                null,
                createdAt,
                now);
    }
}
