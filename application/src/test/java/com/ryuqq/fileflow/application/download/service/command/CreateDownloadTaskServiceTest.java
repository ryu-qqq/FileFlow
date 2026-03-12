package com.ryuqq.fileflow.application.download.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.download.assembler.DownloadAssembler;
import com.ryuqq.fileflow.application.download.dto.command.CreateDownloadTaskCommand;
import com.ryuqq.fileflow.application.download.dto.response.DownloadTaskResponse;
import com.ryuqq.fileflow.application.download.factory.command.DownloadCommandFactory;
import com.ryuqq.fileflow.application.download.manager.cache.DownloadUrlBlacklistManager;
import com.ryuqq.fileflow.application.download.manager.command.DownloadCommandManager;
import com.ryuqq.fileflow.application.download.manager.command.DownloadQueueOutboxCommandManager;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadQueueOutbox;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTaskFixture;
import com.ryuqq.fileflow.domain.download.exception.DownloadException;
import com.ryuqq.fileflow.domain.download.id.DownloadQueueOutboxId;
import java.time.Instant;
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
@DisplayName("CreateDownloadTaskService 단위 테스트")
class CreateDownloadTaskServiceTest {

    @InjectMocks private CreateDownloadTaskService sut;
    @Mock private DownloadCommandFactory downloadCommandFactory;
    @Mock private DownloadCommandManager downloadCommandManager;
    @Mock private DownloadQueueOutboxCommandManager downloadQueueOutboxCommandManager;
    @Mock private DownloadAssembler downloadAssembler;
    @Mock private DownloadUrlBlacklistManager downloadUrlBlacklistManager;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("커맨드로 다운로드 태스크를 생성하고 아웃박스에 기록한 후 응답을 반환한다")
        void execute_ValidCommand_CreatesTaskAndOutboxAndReturnsResponse() {
            // given
            CreateDownloadTaskCommand command =
                    new CreateDownloadTaskCommand(
                            "https://example.com/image.jpg",
                            AccessType.PUBLIC,
                            "product-image",
                            "commerce-service",
                            "https://callback.example.com/done");

            DownloadTask downloadTask = DownloadTaskFixture.aQueuedTask();
            DownloadQueueOutbox outbox =
                    DownloadQueueOutbox.forNew(
                            DownloadQueueOutboxId.of("outbox-001"),
                            downloadTask.idValue(),
                            Instant.now());
            DownloadTaskResponse expectedResponse =
                    new DownloadTaskResponse(
                            downloadTask.idValue(),
                            downloadTask.sourceUrlValue(),
                            downloadTask.s3Key(),
                            downloadTask.bucket(),
                            downloadTask.accessType(),
                            downloadTask.purpose(),
                            downloadTask.source(),
                            downloadTask.status().name(),
                            downloadTask.retryCount(),
                            downloadTask.maxRetries(),
                            downloadTask.callbackUrl(),
                            downloadTask.assetId(),
                            downloadTask.lastError(),
                            downloadTask.createdAt(),
                            downloadTask.startedAt(),
                            downloadTask.completedAt());

            given(downloadUrlBlacklistManager.isBlacklisted(command.sourceUrl())).willReturn(false);
            given(downloadCommandFactory.create(command)).willReturn(downloadTask);
            given(downloadCommandFactory.createQueueOutbox(downloadTask.idValue()))
                    .willReturn(outbox);
            given(downloadAssembler.toResponse(downloadTask)).willReturn(expectedResponse);

            // when
            DownloadTaskResponse result = sut.execute(command);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(downloadCommandFactory).should().create(command);
            then(downloadCommandManager).should().persist(downloadTask);
            then(downloadCommandFactory).should().createQueueOutbox(downloadTask.idValue());
            then(downloadQueueOutboxCommandManager)
                    .should()
                    .persist(any(DownloadQueueOutbox.class));
            then(downloadAssembler).should().toResponse(downloadTask);
        }

        @Test
        @DisplayName("블랙리스트에 등록된 URL이면 DownloadException을 던진다")
        void execute_BlacklistedUrl_ThrowsException() {
            // given
            CreateDownloadTaskCommand command =
                    new CreateDownloadTaskCommand(
                            "https://cdn.set-of.com/logo/setof-logo.png",
                            AccessType.PUBLIC,
                            "product-image",
                            "commerce-service",
                            null);

            given(downloadUrlBlacklistManager.isBlacklisted(command.sourceUrl())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> sut.execute(command)).isInstanceOf(DownloadException.class);

            then(downloadCommandFactory).shouldHaveNoInteractions();
            then(downloadCommandManager).shouldHaveNoInteractions();
        }
    }
}
