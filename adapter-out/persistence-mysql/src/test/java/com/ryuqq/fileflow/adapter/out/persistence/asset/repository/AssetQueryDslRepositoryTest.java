package com.ryuqq.fileflow.adapter.out.persistence.asset.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.asset.AssetJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.asset.condition.AssetConditionBuilder;
import com.ryuqq.fileflow.adapter.out.persistence.common.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({AssetConditionBuilder.class, AssetQueryDslRepository.class})
@DisplayName("AssetQueryDslRepository 통합 테스트")
class AssetQueryDslRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired private AssetQueryDslRepository queryDslRepository;

    @Autowired private AssetJpaRepository jpaRepository;

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("존재하는 ID로 조회하면 엔티티를 반환한다")
        void returnsEntityWhenExists() {
            var entity = AssetJpaEntityFixture.anAssetEntity();
            jpaRepository.save(entity);
            flushAndClear();

            var result = queryDslRepository.findById("asset-001");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo("asset-001");
            assertThat(result.get().getFileName()).isEqualTo(entity.getFileName());
            assertThat(result.get().getContentType()).isEqualTo(entity.getContentType());
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional을 반환한다")
        void returnsEmptyWhenNotExists() {
            var result = queryDslRepository.findById("non-existent-id");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("soft-deleted 상태의 Asset은 조회되지 않는다")
        void excludesSoftDeletedAsset() {
            var deletedEntity = AssetJpaEntityFixture.aDeletedAssetEntity();
            jpaRepository.save(deletedEntity);
            flushAndClear();

            var result = queryDslRepository.findById("asset-del-001");

            assertThat(result).isEmpty();
        }
    }
}
