package com.ryuqq.fileflow.adapter.out.persistence.asset.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.asset.AssetMetadataJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.asset.condition.AssetConditionBuilder;
import com.ryuqq.fileflow.adapter.out.persistence.common.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({AssetConditionBuilder.class, AssetMetadataQueryDslRepository.class})
@DisplayName("AssetMetadataQueryDslRepository 통합 테스트")
class AssetMetadataQueryDslRepositoryTest extends AbstractRepositoryIntegrationTest {

    @Autowired private AssetMetadataQueryDslRepository queryDslRepository;

    @Autowired private AssetMetadataJpaRepository jpaRepository;

    @Nested
    @DisplayName("findByAssetId")
    class FindByAssetId {

        @Test
        @DisplayName("존재하는 assetId로 조회하면 메타데이터를 반환한다")
        void returnsMetadataWhenExists() {
            var entity = AssetMetadataJpaEntityFixture.anImageMetadataEntity();
            jpaRepository.save(entity);
            flushAndClear();

            var result = queryDslRepository.findByAssetId("asset-001");

            assertThat(result).isPresent();
            assertThat(result.get().getAssetId()).isEqualTo("asset-001");
            assertThat(result.get().getWidth()).isEqualTo(1920);
            assertThat(result.get().getHeight()).isEqualTo(1080);
        }

        @Test
        @DisplayName("존재하지 않는 assetId로 조회하면 빈 Optional을 반환한다")
        void returnsEmptyWhenNotExists() {
            var result = queryDslRepository.findByAssetId("non-existent-asset");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("transformType이 있는 메타데이터도 정상 조회된다")
        void returnsMetadataWithTransformType() {
            var entity = AssetMetadataJpaEntityFixture.aTransformedMetadataEntity();
            jpaRepository.save(entity);
            flushAndClear();

            var result = queryDslRepository.findByAssetId("asset-002");

            assertThat(result).isPresent();
            assertThat(result.get().getTransformType()).isEqualTo("RESIZE");
        }
    }
}
