package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.ExternalDownloadOutboxJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.ExternalDownloadOutboxJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.ExternalDownloadOutboxQueryDslRepository;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadOutboxId;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("ExternalDownloadOutboxQueryAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ExternalDownloadOutboxQueryAdapterTest {

    @Mock private ExternalDownloadOutboxQueryDslRepository queryDslRepository;

    @Mock private ExternalDownloadOutboxJpaMapper mapper;

    private ExternalDownloadOutboxQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ExternalDownloadOutboxQueryAdapter(queryDslRepository, mapper);
    }

    @Nested
    @DisplayName("findByExternalDownloadId 메서드")
    class FindByExternalDownloadIdTest {

        @Test
        @DisplayName("ExternalDownloadId로 조회 시 존재하면 Domain을 반환한다")
        void shouldReturnDomainWhenExists() {
            // given
            Long downloadId = 100L;
            ExternalDownloadId externalDownloadId = ExternalDownloadId.of(downloadId);
            ExternalDownloadOutboxJpaEntity entity = createEntity(1L, downloadId);
            ExternalDownloadOutbox domain = createDomain(1L, downloadId);

            given(queryDslRepository.findByExternalDownloadId(downloadId))
                    .willReturn(Optional.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // when
            Optional<ExternalDownloadOutbox> result =
                    adapter.findByExternalDownloadId(externalDownloadId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getExternalDownloadId().value()).isEqualTo(downloadId);
            verify(queryDslRepository).findByExternalDownloadId(downloadId);
            verify(mapper).toDomain(entity);
        }

        @Test
        @DisplayName("ExternalDownloadId로 조회 시 존재하지 않으면 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenNotExists() {
            // given
            Long downloadId = 999L;
            ExternalDownloadId externalDownloadId = ExternalDownloadId.of(downloadId);

            given(queryDslRepository.findByExternalDownloadId(downloadId))
                    .willReturn(Optional.empty());

            // when
            Optional<ExternalDownloadOutbox> result =
                    adapter.findByExternalDownloadId(externalDownloadId);

            // then
            assertThat(result).isEmpty();
            verify(queryDslRepository).findByExternalDownloadId(downloadId);
        }

        @Test
        @DisplayName("Repository와 Mapper를 순차적으로 호출한다")
        void shouldCallRepositoryThenMapper() {
            // given
            Long downloadId = 100L;
            ExternalDownloadId externalDownloadId = ExternalDownloadId.of(downloadId);
            ExternalDownloadOutboxJpaEntity entity = createEntity(1L, downloadId);
            ExternalDownloadOutbox domain = createDomain(1L, downloadId);

            given(queryDslRepository.findByExternalDownloadId(downloadId))
                    .willReturn(Optional.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // when
            adapter.findByExternalDownloadId(externalDownloadId);

            // then
            verify(queryDslRepository).findByExternalDownloadId(downloadId);
            verify(mapper).toDomain(entity);
        }
    }

    @Nested
    @DisplayName("findUnpublished 메서드")
    class FindUnpublishedTest {

        @Test
        @DisplayName("미발행 Outbox 목록을 조회한다")
        void shouldReturnUnpublishedOutboxes() {
            // given
            int limit = 10;
            List<ExternalDownloadOutboxJpaEntity> entities =
                    List.of(createEntity(1L, 100L), createEntity(2L, 200L), createEntity(3L, 300L));

            List<ExternalDownloadOutbox> domains =
                    List.of(createDomain(1L, 100L), createDomain(2L, 200L), createDomain(3L, 300L));

            given(queryDslRepository.findUnpublished(limit)).willReturn(entities);
            given(mapper.toDomain(entities.get(0))).willReturn(domains.get(0));
            given(mapper.toDomain(entities.get(1))).willReturn(domains.get(1));
            given(mapper.toDomain(entities.get(2))).willReturn(domains.get(2));

            // when
            List<ExternalDownloadOutbox> result = adapter.findUnpublished(limit);

            // then
            assertThat(result).hasSize(3);
            verify(queryDslRepository).findUnpublished(limit);
        }

        @Test
        @DisplayName("미발행 Outbox가 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNoUnpublished() {
            // given
            int limit = 10;

            given(queryDslRepository.findUnpublished(limit)).willReturn(List.of());

            // when
            List<ExternalDownloadOutbox> result = adapter.findUnpublished(limit);

            // then
            assertThat(result).isEmpty();
            verify(queryDslRepository).findUnpublished(limit);
        }

        @Test
        @DisplayName("limit 파라미터가 Repository에 전달된다")
        void shouldPassLimitToRepository() {
            // given
            int limit = 5;

            given(queryDslRepository.findUnpublished(limit)).willReturn(List.of());

            // when
            adapter.findUnpublished(limit);

            // then
            verify(queryDslRepository).findUnpublished(limit);
        }

        @Test
        @DisplayName("각 Entity가 Domain으로 변환된다")
        void shouldConvertEachEntityToDomain() {
            // given
            int limit = 2;
            List<ExternalDownloadOutboxJpaEntity> entities =
                    List.of(createEntity(1L, 100L), createEntity(2L, 200L));

            List<ExternalDownloadOutbox> domains =
                    List.of(createDomain(1L, 100L), createDomain(2L, 200L));

            given(queryDslRepository.findUnpublished(limit)).willReturn(entities);
            given(mapper.toDomain(entities.get(0))).willReturn(domains.get(0));
            given(mapper.toDomain(entities.get(1))).willReturn(domains.get(1));

            // when
            List<ExternalDownloadOutbox> result = adapter.findUnpublished(limit);

            // then
            assertThat(result).hasSize(2);
            verify(mapper).toDomain(entities.get(0));
            verify(mapper).toDomain(entities.get(1));
        }
    }

    // ==================== Helper Methods ====================

    private ExternalDownloadOutbox createDomain(Long id, Long downloadId) {
        return ExternalDownloadOutbox.of(
                ExternalDownloadOutboxId.of(id),
                ExternalDownloadId.of(downloadId),
                false,
                null,
                Instant.now());
    }

    private ExternalDownloadOutboxJpaEntity createEntity(Long id, Long downloadId) {
        LocalDateTime now = LocalDateTime.now();
        return ExternalDownloadOutboxJpaEntity.of(id, downloadId, false, null, now, now);
    }
}
