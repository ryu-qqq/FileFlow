package com.ryuqq.fileflow.application.transform.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.transform.dto.command.RecoverZombieTransformRequestCommand;
import com.ryuqq.fileflow.application.transform.manager.client.TransformQueueManager;
import com.ryuqq.fileflow.application.transform.manager.query.TransformReadManager;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequest;
import com.ryuqq.fileflow.domain.transform.aggregate.TransformRequestFixture;
import java.time.Instant;
import java.util.List;
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
@DisplayName("RecoverZombieTransformRequestService 단위 테스트")
class RecoverZombieTransformRequestServiceTest {

    @InjectMocks private RecoverZombieTransformRequestService sut;
    @Mock private TransformReadManager transformReadManager;
    @Mock private TransformQueueManager transformQueueManager;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("좀비 요청을 조회하고 재큐잉한 후 성공 결과를 반환한다")
        void execute_WithStaleRequests_RequeuesAndReturnsResult() {
            // given
            RecoverZombieTransformRequestCommand command =
                    RecoverZombieTransformRequestCommand.of(100, 300);

            TransformRequest staleRequest = TransformRequestFixture.aResizeRequest();
            List<TransformRequest> staleRequests = List.of(staleRequest);

            given(transformReadManager.getStaleQueuedRequests(any(Instant.class), eq(100)))
                    .willReturn(staleRequests);

            // when
            SchedulerBatchProcessingResult result = sut.execute(command);

            // then
            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isZero();
            then(transformQueueManager).should().enqueue(staleRequest.idValue());
        }

        @Test
        @DisplayName("좀비 요청이 없으면 빈 결과를 반환한다")
        void execute_NoStaleRequests_ReturnsEmptyResult() {
            // given
            RecoverZombieTransformRequestCommand command =
                    RecoverZombieTransformRequestCommand.of(100, 300);

            given(transformReadManager.getStaleQueuedRequests(any(Instant.class), eq(100)))
                    .willReturn(List.of());

            // when
            SchedulerBatchProcessingResult result = sut.execute(command);

            // then
            assertThat(result.total()).isZero();
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isZero();
        }

        @Test
        @DisplayName("재큐잉 실패 시 실패 카운트에 반영한다")
        void execute_EnqueueFails_CountsAsFailure() {
            // given
            RecoverZombieTransformRequestCommand command =
                    RecoverZombieTransformRequestCommand.of(100, 300);

            TransformRequest staleRequest = TransformRequestFixture.aResizeRequest();
            List<TransformRequest> staleRequests = List.of(staleRequest);

            given(transformReadManager.getStaleQueuedRequests(any(Instant.class), eq(100)))
                    .willReturn(staleRequests);
            willThrow(new RuntimeException("Queue error"))
                    .given(transformQueueManager)
                    .enqueue(staleRequest.idValue());

            // when
            SchedulerBatchProcessingResult result = sut.execute(command);

            // then
            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isEqualTo(1);
        }
    }
}
