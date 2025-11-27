package com.ryuqq.fileflow.application.download.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.fileflow.application.download.manager.ExternalDownloadMessageManager;
import com.ryuqq.fileflow.application.download.manager.ExternalDownloadOutboxManager;
import com.ryuqq.fileflow.application.download.port.out.query.ExternalDownloadOutboxQueryPort;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.event.ExternalDownloadRegisteredEvent;
import com.ryuqq.fileflow.domain.download.fixture.ExternalDownloadOutboxFixture;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalDownloadRegisteredEventListener 테스트")
class ExternalDownloadRegisteredEventListenerTest {

    @Mock private ExternalDownloadOutboxQueryPort outboxQueryPort;

    @Mock private ExternalDownloadOutboxManager outboxManager;

    @Mock private ExternalDownloadMessageManager messageManager;

    @InjectMocks private ExternalDownloadRegisteredEventListener listener;

    @Nested
    @DisplayName("handleExternalDownloadRegistered 메서드")
    class HandleExternalDownloadRegisteredTest {

        @Test
        @DisplayName("이벤트 수신 시 SQS 메시지 발행 및 Outbox 상태 업데이트")
        void shouldPublishMessageAndUpdateOutbox() {
            // given
            ExternalDownloadRegisteredEvent event = createEvent(1L);
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.unpublishedOutbox();

            given(outboxQueryPort.findByExternalDownloadId(event.downloadId()))
                    .willReturn(Optional.of(outbox));
            given(messageManager.publishFromEvent(event)).willReturn(true);

            // when
            listener.handleExternalDownloadRegistered(event);

            // then
            verify(outboxQueryPort).findByExternalDownloadId(event.downloadId());
            verify(messageManager).publishFromEvent(event);
            verify(outboxManager).markAsPublished(outbox);
        }

        @Test
        @DisplayName("SQS 발행 성공 시 Outbox를 published 상태로 변경")
        void shouldMarkOutboxAsPublishedOnSuccess() {
            // given
            ExternalDownloadRegisteredEvent event = createEvent(2L);
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.unpublishedOutbox();

            given(outboxQueryPort.findByExternalDownloadId(event.downloadId()))
                    .willReturn(Optional.of(outbox));
            given(messageManager.publishFromEvent(event)).willReturn(true);

            // when
            listener.handleExternalDownloadRegistered(event);

            // then
            verify(outboxManager).markAsPublished(outbox);
            verify(outboxManager, never()).markAsFailed(any());
        }

        @Test
        @DisplayName("SQS 발행 실패 시 (false 반환) Outbox를 failed 상태로 변경")
        void shouldMarkOutboxAsFailedOnPublishFailure() {
            // given
            ExternalDownloadRegisteredEvent event = createEvent(3L);
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.unpublishedOutbox();

            given(outboxQueryPort.findByExternalDownloadId(event.downloadId()))
                    .willReturn(Optional.of(outbox));
            given(messageManager.publishFromEvent(event)).willReturn(false);

            // when
            listener.handleExternalDownloadRegistered(event);

            // then
            verify(outboxManager).markAsFailed(outbox);
            verify(outboxManager, never()).markAsPublished(any());
        }

        @Test
        @DisplayName("SQS 발행 중 예외 발생 시 Outbox를 failed 상태로 변경")
        void shouldMarkOutboxAsFailedOnException() {
            // given
            ExternalDownloadRegisteredEvent event = createEvent(4L);
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.unpublishedOutbox();

            given(outboxQueryPort.findByExternalDownloadId(event.downloadId()))
                    .willReturn(Optional.of(outbox));
            given(messageManager.publishFromEvent(event))
                    .willThrow(new RuntimeException("SQS connection failed"));

            // when
            listener.handleExternalDownloadRegistered(event);

            // then
            verify(outboxManager).markAsFailed(outbox);
            verify(outboxManager, never()).markAsPublished(any());
        }

        @Test
        @DisplayName("Outbox가 존재하지 않으면 처리를 중단한다")
        void shouldStopProcessingWhenOutboxNotFound() {
            // given
            ExternalDownloadRegisteredEvent event = createEvent(999L);

            given(outboxQueryPort.findByExternalDownloadId(event.downloadId()))
                    .willReturn(Optional.empty());

            // when
            listener.handleExternalDownloadRegistered(event);

            // then
            verify(outboxQueryPort).findByExternalDownloadId(event.downloadId());
            verify(messageManager, never()).publishFromEvent(any());
            verify(outboxManager, never()).markAsPublished(any());
            verify(outboxManager, never()).markAsFailed(any());
        }

        @Test
        @DisplayName("이벤트 처리 시 Outbox 조회가 먼저 수행된다")
        void shouldQueryOutboxBeforePublishing() {
            // given
            ExternalDownloadRegisteredEvent event = createEvent(1L);
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.unpublishedOutbox();

            given(outboxQueryPort.findByExternalDownloadId(event.downloadId()))
                    .willReturn(Optional.of(outbox));
            given(messageManager.publishFromEvent(event)).willReturn(true);

            // when
            listener.handleExternalDownloadRegistered(event);

            // then
            verify(outboxQueryPort).findByExternalDownloadId(event.downloadId());
            verify(messageManager).publishFromEvent(event);
        }

        @Test
        @DisplayName("여러 이벤트를 순차적으로 처리할 수 있다")
        void shouldHandleMultipleEventsSequentially() {
            // given
            ExternalDownloadRegisteredEvent event1 = createEvent(1L);
            ExternalDownloadRegisteredEvent event2 = createEvent(2L);
            ExternalDownloadOutbox outbox1 = ExternalDownloadOutboxFixture.unpublishedOutbox();
            ExternalDownloadOutbox outbox2 = ExternalDownloadOutboxFixture.unpublishedOutbox();

            given(outboxQueryPort.findByExternalDownloadId(event1.downloadId()))
                    .willReturn(Optional.of(outbox1));
            given(outboxQueryPort.findByExternalDownloadId(event2.downloadId()))
                    .willReturn(Optional.of(outbox2));
            given(messageManager.publishFromEvent(event1)).willReturn(true);
            given(messageManager.publishFromEvent(event2)).willReturn(true);

            // when
            listener.handleExternalDownloadRegistered(event1);
            listener.handleExternalDownloadRegistered(event2);

            // then
            verify(messageManager).publishFromEvent(event1);
            verify(messageManager).publishFromEvent(event2);
            verify(outboxManager).markAsPublished(outbox1);
            verify(outboxManager).markAsPublished(outbox2);
        }
    }

    // ==================== Helper Methods ====================

    private ExternalDownloadRegisteredEvent createEvent(Long downloadId) {
        return ExternalDownloadRegisteredEvent.of(
                ExternalDownloadId.of(downloadId),
                SourceUrl.of("https://example.com/file" + downloadId + ".jpg"),
                1L,
                100L,
                null,
                Instant.now());
    }
}
