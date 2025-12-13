package com.ryuqq.fileflow.application.download.service.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.ryuqq.fileflow.application.download.dto.command.ExecuteExternalDownloadCommand;
import com.ryuqq.fileflow.application.download.facade.ExternalDownloadProcessingFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExecuteExternalDownloadService 테스트")
class ExecuteExternalDownloadServiceTest {

    @Mock private ExternalDownloadProcessingFacade facade;

    @InjectMocks private ExecuteExternalDownloadService service;

    @Nested
    @DisplayName("execute 메서드")
    class ExecuteTest {

        @Test
        @DisplayName("정상적으로 다운로드를 실행한다")
        void shouldExecuteDownloadSuccessfully() {
            // given
            String downloadId = "00000000-0000-0000-0000-000000000001";
            ExecuteExternalDownloadCommand command = new ExecuteExternalDownloadCommand(downloadId);

            // when
            service.execute(command);

            // then
            verify(facade).process(downloadId);
        }

        @Test
        @DisplayName("Command의 externalDownloadId로 Facade를 호출한다")
        void shouldCallFacadeWithDownloadId() {
            // given
            String downloadId = "00000000-0000-0000-0000-0000000003e7";
            ExecuteExternalDownloadCommand command = new ExecuteExternalDownloadCommand(downloadId);

            // when
            service.execute(command);

            // then
            verify(facade).process(downloadId);
        }

        @Test
        @DisplayName("Facade에서 예외 발생 시 예외를 전파한다")
        void shouldPropagateExceptionFromFacade() {
            // given
            String downloadId = "00000000-0000-0000-0000-000000000001";
            ExecuteExternalDownloadCommand command = new ExecuteExternalDownloadCommand(downloadId);

            RuntimeException exception = new RuntimeException("Download failed");
            doThrow(exception).when(facade).process(downloadId);

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Download failed");

            verify(facade).process(downloadId);
        }

        @Test
        @DisplayName("IllegalStateException 발생 시 예외를 전파한다")
        void shouldPropagateIllegalStateException() {
            // given
            String downloadId = "00000000-0000-0000-0000-0000000003e7";
            ExecuteExternalDownloadCommand command = new ExecuteExternalDownloadCommand(downloadId);

            IllegalStateException exception =
                    new IllegalStateException("ExternalDownload not found: " + downloadId);
            doThrow(exception).when(facade).process(downloadId);

            // when & then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ExternalDownload not found");
        }

        @Test
        @DisplayName("여러 다운로드를 순차적으로 실행할 수 있다")
        void shouldExecuteMultipleDownloadsSequentially() {
            // given
            ExecuteExternalDownloadCommand command1 =
                    new ExecuteExternalDownloadCommand("00000000-0000-0000-0000-000000000001");
            ExecuteExternalDownloadCommand command2 =
                    new ExecuteExternalDownloadCommand("00000000-0000-0000-0000-000000000002");
            ExecuteExternalDownloadCommand command3 =
                    new ExecuteExternalDownloadCommand("00000000-0000-0000-0000-000000000003");

            // when
            service.execute(command1);
            service.execute(command2);
            service.execute(command3);

            // then
            verify(facade).process("00000000-0000-0000-0000-000000000001");
            verify(facade).process("00000000-0000-0000-0000-000000000002");
            verify(facade).process("00000000-0000-0000-0000-000000000003");
        }
    }
}
