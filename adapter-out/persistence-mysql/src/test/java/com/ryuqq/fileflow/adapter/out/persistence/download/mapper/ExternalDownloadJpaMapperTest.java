package com.ryuqq.fileflow.adapter.out.persistence.download.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.ExternalDownloadJpaEntity;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadStatus;
import com.ryuqq.fileflow.domain.download.vo.RetryCount;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import com.ryuqq.fileflow.domain.download.vo.WebhookUrl;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ExternalDownloadJpaMapper 단위 테스트")
class ExternalDownloadJpaMapperTest {

    private ExternalDownloadJpaMapper mapper;
    private static final ZoneId ZONE_ID = ZoneId.of("UTC");

    @BeforeEach
    void setUp() {
        mapper = new ExternalDownloadJpaMapper();
    }

    @Nested
    @DisplayName("toEntity 메서드")
    class ToEntityTest {

        @Test
        @DisplayName("Domain을 Entity로 변환할 수 있다")
        void shouldConvertDomainToEntity() {
            // given
            UUID id = UUID.randomUUID();
            ExternalDownload domain = createDomainWithId(id);

            // when
            ExternalDownloadJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getSourceUrl()).isEqualTo(domain.getSourceUrl().value());
            assertThat(entity.getTenantId()).isEqualTo(domain.getTenantId());
            assertThat(entity.getOrganizationId()).isEqualTo(domain.getOrganizationId());
            assertThat(entity.getS3Bucket()).isEqualTo(domain.getS3Bucket().bucketName());
            assertThat(entity.getS3PathPrefix()).isEqualTo(domain.getS3PathPrefix());
            assertThat(entity.getStatus()).isEqualTo(domain.getStatus());
            assertThat(entity.getRetryCount()).isEqualTo(domain.getRetryCountValue());
        }

