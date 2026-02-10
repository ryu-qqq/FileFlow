package com.ryuqq.fileflow.adapter.out.persistence.asset.adapter;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.adapter.out.persistence.asset.AssetJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.AssetJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.asset.mapper.AssetJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.AssetJpaRepository;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetFixture;
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
@DisplayName("AssetCommandAdapter 단위 테스트")
class AssetCommandAdapterTest {

    @InjectMocks private AssetCommandAdapter commandAdapter;
    @Mock private AssetJpaRepository jpaRepository;
    @Mock private AssetJpaMapper mapper;

    @Nested
    @DisplayName("persist 메서드 테스트")
    class PersistTest {

        @Test
        @DisplayName("도메인 객체를 엔티티로 변환하여 저장합니다")
        void persist_shouldMapAndSave() {
            // given
            Asset asset = AssetFixture.anAsset();
            AssetJpaEntity entity = AssetJpaEntityFixture.anAssetEntity();
            given(mapper.toEntity(asset)).willReturn(entity);

            // when
            commandAdapter.persist(asset);

            // then
            then(mapper).should().toEntity(asset);
            then(jpaRepository).should().save(entity);
        }
    }
}
