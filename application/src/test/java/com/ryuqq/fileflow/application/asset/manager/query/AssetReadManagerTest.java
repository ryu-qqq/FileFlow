package com.ryuqq.fileflow.application.asset.manager.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.ryuqq.fileflow.application.asset.port.out.query.AssetQueryPort;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetFixture;
import com.ryuqq.fileflow.domain.asset.exception.AssetNotFoundException;
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
@DisplayName("AssetReadManager 단위 테스트")
class AssetReadManagerTest {

    @InjectMocks private AssetReadManager sut;
    @Mock private AssetQueryPort assetQueryPort;

    @Nested
    @DisplayName("getAsset 메서드")
    class GetAssetTest {

        @Test
        @DisplayName("존재하는 Asset ID로 Asset을 반환한다")
        void getAsset_ExistingId_ReturnsAsset() {
            // given
            String assetId = "asset-001";
            Asset expectedAsset = AssetFixture.anAsset();

            given(assetQueryPort.findById(AssetId.of(assetId)))
                    .willReturn(Optional.of(expectedAsset));

            // when
            Asset result = sut.getAsset(assetId);

            // then
            assertThat(result).isEqualTo(expectedAsset);
        }

        @Test
        @DisplayName("존재하지 않는 Asset ID로 AssetNotFoundException을 던진다")
        void getAsset_NonExistingId_ThrowsAssetNotFoundException() {
            // given
            String assetId = "non-existing-asset";

            given(assetQueryPort.findById(AssetId.of(assetId))).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.getAsset(assetId))
                    .isInstanceOf(AssetNotFoundException.class);
        }
    }
}
