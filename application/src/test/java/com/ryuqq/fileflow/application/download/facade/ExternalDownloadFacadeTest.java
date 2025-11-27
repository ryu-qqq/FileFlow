package com.ryuqq.fileflow.application.download.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

import com.ryuqq.fileflow.application.download.dto.ExternalDownloadBundle;
import com.ryuqq.fileflow.application.download.manager.ExternalDownloadManager;
import com.ryuqq.fileflow.application.download.manager.ExternalDownloadOutboxManager;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.event.ExternalDownloadRegisteredEvent;
import com.ryuqq.fileflow.domain.download.fixture.ExternalDownloadFixture;
import com.ryuqq.fileflow.domain.download.fixture.ExternalDownloadOutboxFixture;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadOutboxId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalDownloadFacade 테스트")
class ExternalDownloadFacadeTest {

    @Mock private ExternalDownloadManager externalDownloadManager;

    @Mock private ExternalDownloadOutboxManager externalDownloadOutboxManager;

    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private ExternalDownloadFacade facade;

    @Nested
    @DisplayName("saveAndPublishEvent 메서드")
    class SaveAndPublishEventTest {

        @Test
        @DisplayName("Bundle 저장 시 순서대로 처리되고 이벤트가 발행된다")
        void shouldSaveInOrderAndPublishEvent() {
            // given
            ExternalDownload download = ExternalDownloadFixture.pendingDownloadWithWebhook();
            // 이벤트 등록 (Assembler 역할 시뮬레이션)
            download.registerEvent(download.createRegisteredEvent());

            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.defaultOutbox();
            ExternalDownloadBundle bundle = new ExternalDownloadBundle(download, outbox);

            ExternalDownloadId downloadId = ExternalDownloadId.of(1L);
            ExternalDownloadOutboxId outboxId = ExternalDownloadOutboxId.of(1L);

            given(externalDownloadManager.save(download)).willReturn(downloadId);
            given(externalDownloadOutboxManager.save(outbox)).willReturn(outboxId);

            // when
            ExternalDownloadId result = facade.saveAndPublishEvent(bundle);

            // then
            assertThat(result).isEqualTo(downloadId);

            InOrder inOrder =
                    inOrder(externalDownloadManager, externalDownloadOutboxManager, eventPublisher);
            inOrder.verify(externalDownloadManager).save(download);
            inOrder.verify(externalDownloadOutboxManager).save(outbox);
            inOrder.verify(eventPublisher).publishEvent(any(ExternalDownloadRegisteredEvent.class));
        }

        @Test
        @DisplayName("저장 후 도메인 이벤트가 클리어된다")
        void shouldClearDomainEventsAfterPublishing() {
            // given
            ExternalDownload download = ExternalDownloadFixture.pendingDownloadWithWebhook();
            // 이벤트 등록 (Assembler 역할 시뮬레이션)
            download.registerEvent(download.createRegisteredEvent());

            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.defaultOutbox();
            ExternalDownloadBundle bundle = new ExternalDownloadBundle(download, outbox);

            ExternalDownloadId downloadId = ExternalDownloadId.of(1L);
            ExternalDownloadOutboxId outboxId = ExternalDownloadOutboxId.of(1L);

            given(externalDownloadManager.save(download)).willReturn(downloadId);
            given(externalDownloadOutboxManager.save(outbox)).willReturn(outboxId);

            // when
            facade.saveAndPublishEvent(bundle);

            // then
            assertThat(download.getDomainEvents()).isEmpty();
        }
    }
}
