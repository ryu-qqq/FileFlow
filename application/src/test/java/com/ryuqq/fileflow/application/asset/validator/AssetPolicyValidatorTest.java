package com.ryuqq.fileflow.application.asset.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.ryuqq.fileflow.application.asset.manager.query.AssetReadManager;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetFixture;
import com.ryuqq.fileflow.domain.asset.exception.AssetAccessDeniedException;
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
@DisplayName("AssetPolicyValidator 단위 테스트")
class AssetPolicyValidatorTest {

    @InjectMocks private AssetPolicyValidator sut;
    @Mock private AssetReadManager assetReadManager;

    @Nested
    @DisplayName("validateCanDelete 메서드")
    class ValidateCanDeleteTest {

        @Test
        @DisplayName("동일한 source로 요청하면 Asset을 반환한다")
        void validateCanDelete_SameSource_ReturnsAsset() {
            // given
            String assetId = "asset-001";
            String source = "commerce-service";
            Asset asset = AssetFixture.anAsset();

            given(assetReadManager.getAsset(assetId)).willReturn(asset);

            // when
            Asset result = sut.validateCanDelete(assetId, source);

            // then
            assertThat(result).isEqualTo(asset);
        }

        @Test
        @DisplayName("다른 source로 요청하면 AssetAccessDeniedException을 던진다")
        void validateCanDelete_DifferentSource_ThrowsAccessDeniedException() {
            // given
            String assetId = "asset-001";
            String requestSource = "other-service";
            Asset asset = AssetFixture.anAsset();

            given(assetReadManager.getAsset(assetId)).willReturn(asset);

            // when & then
            assertThatThrownBy(() -> sut.validateCanDelete(assetId, requestSource))
                    .isInstanceOf(AssetAccessDeniedException.class);
        }
    }
}
