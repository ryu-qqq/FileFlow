package com.ryuqq.fileflow.application.transform.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;

import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.transform.dto.bundle.TransformCompletionBundle;
import com.ryuqq.fileflow.application.transform.dto.bundle.TransformFailureBundle;
import com.ryuqq.fileflow.application.transform.dto.result.ImageTransformResult;
import com.ryuqq.fileflow.application.transform.factory.command.TransformCommandFactory;
import com.ryuqq.fileflow.application.transform.manager.command.TransformCommandManager;
import com.ryuqq.fileflow.application.transform.manager.query.TransformReadManager;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetFixture;
import com.ryuqq.fileflow.domain.asset.vo.FileInfo;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequestFixture;
import com.ryuqq.fileflow.domain.transform.vo.ImageDimension;
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
@DisplayName("TransformExecutionCoordinator 단위 테스트")
class TransformExecutionCoordinatorTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @InjectMocks private TransformExecutionCoordinator sut;
    @Mock private TransformCommandFactory transformCommandFactory;
    @Mock private ImageTransformFacade imageTransformFacade;
    @Mock private TransformCommandManager transformCommandManager;
    @Mock private TransformReadManager transformReadManager;
    @Mock private TransformCompletionFacade transformCompletionFacade;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("변환 성공 시 start -> transform -> complete 흐름을 수행한다")
        void execute_TransformSuccess_CompletesSuccessfully() {
            // given
            TransformRequest request = TransformRequestFixture.aResizeRequest();
            Asset sourceAsset = AssetFixture.anAsset();

            StatusChangeContext<String> startContext =
                    new StatusChangeContext<>(request.idValue(), NOW);
            given(transformCommandFactory.createStartContext(request.idValue()))
                    .willReturn(startContext);

            FileInfo fileInfo =
                    FileInfo.of("resized.jpg", 2048L, "image/jpeg", "etag-resized", "jpg");
            ImageDimension dimension = ImageDimension.of(800, 600);
            ImageTransformResult successResult =
                    ImageTransformResult.success(
                            "result/resized.jpg", "fileflow-bucket", fileInfo, dimension);

            given(imageTransformFacade.transform(sourceAsset, request)).willReturn(successResult);

            Instant completedAt = NOW.plusSeconds(30);
            Asset resultAsset = AssetFixture.anAssetWithId("result-001");
            TransformCompletionBundle completionBundle =
                    new TransformCompletionBundle(resultAsset, request, dimension, completedAt);
            given(
                            transformCommandFactory.createCompletionBundle(
                                    successResult, request, sourceAsset))
                    .willReturn(completionBundle);

            // when
            sut.execute(request, sourceAsset);

            // then
            then(transformCommandManager).should().persist(request);
            then(imageTransformFacade).should().transform(sourceAsset, request);
            then(transformCompletionFacade).should().complete(completionBundle);
        }

        @Test
        @DisplayName("변환 실패 시 start -> transform -> fail 흐름을 수행한다")
        void execute_TransformFailure_HandlesFail() {
            // given
            TransformRequest request = TransformRequestFixture.aResizeRequest();
            Asset sourceAsset = AssetFixture.anAsset();

            StatusChangeContext<String> startContext =
                    new StatusChangeContext<>(request.idValue(), NOW);
            given(transformCommandFactory.createStartContext(request.idValue()))
                    .willReturn(startContext);

            ImageTransformResult failureResult =
                    ImageTransformResult.failure("Image processing failed");
            given(imageTransformFacade.transform(sourceAsset, request)).willReturn(failureResult);

            TransformFailureBundle failureBundle =
                    new TransformFailureBundle(request, "Image processing failed", NOW);
            given(transformCommandFactory.createFailureBundle(request, failureResult))
                    .willReturn(failureBundle);

            // when
            sut.execute(request, sourceAsset);

            // then
            then(transformCommandManager).should().persist(request);
            then(imageTransformFacade).should().transform(sourceAsset, request);
            then(transformCompletionFacade).should().fail(failureBundle);
        }

        @Test
        @DisplayName("complete 중 예외 발생 시 safeFailTransform으로 처리한다")
        void execute_CompleteThrowsException_SafeFailsTransform() {
            // given
            TransformRequest request = TransformRequestFixture.aResizeRequest();
            Asset sourceAsset = AssetFixture.anAsset();

            StatusChangeContext<String> startContext =
                    new StatusChangeContext<>(request.idValue(), NOW);
            given(transformCommandFactory.createStartContext(request.idValue()))
                    .willReturn(startContext);

            FileInfo fileInfo =
                    FileInfo.of("resized.jpg", 2048L, "image/jpeg", "etag-resized", "jpg");
            ImageDimension dimension = ImageDimension.of(800, 600);
            ImageTransformResult successResult =
                    ImageTransformResult.success(
                            "result/resized.jpg", "fileflow-bucket", fileInfo, dimension);

            given(imageTransformFacade.transform(sourceAsset, request)).willReturn(successResult);

            Instant completedAt = NOW.plusSeconds(30);
            Asset resultAsset = AssetFixture.anAssetWithId("result-001");
            TransformCompletionBundle completionBundle =
                    new TransformCompletionBundle(resultAsset, request, dimension, completedAt);
            given(
                            transformCommandFactory.createCompletionBundle(
                                    successResult, request, sourceAsset))
                    .willReturn(completionBundle);
            willThrow(new RuntimeException("DB error"))
                    .given(transformCompletionFacade)
                    .complete(completionBundle);

            TransformFailureBundle failureBundle =
                    new TransformFailureBundle(request, "DB error", NOW);
            given(transformCommandFactory.createFailureBundle(any(), any()))
                    .willReturn(failureBundle);

            // when
            sut.execute(request, sourceAsset);

            // then
            then(transformCompletionFacade).should().fail(failureBundle);
        }

        @Test
        @DisplayName("safeFailTransform도 실패 시 직접 persist를 시도한다")
        void execute_SafeFailAlsoFails_DirectPersistAttempted() {
            // given
            TransformRequest request = TransformRequestFixture.aResizeRequest();
            Asset sourceAsset = AssetFixture.anAsset();

            StatusChangeContext<String> startContext =
                    new StatusChangeContext<>(request.idValue(), NOW);
            given(transformCommandFactory.createStartContext(request.idValue()))
                    .willReturn(startContext);

            given(imageTransformFacade.transform(sourceAsset, request))
                    .willThrow(new RuntimeException("Transform crash"));

            given(transformCommandFactory.createFailureBundle(any(), any()))
                    .willThrow(new RuntimeException("Factory also fails"));

            TransformRequest freshRequest = TransformRequestFixture.aProcessingRequest();
            given(transformReadManager.getTransformRequest(request.idValue()))
                    .willReturn(freshRequest);

            // when
            sut.execute(request, sourceAsset);

            // then
            then(transformReadManager).should().getTransformRequest(request.idValue());
            then(transformCommandManager).should(times(2)).persist(any(TransformRequest.class));
        }

        @Test
        @DisplayName("최종 persist까지 실패해도 예외를 던지지 않는다")
        void execute_LastResortFails_DoesNotThrow() {
            // given
            TransformRequest request = TransformRequestFixture.aResizeRequest();
            Asset sourceAsset = AssetFixture.anAsset();

            StatusChangeContext<String> startContext =
                    new StatusChangeContext<>(request.idValue(), NOW);
            given(transformCommandFactory.createStartContext(request.idValue()))
                    .willReturn(startContext);

            given(imageTransformFacade.transform(sourceAsset, request))
                    .willThrow(new RuntimeException("Transform crash"));

            given(transformCommandFactory.createFailureBundle(any(), any()))
                    .willThrow(new RuntimeException("Factory fails"));

            given(transformReadManager.getTransformRequest(request.idValue()))
                    .willThrow(new RuntimeException("Read also fails"));

            // when - 예외 없이 완료되어야 함
            sut.execute(request, sourceAsset);

            // then - 예외가 발생하지 않으면 성공
            then(transformReadManager).should().getTransformRequest(request.idValue());
        }
    }
}
