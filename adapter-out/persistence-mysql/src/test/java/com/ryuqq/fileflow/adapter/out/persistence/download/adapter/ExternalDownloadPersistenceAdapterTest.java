package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.ExternalDownloadJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.ExternalDownloadJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.ExternalDownloadJpaRepository;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadStatus;
import com.ryuqq.fileflow.domain.download.vo.RetryCount;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
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

@DisplayName("ExternalDownloadPersistenceAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ExternalDownloadPersistenceAdapterTest {

    @Mock private ExternalDownloadJpaRepository jpaRepository;

    @Mock private ExternalDownloadJpaMapper mapper;

    private ExternalDownloadPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ExternalDownloadPersistenceAdapter(jpaRepository, mapper);
    }

    @Nested
    @DisplayName("persist 메서드")
    class PersistTest {

        @Test
        @DisplayName("Domain을 Entity로 변환하고 저장한 후 ID를 반환한다")
        void shouldConvertAndSaveAndReturnId() {
            // given
            UUID id = UUID.randomUUID();
            ExternalDownload domain = createDomain();
            ExternalDownloadJpaEntity entity = createEntity(id);

            given(mapper.toEntity(domain)).willReturn(entity);
            given(jpaRepository.save(entity)).willReturn(entity);

            // when
            ExternalDownloadId result = adapter.persist(domain);

            // then
            assertThat(result.value()).isEqualTo(id);
            verify(mapper).toEntity(domain);
            verify(jpaRepository).save(entity);
        }

        @Test
        @DisplayName("Mapper를 호출하여 Entity로 변환한다")
        void shouldCallMapperToEntity() {
            // given
            UUID id = UUID.randomUUID();
            ExternalDownload domain = createDomain();
            ExternalDownloadJpaEntity entity = createEntity(id);

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
            UUID id = UUID.randomUUID();
            ExternalDownload domain = createDomain();
            ExternalDownloadJpaEntity entity = createEntity(id);

            given(mapper.toEntity(domain)).willReturn(entity);
            given(jpaRepository.save(entity)).willReturn(entity);

            // when
            adapter.persist(domain);

            // then
            verify(jpaRepository).save(entity);
        }

        @Test
        @DisplayName("신규 Domain 저장 시 생성된 ID를 반환한다")
        void shouldReturnGeneratedIdForNewDomain() {
            // given
            ExternalDownload newDomain =
                    ExternalDownload.of(
                            ExternalDownloadId.forNew(),
                            SourceUrl.of("https://example.com/new-file.jpg"),
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

            UUID savedId = UUID.randomUUID();
            ExternalDownloadJpaEntity entityWithoutId = createEntity(null);
            ExternalDownloadJpaEntity savedEntity = createEntity(savedId);

            given(mapper.toEntity(newDomain)).willReturn(entityWithoutId);
            given(jpaRepository.save(entityWithoutId)).willReturn(savedEntity);

            // when
            ExternalDownloadId result = adapter.persist(newDomain);

            // then
            assertThat(result.value()).isEqualTo(savedId);
        }

        @Test
        @DisplayName("기존 Domain 업데이트 시 동일한 ID를 반환한다")
        void shouldReturnSameIdForExistingDomain() {
            // given
            UUID existingId = UUID.randomUUID();
            ExternalDownload existingDomain =
                    ExternalDownload.of(
                            ExternalDownloadId.of(existingId),
                            SourceUrl.of("https://example.com/existing-file.jpg"),
                            100L,
                            200L,
                            S3Bucket.of("test-bucket"),
                            "downloads/",
                            ExternalDownloadStatus.PROCESSING,
                            RetryCount.of(1),
                            null,
                            null,
                            null,
                            Instant.now(),
                            Instant.now());

            ExternalDownloadJpaEntity entity = createEntity(existingId);

            given(mapper.toEntity(existingDomain)).willReturn(entity);
            given(jpaRepository.save(entity)).willReturn(entity);

            // when
            ExternalDownloadId result = adapter.persist(existingDomain);

            // then
            assertThat(result.value()).isEqualTo(existingId);
        }
    }

    // ==================== Helper Methods ====================

    private ExternalDownload createDomain() {
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
                null,
                0L,
                now,
                now);
    }
}
