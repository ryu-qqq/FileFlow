package com.ryuqq.fileflow.adapter.out.persistence.asset.adapter;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.adapter.out.persistence.asset.AssetMetadataJpaEntityFixture;
import com.ryuqq.fileflow.adapter.out.persistence.asset.entity.AssetMetadataJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.asset.mapper.AssetMetadataJpaMapper;
import com.ryuqq.fileflow.adapter.out.persistence.asset.repository.AssetMetadataJpaRepository;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadata;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadataFixture;
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
@DisplayName("AssetMetadataCommandAdapter 단위 테스트")
class AssetMetadataCommandAdapterTest {

    @InjectMocks private AssetMetadataCommandAdapter commandAdapter;
    @Mock private AssetMetadataJpaRepository jpaRepository;
    @Mock private AssetMetadataJpaMapper mapper;

    @Nested
    @DisplayName("persist 메서드 테스트")
    class PersistTest {

        @Test
        @DisplayName("도메인 객체를 엔티티로 변환하여 저장합니다")
        void persist_shouldMapAndSave() {
            // given
            AssetMetadata metadata = AssetMetadataFixture.anImageMetadata();
            AssetMetadataJpaEntity entity = AssetMetadataJpaEntityFixture.anImageMetadataEntity();
            given(mapper.toEntity(metadata)).willReturn(entity);

            // when
            commandAdapter.persist(metadata);

            // then
            then(mapper).should().toEntity(metadata);
            then(jpaRepository).should().save(entity);
        }
    }
}
