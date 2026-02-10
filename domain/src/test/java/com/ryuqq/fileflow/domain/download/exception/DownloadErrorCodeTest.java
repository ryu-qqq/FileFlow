package com.ryuqq.fileflow.domain.download.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.fileflow.domain.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DownloadErrorCode")
class DownloadErrorCodeTest {

    @Test
    @DisplayName("DOWNLOAD_TASK_NOT_FOUND: code=DOWNLOAD-001, httpStatus=404")
    void downloadTaskNotFound() {
        DownloadErrorCode code = DownloadErrorCode.DOWNLOAD_TASK_NOT_FOUND;

        assertThat(code.getCode()).isEqualTo("DOWNLOAD-001");
        assertThat(code.getHttpStatus()).isEqualTo(404);
        assertThat(code.getMessage()).isEqualTo("다운로드 작업을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("DOWNLOAD_ALREADY_COMPLETED: code=DOWNLOAD-002, httpStatus=409")
    void downloadAlreadyCompleted() {
        DownloadErrorCode code = DownloadErrorCode.DOWNLOAD_ALREADY_COMPLETED;

        assertThat(code.getCode()).isEqualTo("DOWNLOAD-002");
        assertThat(code.getHttpStatus()).isEqualTo(409);
        assertThat(code.getMessage()).isEqualTo("이미 완료된 다운로드 작업입니다");
    }

    @Test
    @DisplayName("DOWNLOAD_MAX_RETRIES_EXCEEDED: code=DOWNLOAD-003, httpStatus=422")
    void downloadMaxRetriesExceeded() {
        DownloadErrorCode code = DownloadErrorCode.DOWNLOAD_MAX_RETRIES_EXCEEDED;

        assertThat(code.getCode()).isEqualTo("DOWNLOAD-003");
        assertThat(code.getHttpStatus()).isEqualTo(422);
        assertThat(code.getMessage()).isEqualTo("최대 재시도 횟수를 초과했습니다");
    }

    @Test
    @DisplayName("INVALID_DOWNLOAD_STATUS: code=DOWNLOAD-004, httpStatus=400")
    void invalidDownloadStatus() {
        DownloadErrorCode code = DownloadErrorCode.INVALID_DOWNLOAD_STATUS;

        assertThat(code.getCode()).isEqualTo("DOWNLOAD-004");
        assertThat(code.getHttpStatus()).isEqualTo(400);
        assertThat(code.getMessage()).isEqualTo("유효하지 않은 다운로드 상태입니다");
    }

    @Test
    @DisplayName("모든 값이 ErrorCode 인터페이스를 구현한다")
    void implementsErrorCode() {
        for (DownloadErrorCode code : DownloadErrorCode.values()) {
            assertThat(code).isInstanceOf(ErrorCode.class);
            assertThat(code.getCode()).isNotBlank();
            assertThat(code.getHttpStatus()).isPositive();
            assertThat(code.getMessage()).isNotBlank();
        }
    }
}
