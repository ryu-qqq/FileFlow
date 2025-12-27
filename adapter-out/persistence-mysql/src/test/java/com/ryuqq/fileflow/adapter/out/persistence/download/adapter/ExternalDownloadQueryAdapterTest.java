package com.ryuqq.fileflow.adapter.out.persistence.download.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.fileflow.adapter.out.persistence.download.entity.ExternalDownloadJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.download.mapper.ExternalDownloadJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.download.repository.ExternalDownloadQueryDslRepository;
import com.ryuqq.fileflow.domain.common.vo.IdempotencyKey;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadStatus;
import com.ryuqq.fileflow.domain.download.vo.RetryCount;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("ExternalDownloadQueryAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ExternalDownloadQueryAdapterTest {
    // 테스트용 UUIDv7 값 (실제 UUIDv7 형식)
    private static final String TEST_TENANT_ID = TenantId.generate().value();
    private static final String TEST_ORG_ID = OrganizationId.generate().value();

    @Mock private ExternalDownloadQueryDslRepository queryDslRepository;

    @Mock private ExternalDownloadJpaMapper mapper;

    private ExternalDownloadQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ExternalDownloadQueryAdapter(queryDslRepository, mapper);
    }

    @Nested
    @DisplayName("findById 메서드")
    class FindByIdTest {

        @Test
        @DisplayName("ID로 조회 시 존재하면 Domain을 반환한다")
        void shouldReturnDomainWhenExists() {
            // given
            UUID id = UUID.randomUUID();
            ExternalDownloadId downloadId = ExternalDownloadId.of(id);
            ExternalDownloadJpaEntity entity = createEntity(id);
            ExternalDownload domain = createDomain(id);

            given(queryDslRepository.findById(id)).willReturn(Optional.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // when
            Optional<ExternalDownload> result = adapter.findById(downloadId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId().value()).isEqualTo(id);
            verify(queryDslRepository).findById(id);
            verify(mapper).toDomain(entity);
        }

        @Test
        @DisplayName("ID로 조회 시 존재하지 않으면 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenNotExists() {
            // given
            UUID id = UUID.randomUUID();
            ExternalDownloadId downloadId = ExternalDownloadId.of(id);

            given(queryDslRepository.findById(id)).willReturn(Optional.empty());

            // when
            Optional<ExternalDownload> result = adapter.findById(downloadId);

            // then
            assertThat(result).isEmpty();
            verify(queryDslRepository).findById(id);
        }

        @Test
        @DisplayName("Repository와 Mapper를 순차적으로 호출한다")
        void shouldCallRepositoryThenMapper() {
            // given
            UUID id = UUID.randomUUID();
            ExternalDownloadId downloadId = ExternalDownloadId.of(id);
            ExternalDownloadJpaEntity entity = createEntity(id);
            ExternalDownload domain = createDomain(id);

            given(queryDslRepository.findById(id)).willReturn(Optional.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // when
            adapter.findById(downloadId);

            // then
            verify(queryDslRepository).findById(id);
            verify(mapper).toDomain(entity);
        }
    }

    @Nested
    @DisplayName("findByIdAndTenantId 메서드")
    class FindByIdAndTenantIdTest {

        @Test
        @DisplayName("ID와 TenantId로 조회 시 존재하면 Domain을 반환한다")
        void shouldReturnDomainWhenExists() {
            // given
            UUID id = UUID.randomUUID();
            String tenantId = TEST_TENANT_ID;
            ExternalDownloadId downloadId = ExternalDownloadId.of(id);
            ExternalDownloadJpaEntity entity = createEntity(id);
            ExternalDownload domain = createDomain(id);

            given(queryDslRepository.findByIdAndTenantId(id, tenantId))
                    .willReturn(Optional.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // when
            Optional<ExternalDownload> result = adapter.findByIdAndTenantId(downloadId, tenantId);

            // then
            assertThat(result).isPresent();
            verify(queryDslRepository).findByIdAndTenantId(id, tenantId);
        }

        @Test
        @DisplayName("TenantId가 다르면 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenTenantMismatch() {
            // given
            UUID id = UUID.randomUUID();
            String wrongTenantId = TenantId.generate().value(); // 다른 테넌트 ID 생성
            ExternalDownloadId downloadId = ExternalDownloadId.of(id);

            given(queryDslRepository.findByIdAndTenantId(id, wrongTenantId))
                    .willReturn(Optional.empty());

            // when
            Optional<ExternalDownload> result =
                    adapter.findByIdAndTenantId(downloadId, wrongTenantId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("ID와 TenantId 모두 일치해야 조회된다")
        void shouldRequireBothIdAndTenantIdMatch() {
            // given
            UUID id = UUID.randomUUID();
            String tenantId = TEST_TENANT_ID;
            ExternalDownloadId downloadId = ExternalDownloadId.of(id);
            ExternalDownloadJpaEntity entity = createEntity(id);
            ExternalDownload domain = createDomain(id);

            given(queryDslRepository.findByIdAndTenantId(id, tenantId))
                    .willReturn(Optional.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // when
            Optional<ExternalDownload> result = adapter.findByIdAndTenantId(downloadId, tenantId);

            // then
            assertThat(result).isPresent();
            verify(queryDslRepository).findByIdAndTenantId(id, tenantId);
        }
    }

    @Nested
    @DisplayName("findByTenantIdAndIdempotencyKey 메서드")
    class FindByTenantIdAndIdempotencyKeyTest {

        @Test
        @DisplayName("테넌트ID와 멱등성키로 조회 시 존재하면 Domain을 반환한다")
        void shouldReturnDomainWhenExists() {
            // given
            UUID id = UUID.randomUUID();
            TenantId tenantId = TenantId.of(TEST_TENANT_ID);
            IdempotencyKey idempotencyKey = IdempotencyKey.forNew();
            ExternalDownloadJpaEntity entity = createEntity(id);
            ExternalDownload domain = createDomain(id);

            given(
                            queryDslRepository.findByTenantIdAndIdempotencyKey(
                                    tenantId.value(), idempotencyKey.getValue()))
                    .willReturn(Optional.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // when
            Optional<ExternalDownload> result =
                    adapter.findByTenantIdAndIdempotencyKey(tenantId, idempotencyKey);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId().value()).isEqualTo(id);
            verify(queryDslRepository)
                    .findByTenantIdAndIdempotencyKey(tenantId.value(), idempotencyKey.getValue());
            verify(mapper).toDomain(entity);
        }

        @Test
        @DisplayName("테넌트ID와 멱등성키로 조회 시 존재하지 않으면 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenNotExists() {
            // given
            TenantId tenantId = TenantId.of(TEST_TENANT_ID);
            IdempotencyKey idempotencyKey = IdempotencyKey.forNew();

            given(
                            queryDslRepository.findByTenantIdAndIdempotencyKey(
                                    tenantId.value(), idempotencyKey.getValue()))
                    .willReturn(Optional.empty());

            // when
            Optional<ExternalDownload> result =
                    adapter.findByTenantIdAndIdempotencyKey(tenantId, idempotencyKey);

            // then
            assertThat(result).isEmpty();
            verify(queryDslRepository)
                    .findByTenantIdAndIdempotencyKey(tenantId.value(), idempotencyKey.getValue());
        }
    }

    @Nested
    @DisplayName("findByCriteria 메서드")
    class FindByCriteriaTest {

        @Test
        @DisplayName("조회 결과가 있으면 Domain 목록을 반환한다")
        void shouldReturnDomainListWhenResultsExist() {
            // given
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            String orgId = TEST_ORG_ID;
            String tenantId = TEST_TENANT_ID;
            ExternalDownloadStatus status = ExternalDownloadStatus.PENDING;
            long offset = 0L;
            int limit = 10;

            ExternalDownloadJpaEntity entity1 = createEntity(id1);
            ExternalDownloadJpaEntity entity2 = createEntity(id2);
            ExternalDownload domain1 = createDomain(id1);
            ExternalDownload domain2 = createDomain(id2);

            given(queryDslRepository.findByCriteria(orgId, tenantId, status, offset, limit))
                    .willReturn(java.util.List.of(entity1, entity2));
            given(mapper.toDomain(entity1)).willReturn(domain1);
            given(mapper.toDomain(entity2)).willReturn(domain2);

            // when
            java.util.List<ExternalDownload> result =
                    adapter.findByCriteria(orgId, tenantId, status, offset, limit);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId().value()).isEqualTo(id1);
            assertThat(result.get(1).getId().value()).isEqualTo(id2);
            verify(queryDslRepository).findByCriteria(orgId, tenantId, status, offset, limit);
        }

        @Test
        @DisplayName("조회 결과가 없으면 빈 목록을 반환한다")
        void shouldReturnEmptyListWhenNoResults() {
            // given
            String orgId = TEST_ORG_ID;
            String tenantId = TEST_TENANT_ID;
            ExternalDownloadStatus status = ExternalDownloadStatus.PENDING;
            long offset = 0L;
            int limit = 10;

            given(queryDslRepository.findByCriteria(orgId, tenantId, status, offset, limit))
                    .willReturn(java.util.List.of());

            // when
            java.util.List<ExternalDownload> result =
                    adapter.findByCriteria(orgId, tenantId, status, offset, limit);

            // then
            assertThat(result).isEmpty();
            verify(queryDslRepository).findByCriteria(orgId, tenantId, status, offset, limit);
        }
    }

    @Nested
    @DisplayName("countByCriteria 메서드")
    class CountByCriteriaTest {

        @Test
        @DisplayName("조회 결과 개수를 반환한다")
        void shouldReturnCount() {
            // given
            String orgId = TEST_ORG_ID;
            String tenantId = TEST_TENANT_ID;
            ExternalDownloadStatus status = ExternalDownloadStatus.PENDING;

            given(queryDslRepository.countByCriteria(orgId, tenantId, status)).willReturn(5L);

            // when
            long result = adapter.countByCriteria(orgId, tenantId, status);

            // then
            assertThat(result).isEqualTo(5L);
            verify(queryDslRepository).countByCriteria(orgId, tenantId, status);
        }

        @Test
        @DisplayName("결과가 없으면 0을 반환한다")
        void shouldReturnZeroWhenNoResults() {
            // given
            String orgId = TEST_ORG_ID;
            String tenantId = TEST_TENANT_ID;
            ExternalDownloadStatus status = ExternalDownloadStatus.FAILED;

            given(queryDslRepository.countByCriteria(orgId, tenantId, status)).willReturn(0L);

            // when
            long result = adapter.countByCriteria(orgId, tenantId, status);

            // then
            assertThat(result).isZero();
            verify(queryDslRepository).countByCriteria(orgId, tenantId, status);
        }
    }

    @Nested
    @DisplayName("existsById 메서드")
    class ExistsByIdTest {

        @Test
        @DisplayName("존재하면 true를 반환한다")
        void shouldReturnTrueWhenExists() {
            // given
            UUID id = UUID.randomUUID();
            ExternalDownloadId downloadId = ExternalDownloadId.of(id);

            given(queryDslRepository.existsById(id)).willReturn(true);

            // when
            boolean result = adapter.existsById(downloadId);

            // then
            assertThat(result).isTrue();
            verify(queryDslRepository).existsById(id);
        }

        @Test
        @DisplayName("존재하지 않으면 false를 반환한다")
        void shouldReturnFalseWhenNotExists() {
            // given
            UUID id = UUID.randomUUID();
            ExternalDownloadId downloadId = ExternalDownloadId.of(id);

            given(queryDslRepository.existsById(id)).willReturn(false);

            // when
            boolean result = adapter.existsById(downloadId);

            // then
            assertThat(result).isFalse();
            verify(queryDslRepository).existsById(id);
        }

        @Test
        @DisplayName("Repository의 existsById를 직접 호출한다")
        void shouldCallRepositoryExistsById() {
            // given
            UUID id = UUID.randomUUID();
            ExternalDownloadId downloadId = ExternalDownloadId.of(id);

            given(queryDslRepository.existsById(id)).willReturn(true);

            // when
            adapter.existsById(downloadId);

            // then
            verify(queryDslRepository).existsById(id);
        }
    }

    // ==================== Helper Methods ====================

    private ExternalDownload createDomain(UUID id) {
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
