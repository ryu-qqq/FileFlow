package com.ryuqq.fileflow.domain.session.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SessionErrorCode Enum Tests")
class SessionErrorCodeTest {

    @Test
    @DisplayName("FILE_SIZE_EXCEEDED의 code, message, httpStatus를 올바르게 반환해야 한다")
    void shouldReturnCorrectValuesForFileSizeExceeded() {
        SessionErrorCode errorCode = SessionErrorCode.FILE_SIZE_EXCEEDED;

        assertThat(errorCode.getCode()).isEqualTo("FILE-SIZE-EXCEEDED");
        assertThat(errorCode.getMessage()).isEqualTo("파일 크기가 최대 허용 크기를 초과했습니다");
        assertThat(errorCode.getHttpStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("UNSUPPORTED_FILE_TYPE의 code, message, httpStatus를 올바르게 반환해야 한다")
    void shouldReturnCorrectValuesForUnsupportedFileType() {
        SessionErrorCode errorCode = SessionErrorCode.UNSUPPORTED_FILE_TYPE;

        assertThat(errorCode.getCode()).isEqualTo("UNSUPPORTED-FILE-TYPE");
        assertThat(errorCode.getMessage()).isEqualTo("지원하지 않는 파일 타입입니다");
        assertThat(errorCode.getHttpStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("INVALID_SESSION_STATUS의 code, message, httpStatus를 올바르게 반환해야 한다")
    void shouldReturnCorrectValuesForInvalidSessionStatus() {
        SessionErrorCode errorCode = SessionErrorCode.INVALID_SESSION_STATUS;

        assertThat(errorCode.getCode()).isEqualTo("INVALID-SESSION-STATUS");
        assertThat(errorCode.getMessage()).isEqualTo("세션 상태 전환이 불가능합니다");
        assertThat(errorCode.getHttpStatus()).isEqualTo(409);
    }

    @Test
    @DisplayName("SESSION_EXPIRED의 code, message, httpStatus를 올바르게 반환해야 한다")
    void shouldReturnCorrectValuesForSessionExpired() {
        SessionErrorCode errorCode = SessionErrorCode.SESSION_EXPIRED;

        assertThat(errorCode.getCode()).isEqualTo("SESSION-EXPIRED");
        assertThat(errorCode.getMessage()).isEqualTo("세션이 만료되었습니다");
        assertThat(errorCode.getHttpStatus()).isEqualTo(410);
    }

    @Test
    @DisplayName("모든 ErrorCode는 ErrorCode 인터페이스를 구현해야 한다")
    void shouldImplementErrorCodeInterface() {
        assertThat(SessionErrorCode.FILE_SIZE_EXCEEDED).isInstanceOf(com.ryuqq.fileflow.domain.common.exception.ErrorCode.class);
        assertThat(SessionErrorCode.UNSUPPORTED_FILE_TYPE).isInstanceOf(com.ryuqq.fileflow.domain.common.exception.ErrorCode.class);
        assertThat(SessionErrorCode.INVALID_SESSION_STATUS).isInstanceOf(com.ryuqq.fileflow.domain.common.exception.ErrorCode.class);
        assertThat(SessionErrorCode.SESSION_EXPIRED).isInstanceOf(com.ryuqq.fileflow.domain.common.exception.ErrorCode.class);
    }
}

