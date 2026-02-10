package com.ryuqq.fileflow.application.transform.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.application.transform.dto.response.TransformRequestResponse;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("TransformAssembler 단위 테스트")
class TransformAssemblerTest {

    private TransformAssembler sut;

    @BeforeEach
    void setUp() {
        sut = new TransformAssembler();
    }

    @Nested
    @DisplayName("toResponse 메서드")
    class ToResponseTest {

        @Test
        @DisplayName("QUEUED 상태의 TransformRequest를 Response로 변환한다")
        void toResponse_QueuedRequest_ReturnsCorrectResponse() {
            // given
            TransformRequest request = TransformRequestFixture.aResizeRequest();

            // when
            TransformRequestResponse result = sut.toResponse(request);

            // then
            assertThat(result.transformRequestId()).isEqualTo(request.idValue());
            assertThat(result.sourceAssetId()).isEqualTo(request.sourceAssetIdValue());
            assertThat(result.sourceContentType()).isEqualTo(request.sourceContentType());
            assertThat(result.transformType()).isEqualTo(request.type().name());
            assertThat(result.width()).isEqualTo(request.params().width());
            assertThat(result.height()).isEqualTo(request.params().height());
            assertThat(result.quality()).isEqualTo(request.params().quality());
            assertThat(result.targetFormat()).isEqualTo(request.params().targetFormat());
            assertThat(result.status()).isEqualTo("QUEUED");
            assertThat(result.resultAssetId()).isNull();
            assertThat(result.lastError()).isNull();
            assertThat(result.createdAt()).isEqualTo(request.createdAt());
            assertThat(result.completedAt()).isNull();
        }

        @Test
        @DisplayName("COMPLETED 상태의 TransformRequest를 Response로 변환한다")
        void toResponse_CompletedRequest_ReturnsCompletedStatus() {
            // given
            TransformRequest request = TransformRequestFixture.aCompletedRequest();

            // when
            TransformRequestResponse result = sut.toResponse(request);

            // then
            assertThat(result.status()).isEqualTo("COMPLETED");
            assertThat(result.resultAssetId()).isEqualTo(request.resultAssetIdValue());
            assertThat(result.completedAt()).isEqualTo(request.completedAt());
            assertThat(result.lastError()).isNull();
        }

        @Test
        @DisplayName("FAILED 상태의 TransformRequest를 Response로 변환한다")
        void toResponse_FailedRequest_ReturnsFailedStatusWithError() {
            // given
            TransformRequest request = TransformRequestFixture.aFailedRequest();

            // when
            TransformRequestResponse result = sut.toResponse(request);

            // then
            assertThat(result.status()).isEqualTo("FAILED");
            assertThat(result.lastError()).isEqualTo("Processing error");
            assertThat(result.completedAt()).isEqualTo(request.completedAt());
        }

        @Test
        @DisplayName("CONVERT 타입의 TransformRequest를 올바르게 변환한다")
        void toResponse_ConvertRequest_ReturnsCorrectTypeAndFormat() {
            // given
            TransformRequest request = TransformRequestFixture.aConvertRequest();

            // when
            TransformRequestResponse result = sut.toResponse(request);

            // then
            assertThat(result.transformType()).isEqualTo("CONVERT");
            assertThat(result.targetFormat()).isEqualTo("webp");
            assertThat(result.width()).isNull();
            assertThat(result.height()).isNull();
        }
    }
}
