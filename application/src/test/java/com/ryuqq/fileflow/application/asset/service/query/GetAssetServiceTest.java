package com.ryuqq.fileflow.application.asset.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.asset.assembler.AssetAssembler;
import com.ryuqq.fileflow.application.asset.dto.response.AssetResponse;
import com.ryuqq.fileflow.application.asset.manager.query.AssetReadManager;
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
@DisplayName("GetAssetService 단위 테스트")
class GetAssetServiceTest {

    @InjectMocks private GetAssetService sut;
    @Mock private AssetReadManager assetReadManager;
    @Mock private AssetAssembler assetAssembler;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("Asset ID로 Asset을 조회하고 응답으로 변환한다")
        void execute_ValidAssetId_ReturnsAssetResponse() {
            // given
            String assetId = "asset-001";
            Asset asset = AssetFixture.anAsset();
            AssetResponse expectedResponse =
                    new AssetResponse(
                            asset.idValue(),
                            asset.s3Key(),
                            asset.bucket(),
                            asset.accessType(),
                            asset.fileName(),
                            asset.fileSize(),
                            asset.contentType(),
                            asset.etag(),
                            asset.extension(),
                            asset.origin(),
                            asset.originId(),
                            asset.purpose(),
                            asset.source(),
                            asset.createdAt());

            given(assetReadManager.getAsset(assetId)).willReturn(asset);
            given(assetAssembler.toResponse(asset)).willReturn(expectedResponse);

            // when
            AssetResponse result = sut.execute(assetId);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(assetReadManager).should().getAsset(assetId);
            then(assetAssembler).should().toResponse(asset);
        }
    }
}
