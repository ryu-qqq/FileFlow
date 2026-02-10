package com.ryuqq.fileflow.application.asset.manager.command;

import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.asset.port.out.command.AssetMetadataPersistencePort;
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
@DisplayName("AssetMetadataCommandManager 단위 테스트")
class AssetMetadataCommandManagerTest {

    @InjectMocks private AssetMetadataCommandManager sut;
    @Mock private AssetMetadataPersistencePort assetMetadataPersistencePort;

    @Nested
    @DisplayName("persist 메서드")
    class PersistTest {

        @Test
        @DisplayName("AssetMetadata를 영속화 포트에 위임한다")
        void persist_AssetMetadata_DelegatesToPort() {
            // given
            AssetMetadata metadata = AssetMetadataFixture.anImageMetadata();

            // when
            sut.persist(metadata);

            // then
            then(assetMetadataPersistencePort).should().persist(metadata);
        }
    }
}
