package com.ryuqq.fileflow.adapter.out.persistence.download.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.ExternalDownloadOutboxJpaEntity;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadOutboxId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ExternalDownloadOutboxJpaMapper 단위 테스트")
class ExternalDownloadOutboxJpaMapperTest {

    private ExternalDownloadOutboxJpaMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ExternalDownloadOutboxJpaMapper();
    }

    @Nested
    @DisplayName("toEntity 메서드")
    class ToEntityTest {

        @Test
        @DisplayName("Domain을 Entity로 변환할 수 있다")
        void shouldConvertDomainToEntity() {
            // given
            UUID id = UUID.randomUUID();
            ExternalDownloadOutbox domain = createDomainWithId(id);

            // when
            ExternalDownloadOutboxJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getExternalDownloadId())
                    .isEqualTo(domain.getExternalDownloadId().value());
            assertThat(entity.getPublished()).isEqualTo(domain.isPublished());
        }

        @Test
        @DisplayName("신규 Domain도 UUID를 가지므로 Entity ID가 설정된다")
        void shouldHaveIdForNewDomain() {
            // given
            ExternalDownloadOutbox domain = createNewDomain();

            // when
            ExternalDownloadOutboxJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getId()).isNotNull(); // UUID는 항상 값이 있음
        }

        @Test
        @DisplayName("미발행 상태의 Domain을 Entity로 변환할 수 있다")
        void shouldConvertUnpublishedDomain() {
            // given
            UUID outboxId = UUID.randomUUID();
            UUID downloadId = UUID.randomUUID();
            ExternalDownloadOutbox domain =
                    ExternalDownloadOutbox.of(
                            ExternalDownloadOutboxId.of(outboxId),
                            ExternalDownloadId.of(downloadId),
                            false,
                            null,
                            Instant.now());

            // when
            ExternalDownloadOutboxJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getPublished()).isFalse();
            assertThat(entity.getPublishedAt()).isNull();
        }

        @Test
        @DisplayName("발행 완료 상태의 Domain을 Entity로 변환할 수 있다")
        void shouldConvertPublishedDomain() {
            // given
            UUID outboxId = UUID.randomUUID();
            UUID downloadId = UUID.randomUUID();
            Instant publishedAt = Instant.now();
            ExternalDownloadOutbox domain =
                    ExternalDownloadOutbox.of(
                            ExternalDownloadOutboxId.of(outboxId),
                            ExternalDownloadId.of(downloadId),
                            true,
                            publishedAt,
                            Instant.now());

            // when
            ExternalDownloadOutboxJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getPublished()).isTrue();
            assertThat(entity.getPublishedAt()).isNotNull();
        }

        @Test
        @DisplayName("Instant 시간 정보가 Entity에 전달된다")
        void shouldConvertInstantToEntity() {
            // given
            UUID outboxId = UUID.randomUUID();
            UUID downloadId = UUID.randomUUID();
            Instant createdAt = Instant.parse("2025-11-26T12:00:00Z");
            ExternalDownloadOutbox domain =
                    ExternalDownloadOutbox.of(
                            ExternalDownloadOutboxId.of(outboxId),
                            ExternalDownloadId.of(downloadId),
                            false,
                            null,
                            createdAt);

            // when
            ExternalDownloadOutboxJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        }
    }

    @Nested
    @DisplayName("toDomain 메서드")
    class ToDomainTest {

        @Test
        @DisplayName("Entity를 Domain으로 변환할 수 있다")
        void shouldConvertEntityToDomain() {
            // given
            ExternalDownloadOutboxJpaEntity entity = createEntity(UUID.randomUUID(), false);

            // when
            ExternalDownloadOutbox domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getId().value()).isEqualTo(entity.getId());
            assertThat(domain.getExternalDownloadId().value())
                    .isEqualTo(entity.getExternalDownloadId());
            assertThat(domain.isPublished()).isEqualTo(entity.getPublished());
        }

        @Test
        @DisplayName("미발행 상태의 Entity를 Domain으로 변환할 수 있다")
        void shouldConvertUnpublishedEntity() {
            // given
            ExternalDownloadOutboxJpaEntity entity = createEntity(UUID.randomUUID(), false);

            // when
            ExternalDownloadOutbox domain = mapper.toDomain(entity);

            // then
            assertThat(domain.isPublished()).isFalse();
            assertThat(domain.getPublishedAt()).isNull();
        }

        @Test
        @DisplayName("발행 완료 상태의 Entity를 Domain으로 변환할 수 있다")
        void shouldConvertPublishedEntity() {
            // given
            ExternalDownloadOutboxJpaEntity entity = createEntity(UUID.randomUUID(), true);

            // when
            ExternalDownloadOutbox domain = mapper.toDomain(entity);

            // then
            assertThat(domain.isPublished()).isTrue();
            assertThat(domain.getPublishedAt()).isNotNull();
        }

        @Test
        @DisplayName("Entity의 Instant가 Domain으로 전달된다")
        void shouldConvertInstantToDomain() {
            // given
            UUID outboxId = UUID.randomUUID();
            UUID downloadId = UUID.randomUUID();
            Instant createdAt = Instant.parse("2025-11-26T12:00:00Z");
            ExternalDownloadOutboxJpaEntity entity =
                    ExternalDownloadOutboxJpaEntity.of(
                            outboxId, downloadId, false, null, createdAt, createdAt);

            // when
            ExternalDownloadOutbox domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getCreatedAt()).isEqualTo(createdAt);
        }
    }

    @Nested
    @DisplayName("양방향 변환 테스트")
    class RoundTripTest {

        @Test
        @DisplayName("Domain → Entity → Domain 변환 시 데이터가 보존된다")
        void shouldPreserveDataInRoundTrip() {
            // given
            ExternalDownloadOutbox original = createDomainWithId(UUID.randomUUID());

            // when
            ExternalDownloadOutboxJpaEntity entity = mapper.toEntity(original);
            ExternalDownloadOutbox restored = mapper.toDomain(entity);

            // then
            assertThat(restored.getId().value()).isEqualTo(original.getId().value());
            assertThat(restored.getExternalDownloadId().value())
                    .isEqualTo(original.getExternalDownloadId().value());
            assertThat(restored.isPublished()).isEqualTo(original.isPublished());
        }

        @Test
        @DisplayName("미발행 상태의 양방향 변환이 보존된다")
        void shouldPreserveUnpublishedStateInRoundTrip() {
            // given
            UUID outboxId = UUID.randomUUID();
            UUID downloadId = UUID.randomUUID();
            ExternalDownloadOutbox original =
                    ExternalDownloadOutbox.of(
                            ExternalDownloadOutboxId.of(outboxId),
                            ExternalDownloadId.of(downloadId),
                            false,
                            null,
                            Instant.now());

            // when
            ExternalDownloadOutboxJpaEntity entity = mapper.toEntity(original);
            ExternalDownloadOutbox restored = mapper.toDomain(entity);

            // then
            assertThat(restored.isPublished()).isFalse();
            assertThat(restored.getPublishedAt()).isNull();
        }

        @Test
        @DisplayName("발행 완료 상태의 양방향 변환이 보존된다")
        void shouldPreservePublishedStateInRoundTrip() {
            // given
            UUID outboxId = UUID.randomUUID();
            UUID downloadId = UUID.randomUUID();
            Instant publishedAt = Instant.now();
            ExternalDownloadOutbox original =
                    ExternalDownloadOutbox.of(
                            ExternalDownloadOutboxId.of(outboxId),
                            ExternalDownloadId.of(downloadId),
                            true,
                            publishedAt,
                            Instant.now());

            // when
            ExternalDownloadOutboxJpaEntity entity = mapper.toEntity(original);
            ExternalDownloadOutbox restored = mapper.toDomain(entity);

            // then
            assertThat(restored.isPublished()).isTrue();
            assertThat(restored.getPublishedAt()).isNotNull();
        }

        @Test
        @DisplayName("시간 정보가 양방향 변환에서 보존된다")
        void shouldPreserveTimeInRoundTrip() {
            // given
            UUID outboxId = UUID.randomUUID();
            UUID downloadId = UUID.randomUUID();
            Instant createdAt = Instant.parse("2025-11-26T12:00:00Z");
            ExternalDownloadOutbox original =
                    ExternalDownloadOutbox.of(
                            ExternalDownloadOutboxId.of(outboxId),
                            ExternalDownloadId.of(downloadId),
                            false,
                            null,
                            createdAt);

            // when
            ExternalDownloadOutboxJpaEntity entity = mapper.toEntity(original);
            ExternalDownloadOutbox restored = mapper.toDomain(entity);

            // then
            assertThat(restored.getCreatedAt()).isEqualTo(original.getCreatedAt());
        }
    }

    // ==================== Helper Methods ====================

    private ExternalDownloadOutbox createDomainWithId(UUID id) {
        UUID downloadId = UUID.randomUUID();
        return ExternalDownloadOutbox.of(
                ExternalDownloadOutboxId.of(id),
                ExternalDownloadId.of(downloadId),
                false,
                null,
                Instant.now());
    }

    private ExternalDownloadOutbox createNewDomain() {
        UUID downloadId = UUID.randomUUID();
        return ExternalDownloadOutbox.of(
                ExternalDownloadOutboxId.forNew(),
                ExternalDownloadId.of(downloadId),
                false,
                null,
                Instant.now());
    }

    private ExternalDownloadOutboxJpaEntity createEntity(UUID id, boolean published) {
        UUID downloadId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant publishedAt = published ? now : null;
        return ExternalDownloadOutboxJpaEntity.of(id, downloadId, published, publishedAt, now, now);
    }
}
