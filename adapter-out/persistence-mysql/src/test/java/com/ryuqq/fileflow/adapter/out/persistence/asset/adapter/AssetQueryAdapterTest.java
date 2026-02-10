package com.ryuqq.fileflow.adapter.out.persistence.asset.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.adapter.out.persistence.asset.AssetJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.AssetJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.asset.mapper.AssetJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.AssetQueryDslRepository;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetFixture;
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
@DisplayName("AssetQueryAdapter 단위 테스트")
class AssetQueryAdapterTest {

    @InjectMocks private AssetQueryAdapter queryAdapter;
    @Mock private AssetQueryDslRepository queryDslRepository;
    @Mock private AssetJpaMapper mapper;

    @Nested
    @DisplayName("findById 메서드 테스트")
    class FindByIdTest {

        @Test
        @DisplayName("존재하는 ID로 조회하면 도메인 객체를 반환합니다")
        void findById_existingId_shouldReturnDomain() {
            // given
            AssetId assetId = AssetId.of("asset-001");
            AssetJpaEntity entity = AssetJpaEntityFixture.anAssetEntity();
            Asset domain = AssetFixture.anAsset();

            given(queryDslRepository.findById(assetId.value())).willReturn(Optional.of(entity));
            given(mapper.toDomain(entity)).willReturn(domain);

            // when
            Optional<Asset> result = queryAdapter.findById(assetId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().idValue()).isEqualTo(assetId.value());
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 빈 Optional을 반환합니다")
        void findById_nonExistingId_shouldReturnEmpty() {
            // given
            AssetId assetId = AssetId.of("non-existing-id");
            given(queryDslRepository.findById(assetId.value())).willReturn(Optional.empty());

            // when
            Optional<Asset> result = queryAdapter.findById(assetId);

            // then
            assertThat(result).isEmpty();
            then(mapper).shouldHaveNoInteractions();
        }
    }
}
