package com.ryuqq.fileflow.application.asset.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.asset.assembler.AssetMetadataAssembler;
import com.ryuqq.fileflow.application.asset.dto.response.AssetMetadataResponse;
import com.ryuqq.fileflow.application.asset.manager.query.AssetMetadataReadManager;
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
@DisplayName("GetAssetMetadataService 단위 테스트")
class GetAssetMetadataServiceTest {

    @InjectMocks private GetAssetMetadataService sut;
    @Mock private AssetMetadataReadManager assetMetadataReadManager;
    @Mock private AssetMetadataAssembler assetMetadataAssembler;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("Asset ID로 AssetMetadata를 조회하고 응답으로 변환한다")
        void execute_ValidAssetId_ReturnsAssetMetadataResponse() {
            // given
            String assetId = "asset-001";
            AssetMetadata metadata = AssetMetadataFixture.anImageMetadata();
            AssetMetadataResponse expectedResponse =
                    new AssetMetadataResponse(
                            metadata.idValue(),
                            metadata.assetIdValue(),
                            metadata.width(),
                            metadata.height(),
                            metadata.transformType(),
                            metadata.createdAt());

            given(assetMetadataReadManager.getAssetMetadata(assetId)).willReturn(metadata);
            given(assetMetadataAssembler.toResponse(metadata)).willReturn(expectedResponse);

            // when
            AssetMetadataResponse result = sut.execute(assetId);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(assetMetadataReadManager).should().getAssetMetadata(assetId);
            then(assetMetadataAssembler).should().toResponse(metadata);
        }
    }
}
