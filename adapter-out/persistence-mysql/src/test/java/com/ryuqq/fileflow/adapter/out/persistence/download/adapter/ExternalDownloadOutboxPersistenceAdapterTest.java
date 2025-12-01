package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.ExternalDownloadOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.ExternalDownloadOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.ExternalDownloadOutboxJpaRepository;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadOutboxId;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("ExternalDownloadOutboxPersistenceAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ExternalDownloadOutboxPersistenceAdapterTest {

    @Mock private ExternalDownloadOutboxJpaRepository jpaRepository;

    @Mock private ExternalDownloadOutboxJpaMapper mapper;

    private ExternalDownloadOutboxPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ExternalDownloadOutboxPersistenceAdapter(jpaRepository, mapper);
    }

    @Nested
    @DisplayName("persist 메서드")
    class PersistTest {

        @Test
        @DisplayName("Domain을 Entity로 변환하고 저장한 후 ID를 반환한다")
        void shouldConvertAndSaveAndReturnId() {
            // given
            ExternalDownloadOutbox domain = createDomain();
            UUID expectedId = UUID.randomUUID();
            ExternalDownloadOutboxJpaEntity entity = createEntity(expectedId);

            given(mapper.toEntity(domain)).willReturn(entity);
            given(jpaRepository.save(entity)).willReturn(entity);

            // when
            ExternalDownloadOutboxId result = adapter.persist(domain);

            // then
            assertThat(result.value()).isEqualTo(expectedId);
            verify(mapper).toEntity(domain);
            verify(jpaRepository).save(entity);
        }

        @Test
        @DisplayName("Mapper를 호출하여 Entity로 변환한다")
        void shouldCallMapperToEntity() {
            // given
            ExternalDownloadOutbox domain = createDomain();
            ExternalDownloadOutboxJpaEntity entity = createEntity(UUID.randomUUID());

            given(mapper.toEntity(domain)).willReturn(entity);
            given(jpaRepository.save(any())).willReturn(entity);

            // when
            adapter.persist(domain);

            // then
            verify(mapper).toEntity(domain);
        }

        @Test
        @DisplayName("Repository를 호출하여 Entity를 저장한다")
        void shouldCallRepositorySave() {
            // given
            ExternalDownloadOutbox domain = createDomain();
            ExternalDownloadOutboxJpaEntity entity = createEntity(UUID.randomUUID());

            given(mapper.toEntity(domain)).willReturn(entity);
            given(jpaRepository.save(entity)).willReturn(entity);

            // when
            adapter.persist(domain);

            // then
            verify(jpaRepository).save(entity);
        }

        @Test
        @DisplayName("신규 Outbox 저장 시 생성된 ID를 반환한다")
        void shouldReturnGeneratedIdForNewOutbox() {
            // given
            UUID downloadId = UUID.randomUUID();
            ExternalDownloadOutbox newOutbox =
                    ExternalDownloadOutbox.of(
                            ExternalDownloadOutboxId.forNew(),
                            ExternalDownloadId.of(downloadId),
                            false,
                            null,
                            Instant.now());

            UUID generatedId = UUID.randomUUID();
            ExternalDownloadOutboxJpaEntity entityWithoutId = createEntity(null);
            ExternalDownloadOutboxJpaEntity savedEntity = createEntity(generatedId);

            given(mapper.toEntity(newOutbox)).willReturn(entityWithoutId);
            given(jpaRepository.save(entityWithoutId)).willReturn(savedEntity);

            // when
            ExternalDownloadOutboxId result = adapter.persist(newOutbox);

            // then
            assertThat(result.value()).isEqualTo(generatedId);
        }

        @Test
        @DisplayName("기존 Outbox 업데이트 시 동일한 ID를 반환한다")
        void shouldReturnSameIdForExistingOutbox() {
            // given
            UUID existingId = UUID.randomUUID();
            UUID downloadId = UUID.randomUUID();
            ExternalDownloadOutbox existingOutbox =
                    ExternalDownloadOutbox.of(
                            ExternalDownloadOutboxId.of(existingId),
                            ExternalDownloadId.of(downloadId),
                            true,
                            Instant.now(),
                            Instant.now());

            ExternalDownloadOutboxJpaEntity entity = createEntity(existingId);

            given(mapper.toEntity(existingOutbox)).willReturn(entity);
            given(jpaRepository.save(entity)).willReturn(entity);

            // when
            ExternalDownloadOutboxId result = adapter.persist(existingOutbox);

            // then
            assertThat(result.value()).isEqualTo(existingId);
        }

        @Test
        @DisplayName("미발행 상태의 Outbox를 저장할 수 있다")
        void shouldPersistUnpublishedOutbox() {
            // given
            UUID downloadId = UUID.randomUUID();
            ExternalDownloadOutbox unpublishedOutbox =
                    ExternalDownloadOutbox.of(
                            ExternalDownloadOutboxId.forNew(),
                            ExternalDownloadId.of(downloadId),
                            false,
                            null,
                            Instant.now());

            ExternalDownloadOutboxJpaEntity entity = createEntity(UUID.randomUUID());

            given(mapper.toEntity(unpublishedOutbox)).willReturn(entity);
            given(jpaRepository.save(entity)).willReturn(entity);

            // when
            ExternalDownloadOutboxId result = adapter.persist(unpublishedOutbox);

            // then
            assertThat(result).isNotNull();
            verify(mapper).toEntity(unpublishedOutbox);
            verify(jpaRepository).save(entity);
        }
    }

    // ==================== Helper Methods ====================

    private ExternalDownloadOutbox createDomain() {
        UUID downloadId = UUID.randomUUID();
        return ExternalDownloadOutbox.of(
                ExternalDownloadOutboxId.forNew(),
                ExternalDownloadId.of(downloadId),
                false,
                null,
                Instant.now());
    }

    private ExternalDownloadOutboxJpaEntity createEntity(UUID outboxId) {
        UUID downloadId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        return ExternalDownloadOutboxJpaEntity.of(outboxId, downloadId, false, null, now, now);
    }
}
