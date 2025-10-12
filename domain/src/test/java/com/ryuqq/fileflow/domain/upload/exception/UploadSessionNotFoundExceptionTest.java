package com.ryuqq.fileflow.domain.upload.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UploadSessionNotFoundException 테스트")
class UploadSessionNotFoundExceptionTest {

    @Test
    @DisplayName("기본 메시지로 예외를 생성한다")
    void createExceptionWithDefaultMessage() {
        // when
        UploadSessionNotFoundException exception = new UploadSessionNotFoundException();

        // then
        assertThat(exception.getMessage()).isEqualTo("Upload session not found");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("세션 ID를 포함한 메시지로 예외를 생성한다")
    void createExceptionWithSessionId() {
        // given
        String sessionId = "session-123";

        // when
        UploadSessionNotFoundException exception = new UploadSessionNotFoundException(sessionId);

        // then
        assertThat(exception.getMessage()).isEqualTo("Upload session not found: session-123");
    }

    @Test
    @DisplayName("메시지와 원인을 포함한 예외를 생성한다")
    void createExceptionWithMessageAndCause() {
        // given
        String message = "Custom error message";
        Throwable cause = new RuntimeException("Original cause");

        // when
        UploadSessionNotFoundException exception = new UploadSessionNotFoundException(message, cause);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Original cause");
    }

    @Test
    @DisplayName("null 세션 ID로 예외를 생성해도 NPE가 발생하지 않는다")
    void createExceptionWithNullSessionId() {
        // when
        UploadSessionNotFoundException exception = new UploadSessionNotFoundException((String) null);

        // then
        assertThat(exception.getMessage()).isEqualTo("Upload session not found: null");
    }

    @Test
    @DisplayName("빈 세션 ID로 예외를 생성한다")
    void createExceptionWithEmptySessionId() {
        // when
        UploadSessionNotFoundException exception = new UploadSessionNotFoundException("");

        // then
        assertThat(exception.getMessage()).isEqualTo("Upload session not found: ");
    }
}
