package com.ryuqq.fileflow.adapter.out.persistence.asset.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.asset.entity.QFileAssetJpaEntity.fileAssetJpaEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.FileAssetJpaEntity;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("FileAssetQueryDslRepository 단위 테스트")
@ExtendWith(MockitoExtension.class)
class FileAssetQueryDslRepositoryTest {
    // 테스트용 UUIDv7 값 (실제 UUIDv7 형식)
    private static final String TEST_TENANT_ID = TenantId.generate().value();
    private static final String TEST_ORG_ID = OrganizationId.generate().value();

    @Mock private JPAQueryFactory queryFactory;

    @Mock private JPAQuery<FileAssetJpaEntity> jpaQuery;

    @Mock private JPAQuery<Long> countQuery;

    private FileAssetQueryDslRepository repository;

    @BeforeEach
    void setUp() {
        repository = new FileAssetQueryDslRepository(queryFactory);
    }

    @Nested
    @DisplayName("findById 테스트")
    class FindByIdTest {

        @Test
        @DisplayName("ID, organizationId, tenantId로 FileAsset을 조회할 수 있다")
        void findById_WithValidParams_ShouldReturnEntity() {
            // given
            String id = "asset-123";
            String organizationId = TEST_ORG_ID;
            String tenantId = TEST_TENANT_ID;
            FileAssetJpaEntity entity = createEntity(id, organizationId, tenantId);

            when(queryFactory.selectFrom(fileAssetJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate[].class))).thenReturn(jpaQuery);
            when(jpaQuery.fetchOne()).thenReturn(entity);

            // when
            Optional<FileAssetJpaEntity> result = repository.findById(id, organizationId, tenantId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(id);
            assertThat(result.get().getOrganizationId()).isEqualTo(organizationId);
            assertThat(result.get().getTenantId()).isEqualTo(tenantId);
        }

        @Test
        @DisplayName("조회 결과가 없으면 empty Optional을 반환한다")
        void findById_WhenNotFound_ShouldReturnEmpty() {
            // given
            when(queryFactory.selectFrom(fileAssetJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate[].class))).thenReturn(jpaQuery);
            when(jpaQuery.fetchOne()).thenReturn(null);

            // when
            Optional<FileAssetJpaEntity> result =
                    repository.findById("not-exist", TEST_ORG_ID, TEST_TENANT_ID);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByCriteria 테스트")
    class FindByCriteriaTest {

        @Test
        @DisplayName("조건에 맞는 FileAsset 목록을 조회할 수 있다")
        void findByCriteria_WithConditions_ShouldReturnEntities() {
            // given
            String organizationId = TEST_ORG_ID;
            String tenantId = TEST_TENANT_ID;
            FileAssetStatus status = FileAssetStatus.COMPLETED;
            FileCategory category = FileCategory.IMAGE;
            long offset = 0;
            int limit = 10;

            List<FileAssetJpaEntity> entities =
                    List.of(
                            createEntity("asset-1", organizationId, tenantId),
                            createEntity("asset-2", organizationId, tenantId));

            when(queryFactory.selectFrom(fileAssetJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate[].class))).thenReturn(jpaQuery);
            when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
            when(jpaQuery.offset(offset)).thenReturn(jpaQuery);
            when(jpaQuery.limit(limit)).thenReturn(jpaQuery);
            when(jpaQuery.fetch()).thenReturn(entities);

            // when
            List<FileAssetJpaEntity> result =
                    repository.findByCriteria(
                            organizationId,
                            tenantId,
                            status,
                            category,
                            null,
                            null,
                            null,
                            "CREATED_AT",
                            false,
                            offset,
                            limit);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo("asset-1");
            assertThat(result.get(1).getId()).isEqualTo("asset-2");
        }

        @Test
        @DisplayName("조건에 맞는 FileAsset이 없으면 빈 목록을 반환한다")
        void findByCriteria_WhenNoMatch_ShouldReturnEmptyList() {
            // given
            when(queryFactory.selectFrom(fileAssetJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate[].class))).thenReturn(jpaQuery);
            when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
            when(jpaQuery.offset(0L)).thenReturn(jpaQuery);
            when(jpaQuery.limit(10)).thenReturn(jpaQuery);
            when(jpaQuery.fetch()).thenReturn(List.of());

            // when
            List<FileAssetJpaEntity> result =
                    repository.findByCriteria(
                            TEST_ORG_ID,
                            TEST_TENANT_ID,
                            null,
                            null,
                            null,
                            null,
                            null,
                            "CREATED_AT",
                            false,
                            0,
                            10);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("status와 category가 null이면 필터링 없이 조회한다")
        void findByCriteria_WithNullFilters_ShouldNotFilter() {
            // given
            List<FileAssetJpaEntity> entities =
                    List.of(createEntity("asset-1", TEST_ORG_ID, TEST_TENANT_ID));

            when(queryFactory.selectFrom(fileAssetJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate[].class))).thenReturn(jpaQuery);
            when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
            when(jpaQuery.offset(0L)).thenReturn(jpaQuery);
            when(jpaQuery.limit(10)).thenReturn(jpaQuery);
            when(jpaQuery.fetch()).thenReturn(entities);

