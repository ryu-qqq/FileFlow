package com.ryuqq.fileflow.application.asset.manager.command;

import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.asset.port.out.command.AssetPersistencePort;
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
@DisplayName("AssetCommandManager 단위 테스트")
class AssetCommandManagerTest {

    @InjectMocks private AssetCommandManager sut;
    @Mock private AssetPersistencePort assetPersistencePort;

    @Nested
    @DisplayName("persist 메서드")
    class PersistTest {

        @Test
        @DisplayName("Asset을 영속화 포트에 위임한다")
        void persist_Asset_DelegatesToPort() {
            // given
            Asset asset = AssetFixture.anAsset();

            // when
            sut.persist(asset);

            // then
            then(assetPersistencePort).should().persist(asset);
        }
    }
}
