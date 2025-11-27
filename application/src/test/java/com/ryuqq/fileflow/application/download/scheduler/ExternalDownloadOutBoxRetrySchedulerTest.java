package com.ryuqq.fileflow.application.download.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ryuqq.fileflow.application.download.dto.ExternalDownloadMessage;
import com.ryuqq.fileflow.application.download.manager.ExternalDownloadOutboxManager;
import com.ryuqq.fileflow.application.download.port.out.client.SqsPublishPort;
import com.ryuqq.fileflow.application.download.port.out.query.ExternalDownloadOutboxQueryPort;
import com.ryuqq.fileflow.application.download.port.out.query.ExternalDownloadQueryPort;
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

@DisplayName("ExternalDownloadOutBoxRetryScheduler 단위 테스트")
@ExtendWith(MockitoExtension.class)
class ExternalDownloadOutBoxRetrySchedulerTest {

    @Mock private ExternalDownloadOutboxQueryPort outboxQueryPort;
    @Mock private ExternalDownloadQueryPort downloadQueryPort;
    @Mock private ExternalDownloadOutboxManager outboxManager;
    @Mock private SqsPublishPort sqsPublishPort;

    @InjectMocks private ExternalDownloadOutBoxRetryScheduler scheduler;

    @Nested
    @DisplayName("retryUnpublishedOutboxes")
    class RetryUnpublishedOutboxes {

        @Test
        @DisplayName("미발행 Outbox가 없으면 아무 처리도 하지 않는다")
        void retryUnpublishedOutboxes_WhenNoUnpublished_ShouldDoNothing() {
            // given
            when(outboxQueryPort.findUnpublished(eq(100))).thenReturn(Collections.emptyList());

            // when
            scheduler.retryUnpublishedOutboxes();

            // then
            verify(outboxQueryPort).findUnpublished(eq(100));
            verify(sqsPublishPort, never()).publish(any(ExternalDownloadMessage.class));
            verify(outboxManager, never()).markAsPublished(any());
        }

        @Test
        @DisplayName("미발행 Outbox가 있고 SQS 발행 성공 시 markAsPublished 호출")
        void retryUnpublishedOutboxes_WhenPublishSuccess_ShouldMarkAsPublished() {
            // given
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.unpublishedOutbox();
            ExternalDownload download = ExternalDownloadFixture.pendingDownload();

            when(outboxQueryPort.findUnpublished(eq(100)))
                    .thenReturn(List.of(outbox))
                    .thenReturn(Collections.emptyList());
            when(downloadQueryPort.findById(any(ExternalDownloadId.class)))
                    .thenReturn(Optional.of(download));
            when(sqsPublishPort.publish(any(ExternalDownloadMessage.class))).thenReturn(true);

            // when
            scheduler.retryUnpublishedOutboxes();

            // then
            verify(sqsPublishPort).publish(any(ExternalDownloadMessage.class));
            verify(outboxManager).markAsPublished(outbox);
        }

        @Test
        @DisplayName("SQS 발행 실패(false 반환) 시 markAsPublished 호출하지 않음")
        void retryUnpublishedOutboxes_WhenPublishReturnsFalse_ShouldNotMarkAsPublished() {
            // given
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.unpublishedOutbox();
            ExternalDownload download = ExternalDownloadFixture.pendingDownload();

            when(outboxQueryPort.findUnpublished(eq(100)))
                    .thenReturn(List.of(outbox))
                    .thenReturn(Collections.emptyList());
            when(downloadQueryPort.findById(any(ExternalDownloadId.class)))
                    .thenReturn(Optional.of(download));
            when(sqsPublishPort.publish(any(ExternalDownloadMessage.class))).thenReturn(false);

            // when
            scheduler.retryUnpublishedOutboxes();

            // then
            verify(sqsPublishPort).publish(any(ExternalDownloadMessage.class));
            verify(outboxManager, never()).markAsPublished(any());
        }

        @Test
        @DisplayName("ExternalDownload를 찾을 수 없으면 건너뛴다")
        void retryUnpublishedOutboxes_WhenDownloadNotFound_ShouldSkip() {
            // given
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.unpublishedOutbox();

            when(outboxQueryPort.findUnpublished(eq(100)))
                    .thenReturn(List.of(outbox))
                    .thenReturn(Collections.emptyList());
            when(downloadQueryPort.findById(any(ExternalDownloadId.class)))
                    .thenReturn(Optional.empty());

            // when
            scheduler.retryUnpublishedOutboxes();

            // then
            verify(sqsPublishPort, never()).publish(any(ExternalDownloadMessage.class));
            verify(outboxManager, never()).markAsPublished(any());
        }

