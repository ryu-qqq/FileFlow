package com.ryuqq.fileflow.application.transform.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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
@DisplayName("SourceAssetValidator 단위 테스트")
class SourceAssetValidatorTest {

    @InjectMocks private SourceAssetValidator sut;
    @Mock private AssetReadManager assetReadManager;

    @Nested
    @DisplayName("validateAndGetContentType 메서드")
    class ValidateAndGetContentTypeTest {

        @Test
        @DisplayName("소스 에셋 ID로 존재 여부를 검증하고 contentType을 반환한다")
        void validateAndGetContentType_ExistingAsset_ReturnsContentType() {
            // given
            String sourceAssetId = "asset-001";
            Asset asset = AssetFixture.anAsset();

            given(assetReadManager.getAsset(sourceAssetId)).willReturn(asset);

            // when
            String result = sut.validateAndGetContentType(sourceAssetId);

            // then
            assertThat(result).isEqualTo("image/jpeg");
            then(assetReadManager).should().getAsset(sourceAssetId);
        }
    }
}
