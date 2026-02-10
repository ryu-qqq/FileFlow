package com.ryuqq.fileflow.application.transform.factory.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.fileflow.application.asset.factory.command.AssetCommandFactory;
import com.ryuqq.fileflow.application.common.dto.command.StatusChangeContext;
import com.ryuqq.fileflow.application.common.port.out.IdGeneratorPort;
import com.ryuqq.fileflow.application.common.time.TimeProvider;
import com.ryuqq.fileflow.application.transform.dto.bundle.TransformCompletionBundle;
import com.ryuqq.fileflow.application.transform.dto.bundle.TransformFailureBundle;
import com.ryuqq.fileflow.application.transform.dto.command.CreateTransformRequestCommand;
import com.ryuqq.fileflow.application.transform.dto.result.ImageTransformResult;
import com.ryuqq.fileflow.domain.asset.aggregate.Asset;
import com.ryuqq.fileflow.domain.asset.aggregate.AssetFixture;
import com.ryuqq.fileflow.domain.asset.vo.FileInfo;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequestFixture;
import com.ryuqq.fileflow.domain.transform.vo.ImageDimension;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("TransformCommandFactory 단위 테스트")
class TransformCommandFactoryTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Mock private IdGeneratorPort idGeneratorPort;
    @Mock private AssetCommandFactory assetCommandFactory;

    private TransformCommandFactory sut;

    @BeforeEach
    void setUp() {
        TimeProvider timeProvider = new TimeProvider(Clock.fixed(NOW, ZoneOffset.UTC));
        sut = new TransformCommandFactory(idGeneratorPort, timeProvider, assetCommandFactory);
    }

    @Nested
    @DisplayName("createTransformRequest 메서드")
    class CreateTransformRequestTest {

        @Test
        @DisplayName("커맨드를 TransformRequest 도메인 객체로 변환한다")
        void createTransformRequest_ValidCommand_ReturnsTransformRequest() {
            // given
            given(idGeneratorPort.generate()).willReturn("transform-new-001");

            CreateTransformRequestCommand command =
                    new CreateTransformRequestCommand("asset-001", "RESIZE", 800, 600, null, null);

            // when
            TransformRequest result = sut.createTransformRequest(command, "image/jpeg");

            // then
            assertThat(result.idValue()).isEqualTo("transform-new-001");
            assertThat(result.sourceAssetIdValue()).isEqualTo("asset-001");
            assertThat(result.sourceContentType()).isEqualTo("image/jpeg");
            assertThat(result.type().name()).isEqualTo("RESIZE");
            assertThat(result.params().width()).isEqualTo(800);
            assertThat(result.params().height()).isEqualTo(600);
            assertThat(result.status().name()).isEqualTo("QUEUED");
            assertThat(result.createdAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("CONVERT 타입 커맨드를 올바르게 변환한다")
        void createTransformRequest_ConvertCommand_ReturnsConvertRequest() {
            // given
            given(idGeneratorPort.generate()).willReturn("transform-new-002");

            CreateTransformRequestCommand command =
                    new CreateTransformRequestCommand(
                            "asset-001", "CONVERT", null, null, null, "webp");

            // when
            TransformRequest result = sut.createTransformRequest(command, "image/jpeg");

            // then
            assertThat(result.type().name()).isEqualTo("CONVERT");
            assertThat(result.params().targetFormat()).isEqualTo("webp");
        }
    }

    @Nested
    @DisplayName("createStartContext 메서드")
    class CreateStartContextTest {

        @Test
        @DisplayName("변환 요청 ID와 현재 시간으로 StatusChangeContext를 생성한다")
        void createStartContext_ValidId_ReturnsContext() {
            // given
            String transformRequestId = "transform-001";

            // when
            StatusChangeContext<String> result = sut.createStartContext(transformRequestId);

            // then
            assertThat(result.id()).isEqualTo(transformRequestId);
            assertThat(result.changedAt()).isEqualTo(NOW);
        }
    }

    @Nested
    @DisplayName("createCompletionBundle 메서드")
    class CreateCompletionBundleTest {

        @Test
        @DisplayName("성공 결과로 TransformCompletionBundle을 생성한다 (completedAt은 TimeProvider)")
        void createCompletionBundle_SuccessResult_ReturnsBundle() {
            // given
            TransformRequest request = TransformRequestFixture.aProcessingRequest();
            Asset sourceAsset = AssetFixture.anAsset();
            Asset resultAsset = AssetFixture.anAssetWithId("result-asset-001");

            FileInfo fileInfo =
                    FileInfo.of("resized.jpg", 2048L, "image/jpeg", "etag-resized", "jpg");
            ImageDimension dimension = ImageDimension.of(800, 600);
            ImageTransformResult result =
                    ImageTransformResult.success(
                            "result/resized.jpg", "fileflow-bucket", fileInfo, dimension);

            given(assetCommandFactory.createAsset(org.mockito.ArgumentMatchers.any()))
                    .willReturn(resultAsset);

            // when
            TransformCompletionBundle bundle =
                    sut.createCompletionBundle(result, request, sourceAsset);

            // then
            assertThat(bundle.resultAsset()).isEqualTo(resultAsset);
            assertThat(bundle.request()).isEqualTo(request);
            assertThat(bundle.dimension()).isEqualTo(dimension);
            assertThat(bundle.completedAt()).isEqualTo(NOW);
        }
    }

    @Nested
    @DisplayName("createFailureBundle 메서드")
    class CreateFailureBundleTest {

        @Test
        @DisplayName("실패 결과로 TransformFailureBundle을 생성한다 (failedAt은 TimeProvider)")
        void createFailureBundle_FailureResult_ReturnsBundle() {
            // given
            TransformRequest request = TransformRequestFixture.aProcessingRequest();
            ImageTransformResult result = ImageTransformResult.failure("Processing error");

            // when
            TransformFailureBundle bundle = sut.createFailureBundle(request, result);

            // then
            assertThat(bundle.request()).isEqualTo(request);
            assertThat(bundle.errorMessage()).isEqualTo("Processing error");
            assertThat(bundle.failedAt()).isEqualTo(NOW);
        }
    }
}
