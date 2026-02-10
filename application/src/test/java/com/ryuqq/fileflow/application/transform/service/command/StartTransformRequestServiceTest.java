package com.ryuqq.fileflow.application.transform.service.command;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.transform.internal.TransformExecutionCoordinator;
import com.ryuqq.fileflow.application.transform.validator.TransformExecutionValidator;
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
@DisplayName("StartTransformRequestService 단위 테스트")
class StartTransformRequestServiceTest {

    @InjectMocks private StartTransformRequestService sut;
    @Mock private TransformExecutionValidator transformExecutionValidator;
    @Mock private TransformExecutionCoordinator transformExecutionCoordinator;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("변환 요청을 검증하고 실행 코디네이터에 위임한다")
        void execute_ValidId_DelegatesToCoordinator() {
            // given
            String transformRequestId = "transform-001";
            TransformRequest request = TransformRequestFixture.aResizeRequest();
            Asset sourceAsset = AssetFixture.anAsset();

            given(transformExecutionValidator.getTransformRequest(transformRequestId))
                    .willReturn(request);
            given(transformExecutionValidator.getSourceAsset(request.sourceAssetIdValue()))
                    .willReturn(sourceAsset);

            // when
            sut.execute(transformRequestId);

            // then
            then(transformExecutionValidator).should().getTransformRequest(transformRequestId);
            then(transformExecutionValidator).should().getSourceAsset(request.sourceAssetIdValue());
            then(transformExecutionCoordinator).should().execute(request, sourceAsset);
        }
    }
}
