package com.ryuqq.fileflow.application.transform.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.asset.manager.query.AssetReadManager;
import com.ryuqq.fileflow.application.transform.manager.query.TransformReadManager;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetFixture;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequestFixture;
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
@DisplayName("TransformExecutionValidator 단위 테스트")
class TransformExecutionValidatorTest {

    @InjectMocks private TransformExecutionValidator sut;
    @Mock private TransformReadManager transformReadManager;
    @Mock private AssetReadManager assetReadManager;

    @Nested
    @DisplayName("getTransformRequest 메서드")
    class GetTransformRequestTest {

        @Test
        @DisplayName("변환 요청 ID로 TransformRequest를 조회하여 반환한다")
        void getTransformRequest_ExistingId_ReturnsRequest() {
            // given
            String transformRequestId = "transform-001";
            TransformRequest expectedRequest = TransformRequestFixture.aResizeRequest();

            given(transformReadManager.getTransformRequest(transformRequestId))
                    .willReturn(expectedRequest);

            // when
            TransformRequest result = sut.getTransformRequest(transformRequestId);

            // then
            assertThat(result).isEqualTo(expectedRequest);
            then(transformReadManager).should().getTransformRequest(transformRequestId);
        }
    }

    @Nested
    @DisplayName("getSourceAsset 메서드")
    class GetSourceAssetTest {

        @Test
        @DisplayName("에셋 ID로 소스 에셋을 조회하여 반환한다")
        void getSourceAsset_ExistingId_ReturnsAsset() {
            // given
            String assetId = "asset-001";
            Asset expectedAsset = AssetFixture.anAsset();

            given(assetReadManager.getAsset(assetId)).willReturn(expectedAsset);

            // when
            Asset result = sut.getSourceAsset(assetId);

            // then
            assertThat(result).isEqualTo(expectedAsset);
            then(assetReadManager).should().getAsset(assetId);
        }
    }
}
