package com.ryuqq.fileflow.adapter.out.persistence.asset.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.adapter.out.persistence.asset.AssetMetadataJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.AssetMetadataJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.asset.mapper.AssetMetadataJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.AssetMetadataQueryDslRepository;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadata;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadataFixture;
import com.ryuqq.fileflow.domain.asset.id.AssetId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("AssetMetadataQueryAdapter 단위 테스트")
class AssetMetadataQueryAdapterTest {

    @InjectMocks private AssetMetadataQueryAdapter queryAdapter;
    @Mock private AssetMetadataQueryDslRepository queryDslRepository;
    @Mock private AssetMetadataJpaMapper mapper;

    @Nested
    @DisplayName("findByAssetId 메서드 테스트")
    class FindByAssetIdTest {

        @Test
        @DisplayName("존재하는 assetId로 조회하면 도메인 객체를 반환합니다")
        void findByAssetId_existingId_shouldReturnDomain() {
            // given
            AssetId assetId = AssetId.of("asset-001");
            AssetMetadataJpaEntity entity = AssetMetadataJpaEntityFixture.anImageMetadataEntity();
            AssetMetadata domain = AssetMetadataFixture.anImageMetadata();

            given(queryDslRepository.findByAssetId(assetId.value()))
                    .willReturn(Optional.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // when
            Optional<AssetMetadata> result = queryAdapter.findByAssetId(assetId);

            // then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("존재하지 않는 assetId로 조회하면 빈 Optional을 반환합니다")
        void findByAssetId_nonExistingId_shouldReturnEmpty() {
            // given
            AssetId assetId = AssetId.of("non-existing-id");
            given(queryDslRepository.findByAssetId(assetId.value())).willReturn(Optional.empty());

            // when
            Optional<AssetMetadata> result = queryAdapter.findByAssetId(assetId);

            // then
            assertThat(result).isEmpty();
            then(mapper).shouldHaveNoInteractions();
        }
    }
}
