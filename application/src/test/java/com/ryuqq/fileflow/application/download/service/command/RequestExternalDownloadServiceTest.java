package com.ryuqq.fileflow.application.download.service.command;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.ryuqq.fileflow.application.download.dto.ExternalDownloadBundle;
import com.ryuqq.fileflow.application.download.dto.command.RequestExternalDownloadCommand;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadResponse;
import com.ryuqq.fileflow.application.download.facade.ExternalDownloadFacade;
import com.ryuqq.fileflow.application.download.factory.command.ExternalDownloadCommandFactory;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import com.ryuqq.fileflow.domain.download.fixture.ExternalDownloadFixture;
import com.ryuqq.fileflow.domain.download.fixture.ExternalDownloadOutboxFixture;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadId;
import com.ryuqq.fileflow.domain.download.vo.ExternalDownloadStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("RequestExternalDownloadService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class RequestExternalDownloadServiceTest {

    private static final String SOURCE_URL = "https://example.com/image.jpg";
    private static final String TENANT_ID = "01912345-6789-7abc-def0-123456789001";
    private static final String ORGANIZATION_ID = "01912345-6789-7abc-def0-123456789100";
    private static final String WEBHOOK_URL = "https://callback.example.com/webhook";

    @Mock private ExternalDownloadCommandFactory commandFactory;

    @Mock private ExternalDownloadFacade facade;

    private RequestExternalDownloadService service;

    @BeforeEach
    void setUp() {
        service = new RequestExternalDownloadService(commandFactory, facade);
    }

    @Nested
    @DisplayName("execute 테스트")
    class ExecuteTest {

        @Test
        @DisplayName("외부 다운로드 요청 시 CommandFactory와 Facade를 통해 처리되고 ID가 반환된다")
        void execute_ShouldUseCommandFactoryAndFacadeAndReturnId() {
            // given
            RequestExternalDownloadCommand command =
                    new RequestExternalDownloadCommand(
                            SOURCE_URL, TENANT_ID, ORGANIZATION_ID, null);

            ExternalDownload download = ExternalDownloadFixture.pendingDownload();
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.unpublishedOutbox();
            ExternalDownloadBundle bundle = new ExternalDownloadBundle(download, outbox);
            ExternalDownloadId savedId =
                    ExternalDownloadId.of("00000000-0000-0000-0000-000000000001");

            given(commandFactory.createBundle(command)).willReturn(bundle);
            given(facade.saveAndPublishEvent(bundle)).willReturn(savedId);

            // when
            ExternalDownloadResponse response = service.execute(command);

            // then
            assertThat(response.id()).isEqualTo("00000000-0000-0000-0000-000000000001");
            assertThat(response.status()).isEqualTo(ExternalDownloadStatus.PENDING.name());
            assertThat(response.createdAt()).isEqualTo(download.getCreatedAt());

            then(commandFactory).should().createBundle(command);
            then(facade).should().saveAndPublishEvent(bundle);
        }

        @Test
        @DisplayName("webhookUrl이 있는 경우 ExternalDownload에 설정된다")
        void execute_WithWebhookUrl_ShouldSetWebhookUrl() {
            // given
            RequestExternalDownloadCommand command =
                    new RequestExternalDownloadCommand(
                            SOURCE_URL, TENANT_ID, ORGANIZATION_ID, WEBHOOK_URL);

            ExternalDownload download = ExternalDownloadFixture.pendingDownloadWithWebhook();
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.unpublishedOutbox();
            ExternalDownloadBundle bundle = new ExternalDownloadBundle(download, outbox);
            ExternalDownloadId savedId =
                    ExternalDownloadId.of("00000000-0000-0000-0000-000000000001");

            given(commandFactory.createBundle(command)).willReturn(bundle);
            given(facade.saveAndPublishEvent(bundle)).willReturn(savedId);

            // when
            service.execute(command);

            // then
            then(commandFactory).should().createBundle(command);
            then(facade).should().saveAndPublishEvent(bundle);
        }

        @Test
        @DisplayName("CommandFactory에서 예외 발생 시 전파된다")
        void execute_WhenCommandFactoryThrows_ShouldPropagate() {
            // given
            RequestExternalDownloadCommand command =
                    new RequestExternalDownloadCommand(
                            "not-a-valid-url", TENANT_ID, ORGANIZATION_ID, null);

            given(commandFactory.createBundle(command))
                    .willThrow(new IllegalArgumentException("Invalid URL"));

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid URL");
        }

        @Test
        @DisplayName("Facade에서 예외 발생 시 전파된다")
        void execute_WhenFacadeThrows_ShouldPropagate() {
            // given
            RequestExternalDownloadCommand command =
                    new RequestExternalDownloadCommand(
                            SOURCE_URL, TENANT_ID, ORGANIZATION_ID, null);

            ExternalDownload download = ExternalDownloadFixture.pendingDownload();
            ExternalDownloadOutbox outbox = ExternalDownloadOutboxFixture.unpublishedOutbox();
            ExternalDownloadBundle bundle = new ExternalDownloadBundle(download, outbox);

            given(commandFactory.createBundle(command)).willReturn(bundle);
            given(facade.saveAndPublishEvent(bundle))
                    .willThrow(new RuntimeException("Database error"));

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database error");
        }
    }
}
