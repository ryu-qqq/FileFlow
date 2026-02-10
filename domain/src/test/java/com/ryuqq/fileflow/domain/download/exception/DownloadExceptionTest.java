package com.ryuqq.fileflow.domain.download.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.exception.DomainException;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("DownloadException")
class DownloadExceptionTest {

    @Nested
    @DisplayName("ErrorCode만으로 생성")
    class CreateWithErrorCodeOnly {

        @Test
        @DisplayName("ErrorCode의 message가 예외 메시지로 사용된다")
        void usesErrorCodeMessage() {
            DownloadException exception =
                    new DownloadException(DownloadErrorCode.INVALID_DOWNLOAD_STATUS);

            assertThat(exception.getMessage()).isEqualTo("유효하지 않은 다운로드 상태입니다");
            assertThat(exception.code()).isEqualTo("DOWNLOAD-004");
            assertThat(exception.httpStatus()).isEqualTo(400);
            assertThat(exception.args()).isEmpty();
        }
    }

    @Nested
    @DisplayName("ErrorCode + 커스텀 메시지로 생성")
    class CreateWithCustomMessage {

        @Test
        @DisplayName("커스텀 메시지가 예외 메시지로 사용된다")
        void usesCustomMessage() {
            String customMessage = "Cannot start download in status: DOWNLOADING";

            DownloadException exception =
                    new DownloadException(DownloadErrorCode.INVALID_DOWNLOAD_STATUS, customMessage);

            assertThat(exception.getMessage()).isEqualTo(customMessage);
            assertThat(exception.code()).isEqualTo("DOWNLOAD-004");
            assertThat(exception.httpStatus()).isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("ErrorCode + 커스텀 메시지 + args로 생성")
    class CreateWithArgs {

        @Test
        @DisplayName("args가 포함된 예외를 생성한다")
        void createsWithArgs() {
            Map<String, Object> args = Map.of("taskId", "download-001");

            DownloadException exception =
                    new DownloadException(
                            DownloadErrorCode.DOWNLOAD_TASK_NOT_FOUND, "다운로드 작업을 찾을 수 없습니다", args);

            assertThat(exception.code()).isEqualTo("DOWNLOAD-001");
            assertThat(exception.httpStatus()).isEqualTo(404);
            assertThat(exception.args()).containsEntry("taskId", "download-001");
        }
    }

    @Nested
    @DisplayName("상속 관계")
    class Inheritance {

        @Test
        @DisplayName("DomainException을 상속한다")
        void extendsDomainException() {
            DownloadException exception =
                    new DownloadException(DownloadErrorCode.INVALID_DOWNLOAD_STATUS);

            assertThat(exception).isInstanceOf(DomainException.class);
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }
}
