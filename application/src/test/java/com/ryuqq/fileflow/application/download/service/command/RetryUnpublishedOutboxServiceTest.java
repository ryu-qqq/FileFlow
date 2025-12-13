package com.ryuqq.fileflow.application.download.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ryuqq.fileflow.application.download.dto.ExternalDownloadMessage;
import com.ryuqq.fileflow.application.download.factory.command.ExternalDownloadCommandFactory;
import com.ryuqq.fileflow.application.download.manager.command.ExternalDownloadOutboxTransactionManager;
import com.ryuqq.fileflow.application.download.manager.query.ExternalDownloadOutboxReadManager;
import com.ryuqq.fileflow.application.download.manager.query.ExternalDownloadReadManager;
import com.ryuqq.fileflow.application.download.port.in.command.RetryUnpublishedOutboxUseCase.RetryResult;
import com.ryuqq.fileflow.application.download.port.out.client.ExternalDownloadSqsPublishPort;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.fixture.ExternalDownloadFixture;
import com.ryuqq.fileflow.domain.download.fixture.ExternalDownloadOutboxFixture;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("RetryUnpublishedOutboxService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class RetryUnpublishedOutboxServiceTest {

    @Mock private ExternalDownloadOutboxReadManager outboxReadManager;
    @Mock private ExternalDownloadReadManager downloadReadManager;
    @Mock private ExternalDownloadOutboxTransactionManager outboxTransactionManager;
    @Mock private ExternalDownloadCommandFactory commandFactory;
    @Mock private ExternalDownloadSqsPublishPort sqsPublishPort;

    @InjectMocks private RetryUnpublishedOutboxService service;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("미발행 Outbox가 없으면 빈 결과를 반환한다")
        void execute_WhenNoUnpublished_ShouldReturnEmptyResult() {
            // given
            when(outboxReadManager.findUnpublished(eq(100))).thenReturn(Collections.emptyList());

            // when
            RetryResult result = service.execute();

            // then
            assertThat(result.totalRetried()).isZero();
            assertThat(result.succeeded()).isZero();
            assertThat(result.failed()).isZero();
            assertThat(result.iterations()).isZero();
            verify(sqsPublishPort, never()).publish(any(ExternalDownloadMessage.class));
        }

        @Test
        @DisplayName("미발행 Outbox가 있고 SQS 발행 성공 시 markAsPublished 호출")
        void execute_WhenPublishSuccess_ShouldMarkAsPublished() {
            // given
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.unpublishedOutbox();
            ExternalDownload download = ExternalDownloadFixture.pendingDownload();

            when(outboxReadManager.findUnpublished(eq(100)))
                    .thenReturn(List.of(outbox))
                    .thenReturn(Collections.emptyList());
            when(downloadReadManager.findById(any(ExternalDownloadId.class)))
                    .thenReturn(Optional.of(download));
            when(sqsPublishPort.publish(any(ExternalDownloadMessage.class))).thenReturn(true);

            // when
            RetryResult result = service.execute();

            // then
            assertThat(result.totalRetried()).isEqualTo(1);
            assertThat(result.succeeded()).isEqualTo(1);
            assertThat(result.failed()).isZero();
            verify(sqsPublishPort).publish(any(ExternalDownloadMessage.class));
            verify(commandFactory).markAsPublished(outbox);
            verify(outboxTransactionManager).persist(outbox);
        }

        @Test
        @DisplayName("SQS 발행 실패(false 반환) 시 markAsPublished 호출하지 않음")
        void execute_WhenPublishReturnsFalse_ShouldNotMarkAsPublished() {
            // given
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.unpublishedOutbox();
            ExternalDownload download = ExternalDownloadFixture.pendingDownload();

            when(outboxReadManager.findUnpublished(eq(100)))
                    .thenReturn(List.of(outbox))
                    .thenReturn(Collections.emptyList());
            when(downloadReadManager.findById(any(ExternalDownloadId.class)))
                    .thenReturn(Optional.of(download));
            when(sqsPublishPort.publish(any(ExternalDownloadMessage.class))).thenReturn(false);

            // when
            RetryResult result = service.execute();

            // then
            assertThat(result.totalRetried()).isEqualTo(1);
            assertThat(result.succeeded()).isZero();
            assertThat(result.failed()).isEqualTo(1);
            verify(sqsPublishPort).publish(any(ExternalDownloadMessage.class));
            verify(commandFactory, never()).markAsPublished(any());
            verify(outboxTransactionManager, never()).persist(any());
        }

        @Test
        @DisplayName("ExternalDownload를 찾을 수 없으면 실패로 카운트")
        void execute_WhenDownloadNotFound_ShouldCountAsFailed() {
            // given
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.unpublishedOutbox();

            when(outboxReadManager.findUnpublished(eq(100)))
                    .thenReturn(List.of(outbox))
                    .thenReturn(Collections.emptyList());
            when(downloadReadManager.findById(any(ExternalDownloadId.class)))
                    .thenReturn(Optional.empty());

            // when
            RetryResult result = service.execute();

            // then
            assertThat(result.totalRetried()).isEqualTo(1);
            assertThat(result.failed()).isEqualTo(1);
            verify(sqsPublishPort, never()).publish(any(ExternalDownloadMessage.class));
            verify(commandFactory, never()).markAsPublished(any());
            verify(outboxTransactionManager, never()).persist(any());
        }

        @Test
        @DisplayName("예외 발생 시 다음 Outbox 처리를 계속한다")
        void execute_WhenExceptionOccurs_ShouldContinueWithOthers() {
            // given
            ExternalDownloadOutbox outbox1 = ExternalDownloadOutboxFixture.unpublishedOutbox();
            ExternalDownloadOutbox outbox2 = ExternalDownloadOutboxFixture.unpublishedOutbox();
            ExternalDownload download = ExternalDownloadFixture.pendingDownload();

            when(outboxReadManager.findUnpublished(eq(100)))
                    .thenReturn(List.of(outbox1, outbox2))
                    .thenReturn(Collections.emptyList());
            when(downloadReadManager.findById(any(ExternalDownloadId.class)))
                    .thenReturn(Optional.of(download));
            when(sqsPublishPort.publish(any(ExternalDownloadMessage.class)))
                    .thenThrow(new RuntimeException("SQS error"))
                    .thenReturn(true);

            // when
            RetryResult result = service.execute();

            // then
            assertThat(result.totalRetried()).isEqualTo(2);
            assertThat(result.succeeded()).isEqualTo(1);
            assertThat(result.failed()).isEqualTo(1);
            verify(sqsPublishPort, times(2)).publish(any(ExternalDownloadMessage.class));
        }

        @Test
        @DisplayName("배치 크기만큼 Outbox가 있으면 다음 배치를 조회한다")
        void execute_WhenBatchIsFull_ShouldFetchNextBatch() {
            // given
            List<ExternalDownloadOutbox> firstBatch = createOutboxBatch(100);
            List<ExternalDownloadOutbox> secondBatch = createOutboxBatch(50);
            ExternalDownload download = ExternalDownloadFixture.pendingDownload();

            when(outboxReadManager.findUnpublished(eq(100)))
                    .thenReturn(firstBatch)
                    .thenReturn(secondBatch)
                    .thenReturn(Collections.emptyList());
            when(downloadReadManager.findById(any(ExternalDownloadId.class)))
                    .thenReturn(Optional.of(download));
            when(sqsPublishPort.publish(any(ExternalDownloadMessage.class))).thenReturn(true);

            // when
            RetryResult result = service.execute();

            // then
            assertThat(result.totalRetried()).isEqualTo(150);
            assertThat(result.succeeded()).isEqualTo(150);
            assertThat(result.iterations()).isEqualTo(2);
            verify(outboxReadManager, times(2)).findUnpublished(eq(100));
        }

        @Test
        @DisplayName("persist 예외 발생 시 다음 Outbox 처리를 계속한다")
        void execute_WhenPersistFails_ShouldContinueWithOthers() {
            // given
            ExternalDownloadOutbox outbox1 = ExternalDownloadOutboxFixture.unpublishedOutbox();
            ExternalDownloadOutbox outbox2 = ExternalDownloadOutboxFixture.unpublishedOutbox();
            ExternalDownload download = ExternalDownloadFixture.pendingDownload();

            when(outboxReadManager.findUnpublished(eq(100)))
                    .thenReturn(List.of(outbox1, outbox2))
                    .thenReturn(Collections.emptyList());
            when(downloadReadManager.findById(any(ExternalDownloadId.class)))
                    .thenReturn(Optional.of(download));
            when(sqsPublishPort.publish(any(ExternalDownloadMessage.class))).thenReturn(true);
            doThrow(new RuntimeException("DB error"))
                    .doReturn(null)
                    .when(outboxTransactionManager)
                    .persist(any());

            // when
            RetryResult result = service.execute();

            // then
            assertThat(result.totalRetried()).isEqualTo(2);
            assertThat(result.succeeded()).isEqualTo(1);
            assertThat(result.failed()).isEqualTo(1);
            verify(commandFactory, times(2)).markAsPublished(any());
            verify(outboxTransactionManager, times(2)).persist(any());
        }

        private List<ExternalDownloadOutbox> createOutboxBatch(int size) {
            return java.util.stream.IntStream.range(0, size)
                    .mapToObj(i -> ExternalDownloadOutboxFixture.unpublishedOutbox())
                    .toList();
        }
    }
}
