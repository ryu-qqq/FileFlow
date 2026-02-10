package com.ryuqq.fileflow.application.download.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.download.assembler.DownloadAssembler;
import com.ryuqq.fileflow.application.download.dto.command.CreateDownloadTaskCommand;
import com.ryuqq.fileflow.application.download.dto.response.DownloadTaskResponse;
import com.ryuqq.fileflow.application.download.factory.command.DownloadCommandFactory;
import com.ryuqq.fileflow.application.download.manager.client.DownloadQueueManager;
import com.ryuqq.fileflow.application.download.manager.command.DownloadCommandManager;
import com.ryuqq.fileflow.domain.common.vo.AccessType;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTask;
import com.ryuqq.fileflow.domain.download.aggregate.DownloadTaskFixture;
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
    @Mock private DownloadQueueManager downloadQueueManager;
    @Mock private DownloadAssembler downloadAssembler;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("커맨드로 다운로드 태스크를 생성하고 큐에 등록한 후 응답을 반환한다")
        void execute_ValidCommand_CreatesAndEnqueuesAndReturnsResponse() {
            // given
            CreateDownloadTaskCommand command =
                    new CreateDownloadTaskCommand(
                            "https://example.com/image.jpg",
                            "public/2026/02/download-001.jpg",
                            "test-bucket",
                            AccessType.PUBLIC,
                            "product-image",
                            "commerce-service",
                            "https://callback.example.com/done");

            DownloadTask downloadTask = DownloadTaskFixture.aQueuedTask();
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
                            downloadTask.lastError(),
                            downloadTask.createdAt(),
                            downloadTask.startedAt(),
                            downloadTask.completedAt());

            given(downloadCommandFactory.create(command)).willReturn(downloadTask);
            given(downloadAssembler.toResponse(downloadTask)).willReturn(expectedResponse);

            // when
            DownloadTaskResponse result = sut.execute(command);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(downloadCommandFactory).should().create(command);
            then(downloadCommandManager).should().persist(downloadTask);
            then(downloadQueueManager).should().enqueue(downloadTask.idValue());
            then(downloadAssembler).should().toResponse(downloadTask);
        }
    }
}
