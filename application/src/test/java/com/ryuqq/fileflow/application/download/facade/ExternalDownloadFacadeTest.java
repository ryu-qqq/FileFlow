package com.ryuqq.fileflow.application.download.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

import com.ryuqq.fileflow.application.common.config.TransactionEventRegistry;
import com.ryuqq.fileflow.application.download.dto.ExternalDownloadBundle;
import com.ryuqq.fileflow.application.download.manager.command.ExternalDownloadOutboxTransactionManager;
import com.ryuqq.fileflow.application.download.manager.command.ExternalDownloadTransactionManager;
import com.ryuqq.fileflow.domain.common.event.DomainEvent;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalDownloadFacade 테스트")
class ExternalDownloadFacadeTest {

    @Mock private ExternalDownloadTransactionManager externalDownloadTransactionManager;

    @Mock private ExternalDownloadOutboxTransactionManager outboxTransactionManager;

    @Mock private TransactionEventRegistry transactionEventRegistry;

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

            ExternalDownload savedDownload = ExternalDownloadFixture.pendingExternalDownload();
            ExternalDownloadOutboxId outboxId =
                    ExternalDownloadOutboxId.of("00000000-0000-0000-0000-000000000001");

            given(externalDownloadTransactionManager.persist(download)).willReturn(savedDownload);
            given(outboxTransactionManager.persist(outbox)).willReturn(outboxId);

            // when
            ExternalDownloadId result = facade.saveAndPublishEvent(bundle);

            // then
            assertThat(result).isEqualTo(savedDownload.getId());

            InOrder inOrder =
                    inOrder(
                            transactionEventRegistry,
                            externalDownloadTransactionManager,
                            outboxTransactionManager);
            inOrder.verify(transactionEventRegistry).registerForPublish(any(DomainEvent.class));
            inOrder.verify(externalDownloadTransactionManager).persist(download);
            inOrder.verify(outboxTransactionManager).persist(outbox);
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

            ExternalDownload savedDownload = ExternalDownloadFixture.pendingExternalDownload();
            ExternalDownloadOutboxId outboxId =
                    ExternalDownloadOutboxId.of("00000000-0000-0000-0000-000000000001");

            given(externalDownloadTransactionManager.persist(download)).willReturn(savedDownload);
            given(outboxTransactionManager.persist(outbox)).willReturn(outboxId);

            // when
            facade.saveAndPublishEvent(bundle);

            // then
            assertThat(download.getDomainEvents()).isEmpty();
        }
    }
}
