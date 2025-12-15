package com.ryuqq.fileflow.adapter.out.persistence.asset.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.ProcessedFileAssetJpaEntity;
import com.ryuqq.fileflow.domain.asset.aggregate.ProcessedFileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.ImageFormat;
import com.ryuqq.fileflow.domain.asset.vo.ImageFormatType;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariant;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariantType;
import com.ryuqq.fileflow.domain.asset.vo.ProcessedFileAssetId;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.iam.vo.UserId;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ProcessedFileAssetJpaMapper 단위 테스트")
class ProcessedFileAssetJpaMapperTest {

    private ProcessedFileAssetJpaMapper mapper;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        mapper = new ProcessedFileAssetJpaMapper();
        fixedClock = Clock.fixed(Instant.parse("2025-12-15T10:00:00Z"), ZoneId.of("UTC"));
    }

    @Nested
    @DisplayName("toEntity 메서드")
    class ToEntityTest {

        @Test
        @DisplayName("Domain을 Entity로 변환할 수 있다")
        void shouldConvertDomainToEntity() {
            // given
            ProcessedFileAsset domain = createDomain();

            // when
            ProcessedFileAssetJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getId()).isEqualTo(domain.getId().value());
            assertThat(entity.getOriginalAssetId()).isEqualTo(domain.getOriginalAssetId().value().toString());
            assertThat(entity.getParentAssetId()).isNull();
            assertThat(entity.getVariantType()).isEqualTo(domain.getVariant().type());
            assertThat(entity.getFormatType()).isEqualTo(domain.getFormat().type());
            assertThat(entity.getFileName()).isEqualTo(domain.getFileName().name());
            assertThat(entity.getFileSize()).isEqualTo(domain.getFileSize().size());
            assertThat(entity.getBucket()).isEqualTo(domain.getBucket().bucketName());
            assertThat(entity.getS3Key()).isEqualTo(domain.getS3Key().key());
            assertThat(entity.getUserId()).isEqualTo(domain.getUserId().value());
            assertThat(entity.getOrganizationId()).isEqualTo(domain.getOrganizationId().value());
            assertThat(entity.getTenantId()).isEqualTo(domain.getTenantId().value());
            assertThat(entity.getCreatedAt()).isEqualTo(domain.getCreatedAt());
        }

        @Test
        @DisplayName("parentAssetId가 있는 Domain을 Entity로 변환할 수 있다")
        void shouldConvertDomainWithParentToEntity() {
            // given
            ProcessedFileAsset domain = createDomainWithParent();

            // when
            ProcessedFileAssetJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getParentAssetId()).isNotNull();
            assertThat(entity.getParentAssetId()).isEqualTo(domain.getParentAssetId().value().toString());
        }

        @Test
        @DisplayName("userId가 null인 경우도 처리된다")
        void shouldHandleNullUserId() {
            // given
            ProcessedFileAsset domain = createDomainWithNullUserId();

            // when
            ProcessedFileAssetJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getUserId()).isNull();
        }

        @Test
        @DisplayName("모든 ImageVariant 타입을 변환할 수 있다")
        void shouldConvertAllVariantTypes() {
            // given
            ProcessedFileAsset largeDomain = createDomainWithVariant(ImageVariant.LARGE);
            ProcessedFileAsset mediumDomain = createDomainWithVariant(ImageVariant.MEDIUM);
            ProcessedFileAsset thumbnailDomain = createDomainWithVariant(ImageVariant.THUMBNAIL);

            // when
            ProcessedFileAssetJpaEntity largeEntity = mapper.toEntity(largeDomain);
            ProcessedFileAssetJpaEntity mediumEntity = mapper.toEntity(mediumDomain);
            ProcessedFileAssetJpaEntity thumbnailEntity = mapper.toEntity(thumbnailDomain);

            // then
            assertThat(largeEntity.getVariantType()).isEqualTo(ImageVariantType.LARGE);
            assertThat(mediumEntity.getVariantType()).isEqualTo(ImageVariantType.MEDIUM);
            assertThat(thumbnailEntity.getVariantType()).isEqualTo(ImageVariantType.THUMBNAIL);
        }

        @Test
        @DisplayName("모든 ImageFormat 타입을 변환할 수 있다")
        void shouldConvertAllFormatTypes() {
            // given
            ProcessedFileAsset jpegDomain = createDomainWithFormat(ImageFormat.JPEG);
            ProcessedFileAsset pngDomain = createDomainWithFormat(ImageFormat.PNG);

            // when
            ProcessedFileAssetJpaEntity jpegEntity = mapper.toEntity(jpegDomain);
            ProcessedFileAssetJpaEntity pngEntity = mapper.toEntity(pngDomain);

            // then
            assertThat(jpegEntity.getFormatType()).isEqualTo(ImageFormatType.JPEG);
            assertThat(pngEntity.getFormatType()).isEqualTo(ImageFormatType.PNG);
        }
    }

    @Nested
    @DisplayName("toDomain 메서드")
    class ToDomainTest {

        @Test
        @DisplayName("Entity를 Domain으로 변환할 수 있다")
        void shouldConvertEntityToDomain() {
            // given
            ProcessedFileAssetJpaEntity entity = createEntity();

            // when
            ProcessedFileAsset domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getId().value()).isEqualTo(entity.getId());
            assertThat(domain.getOriginalAssetId().value().toString()).isEqualTo(entity.getOriginalAssetId());
            assertThat(domain.getParentAssetId()).isNull();
            assertThat(domain.getVariant().type()).isEqualTo(entity.getVariantType());
            assertThat(domain.getFormat().type()).isEqualTo(entity.getFormatType());
            assertThat(domain.getFileName().name()).isEqualTo(entity.getFileName());
            assertThat(domain.getFileSize().size()).isEqualTo(entity.getFileSize());
            assertThat(domain.getBucket().bucketName()).isEqualTo(entity.getBucket());
            assertThat(domain.getS3Key().key()).isEqualTo(entity.getS3Key());
            assertThat(domain.getUserId().value()).isEqualTo(entity.getUserId());
            assertThat(domain.getOrganizationId().value()).isEqualTo(entity.getOrganizationId());
            assertThat(domain.getTenantId().value()).isEqualTo(entity.getTenantId());
            assertThat(domain.getCreatedAt()).isEqualTo(entity.getCreatedAt());
        }

        @Test
        @DisplayName("Entity의 parentAssetId가 있는 경우도 처리된다")
        void shouldHandleParentAssetId() {
            // given
            ProcessedFileAssetJpaEntity entity = createEntityWithParent();

            // when
            ProcessedFileAsset domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getParentAssetId()).isNotNull();
            assertThat(domain.hasParentAsset()).isTrue();
        }

        @Test
        @DisplayName("Entity의 userId가 null인 경우도 처리된다")
        void shouldHandleNullUserId() {
            // given
            ProcessedFileAssetJpaEntity entity = createEntityWithNullUserId();

            // when
            ProcessedFileAsset domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getUserId()).isNull();
        }

        @Test
        @DisplayName("모든 ImageVariantType을 Domain으로 변환할 수 있다")
        void shouldConvertAllVariantTypesToDomain() {
            // given
            ProcessedFileAssetJpaEntity originalEntity = createEntityWithVariantType(ImageVariantType.ORIGINAL);
            ProcessedFileAssetJpaEntity largeEntity = createEntityWithVariantType(ImageVariantType.LARGE);
            ProcessedFileAssetJpaEntity mediumEntity = createEntityWithVariantType(ImageVariantType.MEDIUM);
            ProcessedFileAssetJpaEntity thumbnailEntity = createEntityWithVariantType(ImageVariantType.THUMBNAIL);

            // when
            ProcessedFileAsset originalDomain = mapper.toDomain(originalEntity);
            ProcessedFileAsset largeDomain = mapper.toDomain(largeEntity);
            ProcessedFileAsset mediumDomain = mapper.toDomain(mediumEntity);
            ProcessedFileAsset thumbnailDomain = mapper.toDomain(thumbnailEntity);

            // then
            assertThat(originalDomain.getVariant()).isEqualTo(ImageVariant.ORIGINAL);
            assertThat(largeDomain.getVariant()).isEqualTo(ImageVariant.LARGE);
            assertThat(mediumDomain.getVariant()).isEqualTo(ImageVariant.MEDIUM);
            assertThat(thumbnailDomain.getVariant()).isEqualTo(ImageVariant.THUMBNAIL);
        }

        @Test
        @DisplayName("모든 ImageFormatType을 Domain으로 변환할 수 있다")
        void shouldConvertAllFormatTypesToDomain() {
            // given
            ProcessedFileAssetJpaEntity webpEntity = createEntityWithFormatType(ImageFormatType.WEBP);
            ProcessedFileAssetJpaEntity jpegEntity = createEntityWithFormatType(ImageFormatType.JPEG);
            ProcessedFileAssetJpaEntity pngEntity = createEntityWithFormatType(ImageFormatType.PNG);

            // when
            ProcessedFileAsset webpDomain = mapper.toDomain(webpEntity);
            ProcessedFileAsset jpegDomain = mapper.toDomain(jpegEntity);
            ProcessedFileAsset pngDomain = mapper.toDomain(pngEntity);

            // then
            assertThat(webpDomain.getFormat()).isEqualTo(ImageFormat.WEBP);
            assertThat(jpegDomain.getFormat()).isEqualTo(ImageFormat.JPEG);
            assertThat(pngDomain.getFormat()).isEqualTo(ImageFormat.PNG);
        }
    }

    @Nested
    @DisplayName("양방향 변환 테스트")
    class RoundTripTest {

        @Test
        @DisplayName("Domain → Entity → Domain 변환 시 데이터가 보존된다")
        void shouldPreserveDataInRoundTrip() {
            // given
            ProcessedFileAsset original = createDomain();

            // when
            ProcessedFileAssetJpaEntity entity = mapper.toEntity(original);
            ProcessedFileAsset restored = mapper.toDomain(entity);

            // then
            assertThat(restored.getId().value()).isEqualTo(original.getId().value());
            assertThat(restored.getOriginalAssetId().value()).isEqualTo(original.getOriginalAssetId().value());
            assertThat(restored.getParentAssetId()).isEqualTo(original.getParentAssetId());
            assertThat(restored.getVariant()).isEqualTo(original.getVariant());
            assertThat(restored.getFormat()).isEqualTo(original.getFormat());
            assertThat(restored.getFileName().name()).isEqualTo(original.getFileName().name());
            assertThat(restored.getFileSize().size()).isEqualTo(original.getFileSize().size());
            assertThat(restored.getBucket().bucketName()).isEqualTo(original.getBucket().bucketName());
            assertThat(restored.getS3Key().key()).isEqualTo(original.getS3Key().key());
            assertThat(restored.getUserId().value()).isEqualTo(original.getUserId().value());
            assertThat(restored.getOrganizationId().value()).isEqualTo(original.getOrganizationId().value());
            assertThat(restored.getTenantId().value()).isEqualTo(original.getTenantId().value());
            assertThat(restored.getCreatedAt()).isEqualTo(original.getCreatedAt());
        }

        @Test
        @DisplayName("parentAssetId가 있는 경우 양방향 변환이 보존된다")
        void shouldPreserveParentAssetIdInRoundTrip() {
            // given
            ProcessedFileAsset original = createDomainWithParent();

            // when
            ProcessedFileAssetJpaEntity entity = mapper.toEntity(original);
            ProcessedFileAsset restored = mapper.toDomain(entity);

            // then
            assertThat(restored.hasParentAsset()).isTrue();
            assertThat(restored.getParentAssetId().value()).isEqualTo(original.getParentAssetId().value());
        }

        @Test
        @DisplayName("THUMBNAIL + PNG 조합이 양방향 변환 시 보존된다")
        void shouldPreserveThumbnailPngInRoundTrip() {
            // given
            ProcessedFileAsset original = createDomainWithVariantAndFormat(ImageVariant.THUMBNAIL, ImageFormat.PNG);

            // when
            ProcessedFileAssetJpaEntity entity = mapper.toEntity(original);
            ProcessedFileAsset restored = mapper.toDomain(entity);

            // then
            assertThat(restored.getVariant()).isEqualTo(ImageVariant.THUMBNAIL);
            assertThat(restored.getFormat()).isEqualTo(ImageFormat.PNG);
        }
    }

    // ==================== Helper Methods ====================

    private ProcessedFileAsset createDomain() {
        return ProcessedFileAsset.reconstitute(
                new ProcessedFileAssetId(UUID.randomUUID()),
                new FileAssetId(UUID.randomUUID()),
                null,
                ImageVariant.ORIGINAL,
                ImageFormat.WEBP,
                new FileName("test-image.webp"),
                new FileSize(1024L),
                new S3Bucket("test-bucket"),
                new S3Key("uploads/test/test-image.webp"),
                UserId.generate(),
                OrganizationId.generate(),
                TenantId.generate(),
                fixedClock.instant());
    }

    private ProcessedFileAsset createDomainWithParent() {
        return ProcessedFileAsset.reconstitute(
                new ProcessedFileAssetId(UUID.randomUUID()),
                new FileAssetId(UUID.randomUUID()),
                new FileAssetId(UUID.randomUUID()),
                ImageVariant.MEDIUM,
                ImageFormat.WEBP,
                new FileName("extracted-image.webp"),
                new FileSize(512L),
                new S3Bucket("test-bucket"),
                new S3Key("uploads/test/extracted-image.webp"),
                UserId.generate(),
                OrganizationId.generate(),
                TenantId.generate(),
                fixedClock.instant());
    }

    private ProcessedFileAsset createDomainWithNullUserId() {
        return ProcessedFileAsset.reconstitute(
                new ProcessedFileAssetId(UUID.randomUUID()),
                new FileAssetId(UUID.randomUUID()),
                null,
                ImageVariant.ORIGINAL,
                ImageFormat.WEBP,
                new FileName("test-image.webp"),
                new FileSize(1024L),
                new S3Bucket("test-bucket"),
                new S3Key("uploads/test/test-image.webp"),
                null,
                OrganizationId.generate(),
                TenantId.generate(),
                fixedClock.instant());
    }

    private ProcessedFileAsset createDomainWithVariant(ImageVariant variant) {
        return ProcessedFileAsset.reconstitute(
                new ProcessedFileAssetId(UUID.randomUUID()),
                new FileAssetId(UUID.randomUUID()),
                null,
                variant,
                ImageFormat.WEBP,
                new FileName("test-image.webp"),
                new FileSize(1024L),
                new S3Bucket("test-bucket"),
                new S3Key("uploads/test/test-image.webp"),
                UserId.generate(),
                OrganizationId.generate(),
                TenantId.generate(),
                fixedClock.instant());
    }

    private ProcessedFileAsset createDomainWithFormat(ImageFormat format) {
        return ProcessedFileAsset.reconstitute(
                new ProcessedFileAssetId(UUID.randomUUID()),
                new FileAssetId(UUID.randomUUID()),
                null,
                ImageVariant.ORIGINAL,
                format,
                new FileName("test-image." + format.extension()),
                new FileSize(1024L),
                new S3Bucket("test-bucket"),
                new S3Key("uploads/test/test-image." + format.extension()),
                UserId.generate(),
                OrganizationId.generate(),
                TenantId.generate(),
                fixedClock.instant());
    }

    private ProcessedFileAsset createDomainWithVariantAndFormat(ImageVariant variant, ImageFormat format) {
        return ProcessedFileAsset.reconstitute(
                new ProcessedFileAssetId(UUID.randomUUID()),
                new FileAssetId(UUID.randomUUID()),
                null,
                variant,
                format,
                new FileName("test-image." + format.extension()),
                new FileSize(1024L),
                new S3Bucket("test-bucket"),
                new S3Key("uploads/test/test-image." + format.extension()),
                UserId.generate(),
                OrganizationId.generate(),
                TenantId.generate(),
                fixedClock.instant());
    }

    private ProcessedFileAssetJpaEntity createEntity() {
        return ProcessedFileAssetJpaEntity.of(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                null,
                ImageVariantType.ORIGINAL,
                ImageFormatType.WEBP,
                "test-image.webp",
                1024L,
                "test-bucket",
                "uploads/test/test-image.webp",
                UserId.generate().value(),
                OrganizationId.generate().value(),
                TenantId.generate().value(),
                fixedClock.instant());
    }

    private ProcessedFileAssetJpaEntity createEntityWithParent() {
        return ProcessedFileAssetJpaEntity.of(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                ImageVariantType.MEDIUM,
                ImageFormatType.WEBP,
                "extracted-image.webp",
                512L,
                "test-bucket",
                "uploads/test/extracted-image.webp",
                UserId.generate().value(),
                OrganizationId.generate().value(),
                TenantId.generate().value(),
                fixedClock.instant());
    }

    private ProcessedFileAssetJpaEntity createEntityWithNullUserId() {
        return ProcessedFileAssetJpaEntity.of(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                null,
                ImageVariantType.ORIGINAL,
                ImageFormatType.WEBP,
                "test-image.webp",
                1024L,
                "test-bucket",
                "uploads/test/test-image.webp",
                null,
                OrganizationId.generate().value(),
                TenantId.generate().value(),
                fixedClock.instant());
    }

    private ProcessedFileAssetJpaEntity createEntityWithVariantType(ImageVariantType variantType) {
        return ProcessedFileAssetJpaEntity.of(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                null,
                variantType,
                ImageFormatType.WEBP,
                "test-image.webp",
                1024L,
                "test-bucket",
                "uploads/test/test-image.webp",
                UserId.generate().value(),
                OrganizationId.generate().value(),
                TenantId.generate().value(),
                fixedClock.instant());
    }

    private ProcessedFileAssetJpaEntity createEntityWithFormatType(ImageFormatType formatType) {
        return ProcessedFileAssetJpaEntity.of(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                null,
                ImageVariantType.ORIGINAL,
                formatType,
                "test-image.webp",
                1024L,
                "test-bucket",
                "uploads/test/test-image.webp",
                UserId.generate().value(),
                OrganizationId.generate().value(),
                TenantId.generate().value(),
                fixedClock.instant());
    }
}
