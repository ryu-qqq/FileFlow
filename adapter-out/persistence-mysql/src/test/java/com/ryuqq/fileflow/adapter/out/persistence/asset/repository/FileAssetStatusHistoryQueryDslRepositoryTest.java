package com.ryuqq.fileflow.adapter.out.persistence.asset.repository;

import static com.ryuqq.fileflow.adapter.out.persistence.asset.entity.QFileAssetStatusHistoryJpaEntity.fileAssetStatusHistoryJpaEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.FileAssetStatusHistoryJpaEntity;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("FileAssetStatusHistoryQueryDslRepository 단위 테스트")
@ExtendWith(MockitoExtension.class)
class FileAssetStatusHistoryQueryDslRepositoryTest {

    @Mock private JPAQueryFactory queryFactory;

    @Mock private JPAQuery<FileAssetStatusHistoryJpaEntity> jpaQuery;

    private FileAssetStatusHistoryQueryDslRepository repository;

    @BeforeEach
    void setUp() {
        repository = new FileAssetStatusHistoryQueryDslRepository(queryFactory);
    }

    @Nested
    @DisplayName("findByFileAssetId 테스트")
    class FindByFileAssetIdTest {

        @Test
        @DisplayName("FileAsset ID로 상태 변경 이력 목록을 조회할 수 있다")
        void findByFileAssetId_WithValidId_ShouldReturnEntities() {
            // given
            String fileAssetId = "asset-123";
            List<FileAssetStatusHistoryJpaEntity> entities =
                    List.of(
                            createEntity(fileAssetId, FileAssetStatus.PENDING, FileAssetStatus.PROCESSING),
                            createEntity(fileAssetId, FileAssetStatus.PROCESSING, FileAssetStatus.COMPLETED));

            when(queryFactory.selectFrom(fileAssetStatusHistoryJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
            when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
            when(jpaQuery.fetch()).thenReturn(entities);

            // when
            List<FileAssetStatusHistoryJpaEntity> result = repository.findByFileAssetId(fileAssetId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getFileAssetId()).isEqualTo(fileAssetId);
        }

        @Test
        @DisplayName("FileAsset ID에 해당하는 이력이 없으면 빈 목록을 반환한다")
        void findByFileAssetId_WhenNotFound_ShouldReturnEmptyList() {
            // given
            when(queryFactory.selectFrom(fileAssetStatusHistoryJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
            when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
            when(jpaQuery.fetch()).thenReturn(List.of());

            // when
            List<FileAssetStatusHistoryJpaEntity> result = repository.findByFileAssetId("not-exist");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findLatestByFileAssetId 테스트")
    class FindLatestByFileAssetIdTest {

        @Test
        @DisplayName("FileAsset ID로 최신 상태 변경 이력을 조회할 수 있다")
        void findLatestByFileAssetId_WithValidId_ShouldReturnEntity() {
            // given
            String fileAssetId = "asset-123";
            FileAssetStatusHistoryJpaEntity entity =
                    createEntity(fileAssetId, FileAssetStatus.PROCESSING, FileAssetStatus.COMPLETED);

            when(queryFactory.selectFrom(fileAssetStatusHistoryJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
            when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
            when(jpaQuery.fetchFirst()).thenReturn(entity);

            // when
            Optional<FileAssetStatusHistoryJpaEntity> result =
                    repository.findLatestByFileAssetId(fileAssetId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getFileAssetId()).isEqualTo(fileAssetId);
            assertThat(result.get().getToStatus()).isEqualTo(FileAssetStatus.COMPLETED);
        }

        @Test
        @DisplayName("FileAsset ID에 해당하는 이력이 없으면 empty Optional을 반환한다")
        void findLatestByFileAssetId_WhenNotFound_ShouldReturnEmpty() {
            // given
            when(queryFactory.selectFrom(fileAssetStatusHistoryJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
            when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
            when(jpaQuery.fetchFirst()).thenReturn(null);

            // when
            Optional<FileAssetStatusHistoryJpaEntity> result =
                    repository.findLatestByFileAssetId("not-exist");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findExceedingSla 테스트")
    class FindExceedingSlaTest {

        @Test
        @DisplayName("SLA 초과 상태 변경 이력을 조회할 수 있다")
        void findExceedingSla_WithValidParams_ShouldReturnEntities() {
            // given
            long slaMillis = 5000L;
            int limit = 10;
            List<FileAssetStatusHistoryJpaEntity> entities =
                    List.of(
                            createEntityWithDuration("asset-1", 6000L),
                            createEntityWithDuration("asset-2", 7000L));

            when(queryFactory.selectFrom(fileAssetStatusHistoryJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
            when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
            when(jpaQuery.limit(anyLong())).thenReturn(jpaQuery);
            when(jpaQuery.fetch()).thenReturn(entities);

            // when
            List<FileAssetStatusHistoryJpaEntity> result =
                    repository.findExceedingSla(slaMillis, limit);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getDurationMillis()).isGreaterThan(slaMillis);
        }

        @Test
        @DisplayName("SLA 초과 이력이 없으면 빈 목록을 반환한다")
        void findExceedingSla_WhenNoExceeding_ShouldReturnEmptyList() {
            // given
            when(queryFactory.selectFrom(fileAssetStatusHistoryJpaEntity)).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
            when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
            when(jpaQuery.limit(anyLong())).thenReturn(jpaQuery);
            when(jpaQuery.fetch()).thenReturn(List.of());

            // when
            List<FileAssetStatusHistoryJpaEntity> result = repository.findExceedingSla(1000L, 10);

            // then
            assertThat(result).isEmpty();
        }
    }

    // ==================== Helper Methods ====================

    private FileAssetStatusHistoryJpaEntity createEntity(
            String fileAssetId, FileAssetStatus fromStatus, FileAssetStatus toStatus) {
        return FileAssetStatusHistoryJpaEntity.of(
                UUID.randomUUID(),
                fileAssetId,
                fromStatus,
                toStatus,
                "Status changed",
                "system",
                "SYSTEM",
                Instant.now(),
                1000L,
                Instant.now());
    }

    private FileAssetStatusHistoryJpaEntity createEntityWithDuration(
            String fileAssetId, Long durationMillis) {
        return FileAssetStatusHistoryJpaEntity.of(
                UUID.randomUUID(),
                fileAssetId,
                FileAssetStatus.PENDING,
                FileAssetStatus.COMPLETED,
                "Status changed",
                "system",
                "SYSTEM",
                Instant.now(),
                durationMillis,
                Instant.now());
    }
}