        @Test
        @DisplayName("예외 발생 시 다음 Outbox 처리를 계속한다")
        void retryUnpublishedOutboxes_WhenExceptionOccurs_ShouldContinueWithOthers() {
            // given
            ExternalDownloadOutbox outbox1 = ExternalDownloadOutboxFixture.unpublishedOutbox();
            ExternalDownloadOutbox outbox2 = ExternalDownloadOutboxFixture.unpublishedOutbox();
            ExternalDownload download = ExternalDownloadFixture.pendingDownload();

            when(outboxQueryPort.findUnpublished(eq(100)))
                    .thenReturn(List.of(outbox1, outbox2))
                    .thenReturn(Collections.emptyList());
            when(downloadQueryPort.findById(any(ExternalDownloadId.class)))
                    .thenReturn(Optional.of(download));
            when(sqsPublishPort.publish(any(ExternalDownloadMessage.class)))
                    .thenThrow(new RuntimeException("SQS error"))
                    .thenReturn(true);

            // when
            scheduler.retryUnpublishedOutboxes();

            // then
            verify(sqsPublishPort, times(2)).publish(any(ExternalDownloadMessage.class));
            verify(outboxManager, times(1)).markAsPublished(any());
        }

        @Test
        @DisplayName("배치 크기만큼 Outbox가 있으면 다음 배치를 조회한다")
        void retryUnpublishedOutboxes_WhenBatchIsFull_ShouldFetchNextBatch() {
            // given
            List<ExternalDownloadOutbox> firstBatch = createOutboxBatch(100);
            List<ExternalDownloadOutbox> secondBatch = createOutboxBatch(50);
            ExternalDownload download = ExternalDownloadFixture.pendingDownload();

            when(outboxQueryPort.findUnpublished(eq(100)))
                    .thenReturn(firstBatch)
                    .thenReturn(secondBatch)
                    .thenReturn(Collections.emptyList());
            when(downloadQueryPort.findById(any(ExternalDownloadId.class)))
                    .thenReturn(Optional.of(download));
            when(sqsPublishPort.publish(any(ExternalDownloadMessage.class))).thenReturn(true);

            // when
            scheduler.retryUnpublishedOutboxes();

            // then
            verify(outboxQueryPort, times(2)).findUnpublished(eq(100));
            verify(sqsPublishPort, times(150)).publish(any(ExternalDownloadMessage.class));
            verify(outboxManager, times(150)).markAsPublished(any());
        }

        @Test
        @DisplayName("배치 크기보다 적은 Outbox면 추가 조회 없이 종료한다")
        void retryUnpublishedOutboxes_WhenLessThanBatchSize_ShouldNotFetchAgain() {
            // given
            List<ExternalDownloadOutbox> outboxes = createOutboxBatch(50);
            ExternalDownload download = ExternalDownloadFixture.pendingDownload();

            when(outboxQueryPort.findUnpublished(eq(100))).thenReturn(outboxes);
            when(downloadQueryPort.findById(any(ExternalDownloadId.class)))
                    .thenReturn(Optional.of(download));
            when(sqsPublishPort.publish(any(ExternalDownloadMessage.class))).thenReturn(true);

            // when
            scheduler.retryUnpublishedOutboxes();

            // then
            verify(outboxQueryPort, times(1)).findUnpublished(eq(100));
            verify(sqsPublishPort, times(50)).publish(any(ExternalDownloadMessage.class));
        }

        @Test
        @DisplayName("markAsPublished에서 예외 발생 시에도 다음 Outbox 처리를 계속한다")
        void retryUnpublishedOutboxes_WhenMarkAsPublishedFails_ShouldContinueWithOthers() {
            // given
            ExternalDownloadOutbox outbox1 = ExternalDownloadOutboxFixture.unpublishedOutbox();
            ExternalDownloadOutbox outbox2 = ExternalDownloadOutboxFixture.unpublishedOutbox();
            ExternalDownload download = ExternalDownloadFixture.pendingDownload();

            when(outboxQueryPort.findUnpublished(eq(100)))
                    .thenReturn(List.of(outbox1, outbox2))
                    .thenReturn(Collections.emptyList());
            when(downloadQueryPort.findById(any(ExternalDownloadId.class)))
                    .thenReturn(Optional.of(download));
            when(sqsPublishPort.publish(any(ExternalDownloadMessage.class))).thenReturn(true);
            doThrow(new RuntimeException("DB error"))
                    .doNothing()
                    .when(outboxManager)
                    .markAsPublished(any());

            // when
            scheduler.retryUnpublishedOutboxes();

            // then
            verify(outboxManager, times(2)).markAsPublished(any());
        }

        private List<ExternalDownloadOutbox> createOutboxBatch(int size) {
            return java.util.stream.IntStream.range(0, size)
                    .mapToObj(i -> ExternalDownloadOutboxFixture.unpublishedOutbox())
                    .toList();
        }
    }
}
