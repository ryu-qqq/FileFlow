package com.ryuqq.fileflow.adapter.in.sqs.transform;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.ryuqq.fileflow.application.transform.port.in.command.StartTransformRequestUseCase;
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
@DisplayName("TransformRequestSqsConsumer 단위 테스트")
class TransformRequestSqsConsumerTest {

    @InjectMocks private TransformRequestSqsConsumer sut;
    @Mock private StartTransformRequestUseCase startTransformRequestUseCase;

    @Nested
    @DisplayName("consume 메서드")
    class ConsumeTest {

        @Test
        @DisplayName("유효한 변환 요청 ID를 수신하면 UseCase를 실행한다")
        void consume_ValidTransformRequestId_ExecutesUseCase() {
            // given
            String transformRequestId = "transform-request-001";

            // when
            sut.consume(transformRequestId);

            // then
            then(startTransformRequestUseCase).should().execute(transformRequestId);
        }

        @Test
        @DisplayName("UseCase 실행 중 예외가 발생하면 SQS 재시도를 위해 예외를 재전파한다")
        void consume_UseCaseThrows_RethrowsForSqsRetry() {
            // given
            String transformRequestId = "transform-request-002";
            willThrow(new RuntimeException("변환 처리 실패"))
                    .given(startTransformRequestUseCase)
                    .execute(transformRequestId);

            // when & then
            assertThatThrownBy(() -> sut.consume(transformRequestId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("변환 처리 실패");
        }

        @Test
        @DisplayName("정상 처리 완료 후 UseCase가 정확히 한 번만 호출된다")
        void consume_SuccessfulProcessing_UseCaseCalledOnce() {
            // given
            String transformRequestId = "transform-request-003";

            // when
            sut.consume(transformRequestId);

            // then
            then(startTransformRequestUseCase).should().execute(transformRequestId);
            then(startTransformRequestUseCase).shouldHaveNoMoreInteractions();
        }
    }
}
