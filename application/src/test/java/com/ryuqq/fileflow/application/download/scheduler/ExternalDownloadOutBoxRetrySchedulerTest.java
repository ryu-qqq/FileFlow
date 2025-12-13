package com.ryuqq.fileflow.application.download.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ryuqq.fileflow.application.common.metrics.SchedulerMetrics;
import com.ryuqq.fileflow.application.common.port.out.DistributedLockPort;
import com.ryuqq.fileflow.application.download.port.in.command.RetryUnpublishedOutboxUseCase;
import com.ryuqq.fileflow.application.download.port.in.command.RetryUnpublishedOutboxUseCase.RetryResult;
import com.ryuqq.fileflow.domain.common.vo.LockKey;
import com.ryuqq.fileflow.domain.download.vo.OutboxRetryLockKey;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("ExternalDownloadOutBoxRetryScheduler 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ExternalDownloadOutBoxRetrySchedulerTest {

    @Mock private RetryUnpublishedOutboxUseCase retryUnpublishedOutboxUseCase;
    @Mock private DistributedLockPort distributedLockPort;
    @Mock private SchedulerMetrics schedulerMetrics;
    @Mock private Timer.Sample timerSample;

    @InjectMocks private ExternalDownloadOutBoxRetryScheduler scheduler;

    @Nested
    @DisplayName("retryUnpublishedOutboxes")
    class RetryUnpublishedOutboxes {

        @Test
        @DisplayName("분산 락 획득 실패 시 UseCase를 호출하지 않는다")
        void retryUnpublishedOutboxes_WhenLockAcquisitionFails_ShouldNotExecuteUseCase() {
            // given
            when(distributedLockPort.tryLock(
                            any(LockKey.class), anyLong(), anyLong(), any(TimeUnit.class)))
                    .thenReturn(false);

            // when
            scheduler.retryUnpublishedOutboxes();

            // then
            verify(retryUnpublishedOutboxUseCase, never()).execute();
            verify(distributedLockPort, never()).unlock(any(LockKey.class));
            verify(schedulerMetrics, never()).startJob(any());
        }

        @Test
        @DisplayName("분산 락 획득 성공 시 UseCase를 호출하고 락을 해제한다")
        void retryUnpublishedOutboxes_WhenLockAcquired_ShouldExecuteUseCaseAndUnlock() {
            // given
            when(schedulerMetrics.startJob(any())).thenReturn(timerSample);
            when(distributedLockPort.tryLock(
                            any(LockKey.class), anyLong(), anyLong(), any(TimeUnit.class)))
                    .thenReturn(true);
            when(retryUnpublishedOutboxUseCase.execute()).thenReturn(new RetryResult(10, 8, 2, 1));

            // when
            scheduler.retryUnpublishedOutboxes();

            // then
            verify(retryUnpublishedOutboxUseCase).execute();
            verify(distributedLockPort).unlock(any(LockKey.class));
            verify(schedulerMetrics).recordJobItemsProcessed(any(), any(Integer.class));
            verify(schedulerMetrics).recordJobSuccess(any(), any(Timer.Sample.class));
        }

        @Test
        @DisplayName("UseCase 실행 중 예외 발생 시 락을 해제하고 예외를 전파한다")
        void retryUnpublishedOutboxes_WhenUseCaseThrows_ShouldUnlockAndRethrow() {
            // given
            when(schedulerMetrics.startJob(any())).thenReturn(timerSample);
            when(distributedLockPort.tryLock(
                            any(LockKey.class), anyLong(), anyLong(), any(TimeUnit.class)))
                    .thenReturn(true);
            when(retryUnpublishedOutboxUseCase.execute())
                    .thenThrow(new RuntimeException("UseCase error"));

            // when & then
            org.junit.jupiter.api.Assertions.assertThrows(
                    RuntimeException.class, () -> scheduler.retryUnpublishedOutboxes());

            verify(distributedLockPort).unlock(any(LockKey.class));
            verify(schedulerMetrics).recordJobFailure(any(), any(Timer.Sample.class), any());
        }

        @Test
        @DisplayName("올바른 LockKey를 사용하여 분산 락을 획득한다")
        void retryUnpublishedOutboxes_ShouldUseCorrectLockKey() {
            // given
            when(schedulerMetrics.startJob(any())).thenReturn(timerSample);
            when(distributedLockPort.tryLock(
                            any(LockKey.class), anyLong(), anyLong(), any(TimeUnit.class)))
                    .thenReturn(true);
            when(retryUnpublishedOutboxUseCase.execute()).thenReturn(RetryResult.empty());

            // when
            scheduler.retryUnpublishedOutboxes();

            // then
            verify(distributedLockPort)
                    .tryLock(
                            org.mockito.ArgumentMatchers.argThat(
                                    lockKey ->
                                            lockKey instanceof OutboxRetryLockKey
                                                    && lockKey.value()
                                                            .equals(
                                                                    "lock:outbox:retry:external-download")),
                            anyLong(),
                            anyLong(),
                            any(TimeUnit.class));
        }

        @Test
        @DisplayName("메트릭이 올바르게 기록된다")
        void retryUnpublishedOutboxes_ShouldRecordMetricsCorrectly() {
            // given
            when(schedulerMetrics.startJob(any())).thenReturn(timerSample);
            when(distributedLockPort.tryLock(
                            any(LockKey.class), anyLong(), anyLong(), any(TimeUnit.class)))
                    .thenReturn(true);
            RetryResult result = new RetryResult(50, 45, 5, 2);
            when(retryUnpublishedOutboxUseCase.execute()).thenReturn(result);

            // when
            scheduler.retryUnpublishedOutboxes();

            // then
            verify(schedulerMetrics).recordJobItemsProcessed("outbox-retry", 50);
            verify(schedulerMetrics).recordJobSuccess("outbox-retry", timerSample);
        }
    }
}