            // when
            List<FileAssetJpaEntity> result =
                    repository.findByCriteria(
                            TEST_ORG_ID,
                            TEST_TENANT_ID,
                            null,
                            null,
                            null,
                            null,
                            null,
                            "CREATED_AT",
                            false,
                            0,
                            10);

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("파일명으로 정렬하여 조회할 수 있다")
        void findByCriteria_WithFileNameSort_ShouldReturnSortedEntities() {
            // given
            List<FileAssetJpaEntity> entities =
                    List.of(createEntity("asset-1", TEST_ORG_ID, TEST_TENANT_ID));

            when(queryFactory.selectFrom(fileAssetJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate[].class))).thenReturn(jpaQuery);
            when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
            when(jpaQuery.offset(0L)).thenReturn(jpaQuery);
            when(jpaQuery.limit(10)).thenReturn(jpaQuery);
            when(jpaQuery.fetch()).thenReturn(entities);

            // when
            List<FileAssetJpaEntity> result =
                    repository.findByCriteria(
                            TEST_ORG_ID,
                            TEST_TENANT_ID,
                            null,
                            null,
                            null,
                            null,
                            null,
                            "FILE_NAME",
                            true,
                            0,
                            10);

            // then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("countByCriteria 테스트")
    class CountByCriteriaTest {

        @Test
        @DisplayName("조건에 맞는 FileAsset 개수를 반환한다")
        void countByCriteria_WithConditions_ShouldReturnCount() {
            // given
            String organizationId = TEST_ORG_ID;
            String tenantId = TEST_TENANT_ID;
            FileAssetStatus status = FileAssetStatus.COMPLETED;
            FileCategory category = FileCategory.IMAGE;

            when(queryFactory.select(any(NumberExpression.class))).thenReturn(countQuery);
            when(countQuery.from(fileAssetJpaEntity)).thenReturn(countQuery);
            when(countQuery.where(any(Predicate[].class))).thenReturn(countQuery);
            when(countQuery.fetchOne()).thenReturn(5L);

            // when
            long result =
                    repository.countByCriteria(
                            organizationId, tenantId, status, category, null, null, null);

            // then
            assertThat(result).isEqualTo(5L);
        }

        @Test
        @DisplayName("countByCriteria 결과가 null이면 0을 반환한다")
        void countByCriteria_WhenNull_ShouldReturnZero() {
            // given
            when(queryFactory.select(any(NumberExpression.class))).thenReturn(countQuery);
            when(countQuery.from(fileAssetJpaEntity)).thenReturn(countQuery);
            when(countQuery.where(any(Predicate[].class))).thenReturn(countQuery);
            when(countQuery.fetchOne()).thenReturn(null);

            // when
            long result =
                    repository.countByCriteria(
                            TEST_ORG_ID, TEST_TENANT_ID, null, null, null, null, null);

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("조건에 맞는 FileAsset이 없으면 0을 반환한다")
        void countByCriteria_WhenNoMatch_ShouldReturnZero() {
            // given
            when(queryFactory.select(any(NumberExpression.class))).thenReturn(countQuery);
            when(countQuery.from(fileAssetJpaEntity)).thenReturn(countQuery);
            when(countQuery.where(any(Predicate[].class))).thenReturn(countQuery);
            when(countQuery.fetchOne()).thenReturn(0L);

            // when
            long result =
                    repository.countByCriteria(
                            TEST_ORG_ID,
                            TEST_TENANT_ID,
                            FileAssetStatus.PENDING,
                            FileCategory.VIDEO,
                            null,
                            null,
                            null);

            // then
            assertThat(result).isEqualTo(0L);
        }
    }

    // ==================== Helper Methods ====================

    private FileAssetJpaEntity createEntity(String id, String organizationId, String tenantId) {
        Instant now = Instant.now();
        return FileAssetJpaEntity.of(
                id,
                "session-123",
                "test-file.jpg",
                1024 * 1024L,
                "image/jpeg",
                FileCategory.IMAGE,
                null, // imageWidth
                null, // imageHeight
                "test-bucket",
                "assets/test-file.jpg",
                "\"etag-123\"",
                TEST_TENANT_ID,
                organizationId,
                tenantId,
                FileAssetStatus.COMPLETED,
                null, // processedAt
                null, // deletedAt
                null, // lastErrorMessage
                now,
                now);
    }
}