        @Test
        @DisplayName("신규 Domain도 UUID를 가지므로 Entity ID가 설정된다")
        void shouldHaveIdForNewDomain() {
            // given
            ExternalDownload domain = createNewDomain();

            // when
            ExternalDownloadJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getId()).isNotNull(); // UUID는 항상 값이 있음
        }

        @Test
        @DisplayName("WebhookUrl이 null인 경우도 처리된다")
        void shouldHandleNullWebhook() {
            // given
            ExternalDownload domain = createDomainWithoutWebhook();

            // when
            ExternalDownloadJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getWebhookUrl()).isNull();
        }

        @Test
        @DisplayName("FileAssetId가 null인 경우도 처리된다")
        void shouldHandleNullFileAssetId() {
            // given
            ExternalDownload domain = createDomainWithId(UUID.randomUUID());

            // when
            ExternalDownloadJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getFileAssetId()).isNull();
        }

        @Test
        @DisplayName("Instant를 LocalDateTime(UTC)으로 변환한다")
        void shouldConvertInstantToLocalDateTime() {
            // given
            Instant instant = Instant.parse("2025-11-26T12:00:00Z");
            ExternalDownload domain =
                    ExternalDownload.of(
                            ExternalDownloadId.of(UUID.randomUUID()),
                            SourceUrl.of("https://example.com/file.jpg"),
                            100L,
                            200L,
                            S3Bucket.of("test-bucket"),
                            "downloads/",
                            ExternalDownloadStatus.PENDING,
                            RetryCount.initial(),
                            null,
                            null,
                            null,
                            instant,
                            instant);

            // when
            ExternalDownloadJpaEntity entity = mapper.toEntity(domain);

            // then
            LocalDateTime expected = LocalDateTime.ofInstant(instant, ZONE_ID);
            assertThat(entity.getCreatedAt()).isEqualTo(expected);
            assertThat(entity.getUpdatedAt()).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("toDomain 메서드")
    class ToDomainTest {

        @Test
        @DisplayName("Entity를 Domain으로 변환할 수 있다")
        void shouldConvertEntityToDomain() {
            // given
            ExternalDownloadJpaEntity entity = createEntity(UUID.randomUUID());

            // when
            ExternalDownload domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getId().value()).isEqualTo(entity.getId());
            assertThat(domain.getSourceUrl().value()).isEqualTo(entity.getSourceUrl());
            assertThat(domain.getTenantId()).isEqualTo(entity.getTenantId());
            assertThat(domain.getOrganizationId()).isEqualTo(entity.getOrganizationId());
            assertThat(domain.getS3Bucket().bucketName()).isEqualTo(entity.getS3Bucket());
            assertThat(domain.getS3PathPrefix()).isEqualTo(entity.getS3PathPrefix());
            assertThat(domain.getStatus()).isEqualTo(entity.getStatus());
        }

        @Test
        @DisplayName("FileAssetId가 null인 경우도 처리된다")
        void shouldHandleNullFileAssetId() {
            // given
            ExternalDownloadJpaEntity entity = createEntityWithoutFileAsset();

            // when
            ExternalDownload domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getFileAssetId()).isNull();
        }

        @Test
        @DisplayName("WebhookUrl이 null인 경우도 처리된다")
        void shouldHandleNullWebhookUrl() {
            // given
            ExternalDownloadJpaEntity entity = createEntityWithoutWebhook();

            // when
            ExternalDownload domain = mapper.toDomain(entity);

            // then
            assertThat(domain.hasWebhook()).isFalse();
        }

        @Test
        @DisplayName("LocalDateTime을 Instant(UTC)로 변환한다")
        void shouldConvertLocalDateTimeToInstant() {
            // given
            LocalDateTime localDateTime = LocalDateTime.of(2025, 11, 26, 12, 0, 0);
            ExternalDownloadJpaEntity entity =
                    ExternalDownloadJpaEntity.of(
                            UUID.randomUUID(),
                            "https://example.com/file.jpg",
                            100L,
                            200L,
                            "test-bucket",
                            "downloads/",
                            ExternalDownloadStatus.PENDING,
                            0,
                            null,
                            null,
                            null,
                            0L,
                            localDateTime,
                            localDateTime);

            // when
            ExternalDownload domain = mapper.toDomain(entity);

            // then
            Instant expected = localDateTime.atZone(ZONE_ID).toInstant();
            assertThat(domain.getCreatedAt()).isEqualTo(expected);
            assertThat(domain.getUpdatedAt()).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("양방향 변환 테스트")
    class RoundTripTest {

        @Test
        @DisplayName("Domain → Entity → Domain 변환 시 데이터가 보존된다")
        void shouldPreserveDataInRoundTrip() {
            // given
            ExternalDownload original = createDomainWithId(UUID.randomUUID());

            // when
            ExternalDownloadJpaEntity entity = mapper.toEntity(original);
            ExternalDownload restored = mapper.toDomain(entity);

            // then
            assertThat(restored.getId().value()).isEqualTo(original.getId().value());
            assertThat(restored.getSourceUrl().value()).isEqualTo(original.getSourceUrl().value());
            assertThat(restored.getTenantId()).isEqualTo(original.getTenantId());
            assertThat(restored.getOrganizationId()).isEqualTo(original.getOrganizationId());
            assertThat(restored.getStatus()).isEqualTo(original.getStatus());
        }

        @Test
        @DisplayName("FileAssetId가 있는 경우 양방향 변환이 보존된다")
        void shouldPreserveFileAssetIdInRoundTrip() {
            // given
            FileAssetId fileAssetId = FileAssetId.of("550e8400-e29b-41d4-a716-446655440000");
            ExternalDownload original =
                    ExternalDownload.of(
                            ExternalDownloadId.of(UUID.randomUUID()),
                            SourceUrl.of("https://example.com/file.jpg"),
                            100L,
                            200L,
                            S3Bucket.of("test-bucket"),
                            "downloads/",
                            ExternalDownloadStatus.PROCESSING,
                            RetryCount.initial(),
                            fileAssetId,
                            null,
                            null,
                            Instant.now(),
                            Instant.now());

            // when
            ExternalDownloadJpaEntity entity = mapper.toEntity(original);
            ExternalDownload restored = mapper.toDomain(entity);

            // then
            assertThat(restored.getFileAssetId()).isNotNull();
            assertThat(restored.getFileAssetId().getValue())
                    .isEqualTo(original.getFileAssetId().getValue());
        }

        @Test
        @DisplayName("WebhookUrl이 있는 경우 양방향 변환이 보존된다")
        void shouldPreserveWebhookUrlInRoundTrip() {
            // given
            ExternalDownload original =
                    ExternalDownload.of(
                            ExternalDownloadId.of(UUID.randomUUID()),
                            SourceUrl.of("https://example.com/file.jpg"),
                            100L,
                            200L,
                            S3Bucket.of("test-bucket"),
                            "downloads/",
                            ExternalDownloadStatus.PENDING,
                            RetryCount.initial(),
                            null,
                            null,
                            WebhookUrl.of("https://webhook.example.com"),
                            Instant.now(),
                            Instant.now());

            // when
            ExternalDownloadJpaEntity entity = mapper.toEntity(original);
            ExternalDownload restored = mapper.toDomain(entity);

            // then
            assertThat(restored.hasWebhook()).isTrue();
            assertThat(restored.getWebhookUrl().value())
                    .isEqualTo(original.getWebhookUrl().value());
        }
    }

    // ==================== Helper Methods ====================

    private ExternalDownload createDomainWithId(UUID id) {
        return ExternalDownload.of(
                ExternalDownloadId.of(id),
                SourceUrl.of("https://example.com/file.jpg"),
                100L,
                200L,
                S3Bucket.of("test-bucket"),
                "downloads/",
                ExternalDownloadStatus.PENDING,
                RetryCount.initial(),
                null,
                null,
                WebhookUrl.of("https://webhook.example.com"),
                Instant.now(),
                Instant.now());
    }

    private ExternalDownload createNewDomain() {
        return ExternalDownload.of(
                ExternalDownloadId.forNew(),
                SourceUrl.of("https://example.com/file.jpg"),
                100L,
                200L,
                S3Bucket.of("test-bucket"),
                "downloads/",
                ExternalDownloadStatus.PENDING,
                RetryCount.initial(),
                null,
                null,
                null,
                Instant.now(),
                Instant.now());
    }

    private ExternalDownload createDomainWithoutWebhook() {
        return ExternalDownload.of(
                ExternalDownloadId.of(UUID.randomUUID()),
                SourceUrl.of("https://example.com/file.jpg"),
                100L,
                200L,
                S3Bucket.of("test-bucket"),
                "downloads/",
                ExternalDownloadStatus.PENDING,
                RetryCount.initial(),
                null,
                null,
                null,
                Instant.now(),
                Instant.now());
    }

    private ExternalDownloadJpaEntity createEntity(UUID id) {
        LocalDateTime now = LocalDateTime.now();
        return ExternalDownloadJpaEntity.of(
                id,
                "https://example.com/file.jpg",
                100L,
                200L,
                "test-bucket",
                "downloads/",
                ExternalDownloadStatus.PENDING,
                0,
                null,
                null,
                "https://webhook.example.com",
                0L,
                now,
                now);
    }

    private ExternalDownloadJpaEntity createEntityWithoutFileAsset() {
        LocalDateTime now = LocalDateTime.now();
        return ExternalDownloadJpaEntity.of(
                UUID.randomUUID(),
                "https://example.com/file.jpg",
                100L,
                200L,
                "test-bucket",
                "downloads/",
                ExternalDownloadStatus.PENDING,
                0,
                null,
                null,
                null,
                0L,
                now,
                now);
    }

    private ExternalDownloadJpaEntity createEntityWithoutWebhook() {
        LocalDateTime now = LocalDateTime.now();
        return ExternalDownloadJpaEntity.of(
                UUID.randomUUID(),
                "https://example.com/file.jpg",
                100L,
                200L,
                "test-bucket",
                "downloads/",
                ExternalDownloadStatus.PENDING,
                0,
                null,
                null,
                null,
                0L,
                now,
                now);
    }
}
