package com.ryuqq.fileflow.adapter.out.persistence.asset.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.FileAssetStatusHistoryJpaEntity;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatusHistoryId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FileAssetStatusHistoryJpaMapper 단위 테스트")
class FileAssetStatusHistoryJpaMapperTest {

    private FileAssetStatusHistoryJpaMapper mapper;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        mapper = new FileAssetStatusHistoryJpaMapper();
        fixedClock = Clock.fixed(Instant.parse("2025-12-15T10:00:00Z"), ZoneId.of("UTC"));
    }

    @Nested
    @DisplayName("toEntity 메서드")
    class ToEntityTest {

        @Test
        @DisplayName("Domain을 Entity로 변환할 수 있다")
        void shouldConvertDomainToEntity() {
            // given
            FileAssetStatusHistory domain = createDomain();

            // when
            FileAssetStatusHistoryJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getId()).isEqualTo(domain.getId().value());
            assertThat(entity.getFileAssetId()).isEqualTo(domain.getFileAssetId().value().toString());
            assertThat(entity.getFromStatus()).isEqualTo(domain.getFromStatus());
            assertThat(entity.getToStatus()).isEqualTo(domain.getToStatus());
            assertThat(entity.getMessage()).isEqualTo(domain.getMessage());
            assertThat(entity.getActor()).isEqualTo(domain.getActor());
            assertThat(entity.getActorType()).isEqualTo(domain.getActorType());
            assertThat(entity.getChangedAt()).isEqualTo(domain.getChangedAt());
            assertThat(entity.getDurationMillis()).isEqualTo(domain.getDurationMillis());
        }

        @Test
        @DisplayName("fromStatus가 null인 경우도 처리된다")
        void shouldHandleNullFromStatus() {
            // given
            FileAssetStatusHistory domain = createDomainWithNullFromStatus();

            // when
            FileAssetStatusHistoryJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getFromStatus()).isNull();
            assertThat(entity.getToStatus()).isEqualTo(FileAssetStatus.PENDING);
        }

        @Test
        @DisplayName("message가 null인 경우도 처리된다")
        void shouldHandleNullMessage() {
            // given
            FileAssetStatusHistory domain = createDomainWithNullMessage();

            // when
            FileAssetStatusHistoryJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getMessage()).isNull();
        }

        @Test
        @DisplayName("durationMillis가 null인 경우도 처리된다")
        void shouldHandleNullDurationMillis() {
            // given
            FileAssetStatusHistory domain = createDomainWithNullDuration();

            // when
            FileAssetStatusHistoryJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getDurationMillis()).isNull();
        }
    }

    @Nested
    @DisplayName("toDomain 메서드")
    class ToDomainTest {

        @Test
        @DisplayName("Entity를 Domain으로 변환할 수 있다")
        void shouldConvertEntityToDomain() {
            // given
            FileAssetStatusHistoryJpaEntity entity = createEntity();

            // when
            FileAssetStatusHistory domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getId().value()).isEqualTo(entity.getId());
            assertThat(domain.getFileAssetId().value().toString()).isEqualTo(entity.getFileAssetId());
            assertThat(domain.getFromStatus()).isEqualTo(entity.getFromStatus());
            assertThat(domain.getToStatus()).isEqualTo(entity.getToStatus());
            assertThat(domain.getMessage()).isEqualTo(entity.getMessage());
            assertThat(domain.getActor()).isEqualTo(entity.getActor());
            assertThat(domain.getActorType()).isEqualTo(entity.getActorType());
            assertThat(domain.getChangedAt()).isEqualTo(entity.getChangedAt());
            assertThat(domain.getDurationMillis()).isEqualTo(entity.getDurationMillis());
        }

        @Test
        @DisplayName("Entity의 fromStatus가 null인 경우도 처리된다")
        void shouldHandleNullFromStatus() {
            // given
            FileAssetStatusHistoryJpaEntity entity = createEntityWithNullFromStatus();

            // when
            FileAssetStatusHistory domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getFromStatus()).isNull();
            assertThat(domain.isInitialCreation()).isTrue();
        }

        @Test
        @DisplayName("Entity의 message가 null인 경우도 처리된다")
        void shouldHandleNullMessage() {
            // given
            FileAssetStatusHistoryJpaEntity entity = createEntityWithNullMessage();

            // when
            FileAssetStatusHistory domain = mapper.toDomain(entity);

            // then
            assertThat(domain.getMessage()).isNull();
        }
    }

    @Nested
    @DisplayName("양방향 변환 테스트")
    class RoundTripTest {

        @Test
        @DisplayName("Domain → Entity → Domain 변환 시 데이터가 보존된다")
        void shouldPreserveDataInRoundTrip() {
            // given
            FileAssetStatusHistory original = createDomain();

            // when
            FileAssetStatusHistoryJpaEntity entity = mapper.toEntity(original);
            FileAssetStatusHistory restored = mapper.toDomain(entity);

            // then
            assertThat(restored.getId().value()).isEqualTo(original.getId().value());
            assertThat(restored.getFileAssetId().value()).isEqualTo(original.getFileAssetId().value());
            assertThat(restored.getFromStatus()).isEqualTo(original.getFromStatus());
            assertThat(restored.getToStatus()).isEqualTo(original.getToStatus());
            assertThat(restored.getMessage()).isEqualTo(original.getMessage());
            assertThat(restored.getActor()).isEqualTo(original.getActor());
            assertThat(restored.getActorType()).isEqualTo(original.getActorType());
            assertThat(restored.getChangedAt()).isEqualTo(original.getChangedAt());
            assertThat(restored.getDurationMillis()).isEqualTo(original.getDurationMillis());
        }

        @Test
        @DisplayName("FAILED 상태인 경우 양방향 변환이 보존된다")
        void shouldPreserveFailedStatusInRoundTrip() {
            // given
            FileAssetStatusHistory original = createFailedDomain();

            // when
            FileAssetStatusHistoryJpaEntity entity = mapper.toEntity(original);
            FileAssetStatusHistory restored = mapper.toDomain(entity);

            // then
            assertThat(restored.getToStatus()).isEqualTo(FileAssetStatus.FAILED);
            assertThat(restored.isFailure()).isTrue();
        }
    }

    // ==================== Helper Methods ====================

    private FileAssetStatusHistory createDomain() {
        return FileAssetStatusHistory.reconstitute(
                new FileAssetStatusHistoryId(UUID.randomUUID()),
                new FileAssetId(UUID.randomUUID()),
                FileAssetStatus.PENDING,
                FileAssetStatus.PROCESSING,
                "Processing started",
                "system",
                "SYSTEM",
                fixedClock.instant(),
                1000L);
    }

    private FileAssetStatusHistory createDomainWithNullFromStatus() {
        return FileAssetStatusHistory.reconstitute(
                new FileAssetStatusHistoryId(UUID.randomUUID()),
                new FileAssetId(UUID.randomUUID()),
                null,
                FileAssetStatus.PENDING,
                "Initial creation",
                "system",
                "SYSTEM",
                fixedClock.instant(),
                null);
    }

    private FileAssetStatusHistory createDomainWithNullMessage() {
        return FileAssetStatusHistory.reconstitute(
                new FileAssetStatusHistoryId(UUID.randomUUID()),
                new FileAssetId(UUID.randomUUID()),
                FileAssetStatus.PENDING,
                FileAssetStatus.PROCESSING,
                null,
                "system",
                "SYSTEM",
                fixedClock.instant(),
                1000L);
    }

    private FileAssetStatusHistory createDomainWithNullDuration() {
        return FileAssetStatusHistory.reconstitute(
                new FileAssetStatusHistoryId(UUID.randomUUID()),
                new FileAssetId(UUID.randomUUID()),
                FileAssetStatus.PENDING,
                FileAssetStatus.PROCESSING,
                "Processing started",
                "system",
                "SYSTEM",
                fixedClock.instant(),
                null);
    }

    private FileAssetStatusHistory createFailedDomain() {
        return FileAssetStatusHistory.reconstitute(
                new FileAssetStatusHistoryId(UUID.randomUUID()),
                new FileAssetId(UUID.randomUUID()),
                FileAssetStatus.PROCESSING,
                FileAssetStatus.FAILED,
                "Processing failed: timeout",
                "system",
                "SYSTEM",
                fixedClock.instant(),
                5000L);
    }

    private FileAssetStatusHistoryJpaEntity createEntity() {
        return FileAssetStatusHistoryJpaEntity.of(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                FileAssetStatus.PENDING,
                FileAssetStatus.PROCESSING,
                "Processing started",
                "system",
                "SYSTEM",
                fixedClock.instant(),
                1000L,
                Instant.now());
    }

    private FileAssetStatusHistoryJpaEntity createEntityWithNullFromStatus() {
        return FileAssetStatusHistoryJpaEntity.of(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                null,
                FileAssetStatus.PENDING,
                "Initial creation",
                "system",
                "SYSTEM",
                fixedClock.instant(),
                null,
                Instant.now());
    }

    private FileAssetStatusHistoryJpaEntity createEntityWithNullMessage() {
        return FileAssetStatusHistoryJpaEntity.of(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                FileAssetStatus.PENDING,
                FileAssetStatus.PROCESSING,
                null,
                "system",
                "SYSTEM",
                fixedClock.instant(),
                1000L,
                Instant.now());
    }
}
