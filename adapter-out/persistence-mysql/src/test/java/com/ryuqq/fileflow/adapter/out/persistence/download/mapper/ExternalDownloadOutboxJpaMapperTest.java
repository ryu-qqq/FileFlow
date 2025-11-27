package com.ryuqq.fileflow.adapter.out.persistence.download.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.ExternalDownloadOutboxJpaEntity;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadOutboxId;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ExternalDownloadOutboxJpaMapper 단위 테스트")
class ExternalDownloadOutboxJpaMapperTest {

    private ExternalDownloadOutboxJpaMapper mapper;
    private static final ZoneId ZONE_ID = ZoneId.of("UTC");

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
            ExternalDownloadOutbox domain = createDomainWithId(1L);

            // when
            ExternalDownloadOutboxJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getExternalDownloadId())
                    .isEqualTo(domain.getExternalDownloadId().value());
            assertThat(entity.getPublished()).isEqualTo(domain.isPublished());
        }

        @Test
        @DisplayName("신규 Domain(ID가 new)인 경우 Entity ID는 null이다")
        void shouldHaveNullIdForNewDomain() {
            // given
            ExternalDownloadOutbox domain = createNewDomain();

            // when
            ExternalDownloadOutboxJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getId()).isNull();
        }

        @Test
        @DisplayName("미발행 상태의 Domain을 Entity로 변환할 수 있다")
        void shouldConvertUnpublishedDomain() {
            // given
            ExternalDownloadOutbox domain =
                    ExternalDownloadOutbox.of(
                            ExternalDownloadOutboxId.of(1L),
                            ExternalDownloadId.of(100L),
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
            Instant publishedAt = Instant.now();
            ExternalDownloadOutbox domain =
                    ExternalDownloadOutbox.of(
                            ExternalDownloadOutboxId.of(1L),
                            ExternalDownloadId.of(100L),
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
        @DisplayName("Instant를 LocalDateTime(UTC)으로 변환한다")
        void shouldConvertInstantToLocalDateTime() {
            // given
            Instant createdAt = Instant.parse("2025-11-26T12:00:00Z");
            ExternalDownloadOutbox domain =
                    ExternalDownloadOutbox.of(
                            ExternalDownloadOutboxId.of(1L),
                            ExternalDownloadId.of(100L),
                            false,
                            null,
                            createdAt);

            // when
            ExternalDownloadOutboxJpaEntity entity = mapper.toEntity(domain);

            // then
            LocalDateTime expected = LocalDateTime.ofInstant(createdAt, ZONE_ID);
            assertThat(entity.getCreatedAt()).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("toDomain 메서드")
    class ToDomainTest {

        @Test
        @DisplayName("Entity를 Domain으로 변환할 수 있다")
        void shouldConvertEntityToDomain() {
            // given
            ExternalDownloadOutboxJpaEntity entity = createEntity(1L, false);

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
            ExternalDownloadOutboxJpaEntity entity = createEntity(1L, false);

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
            ExternalDownloadOutboxJpaEntity entity = createEntity(1L, true);

            // when
            ExternalDownloadOutbox domain = mapper.toDomain(entity);

            // then
            assertThat(domain.isPublished()).isTrue();
            assertThat(domain.getPublishedAt()).isNotNull();
        }

        @Test
        @DisplayName("LocalDateTime을 Instant(UTC)로 변환한다")
        void shouldConvertLocalDateTimeToInstant() {
            // given
            LocalDateTime createdAt = LocalDateTime.of(2025, 11, 26, 12, 0, 0);
            ExternalDownloadOutboxJpaEntity entity =
                    ExternalDownloadOutboxJpaEntity.of(1L, 100L, false, null, createdAt, createdAt);

            // when
            ExternalDownloadOutbox domain = mapper.toDomain(entity);

            // then
            Instant expected = createdAt.atZone(ZONE_ID).toInstant();
            assertThat(domain.getCreatedAt()).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("양방향 변환 테스트")
    class RoundTripTest {

        @Test
        @DisplayName("Domain → Entity → Domain 변환 시 데이터가 보존된다")
        void shouldPreserveDataInRoundTrip() {
            // given
            ExternalDownloadOutbox original = createDomainWithId(1L);

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
            ExternalDownloadOutbox original =
                    ExternalDownloadOutbox.of(
                            ExternalDownloadOutboxId.of(1L),
                            ExternalDownloadId.of(100L),
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
            Instant publishedAt = Instant.now();
            ExternalDownloadOutbox original =
                    ExternalDownloadOutbox.of(
                            ExternalDownloadOutboxId.of(1L),
                            ExternalDownloadId.of(100L),
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
            Instant createdAt = Instant.parse("2025-11-26T12:00:00Z");
            ExternalDownloadOutbox original =
                    ExternalDownloadOutbox.of(
                            ExternalDownloadOutboxId.of(1L),
                            ExternalDownloadId.of(100L),
                            false,
                            null,
                            createdAt);

            // when
            ExternalDownloadOutboxJpaEntity entity = mapper.toEntity(original);
            ExternalDownloadOutbox restored = mapper.toDomain(entity);

            // then
            // LocalDateTime 변환으로 인한 나노초 손실 허용
            assertThat(restored.getCreatedAt().getEpochSecond())
                    .isEqualTo(original.getCreatedAt().getEpochSecond());
        }
    }

    // ==================== Helper Methods ====================

    private ExternalDownloadOutbox createDomainWithId(Long id) {
        return ExternalDownloadOutbox.of(
                ExternalDownloadOutboxId.of(id),
                ExternalDownloadId.of(100L),
                false,
                null,
                Instant.now());
    }

    private ExternalDownloadOutbox createNewDomain() {
        return ExternalDownloadOutbox.of(
                ExternalDownloadOutboxId.forNew(),
                ExternalDownloadId.of(100L),
                false,
                null,
                Instant.now());
    }

    private ExternalDownloadOutboxJpaEntity createEntity(Long id, boolean published) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime publishedAt = published ? now : null;
        return ExternalDownloadOutboxJpaEntity.of(id, 100L, published, publishedAt, now, now);
    }
}
