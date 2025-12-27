package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.ExternalDownloadJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.ExternalDownloadJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.ExternalDownloadJpaRepository;
import com.ryuqq.fileflow.domain.common.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadStatus;
import com.ryuqq.fileflow.domain.download.vo.RetryCount;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import jakarta.persistence.EntityManager;
import java.lang.reflect.Field;
import java.time.Instant;
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

    @Mock private EntityManager entityManager;

    private ExternalDownloadPersistenceAdapter adapter;

    // 테스트용 UUIDv7 값 (실제 UUIDv7 형식)
    private static final String TEST_TENANT_ID = TenantId.generate().value();
    private static final String TEST_ORG_ID = OrganizationId.generate().value();

    @BeforeEach
    void setUp() throws Exception {
        adapter = new ExternalDownloadPersistenceAdapter(jpaRepository, mapper);
        // @PersistenceContext로 주입되는 EntityManager를 리플렉션으로 설정
        Field entityManagerField =
                ExternalDownloadPersistenceAdapter.class.getDeclaredField("entityManager");
        entityManagerField.setAccessible(true);
        entityManagerField.set(adapter, entityManager);
    }

    @Nested
    @DisplayName("persist 메서드")
    class PersistTest {

        @Test
        @DisplayName("Domain을 Entity로 변환하고 저장한 후 Domain을 반환한다")
        void shouldConvertAndSaveAndReturnDomain() {
            // given
            UUID id = UUID.randomUUID();
            ExternalDownload domain = createDomain();
            ExternalDownloadJpaEntity entity = createEntity(id);
            ExternalDownload savedDomain = createDomainWithId(id);

            given(mapper.toEntity(domain)).willReturn(entity);
            given(jpaRepository.saveAndFlush(entity)).willReturn(entity);
            given(mapper.toDomain(entity)).willReturn(savedDomain);

            // when
            ExternalDownload result = adapter.persist(domain);

            // then
            assertThat(result.getId().value()).isEqualTo(id);
            verify(mapper).toEntity(domain);
            verify(jpaRepository).saveAndFlush(entity);
            verify(mapper).toDomain(entity);
        }

        @Test
        @DisplayName("Mapper를 호출하여 Entity로 변환한다")
        void shouldCallMapperToEntity() {
            // given
            UUID id = UUID.randomUUID();
            ExternalDownload domain = createDomain();
            ExternalDownloadJpaEntity entity = createEntity(id);
            ExternalDownload savedDomain = createDomainWithId(id);

            given(mapper.toEntity(domain)).willReturn(entity);
            given(jpaRepository.saveAndFlush(any())).willReturn(entity);
            given(mapper.toDomain(entity)).willReturn(savedDomain);

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
            ExternalDownload savedDomain = createDomainWithId(id);

            given(mapper.toEntity(domain)).willReturn(entity);
            given(jpaRepository.saveAndFlush(entity)).willReturn(entity);
            given(mapper.toDomain(entity)).willReturn(savedDomain);

            // when
            adapter.persist(domain);

            // then
            verify(jpaRepository).saveAndFlush(entity);
        }

        @Test
        @DisplayName("신규 Domain 저장 시 생성된 ID가 포함된 Domain을 반환한다")
        void shouldReturnDomainWithGeneratedIdForNewDomain() {
            // given
            ExternalDownload newDomain =
                    ExternalDownload.of(
                            ExternalDownloadId.forNew(),
                            IdempotencyKey.forNew(),
                            SourceUrl.of("https://example.com/new-file.jpg"),
                            TenantId.of(TEST_TENANT_ID),
                            OrganizationId.of(TEST_ORG_ID),
                            S3Bucket.of("test-bucket"),
                            "downloads/",
                            ExternalDownloadStatus.PENDING,
                            RetryCount.initial(),
                            null,
                            null,
                            null,
                            Instant.now(),
                            Instant.now(),
                            0L);

            UUID savedId = UUID.randomUUID();
            ExternalDownloadJpaEntity entityWithoutId = createEntity(null);
            ExternalDownloadJpaEntity savedEntity = createEntity(savedId);
            ExternalDownload savedDomain = createDomainWithId(savedId);

            given(mapper.toEntity(newDomain)).willReturn(entityWithoutId);
            given(jpaRepository.saveAndFlush(entityWithoutId)).willReturn(savedEntity);
            given(mapper.toDomain(savedEntity)).willReturn(savedDomain);

            // when
            ExternalDownload result = adapter.persist(newDomain);

            // then
            assertThat(result.getId().value()).isEqualTo(savedId);
        }

        @Test
        @DisplayName("기존 Domain 업데이트 시 동일한 ID가 포함된 Domain을 반환한다")
        void shouldReturnDomainWithSameIdForExistingDomain() {
            // given
            UUID existingId = UUID.randomUUID();
            ExternalDownload existingDomain =
                    ExternalDownload.of(
                            ExternalDownloadId.of(existingId),
                            IdempotencyKey.forNew(),
                            SourceUrl.of("https://example.com/existing-file.jpg"),
                            TenantId.of(TEST_TENANT_ID),
                            OrganizationId.of(TEST_ORG_ID),
                            S3Bucket.of("test-bucket"),
                            "downloads/",
                            ExternalDownloadStatus.PROCESSING,
                            RetryCount.of(1),
                            null,
                            null,
                            null,
                            Instant.now(),
                            Instant.now(),
                            0L);

            ExternalDownloadJpaEntity entity = createEntity(existingId);
            ExternalDownload savedDomain = createDomainWithId(existingId);

            given(mapper.toEntity(existingDomain)).willReturn(entity);
            given(jpaRepository.saveAndFlush(entity)).willReturn(entity);
            given(mapper.toDomain(entity)).willReturn(savedDomain);

            // when
            ExternalDownload result = adapter.persist(existingDomain);

            // then
            assertThat(result.getId().value()).isEqualTo(existingId);
        }
    }

    // ==================== Helper Methods ====================

    private ExternalDownload createDomain() {
        return ExternalDownload.of(
                ExternalDownloadId.forNew(),
                IdempotencyKey.forNew(),
                SourceUrl.of("https://example.com/file.jpg"),
                TenantId.of(TEST_TENANT_ID),
                OrganizationId.of(TEST_ORG_ID),
                S3Bucket.of("test-bucket"),
                "downloads/",
                ExternalDownloadStatus.PENDING,
                RetryCount.initial(),
                null,
                null,
                null,
                Instant.now(),
                Instant.now(),
                0L);
    }

    private ExternalDownload createDomainWithId(UUID id) {
        return ExternalDownload.of(
                ExternalDownloadId.of(id),
                IdempotencyKey.forNew(),
                SourceUrl.of("https://example.com/file.jpg"),
                TenantId.of(TEST_TENANT_ID),
                OrganizationId.of(TEST_ORG_ID),
                S3Bucket.of("test-bucket"),
                "downloads/",
                ExternalDownloadStatus.PENDING,
                RetryCount.initial(),
                null,
                null,
                null,
                Instant.now(),
                Instant.now(),
                0L);
    }

    private ExternalDownloadJpaEntity createEntity(UUID id) {
        Instant now = Instant.now();
        return ExternalDownloadJpaEntity.of(
                id,
                UUID.randomUUID().toString(),
                "https://example.com/file.jpg",
                TEST_TENANT_ID,
                TEST_ORG_ID,
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
