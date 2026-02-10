package com.ryuqq.fileflow.application.asset.manager.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.ryuqq.fileflow.application.asset.port.out.query.AssetMetadataQueryPort;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadata;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetMetadataFixture;
import com.ryuqq.fileflow.domain.asset.exception.AssetMetadataNotFoundException;
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
@DisplayName("AssetMetadataReadManager 단위 테스트")
class AssetMetadataReadManagerTest {

    @InjectMocks private AssetMetadataReadManager sut;
    @Mock private AssetMetadataQueryPort assetMetadataQueryPort;

    @Nested
    @DisplayName("getAssetMetadata 메서드")
    class GetAssetMetadataTest {

        @Test
        @DisplayName("존재하는 Asset ID로 AssetMetadata를 반환한다")
        void getAssetMetadata_ExistingId_ReturnsMetadata() {
            // given
            String assetId = "asset-001";
            AssetMetadata expectedMetadata = AssetMetadataFixture.anImageMetadata();

            given(assetMetadataQueryPort.findByAssetId(AssetId.of(assetId)))
                    .willReturn(Optional.of(expectedMetadata));

            // when
            AssetMetadata result = sut.getAssetMetadata(assetId);

            // then
            assertThat(result).isEqualTo(expectedMetadata);
        }

        @Test
        @DisplayName("존재하지 않는 Asset ID로 AssetMetadataNotFoundException을 던진다")
        void getAssetMetadata_NonExistingId_ThrowsAssetMetadataNotFoundException() {
            // given
            String assetId = "non-existing-asset";

            given(assetMetadataQueryPort.findByAssetId(AssetId.of(assetId)))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.getAssetMetadata(assetId))
                    .isInstanceOf(AssetMetadataNotFoundException.class);
        }
    }
}
