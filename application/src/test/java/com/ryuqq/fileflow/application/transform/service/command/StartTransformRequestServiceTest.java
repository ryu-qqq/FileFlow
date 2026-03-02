package com.ryuqq.fileflow.application.transform.service.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.fileflow.application.common.time.TimeProvider;
import com.ryuqq.fileflow.application.transform.internal.TransformExecutionCoordinator;
import com.ryuqq.fileflow.application.transform.manager.command.TransformCommandManager;
import com.ryuqq.fileflow.application.transform.validator.TransformExecutionValidator;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetFixture;
import com.ryuqq.fileflow.domain.asset.exception.AssetNotFoundException;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequestFixture;
import java.time.Instant;
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

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @InjectMocks private StartTransformRequestService sut;
    @Mock private TransformExecutionValidator transformExecutionValidator;
    @Mock private TransformExecutionCoordinator transformExecutionCoordinator;
    @Mock private TransformCommandManager transformCommandManager;
    @Mock private TimeProvider timeProvider;

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

        @Test
        @DisplayName("이미 COMPLETED 상태이면 처리를 건너뛴다")
        void execute_AlreadyCompleted_Skips() {
            // given
            String transformRequestId = "transform-001";
            TransformRequest request = TransformRequestFixture.aCompletedRequest();

            given(transformExecutionValidator.getTransformRequest(transformRequestId))
                    .willReturn(request);

            // when
            sut.execute(transformRequestId);

            // then
            then(transformExecutionCoordinator).should(never()).execute(any(), any());
        }

        @Test
        @DisplayName("이미 FAILED 상태이면 처리를 건너뛴다")
        void execute_AlreadyFailed_Skips() {
            // given
            String transformRequestId = "transform-001";
            TransformRequest request = TransformRequestFixture.aFailedRequest();

            given(transformExecutionValidator.getTransformRequest(transformRequestId))
                    .willReturn(request);

            // when
            sut.execute(transformRequestId);

            // then
            then(transformExecutionCoordinator).should(never()).execute(any(), any());
        }

        @Test
        @DisplayName("소스 에셋 조회 실패 시 변환 요청을 FAILED로 마킹하고 예외를 던진다")
        void execute_SourceAssetNotFound_FailsRequestAndThrows() {
            // given
            String transformRequestId = "transform-001";
            TransformRequest request = TransformRequestFixture.aResizeRequest();

            given(transformExecutionValidator.getTransformRequest(transformRequestId))
                    .willReturn(request);
            given(transformExecutionValidator.getSourceAsset(request.sourceAssetIdValue()))
                    .willThrow(new AssetNotFoundException(request.sourceAssetIdValue()));
            given(timeProvider.now()).willReturn(NOW);

            // when & then
            assertThatThrownBy(() -> sut.execute(transformRequestId))
                    .isInstanceOf(AssetNotFoundException.class);

            then(transformCommandManager).should().persist(request);
        }
    }
}
