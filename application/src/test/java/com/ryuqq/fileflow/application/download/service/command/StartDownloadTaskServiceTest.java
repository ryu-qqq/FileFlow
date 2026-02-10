package com.ryuqq.fileflow.application.download.service.command;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.fileflow.application.download.internal.DownloadExecutionCoordinator;
import com.ryuqq.fileflow.application.download.validator.DownloadTaskValidator;
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
@DisplayName("StartDownloadTaskService 단위 테스트")
class StartDownloadTaskServiceTest {

    @InjectMocks private StartDownloadTaskService sut;
    @Mock private DownloadTaskValidator downloadTaskValidator;
    @Mock private DownloadExecutionCoordinator downloadExecutionCoordinator;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("다운로드 태스크를 검증하고 실행 코디네이터에 위임한다")
        void execute_ValidTaskId_ValidatesAndDelegatesToCoordinator() {
            // given
            String downloadTaskId = "download-001";
            DownloadTask downloadTask = DownloadTaskFixture.aQueuedTask();

            given(downloadTaskValidator.getExistingTask(downloadTaskId)).willReturn(downloadTask);

            // when
            sut.execute(downloadTaskId);

            // then
            then(downloadTaskValidator).should().getExistingTask(downloadTaskId);
            then(downloadExecutionCoordinator).should().execute(downloadTask);
        }
    }
}
