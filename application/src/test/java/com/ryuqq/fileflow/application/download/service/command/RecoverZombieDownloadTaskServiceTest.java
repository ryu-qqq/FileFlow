package com.ryuqq.fileflow.application.download.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.ryuqq.fileflow.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.fileflow.application.download.dto.command.RecoverZombieDownloadTaskCommand;
import com.ryuqq.fileflow.application.download.manager.client.DownloadQueueManager;
import com.ryuqq.fileflow.application.download.manager.query.DownloadReadManager;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTaskFixture;
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
@DisplayName("RecoverZombieDownloadTaskService 단위 테스트")
class RecoverZombieDownloadTaskServiceTest {

    @InjectMocks private RecoverZombieDownloadTaskService sut;
    @Mock private DownloadReadManager downloadReadManager;
    @Mock private DownloadQueueManager downloadQueueManager;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("좀비 태스크를 조회하고 재큐잉한 후 성공 결과를 반환한다")
        void execute_WithStaleTasks_RequeuesAndReturnsResult() {
            // given
            RecoverZombieDownloadTaskCommand command =
                    RecoverZombieDownloadTaskCommand.of(100, 300);

            DownloadTask staleTask = DownloadTaskFixture.aQueuedTask();
            List<DownloadTask> staleTasks = List.of(staleTask);

            given(downloadReadManager.getStaleQueuedTasks(any(Instant.class), eq(100)))
                    .willReturn(staleTasks);

            // when
            SchedulerBatchProcessingResult result = sut.execute(command);

            // then
            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isZero();
            then(downloadQueueManager).should().enqueue(staleTask.idValue());
        }

        @Test
        @DisplayName("좀비 태스크가 없으면 빈 결과를 반환한다")
        void execute_NoStaleTasks_ReturnsEmptyResult() {
            // given
            RecoverZombieDownloadTaskCommand command =
                    RecoverZombieDownloadTaskCommand.of(100, 300);

            given(downloadReadManager.getStaleQueuedTasks(any(Instant.class), eq(100)))
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
            RecoverZombieDownloadTaskCommand command =
                    RecoverZombieDownloadTaskCommand.of(100, 300);

            DownloadTask staleTask = DownloadTaskFixture.aQueuedTask();
            List<DownloadTask> staleTasks = List.of(staleTask);

            given(downloadReadManager.getStaleQueuedTasks(any(Instant.class), eq(100)))
                    .willReturn(staleTasks);
            willThrow(new RuntimeException("Queue error"))
                    .given(downloadQueueManager)
                    .enqueue(staleTask.idValue());

            // when
            SchedulerBatchProcessingResult result = sut.execute(command);

            // then
            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isEqualTo(1);
        }
    }
}
