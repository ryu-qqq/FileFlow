package com.ryuqq.fileflow.application.asset.validator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.asset.manager.query.AssetReadManager;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetFixture;
import com.ryuqq.fileflow.domain.asset.exception.AssetNotFoundException;
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
@DisplayName("AssetExistenceValidator 단위 테스트")
class AssetExistenceValidatorTest {

    @InjectMocks private AssetExistenceValidator sut;
    @Mock private AssetReadManager assetReadManager;

    @Nested
    @DisplayName("validateExists 메서드")
    class ValidateExistsTest {

        @Test
        @DisplayName("존재하는 Asset ID는 예외 없이 통과한다")
        void validateExists_ExistingAsset_NoException() {
            // given
            String assetId = "asset-001";
            given(assetReadManager.getAsset(assetId)).willReturn(AssetFixture.anAsset());

            // when & then
            assertThatCode(() -> sut.validateExists(assetId)).doesNotThrowAnyException();
            then(assetReadManager).should().getAsset(assetId);
        }

        @Test
        @DisplayName("존재하지 않는 Asset ID는 AssetNotFoundException을 던진다")
        void validateExists_NonExistingAsset_ThrowsAssetNotFoundException() {
            // given
            String assetId = "non-existing-asset";
            given(assetReadManager.getAsset(assetId))
                    .willThrow(new AssetNotFoundException(assetId));

            // when & then
            assertThatThrownBy(() -> sut.validateExists(assetId))
                    .isInstanceOf(AssetNotFoundException.class);
        }
    }
}
