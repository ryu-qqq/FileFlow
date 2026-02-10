package com.ryuqq.fileflow.adapter.out.persistence.asset.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.adapter.out.persistence.asset.AssetMetadataJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.AssetMetadataJpaEntity;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadata;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadataFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("AssetMetadataJpaMapper 단위 테스트")
class AssetMetadataJpaMapperTest {

    private final AssetMetadataJpaMapper mapper = new AssetMetadataJpaMapper();

    @Nested
    @DisplayName("toEntity 메서드 테스트")
    class ToEntityTest {

        @Test
        @DisplayName("도메인 객체를 JPA 엔티티로 변환합니다")
        void toEntity_shouldMapAllFields() {
            // given
            AssetMetadata domain = AssetMetadataFixture.anImageMetadata();

            // when
            AssetMetadataJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getId()).isEqualTo(domain.idValue());
            assertThat(entity.getAssetId()).isEqualTo(domain.assetIdValue());
            assertThat(entity.getWidth()).isEqualTo(domain.width());
            assertThat(entity.getHeight()).isEqualTo(domain.height());
            assertThat(entity.getTransformType()).isNull();
        }

        @Test
        @DisplayName("변환된 메타데이터의 transformType이 매핑됩니다")
        void toEntity_withTransformType_shouldMapTransformType() {
            // given
            AssetMetadata domain = AssetMetadataFixture.aTransformedImageMetadata();

            // when
            AssetMetadataJpaEntity entity = mapper.toEntity(domain);

            // then
            assertThat(entity.getTransformType()).isEqualTo("RESIZE");
        }
    }

    @Nested
    @DisplayName("toDomain 메서드 테스트")
    class ToDomainTest {

        @Test
        @DisplayName("JPA 엔티티를 도메인 객체로 변환합니다")
        void toDomain_shouldMapAllFields() {
            // given
            AssetMetadataJpaEntity entity = AssetMetadataJpaEntityFixture.anImageMetadataEntity();

            // when
            AssetMetadata domain = mapper.toDomain(entity);

            // then
            assertThat(domain.idValue()).isEqualTo(entity.getId());
            assertThat(domain.assetIdValue()).isEqualTo(entity.getAssetId());
            assertThat(domain.width()).isEqualTo(entity.getWidth());
            assertThat(domain.height()).isEqualTo(entity.getHeight());
            assertThat(domain.transformType()).isNull();
        }
    }
}
