package com.ryuqq.fileflow.application.download.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.download.assembler.DownloadAssembler;
import com.ryuqq.fileflow.application.download.dto.response.DownloadTaskResponse;
import com.ryuqq.fileflow.application.download.manager.query.DownloadReadManager;
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
@DisplayName("GetDownloadTaskService 단위 테스트")
class GetDownloadTaskServiceTest {

    @InjectMocks private GetDownloadTaskService sut;
    @Mock private DownloadReadManager downloadReadManager;
    @Mock private DownloadAssembler downloadAssembler;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("다운로드 태스크 ID로 태스크를 조회하고 응답으로 변환한다")
        void execute_ValidTaskId_ReturnsDownloadTaskResponse() {
            // given
            String downloadTaskId = "download-001";
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

            given(downloadReadManager.getDownloadTask(downloadTaskId)).willReturn(downloadTask);
            given(downloadAssembler.toResponse(downloadTask)).willReturn(expectedResponse);

            // when
            DownloadTaskResponse result = sut.execute(downloadTaskId);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(downloadReadManager).should().getDownloadTask(downloadTaskId);
            then(downloadAssembler).should().toResponse(downloadTask);
        }
    }
}
