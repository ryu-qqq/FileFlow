package com.ryuqq.fileflow.adapter.out.persistence.asset.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.asset.entity.QProcessedFileAssetJpaEntity.processedFileAssetJpaEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.ProcessedFileAssetJpaEntity;
import com.ryuqq.fileflow.domain.asset.vo.ImageFormatType;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariantType;
import com.ryuqq.fileflow.domain.iam.vo.OrganizationId;
import com.ryuqq.fileflow.domain.iam.vo.TenantId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("ProcessedFileAssetQueryDslRepository 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ProcessedFileAssetQueryDslRepositoryTest {

    private static final String TEST_TENANT_ID = TenantId.generate().value();
    private static final String TEST_ORG_ID = OrganizationId.generate().value();

    @Mock private JPAQueryFactory queryFactory;

    @Mock private JPAQuery<ProcessedFileAssetJpaEntity> jpaQuery;

    private ProcessedFileAssetQueryDslRepository repository;

    @BeforeEach
    void setUp() {
        repository = new ProcessedFileAssetQueryDslRepository(queryFactory);
    }

    @Nested
    @DisplayName("findByOriginalAssetId 테스트")
    class FindByOriginalAssetIdTest {

        @Test
        @DisplayName("원본 FileAsset ID로 ProcessedFileAsset 목록을 조회할 수 있다")
        void findByOriginalAssetId_WithValidId_ShouldReturnEntities() {
            // given
            String originalAssetId = "original-asset-123";
            List<ProcessedFileAssetJpaEntity> entities =
                    List.of(
                            createEntity(originalAssetId, null),
                            createEntity(originalAssetId, null));

            when(queryFactory.selectFrom(processedFileAssetJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
            when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
            when(jpaQuery.fetch()).thenReturn(entities);

            // when
            List<ProcessedFileAssetJpaEntity> result =
                    repository.findByOriginalAssetId(originalAssetId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getOriginalAssetId()).isEqualTo(originalAssetId);
        }

        @Test
        @DisplayName("원본 FileAsset ID에 해당하는 결과가 없으면 빈 목록을 반환한다")
        void findByOriginalAssetId_WhenNotFound_ShouldReturnEmptyList() {
            // given
            when(queryFactory.selectFrom(processedFileAssetJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
            when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
            when(jpaQuery.fetch()).thenReturn(List.of());

            // when
            List<ProcessedFileAssetJpaEntity> result =
                    repository.findByOriginalAssetId("not-exist");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByParentAssetId 테스트")
    class FindByParentAssetIdTest {

        @Test
        @DisplayName("부모 ProcessedFileAsset ID로 하위 목록을 조회할 수 있다")
        void findByParentAssetId_WithValidId_ShouldReturnEntities() {
            // given
            String parentAssetId = "parent-asset-123";
            List<ProcessedFileAssetJpaEntity> entities =
                    List.of(
                            createEntity("original-1", parentAssetId),
                            createEntity("original-2", parentAssetId));

            when(queryFactory.selectFrom(processedFileAssetJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
            when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
            when(jpaQuery.fetch()).thenReturn(entities);

            // when
            List<ProcessedFileAssetJpaEntity> result =
                    repository.findByParentAssetId(parentAssetId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getParentAssetId()).isEqualTo(parentAssetId);
        }

        @Test
        @DisplayName("부모 ProcessedFileAsset ID에 해당하는 결과가 없으면 빈 목록을 반환한다")
        void findByParentAssetId_WhenNotFound_ShouldReturnEmptyList() {
            // given
            when(queryFactory.selectFrom(processedFileAssetJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
            when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
            when(jpaQuery.fetch()).thenReturn(List.of());

            // when
            List<ProcessedFileAssetJpaEntity> result =
                    repository.findByParentAssetId("not-exist");

            // then
            assertThat(result).isEmpty();
        }
    }

    // ==================== Helper Methods ====================

    private ProcessedFileAssetJpaEntity createEntity(String originalAssetId, String parentAssetId) {
        return ProcessedFileAssetJpaEntity.of(
                UUID.randomUUID(),
                originalAssetId,
                parentAssetId,
                ImageVariantType.ORIGINAL,
                ImageFormatType.JPEG,
                "test-file.jpg",
                1024L,
                "test-bucket",
                "processed/test-file.jpg",
                "user-123",
                TEST_ORG_ID,
                TEST_TENANT_ID,
                Instant.now());
    }
}
