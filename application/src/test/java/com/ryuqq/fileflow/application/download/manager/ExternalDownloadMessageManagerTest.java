package com.ryuqq.fileflow.application.download.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.fileflow.application.download.dto.ExternalDownloadMessage;
import com.ryuqq.fileflow.application.download.port.out.client.SqsPublishPort;
import com.ryuqq.fileflow.domain.download.event.ExternalDownloadRegisteredEvent;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.SourceUrl;
import com.ryuqq.fileflow.domain.download.vo.WebhookUrl;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalDownloadMessageManager 테스트")
class ExternalDownloadMessageManagerTest {

    @Mock private SqsPublishPort sqsPublishPort;

    @InjectMocks private ExternalDownloadMessageManager manager;

    @Nested
    @DisplayName("publishFromEvent 메서드")
    class PublishFromEventTest {

        @Test
        @DisplayName("이벤트로부터 SQS 메시지를 생성하고 발행한다")
        void shouldPublishMessageFromEvent() {
            // given
            ExternalDownloadRegisteredEvent event =
                    createEvent(
                            "00000000-0000-0000-0000-000000000001",
                            "https://example.com/image.jpg");

            given(sqsPublishPort.publish(any(ExternalDownloadMessage.class))).willReturn(true);

            // when
            boolean result = manager.publishFromEvent(event);

            // then
            assertThat(result).isTrue();
            verify(sqsPublishPort).publish(any(ExternalDownloadMessage.class));
        }

        @Test
        @DisplayName("메시지 발행 성공 시 true를 반환한다")
        void shouldReturnTrueOnSuccess() {
            // given
            ExternalDownloadRegisteredEvent event =
                    createEvent(
                            "00000000-0000-0000-0000-000000000002", "https://example.com/test.png");

            given(sqsPublishPort.publish(any(ExternalDownloadMessage.class))).willReturn(true);

            // when
            boolean result = manager.publishFromEvent(event);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("메시지 발행 실패 시 false를 반환한다")
        void shouldReturnFalseOnFailure() {
            // given
            ExternalDownloadRegisteredEvent event =
                    createEvent(
                            "00000000-0000-0000-0000-000000000003", "https://example.com/fail.jpg");

            given(sqsPublishPort.publish(any(ExternalDownloadMessage.class))).willReturn(false);

            // when
            boolean result = manager.publishFromEvent(event);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("이벤트 정보가 메시지로 올바르게 변환된다")
        void shouldConvertEventToMessageCorrectly() {
            // given
            String downloadId = "00000000-0000-0000-0000-000000000064";
            String sourceUrl = "https://example.com/convert-test.jpg";
            long tenantId = 1L;
            long organizationId = 200L;

            ExternalDownloadRegisteredEvent event =
                    ExternalDownloadRegisteredEvent.of(
                            ExternalDownloadId.of(downloadId),
                            SourceUrl.of(sourceUrl),
                            tenantId,
                            organizationId,
                            null,
                            Instant.now());

            given(sqsPublishPort.publish(any(ExternalDownloadMessage.class))).willReturn(true);

            // when
            manager.publishFromEvent(event);

            // then
            ArgumentCaptor<ExternalDownloadMessage> captor =
                    ArgumentCaptor.forClass(ExternalDownloadMessage.class);
            verify(sqsPublishPort).publish(captor.capture());

            ExternalDownloadMessage message = captor.getValue();
            assertThat(message.externalDownloadId()).isEqualTo(downloadId);
            assertThat(message.sourceUrl()).isEqualTo(sourceUrl);
            assertThat(message.tenantId()).isEqualTo(tenantId);
            assertThat(message.organizationId()).isEqualTo(organizationId);
        }

        @Test
        @DisplayName("WebhookUrl이 포함된 이벤트도 정상적으로 발행된다")
        void shouldPublishEventWithWebhookUrl() {
            // given
            ExternalDownloadRegisteredEvent event =
                    ExternalDownloadRegisteredEvent.of(
                            ExternalDownloadId.of("00000000-0000-0000-0000-000000000005"),
                            SourceUrl.of("https://example.com/webhook-test.jpg"),
                            1L,
                            100L,
                            WebhookUrl.of("https://callback.example.com/webhook"),
                            Instant.now());

            given(sqsPublishPort.publish(any(ExternalDownloadMessage.class))).willReturn(true);

            // when
            boolean result = manager.publishFromEvent(event);

            // then
            assertThat(result).isTrue();
            verify(sqsPublishPort).publish(any(ExternalDownloadMessage.class));
        }
    }

    private ExternalDownloadRegisteredEvent createEvent(String downloadId, String sourceUrl) {
        return ExternalDownloadRegisteredEvent.of(
                ExternalDownloadId.of(downloadId),
                SourceUrl.of(sourceUrl),
                1L,
                100L,
                null,
                Instant.now());
    }
}
