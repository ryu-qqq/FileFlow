package com.ryuqq.fileflow.domain.session.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ryuqq.fileflow.domain.common.exception.DomainException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UnsupportedFileTypeException Tests")
class UnsupportedFileTypeExceptionTest {

    @Test
    @DisplayName("mimeType으로 예외를 생성할 수 있어야 한다")
    void shouldCreateExceptionWithCorrectMessage() {
        // given
        String mimeType = "application/pdf";

        // when
        UnsupportedFileTypeException exception = new UnsupportedFileTypeException(mimeType);

        // then
        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception.getMessage()).contains("지원하지 않는 파일 타입입니다");
        assertThat(exception.getMessage()).contains("요청: application/pdf");
        assertThat(exception.getMessage()).contains("허용: image/*, text/html");
        assertThat(exception.errorCode()).isEqualTo(SessionErrorCode.UNSUPPORTED_FILE_TYPE);
    }

    @Test
    @DisplayName("HTTP 상태 코드는 400을 반환해야 한다")
    void shouldReturnHttpStatus400() {
        // given
        String mimeType = "video/mp4";

        // when
        UnsupportedFileTypeException exception = new UnsupportedFileTypeException(mimeType);

        // then
        assertThat(exception.httpStatus()).isEqualTo(400);
        assertThat(exception.code()).isEqualTo("UNSUPPORTED-FILE-TYPE");
    }
}

